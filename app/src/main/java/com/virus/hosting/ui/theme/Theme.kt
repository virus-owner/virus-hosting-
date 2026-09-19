package com.virus.hosting.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object VirusColors {
    val Cyan = Color(0xFF22D3EE)
    val CyanDark = Color(0xFF0891B2)
    val CyanLight = Color(0xFF67E8F9)
    val Purple = Color(0xFFA78BFA)
    val PurpleDark = Color(0xFF7C3AED)
    val Pink = Color(0xFFF472B6)
    val Green = Color(0xFF10B981)
    val Red = Color(0xFFEF4444)
    val Yellow = Color(0xFFFBBF24)
    val Orange = Color(0xFFFB923C)
    val Blue = Color(0xFF3B82F6)

    val Bg = Color(0xFF0A0E17)
    val BgDark = Color(0xFF060911)
    val Surface = Color(0xFF111827)
    val Surface2 = Color(0xFF1A2232)
    val Surface3 = Color(0xFF1F2937)
    val Border = Color(0xFF2D3748)

    val TextPrimary = Color(0xFFF1F5F9)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF64748B)
}

private val VirusDarkScheme = darkColorScheme(
    primary = VirusColors.Cyan,
    onPrimary = VirusColors.Bg,
    primaryContainer = VirusColors.CyanDark,
    onPrimaryContainer = VirusColors.Bg,

    secondary = VirusColors.Purple,
    onSecondary = VirusColors.Bg,
    secondaryContainer = VirusColors.PurpleDark,
    onSecondaryContainer = VirusColors.TextPrimary,

    tertiary = VirusColors.Pink,
    onTertiary = VirusColors.Bg,

    background = VirusColors.Bg,
    onBackground = VirusColors.TextPrimary,

    surface = VirusColors.Surface,
    onSurface = VirusColors.TextPrimary,
    surfaceVariant = VirusColors.Surface2,
    onSurfaceVariant = VirusColors.TextSecondary,

    error = VirusColors.Red,
    onError = VirusColors.TextPrimary,

    outline = VirusColors.Border,
    outlineVariant = VirusColors.Surface3
)

@Composable
fun VirusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VirusDarkScheme,
        content = content
    )
}
