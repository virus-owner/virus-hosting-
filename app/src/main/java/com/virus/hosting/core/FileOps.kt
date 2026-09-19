package com.virus.hosting.core

import java.io.File
import java.util.zip.ZipInputStream

object FileOps {

    data class FileItem(
        val name: String,
        val path: String,
        val isDir: Boolean,
        val size: Long,
        val modified: Long
    )

    fun list(dir: File): List<FileItem> {
        if (!dir.exists() || !dir.isDirectory) return emptyList()
        return dir.listFiles()?.map {
            FileItem(
                name = it.name,
                path = it.absolutePath,
                isDir = it.isDirectory,
                size = it.length(),
                modified = it.lastModified()
            )
        }?.sortedWith(compareByDescending<FileItem> { it.isDir }.thenBy { it.name.lowercase() })
            ?: emptyList()
    }

    fun delete(file: File): Boolean {
        return if (file.isDirectory) file.deleteRecursively() else file.delete()
    }

    fun rename(file: File, newName: String): Boolean {
        val target = File(file.parentFile, newName)
        return file.renameTo(target)
    }

    fun copy(src: File, dst: File): Boolean {
        return try {
            if (src.isDirectory) {
                src.copyRecursively(dst, overwrite = true)
            } else {
                dst.parentFile?.mkdirs()
                src.copyTo(dst, overwrite = true)
            }
            true
        } catch (_: Exception) { false }
    }

    fun move(src: File, dst: File): Boolean {
        return try {
            if (src.renameTo(dst)) true
            else copy(src, dst) && src.deleteRecursively()
        } catch (_: Exception) { false }
    }

    fun createFolder(parent: File, name: String): Boolean {
        return File(parent, name).mkdirs()
    }

    fun createFile(parent: File, name: String): Boolean {
        return try {
            File(parent, name).createNewFile()
        } catch (_: Exception) { false }
    }

    fun readText(file: File): String {
        return try { file.readText() } catch (e: Exception) { "Error: ${e.message}" }
    }

    fun writeText(file: File, content: String): Boolean {
        return try {
            file.parentFile?.mkdirs()
            file.writeText(content)
            true
        } catch (_: Exception) { false }
    }

    fun extractZip(zipFile: File, targetDir: File): Boolean {
        return try {
            targetDir.mkdirs()
            ZipInputStream(zipFile.inputStream()).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val outFile = File(targetDir, entry.name)
                    if (!outFile.canonicalPath.startsWith(targetDir.canonicalPath)) {
                        entry = zis.nextEntry
                        continue
                    }
                    if (entry.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile?.mkdirs()
                        outFile.outputStream().use { fos -> zis.copyTo(fos) }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
            true
        } catch (_: Exception) { false }
    }

    fun humanSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
            bytes < 1024L * 1024 * 1024 -> "%.1f MB".format(bytes / 1024.0 / 1024)
            else -> "%.1f GB".format(bytes / 1024.0 / 1024 / 1024)
        }
    }

    fun detectLanguage(dir: File): String {
        return when {
            File(dir, "package.json").exists() -> "Node.js"
            File(dir, "requirements.txt").exists() -> "Python"
            File(dir, "main.py").exists() -> "Python"
            File(dir, "app.py").exists() -> "Python"
            File(dir, "index.php").exists() -> "PHP"
            File(dir, "composer.json").exists() -> "PHP"
            File(dir, "index.js").exists() -> "Node.js"
            File(dir, "index.html").exists() -> "HTML"
            File(dir, "pom.xml").exists() -> "Java"
            File(dir, "build.gradle").exists() -> "Java"
            File(dir, "go.mod").exists() -> "Go"
            File(dir, "Cargo.toml").exists() -> "Rust"
            else -> "Unknown"
        }
    }
}
