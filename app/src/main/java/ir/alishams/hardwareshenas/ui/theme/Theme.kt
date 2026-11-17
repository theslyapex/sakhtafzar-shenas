package ir.alishams.hardwareshenas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val HardwareColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = Midnight,
    background = Midnight,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    secondary = CyanAccent,
    onSurfaceVariant = TextSecondary,
    outline = DividerDark
)

@Composable
fun HardwareShenasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HardwareColorScheme,
        content = content
    )
}
