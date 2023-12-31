package studio1a23.lyricovaJukebox.ui.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import studio1a23.lyricovaJukebox.LocalPlayerAwareWindowInsets
import studio1a23.lyricovaJukebox.LocalPlayerConnection
import studio1a23.lyricovaJukebox.R

@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun Player(
    modifier: Modifier = Modifier,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val context = LocalContext.current

    Column(modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom))) {
        Text("Player", color = MaterialTheme.colorScheme.onBackground)
        AndroidView(factory = {
            PlayerView(context).apply {
                player = playerConnection.player
                defaultArtwork = resources.getDrawable(R.drawable.notification_icon_small, null)
                controllerHideOnTouch = false
                showController()
            }
        })
    }
}