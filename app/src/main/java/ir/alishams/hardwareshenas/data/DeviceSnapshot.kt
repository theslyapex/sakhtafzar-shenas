package ir.alishams.hardwareshenas.data

data class DeviceSnapshot(
    val manufacturer: String,
    val brand: String,
    val model: String,
    val device: String,
    val androidVersion: String,
    val sdkInt: Int,
    val supportedAbis: List<String>,
    val processorCount: Int,
    val totalMemoryBytes: Long?,
    val batteryPercent: Int?,
    val batteryTemperatureC: Float?,
    val batteryTechnology: String?
)
