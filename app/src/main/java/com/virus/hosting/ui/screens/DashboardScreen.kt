package com.virus.hosting.ui.screens

import android.content.Intent
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.virus.hosting.HostingService
import com.virus.hosting.core.MultiBotManager
import com.virus.hosting.data.Storage
import com.virus.hosting.ui.NavRoutes
import com.virus.hosting.ui.StatCard
import com.virus.hosting.ui.theme.VirusColors
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(nav: NavController) {
    val ctx = LocalContext.current
    var running by remember { mutableStateOf(HostingService.isRunning) }
    var botsCount by remember { mutableStateOf(0) }
    var projectsCount by remember { mutableStateOf(0) }
    var runningBots by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            running = HostingService.isRunning
            botsCount = Storage.loadBots(ctx).size
            runningBots = Storage.loadBots(ctx).count { it.running }
            projectsCount = Storage.loadProjects(ctx).size
            delay(2000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        Text(
            "🦠",
            fontSize = 48.sp
        )
        Text(
            "Virus Hosting",
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.headlineLarge.copy(
                brush = Brush.horizontalGradient(
                    listOf(VirusColors.Cyan, VirusColors.Purple, VirusColors.Pink)
                )
            )
        )
        Text(
            "استضافة احترافية من الموبايل",
            color = VirusColors.TextSecondary,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(24.dp))

        // كارت حالة السيرفر
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        if (running)
                            listOf(VirusColors.Green.copy(alpha = 0.4f), VirusColors.Cyan.copy(alpha = 0.2f))
                        else
                            listOf(VirusColors.Red.copy(alpha = 0.4f), VirusColors.Orange.copy(alpha = 0.2f))
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (running) "🟢" else "🔴", fontSize = 24.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (running) "السيرفر يعمل" else "السيرفر متوقف",
                        color = VirusColors.TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "http://localhost:8080",
                    color = VirusColors.TextSecondary,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val intent = Intent(ctx, HostingService::class.java)
                if (running) ctx.stopService(intent)
                else ctx.startService(intent)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (running) VirusColors.Red else VirusColors.Cyan
            )
        ) {
            Text(
                if (running) "⏹ إيقاف السيرفر" else "▶ تشغيل السيرفر",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = VirusColors.Bg
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "📊 الإحصائيات",
            color = VirusColors.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("بوتات كلية", "$botsCount", "🤖", VirusColors.Purple, Modifier.weight(1f))
            StatCard("بوتات شغالة", "$runningBots", "⚡", VirusColors.Green, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("مشاريع", "$projectsCount", "📦", VirusColors.Cyan, Modifier.weight(1f))
            StatCard("منفذ", "8080", "🌐", VirusColors.Orange, Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "⚡ إجراءات سريعة",
            color = VirusColors.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        QuickAction("🤖 إدارة البوتات", "أضف وادر بوتات تيليجرام", VirusColors.Purple) {
            nav.navigate(NavRoutes.BOTS)
        }
        Spacer(Modifier.height(10.dp))
        QuickAction("📁 الملفات", "تصفح وادِر ملفاتك", VirusColors.Cyan) {
            nav.navigate(NavRoutes.FILES)
        }
        Spacer(Modifier.height(10.dp))
        QuickAction("📦 المشاريع", "استيراد وتشغيل المشاريع", VirusColors.Green) {
            nav.navigate(NavRoutes.PROJECTS)
        }

        Spacer(Modifier.height(40.dp))
        Text(
            "© 2025 Virus Hosting • v2.0",
            color = VirusColors.TextMuted,
            fontSize = 11.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun QuickAction(title: String, subtitle: String, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VirusColors.Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(title.split(" ").first(), fontSize = 22.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title.substringAfter(" "), color = VirusColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(subtitle, color = VirusColors.TextMuted, fontSize = 12.sp)
            }
            Text("←", color = color, fontSize = 20.sp)
        }
    }
}
