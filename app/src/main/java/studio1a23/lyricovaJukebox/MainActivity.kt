package studio1a23.lyricovaJukebox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import studio1a23.lyricovaJukebox.ui.nav.NavBar
import studio1a23.lyricovaJukebox.ui.player.Player
import studio1a23.lyricovaJukebox.ui.settings.Settings
import studio1a23.lyricovaJukebox.ui.theme.JukeboxTheme
import studio1a23.lyricovaJukebox.ui.tracks.Tracks

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            JukeboxTheme {
                Scaffold(
                    bottomBar = { NavBar(navController = navController) }
                ) { padding ->
                    NavHost(navController = navController, startDestination = "tracks") {
                        composable("player") {
                            Player(Modifier.padding(padding))
                        }
                        composable("tracks") {
                            Tracks(Modifier.padding(padding))
                        }
                        composable("settings") {
                            Settings(Modifier.padding(padding))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
