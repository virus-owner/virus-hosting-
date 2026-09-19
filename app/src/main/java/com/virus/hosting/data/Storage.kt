package com.virus.hosting.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object Storage {
    private val gson = Gson()

    private fun botsFile(ctx: Context) = File(ctx.filesDir, "bots.json")
    private fun projectsFile(ctx: Context) = File(ctx.filesDir, "projects.json")

    fun loadBots(ctx: Context): MutableList<Bot> {
        val f = botsFile(ctx)
        if (!f.exists()) return mutableListOf()
        return try {
            val type = object : TypeToken<MutableList<Bot>>() {}.type
            gson.fromJson(f.readText(), type) ?: mutableListOf()
        } catch (_: Exception) { mutableListOf() }
    }

    fun saveBots(ctx: Context, list: List<Bot>) {
        botsFile(ctx).writeText(gson.toJson(list))
    }

    fun loadProjects(ctx: Context): MutableList<Project> {
        val f = projectsFile(ctx)
        if (!f.exists()) return mutableListOf()
        return try {
            val type = object : TypeToken<MutableList<Project>>() {}.type
            gson.fromJson(f.readText(), type) ?: mutableListOf()
        } catch (_: Exception) { mutableListOf() }
    }

    fun saveProjects(ctx: Context, list: List<Project>) {
        projectsFile(ctx).writeText(gson.toJson(list))
    }
}
