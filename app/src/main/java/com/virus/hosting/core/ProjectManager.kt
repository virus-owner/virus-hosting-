package com.virus.hosting.core

import android.content.Context
import com.virus.hosting.data.Project
import com.virus.hosting.data.Storage
import java.io.File

class ProjectManager(private val ctx: Context) {

    private val projectsRoot: File = File(ctx.filesDir, "projects").apply { mkdirs() }

    fun projectsDir(): File = projectsRoot

    fun projectDir(id: String): File = File(projectsRoot, id).apply { mkdirs() }

    fun list(): List<Project> {
        val stored = Storage.loadProjects(ctx)
        // نتأكد إن المشاريع موجودة على القرص
        return stored.filter { projectDir(it.id).exists() }
    }

    fun create(name: String, language: String = "Unknown"): Project {
        val project = Project(
            name = name,
            language = language,
            port = randomPort()
        )
        projectDir(project.id).mkdirs()
        val list = Storage.loadProjects(ctx)
        list.add(project)
        Storage.saveProjects(ctx, list)
        return project
    }

    fun delete(id: String): Boolean {
        val list = Storage.loadProjects(ctx)
        list.removeAll { it.id == id }
        Storage.saveProjects(ctx, list)
        return projectDir(id).deleteRecursively()
    }

    fun update(project: Project) {
        val list = Storage.loadProjects(ctx)
        val idx = list.indexOfFirst { it.id == project.id }
        if (idx >= 0) {
            list[idx] = project
            Storage.saveProjects(ctx, list)
        }
    }

    fun detectLanguage(id: String): String =
        FileOps.detectLanguage(projectDir(id))

    private fun randomPort(): Int = 4000 + (Math.random() * 1000).toInt()
}
