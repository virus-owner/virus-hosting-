package com.virus.hosting

import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class TelegramBot(
    private val token: String,
    private val ownerId: Long,
    private val handler: (String) -> String
) {
    private val client = OkHttpClient.Builder()
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private var job: Job? = null
    private var offset = 0L
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun start() {
        job = scope.launch {
            while (isActive) {
                try {
                    getUpdates().forEach { u ->
                        handleUpdate(u)
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
        job?.cancel()
        scope.cancel()
    }

    private suspend fun getUpdates(): List<JSONObject> = withContext(Dispatchers.IO) {
        val url = "https://api.telegram.org/bot$token/getUpdates?offset=$offset&timeout=25"
        try {
            val resp = client.newCall(Request.Builder().url(url).build()).execute()
            val body = resp.body?.string() ?: return@withContext emptyList()
            val json = JSONObject(body)
            if (!json.getBoolean("ok")) return@withContext emptyList()
            val arr = json.getJSONArray("result")
            (0 until arr.length()).map { arr.getJSONObject(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun send(chatId: Long, text: String) {
        scope.launch {
            try {
                val json = JSONObject().apply {
                    put("chat_id", chatId)
                    put("text", text)
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

    private fun handleUpdate(update: JSONObject) {
        val msg = update.optJSONObject("message") ?: return
        val chatId = msg.getJSONObject("chat").getLong("id")
        val userId = msg.getJSONObject("from").getLong("id")
        val text = msg.optString("text", "").trim()

        if (userId != ownerId) {
            send(chatId, "Not authorized")
            return
        }

        val reply = when (text) {
            "/start" -> "Virus Hosting Bot\n\nCommands:\n/status\n/stats\n/logs"
            "/status" -> handler("status")
            "/stats" -> handler("stats")
            "/logs" -> handler("logs")
            else -> "Unknown command"
        }
        send(chatId, reply)
    }
}
