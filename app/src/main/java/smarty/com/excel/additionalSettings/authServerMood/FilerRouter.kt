package smarty.com.excel.additionalSettings.authServerMood

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

class FilerRouter(
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

                    if (!targetFile.exists() || !targetFile.path.startsWith(baseDir.canonicalPath)) {
                        call.respondText("404 - Not Found", ContentType.Text.Plain, HttpStatusCode.NotFound)
                        return@get
                    }

                    if (targetFile.isDirectory) {
                        val indexFile = File(targetFile, "index.html")
                        val testFile = File(targetFile, "testServer.html")

                        when {
                            indexFile.exists() -> call.respondFile(indexFile)
                            else -> {
                                val files: List<File> = targetFile.listFiles()?.sortedBy { it.name } ?: emptyList()

                                if (files.isEmpty() && testFile.exists()) {
                                    // ✅ show fallback if no files in folder
                                    call.respondFile(testFile)
                                } else {
                                    // ✅ otherwise show directory listing
                                    val html = buildString {
                                        append("<!doctype html><html lang='en'><head>")
                                        append("<meta charset='utf-8' />")
                                        append("<meta name='viewport' content='width=device-width,initial-scale=1' />")
                                        append("<title>Index of /$relPath</title>")
                                        append("<style>")
                                        append(":root { --bg:#0f172a; --card:#1e293b; --muted:#94a3b8; --text:#e2e8f0; --accent:#22c55e; }")
                                        append("body{font-family:system-ui,Arial;background:var(--bg);color:var(--text);padding:20px;}")
                                        append(".wrap{background:var(--card);border-radius:12px;padding:20px;max-width:720px;margin:auto;}")
                                        append("a{color:var(--accent);text-decoration:none;}")
                                        append("ul{list-style:none;padding:0;} li{margin:6px 0;}")
                                        append("</style></head><body><main class='wrap'>")
                                        append("<h2>Index of /$relPath</h2><ul>")

                                        if (relPath.isNotEmpty()) {
                                            val parent = File(relPath).parent ?: ""
                                            append("<li><a href='/${parent}'>../</a></li>")
                                        }

                                        for (f in files) {
                                            val name = f.name + if (f.isDirectory) "/" else ""
                                            append("<li><a href='/${if (relPath.isEmpty()) "" else "$relPath/"}$name'>$name</a></li>")
                                        }

                                        append("</ul></main></body></html>")
                                    }

                                    call.respondText(html, ContentType.Text.Html)
                                }
                            }
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
