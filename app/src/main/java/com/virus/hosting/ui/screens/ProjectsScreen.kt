package com.virus.hosting.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.virus.hosting.data.Project
import com.virus.hosting.ui.EmptyState
import com.virus.hosting.ui.theme.VirusColors

@Composable
fun ProjectsScreen() {
    val ctx = LocalContext.current
    val pm = remember { ProjectManager(ctx) }
    var projects by remember { mutableStateOf(pm.list()) }
    var showAdd by remember { mutableStateOf(false) }

    fun refresh() { projects = pm.list() }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Spacer(Modifier.height(10.dp))
            Text("📦 المشاريع", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = VirusColors.TextPrimary)
            Text("${projects.size} مشروع", color = VirusColors.TextSecondary, fontSize = 13.sp)

            Spacer(Modifier.height(20.dp))

            if (projects.isEmpty()) {
                EmptyState("📦", "مفيش مشاريع", "دوس على ➕ لإنشاء مشروع")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(projects, key = { it.id }) { p ->
                        ProjectCard(p, onDelete = { pm.delete(p.id); refresh() })
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = VirusColors.Cyan,
            contentColor = VirusColors.Bg
        ) { Icon(Icons.Default.Add, "إضافة") }
    }

    if (showAdd) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            containerColor = VirusColors.Surface,
            title = { Text("➕ مشروع جديد", color = VirusColors.TextPrimary) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المشروع") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            pm.create(name)
                            refresh()
                            showAdd = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VirusColors.Cyan)
                ) { Text("إنشاء", color = VirusColors.Bg) }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("إلغاء") } }
        )
    }
}

@Composable
private fun ProjectCard(p: Project, onDelete: () -> Unit) {
    Card(
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
            ) { Text("📁", fontSize = 22.sp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(p.name, color = VirusColors.TextPrimary, fontWeight = FontWeight.Bold)
                Text("${p.language} • منفذ ${p.port}", color = VirusColors.TextMuted, fontSize = 12.sp)
            }
            IconButton(onClick = onDelete) {
                Text("🗑️", fontSize = 18.sp)
            }
        }
    }
}
