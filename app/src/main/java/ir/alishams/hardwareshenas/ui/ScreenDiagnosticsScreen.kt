package ir.alishams.hardwareshenas.ui

import android.view.Display
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.alishams.hardwareshenas.ui.theme.CyanAccent
import ir.alishams.hardwareshenas.ui.theme.Midnight
import ir.alishams.hardwareshenas.ui.theme.SurfaceDark
import ir.alishams.hardwareshenas.ui.theme.TextSecondary
import java.util.Locale

private data class ScreenTestColor(val name: String, val color: Color)

private val screenTestColors = listOf(
    ScreenTestColor("مشکی", Color.Black),
    ScreenTestColor("سفید", Color.White),
    ScreenTestColor("قرمز", Color.Red),
    ScreenTestColor("سبز", Color.Green),
    ScreenTestColor("آبی", Color.Blue),
    ScreenTestColor("فیروزه‌ای", Color.Cyan),
    ScreenTestColor("ارغوانی", Color.Magenta),
    ScreenTestColor("زرد", Color.Yellow),
    ScreenTestColor("خاکستری", Color(0xFF808080))
)

@Composable
fun ScreenDiagnosticsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var colorIndex by remember { mutableIntStateOf(-1) }
    var touchCount by remember { mutableIntStateOf(0) }
    var maxTouchCount by remember { mutableIntStateOf(0) }
    val display = if (android.os.Build.VERSION.SDK_INT >= 30) {
        context.display
    } else {
        @Suppress("DEPRECATION")
        (context.getSystemService(android.content.Context.WINDOW_SERVICE) as android.view.WindowManager).defaultDisplay
    }
    val refreshRate = display?.refreshRate
    val supportedModes = display?.supportedModes?.map { it.refreshRate }?.distinct()?.sorted()
    val selectedColor = screenTestColors.getOrNull(colorIndex)

    if (selectedColor != null) {
        Column(
            modifier = modifier.fillMaxSize().background(selectedColor.color).padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "آزمون رنگ: ${selectedColor.name}",
                color = if (selectedColor.color == Color.Black || selectedColor.color == Color.Blue) Color.White else Color.Black,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "برای بررسی پیکسل سوخته، نقاط ثابت و تغییر رنگ غیرعادی را نگاه کنید.",
                    color = if (selectedColor.color == Color.Black || selectedColor.color == Color.Blue) Color.White else Color.Black
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { colorIndex = (colorIndex - 1).coerceAtLeast(0) }) { Text("قبلی") }
                    Button(
                        onClick = {
                            if (colorIndex >= screenTestColors.lastIndex) colorIndex = -1
                            else colorIndex++
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray, contentColor = Color.White)
                    ) { Text(if (colorIndex == screenTestColors.lastIndex) "پایان آزمون" else "رنگ بعدی") }
                }
            }
        }
        return
    }

    Column(
        modifier = modifier.fillMaxSize().background(Midnight).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = CyanAccent)
        ) { Text("بازگشت به داشبورد") }
        Text("آزمون صفحه‌نمایش", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text("آزمون رنگ‌ها برای بررسی چشمی پیکسل‌های معیوب، یکنواختی پنل و نمایش رنگ‌هاست؛ تشخیص خودکار پیکسل سوخته یا کالیبراسیون رنگ انجام نمی‌شود.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("آزمون رنگ و پیکسل", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text("۹ صفحه تمام‌رنگ برای بررسی پیکسل سوخته، گیرکرده و ناهماهنگی رنگی", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                Button(
                    onClick = { colorIndex = 0 },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Midnight)
                ) { Text("شروع آزمون تمام‌صفحه") }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("نرخ نوسازی نمایشگر", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text(
                    refreshRate?.let { String.format(Locale.US, "%.2f Hz", it) } ?: "در دسترس نیست",
                    color = CyanAccent,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "حالت‌های گزارش‌شده: " + (supportedModes?.joinToString { String.format(Locale.US, "%.0f Hz", it) } ?: "نامشخص"),
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                Text("نرخ نمایش‌داده‌شده، حالت فعلی سیستم است و ممکن است با نرخ‌های پشتیبانی‌شده پنل تفاوت داشته باشد.", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("آزمون لمس چندنقطه‌ای", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text("انگشت‌ها را هم‌زمان روی محدوده زیر قرار دهید و حرکت دهید.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                Box(
                    modifier = Modifier.fillMaxWidth().aspectRatio(1.5f)
                        .background(SurfaceDark, RoundedCornerShape(14.dp))
                        .border(1.dp, CyanAccent.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                        .pointerInteropFilter { event ->
                            when (event.actionMasked) {
                                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_MOVE -> {
                                    touchCount = event.pointerCount
                                    if (touchCount > maxTouchCount) maxTouchCount = touchCount
                                }
                                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                                    touchCount = (event.pointerCount - 1).coerceAtLeast(0)
                                }
                            }
                            true
                        },
                ) {
                    Column(Modifier.align(androidx.compose.ui.Alignment.Center), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text("$touchCount", color = CyanAccent, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                        Text("نقطه لمس هم‌زمان", color = TextSecondary)
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("بیشترین لمس ثبت‌شده", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    Text("$maxTouchCount", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = { maxTouchCount = 0; touchCount = 0 }) { Text("پاک‌کردن نتیجه") }
            }
        }
        Spacer(Modifier.height(2.dp))
        Text("POWERED BY ALISHAMS DIGITAL STUDIO", color = TextSecondary.copy(alpha = 0.65f), style = MaterialTheme.typography.labelSmall)
    }
}
