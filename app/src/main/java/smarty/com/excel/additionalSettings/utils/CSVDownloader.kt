package smarty.com.excel.additionalSettings.utils

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class CSVDownloader {

    fun downloadCSV(getModifiedUrl: String, clo: String, demo: String, lastEnd: String): String {
        val stringBuilder = StringBuilder()
        try {

           // val downloadUrl = "$getModifiedUrl/$clo/$demo/Start/start1.csv"
            val downloadUrl = "$getModifiedUrl/$clo/$demo/$lastEnd"

            Log.d("downloadUrl", "downloadUrl: $downloadUrl")

            val url = URL(downloadUrl)
            val connection = url.openConnection() as HttpURLConnection
            val code = connection.responseCode

            if (code == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line).append("\n")
                }
            } else {
                Log.e("CSVReadError", "Response code: $code")
            }
        } catch (e: Exception) {
            Log.e("CSVReadError", "Error reading CSV: ${e.message}")
            e.printStackTrace()
        }
        return stringBuilder.toString()
    }
}
