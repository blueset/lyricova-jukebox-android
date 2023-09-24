package studio1a23.lyricovaJukebox.workers

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat.getSystemService
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import okio.HashingSink
import okio.blackholeSink
import okio.buffer
import okio.source
import studio1a23.lyricovaJukebox.data.SERVER_ROOT
import studio1a23.lyricovaJukebox.data.musicFile.MusicFileRepo
import java.io.File


const val DOWNLOAD_TEST_WORKER_TAG = "DOWNLOAD_TEST_WORKER"
const val MUSIC_FILE_ID_INPUT = "MUSIC_FILE_ID"

@HiltWorker
class DownloadTestWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val musicFileRepo: MusicFileRepo,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val musicFileId = inputData.getInt(MUSIC_FILE_ID_INPUT, -1)
        if (musicFileId == -1) {
            return Result.failure()
        }
        downloadTest(musicFileId)
        return Result.success()
    }

    private fun ifFileMatchesMd5(file: File, md5: String): Boolean {
        if (!file.exists()) {
            return false
        }
        val actualMd5 = file.source().buffer().use { source ->
            HashingSink.md5(blackholeSink()).use { sink ->
                source.readAll(sink)
                sink.hash.hex()
            }
        }
        return actualMd5.lowercase() == md5.lowercase()
    }

    private suspend fun downloadTest(id: Int) {
        val fileInfo = musicFileRepo.getFileInfo(id) ?: return
        val downloadUrl = "$SERVER_ROOT/api/files/$id/file"
        val path = fileInfo.path
        val fullPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val expectedPath = File(fullPath, LYRICOVA_SUB_PATH + path)

        // Download files to MediaStore.Audio with the specified path
        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle(fileInfo.trackName)
            .setDescription("${fileInfo.artistName} / ${fileInfo.albumName}")
            .setAllowedOverRoaming(false)
            .setAllowedOverMetered(false)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_MUSIC,
                "$LYRICOVA_SUB_PATH$path"
            )

        val downloadManager = getSystemService(applicationContext, DownloadManager::class.java)
            ?: throw Exception("Download manager not found")
        val downloadId = downloadManager.enqueue(request)

        val tempUri: String
        while (true) {
            val q = DownloadManager.Query()
            q.setFilterById(downloadId)
            val cursor: Cursor = downloadManager.query(q)
            cursor.moveToFirst()
            val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            if (status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.STATUS_FAILED) {
                tempUri =
                    cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                cursor.close()
                break
            }
            cursor.close()
            delay(1000)
        }

        val tempFile = File(Uri.parse(tempUri).path ?: throw Exception("Downloaded file not found"))

        if (expectedPath.absolutePath != tempFile.absolutePath) {
            if (expectedPath.exists()) {
                val deleteOutcome = expectedPath.delete()
                assert(deleteOutcome)
            }
            tempFile.copyTo(expectedPath, true)
            tempFile.delete()
        }
    }
}