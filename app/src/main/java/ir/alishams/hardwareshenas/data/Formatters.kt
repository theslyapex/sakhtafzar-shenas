package ir.alishams.hardwareshenas.data

import java.util.Locale

fun Long.toStorageLabel(): String {
    if (this <= 0L) return "نامشخص"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var value = toDouble()
    var index = 0
    while (value >= 1024 && index < units.lastIndex) {
        value /= 1024
        index++
    }
    return String.format(Locale.US, "%.1f %s", value, units[index])
}
