package studio1a23.lyricovaJukebox

import BottomSheetPlayer
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import studio1a23.lyricovaJukebox.data.musicFile.MusicFileRepo
import studio1a23.lyricovaJukebox.services.PlayerService
import studio1a23.lyricovaJukebox.ui.constants.AppBarHeight
import studio1a23.lyricovaJukebox.ui.constants.MiniPlayerHeight
import studio1a23.lyricovaJukebox.ui.constants.NavigationBarAnimationSpec
import studio1a23.lyricovaJukebox.ui.constants.NavigationBarHeight
import studio1a23.lyricovaJukebox.ui.nav.NavBar
import studio1a23.lyricovaJukebox.ui.nav.collapsedAnchor
import studio1a23.lyricovaJukebox.ui.nav.rememberBottomSheetState
import studio1a23.lyricovaJukebox.ui.player.Player
import studio1a23.lyricovaJukebox.ui.player.PlayerConnection
import studio1a23.lyricovaJukebox.ui.settings.Settings
import studio1a23.lyricovaJukebox.ui.theme.JukeboxTheme
import studio1a23.lyricovaJukebox.ui.tracks.Tracks
import studio1a23.lyricovaJukebox.ui.utils.appBarScrollBehavior
import studio1a23.lyricovaJukebox.ui.utils.resetHeightOffset
import javax.inject.Inject

@AndroidEntryPoint
@UnstableApi
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var musicFileRepo: MusicFileRepo

    private var playerConnection by mutableStateOf<PlayerConnection?>(null)

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is PlayerService.PlayerServiceBinder) {
                playerConnection =
                    PlayerConnection(this@MainActivity, service, musicFileRepo, lifecycleScope)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            playerConnection?.dispose()
            playerConnection = null
        }
    }

    override fun onStart() {
        super.onStart()
        startForegroundService(Intent(this, PlayerService::class.java))
        bindService(
            Intent(this, PlayerService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onStop() {
        unbindService(serviceConnection)
        super.onStop()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @androidx.media3.common.util.UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CompositionLocalProvider(
                LocalPlayerConnection provides playerConnection,
            ) {
                JukeboxTheme {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        val navController = rememberNavController()
                        val density = LocalDensity.current
                        val windowsInsets = WindowInsets.systemBars
                        val bottomInset = with(density) { windowsInsets.getBottom(density).toDp() }
                        val navBackStackEntry by navController.currentBackStackEntryAsState()

                        var active by rememberSaveable {
                            mutableStateOf(false)
                        }

                        val shouldShowNavigationBar = remember(navBackStackEntry, active) {
                            true
//                navBackStackEntry?.destination?.route == null ||
//                        navigationItems.fastAny { it.route == navBackStackEntry?.destination?.route } && !active
                        }

                        val playerBottomSheetState = rememberBottomSheetState(
                            dismissedBound = 0.dp,
                            collapsedBound = bottomInset + (if (shouldShowNavigationBar) NavigationBarHeight else 0.dp) + MiniPlayerHeight,
                            expandedBound = maxHeight,
                            initialAnchor = collapsedAnchor,
                        )

                        val scrollBehavior = appBarScrollBehavior(canScroll = {
                            navBackStackEntry?.destination?.route?.startsWith("search/") == false && (playerBottomSheetState.isCollapsed || playerBottomSheetState.isDismissed)
                        })

                        val navigationBarHeight by animateDpAsState(
                            targetValue = if (shouldShowNavigationBar) NavigationBarHeight else 0.dp,
                            animationSpec = NavigationBarAnimationSpec,
                            label = ""
                        )

                        val playerAwareWindowInsets = remember(
                            bottomInset,
                            shouldShowNavigationBar,
                            playerBottomSheetState.isDismissed
                        ) {
                            var bottom = bottomInset
                            if (shouldShowNavigationBar) bottom += NavigationBarHeight
                            if (!playerBottomSheetState.isDismissed) bottom += MiniPlayerHeight
                            windowsInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                                .add(WindowInsets(top = AppBarHeight, bottom = bottom))
                        }

                        LaunchedEffect(active) {
                            if (active) {
                                scrollBehavior.state.resetHeightOffset()
                            }
                        }

                        CompositionLocalProvider(
                            LocalPlayerAwareWindowInsets provides playerAwareWindowInsets,
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = "tracks",
                                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                            ) {
                                composable("player") {
                                    Player()
                                }
                                composable("tracks") {
                                    Tracks()
                                }
                                composable("settings") {
                                    Settings()
                                }
                            }

                            BottomSheetPlayer(
                                state = playerBottomSheetState, navController = navController
                            )

                            NavBar(navController = navController,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset {
                                        if (navigationBarHeight == 0.dp) {
                                            IntOffset(
                                                x = 0,
                                                y = (bottomInset + NavigationBarHeight).roundToPx()
                                            )
                                        } else {
                                            val slideOffset =
                                                (bottomInset + NavigationBarHeight) * playerBottomSheetState.progress.coerceIn(
                                                    0f, 1f
                                                )
                                            val hideOffset =
                                                (bottomInset + NavigationBarHeight) * (1 - navigationBarHeight / NavigationBarHeight)
                                            IntOffset(
                                                x = 0, y = (slideOffset + hideOffset).roundToPx()
                                            )
                                        }
                                    })
                        }
                    }
                }
            }
        }
    }
}

val LocalPlayerAwareWindowInsets =
    compositionLocalOf<WindowInsets> { error("No WindowInsets provided") }
val LocalPlayerConnection =
    staticCompositionLocalOf<PlayerConnection?> { error("No PlayerConnection provided") }

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!", modifier = modifier
    )
}
