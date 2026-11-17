package ir.alishams.hardwareshenas.ui

import android.app.ActivityManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.alishams.hardwareshenas.data.DeviceInfoReader
import ir.alishams.hardwareshenas.data.DeviceSnapshot
import ir.alishams.hardwareshenas.ui.theme.CyanAccent
import ir.alishams.hardwareshenas.ui.theme.Midnight
import ir.alishams.hardwareshenas.ui.theme.SurfaceDark
import ir.alishams.hardwareshenas.ui.theme.SurfaceElevated
import ir.alishams.hardwareshenas.ui.theme.TextSecondary

private data class DashboardModule(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

@Composable
fun HardwareShenasApp() {
    val context = LocalContext.current
    val snapshot = remember { DeviceInfoReader(context).readSnapshot() }

    Scaffold(containerColor = Midnight) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Midnight)
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(22.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "سخت‌افزار شناس",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "شناخت دقیق‌تر از دستگاهت",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Text(
                    text = "HS",
                    color = CyanAccent,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .background(SurfaceElevated, RoundedCornerShape(14.dp))
                        .padding(horizontal = 15.dp, vertical = 10.dp)
                )
            }

            Spacer(Modifier.height(22.dp))
            DeviceHero(snapshot)
            Spacer(Modifier.height(18.dp))

            Text(
                text = "بخش‌های دستگاه",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(10.dp))

            val modules = listOf(
                DashboardModule("پردازنده", "اطلاعات CPU و هسته‌ها", Icons.Rounded.Speed),
                DashboardModule("حافظه RAM", "وضعیت حافظه سیستم", Icons.Rounded.Memory),
                DashboardModule("باتری", "شارژ و دمای باتری", Icons.Rounded.BatteryFull),
                DashboardModule("دستگاه", "مشخصات سیستم‌عامل", Icons.Rounded.PhoneAndroid)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(modules) { module ->
                    ModuleCard(module)
                }
            }
            Text(
                text = "POWERED BY ALISHAMS DIGITAL STUDIO",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                color = TextSecondary.copy(alpha = 0.65f),
                style = MaterialTheme.typography.labelSmall
            )
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
            Text(
                text = "${snapshot.manufacturer} ${snapshot.model}",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = "Android ${snapshot.androidVersion}  •  API ${snapshot.sdkInt}",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(module.icon, contentDescription = null, tint = CyanAccent)
            Column {
                Text(module.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                Text(module.subtitle, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
