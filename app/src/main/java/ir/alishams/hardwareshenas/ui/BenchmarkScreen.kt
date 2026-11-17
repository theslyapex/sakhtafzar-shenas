package ir.alishams.hardwareshenas.ui

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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.alishams.hardwareshenas.ui.theme.CyanAccent
import ir.alishams.hardwareshenas.ui.theme.Midnight
import ir.alishams.hardwareshenas.ui.theme.SurfaceDark
import ir.alishams.hardwareshenas.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.sqrt

private data class BenchmarkResult(
    val singleCoreOpsPerSecond: Long,
    val multiCoreOpsPerSecond: Long,
    val memoryBandwidthMbPerSecond: Double,
    val logicalCores: Int,
    val durationMillis: Long
)

@Composable
fun BenchmarkScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var running by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("آماده اجرای آزمون") }
    var result by remember { mutableStateOf<BenchmarkResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxSize().background(Midnight).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = CyanAccent)
        ) { Text("بازگشت به داشبورد") }
        Text("بنچمارک سخت‌افزار", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text(
            "آزمون محلی CPU تک‌هسته‌ای، چند‌هسته‌ای و پهنای باند حافظه. نتایج به دستگاه، دما و وضعیت باتری وابسته‌اند.",
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("آزمون عملکرد", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text(status, color = CyanAccent, style = MaterialTheme.typography.titleMedium)
                if (running) {
                    Text("در طول آزمون، دستگاه را به کار سنگین دیگری مشغول نکنید.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            running = true
                            error = null
                            result = null
                            status = "آماده‌سازی آزمون…"
                            // The benchmark runs in a cancellable coroutine scope owned by this screen.
                        },
                        enabled = !running,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Midnight)
                    ) { Text("شروع بنچمارک") }
                    if (result != null) {
                        OutlinedButton(onClick = { result = null; error = null; status = "آماده اجرای آزمون" }) {
                            Text("پاک‌کردن نتیجه")
                        }
                    }
                }
                if (running) {
                    androidx.compose.runtime.LaunchedEffect(running) {
                        try {
                            status = "آزمون تک‌هسته‌ای CPU…"
                            val start = System.nanoTime()
                            val single = runCpuTest(2200)
                            status = "آزمون چند‌هسته‌ای CPU…"
                            val multi = runMultiCoreTest(2200, Runtime.getRuntime().availableProcessors())
                            status = "اندازه‌گیری پهنای باند RAM…"
                            val memory = runMemoryTest(1200)
                            result = BenchmarkResult(
                                singleCoreOpsPerSecond = single,
                                multiCoreOpsPerSecond = multi,
                                memoryBandwidthMbPerSecond = memory,
                                logicalCores = Runtime.getRuntime().availableProcessors(),
                                durationMillis = (System.nanoTime() - start) / 1_000_000
                            )
                            status = "آزمون با موفقیت به پایان رسید"
                        } catch (e: Exception) {
                            error = e.message ?: "اجرای آزمون ناموفق بود"
                            status = "آزمون متوقف شد"
                        } finally {
                            running = false
                        }
                    }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        }

        result?.let { data ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("نتایج بنچمارک", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    BenchmarkMetric("عملکرد تک‌هسته‌ای", data.singleCoreOpsPerSecond.toString(), "عملیات در ثانیه")
                    BenchmarkMetric("عملکرد چند‌هسته‌ای", data.multiCoreOpsPerSecond.toString(), "عملیات در ثانیه")
                    BenchmarkMetric("پهنای باند حافظه", String.format(Locale.US, "%.1f", data.memoryBandwidthMbPerSecond), "MB/s")
                    BenchmarkMetric("هسته‌های منطقی آزمون", data.logicalCores.toString(), "هسته")
                    BenchmarkMetric("مدت اجرای آزمون", String.format(Locale.US, "%.1f", data.durationMillis / 1000.0), "ثانیه")
                    val experimentalScore = (
                        sqrt(data.singleCoreOpsPerSecond.toDouble() * data.multiCoreOpsPerSecond.toDouble()) / 1000.0 +
                            data.memoryBandwidthMbPerSecond
                        ).toLong()
                    Spacer(Modifier.height(2.dp))
                    Text("امتیاز آزمایشی HS", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                    Text(experimentalScore.toString(), color = CyanAccent, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                    Text(
                        "این امتیاز فعلاً شاخص داخلی و آزمایشی است، نه رتبه استاندارد یا معیار قطعی مقایسه گوشی‌ها. برای مقایسه معتبر باید نسخه آزمون و شرایط اجرا یکسان باشد.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
        Spacer(Modifier.height(2.dp))
        Text("برای نتیجه پایدارتر، آزمون را پس از خنک‌شدن دستگاه و با شارژ کافی اجرا کنید.", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
        Text("POWERED BY ALISHAMS DIGITAL STUDIO", color = TextSecondary.copy(alpha = 0.65f), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun BenchmarkMetric(label: String, value: String, unit: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(label, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            Text(unit, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
        }
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
    }
}

private suspend fun runCpuTest(durationMillis: Long): Long = withContext(Dispatchers.Default) {
    val start = System.nanoTime()
    val deadline = start + durationMillis * 1_000_000
    var value = 0x1234ABCDL
    var operations = 0L
    while (System.nanoTime() < deadline) {
        value = value xor (value shl 13)
        value = value xor (value ushr 7)
        value = value xor (value shl 17)
        value *= 0x9E3779B1L
        operations++
        if ((operations and 0x3FFF) == 0L) currentCoroutineContext().ensureActive()
    }
    BenchmarkSink.value = value
    val elapsed = (System.nanoTime() - start).coerceAtLeast(1L)
    (operations * 1_000_000_000L) / elapsed
}

private suspend fun runMultiCoreTest(durationMillis: Long, cores: Int): Long = coroutineScope {
    val start = System.nanoTime()
    val counts = (0 until cores.coerceAtLeast(1)).map { worker ->
        async(Dispatchers.Default) {
            val deadline = System.nanoTime() + durationMillis * 1_000_000
            var value = 0x1234ABCDL + worker
            var operations = 0L
            while (System.nanoTime() < deadline) {
                value = value xor (value shl 13)
                value = value xor (value ushr 7)
                value = value xor (value shl 17)
                value *= 0x9E3779B1L
                operations++
                if ((operations and 0x3FFF) == 0L) currentCoroutineContext().ensureActive()
            }
            BenchmarkSink.value = value
            operations
        }
    }.awaitAll()
    val elapsed = (System.nanoTime() - start).coerceAtLeast(1L)
    (counts.sum() * 1_000_000_000L) / elapsed
}

private suspend fun runMemoryTest(durationMillis: Long): Double = withContext(Dispatchers.Default) {
    val source = ByteArray(8 * 1024 * 1024) { (it and 0xFF).toByte() }
    val destination = ByteArray(source.size)
    val start = System.nanoTime()
    val deadline = start + durationMillis * 1_000_000
    var bytesCopied = 0L
    var checksum = 0
    while (System.nanoTime() < deadline) {
        System.arraycopy(source, 0, destination, 0, source.size)
        checksum = checksum xor destination[(bytesCopied.toInt() and (source.size - 1))]
        bytesCopied += source.size.toLong() * 2L
        currentCoroutineContext().ensureActive()
    }
    BenchmarkSink.value = checksum.toLong()
    val elapsed = (System.nanoTime() - start).coerceAtLeast(1L)
    bytesCopied.toDouble() / (1024.0 * 1024.0) / (elapsed / 1_000_000_000.0)
}

private object BenchmarkSink {
    @Volatile var value: Long = 0L
}
