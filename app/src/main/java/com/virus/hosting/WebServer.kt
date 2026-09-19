package com.virus.hosting

import android.content.Context
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream

class WebServer(
    private val context: Context,
    port: Int = 8080
) : NanoHTTPD(port) {

    var webRoot: File = File(context.filesDir, "webroot").apply { mkdirs() }
    var onRequest: ((String) -> Unit)? = null

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        onRequest?.invoke("${session.method} $uri")

        return try {
            when {
                uri == "/" -> serveFile(File(webRoot, "index.html"))
                uri == "/api/status" -> newFixedLengthResponse(
                    Response.Status.OK, "application/json",
                    """{"status":"running","port":$listeningPort}"""
                )
                else -> {
                    val target = File(webRoot, uri.trimStart('/'))
                    if (!target.canonicalPath.startsWith(webRoot.canonicalPath)) {
                        return newFixedLengthResponse(
                            Response.Status.FORBIDDEN, "text/plain", "forbidden"
                        )
                    }
                    serveFile(target)
                }
            }
        } catch (e: Exception) {
            newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR, "text/plain",
                "Error: ${e.message}"
            )
        }
    }

    private fun serveFile(file: File): Response {
        if (!file.exists() || !file.isFile) {
            return newFixedLengthResponse(
                Response.Status.NOT_FOUND, "text/html",
                "<h1>404</h1>"
            )
        }
        return newChunkedResponse(Response.Status.OK, mime(file.name), FileInputStream(file))
    }

    private fun mime(name: String): String = when {
        name.endsWith(".html") -> "text/html; charset=utf-8"
        name.endsWith(".css") -> "text/css; charset=utf-8"
        name.endsWith(".js") -> "application/javascript; charset=utf-8"
        name.endsWith(".json") -> "application/json; charset=utf-8"
        name.endsWith(".png") -> "image/png"
        name.endsWith(".jpg") || name.endsWith(".jpeg") -> "image/jpeg"
        name.endsWith(".svg") -> "image/svg+xml"
        name.endsWith(".txt") -> "text/plain; charset=utf-8"
        else -> "application/octet-stream"
    }
}
