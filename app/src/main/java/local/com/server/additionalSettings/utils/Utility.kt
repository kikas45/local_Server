package local.com.server.additionalSettings.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.DownloadManager
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.LinkAddress
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import org.jsoup.Jsoup
import local.com.server.myService.ServerService
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

object Utility {

    fun hideKeyBoard(context: Context, editText: EditText) {
        try {
            editText.clearFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editText.windowToken, 0)
        } catch (ignored: Exception) {
        }
    }


    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun isValidEmail(email: String?): Boolean {
        val emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(\\.[a-zA-Z]{2,})?"
        val pattern = Pattern.compile(emailPattern)
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }

    fun showToastMessage(applicationContext: Context, messages: String) {
        Toast.makeText(applicationContext, messages, Toast.LENGTH_SHORT).show()
    }


    // Fetch and clean URLs from HTML
    fun fetchUrlsFromHtml(url: String): List<String> {
        return try {
            val document = Jsoup.connect(url).get()
            val fileTypes = getSupportedFileTypes()
            val urls = mutableSetOf<String>()

            // Extract URLs from relevant HTML tags
            listOf(
                document.select("a[href]").map { it.attr("abs:href") },
                document.select("img[src]").map { it.attr("abs:src") },
                document.select("link[href]").map { it.attr("abs:href") },
                document.select("script[src]").map { it.attr("abs:src") }
            ).flatten().forEach { dirtyUrl ->
                cleanUrl(dirtyUrl)?.let { clean ->
                    if (isValidUrl(clean) && hasSupportedFileType(clean, fileTypes)) {
                        urls.add(clean)
                    }
                }
            }

            urls.toList()
        } catch (e: Exception) {
            Log.e("Utility", "Failed to fetch URLs: ${e.message}")
            emptyList()
        }
    }


    // Clean URL by stripping fragments and queries
    private fun cleanUrl(dirtyUrl: String): String? {
        return try {
            val uri = URI(dirtyUrl)
            URI(
                uri.scheme,
                uri.authority,
                uri.path,
                null,  // Remove query
                null   // Remove fragment
            ).toString()
        } catch (e: URISyntaxException) {
            Log.e("Utility", "Invalid URL: $dirtyUrl")
            null
        }
    }


    // Validate URL format
    private fun isValidUrl(url: String): Boolean {
        return url.isNotBlank() &&
                Patterns.WEB_URL.matcher(url).matches() &&
                url.startsWith("http", ignoreCase = true)
    }


    // Check if URL ends with a supported file type
    private fun hasSupportedFileType(url: String, fileTypes: List<String>): Boolean {
        return fileTypes.any { url.endsWith(it, ignoreCase = true) }
    }


    // Define supported file extensions
    private fun getSupportedFileTypes(): List<String> {
        return listOf(
            // Fonts
            ".ttf", ".otf", ".woff", ".woff2",
            // Images
            ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".ico", ".ico", ".svg", ".webp",
            // Videos
            ".mp4", ".avi", ".mov", ".wmv", ".mkv", ".webm",
            // Audio
            ".mp3", ".wav", ".aac", ".ogg", ".flac", ".m4a", ".wma",
            // Documents
            ".pdf", ".doc", ".docx", ".ppt", ".pptx", ".epub", ".xlsx", ".xls", ".csv", ".txt",
            // Web
            ".html", ".htm", ".asp", ".aspx", ".php", ".css", ".js", ".json", ".webmanifest",
            // Archives
            ".zip", ".rar", ".tar", ".gz",
            // Data
            ".sqlite", ".xml", ".yml", ".yaml", ".scss"
        )
    }


    fun startPulseAnimationForText(view: View) {
        val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f).setDuration(300)
        val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f).setDuration(300)

        val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1.2f, 1f).setDuration(300)
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.2f, 1f).setDuration(300)

        // Play the animations together
        scaleUpX.start()
        scaleUpY.start()

        // Set up an animation listener to play the downward scale after the upward scale
        scaleUpX.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                scaleDownX.start()
                scaleDownY.start()
            }
        })

        // Repeat the animation
        scaleDownX.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                startPulseAnimationForText(view) // Restart the pulse effect
            }
        })
    }


    //// used to manage foreground services
    fun foregroundForSeverServiceClass(context: Context): Boolean {
        val activityManager =
            context.applicationContext.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
        for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
            if (ServerService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }




/*    fun getLocalIpAddress(context: Context): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        for (network: Network in cm.allNetworks) {
            val caps = cm.getNetworkCapabilities(network) ?: continue
            val linkProperties = cm.getLinkProperties(network) ?: continue

            for (linkAddress: LinkAddress in linkProperties.linkAddresses) {
                val host = linkAddress.address
                // Only return valid IPv4 addresses, skip loopback + IPv6
                if (host is Inet4Address && !host.isLoopbackAddress) {
                    return host.hostAddress ?: "0.0.0.0"
                }
            }
        }

        return "0.0.0.0"
    }

    */


    fun getLocalIpAddress(context: Context): String? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        for (network: Network in cm.allNetworks) {
            val caps = cm.getNetworkCapabilities(network) ?: continue
            val linkProperties = cm.getLinkProperties(network) ?: continue

            for (linkAddress: LinkAddress in linkProperties.linkAddresses) {
                val host = linkAddress.address
                if (host is Inet4Address && !host.isLoopbackAddress) {
                    // Only accept private LAN ranges
                    val ip = host.hostAddress ?: continue
                    if (ip.startsWith("192.168.") ||
                        ip.startsWith("10.") ||
                        ip.matches(Regex("^172\\.(1[6-9]|2[0-9]|3[0-1])\\..*"))
                    ) {
                        return ip
                    }
                }
            }
        }

        return null // ⛔ no valid IP
    }



    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("SetTextI18n", "Range")
    fun getDownloadStatus(
        context: Context,
        progressBarPref: ProgressBar,
        textprogressPercentage: TextView,
        get_UserID: String,
        get_LicenseKey: String,
        url: String,
        fileName: String,
        myDownloadClass: SharedPreferences,
        downloadKey: String,
        onDownloadComplete: () -> Unit
    ) {
        try {
            val downloadRef = myDownloadClass.getLong(downloadKey, -15)
            val query = DownloadManager.Query().apply { setFilterById(downloadRef) }
            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val cursor = downloadManager.query(query)

            if (cursor != null && cursor.moveToFirst()) {
                val bytesDownloaded =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        .toLong()
                val bytesTotal =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        .toLong()
                val progressPercentage =
                    (bytesDownloaded.toDouble() / bytesTotal.toDouble() * 100f).toInt()

                progressBarPref.progress = progressPercentage
                textprogressPercentage.text = "$progressPercentage% Zip Downloaded"

                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    onDownloadComplete()
                }
            }
            cursor?.close()
        } catch (ignored: Exception) {
        }
    }


    fun hideSystemBars(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
    }


    fun saveStateHeathChecker(CLO: String, DEMO: String) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.reference.child("local").child(CLO).child(DEMO)

        // Get current date and time as a string
        val currentTime =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        // Use a HashMap<String, Any> for Firebase updateChildren
        val hashMap = HashMap<String, Any>()
        hashMap["LastUpdatedTime"] = currentTime
        hashMap["App"] = "Running"

        myRef.updateChildren(hashMap)
    }

     fun getPublicIP(): String {
        return try {
            val url = URL("https://api.ipify.org")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val ip = reader.readLine()
            reader.close()
            connection.disconnect()
            ip
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }


    // get netowek info
    // get netowek info

    data class NetworkDetails(
        val connectionType: String,
        val networkName: String,
        val localIp: String,
        val infoString: String
    )

    fun getLocalNetworkDetails(context: Context): NetworkDetails? {
        val info = StringBuilder().append("CURRENT NETWORK DETAILS\n")
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        for (network in cm.allNetworks) {
            val caps = cm.getNetworkCapabilities(network) ?: continue
            val linkProperties = cm.getLinkProperties(network) ?: continue

            var connectionType = "Unknown"
            var networkName = "Unknown"

            when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    connectionType = "Wi-Fi"
                    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
                    networkName = wifiManager?.connectionInfo?.ssid?.replace("\"", "") ?: "Unknown SSID"
                }
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    connectionType = "Ethernet"
                    networkName = linkProperties.interfaceName ?: "Ethernet"
                }
            }

            for (linkAddress in linkProperties.linkAddresses) {
                val host = linkAddress.address
                if (host is Inet4Address && !host.isLoopbackAddress) {
                    info.append("Connected To: $connectionType\n")
                    info.append("Network Name: $networkName\n")
                    info.append("Local IP: ${host.hostAddress}\n")

                    return NetworkDetails(
                        connectionType,
                        networkName,
                        host.hostAddress,
                        info.toString()
                    )
                }
            }
        }

        return null // no active local IP
    }

}
