package com.virus.hosting.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.virus.hosting.core.FileOps
import com.virus.hosting.ui.EmptyState
import com.virus.hosting.ui.theme.VirusColors
import java.io.File

@Composable
fun FilesScreen() {
    val ctx = LocalContext.current
    var currentDir by remember { mutableStateOf(File(ctx.filesDir, "webroot").apply { mkdirs() }) }
    var files by remember { mutableStateOf(FileOps.list(currentDir)) }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var showRename by remember { mutableStateOf<File?>(null) }

    fun refresh() { files = FileOps.list(currentDir) }

    val editorContent = remember { mutableStateOf("") }
    if (selectedFile != null) {
        EditorDialog(
            file = selectedFile!!,
            content = editorContent.value,
            onContentChange = { editorContent.value = it },
            onSave = { FileOps.writeText(selectedFile!!, editorContent.value); selectedFile = null },
            onDismiss = { selectedFile = null }
        )
    }

    if (showRename != null) {
        var newName by remember { mutableStateOf(showRename!!.name) }
        AlertDialog(
            onDismissRequest = { showRename = null },
            containerColor = VirusColors.Surface,
            title = { Text("✏️ إعادة تسمية", color = VirusColors.TextPrimary) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("الاسم الجديد") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        FileOps.rename(showRename!!, newName)
                        showRename = null
                        refresh()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VirusColors.Cyan)
                ) { Text("حفظ", color = VirusColors.Bg) }
            },
            dismissButton = { TextButton(onClick = { showRename = null }) { Text("إلغاء") } }
        )
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Spacer(Modifier.height(10.dp))
        Text("📁 مدير الملفات", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = VirusColors.TextPrimary)

        Spacer(Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = VirusColors.Surface2),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = {
                    val parent = currentDir.parentFile
                    if (parent != null && parent.absolutePath.startsWith(ctx.filesDir.absolutePath)) {
                        currentDir = parent
                        refresh()
                    }
                }) { Text("⬅️", fontSize = 18.sp) }
                Text(
                    currentDir.name.ifEmpty { "/" },
                    color = VirusColors.Cyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (files.isEmpty()) {
            EmptyState("📭", "فولدر فاضي", "ارفع ملفات هنا")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(files, key = { it.path }) { item ->
                    FileRow(
                        item = item,
                        onClick = {
                            val f = File(item.path)
                            if (item.isDir) { currentDir = f; refresh() }
                            else { editorContent.value = FileOps.readText(f); selectedFile = f }
                        },
                        onRename = { showRename = File(item.path) },
                        onDelete = { FileOps.delete(File(item.path)); refresh() }
                    )
                }
            }
        }
    }
}

@Composable
private fun FileRow(
    item: FileOps.FileItem,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = VirusColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(if (item.isDir) "📁" else iconFor(item.name), fontSize = 22.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.name, color = VirusColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(
                    if (item.isDir) "مجلد" else FileOps.humanSize(item.size),
                    color = VirusColors.TextMuted, fontSize = 11.sp
                )
            }
            IconButton(onClick = onRename) { Text("✏️", fontSize = 14.sp) }
            IconButton(onClick = onDelete) { Text("🗑️", fontSize = 14.sp) }
        }
    }
}

private fun iconFor(name: String): String = when {
    name.endsWith(".js") -> "🟨"
    name.endsWith(".py") -> "🐍"
    name.endsWith(".php") -> "🐘"
    name.endsWith(".html") -> "🌐"
    name.endsWith(".json") -> "📋"
    name.endsWith(".md") -> "📝"
    name.endsWith(".txt") -> "📄"
    else -> "📄"
}

@Composable
fun EditorDialog(
    file: File,
    content: String,
    onContentChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VirusColors.Surface,
        title = { Text("✏️ ${file.name}", color = VirusColors.TextPrimary, fontSize = 16.sp) },
        text = {
            OutlinedTextField(
                value = content,
                onValueChange = onContentChange,
                modifier = Modifier.fillMaxWidth().height(300.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 12.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = VirusColors.TextPrimary
                )
            )
        },
        confirmButton = {
            Button(onClick = onSave, colors = ButtonDefaults.buttonColors(containerColor = VirusColors.Cyan)) {
                Text("💾 حفظ", color = VirusColors.Bg)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
