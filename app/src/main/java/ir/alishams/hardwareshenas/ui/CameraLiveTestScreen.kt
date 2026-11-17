package ir.alishams.hardwareshenas.ui

import android.Manifest
import android.content.pm.PackageManager
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import ir.alishams.hardwareshenas.ui.theme.CyanAccent
import ir.alishams.hardwareshenas.ui.theme.Midnight
import ir.alishams.hardwareshenas.ui.theme.SurfaceDark
import ir.alishams.hardwareshenas.ui.theme.TextSecondary
import java.util.concurrent.TimeUnit

@Composable
fun CameraLiveTestScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    val cameraState = remember { mutableStateOf<Camera?>(null) }
    var torchEnabled by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("برای شروع آزمون، مجوز دوربین را تأیید کن.") }
    var cameraError by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
        status = if (granted) "مجوز دوربین صادر شد." else "مجوز دوربین صادر نشد."
    }

    DisposableEffect(hasPermission, lensFacing, previewView, lifecycleOwner) {
        var boundProvider: ProcessCameraProvider? = null
        var disposed = false
        if (hasPermission && previewView != null) {
            val future = ProcessCameraProvider.getInstance(context)
            future.addListener({
                if (!disposed) {
                    try {
                        val provider = future.get()
                        boundProvider = provider
                        provider.unbindAll()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView!!.surfaceProvider)
                        }
                        val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
                        val boundCamera = provider.bindToLifecycle(lifecycleOwner, selector, preview)
                        cameraState.value = boundCamera
                        torchEnabled = false
                        cameraError = null
                        status = if (lensFacing == CameraSelector.LENS_FACING_BACK) "پیش‌نمایش دوربین پشت فعال است." else "پیش‌نمایش دوربین جلو فعال است."
                    } catch (e: Exception) {
                        cameraState.value = null
                        cameraError = e.localizedMessage ?: "اتصال به دوربین ممکن نشد."
                        status = "راه‌اندازی دوربین ناموفق بود."
                    }
                }
            }, ContextCompat.getMainExecutor(context))
        }
        onDispose {
            disposed = true
            boundProvider?.unbindAll()
            cameraState.value = null
            torchEnabled = false
        }
    }

    Column(
        modifier = modifier.fillMaxSize().background(Midnight).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("آزمون زنده دوربین", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            TextButton(onClick = onBack) { Text("بازگشت", color = CyanAccent) }
        }
        Text("پیش‌نمایش زنده، فوکوس لمسی و فلاش. هیچ تصویری ثبت یا ذخیره نمی‌شود.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        if (!hasPermission) {
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceDark), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("دسترسی به دوربین لازم است", color = MaterialTheme.colorScheme.onSurface)
                    Text("فقط پس از تأیید تو، پیش‌نمایش زنده فعال می‌شود. این بخش عکس یا ویدئو نمی‌گیرد.", color = TextSecondary)
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)) {
                        Text("اجازه دسترسی به دوربین", color = Midnight)
                    }
                }
            }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { lensFacing = CameraSelector.LENS_FACING_BACK }, enabled = lensFacing != CameraSelector.LENS_FACING_BACK,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)) {
                    Text("دوربین پشت", color = Midnight)
                }
                Button(onClick = { lensFacing = CameraSelector.LENS_FACING_FRONT }, enabled = lensFacing != CameraSelector.LENS_FACING_FRONT,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)) {
                    Text("دوربین جلو", color = Midnight)
                }
            }
            Box(Modifier.fillMaxWidth().height(330.dp).background(SurfaceDark, RoundedCornerShape(20.dp))) {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).also { view ->
                            previewView = view
                            view.scaleType = PreviewView.ScaleType.FILL_CENTER
                            view.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            view.setOnTouchListener { _, event ->
                                if (event.action == MotionEvent.ACTION_UP) {
                                    val point = view.meteringPointFactory.createPoint(event.x, event.y)
                                    val activeCamera = cameraState.value
                                    if (point != null && activeCamera != null) {
                                        val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
                                            .setAutoCancelDuration(3, TimeUnit.SECONDS).build()
                                        activeCamera.cameraControl.startFocusAndMetering(action)
                                        status = "درخواست فوکوس روی نقطه لمس‌شده ارسال شد."
                                    }
                                }
                                true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text(status, color = CyanAccent, style = MaterialTheme.typography.bodySmall)
            cameraError?.let { Text("خطا: $it", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            val activeCamera = cameraState.value
            if (activeCamera?.cameraInfo?.hasFlashUnit() == true && lensFacing == CameraSelector.LENS_FACING_BACK) {
                Button(onClick = {
                    val next = !torchEnabled
                    activeCamera.cameraControl.enableTorch(next)
                        .addListener({ torchEnabled = next }, ContextCompat.getMainExecutor(context))
                }, colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark)) {
                    Text(if (torchEnabled) "خاموش کردن فلاش" else "روشن کردن فلاش", color = CyanAccent)
                }
            } else {
                Text("فلاش برای این دوربین در دسترس گزارش نشده است.", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text("نکته: نتیجه فوکوس و کیفیت تصویر به سخت‌افزار، نور محیط و پیاده‌سازی سازنده وابسته است. لمس تصویر فقط درخواست فوکوس می‌فرستد و تضمین فوکوس موفق نیست.",
            color = TextSecondary, style = MaterialTheme.typography.labelSmall)
    }
}
