package ir.alishams.hardwareshenas.data

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Debug
import kotlin.math.roundToInt

class DeviceInfoReader(private val context: Context) {

    fun readSnapshot(): DeviceSnapshot {
        val memoryInfo = ActivityManager.MemoryInfo()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.getMemoryInfo(memoryInfo)

        val batteryIntent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPercent = if (level >= 0 && scale > 0) {
            (level * 100f / scale).roundToInt()
        } else null

        val temperatureRaw = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        val temperature = if (temperatureRaw >= 0) temperatureRaw / 10f else null

        return DeviceSnapshot(
            manufacturer = Build.MANUFACTURER.orUnknown(),
            brand = Build.BRAND.orUnknown(),
            model = Build.MODEL.orUnknown(),
            device = Build.DEVICE.orUnknown(),
            androidVersion = Build.VERSION.RELEASE.orUnknown(),
            sdkInt = Build.VERSION.SDK_INT,
            supportedAbis = Build.SUPPORTED_ABIS.toList(),
            processorCount = Runtime.getRuntime().availableProcessors(),
            totalMemoryBytes = memoryInfo.totalMem.takeIf { it > 0L },
            batteryPercent = batteryPercent,
            batteryTemperatureC = temperature,
            batteryTechnology = batteryIntent
                ?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
                ?.takeIf { it.isNotBlank() }
        )
    }

    private fun String?.orUnknown(): String = this?.takeIf { it.isNotBlank() } ?: "نامشخص"
}
