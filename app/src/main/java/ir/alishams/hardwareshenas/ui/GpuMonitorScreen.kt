package ir.alishams.hardwareshenas.ui

import android.content.Context
import android.content.pm.PackageManager
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ir.alishams.hardwareshenas.ui.theme.CyanAccent
import ir.alishams.hardwareshenas.ui.theme.Midnight
import ir.alishams.hardwareshenas.ui.theme.SurfaceDark
import ir.alishams.hardwareshenas.ui.theme.TextSecondary
import javax.microedition.khronos.egl.EGLConfig
import kotlinx.coroutines.delay
import javax.microedition.khronos.opengles.GL10
import android.opengl.GLSurfaceView.Renderer

private data class GpuSnapshot(
    val vendor: String?,
    val renderer: String?,
    val glVersion: String?,
    val shadingLanguage: String?
)

private data class VulkanSnapshot(
    val supported: Boolean,
    val hardwareLevel: Int?,
    val apiVersion: String?
)

@Composable
fun GpuMonitorScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val gpu = remember { mutableStateOf<GpuSnapshot?>(null) }
    val vulkan = remember { readVulkanSnapshot(context) }
    val gpuTimedOut = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(6000); if (gpu.value == null) gpuTimedOut.value = true }

    Column(
        modifier = modifier.fillMaxSize().background(Midnight).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HardwarePageTopBar(onBack)

        HardwareSectionHeading("پردازنده گرافیکی")
        Text("اطلاعات از طریق زمینه واقعی OpenGL دستگاه خوانده می‌شود.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.055f))
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("مشخصات OpenGL ES", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                val info = gpu.value
                if (info == null) {
                    Text("در حال ایجاد زمینه گرافیکی و خواندن مشخصات…", color = TextSecondary)
                } else {
                    GpuRow("سازنده GPU", info.vendor ?: "گزارش نشد")
                    GpuRow("مدل / Renderer", info.renderer ?: "گزارش نشد")
                    GpuRow("نسخه OpenGL ES", info.glVersion ?: "گزارش نشد")
                    GpuRow("زبان سایه‌زن", info.shadingLanguage ?: "گزارش نشد")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("پشتیبانی Vulkan", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text(
                    if (vulkan.supported) "این دستگاه قابلیت Vulkan را در اطلاعات سیستم اعلام کرده است."
                    else "پشتیبانی سخت‌افزاری Vulkan توسط سیستم اعلام نشده است.",
                    color = if (vulkan.supported) CyanAccent else TextSecondary
                )
                vulkan.hardwareLevel?.let { GpuRow("سطح سخت‌افزاری اعلام‌شده", it.toString()) }
                vulkan.apiVersion?.let { GpuRow("نسخه Vulkan اعلام‌شده", it) }
                Text(
                    "این اطلاعات از قابلیت‌های اعلام‌شده توسط Android خوانده می‌شود و جایگزین آزمون عملکرد یا بررسی کامل درایور Vulkan نیست.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        AndroidView(
            modifier = Modifier.fillMaxWidth().height(2.dp),
            factory = { viewContext ->
                GLSurfaceView(viewContext).apply {
                    setEGLContextClientVersion(2)
                    setRenderer(object : Renderer {
                        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                            val result = GpuSnapshot(
                                vendor = GLES20.glGetString(GLES20.GL_VENDOR)?.trim(),
                                renderer = GLES20.glGetString(GLES20.GL_RENDERER)?.trim(),
                                glVersion = GLES20.glGetString(GLES20.GL_VERSION)?.trim(),
                                shadingLanguage = GLES20.glGetString(GLES20.GL_SHADING_LANGUAGE_VERSION)?.trim()
                            )
                            Handler(Looper.getMainLooper()).post { gpu.value = result }
                        }

                        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) = Unit
                        override fun onDrawFrame(gl: GL10?) = Unit
                    })
                    renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
                    onResume()
                    requestRender()
                }
            },
            onRelease = { it.onPause() }
        )

        Spacer(Modifier.height(4.dp))
        Text(
            "POWERED BY ALISHAMS DIGITAL STUDIO",
            color = TextSecondary.copy(alpha = 0.65f),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun GpuRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(value, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
}

private fun readVulkanSnapshot(context: Context): VulkanSnapshot {
    val packageManager = context.packageManager
    val features = packageManager.systemAvailableFeatures.orEmpty()
    val level = features.firstOrNull { it.name == PackageManager.FEATURE_VULKAN_HARDWARE_LEVEL }?.version
    val version = features.firstOrNull { it.name == PackageManager.FEATURE_VULKAN_HARDWARE_VERSION }?.version
    val supported = packageManager.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_LEVEL) ||
        packageManager.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_VERSION)
    val versionLabel = version?.takeIf { it > 0 }?.let {
        val major = (it shr 22) and 0x7f
        val minor = (it shr 12) and 0x3ff
        val patch = it and 0xfff
        "$major.$minor.$patch"
    }
    return VulkanSnapshot(supported, level?.takeIf { it >= 0 }, versionLabel)
}
