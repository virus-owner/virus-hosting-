package com.virus.hosting.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.virus.hosting.ui.theme.VirusColors

@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(VirusColors.Cyan, VirusColors.Purple),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(colors))
            .padding(1.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(19.dp))
                .background(VirusColors.Surface)
                .fillMaxWidth()
        ) {
            content()
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = VirusColors.Surface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(icon, fontSize = 26.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                value,
                color = color,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                title,
                color = VirusColors.TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun GradientTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 34.sp,
        fontWeight = FontWeight.ExtraBold,
        style = MaterialTheme.typography.headlineLarge.copy(
            brush = Brush.horizontalGradient(
                listOf(VirusColors.Cyan, VirusColors.Purple, VirusColors.Pink)
            )
        )
    )
}

@Composable
fun EmptyState(icon: String, title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, fontSize = 60.sp)
        Spacer(Modifier.height(16.dp))
        Text(title, color = VirusColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = VirusColors.TextMuted, fontSize = 14.sp)
    }
}

@Composable
fun SectionHeader(title: String, icon: String = "") {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon.isNotEmpty()) {
            Text(icon, fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
        }
        Text(
            title,
            color = VirusColors.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
