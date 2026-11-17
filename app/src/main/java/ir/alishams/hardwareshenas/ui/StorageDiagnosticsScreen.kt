package ir.alishams.hardwareshenas.ui

import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ir.alishams.hardwareshenas.ui.theme.Midnight
import ir.alishams.hardwareshenas.ui.theme.SurfaceDark
import ir.alishams.hardwareshenas.ui.theme.TextSecondary
import java.util.Locale

private data class StorageVolumeInfo(val path: String, val totalBytes: Long, val freeBytes: Long, val removable: Boolean)

@Composable
fun StorageDiagnosticsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val volumes = remember(context) {
        buildList {
            val internalPath = Environment.getDataDirectory().absolutePath
            runCatching {
                val stat = StatFs(internalPath)
                add(StorageVolumeInfo(internalPath, stat.totalBytes, stat.availableBytes, false))
            }
            // Emulated public storage is an alias of internal storage, not a second disk.
            context.getExternalFilesDirs(null).filterNotNull()
                .filter { runCatching { Environment.isExternalStorageRemovable(it) }.getOrDefault(false) }
                .distinctBy { it.absolutePath.substringBefore("/Android/") }
                .forEach { directory ->
                    runCatching {
                        val root = directory.absolutePath.substringBefore("/Android/")
                        val stat = StatFs(directory.absolutePath)
                        add(StorageVolumeInfo(root, stat.totalBytes, stat.availableBytes, true))
                    }
                }
        }
    }
    Column(
        modifier = modifier.fillMaxSize().background(Midnight)
            .verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        HardwarePageTopBar(onBack)
        HardwareSectionHeading("بررسی فضای ذخیره‌سازی", eyebrow = "STORAGE DIAGNOSTICS")
        Text("ظرفیت، فضای اشغال‌شده و فضای آزاد حافظه‌ای که اندروید در اختیار برنامه قرار می‌دهد.",
            color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        if (volumes.isEmpty()) Text("اطلاعات فضای ذخیره‌سازی از سیستم دریافت نشد.", color = TextSecondary)
        volumes.forEachIndexed { index, volume ->
            val used = (volume.totalBytes - volume.freeBytes).coerceAtLeast(0L)
            val percent = if (volume.totalBytes > 0) used.toFloat() / volume.totalBytes else 0f
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (index == 0) "حافظه داخلی دستگاه" else "فضای ذخیره‌سازی اضافی",
                        color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Text("\u2066${formatBytes(used)}\u2069 از \u2066${formatBytes(volume.totalBytes)}\u2069",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall.copy(textDirection = TextDirection.Content))
                    androidx.compose.foundation.layout.Box(Modifier.fillMaxWidth().height(11.dp)
                        .background(androidx.compose.ui.graphics.Color(0xFF1A2638), RoundedCornerShape(8.dp))) {
                        androidx.compose.foundation.layout.Box(Modifier.fillMaxWidth(percent.coerceIn(0f, 1f))
                            .height(11.dp).background(ir.alishams.hardwareshenas.ui.theme.CyanAccent, RoundedCornerShape(8.dp)))
                    }
                    StorageRow("فضای اشغال‌شده", formatBytes(used))
                    StorageRow("فضای آزاد", formatBytes(volume.freeBytes))
                    StorageRow("درصد استفاده", String.format(Locale.US, "%.1f%%", percent * 100f))
                    StorageRow("مسیر", volume.path)
                    StorageRow("نوع حافظه", if (volume.removable) "قابل‌جداشدن" else "حافظه داخلی")
                }
            }
        }
        Spacer(Modifier.height(5.dp))
        Text("اعداد بر اساس فضای قابل مشاهده برای اندروید گزارش می‌شوند. فضای قابل استفاده ممکن است با ظرفیت اسمی درج‌شده روی دستگاه متفاوت باشد.",
            color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun StorageRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        val hasLtrData = value.any { it.isDigit() || it.toString() == "/" || (it.isLetter() && it.code < 128) }
        Text(value, color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall.copy(textDirection = if (hasLtrData) TextDirection.Ltr else TextDirection.Content),
            fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
}

private fun formatBytes(bytes: Long): String {
    val gb = bytes / (1024.0 * 1024.0 * 1024.0)
    return if (gb >= 1.0) String.format(Locale.US, "%.2f GB", gb)
    else String.format(Locale.US, "%.0f MB", bytes / (1024.0 * 1024.0))
}
