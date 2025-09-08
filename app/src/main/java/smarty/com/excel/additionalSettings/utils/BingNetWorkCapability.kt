package smarty.com.excel.additionalSettings.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

suspend fun isInternetAvailableOnBing(): Boolean {
    val testUrls = listOf(
        "https://www.google.com/",
        "https://cloudflare.com/"
    )

    return withContext(Dispatchers.IO) {
        for (urlString in testUrls) {
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = true
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.requestMethod = "HEAD"
                val responseCode = connection.responseCode
                Log.d("InternetCheck", "URL: $urlString - Response: $responseCode")
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return@withContext true
                }
            } catch (e: Exception) {
                Log.e("InternetCheck", "Failed to reach $urlString - ${e.message}")
                // Try next URL
            }
        }
        return@withContext false
    }
}
