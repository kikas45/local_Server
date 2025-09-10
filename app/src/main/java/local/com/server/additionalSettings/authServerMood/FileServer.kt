package local.com.server.additionalSettings.authServerMood

import android.util.Log
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.partialcontent.PartialContent
import io.ktor.server.routing.*
import java.io.File


class FileServer(
    private val baseDir: File,
    private val port: Int = 8080
) {
    private var server: ApplicationEngine? = null

    fun start() {
        if (server != null) return

        server = embeddedServer(CIO, port = port) {
            install(CORS) {
                anyHost()
            }
            install(PartialContent) // allow video/audio streaming

            routing {
                staticFiles("/", baseDir) {
                    default("index.html")
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

