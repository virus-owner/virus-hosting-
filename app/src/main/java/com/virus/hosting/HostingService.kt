package com.virus.hosting

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.File

class HostingService : Service() {

    companion object {
        const val CHANNEL_ID = "virus_hosting"
        const val NOTIF_ID = 1
        @Volatile var isRunning = false
            private set
    }

    private lateinit var webServer: WebServer
    private var telegramBot: TelegramBot? = null
    private val logs = mutableListOf<String>()

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        createChannel()
        startForeground(NOTIF_ID, buildNotif("Starting..."))

        val webroot = File(filesDir, "webroot").apply {
            if (!exists()) { mkdirs(); writeDefaultIndex(this) }
        }

        webServer = WebServer(this, 8080).apply {
            this.webRoot = webroot
            onRequest = { line ->
                synchronized(logs) {
                    logs.add("[${System.currentTimeMillis()}] $line")
                    if (logs.size > 500) logs.removeAt(0)
                }
            }
        }

        try {
            webServer.start(5000, false)
            updateNotif("Running on port 8080")
        } catch (e: Exception) {
            updateNotif("Failed: ${e.message}")
        }
    }

    private fun writeDefaultIndex(dir: File) {
        File(dir, "index.html").writeText(
            "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Virus Hosting</title></head>" +
            "<body style=\"font-family:sans-serif;text-align:center;padding:60px;background:#0a0e17;color:#22d3ee\">" +
            "<h1>Virus Hosting</h1>" +
            "<p style=\"color:#94a3b8\">Server is running!</p>" +
            "</body></html>"
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            if (it.action == "START_BOT") {
                val token = it.getStringExtra("token")
                val ownerId = it.getLongExtra("ownerId", 0L)
                if (!token.isNullOrEmpty() && ownerId != 0L) {
                    telegramBot?.stop()
                    telegramBot = TelegramBot(token, ownerId) { cmd ->
                        handleBotCommand(cmd)
                    }
                    telegramBot?.start()
                    updateNotif("Server + Bot running")
                }
            }
        }
        return START_STICKY
    }

    private fun handleBotCommand(cmd: String): String {
        return when (cmd) {
            "stats" -> {
                val rt = Runtime.getRuntime()
                val used = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024
                val max = rt.maxMemory() / 1024 / 1024
                "Stats\nRAM: $used / $max MB\nLogs: ${logs.size}"
            }
            "logs" -> synchronized(logs) {
                logs.takeLast(15).joinToString("\n").ifEmpty { "No logs" }
            }
            "status" -> if (isRunning) "Server is running" else "Server stopped"
            else -> "Unknown command"
        }
    }

    override fun onDestroy() {
        isRunning = false
        try { webServer.stop() } catch (_: Exception) {}
        telegramBot?.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Virus Hosting",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotif(text: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Virus Hosting")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setOngoing(true)
            .build()

    private fun updateNotif(text: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, buildNotif(text))
    }
}
