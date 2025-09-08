package smarty.com.excel.additionalSettings.utils

import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class FileChecker(private val url: String) {

    suspend fun checkFileChange(): String {
        return withContext(Dispatchers.IO) {
            try {
                val response = fetchResponse(url)
                val lastModified = response.header("Last-Modified")
                val etag = response.header("ETag")
                val contentType = response.header("Content-Type")

                // Check for Last-Modified header
                if (!lastModified.isNullOrEmpty()) {
                    return@withContext formatDate(lastModified)
                }

                // Check for ETag
                if (!etag.isNullOrEmpty()) {
                    return@withContext "ETag: $etag"
                }

                // Log file type (useful for debugging)
                if (!contentType.isNullOrEmpty()) {
                    return@withContext "File type: $contentType"
                }

                // Fallback: Compare content (not recommended for large files)
                val content = response.body().string()
                val storedContent = readStoredContent()

                if (content != storedContent) {
                    return@withContext "Content has changed."
                }

                return@withContext "No changes detected."
            } catch (e: Exception) {
                "Failed to check file"
            }
        }
    }

    private fun fetchResponse(url: String): com.squareup.okhttp.Response {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw RuntimeException("Failed to check file")
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
