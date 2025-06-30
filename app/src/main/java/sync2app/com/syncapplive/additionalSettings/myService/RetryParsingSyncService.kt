package sync2app.com.syncapplive.myService

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sync2app.com.syncapplive.R
import sync2app.com.syncapplive.additionalSettings.myApiDownload.FilesApi
import sync2app.com.syncapplive.additionalSettings.myCompleteDownload.DnApi
import sync2app.com.syncapplive.additionalSettings.myCompleteDownload.DnViewModel
import sync2app.com.syncapplive.additionalSettings.myFailedDownloadfiles.DnFailedApi
import sync2app.com.syncapplive.additionalSettings.myFailedDownloadfiles.DnFailedViewModel
import sync2app.com.syncapplive.additionalSettings.urlchecks.checkUrlExistence
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.additionalSettings.utils.Utility
import java.io.File
import java.util.Objects

@OptIn(DelicateCoroutinesApi::class)
class RetryParsingSyncService : Service() {

   // private lateinit var dnFailedViewModel: DnFailedViewModel
    private lateinit var dnViewModel: DnViewModel

    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private val myHandler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }
    private val handlerParsing: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private val sharedBiometric: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_BIOMETRIC,
            Context.MODE_PRIVATE
        )
    }

    private var KoloLog = "ParsingSyncService"

    private var manager: DownloadManager? = null
    private var currentDownloadIndex = 0
    private var downloadedFilesCount = 0
    private var totalFiles: Int = 0
    private var isFailedDownload = false
    private var isProgresStarted = true
    private var isDownloadBytesSent = true
    private var downloadReference: Long = -1L
    private var isCalled = true
    private val sharedP: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.MY_DOWNLOADER_CLASS,
            Context.MODE_PRIVATE
        )
    }

    private var isDnFailed = true


    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()

        // Initialize ViewModels using the application context
        dnViewModel = DnViewModel(application)

        manager = applicationContext.getSystemService(DOWNLOAD_SERVICE) as DownloadManager


        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            startMyOwnForeground()
        } else {
            startForeground(1, Notification())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val editor = sharedP.edit()
        editor.remove(Constants.numberFailedFiles)
        editor.remove(Constants.ParsingDownloadBytesProgress)
        editor.apply()

        Toast.makeText(applicationContext, "Retry Parsing Service Called", Toast.LENGTH_SHORT).show()



        if (myHandler != null) {
            myHandler!!.removeCallbacks(runnableGetDownloadProgress)
        }

        startParsringDownload()


        return START_STICKY
    }



    private fun startParsringDownload() {

        if (Utility.isNetworkAvailable(applicationContext)) {
            val imagUsemanualOrnotuseManual = sharedBiometric.getString(Constants.imagSwtichEnableManualOrNot, "").toString()

            if (imagUsemanualOrnotuseManual.equals(Constants.imagSwtichEnableManualOrNot)) {
                handlerParsing.postDelayed(Runnable {

                    val getSavedEditTextInputSynUrlZip = sharedP.getString(Constants.getSavedEditTextInputSynUrlZip, "").toString()

                    if (getSavedEditTextInputSynUrlZip.contains("/App/index.html")) {
                        myHandler.postDelayed(runnableManual, 500)
                    } else {
                        showToastMessage("Something went wrong, System Could not locate CSV or index file  from this Location")


                    }

                }, 1000)

            } else {

                handler.postDelayed(Runnable {
                    myHandler.postDelayed(runnableGetApiStart, 500)

                }, 5000)

            }

        } else {

            Toast.makeText(applicationContext, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }

    }



    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()

        if (myHandler != null) {
            myHandler!!.removeCallbacks(runnableGetDownloadProgress)
        }

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        val newsTitle = "Re_Parsing Sync"

        val builder = NotificationCompat.Builder(applicationContext, "ChannelId")
            .setSmallIcon(R.drawable.img_logo_icon)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentText(newsTitle)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("ChannelId", "News", importance)

            notificationManager.createNotificationChannel(channel)

            startForeground(2, builder.build())
        }
    }


    /////  Next step is to begin sequential download
    private val runnableGetApiStart: java.lang.Runnable = object : java.lang.Runnable {
        @SuppressLint("SetTextI18n")
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun run() {
            dnViewModel.readAllData.observe(ProcessLifecycleOwner.get(),
                Observer { files ->
                    if (files.isNotEmpty()) {

                        handler.postDelayed(Runnable {
                            totalFiles = files.size.toInt()
                            downloadSequentially(files)
                        }, 2000)

                    } else {
                        // showToastMessage("No files found")
                    }
                })
        }

    }



    // for manual
    private val runnableManual: Runnable = object : Runnable {
        @SuppressLint("SetTextI18n")
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun run() {
            dnViewModel.readAllData.observe(ProcessLifecycleOwner.get(),
                Observer { files ->
                    if (files.isNotEmpty()) {

                        handler.postDelayed(Runnable {
                            totalFiles = files.size.toInt()
                            downloadSequentiallyManually(files)
                        }, 2000)

                    } else {
                        /// showToastMessage("No files found")
                    }
                })
        }

    }




    @SuppressLint("SetTextI18n")
    private fun downloadSequentially(files: List<DnApi>) {
        if (currentDownloadIndex < files.size) {
            val file = files[currentDownloadIndex]
            handler.postDelayed(Runnable {
                getZipDownloads(file.SN, file.FolderName, file.FileName)
            }, 1000)

        }
    }



    // for manaul
    @SuppressLint("SetTextI18n")
    private fun downloadSequentiallyManually(files: List<DnApi>) {
        if (currentDownloadIndex < files.size) {
            val file = files[currentDownloadIndex]
            handlerParsing.postDelayed(Runnable {
                getZipDownloadsManually(file.SN, file.FolderName, file.FileName)
            }, 1000)

        }
    }

    private fun getZipDownloadsManually(sn: String, folderName: String, fileName: String) {
        if (isProgresStarted) {
            isProgresStarted = false

            val intent = Intent(Constants.RECIVER_PROGRESS)
            intent.putExtra(Constants.ParsingStatusSync, Constants.PR_Downloading)
            sendBroadcast(intent)

            getDownloadStatus()
            myHandler?.postDelayed(runnableGetDownloadProgress, 500)
        }

        val Syn2AppLive = Constants.Syn2AppLive
        val Demo_Parsing_Folder = Constants.TEMP_PARS_FOLDER
        val relativePath = "$Syn2AppLive/$Demo_Parsing_Folder/CLO/MANUAL/DEMO/$folderName"

        val originalUrl = sharedP.getString(Constants.getSavedEditTextInputSynUrlZip, "").orEmpty()
        val replacedUrl = if (originalUrl.contains("/App/index.html")) {
            originalUrl.replace("/App/index.html", "/$folderName/$fileName")
        } else {
            Log.d("getZipDownloadsManually", "Unable to replace this url")
            originalUrl // fallback to the original
        }

        val targetDir = File(getExternalFilesDir(null), relativePath)
        val targetFile = File(targetDir, fileName)
        if (targetFile.exists()) targetFile.delete()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = checkUrlExistence(replacedUrl)
                if (result) {
                    withContext(Dispatchers.Main) {
                        sharedP.edit().apply {
                            putString(Constants.fileNumber, sn)
                            putString(Constants.folderName, folderName)
                            putString(Constants.fileName, fileName)
                            apply()
                        }

                        if (!targetDir.exists()) {
                            targetDir.mkdirs()
                        }

                        val managerDownload = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                        val request = DownloadManager.Request(Uri.parse(replacedUrl)).apply {
                            setTitle(fileName)
                            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            setDestinationUri(Uri.fromFile(targetFile)) // ✅ Scoped storage-compliant
                        }

                        val downloadReferenceMain = managerDownload.enqueue(request)
                        downloadReference = downloadReferenceMain

                        sharedP.edit().putLong(Constants.downloadKey, downloadReferenceMain).apply()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        getNextOnFailedDownload(sn, folderName, fileName)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    getNextOnFailedDownload(sn, folderName, fileName)
                    Log.d(KoloLog, "getZipDownloadsManually: ${e.message}")
                }
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun getZipDownloads(sn: String, folderName: String, fileName: String) {
        if (isProgresStarted) {
            isProgresStarted = false

            val intent = Intent(Constants.RECIVER_PROGRESS)
            intent.putExtra(Constants.ParsingStatusSync, Constants.PR_Downloading)
            sendBroadcast(intent)

            getDownloadStatus()
            myHandler?.postDelayed(runnableGetDownloadProgress, 500)
        }

        val getFolderClo = sharedP.getString(Constants.getFolderClo, "").orEmpty()
        val getFolderSubpath = sharedP.getString(Constants.getFolderSubpath, "").orEmpty()
        val get_ModifiedUrl = sharedP.getString(Constants.get_ModifiedUrl, "").orEmpty()
        val Syn2AppLive = Constants.Syn2AppLive
        val Demo_Parsing_Folder = Constants.TEMP_PARS_FOLDER

        val relativePath = "$Syn2AppLive/$Demo_Parsing_Folder/$getFolderClo/$getFolderSubpath/$folderName"
        val getFileUrl = "$get_ModifiedUrl/$getFolderClo/$getFolderSubpath/$folderName/$fileName"
        val targetDir = File(getExternalFilesDir(null), relativePath)
        val targetFile = File(targetDir, fileName)

        if (targetFile.exists()) targetFile.delete()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = checkUrlExistence(getFileUrl)
                if (result) {
                    withContext(Dispatchers.Main) {
                        sharedP.edit().apply {
                            putString(Constants.fileNumber, sn)
                            putString(Constants.folderName, folderName)
                            putString(Constants.fileName, fileName)
                            apply()
                        }

                        if (!targetDir.exists()) {
                            targetDir.mkdirs()
                        }

                        val managerDownload = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                        val request = DownloadManager.Request(Uri.parse(getFileUrl)).apply {
                            setTitle(fileName)
                            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            setDestinationUri(Uri.fromFile(targetFile)) // ✅ Scoped storage-compliant
                        }

                        val downloadReferenceMain = managerDownload.enqueue(request)
                        downloadReference = downloadReferenceMain

                        sharedP.edit().putLong(Constants.downloadKey, downloadReferenceMain).apply()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        getNextOnFailedDownload(sn, folderName, fileName)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    getNextOnFailedDownload(sn, folderName, fileName)
                    Log.d(KoloLog, "getZipDownloads: ${e.message}")
                }
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun getNextOnFailedDownload(sn: String, folderName: String, fileName: String) {
        if (isDnFailed) {
            isDnFailed = false

                handler.postDelayed(Runnable {
                    Log.d(KoloLog, "checkUrlExistence: Falsed")
                    val editior = sharedP.edit()
                    editior.putString(Constants.fileNumber, sn)
                    editior.putString(Constants.folderName, folderName)
                    editior.putString(Constants.fileName, fileName)
                    editior.apply()

                    checkWhatAreaToDownloadFromDueToErrorIssues(
                        currentDownloadIndex.toString(),
                        folderName,
                        fileName
                    )

                    isDnFailed = true

                }, 2000)

        }
    }





    private fun checkWhatAreaToDownloadFromDueToErrorIssues(
        sn: String,
        folderName: String,
        fileName: String
    ) {
        isFailedDownload = true
        GlobalScope.launch(Dispatchers.IO){
            currentDownloadIndex++
            downloadSequentially(dnViewModel.readAllData.value ?: emptyList())

            downloadedFilesCount++
            copyFilesToNewFolder()

            withContext(Dispatchers.Main) {
                val message = "DL:$currentDownloadIndex/$totalFiles"
                val intent = Intent(Constants.RECIVER_PROGRESS)
                intent.putExtra(Constants.ParsingProgressBar, message)
                sendBroadcast(intent)
            }

        }
    }


    @SuppressLint("SetTextI18n")
    private fun nextFileDownloadOnSucessNote() {
        GlobalScope.launch(Dispatchers.IO){
            currentDownloadIndex++
            downloadSequentially(dnViewModel.readAllData.value ?: emptyList())

            downloadedFilesCount++
            copyFilesToNewFolder()

            withContext(Dispatchers.Main) {

                val message = "DL:$currentDownloadIndex/$totalFiles"
                val intent = Intent(Constants.RECIVER_PROGRESS)
                intent.putExtra(Constants.ParsingProgressBar, message)
                sendBroadcast(intent)


                if (isDownloadBytesSent) {
                    isDownloadBytesSent = false
                    handler.postDelayed(Runnable {
                        val percent = (currentDownloadIndex.toFloat() / totalFiles * 100).toInt()
                        val intentBytes = Intent(Constants.RECIVER_DOWNLOAD_BYTES_PROGRESS)
                        sendBroadcast(intentBytes)
                        isDownloadBytesSent = true

                        val editor = sharedP.edit()
                        editor.putInt(Constants.ParsingDownloadBytesProgress, percent)
                        editor.apply()

                        Log.d("isDownloadBytesSent", "$percent")
                    }, 2000)
                }

            }

        }
    }


    private val runnableGetDownloadProgress: java.lang.Runnable = object : java.lang.Runnable {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun run() {
            getDownloadStatus()
            myHandler.postDelayed(this, 500)
        }
    }

   private fun getDownloadStatus() {
        GlobalScope.launch (Dispatchers.IO){
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

                    //    binding.progressBar.setProgress(dl_progress)
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
    }


    @SuppressLint("Range", "SetTextI18n")
    private fun statusMessage(c: Cursor, dl_progress: Int, get_fileName: String) {
        GlobalScope.launch(Dispatchers.IO){
            var msg: String
            when (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))) {


                DownloadManager.STATUS_PENDING -> {
                    try {
                        msg = "Pending download .."
                        //  Log.d(KoloLog, "$msg")
                    } catch (e: Exception) {
                        Log.e(KoloLog, "Error handling download success", e)
                    }

                }



                DownloadManager.STATUS_PAUSED -> {
                    try {
                        msg = "Download Paused, check internet"
                        //  Log.d(KoloLog, "$msg")
                        withContext(Dispatchers.Main){
                            ptDownloadFiled()
                        }
                    } catch (e: Exception) {
                        Log.e(KoloLog, "Error handling STATUS_PAUSED", e)
                        withContext(Dispatchers.Main){
                            ptDownloadFiled()
                        }
                    }

                }

                DownloadManager.STATUS_FAILED -> {
                    try {
                        msg = "Download Failed!, Retry.."
                        withContext(Dispatchers.Main) {
                            ptDownloadFiled()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Log.e(KoloLog, "Error handling STATUS_FAILED", e)
                            ptDownloadFiled()
                        }
                    }

                }



                DownloadManager.STATUS_SUCCESSFUL -> {
                    try {
                        withContext(Dispatchers.Main){
                            ptNextDownload()
                        }
                    } catch (e: Exception) {
                        Log.e(KoloLog, "Error handling download success", e)
                        withContext(Dispatchers.Main){
                            ptNextDownload()
                        }
                    }
                }


            }

        }

    }

    @SuppressLint("SuspiciousIndentation")
    private fun ptNextDownload(){
        if (isDnFailed) {
            isDnFailed = false

                handler.postDelayed({
                    nextFileDownloadOnSucessNote()
                    isDnFailed = true
                }, 2000)

        }

    }



    @SuppressLint("SuspiciousIndentation")
    private fun ptDownloadFiled(){
        if (isDnFailed){
            isDnFailed = false

                handler.postDelayed(Runnable {
                    val get_folderName = sharedP.getString(Constants.folderName, "").toString()
                    val get_fileName = sharedP.getString(Constants.fileName, "").toString()

                    showToastMessage("Unable to download $get_fileName")

                    checkWhatAreaToDownloadFromDueToErrorIssues(
                        currentDownloadIndex.toString(),
                        get_folderName,
                        get_fileName
                    )

                    isDnFailed = true

                }, 2000)
        }

    }

    private fun copyFilesToNewFolder() {
        try {
            GlobalScope.launch {
                if (downloadedFilesCount >= totalFiles) {

                    // Stop all Handlers
                    if (myHandler != null) {
                        myHandler!!.removeCallbacks(runnableGetDownloadProgress)
                    }

                    if (isCalled){
                        isCalled = false

                            val message = "DL:$totalFiles/$totalFiles"
                            val intent = Intent(Constants.RECIVER_PROGRESS)
                            intent.putExtra(Constants.ParsingProgressBar, message)
                            sendBroadcast(intent)

                            myHandler.postDelayed(Runnable {
                                stratParsingFilesSorting()
                            }, 2000)

                            Log.d(KoloLog, "All Some down,oad failed")
                    }

                }


            }
        }catch (e:Exception){

            val message = "DL:$totalFiles/$totalFiles"
            val intent = Intent(Constants.RECIVER_PROGRESS)
            intent.putExtra(Constants.ParsingProgressBar, message)
            sendBroadcast(intent)


            myHandler.postDelayed(Runnable {
                stratParsingFilesSorting()
            }, 2000)

            Log.d(KoloLog, "All Some down,oad failed")
        }
    }

    private fun showToastMessage(messages: String) {

        try {
            Toast.makeText(applicationContext, messages, Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
        }
    }





    private fun stratParsingFilesSorting() {
        handlerParsing.postDelayed(Runnable {

            val intent = Intent(Constants.RECIVER_PROGRESS)
            intent.putExtra(Constants.ParsingStatusSync, Constants.PR_Indexing_Files)
            sendBroadcast(intent)

            handlerParsing.postDelayed(Runnable {
                val intent22 = Intent(Constants.RECIVER_PROGRESS)
                intent22.putExtra(Constants.ParsingStatusSync, Constants.PR_Refresh)
                sendBroadcast(intent22)

                applicationContext.stopService(Intent(applicationContext, RetryParsingSyncService::class.java))

                Log.d(KoloLog, "All Some download Complete")

            }, 2000)


        }, 700)

    }






    /*
        private fun stratParsingFilesSorting() {
            handler.postDelayed(Runnable {

                val intent = Intent(Constants.RECIVER_PROGRESS)
                intent.putExtra(Constants.ParsingStatusSync, Constants.PR_Indexing_Files)
                sendBroadcast(intent)

               /// copyFilesAndFolders()


                handler.postDelayed(Runnable {
                    val intent22 = Intent(Constants.RECIVER_PROGRESS)
                    intent22.putExtra(Constants.ParsingStatusSync, Constants.PR_Refresh)
                    sendBroadcast(intent22)

                    applicationContext.stopService(Intent(applicationContext, RetryParsingSyncService::class.java))

                    Log.d(KoloLog, "All Some download Complete")

                }, 2000)

            }, 700)

        }
    */




}
