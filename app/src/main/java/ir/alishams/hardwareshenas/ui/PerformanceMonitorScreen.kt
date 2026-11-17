package ir.alishams.hardwareshenas.ui

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.PowerManager
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.alishams.hardwareshenas.ui.theme.CyanAccent
import ir.alishams.hardwareshenas.ui.theme.Midnight
import ir.alishams.hardwareshenas.ui.theme.SurfaceDark
import ir.alishams.hardwareshenas.ui.theme.SurfaceElevated
import ir.alishams.hardwareshenas.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale

private data class CpuCounters(val total: Long, val idle: Long)
private data class PerformanceSnapshot(
    val cpuPercent: Float?,
    val usedRamBytes: Long,
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val cpuFrequencyMhz: Int?,
    val thermalStatus: Int?,
    val batteryTemperatureC: Float?
)

@Composable
fun PerformanceMonitorScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val snapshot = remember { mutableStateOf<PerformanceSnapshot?>(null) }
    val cpuHistory = remember { mutableStateListOf<Float>() }

    LaunchedEffect(context) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        var previous = readCpuCounters()
        while (true) {
            val memory = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memory)
            val current = readCpuCounters()
            val usage = if (previous != null && current != null) {
                val totalDelta = current.total - previous.total
                val idleDelta = current.idle - previous.idle
                if (totalDelta > 0) ((totalDelta - idleDelta).toFloat() / totalDelta * 100f).coerceIn(0f, 100f) else null
            } else null
            previous = current ?: previous
            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val batteryTemp = batteryIntent?.getIntExtra("temperature", -1)?.takeIf { it > 0 }?.div(10f)
            val thermal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) powerManager.currentThermalStatus else null
            val frequency = readCurrentCpuFrequency()
            snapshot.value = PerformanceSnapshot(
                cpuPercent = usage,
                usedRamBytes = (memory.totalMem - memory.availMem).coerceAtLeast(0L),
                totalRamBytes = memory.totalMem,
                availableRamBytes = memory.availMem,
                cpuFrequencyMhz = frequency,
                thermalStatus = thermal,
                batteryTemperatureC = batteryTemp
            )
            if (usage != null) {
                cpuHistory.add(usage)
                while (cpuHistory.size > 60) cpuHistory.removeAt(0)
            }
            delay(1000)
        }
    }

    val current = snapshot.value
    Column(
        modifier = modifier.fillMaxSize().background(Midnight).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = CyanAccent)
        ) { Text("بازگشت به داشبورد") }
        Text("پایش زنده عملکرد", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text("به‌روزرسانی خودکار هر یک ثانیه، تا وقتی این صفحه باز است.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("بار پردازنده", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text(
                    current?.cpuPercent?.let { String.format(Locale.getDefault(), "%.1f%%", it) } ?: "در حال نمونه‌برداری…",
                    color = CyanAccent,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text("نمودار ۶۰ نمونه اخیر", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                CpuHistoryChart(cpuHistory.toList())
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("حافظه RAM", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                val used = current?.usedRamBytes
                val total = current?.totalRamBytes
                val percent = if (used != null && total != null && total > 0) used.toFloat() / total else 0f
                Text(
                    if (used != null && total != null) "${used.toPerfSize()} از ${total.toPerfSize()}" else "در حال خواندن اطلاعات…",
                    color = MaterialTheme.colorScheme.onSurface
                )
                androidx.compose.foundation.layout.Box(
                    Modifier.fillMaxWidth().height(10.dp).background(SurfaceElevated, RoundedCornerShape(8.dp))
                ) {
                    androidx.compose.foundation.layout.Box(
                        Modifier.fillMaxWidth(percent.coerceIn(0f, 1f)).height(10.dp).background(CyanAccent, RoundedCornerShape(8.dp))
                    )
                }
                PerfRow("مصرف‌شده", used?.toPerfSize() ?: "نامشخص")
                PerfRow("در دسترس", current?.availableRamBytes?.toPerfSize() ?: "نامشخص")
                PerfRow("درصد مصرف", if (used != null && total != null && total > 0) String.format(Locale.getDefault(), "%.1f%%", percent * 100f) else "نامشخص")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("فرکانس و وضعیت حرارتی", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                PerfRow("فرکانس قابل‌خواندن CPU", current?.cpuFrequencyMhz?.let { "$it MHz" } ?: "محدودیت دسترسی سیستم")
                PerfRow("وضعیت حرارتی Android", current?.thermalStatus?.let { thermalLabel(it) } ?: "در دسترس نیست")
                PerfRow("دمای باتری", current?.batteryTemperatureC?.let { "${String.format(Locale.getDefault(), "%.1f", it)} °C" } ?: "در دسترس نیست")
                Text(
                    "وضعیت حرارتی، سطح هشدار سیستم است و دمای واقعی هسته CPU یا GPU محسوب نمی‌شود. دمای باتری نیز با دمای پردازنده تفاوت دارد.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text("POWERED BY ALISHAMS DIGITAL STUDIO", color = TextSecondary.copy(alpha = 0.65f), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun CpuHistoryChart(values: List<Float>) {
    Canvas(Modifier.fillMaxWidth().height(110.dp)) {
        if (values.size < 2) return@Canvas
        val path = Path()
        val stepX = size.width / (values.size - 1).toFloat()
        values.forEachIndexed { index, value ->
            val x = index * stepX
            val y = size.height - (value / 100f * size.height)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = CyanAccent, style = Stroke(width = 3.dp.toPx()))
    }
}

@Composable
private fun PerfRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(value, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
}

private fun readCpuCounters(): CpuCounters? = runCatching {
    val line = File("/proc/stat").useLines { lines -> lines.firstOrNull { it.startsWith("cpu ") } } ?: return null
    val values = line.trim().split(Regex("\\s+")).drop(1).mapNotNull { it.toLongOrNull() }
    if (values.size < 4) return null
    CpuCounters(values.sum(), values[3] + (values.getOrNull(4) ?: 0L))
}.getOrNull()

private fun readCurrentCpuFrequency(): Int? {
    val cores = Runtime.getRuntime().availableProcessors()
    val values = (0 until cores).mapNotNull { core ->
        runCatching {
            File("/sys/devices/system/cpu/cpu$core/cpufreq/scaling_cur_freq")
                .takeIf { it.canRead() }?.readText()?.trim()?.toIntOrNull()?.div(1000)
        }.getOrNull()
    }
    return values.takeIf { it.isNotEmpty() }?.average()?.toInt()
}

private fun thermalLabel(status: Int): String = when (status) {
    PowerManager.THERMAL_STATUS_NONE -> "عادی"
    PowerManager.THERMAL_STATUS_LIGHT -> "گرمایش خفیف"
    PowerManager.THERMAL_STATUS_MODERATE -> "گرمایش متوسط"
    PowerManager.THERMAL_STATUS_SEVERE -> "گرمایش شدید"
    PowerManager.THERMAL_STATUS_CRITICAL -> "بحرانی"
    PowerManager.THERMAL_STATUS_EMERGENCY -> "اضطراری"
    PowerManager.THERMAL_STATUS_SHUTDOWN -> "خاموشی حفاظتی"
    else -> "نامشخص"
}

private fun Long.toPerfSize(): String {
    val mb = this / (1024.0 * 1024.0)
    return if (mb >= 1024) String.format(Locale.getDefault(), "%.2f GB", mb / 1024.0)
    else String.format(Locale.getDefault(), "%.0f MB", mb)
}
