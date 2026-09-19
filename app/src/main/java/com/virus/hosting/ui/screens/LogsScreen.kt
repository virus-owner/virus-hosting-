package com.virus.hosting.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.virus.hosting.HostingService
import com.virus.hosting.ui.theme.VirusColors

@Composable
fun LogsScreen() {
    var logs by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        while (true) {
            logs = HostingService.currentLogs()
            kotlinx.coroutines.delay(1500)
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Spacer(Modifier.height(10.dp))
        Text("📜 السجلات", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = VirusColors.TextPrimary)
        Text("${logs.size} سطر", color = VirusColors.TextMuted, fontSize = 12.sp)

        Spacer(Modifier.height(16.dp))

        if (logs.isEmpty()) {
            Text("مفيش سجلات لسه", color = VirusColors.TextMuted)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(VirusColors.BgDark)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(logs.reversed()) { log ->
                    Text(
                        log,
                        color = if (log.contains("[ERR]")) VirusColors.Red
                                else VirusColors.TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
