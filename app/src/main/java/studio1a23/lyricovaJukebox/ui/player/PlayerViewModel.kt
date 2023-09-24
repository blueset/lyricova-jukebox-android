package studio1a23.lyricovaJukebox.ui.player

import android.content.ComponentName
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import studio1a23.lyricovaJukebox.data.musicFile.MusicFileRepo
import studio1a23.lyricovaJukebox.providers.getCoverArtUri
import studio1a23.lyricovaJukebox.services.PlayerService
import studio1a23.lyricovaJukebox.util.TAG
import studio1a23.lyricovaJukebox.workers.LYRICOVA_SUB_PATH
import java.io.File
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val musicFileRepo: MusicFileRepo,
) : ViewModel(), Player.Listener {
    val currentMediaMetadata = MutableStateFlow<MediaMetadata?>(null)

    var player: MediaController? = null

    init {
        val sessionToken =
            SessionToken(appContext, ComponentName(appContext, PlayerService::class.java))
        val controllerFuture = MediaController.Builder(appContext, sessionToken).buildAsync()

        controllerFuture.addListener(
            /* listener = */ {
                player = controllerFuture.get()
                // player.addListener(listener)
                player?.prepare()
                player?.addListener(this)

//                viewModelScope.launch {
//                    while (isActive) {
//                        duration = player.duration.takeUnless { it == C.TIME_UNSET }?.milliseconds
//                            ?: Duration.ZERO
//                        position = player.currentPosition.milliseconds
//                        delay(500)
//                    }
//                }
            },
            /* executor = */ MoreExecutors.directExecutor()
        )
    }

    override fun onCleared() {
        super.onCleared()
        player?.release()
    }

    fun playFromItem(item: MediaItem) {
        player?.let { player ->
            // Set the media item to be played.
            player.setMediaItem(item)
            // Prepare the player.
            player.prepare()
            // Start the playback.
            player.play()

            player.currentTimeline.let {
                List(it.windowCount) { index ->
                    it.getWindow(index, Timeline.Window()).mediaItem
                }
            }
        }
    }

    fun playFromItemInItems(items: List<MediaItem>, index: Int) {
        player?.let { player ->
            player.setMediaItems(items, index, 0)
//            player.prepare()
            player.play()

            player.currentTimeline.let {
                List(it.windowCount) { index ->
                    it.getWindow(index, Timeline.Window()).mediaItem
                }
            }
        }
    }

    fun playFromPath(path: String) {
        player?.let { player ->
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                LYRICOVA_SUB_PATH + path
            )
            if (!file.exists()) {
                Log.e(TAG, "File to play is not found: $file")
                return
            }
            val uri = file.toUri()
            val mediaItem = MediaItem.fromUri(uri)
            // Set the media item to be played.
            player.setMediaItem(mediaItem)
            // Prepare the player.
            player.prepare()
            // Start the playback.
            player.play()
        }
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        currentMediaMetadata.value = mediaMetadata.buildUpon()
            .setArtworkUri(getCoverArtUri(mediaMetadata.extras?.getInt("lyricova.musicFileId") ?: return, appContext))
            .build()
        Log.d(
            TAG,
            "onMediaMetadataChanged: $mediaMetadata, ${currentMediaMetadata.value?.artworkUri}"
        )
    }

    override fun onEvents(player: Player, events: Player.Events) {
        if (events.containsAny(
                Player.EVENT_TIMELINE_CHANGED,
                Player.EVENT_POSITION_DISCONTINUITY
            )
        ) {
        } else {
            super.onEvents(player, events)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun contentProviderTest() {
        viewModelScope.launch(Dispatchers.IO) {
//            appContext.contentResolver.query(getCoverArtUri(11353), null, null, null, null)
//                ?.let { cursor ->
//                    cursor.moveToFirst()
//                    Log.d(TAG, "Content provider test: cursor.count = ${cursor.count}")
//                    Log.d(TAG, "Content provider test: cursor.columnCount = ${cursor.columnCount}")
//                    Log.d(TAG, "Content provider test: cursor.columnNames = ${cursor.columnNames.joinToString(",")}")
//                    Log.d(TAG, "Content provider test: cursor.position = ${cursor.position}")
//                    Log.d(TAG, "Content provider test: cursor.getType(0) = ${cursor.getType(0)}")
//                    Log.d(TAG, "Content provider test: cursor.getBlob(0).size = ${cursor.getBlob(0).size}")
//                    cursor.close()
//                }
            appContext.contentResolver.openInputStream(getCoverArtUri(11353, appContext))
                ?.let { stream ->
                    Log.d(TAG, "Content provider test: stream.available() = ${stream.available()}")
                    val buffer = ByteArray(50)
                    stream.read(buffer, 0, 50)
                    Log.d(TAG, "Content provider test: stream.read() = ${buffer.toHexString()}")
                    stream.close()
                }
        }
    }
}