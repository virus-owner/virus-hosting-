package com.virus.hosting

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val notifPerm = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPerm.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF22D3EE),
                    background = Color(0xFF0A0E17),
                    surface = Color(0xFF111827),
                    onPrimary = Color(0xFF0A0E17)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppScreen()
                }
            }
        }
    }
}

@Composable
fun AppScreen() {
    val context = LocalContext.current
    var running by remember { mutableStateOf(HostingService.isRunning) }
    var botToken by remember { mutableStateOf("") }
    var ownerId by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            running = HostingService.isRunning
            delay(1500)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "🦠 Virus Hosting",
            color = Color(0xFF22D3EE),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 20.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (running)
                    Color(0xFF10B981).copy(alpha = 0.25f)
                else
                    Color(0xFFEF4444).copy(alpha = 0.25f)
            )
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    if (running) "🟢 السيرفر يعمل" else "🔴 السيرفر متوقف",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text("http://localhost:8080", color = Color(0xFF94A3B8))
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                val intent = Intent(context, HostingService::class.java)
                if (running) context.stopService(intent)
                else context.startService(intent)
            },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (running) Color(0xFFEF4444) else Color(0xFF22D3EE)
            )
        ) {
            Text(
                if (running) "⏹ إيقاف السيرفر" else "▶ تشغيل السيرفر",
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = Uri.parse("package:${context.packageName}")
                    context.startActivity(intent)
                } catch (e: Exception) {
                    try {
                        context.startActivity(
                            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        )
                    } catch (_: Exception) {}
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🔋 إلغاء تحسين البطارية")
        }

        Spacer(Modifier.height(30.dp))
        HorizontalDivider(color = Color(0xFF1F2937))
        Spacer(Modifier.height(20.dp))

        Text(
            "🤖 إعدادات بوت تيليجرام",
            fontSize = 18.sp,
            color = Color(0xFF22D3EE)
        )
        Spacer(Modifier.height(15.dp))

        OutlinedTextField(
            value = botToken,
            onValueChange = { botToken = it },
            label = { Text("Bot Token") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = ownerId,
            onValueChange = { ownerId = it },
            label = { Text("Telegram Owner ID") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(15.dp))

        Button(
            onClick = {
                if (botToken.isBlank() || ownerId.toLongOrNull() == null) {
                    message = "❌ املأ البيانات صح"
                    return@Button
                }
                val intent = Intent(context, HostingService::class.java).apply {
                    action = "START_BOT"
                    putExtra("token", botToken)
                    putExtra("ownerId", ownerId.toLong())
                }
                context.startService(intent)
                message = "✅ تم تشغيل البوت"
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6366F1)
            )
        ) { Text("🚀 تشغيل البوت") }

        if (message.isNotEmpty()) {
            Spacer(Modifier.height(15.dp))
            Text(message, color = Color(0xFF22D3EE))
        }

        Spacer(Modifier.height(40.dp))
        Text(
            "© 2025 Virus Hosting",
            color = Color(0xFF64748B),
            fontSize = 12.sp
        )
    }
}
