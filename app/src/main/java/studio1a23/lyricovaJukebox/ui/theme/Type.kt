package studio1a23.lyricovaJukebox.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle

val defaultTypography = Typography()
val textStyleMod = TextStyle(fontFeatureSettings = "palt")

// Set of Material typography styles to start with
val typography = Typography(
    displayLarge = defaultTypography.displayLarge.merge(textStyleMod),
    displayMedium = defaultTypography.displayMedium.merge(textStyleMod),
    displaySmall = defaultTypography.displaySmall.merge(textStyleMod),
    headlineLarge = defaultTypography.headlineLarge.merge(textStyleMod),
    headlineMedium = defaultTypography.headlineMedium.merge(textStyleMod),
    headlineSmall = defaultTypography.headlineSmall.merge(textStyleMod),
    titleLarge = defaultTypography.titleLarge.merge(textStyleMod),
    titleMedium = defaultTypography.titleMedium.merge(textStyleMod),
    titleSmall = defaultTypography.titleSmall.merge(textStyleMod),
    bodyLarge = defaultTypography.bodyLarge.merge(textStyleMod),
    bodyMedium = defaultTypography.bodyMedium.merge(textStyleMod),
    bodySmall = defaultTypography.bodySmall.merge(textStyleMod),
    labelLarge = defaultTypography.labelLarge.merge(textStyleMod),
    labelMedium = defaultTypography.labelMedium.merge(textStyleMod),
    labelSmall = defaultTypography.labelSmall.merge(textStyleMod),
)