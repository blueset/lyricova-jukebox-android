package studio1a23.lyricovaJukebox.ui.theme

import android.app.Activity
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.media3.common.util.UnstableApi
import coil.ImageLoader
import coil.request.ImageRequest
import coil.util.DebugLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import studio1a23.lyricovaJukebox.LocalPlayerConnection
import studio1a23.lyricovaJukebox.util.TAG

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

private object AccentRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor() = LocalContentColor.current

    @Composable
    override fun rippleAlpha(): RippleAlpha =
        if (LocalContentColor.current.luminance() > 0.5) RippleAlpha(
            pressedAlpha = 0.45f,
            focusedAlpha = 0.34f,
            draggedAlpha = 0.26f,
            hoveredAlpha = 0.18f
        ) else RippleAlpha(
            pressedAlpha = 0.24f,
            focusedAlpha = 0.24f,
            draggedAlpha = 0.16f,
            hoveredAlpha = 0.08f
        )
}

@Composable
@UnstableApi
fun JukeboxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val playerConnection = LocalPlayerConnection.current
    val context = LocalContext.current
    var themeColor by rememberSaveable(stateSaver = ColorSaver) {
        mutableStateOf(Color.Unspecified)
    }

    LaunchedEffect(playerConnection) {
        if (playerConnection == null) {
            themeColor = Color.Unspecified
            return@LaunchedEffect
        }
        playerConnection.service.currentMediaMetadata.collectLatest { currentMetadata ->
            Log.d(
                TAG,
                "Launched effect theme: current metadata changed, ${currentMetadata?.artworkUri}"
            )
            themeColor = if (currentMetadata?.artworkUri == null) {
                Log.d(TAG, "Launched effect theme: color = undefined")
                Color.Unspecified
            } else {
                withContext(Dispatchers.IO) {
                    Log.d(
                        TAG,
                        "Launched effect theme: load artwork uri: ${currentMetadata.artworkUri}"
                    )
                    try {
                        val imageLoader = ImageLoader.Builder(context).logger(
                            DebugLogger(Log.VERBOSE)
                        ).build()
                        val request = ImageRequest.Builder(context)
                            .data(currentMetadata.artworkUri)
                            .allowHardware(false) // pixel access is not supported on Config#HARDWARE bitmaps
                            .build()
                        Log.d(TAG, "Launched effect theme: loader = $imageLoader, request = $request");
                        val result = imageLoader.execute(request)
                        Log.d(TAG, "Launched effect theme: result = $result")
                        val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                        val color = bitmap?.extractThemeColor()
                        Log.d(
                            TAG,
                            "Launched effect theme: color = $color, bitmap = $bitmap"
                        )
                        color ?: Color.Unspecified
                    } catch (e: Exception) {
                        Log.e(TAG, "Launched effect theme: error", e)
                        Color.Unspecified
                    }
                }
            }
        }
    }

    val colorScheme = when {
        themeColor != Color.Unspecified -> themeColor.toColorScheme(darkTheme)
        dynamicColor -> {
            LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = {
            CompositionLocalProvider(LocalRippleTheme provides AccentRippleTheme) {
                content()
            }
        },
    )
}