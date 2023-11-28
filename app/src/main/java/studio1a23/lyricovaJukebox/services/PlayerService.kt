package studio1a23.lyricovaJukebox.services

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Binder
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.Assertions
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSourceUtil
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CacheBitmapLoader
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import com.google.common.base.Supplier
import com.google.common.base.Suppliers
import com.google.common.primitives.Bytes
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ListeningExecutorService
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import studio1a23.lyricovaJukebox.MainActivity
import studio1a23.lyricovaJukebox.providers.getCoverArtUri
import studio1a23.lyricovaJukebox.util.TAG
import java.io.IOException
import java.util.concurrent.Executors
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class PlayerService : MediaLibraryService(), Player.Listener {
    lateinit var player: ExoPlayer
    private var mediaLibrarySession: MediaLibrarySession? = null
    private val binder = PlayerServiceBinder()

    @Inject
    lateinit var callback: MediaLibrarySessionCallback

    lateinit var notificationProvider: MediaNotification.Provider

    val currentMediaMetadata = MutableStateFlow<MediaMetadata?>(null)

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
        player.addListener(this)
        mediaLibrarySession = MediaLibrarySession.Builder(this, player, callback)
            .setSessionActivity(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setBitmapLoader(CacheBitmapLoader(DataSourceBitmapLoader(/* context= */ this)))
            .build()
        notificationProvider = DefaultMediaNotificationProvider(applicationContext)
        setMediaNotificationProvider(notificationProvider)
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        currentMediaMetadata.value = mediaMetadata.buildUpon()
            .setArtworkUri(
                getCoverArtUri(
                    mediaMetadata.extras?.getInt("lyricova.musicFileId") ?: return, this
                )
            )
            .build()
        Log.d(
            TAG,
            "onMediaMetadataChanged: $mediaMetadata, ${currentMediaMetadata.value?.artworkUri}"
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? =
        mediaLibrarySession

    override fun onDestroy() {
        mediaLibrarySession?.run {
            player.release()
            release()
            mediaLibrarySession = null
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = super.onBind(intent) ?: binder

    inner class PlayerServiceBinder : Binder() {
        val service: PlayerService
            get() = this@PlayerService
    }
}

@UnstableApi
class DataSourceBitmapLoader
/**
 * Creates an instance that delegates loading tasks to the [ListeningExecutorService].
 *
 * @param listeningExecutorService The [ListeningExecutorService].
 * @param dataSourceFactory The [DataSource.Factory] that creates the [DataSource]
 * used to load the image.
 */(
    private val listeningExecutorService: ListeningExecutorService,
    private val dataSourceFactory: DataSource.Factory
) : BitmapLoader {
    /**
     * Creates an instance that uses a [DefaultHttpDataSource] for image loading and delegates
     * loading tasks to a [Executors.newSingleThreadExecutor].
     */
    constructor(context: Context?) : this(
        Assertions.checkStateNotNull<ListeningExecutorService>(DEFAULT_EXECUTOR_SERVICE.get()),
        DefaultDataSource.Factory(
            context!!
        )
    )

    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> {
        return listeningExecutorService.submit<Bitmap> {
            decode(data)
        }
    }

    /** Loads an image from a [Uri].  */
    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
        return listeningExecutorService.submit<Bitmap> {
            load(dataSourceFactory.createDataSource(), uri)
        }
    }

    companion object {
        val DEFAULT_EXECUTOR_SERVICE: Supplier<ListeningExecutorService> = Suppliers.memoize {
            MoreExecutors.listeningDecorator(
                Executors.newSingleThreadExecutor()
            )
        }

        @OptIn(ExperimentalUnsignedTypes::class, ExperimentalStdlibApi::class)
        private fun decode(data: ByteArray): Bitmap {
            Log.d(TAG, "Decoding image: ${data.size} bytes, ${data.toHexString().take(100)}")
            return BitmapFactory.decodeByteArray(data,  /* offset= */0, data.size)
                ?: Bytes.indexOf(data, ubyteArrayOf(0xFFU, 0xD8U, 0xFFU, 0xDBU).toByteArray()).let { index ->
                    if (index != -1) {
                        Log.d(TAG, "Decoding image: Found JPEG header at $index, ${data.drop(index).toByteArray().toHexString().take(100)}")
                        BitmapFactory.decodeByteArray(data, index, data.size - index)
                    } else {
                        Log.d(TAG, "Decoding image: failed to decode")
                        throw IOException("Cannot decode bitmap")
                    }
                }
        }

        @Throws(IOException::class)
        private fun load(dataSource: DataSource, uri: Uri): Bitmap {
            val dataSpec = DataSpec(uri)
            dataSource.open(dataSpec)
            val readData = DataSourceUtil.readToEnd(dataSource)
            return decode(readData)
        }
    }
}
