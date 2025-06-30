package sync2app.com.syncapplive.additionalSettings.utils


import android.content.Context
import android.net.*
import android.os.Handler
import android.os.Looper

class NetworkMonitor(
    private val context: Context,
    private val onStatusChange: (isConnected: Boolean) -> Unit
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Utility.isInternetAvailable(context) { available ->
                Handler(Looper.getMainLooper()).post {
                    onStatusChange(available)
                }
            }
        }

        override fun onLost(network: Network) {
            Handler(Looper.getMainLooper()).post {
                onStatusChange(false)
            }
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            val validated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            Handler(Looper.getMainLooper()).post {
                onStatusChange(validated)
            }
        }
    }

    private val request = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    fun register() {
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    fun unregister() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {
        }
    }

    fun checkNow() {
        Utility.isInternetAvailable(context) { available ->
            Handler(Looper.getMainLooper()).post {
                onStatusChange(available)
            }
        }
    }
}
