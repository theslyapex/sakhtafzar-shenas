package ir.alishams.hardwareshenas.data

import android.app.ActivityManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import java.io.File
import java.util.Locale

data class CpuDetails(val socModel: String?, val cpuName: String?, val coreCount: Int, val maxFrequencyMhz: Int?, val currentFrequenciesMhz: List<Int>)
data class MemoryDetails(val totalBytes: Long, val availableBytes: Long, val lowMemory: Boolean)
data class StorageDetails(val totalBytes: Long, val availableBytes: Long)
data class SensorDetails(val name: String, val vendor: String, val type: Int, val wakeUp: Boolean, val maximumRange: Float, val resolution: Float)
data class HardwareDetails(val cpu: CpuDetails, val memory: MemoryDetails, val storage: StorageDetails, val sensors: List<SensorDetails>)

class HardwareDetailsReader(private val context: Context) {
    fun read(): HardwareDetails {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memory = ActivityManager.MemoryInfo()
        manager.getMemoryInfo(memory)
        val stat = StatFs(Environment.getDataDirectory().absolutePath)
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        return HardwareDetails(
            cpu = readCpu(),
            memory = MemoryDetails(memory.totalMem, memory.availMem, memory.lowMemory),
            storage = StorageDetails(stat.totalBytes, stat.availableBytes),
            sensors = sensorManager.getSensorList(Sensor.TYPE_ALL).map {
                SensorDetails(it.name, it.vendor, it.type, it.isWakeUpSensor, it.maximumRange, it.resolution)
            }
        )
    }

    private fun readCpu(): CpuDetails {
        val cpuInfo = readText("/proc/cpuinfo").orEmpty()
        val cpuName = cpuInfo.lineSequence().firstOrNull {
            val line = it.lowercase(Locale.ROOT)
            line.startsWith("model name") || line.startsWith("hardware") || (line.startsWith("processor") && it.contains(':'))
        }?.substringAfter(':')?.trim()?.takeIf { it.isNotBlank() }
        val coreCount = Runtime.getRuntime().availableProcessors()
        val current = (0 until coreCount).mapNotNull { core ->
            readText("/sys/devices/system/cpu/cpu$core/cpufreq/scaling_cur_freq")?.trim()?.toIntOrNull()?.div(1000)
        }
        val maximum = (0 until coreCount).mapNotNull { core ->
            readText("/sys/devices/system/cpu/cpu$core/cpufreq/cpuinfo_max_freq")?.trim()?.toIntOrNull()?.div(1000)
        }.maxOrNull()
        val soc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Build.SOC_MODEL.takeIf { it.isNotBlank() } else null
        return CpuDetails(soc, cpuName, coreCount, maximum, current)
    }

    private fun readText(path: String): String? = runCatching {
        File(path).takeIf { it.canRead() }?.readText()
    }.getOrNull()
}
