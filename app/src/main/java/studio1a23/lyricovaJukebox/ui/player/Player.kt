package studio1a23.lyricovaJukebox.ui.player

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import studio1a23.lyricovaJukebox.R

@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun Player(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel<PlayerViewModel>(),
) {
    val context = LocalContext.current

    Column(modifier) {
        Text("Player")
        AndroidView(factory = {
            PlayerView(context).apply {
                player = viewModel.player
                defaultArtwork = resources.getDrawable(R.drawable.notification_icon_small, null)
                controllerHideOnTouch = false
                showController()
            }
        })
    }
}