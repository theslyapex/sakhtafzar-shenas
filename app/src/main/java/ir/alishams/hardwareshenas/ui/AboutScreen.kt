package ir.alishams.hardwareshenas.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Studio
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.alishams.hardwareshenas.ui.theme.CyanAccent
import ir.alishams.hardwareshenas.ui.theme.Midnight
import ir.alishams.hardwareshenas.ui.theme.SurfaceDark
import ir.alishams.hardwareshenas.ui.theme.TextSecondary

@Composable
fun AboutScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(
        modifier = modifier.fillMaxSize().background(Midnight)
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HardwarePageTopBar(onBack)
        HardwareSectionHeading("درباره ما", eyebrow = "ABOUT HARDWARE SHENAS")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Rounded.Memory, null, tint = CyanAccent, modifier = Modifier.height(38.dp))
                Text("سخت‌افزار شناس", color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Text(
                    "سخت‌افزار شناس با هدف ارائه اطلاعات روشن و کاربردی از اجزای سخت‌افزاری و وضعیت عملکرد دستگاه‌های اندرویدی توسعه داده شده است. تلاش ما این است که ابزارهای تشخیصی، پایش و آزمون‌های دستگاه را در محیطی یکپارچه و قابل فهم کنار هم قرار دهیم.",
                    color = TextSecondary, style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Rounded.Studio, null, tint = CyanAccent, modifier = Modifier.height(34.dp))
                Text("استودیو دیجیتال علی شمس", color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Alishams Digital Studio یک مجموعه مستقل در حوزه طراحی و توسعه محصولات دیجیتال، طراحی وب‌سایت و ابزارهای نرم‌افزاری است. سخت‌افزار شناس یکی از پروژه‌های نرم‌افزاری این مجموعه است.",
                    color = TextSecondary, style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(2.dp))
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://alishams.ir"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Midnight)
                ) {
                    Icon(Icons.Rounded.Language, null)
                    Text("مشاهده وب‌سایت alishams.ir", fontWeight = FontWeight.Bold)
                    Icon(Icons.Rounded.OpenInNew, null)
                }
            }
        }
        Text(
            "طراحی و توسعه با تمرکز بر خوانایی اطلاعات، شفافیت محدودیت‌های فنی و احترام به حریم خصوصی کاربر.",
            color = TextSecondary, style = MaterialTheme.typography.bodySmall
        )
        Text("POWERED BY ALISHAMS DIGITAL STUDIO", color = TextSecondary.copy(alpha = .65f),
            style = MaterialTheme.typography.labelSmall)
    }
}
