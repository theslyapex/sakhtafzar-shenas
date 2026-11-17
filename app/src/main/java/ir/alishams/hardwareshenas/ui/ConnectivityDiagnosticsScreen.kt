package ir.alishams.hardwareshenas.ui

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.nfc.NfcAdapter
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.alishams.hardwareshenas.ui.theme.CyanAccent
import ir.alishams.hardwareshenas.ui.theme.Midnight
import ir.alishams.hardwareshenas.ui.theme.SurfaceDark
import ir.alishams.hardwareshenas.ui.theme.TextSecondary

private data class ConnectivitySnapshot(
    val activeNetwork: String,
    val internet: String,
    val validated: String,
    val metered: String,
    val wifiEnabled: String,
    val bluetooth: String,
    val nfc: String,
    val gps: String,
    val sim: String,
    val supportsWifi: Boolean,
    val supportsBluetooth: Boolean,
    val supportsNfc: Boolean,
    val supportsGps: Boolean,
    val supportsTelephony: Boolean
)

@Composable
fun ConnectivityDiagnosticsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var snapshot by remember { mutableStateOf(readConnectivitySnapshot(context)) }
    val bluetoothPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val hasBluetoothPermission = !bluetoothPermission ||
        context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { snapshot = readConnectivitySnapshot(context) }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(Midnight).padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            HardwarePageTopBar(onBack)
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                HardwareSectionHeading("ارتباطات و شبکه")
                Text("وضعیت اتصال فعال و قابلیت‌های ارتباطی دستگاه از رابط‌های استاندارد Android خوانده می‌شود.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            DiagnosticCard("اتصال فعلی", listOf(
                "نوع اتصال فعال" to snapshot.activeNetwork,
                "دسترسی اینترنت" to snapshot.internet,
                "اعتبارسنجی اینترنت توسط Android" to snapshot.validated,
                "نوع مصرف شبکه" to snapshot.metered
            ))
        }
        item {
            DiagnosticCard("وضعیت رابط‌های ارتباطی", listOf(
                "Wi-Fi" to snapshot.wifiEnabled,
                "Bluetooth" to snapshot.bluetooth,
                "NFC" to snapshot.nfc,
                "موقعیت‌یاب GPS" to snapshot.gps
            ))
        }
        item {
            DiagnosticCard("قابلیت‌های سخت‌افزاری", listOf(
                "پشتیبانی Wi-Fi" to yesNo(snapshot.supportsWifi),
                "پشتیبانی Bluetooth" to yesNo(snapshot.supportsBluetooth),
                "پشتیبانی NFC" to yesNo(snapshot.supportsNfc),
                "گیرنده GPS" to yesNo(snapshot.supportsGps),
                "شبکه موبایل" to yesNo(snapshot.supportsTelephony),
                "وضعیت سیم‌کارت" to snapshot.sim
            ))
        }
        if (bluetoothPermission && !hasBluetoothPermission && snapshot.supportsBluetooth) {
            item {
                OutlinedButton(
                    onClick = { permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("اعطای مجوز برای خواندن وضعیت Bluetooth") }
            }
        }
        item {
            OutlinedButton(
                onClick = { snapshot = readConnectivitySnapshot(context) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("به‌روزرسانی وضعیت") }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.055f))
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("حریم خصوصی", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Text(
                        "این صفحه نام شبکه Wi-Fi، نشانی IP، MAC، IMEI، شماره تلفن، شناسه سیم‌کارت یا رمز عبور را نمی‌خواند. برای اطلاعات سیم‌کارت فقط وضعیت کلی آماده‌بودن آن نمایش داده می‌شود.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        item {
            Spacer(Modifier.height(2.dp))
            Text("POWERED BY ALISHAMS DIGITAL STUDIO", color = TextSecondary.copy(alpha = 0.65f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun DiagnosticCard(title: String, rows: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            rows.forEach { (label, value) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, color = TextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(value, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private fun readConnectivitySnapshot(context: Context): ConnectivitySnapshot {
    val pm = context.packageManager
    val supportsWifi = pm.hasSystemFeature(PackageManager.FEATURE_WIFI)
    val supportsBluetooth = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
    val supportsNfc = pm.hasSystemFeature(PackageManager.FEATURE_NFC)
    val supportsGps = pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
    val supportsTelephony = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)

    val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivity.activeNetwork
    val caps = network?.let { connectivity.getNetworkCapabilities(it) }
    val transports = mutableListOf<String>()
    if (caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) transports += "Wi-Fi"
    if (caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true) transports += "شبکه موبایل"
    if (caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true) transports += "Ethernet"
    if (caps?.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) == true) transports += "Bluetooth"
    if (caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true) transports += "VPN"
    val active = transports.joinToString(" + ").ifBlank { "بدون اتصال فعال یا نامشخص" }

    val wifiEnabled = runCatching {
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wifi.isWifiEnabled) "روشن" else "خاموش"
    }.getOrDefault("نامشخص")

    val bluetoothState = runCatching {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = manager.adapter
        when {
            adapter == null -> "سخت‌افزار موجود نیست"
            adapter.isEnabled -> "روشن"
            else -> "خاموش"
        }
    }.getOrElse {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) "نیازمند مجوز" else "نامشخص"
    }

    val nfcState = runCatching {
        val adapter = NfcAdapter.getDefaultAdapter(context)
        when {
            adapter == null -> "پشتیبانی نمی‌شود"
            adapter.isEnabled -> "روشن"
            else -> "خاموش"
        }
    }.getOrDefault("نامشخص")

    val gpsState = runCatching {
        val location = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (location.isLocationEnabled) "موقعیت‌یابی روشن" else "موقعیت‌یابی خاموش"
        } else {
            if (location.isProviderEnabled(LocationManager.GPS_PROVIDER)) "روشن" else "خاموش"
        }
    }.getOrDefault("نامشخص")

    val simState = if (!supportsTelephony) {
        "شبکه موبایل پشتیبانی نمی‌شود"
    } else {
        runCatching {
            val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            when (telephony.simState) {
                TelephonyManager.SIM_STATE_READY -> "سیم‌کارت آماده"
                TelephonyManager.SIM_STATE_ABSENT -> "سیم‌کارت وجود ندارد"
                TelephonyManager.SIM_STATE_PIN_REQUIRED -> "نیازمند PIN"
                TelephonyManager.SIM_STATE_PUK_REQUIRED -> "نیازمند PUK"
                TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "قفل شبکه"
                TelephonyManager.SIM_STATE_UNKNOWN -> "نامشخص"
                else -> "وضعیت دیگر"
            }
        }.getOrDefault("نامشخص")
    }

    return ConnectivitySnapshot(
        activeNetwork = active,
        internet = if (caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true) "قابلیت اینترنت موجود است" else "اینترنت در دسترس نیست",
        validated = if (caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true) "تأیید شده" else "تأیید نشده",
        metered = when {
            caps == null -> "نامشخص"
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) -> "غیرحجمی / نامحدود از دید سیستم"
            else -> "احتمالاً حجمی"
        },
        wifiEnabled = wifiEnabled,
        bluetooth = bluetoothState,
        nfc = nfcState,
        gps = gpsState,
        sim = simState,
        supportsWifi = supportsWifi,
        supportsBluetooth = supportsBluetooth,
        supportsNfc = supportsNfc,
        supportsGps = supportsGps,
        supportsTelephony = supportsTelephony
    )
}

private fun yesNo(value: Boolean) = if (value) "پشتیبانی می‌شود" else "پشتیبانی نمی‌شود"
