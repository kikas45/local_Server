package sync2app.com.syncapplive.additionalSettings

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sync2app.com.syncapplive.WebViewPage
import sync2app.com.syncapplive.R
import sync2app.com.syncapplive.additionalSettings.autostartAppOncrash.Methods
import sync2app.com.syncapplive.additionalSettings.myApiDownload.FilesApi
import sync2app.com.syncapplive.additionalSettings.myApiDownload.FilesViewModel
import sync2app.com.syncapplive.additionalSettings.myCompleteDownload.DnApi
import sync2app.com.syncapplive.additionalSettings.myCompleteDownload.DnViewModel
import sync2app.com.syncapplive.additionalSettings.myCompleteDownload.SavedDownloadsAdapter
import sync2app.com.syncapplive.additionalSettings.myFailedDownloadfiles.DnFailedApi
import sync2app.com.syncapplive.additionalSettings.myFailedDownloadfiles.DnFailedViewModel
import sync2app.com.syncapplive.additionalSettings.urlchecks.checkUrlExistence
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.additionalSettings.utils.Utility
import sync2app.com.syncapplive.databinding.ActivityRetryApiDownloadBinding
import sync2app.com.syncapplive.databinding.CustomFailedDownloadsLayoutBinding
import sync2app.com.syncapplive.databinding.ProgressDialogLayoutBinding
import java.io.File
import java.util.Objects

class RetryApiDownloadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRetryApiDownloadBinding

    private var powerManager: PowerManager? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private lateinit var customProgressDialog: Dialog

    private var countdownTimer: CountDownTimer? = null

    private var currentDownloadIndex = 0
    private var currentFailedDwonload = 0
    private var downloadedFilesCount = 0
    private var sn = 0
    private val mfilesViewModel by viewModels<FilesViewModel>()

    private val dnViewModel by viewModels<DnViewModel>()
    private val dnFailedViewModel by viewModels<DnFailedViewModel>()

    private var isFailedDownload = false

    var manager: DownloadManager? = null

    private var totalFiles: Int = 0


    private val adapter by lazy {
        SavedDownloadsAdapter()
    }

    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }


    private val sharedP: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.MY_DOWNLOADER_CLASS,
            Context.MODE_PRIVATE
        )
    }

    private val sharedBiometric: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_BIOMETRIC,
            Context.MODE_PRIVATE
        )
    }


    private val myHandler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private  var isSystemActive = true


    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    private val sharedTVAPPModePreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_TV_APP_MODE, Context.MODE_PRIVATE
        )
    }

    @SuppressLint("WakelockTimeout", "SourceLockedOrientationActivity",
        "UnspecifiedRegisterReceiverFlag"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRetryApiDownloadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyOritenation()


        var isEmpty = true
        mfilesViewModel.readAllData.observe(this@RetryApiDownloadActivity,
            Observer { files ->
                if (files.isEmpty()){
                    if (isEmpty){
                        isEmpty = false
                        myHandler.postDelayed(Runnable {
                            copyFilesToFailedDownloads()
                        }, 2000)

                    } }
            })




        setUpFullScreenWindows()




        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            registerReceiver(downloadCompleteReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(downloadCompleteReceiver, filter)
        }



        countdownTimer?.cancel()


        manager = getApplicationContext().getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock =
            powerManager!!.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YourApp::MyWakelockTag")
        wakeLock!!.acquire()

        binding.closeBs.setOnClickListener {
            closeDownloadpage()
        }

        binding.textCancelBtn.setOnClickListener {
            closeDownloadpage()
        }


      binding.textRetryBtn.setOnClickListener {
            closeDownloadpage()
        }

        binding.textLaunchApplication.setOnClickListener {
            stratMyACtivity()
        }

        //add exception
        Methods.addExceptionHandler(this)

        startMyCSVApiDownload()

 /*       binding.textRetryBtn.setOnClickListener {

            if (isFailedDownload == true) {
                try {
                    if (downloadCompleteReceiver != null) {
                        unregisterReceiver(downloadCompleteReceiver)
                    }
                } catch (e: Exception) {
                }

                showCustomProgressDialog("Please wait!")
                mfilesViewModel.deleteAllFiles()
                copyFilesToFailedDownloads()
                myHandler.postDelayed(Runnable {
                    reTryTheDownlaods()
                    customProgressDialog.cancel()
                }, 4000)

            } else {
                showToastMessage("Download in Progress..")
            }


        }
*/


        binding.apply {
            handler.postDelayed(Runnable {
                recyclerApiDownloads.adapter = adapter
                recyclerApiDownloads.layoutManager = LinearLayoutManager(applicationContext)
                dnViewModel.readAllData.observe(this@RetryApiDownloadActivity, Observer { filesApi ->
                    adapter.setDataApi(filesApi)

                })
            }, 300)
        }

    }


    private fun setUpFullScreenWindows() {
        val get_INSTALL_TV_JSON_USER_CLICKED = sharedTVAPPModePreferences.getString(Constants.INSTALL_TV_JSON_USER_CLICKED, "").toString()
        if (get_INSTALL_TV_JSON_USER_CLICKED != Constants.INSTALL_TV_JSON_USER_CLICKED) {
            val img_imgImmesriveModeToggle = preferences.getBoolean(Constants.immersive_mode, false)
            if (img_imgImmesriveModeToggle){
                Utility.hideSystemBars(window)
            }else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }


        }else{

            val immersive_Mode_APP = sharedTVAPPModePreferences.getBoolean(Constants.immersive_Mode_APP, false)
            if (immersive_Mode_APP) {
                Utility.hideSystemBars(window)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }

        }
    }


    private fun reTryTheDownlaods() {

        try {
        myHandler.postDelayed(Runnable {
            if (isSystemActive) {
                dnViewModel.deleteAllFiles()
                val intent = Intent(applicationContext, DownloadApisFilesActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 500)

            customProgressDialog.cancel()
        } catch (e: Exception) {
        }

    }


    private fun startMyCSVApiDownload() {
        binding.apply {
            val connectivityManager22: ConnectivityManager =
                applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo22: NetworkInfo? = connectivityManager22.activeNetworkInfo

            if (networkInfo22 != null && networkInfo22.isConnected) {
                val imagUsemanualOrnotuseManual =
                    sharedBiometric.getString(Constants.imagSwtichEnableManualOrNot, "")

                if (imagUsemanualOrnotuseManual.equals(Constants.imagSwtichEnableManualOrNot)) {
                    handler.postDelayed(Runnable {

                        val getSavedEditTextInputSynUrlZip =
                            sharedP.getString(Constants.getSavedEditTextInputSynUrlZip, "")
                                .toString()

                        if (getSavedEditTextInputSynUrlZip.contains("/Start/start1.csv")) {
                            myHandler.postDelayed(runnableManual, 500)


                        } else if (getSavedEditTextInputSynUrlZip.contains("/Api/update1.csv")) {
                            myHandler.postDelayed(runnableManual, 500)

                        } else {
                            // showToastMessage("Something went wrong, System Could not locate CSV from this Location")
                            binding.textCsvStatus.text = Constants.Error_CSv_Message

                        }

                    }, 1000)

                } else {

                    handler.postDelayed(Runnable {
                        myHandler.postDelayed(runnableGetApiStart, 500)

                    }, 1000)

                }


            } else {
                Toast.makeText(applicationContext, "No Internet Connection", Toast.LENGTH_SHORT)
                    .show()
                binding.textCsvStatus.text = "Poor Internet, Retry download";
                isFailedDownload = true

                handler.postDelayed(Runnable {
                    recreate()
                }, 3000)

            }

        }
    }



    private fun closeDownloadpage() {


        lifecycleScope.launch(Dispatchers.IO) {

            dnViewModel.deleteAllFiles()
            mfilesViewModel.deleteAllFiles()
            dnFailedViewModel.deleteAllFiles()

            val sharedP = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
            val getFolderClo = sharedP.getString("${Constants.getFolderClo}", "") ?: ""
            val getFolderSubpath = sharedP.getString("${Constants.getFolderSubpath}", "") ?: ""
            val zip = sharedP.getString("${Constants.Zip}", "") ?: ""
            val fileName = sharedP.getString("${Constants.fileName}", "") ?: ""


            Log.d("second_cancel_download", ":  $getFolderClo ::  $getFolderSubpath ::  $zip  :: $fileName  ")

            // Use app-private storage now
            val baseDir = getExternalFilesDir(null)
            val finalPath = File(baseDir, "Syn2AppLive/$getFolderClo/$getFolderSubpath/$zip/$fileName")

            if (finalPath.exists()) {
                finalPath.delete()
            }

            withContext(Dispatchers.Main) {

                if (downloadCompleteReceiver != null) {
                    unregisterReceiver(downloadCompleteReceiver)
                }

                myHandler.postDelayed(Runnable {
                    val intent = Intent(applicationContext, ReSyncActivity::class.java)
                    startActivity(intent)
                    finishAffinity()

                }, 500)
            }
        }


        try {

            if ( customProgressDialog != null){
                customProgressDialog.cancel()
            }
        } catch (e: Exception) {
        }
    }


    private val runnableGetApiStart: Runnable = object : Runnable {
        @SuppressLint("SetTextI18n")
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun run() {
            dnViewModel.deleteAllFiles()
            mfilesViewModel.readAllData.observe(this@RetryApiDownloadActivity, Observer { files ->
                if (files.isNotEmpty()) {

                    handler.postDelayed(Runnable {
                        binding.textRemainging.text = "0 / ${files.size}   Files Downloaded"
                        totalFiles = files.size.toInt()
                        binding.textCsvStatus.visibility = View.GONE
                        binding.textPercentageCompleted.visibility = View.VISIBLE
                        downloadSequentially(files)

                    }, 500)

                } else {
                    // showToastMessage("No files found")
                }
            })
        }

    }


    private val runnableManual: Runnable = object : Runnable {
        @SuppressLint("SetTextI18n")
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun run() {
            dnViewModel.deleteAllFiles()
            mfilesViewModel.readAllData.observe(this@RetryApiDownloadActivity, Observer { files ->
                if (files.isNotEmpty()) {

                    handler.postDelayed(Runnable {
                        binding.textRemainging.text = "0 / ${files.size}   Files Downloaded"
                        totalFiles = files.size.toInt()
                        binding.textCsvStatus.visibility = View.GONE
                        binding.textPercentageCompleted.visibility = View.VISIBLE
                        downloadSequentiallyManually(files)

                    }, 500)


                } else {
                    // showToastMessage("No files found")
                }
            })
        }

    }


    @SuppressLint("SetTextI18n")
    private fun downloadSequentially(files: List<FilesApi>) {

        if (currentDownloadIndex < files.size) {
            val file = files[currentDownloadIndex]
            handler.postDelayed(Runnable {
                getZipDownloads(file.SN, file.FolderName, file.FileName)
            }, 1000)

        }
    }

    @SuppressLint("SetTextI18n")
    private fun downloadSequentiallyManually(files: List<FilesApi>) {

        if (currentDownloadIndex < files.size) {
            val file = files[currentDownloadIndex]
            handler.postDelayed(Runnable {
                getZipDownloadsManually(file.SN, file.FolderName, file.FileName)

            }, 500)

        } else {
            //  showToastMessage("All files Downloaded")
        }
    }


    private val downloadCompleteReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
                handler.postDelayed(Runnable {

                    val imagUsemanualOrnotuseManual =
                        sharedBiometric.getString(Constants.imagSwtichEnableManualOrNot, "")

                    if (imagUsemanualOrnotuseManual.equals(Constants.imagSwtichEnableManualOrNot)) {
                        pre_Laucnh_Files_For_Manual()

                    } else {
                        pre_Laucnh_Files()
                    }

                }, 500)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun pre_Laucnh_Files() {
        currentDownloadIndex++
        downloadSequentially(mfilesViewModel.readAllData.value ?: emptyList())

        downloadedFilesCount++
        copyFilesToNewFolder(mfilesViewModel.readAllData.value ?: emptyList())


        val get_fileNumber = sharedP.getString(Constants.fileNumber, "").toString()
        val get_folderName = sharedP.getString(Constants.folderName, "").toString()
        val get_fileName = sharedP.getString(Constants.fileName, "").toString()

        binding.textRemainging.text = "$currentDownloadIndex / $totalFiles Files Downloaded"

        // Save the particular download after successful
        val dnApi = DnApi(
            SN = get_fileNumber,
            FolderName = get_folderName,
            FileName = get_fileName,
            Status = "true"
        )
        dnViewModel.addFiles(dnApi)

        // remove a successful the failed downloads
        val dnApiFailed = DnFailedApi(
            SN = get_fileNumber,
            FolderName = get_folderName,
            FileName = get_fileName,
            Status = "true"
        )
        dnFailedViewModel.deleteFiles(dnApiFailed)

    }

    @SuppressLint("SetTextI18n")
    private fun pre_Laucnh_Files_For_Manual() {
        currentDownloadIndex++
        downloadSequentiallyManually(mfilesViewModel.readAllData.value ?: emptyList())

        downloadedFilesCount++
        copyFilesToNewFolder(mfilesViewModel.readAllData.value ?: emptyList())


        val get_fileNumber = sharedP.getString(Constants.fileNumber, "").toString()
        val get_folderName = sharedP.getString(Constants.folderName, "").toString()
        val get_fileName = sharedP.getString(Constants.fileName, "").toString()

        binding.textRemainging.text = "$downloadedFilesCount / $totalFiles Files Downloaded"

        // Save the particular download after successful
        val dnApi = DnApi(
            SN = get_fileNumber,
            FolderName = get_folderName,
            FileName = get_fileName,
            Status = "true"
        )
        dnViewModel.addFiles(dnApi)

        // remove a successful the failed downloads
        val dnApiFailed = DnFailedApi(
            SN = get_fileNumber,
            FolderName = get_folderName,
            FileName = get_fileName,
            Status = "true"
        )
        dnFailedViewModel.deleteFiles(dnApiFailed)


    }


    @SuppressLint("SetTextI18n")
    private fun getZipDownloads(sn: String, folderName: String, fileName: String) {

        val getFolderClo = sharedP.getString(Constants.getFolderClo, "").toString()
        val getFolderSubpath = sharedP.getString(Constants.getFolderSubpath, "").toString()
        val get_ModifiedUrl = sharedP.getString(Constants.get_ModifiedUrl, "").toString()

        val Syn2AppLive = Constants.Syn2AppLive
        val relativePath = "$Syn2AppLive/$getFolderClo/$getFolderSubpath/$folderName"

        val baseDir = getExternalFilesDir(null) // ✅ App-private scoped external dir
        val targetDir = File(baseDir, relativePath)
        val targetFile = File(targetDir, fileName)

        // Delete existing file if it exists
        if (targetFile.exists()) {
            targetFile.delete()
        }

        val getFileUrl = "$get_ModifiedUrl/$getFolderClo/$getFolderSubpath/$folderName/$fileName"

        lifecycleScope.launch {
            try {
                val result = checkUrlExistence(getFileUrl)
                if (result) {
                    handler.postDelayed(Runnable {
                        binding.textRemainging.visibility = View.VISIBLE
                        binding.textPercentageCompleted.visibility = View.VISIBLE

                        val fileNum = currentDownloadIndex.toDouble()
                        val totalPercentage = ((fileNum / totalFiles.toDouble()) * 100).toInt()
                        binding.textPercentageCompleted.text = "$totalPercentage% Complete"

                        val editior = sharedP.edit()
                        editior.putString(Constants.fileNumber, sn)
                        editior.putString(Constants.folderName, folderName)
                        editior.putString(Constants.fileName, fileName)
                        editior.apply()

                        if (!targetDir.exists()) {
                            targetDir.mkdirs()
                        }

                        val managerDownload = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                        val request = DownloadManager.Request(Uri.parse(getFileUrl))
                        request.setTitle(fileName)
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        request.setDestinationUri(Uri.fromFile(targetFile))  // ✅ Scoped-safe URI

                        val downloadReferenceMain = managerDownload.enqueue(request)

                        val editor = sharedP.edit()
                        editor.putLong(Constants.downloadKey, downloadReferenceMain)
                        editor.apply()

                    }, 300)
                } else {
                    Log.d("GRAB_FAILED_URL", "$folderName/$fileName")

                    binding.textRemainging.visibility = View.VISIBLE
                    binding.textPercentageCompleted.visibility = View.VISIBLE

                    val editior = sharedP.edit()
                    editior.putString(Constants.fileNumber, sn)
                    editior.putString(Constants.folderName, folderName)
                    editior.putString(Constants.fileName, fileName)
                    editior.apply()

                    checkWhatAreaToDownloadFrom(
                        currentDownloadIndex.toString(),
                        folderName,
                        fileName
                    )
                }
            } catch (e: Exception) {
                Log.e("DownloadError", "Exception: ${e.message}")
            }
        }
    }


    private fun checkWhatAreaToDownloadFrom(sn: String, folderName: String, fileName: String) {


        val imagUsemanualOrnotuseManual =
            sharedBiometric.getString(Constants.imagSwtichEnableManualOrNot, "")

        if (imagUsemanualOrnotuseManual.equals(Constants.imagSwtichEnableManualOrNot)) {
            currentDownloadIndex++
            downloadSequentiallyManually(mfilesViewModel.readAllData.value ?: emptyList())

            downloadedFilesCount++
            copyFilesToNewFolder(mfilesViewModel.readAllData.value ?: emptyList())


            //   binding.textFileCounts.text = "$sn / "

            val dnApi = DnApi(
                SN = sn,
                FolderName = folderName,
                FileName = fileName,
                Status = "false"
            )
            dnViewModel.addFiles(dnApi)

            isFailedDownload = true
            currentFailedDwonload++


        } else {
            currentDownloadIndex++
            downloadSequentially(mfilesViewModel.readAllData.value ?: emptyList())


            downloadedFilesCount++
            copyFilesToNewFolder(mfilesViewModel.readAllData.value ?: emptyList())


            //   binding.textFileCounts.text = "$sn / "

            val dnApi = DnApi(
                SN = sn,
                FolderName = folderName,
                FileName = fileName,
                Status = "false"
            )
            dnViewModel.addFiles(dnApi)


            isFailedDownload = true
            currentFailedDwonload++
        }


    }


    private fun getZipDownloadsManually(sn: String, folderName: String, fileName: String) {
        val Syn2AppLive = Constants.Syn2AppLive
        val relativePath = "$Syn2AppLive/CLO/MANUAL/DEMO/$folderName"

        val baseDir = getExternalFilesDir(null)  // ✅ App-private scoped storage
        val targetDir = File(baseDir, relativePath)
        val targetFile = File(targetDir, fileName)

        // Clean up old file if it exists
        if (targetFile.exists()) {
            targetFile.delete()
        }

        val getSavedEditTextInputSynUrlZip =
            sharedP.getString(Constants.getSavedEditTextInputSynUrlZip, "").toString()

        var replacedUrl = getSavedEditTextInputSynUrlZip

        replacedUrl = when {
            replacedUrl.contains("/Start/start1.csv") -> {
                replacedUrl.replace("/Start/start1.csv", "/$folderName/$fileName")
            }
            replacedUrl.contains("/Api/update1.csv") -> {
                replacedUrl.replace("/Api/update1.csv", "/$folderName/$fileName")
            }
            else -> {
                Log.d("getZipDownloadsManually", "Unable to replace this url")
                replacedUrl
            }
        }

        lifecycleScope.launch {
            try {
                val result = checkUrlExistence(replacedUrl)
                if (result) {
                    handler.postDelayed(Runnable {
                        binding.textRemainging.visibility = View.VISIBLE
                        binding.textPercentageCompleted.visibility = View.VISIBLE

                        val fileNum = currentDownloadIndex.toDouble()
                        val totalPercentage = ((fileNum / totalFiles.toDouble()) * 100).toInt()
                        binding.textPercentageCompleted.text = "$totalPercentage% Complete"

                        val editior = sharedP.edit()
                        editior.putString(Constants.fileNumber, sn)
                        editior.putString(Constants.folderName, folderName)
                        editior.putString(Constants.fileName, fileName)
                        editior.apply()

                        if (!targetDir.exists()) {
                            targetDir.mkdirs()
                        }

                        val managerDownload = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                        val request = DownloadManager.Request(Uri.parse(replacedUrl))
                        request.setTitle(fileName)
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        request.setDestinationUri(Uri.fromFile(targetFile))  // ✅ Scoped-safe path

                        val downloadReferenceMain = managerDownload.enqueue(request)

                        val editor = sharedP.edit()
                        editor.putLong(Constants.downloadKey, downloadReferenceMain)
                        editor.apply()

                    }, 300)
                } else {
                    binding.textRemainging.visibility = View.VISIBLE
                    binding.textPercentageCompleted.visibility = View.VISIBLE

                    val editior = sharedP.edit()
                    editior.putString(Constants.fileNumber, sn)
                    editior.putString(Constants.folderName, folderName)
                    editior.putString(Constants.fileName, fileName)
                    editior.apply()

                    checkWhatAreaToDownloadFrom(
                        currentDownloadIndex.toString(),
                        folderName,
                        fileName
                    )
                }
            } catch (e: Exception) {
                Log.e("DownloadError", "Exception: ${e.message}")
            }
        }
    }

    private val runnableGetDownloadProgress: Runnable = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun run() {
            getDownloadStatus()
            myHandler.postDelayed(this, 500)
        }
    }


    fun getDownloadStatus() {
        try {

            val download_ref = sharedP.getLong(Constants.downloadKey, -15)
            val query = DownloadManager.Query()
            query.setFilterById(download_ref)
            val c =
                (applicationContext.getSystemService(DOWNLOAD_SERVICE) as DownloadManager).query(
                    query
                )
            if (c!!.moveToFirst()) {
                @SuppressLint("Range") val bytes_downloaded =
                    c.getInt(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        .toLong()
                @SuppressLint("Range") val bytes_total =
                    c.getInt(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)).toLong()
                val dl_progress =
                    (bytes_downloaded.toDouble() / bytes_total.toDouble() * 100f).toInt()

                binding.progressBarPref.setProgress(dl_progress)
                val get_fileName = sharedP.getString(Constants.fileName, "").toString()


                if (c == null) {
                    statusMessage(c, dl_progress, get_fileName)
                } else {
                    c.moveToFirst()
                    statusMessage(c, dl_progress, get_fileName)
                }


            }
        } catch (ignored: java.lang.Exception) {
        }
    }


    @SuppressLint("Range", "SetTextI18n")
    private fun statusMessage(c: Cursor, dl_progress: Int, get_fileName: String) {
        var msg: String
        when (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
            DownloadManager.STATUS_PENDING -> {
             //   msg = "Pending.."
              //  binding.textDownloadSieze.text = msg
             //   binding.textDownloadSieze.setTextColor(resources.getColor(R.color.black))
            }

            DownloadManager.STATUS_RUNNING -> {
                binding.textDownloadSieze.text = "$get_fileName : $dl_progress%"
                binding.textDownloadSieze.setTextColor(resources.getColor(R.color.black))
            }

            DownloadManager.STATUS_PAUSED -> {
                msg = "Download Paused, check internet"
                binding.textDownloadSieze.text = msg
                binding.textDownloadSieze.setTextColor(resources.getColor(R.color.black))

                val get_fileNumber = sharedP.getString(Constants.fileNumber, "").toString()
                val get_folderName = sharedP.getString(Constants.folderName, "").toString()
                val get_fileName = sharedP.getString(Constants.fileName, "").toString()

                checkWhatAreaToDownloadFrom(
                    currentDownloadIndex.toString(),
                    get_folderName,
                    get_fileName
                )

            }

            DownloadManager.STATUS_FAILED -> {
                try {
                    msg = "Download Failed!, Retry.."
                    binding.textDownloadSieze.text = msg
                    binding.textDownloadSieze.setTextColor(resources.getColor(R.color.red))

                    val get_fileNumber = sharedP.getString(Constants.fileNumber, "").toString()
                    val get_folderName = sharedP.getString(Constants.folderName, "").toString()
                    val get_fileName = sharedP.getString(Constants.fileName, "").toString()

                    checkWhatAreaToDownloadFrom(
                        currentDownloadIndex.toString(),
                        get_folderName,
                        get_fileName
                    )


                } catch (e: Exception) {

                }

            }

            DownloadManager.STATUS_SUCCESSFUL -> {
                binding.textDownloadSieze.text = "$get_fileName : 100%"

            }

        }

    }


    fun delete(file: File): Boolean {
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


    private fun showToastMessage(s: String) {
        runOnUiThread {
            Toast.makeText(applicationContext, "$s", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        closeDownloadpage()

    }


    @SuppressLint("WakelockTimeout")
    override fun onResume() {
        super.onResume()
        try {

            isSystemActive = true


            if (myHandler != null) {
                myHandler!!.removeCallbacks(runnableGetDownloadProgress)
            }


            getDownloadStatus()

            if (myHandler != null) {
                myHandler.postDelayed(runnableGetDownloadProgress, 500)
            }

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } catch (ignored: java.lang.Exception) {
        }

    }

    override fun onPause() {
        super.onPause()
        try {
            if (myHandler != null) {
                myHandler.removeCallbacks(runnableGetDownloadProgress)
            }
        } catch (ignored: java.lang.Exception) {
        }
    }

    override fun onStop() {
        super.onStop()
        try {

            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            if (wakeLock != null && wakeLock!!.isHeld) {
                wakeLock!!.release()
            }

            if (myHandler != null) {
                myHandler!!.removeCallbacks(runnableGetDownloadProgress)
            }

        } catch (ignored: java.lang.Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {

            isSystemActive = false

            if (wakeLock != null && wakeLock!!.isHeld) {
                wakeLock!!.release()
            }

            if (downloadCompleteReceiver != null) {
                unregisterReceiver(downloadCompleteReceiver)
            }

            if (myHandler != null) {
                myHandler!!.removeCallbacks(runnableGetDownloadProgress)
            }


        } catch (ignored: java.lang.Exception) {
        }
    }

    private fun stratMyACtivity() {
        try {
            handler.postDelayed(Runnable {

                val editor = sharedBiometric.edit()
                editor.remove(Constants.CALL_RE_SYNC_MANGER)
                editor.apply()

                val getFolderClo = sharedP.getString("getFolderClo", "")
                val getFolderSubpath = sharedP.getString("getFolderSubpath", "")
                val Extracted = sharedP.getString("Extracted", "")

                val getUnzipFileFrom = "/$getFolderClo/$getFolderSubpath/$Extracted"
                val intent = Intent(applicationContext, WebViewPage::class.java)
                intent.putExtra("getUnzipFileFrom", getUnzipFileFrom)

                startActivity(intent)
                finishAffinity()


            }, 6000)


        } catch (_: Exception) {
        }
    }

    private fun showCustomProgressDialog(message: String) {
        try {
            customProgressDialog = Dialog(this)
            val bindingDN = ProgressDialogLayoutBinding.inflate(LayoutInflater.from(this))
            customProgressDialog.setContentView(bindingDN.root)
            customProgressDialog.setCancelable(true)
            customProgressDialog.setCanceledOnTouchOutside(false)
            customProgressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            bindingDN.textLoading.text = message
            bindingDN.imgCloseDialog.visibility = View.GONE


            bindingDN.imgCloseDialog.setOnClickListener {
                customProgressDialog.cancel()
            }

            customProgressDialog.show()
        } catch (_: Exception) {
        }
    }

    private fun showCustomErrorDownload(message: String) {

        val bindingCm: CustomFailedDownloadsLayoutBinding = CustomFailedDownloadsLayoutBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this@RetryApiDownloadActivity)
        builder.setView(bindingCm.getRoot())
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setCancelable(true)

        if (alertDialog.window != null) {
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        bindingCm.apply {

            val get_RetryCount = sharedP.getString(Constants.RetryCount, "")

            if (!get_RetryCount.isNullOrEmpty()) {
                textDisplAyRetryCount.text = "$get_RetryCount/3"

            } else {
                textDisplAyRetryCount.text = "0/3"
            }

            if (get_RetryCount != "3") {

                val minutes = 1L
                val milliseconds = minutes * 10 * 1000 // Convert minutes to

                textCountDown.visibility = View.VISIBLE

                countdownTimer = object : CountDownTimer(milliseconds, 1000) {
                    @SuppressLint("SetTextI18n")
                    override fun onFinish() {
                        try {
                            countdownTimer?.cancel()

                            copyFilesToFailedDownloads()
                            mfilesViewModel.deleteAllFiles()
                            showCustomProgressDialog("Please wait!")

                            val editor = sharedP.edit()
                            if (get_RetryCount.equals("0") || get_RetryCount.isNullOrEmpty()) {
                                editor.putString(Constants.RetryCount, "1")
                                editor.apply()
                            } else if (get_RetryCount.equals("1")) {
                                editor.putString(Constants.RetryCount, "2")
                                editor.apply()
                            } else if (get_RetryCount.equals("2")) {
                                editor.putString(Constants.RetryCount, "3")
                                editor.apply()
                            }

                            handler.postDelayed(Runnable {
                                reTryTheDownlaods()

                            }, 4000)


                        } catch (ignored: java.lang.Exception) {
                        }
                    }

                    override fun onTick(millisUntilFinished: Long) {
                        try {

                            val totalSecondsRemaining = millisUntilFinished / 1000
                            var minutesUntilFinished = totalSecondsRemaining / 60
                            var remainingSeconds = totalSecondsRemaining % 60

                            // Adjusting minutes if seconds are in the range of 0-59
                            if (remainingSeconds == 0L && minutesUntilFinished > 0) {
                                minutesUntilFinished--
                                remainingSeconds = 59
                            }
                            val displayText =
                                String.format("CD: %d:%02d", minutesUntilFinished, remainingSeconds)
                            textCountDown.text = displayText

                        } catch (ignored: java.lang.Exception) {
                        }
                    }
                }
                countdownTimer?.start()


            }



            textLoading2.text = message.toString()

            // if the user decide to click go ahead
            textCancel.setOnClickListener {

                try {
                    copyFilesToFailedDownloads()
                    mfilesViewModel.deleteAllFiles()
                    showCustomProgressDialog("Please wait!")
                    handler.postDelayed(Runnable {
                        reTryTheDownlaods()
                    }, 4000)
                } catch (e: Exception) {
                }

                alertDialog.dismiss()
            }


            textYesButton.setOnClickListener {
                try {
                    if (downloadCompleteReceiver != null) {
                        unregisterReceiver(downloadCompleteReceiver)
                    }
                } catch (e: Exception) {
                }

                showCustomProgressDialog("Please wait!")
                stratMyACtivity()
                alertDialog.dismiss()
            }

        }

        alertDialog.show()
    }


    private fun copyFilesToNewFolder(files: List<FilesApi>) {
        if (downloadedFilesCount >= totalFiles) {

            binding.progressBarPref.progress = 100
            binding.textDownloadSieze.text = "Completed"

            if (isFailedDownload == true) {


                try {
                    if (downloadCompleteReceiver != null) {
                        unregisterReceiver(downloadCompleteReceiver)
                    }
                } catch (e: Exception) {
                }



                try {
                    dnFailedViewModel.readAllData.observe(
                        this@RetryApiDownloadActivity,
                        Observer { files ->
                            showCustomProgressDialog("Please wait!")

                            val message = "Unable to download \n ${files.size} Files"
                            myHandler.postDelayed(Runnable {
                                showCustomErrorDownload(message)
                                customProgressDialog.cancel()
                            }, 2000)

                        })
                } catch (e: Exception) {
                    finish()
                    recreate()
                }


            } else {

                val totalPercentage =  100
                binding.textPercentageCompleted.text = "$totalPercentage% Complete"


                showCustomProgressDialog("Please wait!")
                mfilesViewModel.deleteAllFiles()
                myHandler.postDelayed(Runnable {
                    stratMyACtivity()
                }, 2000)
            }

        }

    }

    private fun copyFilesToFailedDownloads() {
        dnFailedViewModel.getAllFiles().observe(this@RetryApiDownloadActivity) { filesList ->
            if (filesList.isNotEmpty()) {
                val dnFailedList = filesList.map { file ->
                    FilesApi(
                        id = file.id,
                        SN = file.SN,
                        FolderName = file.FolderName,
                        FileName = file.FileName,
                        Status = file.Status
                    )
                }
                mfilesViewModel.addMultipleFiles(dnFailedList)

            } else {
                myHandler.postDelayed(Runnable {

                    val totalPercentage =  100
                    binding.textPercentageCompleted.text = "$totalPercentage% Complete"

                    stratMyACtivity()
                }, 2000)
            }

        }
    }



    @SuppressLint("SourceLockedOrientationActivity")
    private fun applyOritenation() {
        val getState = sharedBiometric.getString(Constants.IMG_TOGGLE_FOR_ORIENTATION, "").toString()

        if (getState == Constants.USE_POTRAIT){
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        }else if (getState == Constants.USE_LANDSCAPE){

            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        }else if (getState == Constants.USE_UNSEPECIFIED){
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        }

    }



}





