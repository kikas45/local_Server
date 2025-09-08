package smarty.com.excel.additionalSettings.utils

import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class IndexFileChecker(private val url: String) {

    suspend fun checkIndexFileChange(): String {
        return withContext(Dispatchers.IO) {
            try {
                val response = fetchIndexFileResponse(url)
                val onlineContent = response.body().string()
                val storedContent = readStoredContent() // Read previously stored content from a file or database

                if (onlineContent != storedContent) {
                    // Content has changed
                    // Display last modified time
                    val lastModified = response.header("Last-Modified")
                    if (lastModified != null) {
                        val formattedDate = formatDate(lastModified)
                        formattedDate + ""
                    } else {
                        ""
                    }
                } else {
                    ""
                }
            } catch (e: Exception) {
                "Failed to fetch index file: ${e.message}"
            }
        }
    }

    private fun fetchIndexFileResponse(url: String): com.squareup.okhttp.Response {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw RuntimeException("Failed to fetch index file: ${response.code()}")
        }
        return response
    }

    private fun formatDate(dateString: String): String {
        val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
        val date = dateFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.ENGLISH)
        return outputFormat.format(date)
    }

    private fun readStoredContent(): String {
        // Implement logic to read stored content from a file or database
        // For simplicity, returning an empty string here
        return ""
    }
}
