
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import studio1a23.lyricovaJukebox.LocalPlayerConnection
import studio1a23.lyricovaJukebox.ui.nav.BottomSheet
import studio1a23.lyricovaJukebox.ui.nav.BottomSheetState
import studio1a23.lyricovaJukebox.ui.nav.MiniPlayer

@Composable
@UnstableApi
fun BottomSheetPlayer(
    state: BottomSheetState,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val playbackState by playerConnection.playbackState.collectAsState()

    var position by rememberSaveable(playbackState) {
        mutableLongStateOf(playerConnection.player.currentPosition)
    }
    var duration by rememberSaveable(playbackState) {
        mutableLongStateOf(playerConnection.player.duration)
    }

    LaunchedEffect(playbackState) {
        if (playbackState == Player.STATE_READY) {
            while (isActive) {
                delay(500)
                position = playerConnection.player.currentPosition
                duration = playerConnection.player.duration
            }
        }
    }

    BottomSheet(
        state = state,
        modifier = modifier,
        backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation),
        onDismiss = {
            // TODO: Stop playing on dismiss
        },
        collapsedContent = {
            // TODO: mini player
            MiniPlayer(position, duration)
        }
    ) {
        Text("Expanded content", color = MaterialTheme.colorScheme.onBackground)
    }
}