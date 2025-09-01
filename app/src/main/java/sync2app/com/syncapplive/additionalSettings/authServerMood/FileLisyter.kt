package sync2app.com.syncapplive.additionalSettings.authServerMood

import android.util.Log
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

class FileLisyter(
    private val baseDir: File,
    private val port: Int = 8080
) {
    private var server: ApplicationEngine? = null

    fun start() {
        if (server != null) return

        server = embeddedServer(CIO, port = port) {
            install(CORS) { anyHost() }
            install(PartialContent) // allow video/audio streaming

            routing {
                get("{...}") {
                    val relPath = call.parameters.getAll("...")?.joinToString("/") ?: ""
                    val targetFile = File(baseDir, relPath).canonicalFile

                    // Security check: must stay inside baseDir
                    if (!targetFile.exists() || !targetFile.path.startsWith(baseDir.canonicalPath)) {
                        call.respondText("404 - Not Found", ContentType.Text.Plain, HttpStatusCode.NotFound)
                        return@get
                    }

                    if (targetFile.isDirectory) {
                        val indexFile = File(targetFile, "index.html")
                        if (indexFile.exists()) {
                            call.respondFile(indexFile)
                        } else {
                            // generate directory listing
                            val files = targetFile.listFiles()?.sortedBy { it.name } ?: emptyList()
                            val html = buildString {
                                append("<!doctype html><html><head><meta charset='utf-8'>")
                                append("<title>Index of /$relPath</title></head><body>")
                                append("<h2>Index of /$relPath</h2><ul>")

                                if (relPath.isNotEmpty()) {
                                    val parent = File(relPath).parent ?: ""
                                    append("<li><a href='/${parent}'>../</a></li>")
                                }

                                for (f in files) {
                                    val name = f.name + if (f.isDirectory) "/" else ""
                                    append("<li><a href='/${if (relPath.isEmpty()) "" else "$relPath/"}$name'>$name</a></li>")
                                }

                                append("</ul></body></html>")
                            }
                            call.respondText(html, ContentType.Text.Html)
                        }
                    } else {
                        call.respondFile(targetFile)
                    }
                }
            }
        }.start(wait = false)

        Log.i("FileServer", "Ktor server started on port $port, serving ${baseDir.absolutePath}")
    }

    fun stop() {
        server?.stop()
        server = null
        Log.i("FileServer", "Ktor server stopped")
    }
}
