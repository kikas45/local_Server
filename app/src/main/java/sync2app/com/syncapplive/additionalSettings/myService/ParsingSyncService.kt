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
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sync2app.com.syncapplive.R
import sync2app.com.syncapplive.additionalSettings.DownloadApisFilesActivityParsing
import sync2app.com.syncapplive.additionalSettings.myApiDownload.FilesApi
import sync2app.com.syncapplive.additionalSettings.myApiDownload.FilesViewModel
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
class ParsingSyncService : Service() {

    private lateinit var mfilesViewModel: FilesViewModel
    private lateinit var dnViewModel: DnViewModel

    private val handlerParsing: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private val myHandlerParsing: Handler by lazy {
        Handler(Looper.getMainLooper())
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

    private val sharedBiometric: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_BIOMETRIC,
            Context.MODE_PRIVATE
        )
    }


    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()

        // Initialize ViewModels using the application context
        mfilesViewModel = FilesViewModel(application)
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

        Toast.makeText(applicationContext, "Parsing Service Called", Toast.LENGTH_SHORT).show()


        if (myHandlerParsing != null) {
            myHandlerParsing!!.removeCallbacks(runnableGetDownloadProgressForParsingFiles)
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
                        myHandlerParsing.postDelayed(runnableManual, 500)
                    } else {
                        showToastMessage("Something went wrong, System Could not locate CSV or index file  from this Location")


                    }

                }, 1000)

            } else {

                handlerParsing.postDelayed(Runnable {
                    myHandlerParsing.postDelayed(runnableStartParsingSync, 500)

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

        if (myHandlerParsing != null) {
            myHandlerParsing!!.removeCallbacks(runnableGetDownloadProgressForParsingFiles)
        }

    }


    @SuppressLint("ForegroundServiceType")
    private fun startMyOwnForeground() {
        val newsTitle = "Parsing Sync"

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



    /////  Next step is to begin sequential download for parsing files

    private val runnableStartParsingSync: Runnable = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {
            mfilesViewModel.readAllData.observe(ProcessLifecycleOwner.get(),
                Observer { files ->
                    if (files.isNotEmpty()) {

                        handlerParsing.postDelayed(Runnable {
                            //  binding.textDisplaytext.text = "0 / ${files.size}   Files Downloaded"
                            totalFiles = files.size.toInt()

                            downloadSequentiallyForTheParsingSync(files)
                            // Log.d(KoloLog, "onStartCommand: downloadSequentially")
                        }, 2000)

                    } else {
                        // showToastMessage("No files found")
                    }
                })
        }

    }



    // for manaul
    private val runnableManual: Runnable = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {
            mfilesViewModel.readAllData.observe(ProcessLifecycleOwner.get(),
                Observer { files ->
                    if (files.isNotEmpty()) {

                        handlerParsing.postDelayed(Runnable {
                            //  binding.textDisplaytext.text = "0 / ${files.size}   Files Downloaded"
                            totalFiles = files.size.toInt()

                            downloadSequentiallyManually(files)
                            // Log.d(KoloLog, "onStartCommand: downloadSequentially")
                        }, 2000)

                    } else {
                        // showToastMessage("No files found")
                    }
                })
        }

    }




    @SuppressLint("SetTextI18n")
    private fun downloadSequentiallyForTheParsingSync(files: List<FilesApi>) {
        if (currentDownloadIndex < files.size) {
            val file = files[currentDownloadIndex]
            handlerParsing.postDelayed(Runnable {
                getParsingFilesDownload(file.SN, file.FolderName, file.FileName)
                /// Log.d(KoloLog, "onStartCommand: getZipDownloads")
            }, 1000)

        }
    }



    // for manaul
    @SuppressLint("SetTextI18n")
    private fun downloadSequentiallyManually(files: List<FilesApi>) {
        if (currentDownloadIndex < files.size) {
            val file = files[currentDownloadIndex]
            handlerParsing.postDelayed(Runnable {
                getZipDownloadsManually(file.SN, file.FolderName, file.FileName)
                /// Log.d(KoloLog, "onStartCommand: getZipDownloads")
            }, 1000)

        }


    }


    // for manaul
    private fun getZipDownloadsManually(sn: String, folderName: String, fileName: String) {

        if (isProgresStarted) {
            isProgresStarted = false

            val intent = Intent(Constants.RECIVER_PROGRESS)
            intent.putExtra(Constants.ParsingStatusSync, Constants.PR_Downloading)
            sendBroadcast(intent)


            getDownloadStatusParsing()
            if (myHandlerParsing != null) {
                myHandlerParsing.postDelayed(runnableGetDownloadProgressForParsingFiles, 500)
            }
        }



        val Syn2AppLive = Constants.Syn2AppLive
        val Demo_Parsing_Folder = Constants.TEMP_PARS_FOLDER
        val saveMyFileToStorage = "/$Syn2AppLive/$Demo_Parsing_Folder/CLO/MANUAL/DEMO/$folderName"

        val getSavedEditTextInputSynUrlZip = sharedP.getString(Constants.getSavedEditTextInputSynUrlZip, "").toString()

        var replacedUrl = getSavedEditTextInputSynUrlZip // Initialize it with original value



        if (getSavedEditTextInputSynUrlZip.contains("/App/index.html")) {
            replacedUrl = getSavedEditTextInputSynUrlZip.replace(
                "/App/index.html",
                "/$folderName/$fileName"
            )

        } else {

            Log.d("getZipDownloadsManually", "Unable to replace this url")
        }


        GlobalScope.launch(Dispatchers.IO) {
            //  Log.d(KoloLog, "getZipDownloads:$getFileUrl ")
            try {
                val result = checkUrlExistence(replacedUrl)
                if (result) {
                    //    Log.d(KoloLog, "checkUrlExistence: Sucessful")

                    handlerParsing.postDelayed(Runnable {
                        val editior = sharedP.edit()
                        editior.putString(Constants.fileNumber, sn)
                        editior.putString(Constants.folderName, folderName)
                        editior.putString(Constants.fileName, fileName)
                        editior.apply()

                        val dir = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            saveMyFileToStorage
                        )
                        if (!dir.exists()) {
                            dir.mkdirs()
                        }

                        val managerDownload =
                            getSystemService(DOWNLOAD_SERVICE) as DownloadManager

                        // save files to this folder
                        val folder = File(
                            Environment.getExternalStorageDirectory()
                                .toString() + "/Download/$saveMyFileToStorage"
                        )

                        if (!folder.exists()) {
                            folder.mkdirs()
                        }

                        val request = DownloadManager.Request(Uri.parse(replacedUrl))
                        request.setTitle(fileName)
                        request.allowScanningByMediaScanner()
                        request.setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS, "/$saveMyFileToStorage/$fileName"
                        )
                        val downloadReferenceMain = managerDownload.enqueue(request)
                        downloadReference = downloadReferenceMain
                        val editor = sharedP.edit()
                        editor.putLong(Constants.downloadKey, downloadReferenceMain)
                        editor.apply()


                    }, 300)
                } else {

                    withContext(Dispatchers.Main) {
                        getNextOnFailedDownload(
                            sn = sn,
                            folderName = folderName,
                            fileName = fileName
                        )

                    }


                }
            } catch (e: Exception) {

                withContext(Dispatchers.Main) {
                    getNextOnFailedDownload(sn = sn, folderName = folderName, fileName = fileName)
                    Log.d(KoloLog, "getZipDownloads: ${e.message.toString()}")
                }


            }

        }


    }



    @SuppressLint("SetTextI18n")
    private fun getParsingFilesDownload(sn: String, folderName: String, fileName: String) {

        if (isProgresStarted) {
            isProgresStarted = false

            val intent = Intent(Constants.RECIVER_PROGRESS)
            intent.putExtra(Constants.ParsingStatusSync, Constants.PR_Downloading)
            sendBroadcast(intent)


            getDownloadStatusParsing()
            if (myHandlerParsing != null) {
                myHandlerParsing.postDelayed(runnableGetDownloadProgressForParsingFiles, 500)
            }
        }

        val getFolderClo = sharedP.getString(Constants.getFolderClo, "").toString()
        val getFolderSubpath = sharedP.getString(Constants.getFolderSubpath, "").toString()
        val get_ModifiedUrl = sharedP.getString(Constants.get_ModifiedUrl, "").toString()
        val Syn2AppLive = Constants.Syn2AppLive
        val Demo_Parsing_Folder = Constants.TEMP_PARS_FOLDER
        val saveMyFileToStorage =
            "/$Syn2AppLive/$Demo_Parsing_Folder/$getFolderClo/$getFolderSubpath/$folderName"

        val getFileUrl = "$get_ModifiedUrl/$getFolderClo/$getFolderSubpath/$folderName/$fileName"

        GlobalScope.launch(Dispatchers.IO) {
            //  Log.d(KoloLog, "getZipDownloads:$getFileUrl ")
            try {
                val result = checkUrlExistence(getFileUrl)
                if (result) {
                    //    Log.d(KoloLog, "checkUrlExistence: Sucessful")

                    handlerParsing.postDelayed(Runnable {
                        val editior = sharedP.edit()
                        editior.putString(Constants.fileNumber, sn)
                        editior.putString(Constants.folderName, folderName)
                        editior.putString(Constants.fileName, fileName)
                        editior.apply()

                        val dir = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            saveMyFileToStorage
                        )
                        if (!dir.exists()) {
                            dir.mkdirs()
                        }

                        val managerDownload =
                            getSystemService(DOWNLOAD_SERVICE) as DownloadManager

                        // save files to this folder
                        val folder = File(
                            Environment.getExternalStorageDirectory()
                                .toString() + "/Download/$saveMyFileToStorage"
                        )

                        if (!folder.exists()) {
                            folder.mkdirs()
                        }

                        val request = DownloadManager.Request(Uri.parse(getFileUrl))
                        request.setTitle(fileName)
                        request.allowScanningByMediaScanner()
                        request.setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS, "/$saveMyFileToStorage/$fileName"
                        )
                        val downloadReferenceMain = managerDownload.enqueue(request)
                        downloadReference = downloadReferenceMain
                        val editor = sharedP.edit()
                        editor.putLong(Constants.downloadKey, downloadReferenceMain)
                        editor.apply()


                    }, 300)
                } else {

                    withContext(Dispatchers.Main) {
                        getNextOnFailedDownload(
                            sn = sn,
                            folderName = folderName,
                            fileName = fileName
                        )

                    }


                }
            } catch (e: Exception) {

                withContext(Dispatchers.Main) {
                    getNextOnFailedDownload(sn = sn, folderName = folderName, fileName = fileName)
                    Log.d(KoloLog, "getZipDownloads: ${e.message.toString()}")
                }


            }

        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun getNextOnFailedDownload(sn: String, folderName: String, fileName: String) {

        if (isDnFailed) {
            isDnFailed = false

            handlerParsing.postDelayed(Runnable {

                Log.d(KoloLog, "checkUrlExistence: Falsed")
                val editior = sharedP.edit()
                editior.putString(Constants.fileNumber, sn)
                editior.putString(Constants.folderName, folderName)
                editior.putString(Constants.fileName, fileName)
                editior.apply()

                showToastMessage("Unable to download $fileName")

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
        GlobalScope.launch(Dispatchers.IO) {
            currentDownloadIndex++
            downloadSequentiallyForTheParsingSync(mfilesViewModel.readAllData.value ?: emptyList())

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
        GlobalScope.launch(Dispatchers.IO) {
            currentDownloadIndex++
            downloadSequentiallyForTheParsingSync(mfilesViewModel.readAllData.value ?: emptyList())

            downloadedFilesCount++
            copyFilesToNewFolder()

            withContext(Dispatchers.Main) {
                Log.d(KoloLog, "$currentDownloadIndex / $totalFiles Files Downloaded")

                val message = "DL:$currentDownloadIndex/$totalFiles"
                val intent = Intent(Constants.RECIVER_PROGRESS)
                intent.putExtra(Constants.ParsingProgressBar, message)
                sendBroadcast(intent)


                if (isDownloadBytesSent) {
                    isDownloadBytesSent = false
                    handlerParsing.postDelayed(Runnable {
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

            // remove a successful files from the failed downloads database
            val get_fileNumber = sharedP.getString(Constants.fileNumber, "").toString()
            val get_folderName = sharedP.getString(Constants.folderName, "").toString()
            val get_fileName = sharedP.getString(Constants.fileName, "").toString()

            val dnApiFailed = DnApi(
                SN = get_fileNumber,
                FolderName = get_folderName,
                FileName = get_fileName,
                Status = "true"
            )

            dnViewModel.deleteFiles(dnApiFailed)

        }
    }


    private val runnableGetDownloadProgressForParsingFiles: Runnable = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun run() {
            getDownloadStatusParsing()
            myHandlerParsing.postDelayed(this, 500)
        }
    }


    private fun getDownloadStatusParsing() {
        GlobalScope.launch(Dispatchers.IO) {
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
                        statusMessageParsing(c, dl_progress, get_fileName)
                    } else {
                        c.moveToFirst()
                        statusMessageParsing(c, dl_progress, get_fileName)
                    }


                }
            } catch (ignored: java.lang.Exception) {
            }
        }
    }


    @SuppressLint("Range", "SetTextI18n")
    private fun statusMessageParsing(c: Cursor, dl_progress: Int, get_fileName: String) {
        GlobalScope.launch(Dispatchers.IO) {
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
                        withContext(Dispatchers.Main) {
                            ptDownloadFiled()
                        }
                    } catch (e: Exception) {
                        Log.e(KoloLog, "Error handling STATUS_PAUSED", e)
                        withContext(Dispatchers.Main) {
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
                        withContext(Dispatchers.Main) {
                            ptNextDownload()
                        }
                    } catch (e: Exception) {
                        Log.e(KoloLog, "Error handling download success", e)
                        withContext(Dispatchers.Main) {
                            ptNextDownload()
                        }
                    }
                }


            }

        }

    }


    private fun ptNextDownload() {
        if (isDnFailed) {
            isDnFailed = false

            handlerParsing.postDelayed({
                nextFileDownloadOnSucessNote()
                isDnFailed = true
            }, 2000)

        }

    }


    private fun ptDownloadFiled() {
        if (isDnFailed) {
            isDnFailed = false

            handlerParsing.postDelayed(Runnable {
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
                    if (myHandlerParsing != null) {
                        myHandlerParsing!!.removeCallbacks(
                            runnableGetDownloadProgressForParsingFiles
                        )
                    }

                    if (isCalled) {
                        isCalled = false


                        if (isFailedDownload) {

                            handlerParsing.postDelayed(Runnable {

                                val intent = Intent(Constants.RECIVER_PROGRESS)
                                intent.putExtra(Constants.ParsingStatusSync, Constants.PR_Retry_Failed)
                                sendBroadcast(intent)

                                var isCalledFiles = false
                                dnViewModel.readAllData.observe(ProcessLifecycleOwner.get(),
                                    Observer { files ->
                                        if (!isCalledFiles && files.isNotEmpty()) {
                                            isCalledFiles = true

                                            val message = "PR:${files.size} Failed"

                                            myHandlerParsing.postDelayed({
                                                Log.d("JoelPowell", "Numbers: ${files.size}")

                                                val intent11 = Intent(Constants.RECIVER_PROGRESS)
                                                intent11.putExtra(Constants.ParsingStatusSync, Constants.PR_Failed_Files_Number)
                                                sendBroadcast(intent11)

                                                val editor = sharedP.edit()
                                                editor.putString(Constants.numberFailedFiles, message)
                                                editor.apply()

                                                var isSendit = true
                                                handlerParsing.postDelayed(Runnable {
                                                    if (isSendit) {
                                                        isSendit = false
                                                        val intent22 = Intent(Constants.RECIVER_PROGRESS)
                                                        intent22.putExtra(Constants.ParsingStatusSync, Constants.RE_START_PARSING)
                                                        sendBroadcast(intent22)

                                                        applicationContext.startService(Intent(applicationContext, RetryParsingSyncService::class.java))
                                                        applicationContext.stopService(Intent(applicationContext, ParsingSyncService::class.java))

                                                    }
                                                }, 8000)


                                            }, 3000)
                                        }
                                    }
                                )


                            }, 1000)

                        } else {

                            val message = "DL:$totalFiles/$totalFiles"
                            val intent = Intent(Constants.RECIVER_PROGRESS)
                            intent.putExtra(Constants.ParsingProgressBar, message)
                            sendBroadcast(intent)

                            myHandlerParsing.postDelayed(Runnable {
                                stratParsingFilesSorting()
                            }, 2000)

                            Log.d(KoloLog, "All Some down,oad failed")


                        }


                    }
                }
            }


        } catch (e: Exception) {
            val message = "DL:$totalFiles/$totalFiles"
            val intent = Intent(Constants.RECIVER_PROGRESS)
            intent.putExtra(Constants.ParsingProgressBar, message)
            sendBroadcast(intent)

            myHandlerParsing.postDelayed(Runnable {
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

            /// copyFilesAndFolders()

            handlerParsing.postDelayed(Runnable {
                val intent22 = Intent(Constants.RECIVER_PROGRESS)
                intent22.putExtra(Constants.ParsingStatusSync, Constants.PR_Refresh)
                sendBroadcast(intent22)

                applicationContext.stopService(Intent(applicationContext, ParsingSyncService::class.java))

                Log.d(KoloLog, "All Some download Complete")

            }, 2000)


        }, 700)

    }
}
