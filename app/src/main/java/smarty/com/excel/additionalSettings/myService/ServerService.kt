package smarty.com.excel.myService

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.DelicateCoroutinesApi
import smarty.com.excel.R
import smarty.com.excel.additionalSettings.authServerMood.FileServer
import smarty.com.excel.additionalSettings.utils.Constants
import smarty.com.excel.additionalSettings.utils.Utility
import java.io.File
import androidx.core.content.edit
import kotlinx.coroutines.Runnable

@OptIn(DelicateCoroutinesApi::class)
class ServerService : Service() {

    private val sharedPreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SAVE_PORT_VALUES,
            Context.MODE_PRIVATE
        )
    }
    private val sharedBiometric: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_BIOMETRIC, Context.MODE_PRIVATE
        )
    }
   private var port = 8080
    private val handlerParsing: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private var server: FileServer? = null

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            startMyOwnForeground()
        } else {
            startForeground(1, Notification())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        sharedPreferences.edit {
            remove(Constants.SERVER_STATE)
            remove(Constants.localUrl)
            remove(Constants.PR_SEVER_STATE)
        }

        handlerParsing.postDelayed(Runnable {
            Toast.makeText(applicationContext, "Starting server…", Toast.LENGTH_SHORT).show()
            startServer()
        }, 2000)

        return START_STICKY
    }



    private fun startServer() {
        val poto = sharedPreferences.getInt("lastPort", 8080)
        port = if (poto in 1000..9999) poto else 8080

        val localIp = Utility.getLocalIpAddress(this)
        if (localIp.isNullOrBlank()) {
            Toast.makeText(this, "⚠️ No valid local IP found. Connect to a LAN and restart server.", Toast.LENGTH_LONG).show()
            stopSelf() // stop service immediately
            return
        }

        val localUrl = "http://$localIp:$port/"
        val fil_CLO = "CLO"
        val fil_DEMO = "USER"

        val baseDir = File(getExternalFilesDir(null), "${Constants.Syn2AppLive}/$fil_CLO/$fil_DEMO/App")
        if (!baseDir.exists()) baseDir.mkdirs()

        // ✅ Ensure index.html exists...
        val indexFile = File(baseDir, "index.html")
        if (!indexFile.exists()) {
            try {
                val fallbackHtml = assets.open("fallback/index.html")
                    .bufferedReader()
                    .use { it.readText() }
                    .replace("{{PATH}}", baseDir.absolutePath)
                    .replace("{{URL}}", localUrl)

                indexFile.writeText(fallbackHtml)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to create fallback index.html", Toast.LENGTH_SHORT).show()
            }
        }

        if (server != null) {
            Toast.makeText(this, "Server already running", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            server = FileServer(baseDir, port).also { it.start() }

            // ✅ Save network info here
            saveCurrentNetworkInfoService()

            sharedPreferences.edit {
                putString(Constants.SERVER_STATE, "Running")
                putString(Constants.localUrl, localUrl)
                putString(Constants.PR_SEVER_STATE, Constants.SL_Running)
                putString(Constants.PR_STROAGE, "Storage Path: ${baseDir.absolutePath}")
            }

            val intent = Intent(Constants.SERVER_PROGRESS_RECIEVER)
            intent.putExtra(Constants.SERVER_RUNNING_STATE, Constants.SERVER_STARTED)
            sendBroadcast(intent)

        } catch (e: Exception) {
            e.printStackTrace()
            server = null
            Toast.makeText(this, "Failed to start server: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }



    @SuppressLint("SetTextI18n")
    private fun saveCurrentNetworkInfoService() {
        val details = Utility.getLocalNetworkDetails(applicationContext)

        if (details != null) {
            sharedPreferences.edit {
                putString("Connected", details.connectionType)
                putString("Network_Name", details.networkName)
                putString("Local_IP", details.localIp)
                putString("serverNetworkType", details.infoString)
            }
        }
    }



    private fun stopServer() {
        if (server == null) {
            Toast.makeText(this, "⚠️ The server is not running.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            server?.stop()
            server = null

            sharedPreferences.edit {
                putString(Constants.SERVER_STATE, "Stopped")
                putString(Constants.localUrl, "--")
                putString(Constants.PR_SEVER_STATE, Constants.SL_Off)
            }

            sharedPreferences.edit {
                remove("Connected")
                remove("Network_Name")
                remove("Local_IP")
                remove("serverNetworkType")
            }

            Toast.makeText(this, "Server Stopped.", Toast.LENGTH_SHORT).show()
            val intent = Intent(Constants.SERVER_PROGRESS_RECIEVER)
            intent.putExtra(Constants.SERVER_RUNNING_STATE, Constants.SERVER_STOPPED)
            sendBroadcast(intent)

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            server = null

        }
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }




    @SuppressLint("ForegroundServiceType")
    private fun startMyOwnForeground() {
        val newsTitle = "Server Service on Port:$port"

        val builder = NotificationCompat.Builder(applicationContext, "Server Running")
            .setSmallIcon(R.drawable.img_logo_icon)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentText(newsTitle)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("Server Running", "Your local server is active", importance)

            notificationManager.createNotificationChannel(channel)

            startForeground(2, builder.build())
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        Log.d("ServerService", "Server destroyed, stopping server.")
        stopServer()
        sharedPreferences.edit {
            putString(Constants.SERVER_STATE, "Stopped")
            putString(Constants.localUrl, "--")
            putString(Constants.PR_SEVER_STATE, Constants.SL_Off)
        }

    }

    override fun stopService(name: Intent?): Boolean {
        Toast.makeText(applicationContext, "Server stopped", Toast.LENGTH_SHORT).show()
        stopServer()
        sharedPreferences.edit {
            putString(Constants.SERVER_STATE, "Stopped")
            putString(Constants.localUrl, "--")
            putString(Constants.PR_SEVER_STATE, Constants.SL_Off)
        }

        return super.stopService(name)
    }

}
