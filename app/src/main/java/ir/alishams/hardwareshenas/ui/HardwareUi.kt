package ir.alishams.hardwareshenas.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.alishams.hardwareshenas.ui.theme.CyanAccent
import ir.alishams.hardwareshenas.ui.theme.SurfaceDark
import ir.alishams.hardwareshenas.ui.theme.TextSecondary

@Composable
fun HardwarePageTopBar(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("HARDWARE SHENAS  /  MODULE", color = CyanAccent, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text("مرکز تشخیص سخت‌افزار", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
        }
        Surface(
            modifier = Modifier.clip(RoundedCornerShape(13.dp)).clickable(onClick = onBack),
            color = SurfaceDark,
            shape = RoundedCornerShape(13.dp),
            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.22f))
        ) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("بازگشت", color = CyanAccent, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(6.dp))
                Text("‹", color = CyanAccent, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HardwareSectionHeading(title: String, modifier: Modifier = Modifier, eyebrow: String = "HARDWARE MODULE") {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(eyebrow, color = CyanAccent.copy(alpha = 0.88f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Text(title, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
    }
}
