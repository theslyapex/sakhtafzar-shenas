package ir.alishams.hardwareshenas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.alishams.hardwareshenas.data.*
import ir.alishams.hardwareshenas.ui.theme.*

private data class DashboardModule(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: androidx.compose.ui.graphics.Color,
    val action: () -> Unit
)

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
    var showCameraLiveTest by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val snapshot = remember { DeviceInfoReader(context).readSnapshot() }
    val details = remember { HardwareDetailsReader(context).read() }
    val usedRam = (details.memory.totalBytes - details.memory.availableBytes).coerceAtLeast(0L)
    val usedStorage = (details.storage.totalBytes - details.storage.availableBytes).coerceAtLeast(0L)
    val onHome = !(showSensors || showGpu || showPerformance || showBenchmark || showBattery ||
        showScreenDiagnostics || showConnectivity || showCameraDiagnostics || showCameraLiveTest)

    Scaffold(
        containerColor = Midnight,
        bottomBar = {
            if (onHome) {
                NavigationBar(
                    containerColor = SurfaceDark,
                    contentColor = TextPrimary,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Rounded.Dashboard, null) },
                        label = { Text("داشبورد") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1; showPerformance = true },
                        icon = { Icon(Icons.Rounded.MonitorHeart, null) },
                        label = { Text("پایش زنده") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2; showBenchmark = true },
                        icon = { Icon(Icons.Rounded.Speed, null) },
                        label = { Text("آزمایش‌ها") }
                    )
                }
            }
        }
    ) { padding ->
        when {
            showSensors -> SensorMonitorScreen(onBack = { showSensors = false }, modifier = Modifier.padding(padding))
            showGpu -> GpuMonitorScreen(onBack = { showGpu = false }, modifier = Modifier.padding(padding))
            showPerformance -> PerformanceMonitorScreen(onBack = { showPerformance = false; selectedTab = 0 }, modifier = Modifier.padding(padding))
            showBenchmark -> BenchmarkScreen(onBack = { showBenchmark = false; selectedTab = 0 }, modifier = Modifier.padding(padding))
            showBattery -> BatteryDiagnosticsScreen(onBack = { showBattery = false }, modifier = Modifier.padding(padding))
            showScreenDiagnostics -> ScreenDiagnosticsScreen(onBack = { showScreenDiagnostics = false }, modifier = Modifier.padding(padding))
            showConnectivity -> ConnectivityDiagnosticsScreen(onBack = { showConnectivity = false }, modifier = Modifier.padding(padding))
            showCameraDiagnostics -> CameraDiagnosticsScreen(
                onBack = { showCameraDiagnostics = false },
                onOpenLiveTest = { showCameraLiveTest = true },
                modifier = Modifier.padding(padding)
            )
            showCameraLiveTest -> CameraLiveTestScreen(onBack = { showCameraLiveTest = false }, modifier = Modifier.padding(padding))
            else -> {
                val modules = listOf(
                    DashboardModule("پردازنده", "مشخصات و فرکانس", Icons.Rounded.Memory, CyanAccent) { showPerformance = true },
                    DashboardModule("حافظه RAM", "${usedRam.toStorageLabel()} مصرف‌شده", Icons.Rounded.Storage, VioletAccent) { showPerformance = true },
                    DashboardModule("پردازنده گرافیکی", "GPU و پشتیبانی Vulkan", Icons.Rounded.GraphicEq, BlueAccent) { showGpu = true },
                    DashboardModule("باتری", "وضعیت و شاخص‌های باتری", Icons.Rounded.BatteryChargingFull, androidx.compose.ui.graphics.Color(0xFFFFC46B)) { showBattery = true },
                    DashboardModule("سنسورها", "${details.sensors.size} سنسور شناسایی‌شده", Icons.Rounded.Sensors, VioletAccent) { showSensors = true },
                    DashboardModule("نمایشگر", "رنگ، نرخ نوسازی و لمس", Icons.Rounded.DisplaySettings, BlueAccent) { showScreenDiagnostics = true },
                    DashboardModule("دوربین", "مشخصات و آزمون زنده", Icons.Rounded.PhotoCamera, CyanAccent) { showCameraDiagnostics = true },
                    DashboardModule("شبکه و ارتباطات", "Wi-Fi، بلوتوث، NFC و GPS", Icons.Rounded.Wifi, BlueAccent) { showConnectivity = true },
                    DashboardModule("ذخیره‌سازی", "${usedStorage.toStorageLabel()} استفاده‌شده", Icons.Rounded.FolderOpen, VioletAccent) { showPerformance = true },
                    DashboardModule("بنچمارک", "سنجش عملکرد CPU و RAM", Icons.Rounded.Speed, androidx.compose.ui.graphics.Color(0xFFFFC46B)) { showBenchmark = true }
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize().background(Midnight).padding(padding),
                    contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 22.dp, bottom = 26.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.weight(1f)) {
                                Text("HARDWARE SHENAS", color = CyanAccent, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text("مرکز کنترل دستگاه", color = TextPrimary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                                Text("تحلیل دقیق سخت‌افزار، در یک نگاه", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                            Box(
                                Modifier.size(52.dp).clip(RoundedCornerShape(17.dp))
                                    .background(Brush.linearGradient(listOf(CyanAccent.copy(alpha = .24f), VioletAccent.copy(alpha = .22f)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Memory, null, tint = CyanAccent, modifier = Modifier.size(29.dp))
                            }
                        }
                    }
                    item { DeviceHero(snapshot) }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text("ماژول‌های سخت‌افزاری", color = TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text("برای ورود به هر بخش، ماژول را انتخاب کن", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                            Surface(color = ColorPrimaryContainer, shape = CircleShape) {
                                Text("10 MODULES", color = CyanAccent, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                            }
                        }
                    }
                    item {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxWidth().height(560.dp),
                            verticalArrangement = Arrangement.spacedBy(11.dp),
                            horizontalArrangement = Arrangement.spacedBy(11.dp),
                            userScrollEnabled = false
                        ) {
                            items(modules) { ModuleCard(it) }
                        }
                    }
                    item {
                        InfoPanel(
                            title = "خلاصه مشخصات دستگاه",
                            icon = Icons.Rounded.Info,
                            rows = listOf(
                                "مدل پردازنده" to (details.cpu.socModel ?: "در دسترس نیست"),
                                "شناسه CPU" to (details.cpu.cpuName ?: "در دسترس نیست"),
                                "هسته‌های قابل‌مشاهده" to details.cpu.coreCount.toString(),
                                "بیشینه فرکانس" to (details.cpu.maxFrequencyMhz?.let { "${it} MHz" } ?: "محدودیت دسترسی سیستم"),
                                "حافظه RAM کل" to details.memory.totalBytes.toStorageLabel(),
                                "فضای ذخیره‌سازی کل" to details.storage.totalBytes.toStorageLabel()
                            )
                        )
                    }
                    item {
                        InfoPanel(
                            title = "سنسورهای شناسایی‌شده",
                            icon = Icons.Rounded.Sensors,
                            rows = details.sensors.take(6).map { it.name to it.vendor } +
                                if (details.sensors.size > 6) listOf("سایر سنسورها" to "${details.sensors.size - 6} مورد دیگر") else emptyList()
                        )
                    }
                    item {
                        Column(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("HARDWARE SHENAS", color = TextSecondary.copy(alpha = .8f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text("POWERED BY ALISHAMS DIGITAL STUDIO", color = TextSecondary.copy(alpha = .55f), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceHero(snapshot: DeviceSnapshot) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Box(Modifier.background(Brush.linearGradient(listOf(Color(0xFF153A42), SurfaceDark, Color(0xFF20203B))))) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = CyanAccent.copy(alpha = .14f), shape = CircleShape) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(7.dp).clip(CircleShape).background(CyanAccent))
                            Spacer(Modifier.width(7.dp))
                            Text("DEVICE OVERVIEW", color = CyanAccent, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Rounded.VerifiedUser, null, tint = CyanAccent.copy(alpha = .8f))
                }
                Spacer(Modifier.height(15.dp))
                Text("${snapshot.manufacturer} ${snapshot.model}", color = TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("Android ${snapshot.androidVersion}  •  API ${snapshot.sdkInt}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    HeroMetric("هسته CPU", snapshot.processorCount.toString(), Modifier.weight(1f))
                    HeroMetric("باتری", snapshot.batteryPercent?.let { "$it٪" } ?: "N/A", Modifier.weight(1f))
                    HeroMetric("دمای باتری", snapshot.batteryTemperatureC?.let { "$it°" } ?: "N/A", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HeroMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(15.dp)).background(Color(0xFF0B1724).copy(alpha = .72f)).padding(11.dp)) {
        Text(label, color = TextSecondary, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        Spacer(Modifier.height(4.dp))
        Text(value, color = CyanAccent, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun ModuleCard(module: DashboardModule) {
    Card(
        modifier = Modifier.fillMaxWidth().height(132.dp).clickable(onClick = module.action),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, module.accent.copy(alpha = .13f))
    ) {
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Box(Modifier.size(39.dp).clip(RoundedCornerShape(13.dp)).background(module.accent.copy(alpha = .13f)), contentAlignment = Alignment.Center) {
                    Icon(module.icon, null, tint = module.accent, modifier = Modifier.size(22.dp))
                }
                Icon(Icons.Rounded.ArrowOutward, null, tint = TextSecondary.copy(alpha = .65f), modifier = Modifier.size(17.dp))
            }
            Column {
                Text(module.title, color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(3.dp))
                Text(module.subtitle, color = TextSecondary, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun InfoPanel(title: String, icon: ImageVector, rows: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(21.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(9.dp))
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            HorizontalDivider(color = DividerDark)
            rows.forEachIndexed { index, row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(row.first, color = TextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(row.second, color = TextPrimary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                }
                if (index != rows.lastIndex) HorizontalDivider(color = DividerDark.copy(alpha = .55f))
            }
        }
    }
}
