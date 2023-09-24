package studio1a23.lyricovaJukebox.ui.theme

import android.graphics.Bitmap
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.palette.graphics.Palette
import dev.sasikanth.material.color.utilities.dynamiccolor.MaterialDynamicColors
import dev.sasikanth.material.color.utilities.hct.Hct
import dev.sasikanth.material.color.utilities.scheme.SchemeContent
import dev.sasikanth.material.color.utilities.scheme.SchemeExpressive
import dev.sasikanth.material.color.utilities.scheme.SchemeFidelity
import dev.sasikanth.material.color.utilities.scheme.SchemeMonochrome
import dev.sasikanth.material.color.utilities.scheme.SchemeNeutral
import dev.sasikanth.material.color.utilities.scheme.SchemeTonalSpot
import dev.sasikanth.material.color.utilities.scheme.SchemeVibrant
import dev.sasikanth.material.color.utilities.score.Score

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

/**
 * A [Saver] for [Color]s.
 * @author https://github.com/z-huang/InnerTune/blob/dev/app/src/main/java/com/zionhuang/music/ui/theme/Theme.kt#L96
 */
val ColorSaver = object : Saver<Color, Int> {
    override fun restore(value: Int): Color = Color(value)
    override fun SaverScope.save(value: Color): Int = value.toArgb()
}

/**
 * Extracts the theme color from a bitmap.
 * @author https://github.com/z-huang/InnerTune/blob/00265c81d16ad7477dbd8505dcc8e94887a10702/app/src/main/java/com/zionhuang/music/ui/theme/Theme.kt#L48C1-L56C2
 */
fun Bitmap.extractThemeColor(): Color {
    val colorsToPopulation = Palette.from(this)
        .maximumColorCount(8)
        .generate()
        .swatches
        .associate { it.rgb to it.population }
    val rankedColors = Score.score(colorsToPopulation)
    return Color(rankedColors.first())
}

enum class PaletteStyle {
    TonalSpot,
    Neutral,
    Vibrant,
    Expressive,
    Monochrome,
    Fidelity,
    Content
}

/**
 * Generate a [ColorScheme] from a [Color].
 * @author https://github.com/jordond/MaterialKolor/blob/main/material-kolor/src/commonMain/kotlin/com/materialkolor/DynamicColorScheme.kt
 */
fun Color.toColorScheme(isDark: Boolean, style: PaletteStyle = PaletteStyle.Vibrant, contrastLevel: Double = 0.0): ColorScheme {
    val hct = Hct.fromInt(this.toArgb())
    val colors = MaterialDynamicColors()
    val scheme = when (style) {
        PaletteStyle.TonalSpot -> SchemeTonalSpot(hct, isDark, contrastLevel)
        PaletteStyle.Neutral -> SchemeNeutral(hct, isDark, contrastLevel)
        PaletteStyle.Vibrant -> SchemeVibrant(hct, isDark, contrastLevel)
        PaletteStyle.Expressive -> SchemeExpressive(hct, isDark, contrastLevel)
        PaletteStyle.Monochrome -> SchemeMonochrome(hct, isDark, contrastLevel)
        PaletteStyle.Fidelity -> SchemeFidelity(hct, isDark, contrastLevel)
        PaletteStyle.Content -> SchemeContent(hct, isDark, contrastLevel)
    }

    return ColorScheme(
        background = Color(colors.background().getArgb(scheme)),
        error = Color(colors.error().getArgb(scheme)),
        errorContainer = Color(colors.errorContainer().getArgb(scheme)),
        inverseOnSurface = Color(colors.inverseOnSurface().getArgb(scheme)),
        inversePrimary = Color(colors.inversePrimary().getArgb(scheme)),
        inverseSurface = Color(colors.inverseSurface().getArgb(scheme)),
        onBackground = Color(colors.onBackground().getArgb(scheme)),
        onError = Color(colors.onError().getArgb(scheme)),
        onErrorContainer = Color(colors.onErrorContainer().getArgb(scheme)),
        onPrimary = Color(colors.onPrimary().getArgb(scheme)),
        onPrimaryContainer = Color(colors.onPrimaryContainer().getArgb(scheme)),
        onSecondary = Color(colors.onSecondary().getArgb(scheme)),
        onSecondaryContainer = Color(colors.onSecondaryContainer().getArgb(scheme)),
        onSurface = Color(colors.onSurface().getArgb(scheme)),
        onSurfaceVariant = Color(colors.onSurfaceVariant().getArgb(scheme)),
        onTertiary = Color(colors.onTertiary().getArgb(scheme)),
        onTertiaryContainer = Color(colors.onTertiaryContainer().getArgb(scheme)),
        outline = Color(colors.outline().getArgb(scheme)),
        outlineVariant = Color(colors.outlineVariant().getArgb(scheme)),
        primary = Color(colors.primary().getArgb(scheme)),
        primaryContainer = Color(colors.primaryContainer().getArgb(scheme)),
        scrim = Color(colors.scrim().getArgb(scheme)),
        secondary = Color(colors.secondary().getArgb(scheme)),
        secondaryContainer = Color(colors.secondaryContainer().getArgb(scheme)),
        surface = Color(colors.surface().getArgb(scheme)),
        surfaceTint = Color(colors.surfaceTint().getArgb(scheme)),
        surfaceVariant = Color(colors.surfaceVariant().getArgb(scheme)),
        tertiary = Color(colors.tertiary().getArgb(scheme)),
        tertiaryContainer = Color(colors.tertiaryContainer().getArgb(scheme))
    )
}