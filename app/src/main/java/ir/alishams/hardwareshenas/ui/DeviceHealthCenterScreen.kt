package ir.alishams.hardwareshenas.ui

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.provider.MediaStore
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
import ir.alishams.hardwareshenas.data.HardwareDetailsReader
import ir.alishams.hardwareshenas.ui.theme.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class HealthReportData(
    val score: Int,
    val battery: String,
    val thermal: String,
    val ram: String,
    val storage: String,
    val note: String
)

@Composable
fun DeviceHealthCenterScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val report = remember { collectHealthReport(context) }
    var exportStatus by remember { mutableStateOf("گزارش هنوز ساخته نشده است.") }
    Column(modifier.fillMaxSize().background(Midnight).verticalScroll(rememberScrollState())
        .padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        HardwarePageTopBar(onBack)
        HardwareSectionHeading("مرکز سلامت دستگاه", eyebrow = "DEVICE HEALTH CENTER")
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("شاخص سلامت قابل ارزیابی", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                Text("${report.score} / 100", color = CyanAccent, style = MaterialTheme.typography.displaySmall)
                LinearProgressIndicator(progress = { report.score / 100f }, modifier = Modifier.fillMaxWidth(), color = CyanAccent)
                Text("این امتیاز یک شاخص مقدماتی بر اساس داده‌های قابل‌دسترسی سیستم است، نه گواهی سلامت قطعات یا نتیجه تست آزمایشگاهی.",
                    color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
        HealthItem("وضعیت گزارش‌شده باتری", report.battery)
        HealthItem("وضعیت حرارتی Android", report.thermal)
        HealthItem("حافظه RAM", report.ram)
        HealthItem("فضای ذخیره‌سازی", report.storage)
        Button(onClick = {
            exportStatus = runCatching { exportHealthPdf(context, report) }
                .getOrElse { "ساخت PDF ناموفق بود: ${it.javaClass.simpleName}" }
        }, modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Midnight)) {
            Text("ساخت گزارش PDF")
        }
        Text(exportStatus, color = CyanAccent, style = MaterialTheme.typography.bodySmall)
        Text(report.note, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun HealthItem(title: String, value: String) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, color = TextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            Text(value, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        }
    }
}

private fun collectHealthReport(context: Context): HealthReportData {
    val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val health = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
    val battery = when (health) {
        BatteryManager.BATTERY_HEALTH_GOOD -> "خوب (گزارش سیستم)"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "بیش‌ازحد گرم"
        BatteryManager.BATTERY_HEALTH_DEAD -> "پایان عمر گزارش‌شده"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "ولتاژ بیش‌ازحد"
        BatteryManager.BATTERY_HEALTH_COLD -> "دمای بسیار پایین"
        else -> "نامشخص / ارائه‌نشده"
    }
    val power = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    val thermal = if (Build.VERSION.SDK_INT >= 29) when (power.currentThermalStatus) {
        PowerManager.THERMAL_STATUS_NONE -> "بدون محدودیت حرارتی گزارش‌شده"
        PowerManager.THERMAL_STATUS_LIGHT -> "گرمای کم"
        PowerManager.THERMAL_STATUS_MODERATE -> "گرمای متوسط"
        PowerManager.THERMAL_STATUS_SEVERE -> "گرمای شدید"
        PowerManager.THERMAL_STATUS_CRITICAL -> "وضعیت بحرانی"
        PowerManager.THERMAL_STATUS_EMERGENCY -> "وضعیت اضطراری"
        PowerManager.THERMAL_STATUS_SHUTDOWN -> "خاموشی حفاظتی"
        else -> "نامشخص"
    } else "در این نسخه اندروید در دسترس نیست"
    val details = HardwareDetailsReader(context).read()
    val ram = "${details.memory.totalBytes / 1024 / 1024} MB کل، ${details.memory.availableBytes / 1024 / 1024} MB آزاد"
    val storage = "${details.storage.totalBytes / 1024 / 1024 / 1024} GB کل، ${details.storage.availableBytes / 1024 / 1024 / 1024} GB آزاد"
    var score = 100
    if (health != BatteryManager.BATTERY_HEALTH_GOOD) score -= if (health == -1) 0 else 25
    if (Build.VERSION.SDK_INT >= 29 && power.currentThermalStatus >= PowerManager.THERMAL_STATUS_SEVERE) score -= 15
    val note = "ظرفیت طراحی و ظرفیت شارژ کامل، چرخه‌های شارژ و سلامت واقعی باتری در همه گوشی‌ها از API عمومی قابل دریافت نیستند. موارد ناموجود در امتیاز قطعی لحاظ نشده‌اند. امتیاز صرفاً شاخصی محدود از داده‌های فعلی است."
    return HealthReportData(score.coerceIn(0,100), battery, thermal, ram, storage, note)
}

private fun exportHealthPdf(context: Context, data: HealthReportData): String {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = document.startPage(pageInfo)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 16f; color = android.graphics.Color.BLACK }
    val title = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 23f; isFakeBoldText = true; color = android.graphics.Color.rgb(0, 130, 140) }
    var y = 70f
    page.canvas.drawText("HARDWARE SHENAS - DEVICE HEALTH REPORT", 42f, y, title)
    y += 42f
    val lines = listOf(
        "Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())}",
        "Indicative score: ${data.score}/100",
        "Battery health reported by Android: ${data.battery}",
        "Thermal status: ${data.thermal}",
        "RAM: ${data.ram}",
        "Storage: ${data.storage}",
        "",
        "Limitations:",
        "This report is based on limited public Android system data.",
        "It is not a laboratory certification or a complete hardware diagnosis.",
        "Battery design capacity and cycle count may not be exposed by the device.",
        "",
        "POWERED BY ALISHAMS DIGITAL STUDIO"
    )
    lines.forEach { line -> page.canvas.drawText(line, 42f, y, paint); y += 28f }
    document.finishPage(page)
    val fileName = "HardwareShenas-Health-${System.currentTimeMillis()}.pdf"
    return try {
        if (Build.VERSION.SDK_INT >= 29) {
            val values = android.content.ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: error("ساخت فایل در پوشه دانلود ممکن نشد")
            context.contentResolver.openOutputStream(uri)?.use { document.writeTo(it) }
                ?: error("امکان نوشتن فایل وجود ندارد")
            "گزارش در پوشه Downloads ذخیره شد: $fileName"
        } else {
            val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
            val file = File(directory, fileName)
            FileOutputStream(file).use { document.writeTo(it) }
            "گزارش در پوشه اسناد اختصاصی برنامه ذخیره شد: ${file.absolutePath}"
        }
    } finally { document.close() }
}
