package ir.alishams.hardwareshenas.ui

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.media.MediaRecorder
import android.util.Size
import android.view.Surface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.alishams.hardwareshenas.ui.theme.CyanAccent
import ir.alishams.hardwareshenas.ui.theme.Midnight
import ir.alishams.hardwareshenas.ui.theme.SurfaceDark
import ir.alishams.hardwareshenas.ui.theme.SurfaceElevated
import ir.alishams.hardwareshenas.ui.theme.TextSecondary
import java.util.Locale

private data class CameraReport(
    val id: String,
    val facing: String,
    val hardwareLevel: String,
    val pixelArray: String,
    val megapixels: String,
    val focalLengths: String,
    val apertures: String,
    val physicalSize: String,
    val flash: String,
    val autofocus: String,
    val opticalStabilization: String,
    val videoStabilization: String,
    val photoSizes: List<String>,
    val videoSizes: List<String>,
    val capabilities: List<String>,
    val error: String? = null
)

@Composable
fun CameraDiagnosticsScreen(onBack: () -> Unit, onOpenLiveTest: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var reports by remember { mutableStateOf(readCameraReports(context)) }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(Midnight),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("بررسی تخصصی دوربین", color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                TextButton(onClick = onBack) { Text("بازگشت", color = CyanAccent) }
            }
            Text("اطلاعات از رابط Camera2 خود اندروید و مشخصات اعلام‌شده توسط سازنده خوانده می‌شود.",
                color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = onOpenLiveTest) {
                Text("آزمون زنده دوربین", color = CyanAccent)
            }
            TextButton(onClick = { reports = readCameraReports(context) }) {
                Text("به‌روزرسانی اطلاعات دوربین‌ها", color = CyanAccent)
            }
        }
        item {
            SummaryCard(
                title = "حریم خصوصی",
                lines = listOf(
                    "این صفحه فقط مشخصات فنی دوربین‌ها را می‌خواند.",
                    "هیچ عکس یا ویدئویی ثبت نمی‌شود و پیش‌نمایش دوربین باز نمی‌شود.",
                    "برای خواندن مشخصات Camera2، برنامه درخواست مجوز دوربین نمی‌کند."
                )
            )
        }
        if (reports.isEmpty()) {
            item {
                SummaryCard("نتیجه بررسی", listOf("هیچ دوربین قابل‌شناسایی از طریق Camera2 گزارش نشد."))
            }
        } else {
            items(reports, key = { it.id }) { report ->
                CameraCard(report)
            }
        }
        item {
            Text("محدودیت داده‌ها: برخی سازندگان بخشی از مشخصات را پنهان می‌کنند یا مقادیر عمومی ارائه می‌دهند. نبود یک قابلیت در گزارش الزاماً به معنی نبود فیزیکی آن نیست.",
                color = TextSecondary, style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
private fun CameraCard(report: CameraReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Text(report.facing, color = CyanAccent, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
            Text("شناسه دوربین: ${report.id}", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            if (report.error != null) {
                Text(report.error, color = MaterialTheme.colorScheme.error)
            } else {
                CameraInfoRow("سطح سخت‌افزاری Camera2", report.hardwareLevel)
                CameraInfoRow("ابعاد آرایه حسگر", report.pixelArray)
                CameraInfoRow("وضوح محاسبه‌شده حسگر", report.megapixels)
                CameraInfoRow("فاصله کانونی", report.focalLengths)
                CameraInfoRow("دیافراگم‌های گزارش‌شده", report.apertures)
                CameraInfoRow("اندازه فیزیکی حسگر", report.physicalSize)
                CameraInfoRow("فلاش", report.flash)
                CameraInfoRow("فوکوس خودکار", report.autofocus)
                CameraInfoRow("لرزش‌گیر اپتیکال (OIS)", report.opticalStabilization)
                CameraInfoRow("لرزش‌گیر ویدئو", report.videoStabilization)
                if (report.photoSizes.isNotEmpty()) {
                    Text("رزولوشن‌های عکس (JPEG)", color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold)
                    Text(report.photoSizes.joinToString("  •  "), color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall)
                }
                if (report.videoSizes.isNotEmpty()) {
                    Text("رزولوشن‌های خروجی ویدئو", color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold)
                    Text(report.videoSizes.joinToString("  •  "), color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall)
                }
                if (report.capabilities.isNotEmpty()) {
                    Text("قابلیت‌های اعلام‌شده", color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold)
                    Text(report.capabilities.joinToString("، "), color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(title: String, lines: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            lines.forEach { Text("• $it", color = TextSecondary, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun CameraInfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f))
        Text(value, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
    androidx.compose.material3.HorizontalDivider(color = SurfaceElevated)
}

private fun readCameraReports(context: Context): List<CameraReport> {
    val manager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return emptyList()
    return try {
        manager.cameraIdList.map { id ->
            try {
                val c = manager.getCameraCharacteristics(id)
                val facing = when (c.get(CameraCharacteristics.LENS_FACING)) {
                    CameraCharacteristics.LENS_FACING_BACK -> "دوربین پشت"
                    CameraCharacteristics.LENS_FACING_FRONT -> "دوربین جلو"
                    CameraCharacteristics.LENS_FACING_EXTERNAL -> "دوربین خارجی"
                    else -> "نوع نامشخص"
                }
                val pixel = c.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
                val mp = pixel?.let {
                    String.format(Locale.US, "%.2f MP", it.width.toDouble() * it.height.toDouble() / 1_000_000.0)
                } ?: "گزارش نشده"
                val map = c.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                val photoSizes = map?.getOutputSizes(ImageFormat.JPEG)
                    ?.sortedByDescending { it.width.toLong() * it.height.toLong() }
                    ?.take(6)?.map { it.toLabel() } ?: emptyList()
                val videoSizes = map?.getOutputSizes(MediaRecorder::class.java)
                    ?.sortedByDescending { it.width.toLong() * it.height.toLong() }
                    ?.take(6)?.map { it.toLabel() } ?: emptyList()
                val focal = c.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                    ?.map { String.format(Locale.US, "%.1f mm", it) }?.distinct()?.joinToString("، ")
                    ?: "گزارش نشده"
                val apertures = c.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
                    ?.map { String.format(Locale.US, "f/%.2f", it) }?.distinct()?.joinToString("، ")
                    ?: "گزارش نشده"
                val physical = c.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)?.let {
                    String.format(Locale.US, "%.2f × %.2f mm", it.width, it.height)
                } ?: "گزارش نشده"
                val hardware = when (c.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)) {
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> "Legacy"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "Limited"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "Full"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "Level 3"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL -> "External"
                    else -> "نامشخص"
                }
                val af = c.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)
                    ?.toList()?.mapNotNull { mode ->
                        when (mode) {
                            CameraMetadata.CONTROL_AF_MODE_OFF -> "خاموش"
                            CameraMetadata.CONTROL_AF_MODE_AUTO -> "خودکار"
                            CameraMetadata.CONTROL_AF_MODE_MACRO -> "ماکرو"
                            CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO -> "پیوسته ویدئو"
                            CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE -> "پیوسته عکس"
                            CameraMetadata.CONTROL_AF_MODE_EDOF -> "EDOF"
                            else -> null
                        }
                    }?.distinct()?.joinToString("، ") ?: "گزارش نشده"
                val ois = c.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
                    ?.map { if (it == CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_ON) "فعال" else "غیرفعال" }
                    ?.distinct()?.joinToString("، ") ?: "گزارش نشده"
                val videoStab = c.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES)
                    ?.map { if (it == CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_ON) "فعال" else "غیرفعال" }
                    ?.distinct()?.joinToString("، ") ?: "گزارش نشده"
                val caps = c.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
                    ?.toList()?.mapNotNull { capability ->
                        when (capability) {
                            CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE -> "سازگاری پایه"
                            CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR -> "کنترل دستی حسگر"
                            CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_POST_PROCESSING -> "پردازش دستی تصویر"
                            CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_RAW -> "خروجی RAW"
                            CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE -> "ثبت پیاپی"
                            CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT -> "خروجی عمق"
                            CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA -> "چنددوربینه منطقی"
                            CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MOTION_TRACKING -> "ردیابی حرکت"
                            CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MONOCHROME -> "حسگر تک‌رنگ"
                            else -> null
                        }
                    }?.distinct() ?: emptyList()
                CameraReport(
                    id = id,
                    facing = facing,
                    hardwareLevel = hardware,
                    pixelArray = pixel?.let { "${it.width} × ${it.height}" } ?: "گزارش نشده",
                    megapixels = mp,
                    focalLengths = focal,
                    apertures = apertures,
                    physicalSize = physical,
                    flash = if (c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true) "دارد" else "ندارد / گزارش نشده",
                    autofocus = af,
                    opticalStabilization = ois,
                    videoStabilization = videoStab,
                    photoSizes = photoSizes,
                    videoSizes = videoSizes,
                    capabilities = caps
                )
            } catch (e: Exception) {
                CameraReport(id, "دوربین", "نامشخص", "-", "-", "-", "-", "-", "-", "-", "-", "-", emptyList(), emptyList(), emptyList(),
                    "خواندن مشخصات این دوربین ممکن نشد: ${e.localizedMessage ?: "خطای ناشناخته"}")
            }
        }
    } catch (e: CameraAccessException) {
        emptyList()
    } catch (e: SecurityException) {
        emptyList()
    }
}

private fun Size.toLabel(): String = "${width}×${height}"
