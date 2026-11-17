package ir.alishams.hardwareshenas.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

private val HardwareTypography = Typography(
    displayLarge = Typography().displayLarge.copy(fontFamily = Estedad, fontWeight = FontWeight.Black),
    displayMedium = Typography().displayMedium.copy(fontFamily = Estedad, fontWeight = FontWeight.ExtraBold),
    displaySmall = Typography().displaySmall.copy(fontFamily = Estedad, fontWeight = FontWeight.Bold),
    headlineLarge = Typography().headlineLarge.copy(fontFamily = Estedad, fontWeight = FontWeight.ExtraBold),
    headlineMedium = Typography().headlineMedium.copy(fontFamily = Estedad, fontWeight = FontWeight.Bold),
    headlineSmall = Typography().headlineSmall.copy(fontFamily = Estedad, fontWeight = FontWeight.Bold),
    titleLarge = Typography().titleLarge.copy(fontFamily = Estedad, fontWeight = FontWeight.Bold),
    titleMedium = Typography().titleMedium.copy(fontFamily = Estedad, fontWeight = FontWeight.SemiBold),
    titleSmall = Typography().titleSmall.copy(fontFamily = Estedad, fontWeight = FontWeight.SemiBold),
    bodyLarge = Typography().bodyLarge.copy(fontFamily = Estedad),
    bodyMedium = Typography().bodyMedium.copy(fontFamily = Estedad),
    bodySmall = Typography().bodySmall.copy(fontFamily = Estedad),
    labelLarge = Typography().labelLarge.copy(fontFamily = Estedad, fontWeight = FontWeight.SemiBold),
    labelMedium = Typography().labelMedium.copy(fontFamily = Estedad, fontWeight = FontWeight.Medium),
    labelSmall = Typography().labelSmall.copy(fontFamily = Estedad, fontWeight = FontWeight.Medium)
)

private val HardwareShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(17.dp),
    large = RoundedCornerShape(23.dp),
    extraLarge = RoundedCornerShape(29.dp)
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
        typography = HardwareTypography,
        shapes = HardwareShapes
    ) {
        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl,
            LocalTextStyle provides MaterialTheme.typography.bodyMedium.copy(
                textDirection = TextDirection.ContentOrRtl
            ),
            content = content
        )
    }
}
