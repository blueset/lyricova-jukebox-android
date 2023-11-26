package studio1a23.lyricovaJukebox.ui.player

import android.content.ComponentName
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import studio1a23.lyricovaJukebox.data.musicFile.MusicFileRepo
import studio1a23.lyricovaJukebox.providers.getCoverArtUri
import studio1a23.lyricovaJukebox.services.PlayerService
import studio1a23.lyricovaJukebox.util.TAG
import studio1a23.lyricovaJukebox.workers.LYRICOVA_SUB_PATH
import java.io.File

@UnstableApi
class PlayerConnection (
    val context: Context,
    binder: PlayerService.PlayerServiceBinder,
    val musicFileRepo: MusicFileRepo,
    val scope: CoroutineScope,
) : Player.Listener {
    private val service = binder.service
    val player = service.player
    val currentMediaMetadata = MutableStateFlow<MediaMetadata?>(null)

    var isPlaying = MutableStateFlow(player.isPlaying)
    val playbackState = MutableStateFlow(player.playbackState)

    init {
        val sessionToken =
            SessionToken(context, ComponentName(context, PlayerService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        // Player obj has to be prepared here for notification to work.
        controllerFuture.addListener(
            /* listener = */ {
                val playerObj = controllerFuture.get()
                playerObj.prepare()
                playerObj.addListener(this)
                isPlaying.value = playerObj.isPlaying
                isPlaying.value = playerObj.isPlaying
                playbackState.value = playerObj.playbackState

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

    fun dispose() {
        player.removeListener(this)
    }

    override fun onPlaybackStateChanged(state: Int) {
        playbackState.value = state
//        error.value = player.playerError
        Log.d(TAG, "onPlaybackStateChanged: $state")
    }

    fun playFromItem(item: MediaItem) {
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

    fun playFromItemInItems(items: List<MediaItem>, index: Int) {
        player.setMediaItems(items, index, 0)
//            player.prepare()
        player.play()

        player.currentTimeline.let {
            List(it.windowCount) { index ->
                it.getWindow(index, Timeline.Window()).mediaItem
            }
        }
    }

    fun playFromPath(path: String) {
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

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        currentMediaMetadata.value = mediaMetadata.buildUpon()
            .setArtworkUri(
                getCoverArtUri(
                    mediaMetadata.extras?.getInt("lyricova.musicFileId") ?: return, context
                )
            )
            .build()
        Log.d(
            TAG,
            "onMediaMetadataChanged: $mediaMetadata, ${currentMediaMetadata.value?.artworkUri}"
        )
    }

    override fun onEvents(player: Player, events: Player.Events) {
        val eventFlags = buildList {
            for (i in 0 until events.size()) {
                add(events.get(i))
            }
        }
        Log.d(TAG, "onEvents: $events, $eventFlags")
        if (events.containsAny(
                Player.EVENT_TIMELINE_CHANGED,
                Player.EVENT_POSITION_DISCONTINUITY
            )
        ) {
            // TODO: events
        }
        if (events.containsAny(Player.EVENT_IS_PLAYING_CHANGED)) {
            Log.d(TAG, "EVENT_IS_PLAYING_CHANGED: ${player.isPlaying}")
            isPlaying.value = player.isPlaying
        }
        super.onEvents(player, events)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun contentProviderTest() {
        scope.launch(Dispatchers.IO) {
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
            context.contentResolver.openInputStream(getCoverArtUri(11353, context))
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