package com.virus.hosting.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.virus.hosting.core.ProjectManager
import com.virus.hosting.data.Storage
import com.virus.hosting.ui.theme.VirusColors

@Composable
fun SettingsScreen() {
    val ctx = LocalContext.current
    var showClear by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        Text("⚙️ الإعدادات", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = VirusColors.TextPrimary)

        Spacer(Modifier.height(20.dp))

        SettingCard("🔋", "إلغاء تحسين البطارية", "خلي التطبيق يشتغل في الخلفية") {
            try {
                val i = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                i.data = Uri.parse("package:${ctx.packageName}")
                ctx.startActivity(i)
            } catch (_: Exception) {
                ctx.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        }

        Spacer(Modifier.height(10.dp))

        SettingCard("📱", "إعدادات التطبيق", "فتح إعدادات Virus Hosting") {
            val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            i.data = Uri.parse("package:${ctx.packageName}")
            ctx.startActivity(i)
        }

        Spacer(Modifier.height(10.dp))

        SettingCard("🌐", "اختبار السيرفر", "افتح localhost:8080 في المتصفح") {
            try {
                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://localhost:8080")))
            } catch (_: Exception) {}
        }

        Spacer(Modifier.height(20.dp))
        Text("📊 معلومات", color = VirusColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(10.dp))

        InfoRow("الإصدار", "2.0.0")
        InfoRow("البوتات", "${Storage.loadBots(ctx).size}")
        InfoRow("المشاريع", "${Storage.loadProjects(ctx).size}")
        InfoRow("المطور", "Virus Hosting")

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { showClear = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = VirusColors.Red)
        ) { Text("🗑️ مسح كل البوتات والمشاريع") }

        Spacer(Modifier.height(40.dp))
        Text(
            "© 2025 Virus Hosting • v2.0",
            color = VirusColors.TextMuted,
            fontSize = 11.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }

    if (showClear) {
        AlertDialog(
            onDismissRequest = { showClear = false },
            containerColor = VirusColors.Surface,
            title = { Text("⚠️ تحذير", color = VirusColors.Red) },
            text = { Text("هيتم مسح كل البوتات والمشاريع، متأكد؟", color = VirusColors.TextPrimary) },
            confirmButton = {
                Button(
                    onClick = {
                        Storage.saveBots(ctx, emptyList())
                        Storage.saveProjects(ctx, emptyList())
                        showClear = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VirusColors.Red)
                ) { Text("مسح") }
            },
            dismissButton = { TextButton(onClick = { showClear = false }) { Text("إلغاء") } }
        )
    }
}

@Composable
private fun SettingCard(icon: String, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VirusColors.Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(VirusColors.Cyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Text(icon, fontSize = 20.sp) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = VirusColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(subtitle, color = VirusColors.TextMuted, fontSize = 12.sp)
            }
            Text("←", color = VirusColors.Cyan, fontSize = 18.sp)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = VirusColors.TextSecondary, fontSize = 13.sp)
        Text(value, color = VirusColors.Cyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
