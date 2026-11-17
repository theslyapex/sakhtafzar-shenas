package ir.alishams.hardwareshenas.ui

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.alishams.hardwareshenas.ui.theme.CyanAccent
import ir.alishams.hardwareshenas.ui.theme.Midnight
import ir.alishams.hardwareshenas.ui.theme.SurfaceDark
import ir.alishams.hardwareshenas.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.util.Locale

private data class BatterySnapshot(
    val levelPercent: Int?,
    val status: Int?,
    val health: Int?,
    val plugged: Int?,
    val temperatureC: Float?,
    val voltageMv: Int?,
    val technology: String?,
    val chargeCounterMah: Float?,
    val currentMa: Float?,
    val energyWh: Float?,
    val cycleCount: Int?
)

@Composable
fun BatteryDiagnosticsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val data = remember { mutableStateOf<BatterySnapshot?>(null) }

    LaunchedEffect(context) {
        val manager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        while (true) {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val percent = if (level >= 0 && scale > 0) (level * 100f / scale).toInt() else null
            val temp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)?.takeIf { it >= 0 }?.div(10f)
            val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)?.takeIf { it > 0 }
            val charge = manager.readProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)?.let { it / 1000f }
            val current = manager.readSignedProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)?.let { it / 1000f }
            val energy = manager.readLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)?.takeIf { it > 0L }?.div(1_000_000_000f)
            // Android's public BatteryManager API does not expose a portable cycle-count property.
            val cycles: Int? = null
            data.value = BatterySnapshot(
                levelPercent = percent,
                status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1),
                health = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1),
                plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0),
                temperatureC = temp,
                voltageMv = voltage,
                technology = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)?.takeIf { it.isNotBlank() },
                chargeCounterMah = charge,
                currentMa = current,
                energyWh = energy,
                cycleCount = cycles
            )
            delay(3000)
        }
    }

    val battery = data.value
    Column(
        modifier = modifier.fillMaxSize().background(Midnight).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = CyanAccent)
        ) { Text("بازگشت به داشبورد") }
        Text("تشخیص و وضعیت باتری", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text("اطلاعات از رابط‌های استاندارد Android خوانده می‌شوند؛ مواردی که سازنده در اختیار برنامه نگذارد، نامشخص نمایش داده می‌شوند.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("سطح شارژ", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text(battery?.levelPercent?.let { "$it٪" } ?: "در حال خواندن…", color = CyanAccent, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                Text("وضعیت: ${battery?.status?.let { batteryStatus(it) } ?: "نامشخص"}", color = MaterialTheme.colorScheme.onSurface)
                Text("منبع تغذیه: ${battery?.plugged?.let { pluggedLabel(it) } ?: "نامشخص"}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }

        BatteryCard("سلامت و مشخصات", listOf(
            "وضعیت سلامت گزارش‌شده" to (battery?.health?.let { batteryHealth(it) } ?: "نامشخص"),
            "فناوری باتری" to (battery?.technology ?: "نامشخص"),
            "تعداد چرخه شارژ" to (battery?.cycleCount?.takeIf { it >= 0 }?.toString() ?: "از API عمومی Android قابل دریافت نیست")
        ))
        BatteryCard("اندازه‌گیری‌های الکتریکی", listOf(
            "ولتاژ" to (battery?.voltageMv?.let { String.format(Locale.US, "%.3f V", it / 1000.0) } ?: "نامشخص"),
            "جریان لحظه‌ای" to (battery?.currentMa?.let { String.format(Locale.US, "%.0f mA", it) } ?: "نامشخص"),
            "شمارنده شارژ" to (battery?.chargeCounterMah?.let { String.format(Locale.US, "%.0f mAh", it) } ?: "نامشخص"),
            "انرژی گزارش‌شده" to (battery?.energyWh?.let { String.format(Locale.US, "%.3f Wh", it) } ?: "نامشخص"),
            "دمای باتری" to (battery?.temperatureC?.let { String.format(Locale.US, "%.1f °C", it) } ?: "نامشخص")
        ))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("محدودیت تشخیص سلامت", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text(
                    "درصد شارژ با سلامت باتری یکسان نیست. برای محاسبه دقیق افت ظرفیت، ظرفیت اسمی کارخانه و ظرفیت کامل فعلی لازم است. Android معمولاً این دو مقدار را در اختیار برنامه‌های عادی نمی‌گذارد؛ بنابراین سخت‌افزار شناس بدون داده معتبر، سلامت یا ظرفیت باقی‌مانده را حدس نمی‌زند.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        Text("POWERED BY ALISHAMS DIGITAL STUDIO", color = TextSecondary.copy(alpha = 0.65f), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun BatteryCard(title: String, rows: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            rows.forEach { (label, value) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, color = TextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(value, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private fun BatteryManager.readProperty(property: Int): Int? =
    runCatching { getIntProperty(property).takeIf { it != Int.MIN_VALUE && it >= 0 } }.getOrNull()

private fun BatteryManager.readSignedProperty(property: Int): Int? =
    runCatching { getIntProperty(property).takeIf { it != Int.MIN_VALUE } }.getOrNull()

private fun BatteryManager.readLongProperty(property: Int): Long? =
    runCatching { getLongProperty(property).takeIf { it != Long.MIN_VALUE } }.getOrNull()

private fun batteryStatus(status: Int): String = when (status) {
    BatteryManager.BATTERY_STATUS_CHARGING -> "در حال شارژ"
    BatteryManager.BATTERY_STATUS_DISCHARGING -> "در حال تخلیه"
    BatteryManager.BATTERY_STATUS_FULL -> "شارژ کامل"
    BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "شارژ نمی‌شود"
    else -> "نامشخص"
}

private fun batteryHealth(health: Int): String = when (health) {
    BatteryManager.BATTERY_HEALTH_GOOD -> "خوب"
    BatteryManager.BATTERY_HEALTH_OVERHEAT -> "بیش‌ازحد گرم"
    BatteryManager.BATTERY_HEALTH_DEAD -> "فرسوده / پایان عمر گزارش‌شده"
    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "ولتاژ بیش‌ازحد"
    BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "خطای نامشخص"
    BatteryManager.BATTERY_HEALTH_COLD -> "دمای بسیار پایین"
    else -> "نامشخص"
}

private fun pluggedLabel(plugged: Int): String = when {
    plugged and BatteryManager.BATTERY_PLUGGED_USB != 0 -> "USB"
    plugged and BatteryManager.BATTERY_PLUGGED_AC != 0 -> "آداپتور برق"
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && plugged and BatteryManager.BATTERY_PLUGGED_WIRELESS != 0 -> "شارژ بی‌سیم"
    plugged == 0 -> "بدون اتصال شارژر"
    else -> "منبع نامشخص"
}
