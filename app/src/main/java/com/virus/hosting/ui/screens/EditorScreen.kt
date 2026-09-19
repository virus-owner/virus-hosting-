package com.virus.hosting.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.virus.hosting.core.FileOps
import com.virus.hosting.ui.theme.VirusColors
import java.io.File

@Composable
fun EditorScreen() {
    val ctx = LocalContext.current
    var fileName by remember { mutableStateOf("new_file.js") }
    var content by remember { mutableStateOf("// اكتب كودك هنا\n\n") }
    var status by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Spacer(Modifier.height(10.dp))
        Text("✏️ محرر الأكواد", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = VirusColors.TextPrimary)
        Text("اكتب وحفظ أي ملف", color = VirusColors.TextMuted, fontSize = 12.sp)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = fileName,
            onValueChange = { fileName = it },
            label = { Text("اسم الملف (مثلاً: app.js)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            modifier = Modifier.fillMaxWidth().weight(1f),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = VirusColors.TextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    val file = File(File(ctx.filesDir, "webroot"), fileName)
                    if (FileOps.writeText(file, content)) status = "✅ تم الحفظ: $fileName"
                    else status = "❌ فشل الحفظ"
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = VirusColors.Cyan)
            ) { Text("💾 حفظ", color = VirusColors.Bg) }

            OutlinedButton(
                onClick = { content = ""; status = "" },
                modifier = Modifier.weight(1f)
            ) { Text("🗑️ مسح") }
        }

        if (status.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(status, color = VirusColors.Cyan, fontSize = 13.sp)
        }
    }
}
