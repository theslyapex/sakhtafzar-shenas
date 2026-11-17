package ir.alishams.hardwareshenas.ui

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.alishams.hardwareshenas.data.DeviceInfoReader
import ir.alishams.hardwareshenas.data.DeviceSnapshot
import ir.alishams.hardwareshenas.data.HardwareDetailsReader
import ir.alishams.hardwareshenas.data.toStorageLabel
import ir.alishams.hardwareshenas.ui.theme.CyanAccent
import ir.alishams.hardwareshenas.ui.theme.Midnight
import ir.alishams.hardwareshenas.ui.theme.SurfaceDark
import ir.alishams.hardwareshenas.ui.theme.SurfaceElevated
import ir.alishams.hardwareshenas.ui.theme.TextSecondary

private data class DashboardModule(val title: String, val subtitle: String, val icon: ImageVector)

@Composable
fun HardwareShenasApp() {
    val context = LocalContext.current
    var showSensors by remember { mutableStateOf(false) }
    var showGpu by remember { mutableStateOf(false) }
    var showPerformance by remember { mutableStateOf(false) }
    var showBenchmark by remember { mutableStateOf(false) }
    var showBattery by remember { mutableStateOf(false) }
    var showScreenDiagnostics by remember { mutableStateOf(false) }
    var showConnectivity by remember { mutableStateOf(false) }
    var showCameraDiagnostics by remember { mutableStateOf(false) }
    val snapshot = remember { DeviceInfoReader(context).readSnapshot() }
    val details = remember { HardwareDetailsReader(context).read() }
    val usedRam = (details.memory.totalBytes - details.memory.availableBytes).coerceAtLeast(0L)
    val usedStorage = (details.storage.totalBytes - details.storage.availableBytes).coerceAtLeast(0L)
    val modules = listOf(
        DashboardModule("پردازنده", "CPU و فرکانس‌های قابل‌خواندن", Icons.Rounded.Speed),
        DashboardModule("حافظه RAM", "${usedRam.toStorageLabel()} مصرف‌شده", Icons.Rounded.Memory),
        DashboardModule("ذخیره‌سازی", "${usedStorage.toStorageLabel()} استفاده‌شده", Icons.Rounded.Storage),
        DashboardModule("باتری", "شارژ و دمای باتری", Icons.Rounded.BatteryFull),
        DashboardModule("سنسورها", "${details.sensors.size} سنسور شناسایی‌شده", Icons.Rounded.Sensors),
        DashboardModule("دستگاه", "مشخصات سیستم‌عامل", Icons.Rounded.PhoneAndroid)
    )

    Scaffold(containerColor = Midnight) { padding ->
        if (showSensors) {
            SensorMonitorScreen(onBack = { showSensors = false }, modifier = Modifier.padding(padding))
        } else if (showGpu) {
            GpuMonitorScreen(onBack = { showGpu = false }, modifier = Modifier.padding(padding))
        } else if (showPerformance) {
            PerformanceMonitorScreen(onBack = { showPerformance = false }, modifier = Modifier.padding(padding))
        } else if (showBenchmark) {
            BenchmarkScreen(onBack = { showBenchmark = false }, modifier = Modifier.padding(padding))
        } else if (showBattery) {
            BatteryDiagnosticsScreen(onBack = { showBattery = false }, modifier = Modifier.padding(padding))
        } else if (showScreenDiagnostics) {
            ScreenDiagnosticsScreen(onBack = { showScreenDiagnostics = false }, modifier = Modifier.padding(padding))
        } else if (showConnectivity) {
            ConnectivityDiagnosticsScreen(onBack = { showConnectivity = false }, modifier = Modifier.padding(padding))
        } else if (showCameraDiagnostics) {
            CameraDiagnosticsScreen(onBack = { showCameraDiagnostics = false }, modifier = Modifier.padding(padding))
        } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Midnight)
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("سخت‌افزار شناس", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Text("شناخت دقیق‌تر از دستگاهت", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Text(
                        "HS",
                        color = CyanAccent,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.background(SurfaceElevated, RoundedCornerShape(14.dp)).padding(horizontal = 15.dp, vertical = 10.dp)
                    )
                }
            }
            item { DeviceHero(snapshot) }
            item {
                Column {
                    TextButton(onClick = { showSensors = true }) {
                        Text("مشاهده پایش زنده سنسورها", color = CyanAccent, fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onClick = { showGpu = true }) {
                        Text("بررسی پردازنده گرافیکی و Vulkan", color = CyanAccent, fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onClick = { showPerformance = true }) {
                        Text("پایش زنده CPU و RAM", color = CyanAccent, fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onClick = { showBenchmark = true }) {
                        Text("اجرای بنچمارک سخت‌افزار", color = CyanAccent, fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onClick = { showBattery = true }) {
                        Text("تشخیص و سلامت باتری", color = CyanAccent, fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onClick = { showScreenDiagnostics = true }) {
                        Text("آزمون صفحه‌نمایش و لمس", color = CyanAccent, fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onClick = { showConnectivity = true }) {
                        Text("بررسی ارتباطات و شبکه", color = CyanAccent, fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onClick = { showCameraDiagnostics = true }) {
                        Text("بررسی تخصصی دوربین‌ها", color = CyanAccent, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            item {
                Column {
                    Text("نمای کلی سخت‌افزار", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(10.dp))
                    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.height(270.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        userScrollEnabled = false
                    ) {
                        items(modules) { ModuleCard(it) }
                    }
                }
            }
            item {
                InfoPanel(
                    title = "پردازنده",
                    rows = listOf(
                        "مدل SoC" to (details.cpu.socModel ?: "در دسترس نیست"),
                        "شناسه CPU" to (details.cpu.cpuName ?: "در دسترس نیست"),
                        "تعداد هسته‌های قابل‌مشاهده" to details.cpu.coreCount.toString(),
                        "بیشینه فرکانس قابل‌خواندن" to (details.cpu.maxFrequencyMhz?.let { "${it} MHz" } ?: "محدودیت دسترسی سیستم")
                    )
                )
            }
            item {
                InfoPanel(
                    title = "حافظه و ذخیره‌سازی",
                    rows = listOf(
                        "RAM کل" to details.memory.totalBytes.toStorageLabel(),
                        "RAM در دسترس" to details.memory.availableBytes.toStorageLabel(),
                        "حافظه داخلی کل" to details.storage.totalBytes.toStorageLabel(),
                        "حافظه داخلی آزاد" to details.storage.availableBytes.toStorageLabel()
                    )
                )
            }
            item {
                InfoPanel(
                    title = "سنسورهای شناسایی‌شده",
                    rows = details.sensors.take(8).map { it.name to it.vendor } +
                        if (details.sensors.size > 8) listOf("سایر سنسورها" to "${details.sensors.size - 8} مورد دیگر") else emptyList()
                )
            }
            item {
                Text(
                    "POWERED BY ALISHAMS DIGITAL STUDIO",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    color = TextSecondary.copy(alpha = 0.65f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        }
    }
}

@Composable
private fun DeviceHero(snapshot: DeviceSnapshot) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text("${snapshot.manufacturer} ${snapshot.model}", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Text("Android ${snapshot.androidVersion}  •  API ${snapshot.sdkInt}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(18.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Metric("پردازنده منطقی", snapshot.processorCount.toString())
                Metric("باتری", snapshot.batteryPercent?.let { "$it٪" } ?: "نامشخص")
                Metric("دمای باتری", snapshot.batteryTemperatureC?.let { "$it°C" } ?: "نامشخص")
            }
        }
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column {
        Text(label, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(4.dp))
        Text(value, color = CyanAccent, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ModuleCard(module: DashboardModule) {
    Card(
        modifier = Modifier.fillMaxWidth().height(126.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(module.icon, contentDescription = null, tint = CyanAccent)
            Column {
                Text(module.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                Text(module.subtitle, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun InfoPanel(title: String, rows: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            rows.forEachIndexed { index, row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(row.first, color = TextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(row.second, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                }
                if (index != rows.lastIndex) {
                    androidx.compose.material3.HorizontalDivider(color = SurfaceElevated)
                }
            }
        }
    }
}
