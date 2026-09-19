package com.virus.hosting

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.*
import com.virus.hosting.ui.*
import com.virus.hosting.ui.theme.VirusColors
import com.virus.hosting.ui.theme.VirusTheme
import com.virus.hosting.ui.screens.*

class MainActivity : ComponentActivity() {

    private val notifPerm = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPerm.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // تشغيل السيرفر في الخلفية
        startService(Intent(this, HostingService::class.java))

        setContent {
            VirusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = VirusColors.Bg
                ) {
                    VirusApp()
                }
            }
        }
    }
}

@Composable
fun VirusApp() {
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val current = navBackStack?.destination

    Scaffold(
        containerColor = VirusColors.Bg,
        bottomBar = {
            NavigationBar(
                containerColor = VirusColors.Surface,
                tonalElevation = 8.dp
            ) {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = current?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(NavRoutes.DASHBOARD) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, item.label) },
                        label = {
                            Text(
                                item.label,
                                fontSize = androidx.compose.ui.unit.TextUnit(
                                    9f,
                                    androidx.compose.ui.unit.TextUnitType.Sp
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VirusColors.Cyan,
                            selectedTextColor = VirusColors.Cyan,
                            indicatorColor = VirusColors.Cyan.copy(alpha = 0.15f),
                            unselectedIconColor = VirusColors.TextMuted,
                            unselectedTextColor = VirusColors.TextMuted
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.DASHBOARD,
            modifier = Modifier.padding(padding)
        ) {
            composable(NavRoutes.DASHBOARD) { DashboardScreen(navController) }
            composable(NavRoutes.BOTS) { BotsScreen() }
            composable(NavRoutes.PROJECTS) { ProjectsScreen() }
            composable(NavRoutes.FILES) { FilesScreen() }
            composable(NavRoutes.EDITOR) { EditorScreen() }
            composable(NavRoutes.LOGS) { LogsScreen() }
            composable(NavRoutes.SETTINGS) { SettingsScreen() }
        }
    }
}
