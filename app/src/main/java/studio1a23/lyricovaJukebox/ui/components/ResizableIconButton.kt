package studio1a23.lyricovaJukebox.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * @author https://github.com/z-huang/InnerTune/blob/dev/app/src/main/java/com/zionhuang/music/ui/component/IconButton.kt
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResizableIconButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    indication: Indication? = null,
    contentDescription: String? = null,
    onClick: () -> Unit = {},
) {
    SimpleTooltip(contentDescription) {
        Image(
            icon,
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(color),
            modifier = Modifier
                .clickable(
                    indication = indication ?: rememberRipple(bounded = false),
                    interactionSource = remember { MutableInteractionSource() },
                    enabled = enabled,
                    onClick = onClick
                )
                .alpha(if (enabled) 1f else 0.5f)
                .then(modifier)
        )
    }
}