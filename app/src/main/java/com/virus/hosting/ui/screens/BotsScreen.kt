package com.virus.hosting.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.virus.hosting.data.Bot
import com.virus.hosting.data.Storage
import com.virus.hosting.ui.EmptyState
import com.virus.hosting.ui.theme.VirusColors

@Composable
fun BotsScreen() {
    val ctx = LocalContext.current
    var bots by remember { mutableStateOf(Storage.loadBots(ctx)) }
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Bot?>(null) }

    fun refresh() {
        bots = Storage.loadBots(ctx)
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Spacer(Modifier.height(10.dp))
            Text(
                "🤖 البوتات",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = VirusColors.TextPrimary
            )
            Text(
                "${bots.size} بوت • ${bots.count { it.running }} شغال",
                color = VirusColors.TextSecondary,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(20.dp))

            if (bots.isEmpty()) {
                EmptyState(
                    "🤖",
                    "مفيش بوتات لسه",
                    "دوس على زر ➕ لإضافة أول بوت"
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(bots, key = { it.id }) { bot ->
                        BotCard(
                            bot = bot,
                            onToggle = {
                                val updated = bot.copy(enabled = !bot.enabled)
                                val list = Storage.loadBots(ctx)
                                val idx = list.indexOfFirst { it.id == bot.id }
                                if (idx >= 0) { list[idx] = updated; Storage.saveBots(ctx, list) }
                                refresh()
                            },
                            onEdit = { editing = bot },
                            onDelete = {
                                val list = Storage.loadBots(ctx)
                                list.removeAll { it.id == bot.id }
                                Storage.saveBots(ctx, list)
                                refresh()
                            }
                        )
                    }
                }
            }
        }

        // زر الإضافة العائم
        FloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = VirusColors.Cyan,
            contentColor = VirusColors.Bg
        ) {
            Icon(Icons.Default.Add, "إضافة")
        }
    }

    if (showAdd || editing != null) {
        BotDialog(
            bot = editing,
            onDismiss = { showAdd = false; editing = null },
            onSave = { newBot ->
                val list = Storage.loadBots(ctx)
                if (editing == null) {
                    list.add(newBot)
                } else {
                    val idx = list.indexOfFirst { it.id == editing!!.id }
                    if (idx >= 0) list[idx] = newBot
                }
                Storage.saveBots(ctx, list)
                refresh()
                showAdd = false
                editing = null
            }
        )
    }
}

@Composable
private fun BotCard(bot: Bot, onToggle: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VirusColors.Surface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(VirusColors.Purple, VirusColors.Cyan)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🤖", fontSize = 22.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(bot.name, color = VirusColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        if (bot.running) "🟢 يعمل" else "⚪ متوقف",
                        color = if (bot.running) VirusColors.Green else VirusColors.TextMuted,
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = bot.enabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = VirusColors.Cyan,
                        checkedTrackColor = VirusColors.Cyan.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("✏️ تعديل", fontSize = 13.sp) }

                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VirusColors.Red)
                ) { Text("🗑️ حذف", fontSize = 13.sp) }
            }
        }
    }
}

@Composable
private fun BotDialog(bot: Bot?, onDismiss: () -> Unit, onSave: (Bot) -> Unit) {
    var name by remember { mutableStateOf(bot?.name ?: "") }
    var token by remember { mutableStateOf(bot?.token ?: "") }
    var ownerId by remember { mutableStateOf(bot?.ownerId?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VirusColors.Surface,
        title = {
            Text(
                if (bot == null) "➕ إضافة بوت جديد" else "✏️ تعديل بوت",
                color = VirusColors.TextPrimary
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم البوت") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || token.isBlank() || ownerId.toLongOrNull() == null) return@Button
                    val newBot = (bot ?: Bot(name = name, token = token, ownerId = 0L)).copy(
                        name = name, token = token, ownerId = ownerId.toLong()
                    )
                    onSave(newBot)
                },
                colors = ButtonDefaults.buttonColors(containerColor = VirusColors.Cyan)
            ) { Text("حفظ", color = VirusColors.Bg) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
