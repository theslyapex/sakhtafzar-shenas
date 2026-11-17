package ir.alishams.hardwareshenas.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.AudioAttributes
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import ir.alishams.hardwareshenas.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun PhysicalTestsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("آماده اجرای آزمون‌ها") }
    var recording by remember { mutableStateOf(false) }
    var torchOn by remember { mutableStateOf(false) }
    var selectedCameraId by remember { mutableStateOf<String?>(null) }
    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) runMicTest(context, scope, { recording = it }, { status = it })
        else status = "مجوز میکروفون داده نشد"
    }

    DisposableEffect(Unit) {
        onDispose { runCatching { selectedCameraId?.let { cameraManager.setTorchMode(it, false) } } }
    }

    Column(
        modifier.fillMaxSize().background(Midnight).verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HardwarePageTopBar(onBack)
        HardwareSectionHeading("آزمایش سخت‌افزار فیزیکی", eyebrow = "PHYSICAL HARDWARE TESTS")
        Text("آزمون‌های تعاملی بلندگو، میکروفون، ویبره و فلش. هر آزمایش فقط با لمس دکمه اجرا می‌شود.",
            color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        PhysicalTestCard("بلندگو و خروجی صدا", "پخش صدای آزمایشی با فرکانس ۴۴۰ هرتز برای بررسی شنیداری بلندگو.") {
            scope.launch {
                status = "در حال پخش صدای آزمایشی"
                val result = withContext(Dispatchers.Default) { playTone() }
                status = result
            }
        }
        PhysicalTestCard("میکروفون", "ضبط کوتاه محلی برای بررسی دریافت صدا. فایل ضبط‌شده ذخیره یا ارسال نمی‌شود.") {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                runMicTest(context, scope, { recording = it }, { status = it })
            } else micPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
        PhysicalTestCard("موتور ویبره", "یک الگوی لرزش کوتاه برای بررسی عملکرد موتور ویبره.") {
            status = vibrate(context)
        }
        PhysicalTestCard("فلش LED", "فلش دوربین را روشن یا خاموش کن. در صورت پشتیبانی دستگاه.") {
            try {
                val id = selectedCameraId ?: cameraManager.cameraIdList.firstOrNull { cameraId ->
                    cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                }
                if (id == null) status = "فلش در این دستگاه شناسایی نشد"
                else {
                    val next = !torchOn
                    cameraManager.setTorchMode(id, next)
                    selectedCameraId = id
                    torchOn = next
                    status = if (next) "فلش روشن است" else "فلش خاموش است"
                }
            } catch (e: Exception) { status = "کنترل فلش ناموفق بود: ${e.javaClass.simpleName}" }
        }
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("وضعیت آزمون", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                Text(status, color = CyanAccent, style = MaterialTheme.typography.bodyMedium)
                if (recording) LinearProgressIndicator(Modifier.fillMaxWidth(), color = CyanAccent)
                Text("نتیجه آزمون بلندگو و ویبره باید توسط کاربر شنیده یا احساس شود؛ برنامه نمی‌تواند خرابی فیزیکی این قطعات را به‌طور خودکار تأیید کند.",
                    color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
        Text("حریم خصوصی: ضبط میکروفون فقط پس از اجازه صریح کاربر و فقط برای اندازه‌گیری کوتاه در حافظه انجام می‌شود. هیچ صدایی ذخیره یا آپلود نمی‌شود.",
            color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun PhysicalTestCard(title: String, description: String, action: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
            Text(description, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            Button(onClick = action, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Midnight)) {
                Text("اجرای آزمون")
            }
        }
    }
}

private fun playTone(): String {
    val sampleRate = 44100
    val duration = 1.5
    val count = (sampleRate * duration).toInt()
    val samples = ShortArray(count) { i ->
        (sin(2.0 * PI * 440.0 * i / sampleRate) * Short.MAX_VALUE * 0.22).toInt().toShort()
    }
    return try {
        val track = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setBufferSizeInBytes(samples.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC).build()
        track.write(samples, 0, samples.size)
        track.play()
        Thread.sleep((duration * 1000).toLong() + 150)
        track.stop()
        track.release()
        "پخش آزمایشی تمام شد. خروجی صدا را بررسی کن."
    } catch (e: Exception) { "پخش صدا ناموفق بود: ${e.javaClass.simpleName}" }
}

private fun runMicTest(context: Context, scope: kotlinx.coroutines.CoroutineScope,
                       setRecording: (Boolean) -> Unit, setStatus: (String) -> Unit) {
    scope.launch {
        setRecording(true)
        setStatus("در حال نمونه‌برداری میکروفون")
        val result = withContext(Dispatchers.IO) {
            val rate = 16000
            val min = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            if (min <= 0) return@withContext "تنظیمات ضبط روی این دستگاه پشتیبانی نمی‌شود"
            var recorder: AudioRecord? = null
            try {
                recorder = AudioRecord(MediaRecorder.AudioSource.MIC, rate, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, maxOf(min, rate))
                if (recorder.state != AudioRecord.STATE_INITIALIZED) return@withContext "میکروفون قابل راه‌اندازی نیست"
                val buffer = ShortArray(rate)
                recorder.startRecording()
                val read = recorder.read(buffer, 0, buffer.size)
                recorder.stop()
                if (read <= 0) "داده‌ای از میکروفون دریافت نشد"
                else {
                    var peak = 0
                    for (i in 0 until read) peak = maxOf(peak, kotlin.math.abs(buffer[i].toInt()))
                    "دریافت صدا انجام شد. سطح اوج سیگنال: ${peak * 100 / 32767}%"
                }
            } catch (e: Exception) { "آزمون میکروفون ناموفق بود: ${e.javaClass.simpleName}" }
            finally { runCatching { recorder?.release() } }
        }
        setRecording(false)
        setStatus(result)
    }
}

private fun vibrate(context: Context): String = try {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else @Suppress("DEPRECATION") (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
    if (!vibrator.hasVibrator()) "موتور ویبره شناسایی نشد"
    else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vibrator.vibrate(VibrationEffect.createOneShot(350, VibrationEffect.DEFAULT_AMPLITUDE))
        else @Suppress("DEPRECATION") vibrator.vibrate(350)
        "فرمان لرزش ارسال شد. عملکرد ویبره را بررسی کن."
    }
} 
