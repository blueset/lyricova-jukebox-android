package studio1a23.lyricovaJukebox.providers

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.UriMatcher
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.ParcelFileDescriptor
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import studio1a23.lyricovaJukebox.data.musicFile.MusicFileRepo
import studio1a23.lyricovaJukebox.data.musicFile.toUri
import studio1a23.lyricovaJukebox.services.MediaItemIds.NODE_PREFIX
import java.io.ByteArrayInputStream
import java.io.InputStream


private const val AUTHORITY = NODE_PREFIX + "coverArtProvider"
private const val SONG_MATCHER_CODE = 1
private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
    /*
     * Sets the code for a single row to 2. In this case, the # wildcard is
     * used. content://com.example.app.provider/table3/3 matches, but
     * content://com.example.app.provider/table3 doesn't.
     */
    addURI(AUTHORITY, "songId/#", SONG_MATCHER_CODE)
}

fun getCoverArtUri(songId: Int, context: Context): Uri =
    Uri.Builder()
        .scheme("content")
        .authority(AUTHORITY)
        .appendPath("songId")
        .appendPath(songId.toString())
        .build()
        .also { uri ->
            /**
             * These are necessary to grant the cover uri file permissions.
             * systemui is related to this one:
             * https://github.com/PaulWoitaschek/Voice/issues/1860
             *
             * The others are related to watch and car
             * https://github.com/android/uamp/blob/2136c37bcef54da1ee350fd642fc61a744e86654/common/src/main/res/xml/allowed_media_browser_callers.xml
             */
            listOf(
                "com.android.systemui",
                "com.google.android.autosimulator",
                "com.google.android.carassistant",
                "com.google.android.googlequicksearchbox",
                "com.google.android.projection.gearhead",
                "com.google.android.wearable.app",
            ).forEach { grantedPackage ->
                context.grantUriPermission(
                    grantedPackage,
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
        }

/** Content provider to support on-demand cover art resolution through URI. */
class CoverArtProvider : ContentProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ExampleContentProviderEntryPoint {
        fun musicFileRepo(): MusicFileRepo
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val appContext = context?.applicationContext ?: throw IllegalStateException()
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            appContext,
            ExampleContentProviderEntryPoint::class.java
        )
        val musicFileRepo = hiltEntryPoint.musicFileRepo()

        when (sUriMatcher.match(uri)) {
            SONG_MATCHER_CODE -> {
                val songId = uri.lastPathSegment?.toIntOrNull() ?: return null
                val song = musicFileRepo.getMediaFile(songId)
                val mediaMetadataRetriever = MediaMetadataRetriever()
                mediaMetadataRetriever.setDataSource(appContext, song.toUri())
                val embeddedPicture = mediaMetadataRetriever.embeddedPicture ?: return null
                val cursor = android.database.MatrixCursor(arrayOf("data"))
                cursor.addRow(arrayOf(embeddedPicture))
                return cursor
            }

            else -> return null
        }
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val appContext = context?.applicationContext ?: throw IllegalStateException()
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            appContext,
            ExampleContentProviderEntryPoint::class.java
        )
        val musicFileRepo = hiltEntryPoint.musicFileRepo()

        when (sUriMatcher.match(uri)) {
            SONG_MATCHER_CODE -> {
                val songId = uri.lastPathSegment?.toIntOrNull() ?: return null
                val song = musicFileRepo.getMediaFile(songId)
                val mediaMetadataRetriever = MediaMetadataRetriever()
                mediaMetadataRetriever.setDataSource(appContext, song.toUri())
                val embeddedPicture = mediaMetadataRetriever.embeddedPicture ?: return null
                val (inDesc, outDesc) = ParcelFileDescriptor.createPipe()
                val inputStream: InputStream = ByteArrayInputStream(embeddedPicture)
                val outputStream = ParcelFileDescriptor.AutoCloseOutputStream(outDesc)
                var len: Int
                while (inputStream.read().also { len = it } >= 0) {
                    outputStream.write(len)
                }
                inputStream.close()
                outputStream.flush()
                outputStream.close()
                // Return the ParcelFileDescriptor input stream to the calling activity in order to read
                // the file data.
                return inDesc
            }

            else -> return null
        }
    }

    override fun getType(uri: Uri): String? {
        return when (sUriMatcher.match(uri)) {
            SONG_MATCHER_CODE -> "image/png"
            else -> null
        }
    }

    override fun getStreamTypes(uri: Uri, mimeTypeFilter: String): Array<String>? {
        return when (sUriMatcher.match(uri)) {
            SONG_MATCHER_CODE -> if (mimeTypeFilter.startsWith("image/")) arrayOf("image/png") else null
            else -> null
        }
    }

    override fun insert(p0: Uri, p1: ContentValues?): Uri? = null
    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int = 0
    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int = 0

}