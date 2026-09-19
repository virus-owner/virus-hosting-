package com.virus.hosting.core

import android.content.Context
import android.util.Log
import com.virus.hosting.data.Bot
import com.virus.hosting.data.Storage
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class MultiBotManager(private val ctx: Context) {

    private val client = OkHttpClient.Builder()
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val runners = ConcurrentHashMap<String, BotRunner>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    var onLog: ((String) -> Unit)? = null

    inner class BotRunner(val bot: Bot) {
        var job: Job? = null
        var offset = 0L
        @Volatile var isRunning = false

        fun start() {
            if (isRunning) return
            isRunning = true
            job = scope.launch {
                log("🤖 بدأ البوت: ${bot.name}")
                while (isActive && isRunning) {
                    try {
                        val updates = getUpdates(bot.token, offset)
                        for (u in updates) {
                            handleUpdate(bot, u)
                            offset = u.getLong("update_id") + 1
                        }
                    } catch (e: Exception) {
                        Log.e("Bot", "poll error", e)
                        delay(3000)
                    }
                }
            }
        }

        fun stop() {
            isRunning = false
            job?.cancel()
            job = null
            log("🛑 أوقف البوت: ${bot.name}")
        }
    }

    fun startBot(bot: Bot) {
        runners[bot.id]?.stop()
        val runner = BotRunner(bot)
        runners[bot.id] = runner
        runner.start()
        updateRunning(bot.id, true)
    }

    fun stopBot(botId: String) {
        runners[botId]?.stop()
        runners.remove(botId)
        updateRunning(botId, false)
    }

    fun stopAll() {
        runners.values.forEach { it.stop() }
        runners.clear()
    }

    fun isRunning(botId: String): Boolean = runners[botId]?.isRunning == true

    fun runningCount(): Int = runners.size

    private fun updateRunning(botId: String, running: Boolean) {
        val list = Storage.loadBots(ctx)
        val idx = list.indexOfFirst { it.id == botId }
        if (idx >= 0) {
            list[idx] = list[idx].copy(running = running)
            Storage.saveBots(ctx, list)
        }
    }

    private suspend fun getUpdates(token: String, offset: Long): List<JSONObject> =
        withContext(Dispatchers.IO) {
            val url = "https://api.telegram.org/bot$token/getUpdates?offset=$offset&timeout=25"
            try {
                val resp = client.newCall(Request.Builder().url(url).build()).execute()
                val body = resp.body?.string() ?: return@withContext emptyList()
                val json = JSONObject(body)
                if (!json.getBoolean("ok")) return@withContext emptyList()
                val arr = json.getJSONArray("result")
                (0 until arr.length()).map { arr.getJSONObject(it) }
            } catch (_: Exception) { emptyList() }
        }

    private fun sendMessage(token: String, chatId: Long, text: String) {
        scope.launch {
            try {
                val json = JSONObject().apply {
                    put("chat_id", chatId)
                    put("text", text)
                    put("parse_mode", "Markdown")
                }
                val body = json.toString().toRequestBody("application/json".toMediaType())
                client.newCall(
                    Request.Builder()
                        .url("https://api.telegram.org/bot$token/sendMessage")
                        .post(body).build()
                ).execute().close()
            } catch (_: Exception) {}
        }
    }

    private fun handleUpdate(bot: Bot, update: JSONObject) {
        val msg = update.optJSONObject("message") ?: return
        val chatId = msg.getJSONObject("chat").getLong("id")
        val userId = msg.getJSONObject("from").getLong("id")
        val text = msg.optString("text", "").trim()

        if (userId != bot.ownerId) {
            sendMessage(bot.token, chatId, "🚫 غير مصرح")
            return
        }

        val reply = when {
            text == "/start" -> """
                🦠 *${bot.name}*
                
                *الأوامر:*
                /status — حالة السيرفر
                /projects — قايمة المشاريع
                /logs — آخر السجلات
                /help — المساعدة
            """.trimIndent()

            text == "/status" -> "🟢 السيرفر يعمل"
            text == "/projects" -> "📦 قايمة المشاريع (قريباً)"
            text == "/logs" -> "📜 السجلات (قريباً)"
            text == "/help" -> "📚 اكتب /start للقايمة"
            else -> "❓ أمر غير معروف"
        }
        sendMessage(bot.token, chatId, reply)
    }

    private fun log(msg: String) {
        Log.d("MultiBot", msg)
        onLog?.invoke("[${System.currentTimeMillis()}] $msg")
    }
}
