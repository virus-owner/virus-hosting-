package com.virus.hosting

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.virus.hosting.core.MultiBotManager
import com.virus.hosting.data.Storage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class HostingService : Service() {

    companion object {
        const val CHANNEL_ID = "virus_hosting"
        const val NOTIF_ID = 1
        @Volatile var isRunning = false
            private set

        @Volatile private var logsInternal: List<String> = emptyList()

        fun currentLogs(): List<String> = logsInternal
    }

    private lateinit var webServer: WebServer
    private var botManager: MultiBotManager? = null
    private val logs = Collections.synchronizedList(mutableListOf<String>())
    private val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.US)

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
                addLog(line)
            }
        }

        try {
            webServer.start(5000, false)
            updateNotif("Running on port 8080")
            addLog("✅ السيرفر اشتغل على المنفذ 8080")
        } catch (e: Exception) {
            updateNotif("Failed: ${e.message}")
            addLog("❌ فشل: ${e.message}")
        }

        // تشغيل كل البوتات المفعّلة
        botManager = MultiBotManager(this).apply {
            onLog = { addLog(it) }
        }
        val bots = Storage.loadBots(this).filter { it.enabled }
        bots.forEach { botManager?.startBot(it) }
        if (bots.isNotEmpty()) addLog("🚀 تم تشغيل ${bots.size} بوت")
    }

    private fun writeDefaultIndex(dir: File) {
        File(dir, "index.html").writeText(
            "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Virus Hosting</title></head>" +
            "<body style=\"font-family:sans-serif;text-align:center;padding:60px;background:#0a0e17;color:#22d3ee\">" +
            "<h1>🦠 Virus Hosting</h1>" +
            "<p style=\"color:#94a3b8\">السيرفر يعمل بنجاح!</p>" +
            "</body></html>"
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                "RELOAD_BOTS" -> {
                    botManager?.stopAll()
                    val bots = Storage.loadBots(this).filter { b -> b.enabled }
                    bots.forEach { b -> botManager?.startBot(b) }
                    addLog("🔄 تم إعادة تحميل البوتات (${bots.size})")
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        try { webServer.stop() } catch (_: Exception) {}
        botManager?.stopAll()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Virus Hosting",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotif(text: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🦠 Virus Hosting")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setOngoing(true)
            .build()

    private fun updateNotif(text: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, buildNotif(text))
    }

    private fun addLog(line: String) {
        synchronized(logs) {
            logs.add("[${timeFmt.format(Date())}] $line")
            if (logs.size > 500) logs.removeAt(0)
            logsInternal = logs.toList()
        }
    }
}
