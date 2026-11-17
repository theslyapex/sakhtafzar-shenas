package ir.alishams.hardwareshenas.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.alishams.hardwareshenas.ui.theme.CyanAccent
import ir.alishams.hardwareshenas.ui.theme.Midnight
import ir.alishams.hardwareshenas.ui.theme.SurfaceDark
import ir.alishams.hardwareshenas.ui.theme.TextSecondary
import java.util.Locale

private data class LiveSensorReading(
    val sensor: Sensor,
    val values: List<Float>,
    val accuracy: Int
)

@Composable
fun SensorMonitorScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val sensors = remember {
        listOf(
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_LIGHT,
            Sensor.TYPE_PROXIMITY,
            Sensor.TYPE_PRESSURE,
            Sensor.TYPE_GRAVITY,
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_ROTATION_VECTOR
        ).mapNotNull { sensorManager.getDefaultSensor(it) }
            .distinctBy { it.type }
    }
    val readings = remember { mutableStateMapOf<Int, LiveSensorReading>() }

    DisposableEffect(sensorManager, sensors) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                readings[event.sensor.type] = LiveSensorReading(
                    sensor = event.sensor,
                    values = event.values.toList(),
                    accuracy = event.accuracy
                )
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                val old = readings[sensor.type] ?: return
                readings[sensor.type] = old.copy(accuracy = accuracy)
            }
        }
        sensors.forEach { sensor ->
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        onDispose { sensorManager.unregisterListener(listener) }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(Midnight),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            HardwarePageTopBar(onBack)
            Spacer(Modifier.height(8.dp))
            HardwareSectionHeading("پایش زنده سنسورها")
            Text("مقادیر واقعی حسگرها، در صورت پشتیبانی دستگاه", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
        if (sensors.isEmpty()) {
            item {
                SensorCard {
                    Text("حسگر سازگار برای نمایش زنده پیدا نشد.", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        } else {
            items(sensors, key = { it.type }) { sensor ->
                val reading = readings[sensor.type]
                SensorCard {
                    Text(sensor.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(sensor.vendor, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(12.dp))
                    if (reading == null) {
                        Text("هنوز داده زنده‌ای از این حسگر دریافت نشده است", color = TextSecondary)
                    } else {
                        reading.values.forEachIndexed { index, value ->
                            val axis = when (index) {
                                0 -> "X"
                                1 -> "Y"
                                2 -> "Z"
                                else -> "مقدار ${index + 1}"
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(axis, color = CyanAccent, fontWeight = FontWeight.SemiBold)
                                Text(String.format(Locale.getDefault(), "%.4f", value), color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("دقت گزارش‌شده: ${accuracyLabel(reading.accuracy)}", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("حداکثر دامنه: ${sensor.maximumRange}  •  تفکیک‌پذیری: ${sensor.resolution}", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        item {
            Text(
                "برای کاهش مصرف باتری، دریافت داده فقط هنگام باز بودن این صفحه فعال است.",
                color = TextSecondary,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun SensorCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.055f))
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

private fun accuracyLabel(value: Int): String = when (value) {
    SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "زیاد"
    SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "متوسط"
    SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "کم"
    SensorManager.SENSOR_STATUS_UNRELIABLE -> "نامطمئن"
    else -> "نامشخص"
}
