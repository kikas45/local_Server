package sync2app.com.syncapplive.myService

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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.DelicateCoroutinesApi
import sync2app.com.syncapplive.R
import sync2app.com.syncapplive.additionalSettings.authServerMood.FileServer
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.additionalSettings.utils.Utility
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
            Toast.makeText(applicationContext, "Server Service Called", Toast.LENGTH_SHORT).show()
            startServer()
        }, 2000)

        return START_STICKY
    }


    private fun startServer() {
        val poto = sharedPreferences.getInt("lastPort", 8080)
           port = poto
        // Ensure port is 4 digits
        if (port < 1000 || port > 9999) {
            port = 8080
        }

        // Always use hardcoded path from internal storage
        val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
        val fil_CLO = myDownloadClass.getString(Constants.getFolderClo, "").toString()
        val fil_DEMO = myDownloadClass.getString(Constants.getFolderSubpath, "").toString()
        val baseDir =
            File(getExternalFilesDir(null), "${Constants.Syn2AppLive}/$fil_CLO/$fil_DEMO/App")

        if (!baseDir.exists()) baseDir.mkdirs()

        val localUrl = "http://${Utility.getLocalIpAddress(this)}:$port/"


        if (server != null) {
            Toast.makeText(this, "Server already running", Toast.LENGTH_SHORT).show()
            return
        }

        server = FileServer(baseDir, port)
        try {
            server?.start()
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




/*
     @SuppressLint("ForegroundServiceType")
    private fun startMyOwnForeground() {
        val channelId = "server_channel"
        val channelName = "Server on port:$port"

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.img_logo_icon)
            .setContentTitle("Server Running")
            .setContentText("Your local server is active on port :$port")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        startForeground(2, notification)
    }

*/


    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(applicationContext, "Server Destroyed Called", Toast.LENGTH_SHORT).show()
        stopServer()
    }

    override fun stopService(name: Intent?): Boolean {
        Toast.makeText(applicationContext, "Server Stop Called", Toast.LENGTH_SHORT).show()
        stopServer()
        return super.stopService(name)
    }

}
