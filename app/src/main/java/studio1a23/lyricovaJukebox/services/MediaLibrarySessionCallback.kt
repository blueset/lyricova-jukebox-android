package studio1a23.lyricovaJukebox.services

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS
import androidx.media3.common.MediaMetadata.MEDIA_TYPE_MUSIC
import androidx.media3.common.MediaMetadata.MEDIA_TYPE_PLAYLIST
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaConstants
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.guava.future
import studio1a23.lyricovaJukebox.BuildConfig
import studio1a23.lyricovaJukebox.R
import studio1a23.lyricovaJukebox.data.musicFile.MusicFileRepo
import studio1a23.lyricovaJukebox.util.TAG
import javax.inject.Inject

object MediaItemIds {
    const val NODE_PREFIX = "${BuildConfig.APPLICATION_ID}."
    const val ROOT = NODE_PREFIX + "root"
    const val SONG = NODE_PREFIX + "song"
    const val PRODUCER = NODE_PREFIX + "producer"
    const val VOCALIST = NODE_PREFIX + "vocalist"
    const val ALBUM = NODE_PREFIX + "album"
    const val PLAYLIST = NODE_PREFIX + "playlist"
}

@UnstableApi
class MediaLibrarySessionCallback @Inject constructor(
    private val musicFileRepo: MusicFileRepo,
    @ApplicationContext private val applicationContext: Context,
) :
    MediaLibraryService.MediaLibrarySession.Callback {

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onGetLibraryRoot(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<MediaItem>> = Futures.immediateFuture(
        LibraryResult.ofItem(
            MediaItem.Builder()
                .setMediaId(MediaItemIds.ROOT)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setIsPlayable(false)
                        .setIsBrowsable(false)
                        .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                        .build()
                )
                .build(),
            applyLibraryParams(params)
        )
    )

    override fun onGetChildren(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> = scope.future {
        LibraryResult.ofItemList(
            when (parentId) {
                MediaItemIds.ROOT -> listOf(
                    browsableMediaItem(
                        MediaItemIds.SONG,
                        applicationContext.getString(R.string.tracks),
                        null,
                        drawableUri(R.drawable.notification_icon_small), // TODO: Icon
                        MEDIA_TYPE_PLAYLIST
                    ),
                    browsableMediaItem(
                        MediaItemIds.PLAYLIST,
                        "Playlists", // TODO: i18n
                        null,
                        drawableUri(R.drawable.notification_icon_small), // TODO: Icon
                        MEDIA_TYPE_FOLDER_PLAYLISTS
                    ),
                )

                MediaItemIds.SONG -> musicFileRepo.getMediaItems("${MediaItemIds.SONG}/")
                else -> emptyList()
            },
            applyLibraryParams(params)
        )
    }

    override fun onSetMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>,
        startIndex: Int,
        startPositionMs: Long,
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> = scope.future {
        Log.d(
            TAG,
            "onSetMediaItems, mediaItems (${mediaItems.size}), $startIndex, $startPositionMs"
        )
        // Play from Android Auto
        val defaultResult =
            MediaSession.MediaItemsWithStartPosition(emptyList(), startIndex, startPositionMs)
//        return@future MediaSession.MediaItemsWithStartPosition(mediaItems, startIndex, startPositionMs)
        if (mediaItems.size == 1) {
            val mediaItem = mediaItems.first()
            if (mediaItem.mediaId.isEmpty()) {
                if (mediaItem.requestMetadata.searchQuery?.isNotBlank() == true) {
                    return@future MediaSession.MediaItemsWithStartPosition(
                        musicFileRepo.searchMediaItemsByKeyword(
                            mediaItem.requestMetadata.searchQuery!!,
                            "${MediaItemIds.SONG}/"
                        ),
                        0,
                        0
                    )
                } else {
                    // TODO: resume default
                    return@future defaultResult
                }
            } else {
                val path = mediaItems.firstOrNull()?.mediaId?.split("/")
                    ?: return@future defaultResult
                return@future when (path.firstOrNull()) {
                    MediaItemIds.SONG -> {
                        val songId = path.getOrNull(1) ?: return@future defaultResult
                        val allMediaItems = musicFileRepo.getMediaItems()
                        MediaSession.MediaItemsWithStartPosition(
                            allMediaItems,
                            allMediaItems.indexOfFirst { it.mediaId == songId }.takeIf { it != -1 } ?: 0,
                            startPositionMs
                        )
                    }
                    else ->
                        MediaSession.MediaItemsWithStartPosition(mediaItems, startIndex, startPositionMs)
                }
            }
        } else {
            return@future MediaSession.MediaItemsWithStartPosition(mediaItems, startIndex, startPositionMs)
        }
    }

    private fun applyLibraryParams(params: MediaLibraryService.LibraryParams?): MediaLibraryService.LibraryParams =
        MediaLibraryService.LibraryParams.Builder()
            .setOffline(params?.isOffline ?: true)
            .setRecent(params?.isRecent ?: false)
            .setRecent(params?.isRecent ?: false)
            .setExtras(Bundle().apply {
                putBoolean("android.media.browse.CONTENT_STYLE_SUPPORTED", true)
                putInt(
                    MediaConstants.EXTRAS_KEY_CONTENT_STYLE_BROWSABLE,
                    MediaConstants.EXTRAS_VALUE_CONTENT_STYLE_LIST_ITEM,
                )
                putInt(
                    MediaConstants.EXTRAS_KEY_CONTENT_STYLE_PLAYABLE,
                    MediaConstants.EXTRAS_VALUE_CONTENT_STYLE_LIST_ITEM,
                )
            })
            .build()

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>,
    ): ListenableFuture<List<MediaItem>> {
        // Only playing one track at a time when requested from Media Controller Tester
        Log.d(TAG, "onAddMediaItems, $mediaItems")
        return scope.future {
            mediaItems
                .mapNotNull { item ->
                    musicFileRepo.getMediaItem(item.mediaId.split("/").last().toInt())
                }
                .filter { it.mediaMetadata.mediaType == MEDIA_TYPE_MUSIC }
        }
    }

    override fun onGetSearchResult(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> = scope.future {
        Log.d(TAG, "onGetSearchResult, $query, $page, $pageSize, $params")
        LibraryResult.ofItemList(
            musicFileRepo.searchMediaItemsByKeyword(query).drop(page * pageSize).take(pageSize),
            applyLibraryParams(params)
        )
    }

//    override fun onPlaybackResumption( // TODO: Implement
//        mediaSession: MediaSession,
//        controller: MediaSession.ControllerInfo
//    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
//        Log.d(TAG, "onPlaybackResumption, $mediaSession, $controller")
//        return scope.future {
//            MediaSession.MediaItemsWithStartPosition(
//                musicFileRepo.getMediaItems(),
//                0,
//                0
//            )
//        }
//    }

    private fun browsableMediaItem(
        id: String,
        title: String,
        subtitle: String?,
        iconUri: Uri?,
        mediaType: Int = MEDIA_TYPE_MUSIC
    ) =
        MediaItem.Builder()
            .setMediaId(id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setArtist(subtitle)
                    .setArtworkUri(iconUri)
                    .setIsPlayable(false)
                    .setIsBrowsable(true)
                    .setMediaType(mediaType)
                    .build()
            )
            .build()

    private fun drawableUri(@DrawableRes id: Int) = Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(applicationContext.resources.getResourcePackageName(id))
        .appendPath(applicationContext.resources.getResourceTypeName(id))
        .appendPath(applicationContext.resources.getResourceEntryName(id))
        .build()
}