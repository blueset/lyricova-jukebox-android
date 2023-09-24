package studio1a23.lyricovaJukebox.workers

import android.app.DownloadManager
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.database.Cursor
import android.icu.text.MessageFormat
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import okio.HashingSink
import okio.blackholeSink
import okio.buffer
import okio.source
import studio1a23.lyricovaJukebox.R
import studio1a23.lyricovaJukebox.data.SERVER_ROOT
import studio1a23.lyricovaJukebox.data.musicFile.MusicFileEntity
import studio1a23.lyricovaJukebox.data.musicFile.MusicFileEntityWithLyrics
import studio1a23.lyricovaJukebox.data.musicFile.MusicFileRepo
import studio1a23.lyricovaJukebox.data.preference.UserPreferencesRepo
import studio1a23.lyricovaJukebox.util.TAG
import java.io.File
import java.util.Date
import kotlin.properties.Delegates
import kotlin.random.Random

/** Tag for the sync worker. */
const val SYNC_WORKER_TAG = "SYNC_WORKER"

const val LYRICOVA_SUB_PATH = "Lyricova/"

const val SYNC_NOTIFICATION_CHANNEL = "LyricovaSync"

val downloadIds = mutableMapOf<Long, File>()

fun File.ensureFileAndDirExists() {
    val parent = this.parentFile
    if (parent !== null && !parent.exists()) {
        parent.mkdirs()
    }
    if (!this.exists()) {
        Log.d(TAG, "Creating file ${this.absolutePath}")
        this.createNewFile()
    }
}

private fun String.getExt(): String {
    val lastDot = this.lastIndexOf('.')
    return if (lastDot == -1) {
        ""
    } else {
        this.substring(lastDot + 1)
    }
}

private fun String.replaceExt(newExt: String): String {
    val lastDot = this.lastIndexOf('.')
    return if (lastDot == -1) {
        "$this.$newExt"
    } else {
        this.substring(0, lastDot) + ".$newExt"
    }
}

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val userPreferencesRepo: UserPreferencesRepo,
    private val musicFileRepo: MusicFileRepo,
) :
    CoroutineWorker(appContext, workerParams) {

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private var notificationId by Delegates.notNull<Int>()

    private val downloadManager = ContextCompat.getSystemService(
        applicationContext,
        DownloadManager::class.java
    )
        ?: throw Exception("Download manager not found")
    private val notificationManager = ContextCompat.getSystemService(
        applicationContext,
        NotificationManager::class.java
    )
        ?: throw Exception("Notification manager not found")

    override suspend fun doWork(): Result {
        return try {
            syncData()
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync data", e)
            cancelDownload(downloadIds)
            Result.failure()
        }
    }

    private suspend fun syncData() {
        sendNotification()

        val outcome = musicFileRepo.sync()

        val totalFilesToProcess = outcome.inserted.size + outcome.updated.size
        Log.d(TAG, "Processing $totalFilesToProcess files")
        var processed = 0

        try {
            for (entity in outcome.inserted + outcome.updated) {
                updateLyrics(entity)
                val expectedPath = getExpectedPath(entity.musicFileEntity)
                if (!ifFileMatchesMd5(expectedPath, entity.musicFileEntity.hash)) {
                    val downloadId = queueDownload(entity.musicFileEntity)
                    downloadIds[downloadId] = expectedPath
                }
                processed++
                updateNotificationProgress(
                    processed,
                    totalFilesToProcess,
                    MessageFormat.format(applicationContext.getString(R.string.sync_processing_changes_count), processed, totalFilesToProcess)
                )
            }

            val totalFilesToDownload = downloadIds.size
            userPreferencesRepo.updateLastSynced(Date())
            postDownload(downloadIds)
            completeNotification(totalFilesToProcess, totalFilesToDownload)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download files", e)
            cancelDownload(downloadIds)
            failureNotification(e)
        }
    }

    private fun cancelDownload(idToPathMap: MutableMap<Long, File>) {
        Log.d(TAG, "Cancelling ${idToPathMap.size} downloads...")
        if (idToPathMap.isNotEmpty()) {
            val removed = downloadManager.remove(*idToPathMap.keys.toLongArray())
            Log.d(TAG, "Cancelled $removed downloads")
        }
    }

    private fun getExpectedPath(entity: MusicFileEntity): File {
        val musicPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        return File(musicPath, LYRICOVA_SUB_PATH + entity.path)
    }

    private fun updateLyrics(entity: MusicFileEntityWithLyrics) {
        val expectedPath = getExpectedPath(entity.musicFileEntity)

        if (entity.lrc?.isNotBlank() == true) {
            val file = File(expectedPath.absolutePath.replaceExt("lrc"))
            file.ensureFileAndDirExists()
            if (file.readText() != entity.lrc) {
                Log.d(TAG, "Updating lyrics for ${entity.musicFileEntity.trackName}")
                file.writeText(entity.lrc)
            }
        }

        if (entity.lrcx?.isNotBlank() == true) {
            // Android is not recognizing .lrcx as a valid extension for shared media store
            val file = File(expectedPath.absolutePath.replaceExt("lrcx.lrc"))
            file.ensureFileAndDirExists()
            if (file.readText() != entity.lrcx) {
                Log.d(TAG, "Updating lyrics for ${entity.musicFileEntity.trackName}")
                file.writeText(entity.lrcx)
            }
        }
    }

    /**
     * Download a file from the server.
     * @return Download request ID.
     */
    private fun queueDownload(entity: MusicFileEntity): Long {
        val downloadUrl = "$SERVER_ROOT/api/files/${entity.id}/file"
        // val path = entity.path
        // Download to temporary path and force a rename
        // to prevent Android from removing the files when staled
        // https://medium.com/@mkaflowski/disappearing-files-downloaded-by-downloadmanager-7c9ee5c6a66a
        val tempFilename = "${entity.id}.${entity.path.getExt()}"

        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle(entity.trackName)
            .setDescription("${entity.artistName} / ${entity.albumName}")
            .setAllowedOverRoaming(false)
            .setAllowedOverMetered(false)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationInExternalPublicDir(
                // Download to `/Downloads` then move to music to prevent Android
                // from removing the files when staled
//                Environment.DIRECTORY_MUSIC,
                Environment.DIRECTORY_DOWNLOADS,
                "$LYRICOVA_SUB_PATH$tempFilename"
            )
        Log.d(TAG, "Downloading $tempFilename from $downloadUrl");
        return downloadManager.enqueue(request)
    }

    private fun ifFileMatchesMd5(file: File, md5: String): Boolean {
        if (!file.exists()) {
            return false
        }

        if (!file.canRead()) {
            Log.e(TAG, "File ${file.absolutePath} is not readable")
            return false
        }

        try {
            val actualMd5 = file.source().buffer().use { source ->
                HashingSink.md5(blackholeSink()).use { sink ->
                    source.readAll(sink)
                    sink.hash.hex()
                }
            }
            return actualMd5.lowercase() == md5.lowercase()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to calculate MD5 for ${file.absolutePath}", e)
            try {
                file.delete()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete ${file.absolutePath}", e)
            }
            return false
        }
    }

    private suspend fun postDownload(idToPathMap: MutableMap<Long, File>) {
        val downloadsToProcess = idToPathMap.size
        updateNotificationProgress(
            0,
            downloadsToProcess,
            MessageFormat.format(applicationContext.getString(R.string.sync_downloading_count), 0, downloadsToProcess)
        )
        while (idToPathMap.isNotEmpty()) {
            Log.d(TAG, "${idToPathMap.size} downloads remaining...")
            val toPop = mutableSetOf<Long>()
            for ((downloadId, expectedPath) in idToPathMap) {
                val q = DownloadManager.Query()
                q.setFilterById(downloadId)
                val cursor: Cursor = downloadManager.query(q)
                cursor.moveToFirst()
                val status =
                    cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                if (status == DownloadManager.STATUS_FAILED) {
                    Log.e(TAG, "Download failed for $expectedPath")
                    idToPathMap.remove(downloadId)
                }
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    val tempUri =
                        cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                    val tempUriObj = Uri.parse(tempUri).path
                    if (tempUriObj == null) {
                        Log.e(TAG, "Downloaded file not found for $expectedPath")
                        toPop += downloadId
                        cursor.close()
                        continue
                    }
                    val tempFile = File(tempUriObj)
                    if (!tempFile.exists()) {
                        Log.e(TAG, "Downloaded file not found for $expectedPath")
                        toPop += downloadId
                        cursor.close()
                        continue
                    }
                    if (expectedPath.absolutePath != tempFile.absolutePath) {
                        if (expectedPath.exists()) {
                            val deleteOutcome = expectedPath.delete()
                            assert(deleteOutcome)
                        }
                        tempFile.copyTo(expectedPath, true)
                        tempFile.delete()
                    }
                    toPop += downloadId
                }
                cursor.close()
            }
            for (downloadId in toPop) {
                idToPathMap.remove(downloadId)
            }
            updateNotificationProgress(
                downloadsToProcess - idToPathMap.size,
                downloadsToProcess,
                MessageFormat.format(applicationContext.getString(R.string.sync_downloading_count), downloadsToProcess - idToPathMap.size, downloadsToProcess)
            )
            delay(1000)
        }
    }

    private suspend fun sendNotification() {
        val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(id)
        notificationBuilder =
            NotificationCompat.Builder(applicationContext, SYNC_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.notification_icon_small)
                .setContentTitle(applicationContext.getString(R.string.sync_in_progress))
                .setContentInfo(applicationContext.getString(R.string.sync_preparing))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .addAction(
                    android.R.drawable.ic_input_delete,
                    applicationContext.getString(R.string.cancel),
                    intent
                )
        notificationId = Random.nextInt()
        // notificationId is a unique int for each notification that you must define
        val notification = notificationBuilder.build()
        notificationManager.notify(notificationId, notification)
        registerForegroundService(notification)
    }

    private suspend fun updateNotificationProgress(progress: Int, max: Int, stage: String) {
        notificationBuilder.setProgress(max, progress, false)
            .setContentText(stage)
            .setOngoing(true)
        val notification = notificationBuilder.build()
        notificationManager.notify(notificationId, notification)
        registerForegroundService(notification)
    }

    private fun completeNotification(filesProcessed: Int, filesDownloaded: Int) {
        notificationBuilder.setProgress(0, 0, false)
            .setContentTitle(applicationContext.getString(R.string.sync_complete_title))
            .setContentText(MessageFormat.format(applicationContext.getString(R.string.sync_complete_notification), filesProcessed, filesDownloaded))
            .setOngoing(false)
        notificationManager.notify(notificationId, notificationBuilder.build())

        // Send again as service notification is dismissed after worker finishes.
        val notificationId = Random.nextInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun failureNotification(exception: Exception) {
        notificationBuilder.setProgress(0, 0, false)
            .setContentTitle(applicationContext.getString(R.string.sync_failed))
            .setContentText(exception.message)
            .setOngoing(false)
        notificationManager.notify(notificationId, notificationBuilder.build())

        // Send again as service notification is dismissed after worker finishes.
        val notificationId = Random.nextInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    /** See https://developer.android.com/guide/background/persistent/how-to/long-running */
    private suspend fun registerForegroundService(notification: Notification) {
        setForeground(ForegroundInfo(notificationId, notification))
    }
}