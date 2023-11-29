package studio1a23.lyricovaJukebox.ui.nav

import android.content.res.Configuration
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.RenderVectorGroup
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import studio1a23.lyricovaJukebox.LocalPlayerConnection
import studio1a23.lyricovaJukebox.ui.components.SimpleTooltip
import studio1a23.lyricovaJukebox.ui.constants.MiniPlayerHeight
import studio1a23.lyricovaJukebox.ui.constants.ThumbnailCornerRadius
import studio1a23.lyricovaJukebox.ui.theme.JukeboxTheme

/**
 * @author InnerTune https://github.com/z-huang/InnerTune/blob/e549420540dfa26fae4cc8b490e3ddb910edc05a/app/src/main/java/com/zionhuang/music/ui/player/MiniPlayer.kt
 */
@Composable
@UnstableApi
fun MiniPlayer(
    position: Long,
    duration: Long,
    modifier: Modifier = Modifier,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.service.currentMediaMetadata.collectAsState()
    val playbackState by playerConnection.playbackState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(MiniPlayerHeight)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
    ) {
        LinearProgressIndicator(
            progress = (position.toFloat() / duration).coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.BottomCenter)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxSize()
                .padding(end = 6.dp),
        ) {
            Box(Modifier.weight(1f)) {
                mediaMetadata?.let {
                    MiniMediaInfo(
                        mediaMetadata = it,
                        error = null,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }
            }

            SimpleTooltip(tooltip = if (isPlaying) "Pause" else "Play") {
                IconButton(
                    onClick = {
                        if (playbackState == Player.STATE_ENDED) {
                            playerConnection.player.seekTo(0, 0)
                            playerConnection.player.playWhenReady = true
                        } else if (isPlaying) {
                            playerConnection.player.pause()
                        } else {
                            playerConnection.player.play()
                        }
                    }
                ) {
                    Icon(
                        if (playbackState == Player.STATE_ENDED) Icons.Filled.Replay else if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                    )
                }
            }

//            IconButton(
//                enabled = canSkipNext,
//                onClick = playerConnection.player::seekToNext
//            ) {
//                Icon(
//                    painter = painterResource(R.drawable.skip_next),
//                    contentDescription = null
//                )
//            }
        }
    }
}


@Composable
fun MiniMediaInfo(
    mediaMetadata: MediaMetadata,
    error: PlaybackException?,
    modifier: Modifier = Modifier,
) {
    val placeholderIcon = Icons.Filled.MusicNote
    val noCoverPlaceholder = // rememberVectorPainter(Icons.Filled.MusicNote)
        rememberVectorPainter(
            defaultWidth = placeholderIcon.defaultWidth,
            defaultHeight = placeholderIcon.defaultHeight,
            viewportWidth = placeholderIcon.viewportWidth,
            viewportHeight = placeholderIcon.viewportHeight,
            name = placeholderIcon.name,
            tintColor = MaterialTheme.colorScheme.outline,
            tintBlendMode = placeholderIcon.tintBlendMode,
            autoMirror = placeholderIcon.autoMirror,
            content = { _, _ -> RenderVectorGroup(group = placeholderIcon.root) }
        )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(modifier = Modifier.padding(6.dp)) {
            AsyncImage(
                model = mediaMetadata.artworkUri,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(ThumbnailCornerRadius)),
                contentScale = ContentScale.Crop,
                fallback = noCoverPlaceholder,
                error = noCoverPlaceholder,
            )
            androidx.compose.animation.AnimatedVisibility(
                visible = error != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    Modifier
                        .size(48.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(ThumbnailCornerRadius)
                        )
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp)
        ) {
            Text(
                text = mediaMetadata.title.toString(),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                lineHeight = 1.em,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = mediaMetadata.artist.toString(),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 12.sp,
                lineHeight = 1.em,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
fun MiniMediaInfoDemo() {
    val mediaMetadata = MediaMetadata.Builder()
        .setTitle("初音ミクの消失 -DEAD END-")
        .setArtist("cosMo@暴走P feat. 初音ミク")
        .build()

    JukeboxTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                )
                .height(MiniPlayerHeight)
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
        ) {
            MiniMediaInfo(mediaMetadata, null)
        }
    }
}