package studio1a23.lyricovaJukebox.ui.theme

import android.app.Activity
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
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.media3.common.util.UnstableApi

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
    themeColor: Color = Color.Unspecified,
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
//    var themeColor by rememberSaveable(stateSaver = ColorSaver) {
//        mutableStateOf(Color.Unspecified)
//    }

//    val currentMetadata = LocalPlayerConnection.current?.service?.currentMediaMetadata?.collectAsState()
    val context = LocalContext.current
//    val imageLoader = LocalContext.current.imageLoader

//    Log.d("Theme", "Rerender theme, current metadata: ${currentMetadata?.value?.artworkUri}")
//    LaunchedEffect(currentMetadata?.value?.artworkUri) {
//        Log.d(
//            TAG,
//            "Launched effect theme: current metadata changed, ${currentMetadata?.value?.artworkUri}"
//        )
//        val metadata = currentMetadata?.value
//        themeColor = if (metadata?.artworkUri == null) {
//            Log.d(TAG, "Launched effect theme: color = undefined")
//            Color.Unspecified
//        } else {
//            withContext(Dispatchers.IO) {
//                try {
//                    val result = imageLoader.execute(
//                        ImageRequest.Builder(context)
//                            .data(metadata.artworkUri)
//                            .allowHardware(false) // pixel access is not supported on Config#HARDWARE bitmaps
//                            .build()
//                    )
//                    Log.d(TAG, "Launched effect theme: result = $result")
//                    val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
//                    val color = bitmap?.extractThemeColor()
//                    Log.d(TAG, "Launched effect theme: color = $color, bitmap = $bitmap")
//                    color ?: Color.Unspecified
//                } catch (e: Exception) {
//                    Log.e(TAG, "Launched effect theme: error", e)
//                    Color.Unspecified
//                }
//            }
//        }
//    }

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