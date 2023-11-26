package studio1a23.lyricovaJukebox.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import studio1a23.lyricovaJukebox.LocalPlayerConnection
import studio1a23.lyricovaJukebox.ui.constants.MiniPlayerHeight

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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(MiniPlayerHeight)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
    ) {
        Text(text = "Is playing: $isPlaying", color = MaterialTheme.colorScheme.onBackground)

        LinearProgressIndicator(
            progress = (position.toFloat() / duration).coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.BottomCenter)
        )
    }
}