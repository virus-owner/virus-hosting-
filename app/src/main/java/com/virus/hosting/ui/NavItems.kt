package com.virus.hosting.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val navItems = listOf(
    NavItem(NavRoutes.DASHBOARD, "الرئيسية", Icons.Default.Dashboard),
    NavItem(NavRoutes.BOTS, "البوتات", Icons.Default.SmartToy),
    NavItem(NavRoutes.PROJECTS, "المشاريع", Icons.Default.Folder),
    NavItem(NavRoutes.FILES, "الملفات", Icons.Default.FolderOpen),
    NavItem(NavRoutes.EDITOR, "المحرر", Icons.Default.Code),
    NavItem(NavRoutes.LOGS, "السجلات", Icons.Default.ReceiptLong),
    NavItem(NavRoutes.SETTINGS, "الإعدادات", Icons.Default.Settings)
)
