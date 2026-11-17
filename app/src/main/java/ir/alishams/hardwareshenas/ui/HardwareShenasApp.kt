package ir.alishams.hardwareshenas.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.sp
import ir.alishams.hardwareshenas.data.*
import ir.alishams.hardwareshenas.ui.theme.*

private data class DashboardModule(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
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
                    containerColor = Color(0xFF0C1421),
                    contentColor = TextPrimary,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Rounded.Dashboard, null) },
                        label = { Text("خانه") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanAccent,
                            selectedTextColor = CyanAccent,
                            indicatorColor = CyanAccent.copy(alpha = .12f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1; showPerformance = true },
                        icon = { Icon(Icons.Rounded.MonitorHeart, null) },
                        label = { Text("پایش زنده") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanAccent, selectedTextColor = CyanAccent,
                            indicatorColor = CyanAccent.copy(alpha = .12f),
                            unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2; showBenchmark = true },
                        icon = { Icon(Icons.Rounded.Speed, null) },
                        label = { Text("آزمایش‌ها") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanAccent, selectedTextColor = CyanAccent,
                            indicatorColor = CyanAccent.copy(alpha = .12f),
                            unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary
                        )
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
                    DashboardModule("پردازنده", "مشخصات، هسته‌ها و فرکانس", Icons.Rounded.Memory, CyanAccent) { showPerformance = true },
                    DashboardModule("حافظه RAM", "${usedRam.toStorageLabel()} مصرف‌شده", Icons.Rounded.Storage, VioletAccent) { showPerformance = true },
                    DashboardModule("پردازنده گرافیکی", "GPU و پشتیبانی Vulkan", Icons.Rounded.GraphicEq, BlueAccent) { showGpu = true },
                    DashboardModule("باتری", "سلامت گزارش‌شده و وضعیت", Icons.Rounded.BatteryChargingFull, Color(0xFFFFC46B)) { showBattery = true },
                    DashboardModule("سنسورها", "${details.sensors.size} سنسور شناسایی‌شده", Icons.Rounded.Sensors, VioletAccent) { showSensors = true },
                    DashboardModule("نمایشگر", "رنگ، نرخ نوسازی و لمس", Icons.Rounded.DisplaySettings, BlueAccent) { showScreenDiagnostics = true },
                    DashboardModule("دوربین", "مشخصات و آزمون زنده", Icons.Rounded.PhotoCamera, CyanAccent) { showCameraDiagnostics = true },
                    DashboardModule("شبکه و ارتباطات", "Wi-Fi، بلوتوث، NFC و GPS", Icons.Rounded.Wifi, BlueAccent) { showConnectivity = true },
                    DashboardModule("ذخیره‌سازی", "${usedStorage.toStorageLabel()} استفاده‌شده", Icons.Rounded.FolderOpen, VioletAccent) { showPerformance = true },
                    DashboardModule("بنچمارک", "سنجش عملکرد CPU و RAM", Icons.Rounded.Speed, Color(0xFFFFC46B)) { showBenchmark = true }
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color(0xFF0B1421), Midnight, Color(0xFF080D16)))
                    ).padding(padding),
                    contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(19.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    "HARDWARE SHENAS  /  CONTROL CENTER",
                                    color = CyanAccent,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.1.sp
                                )
                                Text(
                                    "مرکز کنترل دستگاه",
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    "تمام جزئیات سخت‌افزار، یک‌جا و دقیق",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Box(
                                Modifier.size(54.dp).clip(RoundedCornerShape(18.dp))
                                    .background(Brush.linearGradient(listOf(CyanAccent.copy(.22f), BlueAccent.copy(.13f), VioletAccent.copy(.22f)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Memory, null, tint = CyanAccent, modifier = Modifier.size(30.dp))
                            }
                        }
                    }

                    item { DeviceHero(snapshot) }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                            SectionHeading("نمای کلی سیستم", "SYSTEM SNAPSHOT", Icons.Rounded.Analytics)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                SnapshotTile(
                                    "هسته‌های CPU", details.cpu.coreCount.toString(),
                                    "قابل مشاهده", Icons.Rounded.Memory, CyanAccent, Modifier.weight(1f)
                                )
                                SnapshotTile(
                                    "حافظه RAM", details.memory.totalBytes.toStorageLabel(),
                                    "کل حافظه", Icons.Rounded.DeveloperBoard, VioletAccent, Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                SnapshotTile(
                                    "فضای ذخیره‌سازی", details.storage.totalBytes.toStorageLabel(),
                                    "حافظه داخلی", Icons.Rounded.SdStorage, BlueAccent, Modifier.weight(1f)
                                )
                                SnapshotTile(
                                    "سنسورها", details.sensors.size.toString(),
                                    "سنسور شناسایی‌شده", Icons.Rounded.Sensors, Color(0xFFFFC46B), Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                            SectionHeading("ماژول‌های تخصصی", "HARDWARE MODULES", Icons.Rounded.GridView)
                            Text(
                                "هر ماژول یک بخش مستقل برای بررسی دقیق‌تر دستگاه است.",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                            modules.chunked(2).forEach { rowModules ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(11.dp)
                                ) {
                                    rowModules.forEach { module ->
                                        ModuleCard(module, Modifier.weight(1f))
                                    }
                                    if (rowModules.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    item {
                        InfoPanel(
                            title = "مشخصات پردازنده و حافظه",
                            icon = Icons.Rounded.Memory,
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
                        Column(
                            Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            HorizontalDivider(color = DividerDark.copy(alpha = .7f))
                            Spacer(Modifier.height(7.dp))
                            Text("HARDWARE SHENAS", color = TextSecondary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                            Text("POWERED BY ALISHAMS DIGITAL STUDIO", color = TextSecondary.copy(alpha = .55f), style = MaterialTheme.typography.labelSmall, letterSpacing = .6.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeading(title: String, eyebrow: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(CyanAccent.copy(alpha = .10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = CyanAccent, modifier = Modifier.size(20.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(eyebrow, color = CyanAccent.copy(alpha = .85f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = .8.sp)
            Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DeviceHero(snapshot: DeviceSnapshot) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(27.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, CyanAccent.copy(alpha = .18f))
    ) {
        Box {
            Box(
                Modifier.matchParentSize().background(
                    Brush.linearGradient(
                        listOf(Color(0xFF12343B), Color(0xFF142438), Color(0xFF201D3B))
                    )
                )
            )
            Column(Modifier.padding(19.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = CyanAccent.copy(alpha = .12f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Box(Modifier.size(7.dp).clip(CircleShape).background(CyanAccent))
                            Text("دستگاه متصل", color = CyanAccent, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Rounded.VerifiedUser, null, tint = CyanAccent.copy(alpha = .8f))
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "${snapshot.manufacturer} ${snapshot.model}",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "Android ${snapshot.androidVersion}   •   API ${snapshot.sdkInt}",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                HorizontalDivider(color = Color.White.copy(alpha = .10f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    HeroMetric("هسته CPU", snapshot.processorCount.toString(), Modifier.weight(1f))
                    HeroMetric("شارژ باتری", snapshot.batteryPercent?.let { "$it٪" } ?: "نامشخص", Modifier.weight(1f))
                    HeroMetric("دمای باتری", snapshot.batteryTemperatureC?.let { "$it°" } ?: "نامشخص", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HeroMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(15.dp))
            .background(Color(0xFF081521).copy(alpha = .68f))
            .padding(horizontal = 10.dp, vertical = 11.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, color = TextSecondary, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        Text(value, color = CyanAccent, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun SnapshotTile(
    label: String,
    value: String,
    hint: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(19.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, accent.copy(alpha = .12f))
    ) {
        Row(
            Modifier.fillMaxWidth().padding(13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = .12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(label, color = TextSecondary, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(value, color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(hint, color = accent.copy(alpha = .85f), style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
        }
    }
}

@Composable
private fun ModuleCard(module: DashboardModule, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(139.dp).clickable(onClick = module.action),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, module.accent.copy(alpha = .17f))
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier.align(Alignment.TopEnd).size(74.dp)
                    .clip(CircleShape)
                    .background(module.accent.copy(alpha = .045f))
            )
            Column(
                Modifier.fillMaxSize().padding(13.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Box(
                        Modifier.size(40.dp).clip(RoundedCornerShape(13.dp))
                            .background(module.accent.copy(alpha = .13f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(module.icon, null, tint = module.accent, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Rounded.ArrowOutward, null, tint = TextSecondary.copy(alpha = .7f), modifier = Modifier.size(17.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(module.title, color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(module.subtitle, color = TextSecondary, style = MaterialTheme.typography.labelSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun InfoPanel(title: String, icon: ImageVector, rows: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(21.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, Color.White.copy(alpha = .045f))
    ) {
        Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(35.dp).clip(RoundedCornerShape(11.dp)).background(CyanAccent.copy(alpha = .11f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = CyanAccent, modifier = Modifier.size(19.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            HorizontalDivider(color = DividerDark)
            rows.forEachIndexed { index, row ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(row.first, color = TextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(row.second, color = TextPrimary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                if (index != rows.lastIndex) HorizontalDivider(color = DividerDark.copy(alpha = .55f))
            }
        }
    }
}
