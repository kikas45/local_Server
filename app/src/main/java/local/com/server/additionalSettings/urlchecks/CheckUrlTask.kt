package local.com.server.additionalSettings.urlchecks

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

suspend fun checkUrlExistence(urlString: String): Boolean {
    return try {
        withContext(Dispatchers.IO) {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            val responseCode = connection.responseCode
            Log.d("CheckUrlTask", "Response Code: $responseCode")
            responseCode == HttpURLConnection.HTTP_OK
        }
    } catch (e: Exception) {
        Log.e("CheckUrlTask", "Error: ${e.message}")
        false
    }
}