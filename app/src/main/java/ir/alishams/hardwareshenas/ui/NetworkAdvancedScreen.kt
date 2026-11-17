package ir.alishams.hardwareshenas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ir.alishams.hardwareshenas.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import kotlin.system.measureTimeMillis

@Composable
fun NetworkAdvancedScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var running by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("برای شروع، آزمون اتصال را اجرا کن.") }
    Column(modifier.fillMaxSize().background(Midnight).verticalScroll(rememberScrollState())
        .padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        HardwarePageTopBar(onBack)
        HardwareSectionHeading("آزمایش پیشرفته شبکه", eyebrow = "NETWORK PERFORMANCE")
        Text("آزمون با درخواست واقعی به سرور عمومی انجام می‌شود. نتیجه به کیفیت اینترنت، VPN و شلوغی شبکه وابسته است.",
            color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("تست تأخیر و سرعت دانلود", color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium)
                Button(onClick = {
                    scope.launch {
                        running = true
                        result = "در حال اتصال و اندازه‌گیری..."
                        result = withContext(Dispatchers.IO) { runNetworkTest() }
                        running = false
                    }
                }, enabled = !running, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Midnight)) {
                    Text(if (running) "در حال آزمایش..." else "شروع آزمون شبکه")
                }
                if (running) LinearProgressIndicator(Modifier.fillMaxWidth(), color = CyanAccent)
                Text(result, color = CyanAccent, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Text("این آزمون حجم محدودی از داده را از speed.cloudflare.com دریافت می‌کند. برای نتیجه دقیق‌تر، VPN را خاموش کن و آزمون را چند بار در شرایط یکسان تکرار کن. این تست جایگزین سنجش کیفیت شبکه اپراتور یا تضمین سرعت قراردادی نیست.",
            color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

private fun runNetworkTest(): String {
    var connection: HttpURLConnection? = null
    return try {
        val started = System.nanoTime()
        connection = URL("https://speed.cloudflare.com/__down?bytes=1000000").openConnection() as HttpURLConnection
        connection.connectTimeout = 10000
        connection.readTimeout = 15000
        connection.setRequestProperty("Cache-Control", "no-cache")
        connection.connect()
        val connectMs = (System.nanoTime() - started) / 1_000_000.0
        if (connection.responseCode !in 200..299) return "سرور آزمون پاسخ موفق نداد: HTTP ${connection.responseCode}"
        var total = 0L
        val buffer = ByteArray(16 * 1024)
        val downloadStart = System.nanoTime()
        connection.inputStream.use { input ->
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                total += count
            }
        }
        val seconds = (System.nanoTime() - downloadStart) / 1_000_000_000.0
        val mbps = if (seconds > 0) total * 8.0 / seconds / 1_000_000.0 else 0.0
        "زمان برقراری اتصال: ${"%.0f".format(connectMs)} ms\nحجم دریافت‌شده: ${total / 1024} KB\nسرعت دانلود تقریبی: ${"%.2f".format(mbps)} Mbps"
    } catch (e: Exception) {
        "آزمون شبکه انجام نشد: ${e.javaClass.simpleName}. اتصال اینترنت را بررسی کن."
    } finally { connection?.disconnect() }
}
