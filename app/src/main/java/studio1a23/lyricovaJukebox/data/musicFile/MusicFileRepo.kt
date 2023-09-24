package studio1a23.lyricovaJukebox.data.musicFile

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata.MEDIA_TYPE_MUSIC
import dagger.hilt.android.qualifiers.ApplicationContext
import studio1a23.lyricovaJukebox.MusicFileQuery
import studio1a23.lyricovaJukebox.MusicFilesQuery
import studio1a23.lyricovaJukebox.data.apolloClient
import studio1a23.lyricovaJukebox.data.preference.UserPreferencesDataSource
import studio1a23.lyricovaJukebox.util.TAG
import studio1a23.lyricovaJukebox.workers.LYRICOVA_SUB_PATH
import java.io.File
import javax.inject.Inject

data class MusicFileEntityWithLyrics(
    val musicFileEntity: MusicFileEntity,
    val lrc: String?,
    val lrcx: String?
)

fun String.sanitizeFileName(): String {
    return this.replace(Regex("[\\\\:*?\"<>|\n]"), "_")
}

fun MusicFileEntity.toUri(): Uri? {
    val file = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
        LYRICOVA_SUB_PATH + path
    )
    if (!file.exists()) {
        Log.e(TAG, "File to play is not found: $file")
        return null
    }
    return file.toUri()
}

fun MusicFileEntity.toMediaItem(prefix: String = ""): MediaItem? {
    val uri = this.toUri() ?: return null
    val mediaItem = MediaItem.fromUri(uri)
    return mediaItem.buildUpon().setMediaId("$prefix$id")
        .setMediaMetadata(
            mediaItem.mediaMetadata.buildUpon()
                .setTitle(trackName)
                .setArtist(artistName)
                .setAlbumTitle(albumName)
                .setSubtitle("$artistName / $albumName")
                .setIsBrowsable(false)
                .setIsPlayable(true)
                .setMediaType(MEDIA_TYPE_MUSIC)
                .setExtras(Bundle().also { it.putInt("lyricova.musicFileId", id) })
//                .setArtworkUri(getCoverArtUri(id, context))
                .build()
        ).build()
}

class MusicFileRepo @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource,
    private val musicFileDao: MusicFileDao,
    @ApplicationContext private val context: Context,
) {
    fun getMusicFilesFlow() = musicFileDao.getAllFlow()
    fun getMusicFiles() = musicFileDao.getAll()

        fun getMediaItems(prefix: String = "") = musicFileDao.getAll().mapNotNull { entity -> entity.toMediaItem(prefix) }

        fun getMediaItem(id: Int, prefix: String = "") = musicFileDao.getById(id).toMediaItem(prefix)
    fun getMediaFile(id: Int) = musicFileDao.getById(id)

    fun searchMediaItemsByKeyword(keyword: String, prefix: String = "") =
            musicFileDao.searchByKeyword(keyword).mapNotNull { entity -> entity.toMediaItem(prefix) }
    // TODO: Sort by relevance ( track > artist > album, original > cover )

    data class SyncResult(
        val inserted: List<MusicFileEntityWithLyrics>,
        val updated: List<MusicFileEntityWithLyrics>,
        val deleted: List<MusicFileEntity>,
    )

    suspend fun sync(): SyncResult {
        Log.d(TAG, "Loading music files from server")
        val response = apolloClient.query(MusicFilesQuery()).execute()
        if (response.data != null) {
            val newEntries = response.data!!.musicFiles.edges.map { it.node }
            Log.d(TAG, "Loaded ${newEntries.size} music files info from server")

            val oldFiles = getMusicFiles()
            val oldFileIds = oldFiles.map { it.id }.toSet()
            val newFilesWithLyrics = newEntries.map {
                MusicFileEntityWithLyrics(
                    MusicFileEntity(
                        it.id,
                        it.trackName,
                        it.trackSortOrder,
                        it.artistName,
                        it.artistSortOrder,
                        it.albumName,
                        it.albumSortOrder,
                        it.duration,
                        it.hash,
                        it.path.sanitizeFileName(),
                    ),
                    it.lrc,
                    it.lrcx
                )
            }
            val newFileIds = newFilesWithLyrics.map { it.musicFileEntity.id }.toSet()

            val toInsertEntities =
                newFilesWithLyrics.filter { it.musicFileEntity.id !in oldFileIds }
            val toUpdateEntities = newFilesWithLyrics.filter { it.musicFileEntity.id in oldFileIds }
            val toInsert = toInsertEntities.map { it.musicFileEntity }
            val toUpdate = toUpdateEntities.map { it.musicFileEntity }
            val toDelete = oldFiles.filter { it.id !in newFileIds }

            Log.d(TAG, "Inserting ${toInsert.size} music files")
            musicFileDao.insert(*toInsert.toTypedArray())
            Log.d(TAG, "Updating ${toUpdate.size} music files")
            musicFileDao.update(*toUpdate.toTypedArray())
            Log.d(TAG, "Deleting ${toDelete.size} music files")
            musicFileDao.delete(*toDelete.toTypedArray())
            return SyncResult(toInsertEntities, toUpdateEntities, toDelete)
        }
        return SyncResult(listOf(), listOf(), listOf())
    }

    suspend fun getFileInfo(id: Int): MusicFileQuery.MusicFile? {
        val response = apolloClient.query(MusicFileQuery(id)).execute()
        return response.data?.musicFile
    }
}