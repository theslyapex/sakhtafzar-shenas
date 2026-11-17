package ir.alishams.hardwareshenas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import ir.alishams.hardwareshenas.R

private val Estedad = FontFamily(
    Font(R.font.estedad, FontWeight.Thin),
    Font(R.font.estedad, FontWeight.ExtraLight),
    Font(R.font.estedad, FontWeight.Light),
    Font(R.font.estedad, FontWeight.Normal),
    Font(R.font.estedad, FontWeight.Medium),
    Font(R.font.estedad, FontWeight.SemiBold),
    Font(R.font.estedad, FontWeight.Bold),
    Font(R.font.estedad, FontWeight.ExtraBold),
    Font(R.font.estedad, FontWeight.Black)
)

private val HardwareColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = Midnight,
    primaryContainer = ColorPrimaryContainer,
    onPrimaryContainer = TextPrimary,
    secondary = VioletAccent,
    onSecondary = TextPrimary,
    tertiary = BlueAccent,
    background = Midnight,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DividerDark,
    error = ErrorAccent
)

@Composable
fun HardwareShenasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HardwareColorScheme,
        typography = Typography(defaultFontFamily = Estedad),
        content = content
    )
}
