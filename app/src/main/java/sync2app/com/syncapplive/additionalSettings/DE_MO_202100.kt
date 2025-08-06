package sync2app.com.syncapplive.additionalSettings

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import sync2app.com.syncapplive.AppNetworkModule.RemoteConfig
import sync2app.com.syncapplive.additionalSettings.urlchecks.checkUrlExistence
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.databinding.ActivityDeMo202100Binding
import java.io.File
import java.util.Objects

class DE_MO_202100 : AppCompatActivity() {

    private lateinit var binding: ActivityDeMo202100Binding

    private var downloadId: Long = -199
    private val fileNameOne = "appConfig.json"
    private val fileNameTwo = "InstallAppSettings.json"


    private var file1 = false
    private var file2 = false


    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }



    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeMo202100Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val filter = IntentFilter().apply {
            addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14 and above requires specifying a flag
            registerReceiver(downloadReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(downloadReceiver, filter)
        }


        binding.textDisplay.setOnClickListener {
            deleteAllConfigJsonFiles()
        }


        binding.button.setOnClickListener {
            if (!file1) {
                file1 = true
                initConfigDownload()
            }

        }

    }



    private fun deleteAllConfigJsonFiles() {
        var isCalled = false
        lifecycleScope.launch(Dispatchers.IO) {
            val syn2AppLive = Constants.Syn2AppLive
            val relativePath = "$syn2AppLive/CLO/DE_MO_2021000/${Constants.App_Config_End_Point}"
            val targetFolder = File(getExternalFilesDir(null), relativePath)
            delete(targetFolder)
            withContext(Dispatchers.Main) {
                if (!isCalled) {
                    isCalled = true
                    handler.postDelayed({
                        showToastMessage("Deleted successfully")
                        }, 1000)

                }
            }
        }
    }




    private fun initConfigDownload() {
        lifecycleScope.launch(Dispatchers.IO) {
            val syn2AppLive = Constants.Syn2AppLive
            val relativePath = "$syn2AppLive/CLO/DE_MO_2021000/${Constants.App_Config_End_Point}/$fileNameOne"
            val targetFolder = File(getExternalFilesDir(null), relativePath)
            delete(targetFolder)
            withContext(Dispatchers.Main) {
                val serverUrl = "https://cp.cloudappserver.co.uk/app_base/public/CLO/DE_MO_2021000/App/Config/appConfig.json"
                startDownload("CLO", "DE_MO_2021000", serverUrl, fileNameOne)
                showToastMessage("downloading config")

            }
        }
    }



    private fun loadLocalConfigFromInternalStorage() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val getFolderClo = "CLO"
                val getFolderSubpath = "DE_MO_2021000"

                val baseDir = getExternalFilesDir(null)
                val relativePath = "Syn2AppLive/$getFolderClo/$getFolderSubpath/${Constants.App_Config_End_Point}"
                val folder = File(baseDir, relativePath)

                val fileName = "appConfig.json"
                val file = File(folder, fileName)

                if (file.exists()) {
                    val json = file.bufferedReader().use { it.readText() }
                    val remoteConfigJson = JSONObject(json).getJSONObject("remoteConfig")
                    val gson = Gson()
                    val config = gson.fromJson(remoteConfigJson.toString(), RemoteConfig::class.java)

                    withContext(Dispatchers.Main) {
                        getRemoteValues(config)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showToastMessage("Config file not found.")
                        getRemoteValues(null)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showToastMessage("Failed to read config.")
                    getRemoteValues(null)
                }
            }
        }


    }


    private fun getRemoteValues(config: RemoteConfig?) {
        if (config == null) {
            binding.textDisplay.text = "unable to read  config"
        }else{
            binding.textDisplay.text = "${config?.homeUrl}\n${config?.splashUrl}\n${config?.Screen1Title}\n${config?.Screen3Title}"

        }
    }



    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            try {
                val receivedId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (receivedId == downloadId) {

                    val get_UserID = "CLO"
                    val get_LicenseKey = "DE_MO_2021000"

                    if (file1 && !file2) {
                        file1 = true
                        file2 = true

                        handler.postDelayed(Runnable {

                            lifecycleScope.launch(Dispatchers.IO) {
                                val syn2AppLive = Constants.Syn2AppLive
                                val relativePath = "$syn2AppLive/CLO/DE_MO_2021000/${Constants.App_Config_End_Point}/$fileNameTwo"
                                val targetFolder = File(getExternalFilesDir(null), relativePath)
                                delete(targetFolder)
                                withContext(Dispatchers.Main) {
                                    val ServerUrl =  "https://cp.cloudappserver.co.uk/app_base/public//CLO/DE_MO_2021000/AppConfig/InstallAppSettings.json"
                                    startDownload(get_UserID, get_LicenseKey, ServerUrl, fileNameTwo)
                                    showToastMessage("downloading config")

                                }
                            }


                        }, 500)

                    }

                     if (file1 && file2) {

                        handler.postDelayed(Runnable {
                            showToastMessage("Dowmload completed")
                            loadLocalConfigFromInternalStorage()
                        }, 500)

                    }


                }

            } catch (e: java.lang.Exception) {
                Log.d("POWELL", "onReceive: ${e.message}")
            }
        }
    }



    private fun startDownload(
        getFolderClo: String, getFolderSubpath: String, serverUrl: String, fileName: String
    ) {
        val syn2AppLive = Constants.Syn2AppLive
        val innerFolder = "App/Config"
        val relativePath = "$syn2AppLive/$getFolderClo/$getFolderSubpath/$innerFolder"

        lifecycleScope.launch(Dispatchers.IO) {
            val result = checkUrlExistence(serverUrl)
            withContext(Dispatchers.Main) {
                if (result) {
                    val targetDir = File(getExternalFilesDir(null), relativePath)
                    if (!targetDir.exists()) {
                        targetDir.mkdirs()
                    }

                    val file = File(targetDir, fileName)
                    val request = DownloadManager.Request(Uri.parse(serverUrl)).apply {
                        setTitle(fileName)
                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        setDestinationUri(Uri.fromFile(file)) // ✅ Scoped & app-safe
                    }

                    val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    downloadId = downloadManager.enqueue(request)
                } else {
                    showToastMessage("Invalid User!")
                }
            }
        }
    }

    private fun showToastMessage(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun delete(file: File): Boolean {
        if (file.isFile) {
            return file.delete()
        } else if (file.isDirectory) {
            for (subFile in Objects.requireNonNull(file.listFiles())) {
                if (!delete(subFile)) return false
            }
            return file.delete()
        }
        return false
    }


    override fun onDestroy() {
        super.onDestroy()

        if (downloadReceiver != null) {
                unregisterReceiver(downloadReceiver)
        }
    }

}

