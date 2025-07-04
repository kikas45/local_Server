package sync2app.com.syncapplive.additionalSettings


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import sync2app.com.syncapplive.WebViewPage
import sync2app.com.syncapplive.R
import sync2app.com.syncapplive.SettingsActivityKT
import sync2app.com.syncapplive.additionalSettings.ApiUrls.ApiUrlViewModel
import sync2app.com.syncapplive.additionalSettings.ApiUrls.DomainUrl
import sync2app.com.syncapplive.additionalSettings.ApiUrls.SavedApiAdapter
import sync2app.com.syncapplive.additionalSettings.autostartAppOncrash.Methods
import sync2app.com.syncapplive.additionalSettings.myApiDownload.FilesApi
import sync2app.com.syncapplive.additionalSettings.myApiDownload.FilesViewModel
import sync2app.com.syncapplive.additionalSettings.myCompleteDownload.DnViewModel
import sync2app.com.syncapplive.additionalSettings.myFailedDownloadfiles.DnFailedApi
import sync2app.com.syncapplive.additionalSettings.myFailedDownloadfiles.DnFailedViewModel
import sync2app.com.syncapplive.additionalSettings.myParsingDownloadDataBase.ParsingApi
import sync2app.com.syncapplive.additionalSettings.myParsingDownloadDataBase.ParsingViewModel
import sync2app.com.syncapplive.additionalSettings.utils.CSVDownloader
import sync2app.com.syncapplive.additionalSettings.savedDownloadHistory.SavedHistoryListAdapter
import sync2app.com.syncapplive.additionalSettings.savedDownloadHistory.User
import sync2app.com.syncapplive.additionalSettings.savedDownloadHistory.UserViewModel
import sync2app.com.syncapplive.additionalSettings.urlchecks.checkUrlExistence
import sync2app.com.syncapplive.additionalSettings.urlchecks.isUrlValid
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.additionalSettings.utils.FileChecker
import sync2app.com.syncapplive.additionalSettings.utils.Utility
import sync2app.com.syncapplive.databinding.ActivitySyncPowellBinding
import sync2app.com.syncapplive.databinding.ContinueWithConfigDownloadBinding
import sync2app.com.syncapplive.databinding.CustomApiHardCodedLayoutBinding
import sync2app.com.syncapplive.databinding.CustomApiUrlLayoutBinding
import sync2app.com.syncapplive.databinding.CustomContinueDownloadLayoutBinding
import sync2app.com.syncapplive.databinding.CustomDefinedTimeIntervalsBinding
import sync2app.com.syncapplive.databinding.CustomGoogleDriveUrlBinding
import sync2app.com.syncapplive.databinding.CustomSavedHistoryLayoutBinding
import sync2app.com.syncapplive.databinding.CustomSelectLauncOrOfflinePopLayoutBinding
import sync2app.com.syncapplive.databinding.CustomSelectSyncTypeBinding
import sync2app.com.syncapplive.databinding.CustomSortFilesLayoutBinding
import sync2app.com.syncapplive.databinding.FinishWithConfigDownloadBinding
import sync2app.com.syncapplive.databinding.ProgressDialogLayoutBinding
import sync2app.com.syncapplive.databinding.SampleProgressConfigLayoutBinding
import java.io.File
import java.io.FileInputStream
import java.util.Objects
import java.util.zip.ZipInputStream


class ReSyncActivity : AppCompatActivity(), SavedHistoryListAdapter.OnItemClickListener,
    SavedApiAdapter.OnItemClickListener {
    private lateinit var binding: ActivitySyncPowellBinding

    private val mUserViewModel by viewModels<UserViewModel>()

    private val mApiViewModel by viewModels<ApiUrlViewModel>()

    private val mFilesViewModel by viewModels<FilesViewModel>()
    private val dnFailedViewModel by viewModels<DnFailedViewModel>()
    private val dnViewModel by viewModels<DnViewModel>()
    private val parsingViewModel by viewModels<ParsingViewModel>()


    private  var filIst = ""
    private var filesToProcess = 0
    private val mutex = Mutex()

    private var processingJob: Job? = null

    private val adapter by lazy {
        SavedHistoryListAdapter(this)
    }

    var isDownloadComplete = false
    private val adapterApi by lazy {
        SavedApiAdapter(this)
    }

    private val TAG_RSYC = "ReSyncActivity"

    private val handlerMoveToWebviewPage: Handler by lazy {
        Handler(Looper.getMainLooper())
    }


    private lateinit var customProgressDialog: Dialog
    private lateinit var customSavedDownloadDialog: Dialog
    private lateinit var custom_ApI_Dialog: Dialog


    private var fil_CLO = ""
    private var fil_DEMO = ""
    private var fil_baseUrl = ""
    private var fil_appIndex = ""

    private var getUrlBasedOnSpinnerText = ""
    private var API_Server = "CP 2-Cloud App Server"
    private var CP_server = "CP 1-Cloud App Server"

    private var Minutes = " Minutes"


    private var getTimeDefined_Prime = ""
    private var timeMinuetes22 = 2L
    private var timeMinuetes55 = 5L
    private var timeMinuetes10 = 10L
    private var timeMinuetes15 = 15L
    private var timeMinuetes30 = 30L
    private var timeMinuetes60 = 60L
    private var timeMinuetes120 = 120L
    private var timeMinuetes180 = 180L
    private var timeMinuetes240 = 240L


    var hour = 0
    var min = 0


    private val sharedBiometric: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_BIOMETRIC, Context.MODE_PRIVATE
        )
    }


    private val myDownloadClass: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.MY_DOWNLOADER_CLASS, Context.MODE_PRIVATE
        )
    }


    private val simpleSavedPassword: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SIMPLE_SAVED_PASSWORD, Context.MODE_PRIVATE
        )
    }

    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }


    private var powerManager: PowerManager? = null
    private var wakeLock: PowerManager.WakeLock? = null


    private var isCompltedAndReady = false

    private var isIndexFileAvaliable = false


    var manager: DownloadManager? = null

    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private val sharedTVAPPModePreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_TV_APP_MODE, Context.MODE_PRIVATE
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("SetTextI18n", "WakelockTimeout", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySyncPowellBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyOritenation()

        setUpFullScreenWindows()


        handlerMoveToWebviewPage.postDelayed(Runnable {
            val intent = Intent(applicationContext, WebViewPage::class.java)
            startActivity(intent)
            finish()

        }, Constants.MOVE_BK_WEBVIEW_TIME)



        powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock =
            powerManager!!.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YourApp::MyWakelockTag")
        wakeLock!!.acquire()


        manager = applicationContext.getSystemService(DOWNLOAD_SERVICE) as DownloadManager?

        //add exception
        Methods.addExceptionHandler(this)


        val get_imgToggleImageBackground =
            sharedBiometric.getString(Constants.imgToggleImageBackground, "")
        val get_imageUseBranding = sharedBiometric.getString(Constants.imageUseBranding, "")
        if (get_imgToggleImageBackground.equals(Constants.imgToggleImageBackground) && get_imageUseBranding.equals(
                Constants.imageUseBranding
            )
        ) {
            loadBackGroundImage()
        }




        binding.apply {

            val getSavedCLOImPutFiled = myDownloadClass.getString(Constants.getSavedCLOImPutFiled, "").toString()
            val getSaveSubFolderInPutFiled = myDownloadClass.getString(Constants.getSaveSubFolderInPutFiled, "").toString()
            val getSavedEditTextInputSynUrlZip = myDownloadClass.getString(Constants.getSavedEditTextInputSynUrlZip, "").toString()

            val getSaved_manaul_index_edit_url_Input = myDownloadClass.getString(Constants.getSaved_manaul_index_edit_url_Input, "").toString()

            if (!getSavedCLOImPutFiled.isNullOrEmpty()) {
                editTextCLOpath.setText(getSavedCLOImPutFiled)
            }

            if (!getSaveSubFolderInPutFiled.isNullOrEmpty()) {
                editTextSubPathFolder.setText(getSaveSubFolderInPutFiled)
            }


            if (!getSavedEditTextInputSynUrlZip.isNullOrEmpty()) {
                editTextInputSynUrlZip.setText(getSavedEditTextInputSynUrlZip)
            }

            if (!getSaved_manaul_index_edit_url_Input.isNullOrEmpty()) {
                editTextInputIndexManual.setText(getSaved_manaul_index_edit_url_Input)
            }


            initViewTooggle()


            // clear the download retry count
            val editor = myDownloadClass.edit()
            editor.remove(Constants.RetryCount)
            editor.apply()





            constrainSelectSyncType.setOnClickListener {
                showSelectedSyncType()
            }





            textTestConnectionAPPer.setOnClickListener {

                val editorTVMODE = sharedTVAPPModePreferences.edit()
                editorTVMODE.putString(Constants.installTVModeForFirstTime, Constants.installTVModeForFirstTime)
                editorTVMODE.apply()

                if (handlerMoveToWebviewPage != null) {
                    handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                }

                hideKeyBoard(binding.editTextInputSynUrlZip)
                try {

                    testConnectionSetup()

                } catch (_: Exception) {
                }


            }



            textDownloadZipSyncOrApiSyncNow.setOnClickListener {

                val editorTVMODE = sharedTVAPPModePreferences.edit()
                editorTVMODE.putString(Constants.installTVModeForFirstTime, Constants.installTVModeForFirstTime)
                editorTVMODE.apply()


                if (handlerMoveToWebviewPage != null) {
                    handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                }

                try {

                    lifecycleScope.launch(Dispatchers.IO) {
                        second_cancel_download()
                    }

                    hideKeyBoard(binding.editTextInputSynUrlZip)

                    handler.postDelayed(Runnable {

                        testAndDownLoadZipConnection()

                    }, 300)


                } catch (_: Exception) {
                }


            }

            textLauncheSaveDownload.setOnClickListener {
                val editorTVMODE = sharedTVAPPModePreferences.edit()
                editorTVMODE.putString(Constants.installTVModeForFirstTime, Constants.installTVModeForFirstTime)
                editorTVMODE.apply()

                if (handlerMoveToWebviewPage != null) {
                    handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                }

                showPopForLaunch_Oblin_offline()
            }



            closeBs.setOnClickListener {

                try {
                    val editorTVMODE = sharedTVAPPModePreferences.edit()
                    editorTVMODE.putString(Constants.installTVModeForFirstTime, Constants.installTVModeForFirstTime)
                    editorTVMODE.apply()


                    if (handlerMoveToWebviewPage != null) {
                        handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                    }

                    val getStateNaviagtion =
                        sharedBiometric.getString(Constants.CALL_RE_SYNC_MANGER, "").toString()

                    val get_navigationS2222 =
                        sharedBiometric.getString(Constants.SAVE_NAVIGATION, "").toString()


                    val editor = sharedBiometric.edit()
                    if (getStateNaviagtion.equals(Constants.CALL_RE_SYNC_MANGER)) {
                        editor.remove(Constants.CALL_RE_SYNC_MANGER)
                        editor.apply()
                        val intent = Intent(applicationContext, WebViewPage::class.java)
                        startActivity(intent)
                        finish()


                    } else {

                        if (get_navigationS2222.equals(Constants.SettingsPage)) {
                            val intent = Intent(applicationContext, SettingsActivityKT::class.java)
                            startActivity(intent)
                            finish()
                        } else if (get_navigationS2222.equals(Constants.AdditionNalPage)) {
                            val intent =
                                Intent(applicationContext, AdditionalSettingsActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else if (get_navigationS2222.equals(Constants.WebViewPage)) {
                            val intent = Intent(applicationContext, WebViewPage::class.java)
                            startActivity(intent)
                            finish()
                        }


                    }
                } catch (e: Exception) {
                }

            }


        }


    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun check_for_valid_confileUrl() {

        val sharedLicenseKeys = getSharedPreferences(Constants.SIMPLE_SAVED_PASSWORD, MODE_PRIVATE)

        val get_tMaster = sharedLicenseKeys.getString(Constants.get_editTextMaster, "").toString()
        val get_UserID = sharedLicenseKeys.getString(Constants.get_UserID, "").toString()
        val get_LicenseKey = sharedLicenseKeys.getString(Constants.get_LicenseKey, "").toString()

        val ServerUrl = "$get_tMaster/$get_UserID/$get_LicenseKey/Zip/Config.zip"


        if (isNetworkAvailable()) {
            lifecycleScope.launch {
                try {
                    val result = checkUrlExistence(ServerUrl)
                    if (result) {
                        showConfirmationDialog(get_UserID, get_LicenseKey, ServerUrl)
                    } else {

                        showPopsForMyConnectionTest("", "", Constants.Invalid_Config_Url)
                        if (customProgressDialog != null){
                            customProgressDialog.dismiss()
                        }

                    }
                } catch (e:java.lang.Exception){
                    showToastMessage("Something went wrong")
                }
            }
        } else {
            showToastMessage("No Internet Connection")
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showConfirmationDialog(
        get_UserID: String,
        get_LicenseKey: String,
        ServerUrl: String,
    ) {
        if (customProgressDialog != null){
            customProgressDialog.dismiss()
        }

        val bindingCM: ContinueWithConfigDownloadBinding =
            ContinueWithConfigDownloadBinding.inflate(
                LayoutInflater.from(this)
            )
        val alertDialogBuilder = AlertDialog.Builder(this@ReSyncActivity)

        alertDialogBuilder.setView(bindingCM.root)
        alertDialogBuilder.setCancelable(false)

        val alertDialog = alertDialogBuilder.create()

        // Set the background of the AlertDialog to be transparent
        if (alertDialog.window != null) {
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }




        bindingCM.textCancel.setOnClickListener {

            if (handlerMoveToWebviewPage != null) {
                handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
            }

            runOnUiThread {
                binding.imagSwtichEnableConfigFileOnline.isChecked = false
                isCompltedAndReady = false
                testAndDownLoadZipConnection()
            }

            alertDialog.dismiss()


        }

        bindingCM.textUpdate.setOnClickListener {
            showCustomDialog(get_UserID, get_LicenseKey, ServerUrl)
            alertDialog.dismiss()
        }


        alertDialog.show()


    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showContinueDialog() {

        val bindingCM: FinishWithConfigDownloadBinding = FinishWithConfigDownloadBinding.inflate(
            LayoutInflater.from(this)
        )
        val alertDialogBuilder = AlertDialog.Builder(this@ReSyncActivity)

        alertDialogBuilder.setView(bindingCM.root)
        alertDialogBuilder.setCancelable(false)

        val alertDialog = alertDialogBuilder.create()

        // Set the background of the AlertDialog to be transparent
        if (alertDialog.window != null) {
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }



        bindingCM.textCancel.setOnClickListener {
            alertDialog.dismiss()
        }



        bindingCM.textDownloadSync.setOnClickListener {
            if (handlerMoveToWebviewPage != null) {
                handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
            }

            runOnUiThread {
                isCompltedAndReady = true
                testAndDownLoadZipConnection()
            }
            alertDialog.dismiss()

        }


        alertDialog.show()


    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showCustomDialog(get_UserID: String, get_LicenseKey: String, ServerUrl: String) {
        val bindingCM: SampleProgressConfigLayoutBinding =
            SampleProgressConfigLayoutBinding.inflate(
                LayoutInflater.from(this)
            )
        val alertDialogBuilder = AlertDialog.Builder(this@ReSyncActivity)

        alertDialogBuilder.setView(bindingCM.root)
        alertDialogBuilder.setCancelable(false)

        val alertDialog = alertDialogBuilder.create()

        // Set the background of the AlertDialog to be transparent
        if (alertDialog.window != null) {
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }


        val consMainAlert_sub_layout = bindingCM.consMainAlertSubLayout
        val textLoading = bindingCM.teextDisplaydownload
        val textRetryAPiDn = bindingCM.textCancel


        val preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(
            applicationContext
        )

        if (preferences.getBoolean("darktheme", false)) {
            consMainAlert_sub_layout.setBackgroundResource(R.drawable.card_design_account_number_dark_pop_layout)

            textLoading.setTextColor(resources.getColor(R.color.dark_light_gray_pop))
            textRetryAPiDn.setTextColor(resources.getColor(R.color.dark_light_gray_pop))

            //  textLogoutButton.setBackgroundResource(R.drawable.card_design_darktheme_outline_pop_layout);
            textRetryAPiDn.setBackgroundResource(R.drawable.card_design_buy_gift_card_extra_dark_black)

        }




        bindingCM.textCancel.setOnClickListener {

            if (handlerMoveToWebviewPage != null) {
                handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
            }

            alertDialog.dismiss()
            binding.imagSwtichEnableConfigFileOnline.isChecked = false
            binding.textConfigfileOnline.text = "Use online config"

            lifecycleScope.launch(Dispatchers.IO) {
                cancel_config_download(get_UserID, get_LicenseKey, ServerUrl)
            }

            isDownloadComplete = true


        }


        lifecycleScope.launch(Dispatchers.IO) {
            downloadConfiGFile(get_UserID, get_LicenseKey, ServerUrl, Constants.Config)
        }

        alertDialog.show()

        isDownloadComplete = false // Flag to track download completion

        val handler = Handler()
        Thread {
            try {
                while (!isDownloadComplete) {

                    runOnUiThread {
                        getDownloadStatus(
                            bindingCM.progressBarPref,
                            bindingCM.teextDisplaydownload,
                            get_UserID,
                            get_LicenseKey,
                            ServerUrl,
                            Constants.Config
                        )
                    }



                    Thread.sleep(1000)
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } finally {
                handler.post {
                    alertDialog.dismiss()
                }
            }
        }.start()


    }


    private fun cancel_config_download(
        get_UserID: String,
        get_LicenseKey: String,
        fileName: String,
    ) {
        try {

            val download_ref: Long = myDownloadClass.getLong(Constants.downloadKey, -15)

            val folderName = Constants.Config
            val Syn2AppLive = Constants.Syn2AppLive

            val DeleteFolderPath = "/$Syn2AppLive/$get_UserID/$get_LicenseKey/$folderName/$fileName"

            val directoryPath = Environment.getExternalStorageDirectory().absolutePath + "/Download/$DeleteFolderPath"
            val file = File(directoryPath)
            delete(file)


            if (download_ref != -15L) {
                val query = DownloadManager.Query()
                query.setFilterById(download_ref)
                val c =
                    (applicationContext.getSystemService(DOWNLOAD_SERVICE) as DownloadManager).query(
                        query
                    )
                if (c.moveToFirst()) {
                    manager!!.remove(download_ref)
                    val editor: SharedPreferences.Editor = myDownloadClass.edit()
                    editor.remove(Constants.downloadKey)
                    editor.apply()
                }

            }

        } catch (ignored: java.lang.Exception) {
        }
    }


    private fun downloadConfiGFile(
        get_UserID: String,
        get_LicenseKey: String,
        url: String,
        fileName: String,
    ) {
        val Syn2AppLive = Constants.Syn2AppLive
        val baseDir = getExternalFilesDir(null) // App-private external files
        val relativePath = "$Syn2AppLive/$get_UserID/$get_LicenseKey"
        val targetDir = File(baseDir, relativePath)

        // Ensure directory exists
        if (!targetDir.exists()) targetDir.mkdirs()

        val targetFile = File(targetDir, fileName)

        // Delete existing
        delete(targetFile)

        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle(fileName)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationUri(Uri.fromFile(targetFile)) // ✅ Safe destination
        }

        val managerDownload = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val downloadReferenceMain = managerDownload.enqueue(request)

        myDownloadClass.edit().apply {
            putLong(Constants.downloadKey, downloadReferenceMain)
            apply()
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("SetTextI18n", "Range")
    fun getDownloadStatus(
        progressBarPref: ProgressBar,
        textprogressPercentage: TextView,
        get_UserID: String,
        get_LicenseKey: String,
        url: String,
        fileName: String,
    ) {
        try {

            val download_ref = myDownloadClass.getLong(Constants.downloadKey, -15)
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
                progressBarPref.progress = dl_progress


                // Calculate the percentage of completion
                val progressPercentage =
                    (bytes_downloaded.toFloat() / bytes_total.toFloat() * 100).toInt()
                textprogressPercentage.text =
                    "" + progressPercentage.toString() + "%  Zip Downloaded"


                if (c.moveToFirst()) {
                    val status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {

                        runOnUiThread {
                            isDownloadComplete = true
                            funUnZipFile(get_UserID, get_LicenseKey, url, fileName)
                        }
                    }

                }

            }
        } catch (ignored: java.lang.Exception) {
        }
    }

    private fun funUnZipFile(
        get_UserID: String,
        get_LicenseKey: String,
        url: String,
        fileName: String,
    ) {
        try {
            showCustomProgressDialog("Please wait for files to unpack")

            lifecycleScope.launch(Dispatchers.IO) {
                val Syn2AppLive = Constants.Syn2AppLive

                val finalFolderPath = "$Syn2AppLive/$get_UserID/$get_LicenseKey"
                val get_Clo = simpleSavedPassword.getString(Constants.get_UserID, "").toString()
                val get_DEmo = simpleSavedPassword.getString(Constants.get_LicenseKey, "").toString()

                val newConfigFolder = "$Syn2AppLive/$get_Clo/$get_DEmo/App"

                val baseDir = getExternalFilesDir(null) ?: return@launch

                val zipFile = File(File(baseDir, finalFolderPath), fileName)
                val destinationFolder = File(baseDir, newConfigFolder)

                if (!destinationFolder.exists()) {
                    destinationFolder.mkdirs()
                }

                if (zipFile.exists()) {
                    extractZip(zipFile.absolutePath, destinationFolder.absolutePath)
                } else {
                    withContext(Dispatchers.Main) {
                        showToastMessage("Zip file could not be found")
                    }
                }
            }
        } catch (_: Exception) {
        }
    }


    suspend fun extractZip(zipFilePath: String, destinationPath: String) {
        try {
            withContext(Dispatchers.Main) {
                showToastMessage(Constants.Extracting)
            }

            val buffer = ByteArray(1024)

            val zipInputStream = ZipInputStream(withContext(Dispatchers.IO) {
                FileInputStream(zipFilePath)
            })
            var entry = zipInputStream.nextEntry

            while (entry != null) {
                val entryFile = File(destinationPath, entry.name)
                val entryDir = entryFile.parent?.let { File(it) }

                if (!entryDir?.exists()!!) {
                    entryDir.mkdirs()
                }

                val outputStream = entryFile.outputStream()

                var len = withContext(Dispatchers.IO) {
                    zipInputStream.read(buffer)
                }
                while (len > 0) {
                    withContext(Dispatchers.IO) {
                        outputStream.write(buffer, 0, len)
                    }
                    len = withContext(Dispatchers.IO) {
                        zipInputStream.read(buffer)
                    }
                }

                outputStream.close()
                zipInputStream.closeEntry()
                entry = zipInputStream.nextEntry


                // Notify MediaScanner about the extracted file
                MediaScannerConnection.scanFile(applicationContext,
                    arrayOf(entryFile.absolutePath),
                    null,
                    object : MediaScannerConnection.OnScanCompletedListener {
                        override fun onScanCompleted(path: String?, uri: Uri?) {
                        }
                    })

            }

            zipInputStream.close()

            withContext(Dispatchers.Main) {
                stratMyACtivity()
                showToastMessage(Constants.media_ready)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                showToastMessage(Constants.Error_during_zip_extraction)
                stratMyACtivity()
            }
        }
    }


    private fun stratMyACtivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            runOnUiThread {
                handler.postDelayed(Runnable {
                    showContinueDialog()
                    customProgressDialog.cancel()
                }, 1000)
            }
        }


    }


    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {

        try {
            val editorTVMODE = sharedTVAPPModePreferences.edit()
            editorTVMODE.putString(
                Constants.installTVModeForFirstTime, Constants.installTVModeForFirstTime
            )
            editorTVMODE.apply()

            val getStateNaviagtion =
                sharedBiometric.getString(Constants.CALL_RE_SYNC_MANGER, "").toString()
            val get_navigationS2222 =
                sharedBiometric.getString(Constants.SAVE_NAVIGATION, "").toString()

            val editor = sharedBiometric.edit()
            if (getStateNaviagtion.equals(Constants.CALL_RE_SYNC_MANGER)) {
                editor.remove(Constants.CALL_RE_SYNC_MANGER)
                editor.apply()
                val intent = Intent(applicationContext, WebViewPage::class.java)
                startActivity(intent)
                finish()
            } else {

                if (get_navigationS2222.equals(Constants.SettingsPage)) {
                    val intent = Intent(applicationContext, SettingsActivityKT::class.java)
                    startActivity(intent)
                    finish()
                } else if (get_navigationS2222.equals(Constants.AdditionNalPage)) {
                    val intent = Intent(applicationContext, AdditionalSettingsActivity::class.java)
                    startActivity(intent)
                    finish()
                } else if (get_navigationS2222.equals(Constants.WebViewPage)) {
                    val intent = Intent(applicationContext, WebViewPage::class.java)
                    startActivity(intent)
                    finish()
                }


            }
        } catch (e: Exception) {
        }


    }

    override fun onResume() {
        super.onResume()

        try {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

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

        } catch (ignored: java.lang.Exception) {
        }


    }

    override fun onDestroy() {
        super.onDestroy()

        try {

            if (handlerMoveToWebviewPage != null) {
                handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
            }

            if (wakeLock != null && wakeLock!!.isHeld) {
                wakeLock!!.release()
            }

        } catch (_: Exception) {
        }
    }


    private fun showCustomProgressDialog(message: String) {
        try {
            customProgressDialog = Dialog(this)
            val binding = ProgressDialogLayoutBinding.inflate(LayoutInflater.from(this))
            customProgressDialog.setContentView(binding.root)
            customProgressDialog.setCancelable(true)
            customProgressDialog.setCanceledOnTouchOutside(false)
            customProgressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            binding.textLoading.text = message


            val consMainAlert_sub_layout = binding.consMainAlertSubLayout
            val textLoading = binding.textLoading
            val imgCloseDialog = binding.imgCloseDialog
            val imagSucessful = binding.imagSucessful
            val progressBar2 = binding.progressBar2



            if (preferences.getBoolean("darktheme", false)) {
                consMainAlert_sub_layout.setBackgroundResource(R.drawable.card_design_account_number_dark_pop_layout)

                textLoading.setTextColor(resources.getColor(R.color.dark_light_gray_pop))

                val drawable_imgCloseDialog =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_close_24)
                drawable_imgCloseDialog?.setColorFilter(
                    ContextCompat.getColor(
                        applicationContext, R.color.white
                    ), PorterDuff.Mode.SRC_IN
                )
                imgCloseDialog.setImageDrawable(drawable_imgCloseDialog)

                val drawable_imagSucessfulg =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_download_24)
                drawable_imagSucessfulg?.setColorFilter(
                    ContextCompat.getColor(
                        applicationContext, R.color.white
                    ), PorterDuff.Mode.SRC_IN
                )
                imagSucessful.setImageDrawable(drawable_imagSucessfulg)

                val colorWhite = ContextCompat.getColor(applicationContext, R.color.white)
                progressBar2.indeterminateDrawable.setColorFilter(
                    colorWhite, PorterDuff.Mode.SRC_IN
                )


            }


            binding.imgCloseDialog.setOnClickListener {
                if (handlerMoveToWebviewPage != null) {
                    handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                }

                customProgressDialog.cancel()
            }

            customProgressDialog.show()
        } catch (_: Exception) {
        }
    }


    private fun testConnectionSetup() {
        binding.apply {
            val getFolderClo = editTextCLOpath.text.toString().trim()
            val getFolderSubpath = editTextSubPathFolder.text.toString().trim()

            val editor = myDownloadClass.edit()
            if (isNetworkAvailable()) {
                if (!imagSwtichEnableManualOrNot.isChecked) {

                    if (imagSwtichPartnerUrl.isChecked) {

                        if (getUrlBasedOnSpinnerText.isNotEmpty()) {


                            when (getUrlBasedOnSpinnerText) {
                                CP_server -> {
                                    if (getFolderClo.isNotEmpty() && getFolderSubpath.isNotEmpty()) {
                                        // update server Json Url
                                        saveTheInputPaths(getFolderClo, getFolderSubpath)

                                    } else {
                                        editTextCLOpath.error = "Input a valid path e.g CLO"
                                        editTextSubPathFolder.error = "Input a valid path e.g DE_MO_2021000"
                                        showToastMessage("Fields can not be empty")
                                    }
                                }

                                API_Server -> {

                                    // update server Json Url
                                    saveTheInputPaths(getFolderClo, getFolderSubpath)

                                }

                            }

                        } else {
                            showToastMessage("Select Partner Url")
                        }

                    } else {

                        // let consider Custom Domain was selected

                        val getFolderClo222 = binding.editTextCLOpath.text.toString().trim()
                        val getFolderSubpath22 = binding.editTextSubPathFolder.text.toString().trim()


                        var Saved_Domains_Urls = myDownloadClass.getString(Constants.Saved_Domains_Urls, "").toString()


                        if (Saved_Domains_Urls.isNotEmpty()) {

                            if (getFolderClo222.isNotEmpty() && getFolderSubpath22.isNotEmpty()) {
                                testConnectionSetup_API_Test(getFolderClo222, getFolderSubpath22)
                                editor.putString(Constants.getSavedCLOImPutFiled, getFolderClo222)
                                editor.putString(Constants.getSaveSubFolderInPutFiled, getFolderSubpath22)
                                editor.apply()

                            } else {
                                editTextCLOpath.error = "Input a valid path e.g CLO"
                                editTextSubPathFolder.error = "Input a valid path e.g DE_MO_2021000"
                                showToastMessage("Fields can not be empty")
                            }


                        } else {
                            showToastMessage("Select Custom Domain")
                        }

                    }

                } else {

                    /// when the button is checked
                    val editInputUrl = editTextInputSynUrlZip.text.toString().trim()
                    // to luanch a live url manual
                    val editInputAppIndex = editTextInputIndexManual.text.toString().trim()

                    if (isUrlValid(editInputUrl) && isUrlValid(editInputAppIndex)) {
                        httpNetSingleUrlTest(editInputUrl, editInputAppIndex)
                        editor.putString(Constants.getSavedEditTextInputSynUrlZip, editInputUrl)
                        editor.putString(Constants.getSaved_manaul_index_edit_url_Input, editInputAppIndex)
                        editor.apply()

                    } else {

                        if (!isUrlValid(editInputUrl)) {
                            binding.editTextInputSynUrlZip.error = "Invalid url format"
                        }

                        if (!isUrlValid(editInputAppIndex)) {
                            binding.editTextInputIndexManual.error = "Invalid url format"
                        }

                        showToastMessage("Invalid url format")
                    }


                }

            } else {
                showToastMessage("No Internet Connection")
            }


        }
    }


    private fun saveTheInputPaths(getFolderClo:String, getFolderSubpath:String){
        httpNetworkTester(getFolderClo, getFolderSubpath)

         val editor = myDownloadClass.edit()
        editor.putString(Constants.getSavedCLOImPutFiled, getFolderClo)
        editor.putString(Constants.getSaveSubFolderInPutFiled, getFolderSubpath)
        editor.apply()

    }



    private fun httpNetworkTester(getFolderClo: String, getFolderSubpath: String) {
        handler.postDelayed(Runnable {
            showCustomProgressDialog("Testing connection")
            val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
            val CP_AP_MASTER_DOMAIN = myDownloadClass.getString(Constants.CP_OR_AP_MASTER_DOMAIN, "").toString()

            val getSyncMethods = sharedBiometric.getString(Constants.IMG_SELECTED_SYNC_METHOD, "").toString()

            if (getSyncMethods == Constants.USE_ZIP_SYNC) {
                val baseUrl = "${CP_AP_MASTER_DOMAIN}/$getFolderClo/$getFolderSubpath/Zip/App.zip"
                lifecycleScope.launch {
                    try {
                        val result = checkUrlExistence(baseUrl)
                        if (result) {
                            showPopsForMyConnectionTest(
                                getFolderClo, getFolderSubpath, "Successful"
                            )

                            // save also to room data base
                            val user = User(
                                CLO = getFolderClo,
                                DEMO = getFolderSubpath,
                                EditUrl = "",
                                EditUrlIndex = ""
                            )
                            mUserViewModel.addUser(user)

                            if (customProgressDialog != null){
                                customProgressDialog.dismiss()
                            }

                            savePathServerUrl(CP_AP_MASTER_DOMAIN, getFolderClo, getFolderSubpath)

                        } else {
                            showPopsForMyConnectionTest(
                                getFolderClo, getFolderSubpath, "Failed!"
                            )
                            if (customProgressDialog != null){
                                customProgressDialog.dismiss()
                            }
                        }
                    } finally {
                        //   customProgressDialog.dismiss()

                    }
                }

            } else if (getSyncMethods == Constants.USE_API_SYNC) {


                val baseUrl = "${CP_AP_MASTER_DOMAIN}/$getFolderClo/$getFolderSubpath/App/index.html"

                lifecycleScope.launch {
                    try {
                        val result = checkUrlExistence(baseUrl)
                        if (result) {
                            showPopsForMyConnectionTest(
                                getFolderClo, getFolderSubpath, "Successful"
                            )

                            val user = User(
                                CLO = getFolderClo,
                                DEMO = getFolderSubpath,
                                EditUrl = "",
                                EditUrlIndex = ""
                            )
                            mUserViewModel.addUser(user)

                            if (customProgressDialog != null){
                                customProgressDialog.dismiss()
                            }
                            savePathServerUrl(CP_AP_MASTER_DOMAIN, getFolderClo, getFolderSubpath)

                        } else {
                            showPopsForMyConnectionTest(
                                getFolderClo, getFolderSubpath, "Failed!"
                            )

                            if (customProgressDialog != null){
                                customProgressDialog.dismiss()
                            }


                        }
                    } catch (e:Exception){

                        showPopsForMyConnectionTest(
                            getFolderClo, getFolderSubpath, "Failed!"
                        )

                        if (customProgressDialog != null){
                            customProgressDialog.dismiss()
                        }

                    }
                }


            }else if (getSyncMethods == Constants.USE_PARSING_SYNC) {


                val baseUrl = "${CP_AP_MASTER_DOMAIN}/$getFolderClo/$getFolderSubpath/App/index.html"
                lifecycleScope.launch {
                    try {
                        val result = checkUrlExistence(baseUrl)
                        if (result) {

                            showPopsForMyConnectionTest(
                                getFolderClo, getFolderSubpath, "Successful"
                            )

                            // save also to room data base
                            val user = User(
                                CLO = getFolderClo,
                                DEMO = getFolderSubpath,
                                EditUrl = "",
                                EditUrlIndex = ""
                            )
                            mUserViewModel.addUser(user)


                            if (customProgressDialog != null){
                                customProgressDialog.dismiss()
                            }
                            savePathServerUrl(CP_AP_MASTER_DOMAIN, getFolderClo, getFolderSubpath)

                        } else {
                            showPopsForMyConnectionTest(
                                getFolderClo, getFolderSubpath, "Failed!"
                            )

                            if (customProgressDialog != null){
                                customProgressDialog.dismiss()
                            }
                        }


                    } catch (e:Exception){

                        showPopsForMyConnectionTest(
                            getFolderClo, getFolderSubpath, "Failed!"
                        )

                        if (customProgressDialog != null){
                            customProgressDialog.dismiss()
                        }

                    }
                }

                // newly added by 10:33 am
            }



        }, 300)
    }

    private fun savePathServerUrl(cpApMasterDomain: String, folderClo: String, folderSubpath: String) {
        val baseUrlSever = "$cpApMasterDomain/$folderClo/$folderSubpath/App/Config/appConfig.json"
        val editorValue = simpleSavedPassword.edit()
        editorValue.putString(Constants.get_masterDomain, baseUrlSever)
        editorValue.apply()

    }


    private fun httpNetSingleUrlTest(editInputUrl: String, editInputAppIndex: String) {
        handler.postDelayed(Runnable {
            showCustomProgressDialog("Testing connection")

            val lastString = editInputUrl.substringAfterLast("/")
            val fileNameWithoutExtension = lastString.substringBeforeLast(".")

            lifecycleScope.launch {
                try {
                    val result = checkUrlExistence(editInputUrl)
                    if (result) {
                        showPopsForMyConnectionTest(
                            "CLO", fileNameWithoutExtension, "Successful"
                        )

                        val user = User(
                            CLO = "",
                            DEMO = "",
                            EditUrl = editInputUrl,
                            EditUrlIndex = editInputAppIndex
                        )
                        mUserViewModel.addUser(user)
                        customProgressDialog.dismiss()

                    } else {

                        showPopsForMyConnectionTest("CLO", fileNameWithoutExtension, "Failed!")
                        customProgressDialog.dismiss()
                    }
                } finally {
                    //  customProgressDialog.dismiss()
                }
            }

        }, 300)
    }


    private fun initViewTooggle() {
        binding.apply {

            //for history
            constraintLayoutSavedDwonlaod.setOnClickListener {
                showSaveduserHistory()
                hideKeyBoard(binding.editTextInputSynUrlZip)
            }


            // for server
            //  getUrlBasedOnSpinnerText = CP_server   // it was hard coded before, but now saved
            constraintLayout4.setOnClickListener {
                hideKeyBoard(binding.editTextInputSynUrlZip)

                if (imagSwtichPartnerUrl.isChecked) {
                    serVerOptionDialog()
                } else {

                    val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
                    val getFolderClo = myDownloadClass.getString(Constants.getFolderClo, "").toString()
                    val getFolderSubpath = myDownloadClass.getString(Constants.getFolderSubpath, "").toString()
                    if (getFolderClo.isNotEmpty() && getFolderSubpath.isNotEmpty()){
                        show_API_Urls()
                    }else{
                        showInfoAlertDialog()
                    }

                }

            }


            //// inilaizing the Set Syn Timmer
            // for time intervals
            // getTimeDefined = timeMinuetesDefined

            initSyncTimmer()



            textIntervalsSelect.setOnClickListener {
                definedTimeIntervals()
            }


            textView12.setOnClickListener {
                //  setUpCustomeTimmer()
                showSyncDialog()
            }


            val imgLunchOnline = sharedBiometric.getString(Constants.imgAllowLunchFromOnline, "")
            imagSwtichEnableLaucngOnline.isChecked =
                imgLunchOnline.equals(Constants.imgAllowLunchFromOnline)

            val editor333 = myDownloadClass.edit()

            if (imgLunchOnline.equals(Constants.imgAllowLunchFromOnline)) {
                textLunchOnline.text = "Launch online"
                editor333.putString(Constants.Tapped_OnlineORoffline, Constants.tapped_launchOnline)
                editor333.apply()


            } else {

                textLunchOnline.text = "Launch offline"
                editor333.putString(
                    Constants.Tapped_OnlineORoffline, Constants.tapped_launchOffline
                )
                editor333.apply()
            }

            imagSwtichEnableLaucngOnline.setOnCheckedChangeListener { compoundButton, isValued -> // we are putting the values into SHARED PREFERENCE
                try {
                    if (handlerMoveToWebviewPage != null) {
                        handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                    }

                    val editor = sharedBiometric.edit()
                    hideKeyBoard(binding.editTextInputSynUrlZip)
                    if (compoundButton.isChecked) {

                        editor.putString(
                            Constants.imgAllowLunchFromOnline, "imgAllowLunchFromOnline"
                        )
                        editor.apply()

                        textLunchOnline.text = "Launch online"

                        editor333.putString(
                            Constants.Tapped_OnlineORoffline, Constants.tapped_launchOnline
                        )
                        editor333.apply()

                    } else {

                        editor.remove("imgAllowLunchFromOnline")
                        editor.apply()

                        textLunchOnline.text = "Launch offline"

                        editor333.putString(
                            Constants.Tapped_OnlineORoffline, Constants.tapped_launchOffline
                        )
                        editor333.apply()

                    }

                } catch (e: Exception) {
                    Log.d(TAG_RSYC, "initViewTooggle: ${e.message.toString()}")
                }


            }


            /// use first Sync  or Do not use First Sync

            // enable satrt file for first synct

            val startFileFirstSync =
                sharedBiometric.getString(Constants.imgStartFileFirstSync, "").toString()
            imgStartFileFirstSync.isChecked =
                startFileFirstSync.equals(Constants.imgStartFileFirstSync)


            if (startFileFirstSync.equals(Constants.imgStartFileFirstSync)) {
                textUseStartFile.text = "Use start file on"
            } else {
                textUseStartFile.text = "Use start file off"
            }


            imgStartFileFirstSync.setOnCheckedChangeListener { compoundButton, isValued -> // we are putting the values into SHARED PREFERENCE

                try {
                    if (handlerMoveToWebviewPage != null) {
                        handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                    }

                    val editor = sharedBiometric.edit()
                    if (compoundButton.isChecked) {
                        editor.putString(
                            Constants.imgStartFileFirstSync, Constants.imgStartFileFirstSync
                        )
                        editor.apply()
                        textUseStartFile.text = "Use start file on"

                    } else {

                        editor.remove(Constants.imgStartFileFirstSync)
                        editor.apply()
                        textUseStartFile.text = "Use start file off"

                    }
                } catch (e: Exception) {
                    Log.d(TAG_RSYC, "initViewTooggle: ${e.message.toString()}")
                }
            }


            // set up toggle for index file change
            // set up toggle for index file change
            // set up toggle for index file change

            val get_imagSwtichUseIndexCahngeOrTimeStamp =
                sharedBiometric.getString(Constants.imagSwtichUseIndexCahngeOrTimeStamp, "")
                    .toString()
            imagSwtichUseIndexCahngeOrTimeStamp.isChecked =
                get_imagSwtichUseIndexCahngeOrTimeStamp.equals(Constants.imagSwtichUseIndexCahngeOrTimeStamp)


            if (get_imagSwtichUseIndexCahngeOrTimeStamp.equals(Constants.imagSwtichUseIndexCahngeOrTimeStamp)) {
                textUseindexChangeOrTimestamp.text = "Use Index Change"
            } else {
                textUseindexChangeOrTimestamp.text = "Use Time Stamp"
            }



            imagSwtichUseIndexCahngeOrTimeStamp.setOnCheckedChangeListener { compoundButton, isValued ->
                try {

                    val editor = sharedBiometric.edit()
                    if (compoundButton.isChecked) {
                        editor.putString(
                            Constants.imagSwtichUseIndexCahngeOrTimeStamp,
                            Constants.imagSwtichUseIndexCahngeOrTimeStamp
                        )
                        editor.apply()
                        textUseindexChangeOrTimestamp.text = "Use Index Change"

                    } else {

                        if (handlerMoveToWebviewPage != null) {
                            handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                        }

                        editor.remove(Constants.imagSwtichUseIndexCahngeOrTimeStamp)
                        editor.apply()
                        textUseindexChangeOrTimestamp.text = "Use Time Stamp"

                        if (handlerMoveToWebviewPage != null) {
                            handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                        }
                        hideKeyBoard(binding.editTextInputSynUrlZip)
                        val editor = sharedBiometric.edit()
                        if (compoundButton.isChecked) {
                            editor.putString(
                                Constants.imagSwtichPartnerUrl, Constants.imagSwtichPartnerUrl
                            )
                            editor.apply()
                            textPartnerUrlLunch.text = "Select Partner Url"


                            val Saved_Parthner_Name111 =
                                myDownloadClass.getString(Constants.Saved_Parthner_Name, "")
                            if (Saved_Parthner_Name111!!.isNotEmpty()) {
                                texturlsViews.text = Saved_Parthner_Name111
                                getUrlBasedOnSpinnerText = Saved_Parthner_Name111

                            } else {
                                texturlsViews.text = "Select Partner Url"
                            }


                        } else {

                            editor.remove(Constants.imagSwtichPartnerUrl)
                            editor.apply()
                            textPartnerUrlLunch.text = "Select Custom Domain"


                            val get_Saved_Domains_Name111 =
                                myDownloadClass.getString(Constants.Saved_Domains_Name, "")
                            if (get_Saved_Domains_Name111!!.isNotEmpty()) {
                                texturlsViews.text = get_Saved_Domains_Name111

                            } else {
                                texturlsViews.text = "Select Custom Domain"
                            }

                        }

                    }
                } catch (_: Exception) {
                }
            }

            // set the toggle by default

            handler.postDelayed(Runnable {

                val editor = sharedBiometric.edit()
                val get_check_if_index_chnage_is_enabled =
                    sharedBiometric.getString(Constants.check_if_index_chnage_is_enabled, "")
                        .toString()

                if (get_check_if_index_chnage_is_enabled.isNullOrEmpty()) {
                    imagSwtichUseIndexCahngeOrTimeStamp.isChecked = true
                    editor.putString(
                        Constants.imagSwtichUseIndexCahngeOrTimeStamp,
                        Constants.imagSwtichUseIndexCahngeOrTimeStamp
                    )
                    editor.putString(
                        Constants.check_if_index_chnage_is_enabled,
                        Constants.check_if_index_chnage_is_enabled
                    )
                    editor.apply()
                    textUseindexChangeOrTimestamp.text = "Use Index Change"

                }


            }, 300)


            // enable config
            // enable config
            // enable config

            val imgLCongigFile = sharedBiometric.getString(Constants.imagSwtichEnableConfigFileOnline, "").toString()
            imagSwtichEnableConfigFileOnline.isChecked = imgLCongigFile.equals(Constants.imagSwtichEnableConfigFileOnline)

            if (imgLCongigFile.equals(Constants.imagSwtichEnableConfigFileOnline)) {
                textConfigfileOnline.text = "Config File Offline"
            } else {
                textConfigfileOnline.text = "Config File Online"
            }


            imagSwtichEnableConfigFileOnline.setOnCheckedChangeListener { compoundButton, isValued ->
                try {
                    if (handlerMoveToWebviewPage != null) {
                        handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                    }


                    val editor = sharedBiometric.edit()
                    if (compoundButton.isChecked) {
                        editor.putString(
                            Constants.imagSwtichEnableConfigFileOnline,
                            "imagSwtichEnableConfigFileOnline"
                        )
                        editor.apply()
                        textConfigfileOnline.text = "Config File Offline"
                    } else {

                        editor.remove("imagSwtichEnableConfigFileOnline")
                        editor.apply()

                        textConfigfileOnline.text = "Config File Online"
                    }
                } catch (_: Exception) {
                }
            }


            // enable Sync on File Change

            val imgEnableFileOnSyncChange =
                sharedBiometric.getString(Constants.imagSwtichEnableSyncOnFilecahnge, "").toString()
            imagSwtichEnableSyncOnFilecahnge.isChecked =
                imgEnableFileOnSyncChange.equals(Constants.imagSwtichEnableSyncOnFilecahnge)


            if (imgEnableFileOnSyncChange.equals(Constants.imagSwtichEnableSyncOnFilecahnge)) {
                textSyncOnFileChangeIntervals.text = "Download on Intervals"
            } else {
                textSyncOnFileChangeIntervals.text = "Download on change"

            }





            imagSwtichEnableSyncOnFilecahnge.setOnCheckedChangeListener { compoundButton, isValued ->

                try {
                    if (handlerMoveToWebviewPage != null) {
                        handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                    }

                    val editor = sharedBiometric.edit()
                    val editorDN = myDownloadClass.edit()
                    editorDN.remove(Constants.SynC_Status)
                    editorDN.remove(Constants.textDownladByes)
                    editorDN.remove(Constants.progressBarPref)
                    editorDN.remove(Constants.progressBarPref)
                    editorDN.remove(Constants.filesChange)
                    editorDN.remove(Constants.numberOfFiles)

                    editorDN.apply()



                    if (compoundButton.isChecked) {
                        editor.putString(
                            Constants.imagSwtichEnableSyncOnFilecahnge,
                            Constants.imagSwtichEnableSyncOnFilecahnge
                        )

                        // editor.putString(Constants.showDownloadSyncStatus, "showDownloadSyncStatus")

                        editor.apply()
                        textSyncOnFileChangeIntervals.text = "Download on Intervals"


                        // if the user switch to use sync on change, then we need to manage with Api or Url Zip

                    } else {
                        // if the user switch to use sync on change, then we need to manage with Api or Url Zip

                        val editor22 = sharedBiometric.edit()
                        editor22.remove(Constants.imagSwtichEnableSyncOnFilecahnge)
                        editor22.apply()

                        textSyncOnFileChangeIntervals.text = "Download on change"


                    }
                } catch (_: Exception) {
                }
            }


            // enable Toggle Mode

            val imgEnableToggleMode =
                sharedBiometric.getString(Constants.imagSwtichEnablEnableToggleOrNot, "").toString()
            imagSwtichEnablEnableToggleOrNot.isChecked =
                imgEnableToggleMode.equals(Constants.imagSwtichEnablEnableToggleOrNot)

            if (imgEnableToggleMode.equals(Constants.imagSwtichEnablEnableToggleOrNot)) {
                textToogleMode.text = "Disable Test Toggle Mode"
            } else {
                textToogleMode.text = "Enable Test Toggle Mode"
            }

            imagSwtichEnablEnableToggleOrNot.setOnCheckedChangeListener { compoundButton, isValued ->

                try {
                    if (handlerMoveToWebviewPage != null) {
                        handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                    }
                    val editor = sharedBiometric.edit()
                    if (compoundButton.isChecked) {
                        editor.putString(
                            Constants.imagSwtichEnablEnableToggleOrNot,
                            Constants.imagSwtichEnablEnableToggleOrNot
                        )
                        editor.apply()
                        textToogleMode.text = "Disable Test Toggle Mode"
                    } else {

                        editor.remove(Constants.imagSwtichEnablEnableToggleOrNot)
                        editor.apply()

                        textToogleMode.text = "Enable Test Toggle Mode"
                    }
                } catch (e: Exception) {
                }
            }






            funManulOrNotInteView()

            imageEnablePartherOrmasterDomain()

            initToggleUseZipSyncOrApI()

        }


    }


    private fun showInfoAlertDialog(){
        AlertDialog.Builder(this)
            .setTitle("Location Error")
            .setMessage("Op's!.. System needs to get absolute Location, try toggling to Partner URL.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


    private fun initToggleUseZipSyncOrApI() {
        binding.apply {
            val getSyncMethods = sharedBiometric.getString(Constants.IMG_SELECTED_SYNC_METHOD, "").toString()

                if (getSyncMethods == Constants.USE_ZIP_SYNC) {

                    editTextInputSynUrlZip.hint = "Input url  ZIP Sync"
                    textDownloadZipSyncOrApiSyncNow.text = "Connect ZIP Sync"
                    textDisplaySelectedSyncType.text = "Use ZIP Sync"

                    textDownloadZipSyncOrApiSyncNow.setBackgroundResource(R.drawable.card_design_buy_gift_card)
                    textDownloadZipSyncOrApiSyncNow.setTextColor(
                        ContextCompat.getColor(
                            applicationContext, R.color.white
                        )
                    )


                } else if (getSyncMethods == Constants.USE_API_SYNC) {

                    textDownloadZipSyncOrApiSyncNow.text = "Connect API Sync"
                    editTextInputSynUrlZip.hint = "Input url  API Sync"

                    textDisplaySelectedSyncType.text = "Use API Sync"

                    textDownloadZipSyncOrApiSyncNow.setBackgroundResource(R.drawable.light_background_blue_color)
                    textDownloadZipSyncOrApiSyncNow.setTextColor(
                        ContextCompat.getColor(
                            applicationContext, R.color.deep_blue
                        )
                    )


                } else if (getSyncMethods == Constants.USE_PARSING_SYNC) {


                    textDownloadZipSyncOrApiSyncNow.text = "Connect Parsing Sync"
                    editTextInputSynUrlZip.hint = "Input url  Parsing Sync"
                    textDisplaySelectedSyncType.text = "Use Parsing Sync"

                    textDownloadZipSyncOrApiSyncNow.setBackgroundResource(R.drawable.light_background_green_color)
                    textDownloadZipSyncOrApiSyncNow.setTextColor(
                        ContextCompat.getColor(
                            applicationContext, R.color.white
                        )
                    )


                } else if (getSyncMethods == Constants.USE_DRIVE_SYNC) {


                    textDownloadZipSyncOrApiSyncNow.text = "Connect Drive Sync"
                    editTextInputSynUrlZip.hint = "Input url  Drive Sync"
                    textDisplaySelectedSyncType.text = "Use Drive Sync"

                    textDownloadZipSyncOrApiSyncNow.setBackgroundResource(R.drawable.light_background_red_color)
                    textDownloadZipSyncOrApiSyncNow.setTextColor(
                        ContextCompat.getColor(
                            applicationContext, R.color.white
                        )
                    )


                }


            }

    }

    private fun imageEnablePartherOrmasterDomain() {

        binding.apply {
            //// logic for Select Partner Url

            val imagPartnerurl =
                sharedBiometric.getString(Constants.imagSwtichPartnerUrl, "").toString()
            imagSwtichPartnerUrl.isChecked = imagPartnerurl.equals(Constants.imagSwtichPartnerUrl)

            val get_Saved_Domains_Name =
                myDownloadClass.getString(Constants.Saved_Domains_Name, "").toString()
            val Saved_Parthner_Name =
                myDownloadClass.getString(Constants.Saved_Parthner_Name, "").toString()

            if (imagPartnerurl.equals(Constants.imagSwtichPartnerUrl)) {
                textPartnerUrlLunch.text = "Select Partner Url"


                if (Saved_Parthner_Name.isNotEmpty()) {
                    texturlsViews.text = Saved_Parthner_Name
                    getUrlBasedOnSpinnerText = Saved_Parthner_Name

                } else {
                    texturlsViews.text = "Select Partner Url"
                }


            } else {
                textPartnerUrlLunch.text = "Select Custom Domain"



                if (get_Saved_Domains_Name.isNotEmpty()) {
                    texturlsViews.text = get_Saved_Domains_Name

                } else {
                    texturlsViews.text = "Select Custom Domain"
                }


            }



            imagSwtichPartnerUrl.setOnCheckedChangeListener { compoundButton, isValued ->

                try {
                    if (handlerMoveToWebviewPage != null) {
                        handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                    }
                    hideKeyBoard(binding.editTextInputSynUrlZip)
                    val editor = sharedBiometric.edit()
                    if (compoundButton.isChecked) {
                        editor.putString(
                            Constants.imagSwtichPartnerUrl, Constants.imagSwtichPartnerUrl
                        )
                        editor.apply()
                        textPartnerUrlLunch.text = "Select Partner Url"


                        val Saved_Parthner_Name111 =
                            myDownloadClass.getString(Constants.Saved_Parthner_Name, "")
                        if (Saved_Parthner_Name111!!.isNotEmpty()) {
                            texturlsViews.text = Saved_Parthner_Name111
                            getUrlBasedOnSpinnerText = Saved_Parthner_Name111

                        } else {
                            texturlsViews.text = "Select Partner Url"
                        }


                    } else {

                        editor.remove(Constants.imagSwtichPartnerUrl)
                        editor.apply()
                        textPartnerUrlLunch.text = "Select Custom Domain"


                        val get_Saved_Domains_Name111 =
                            myDownloadClass.getString(Constants.Saved_Domains_Name, "")
                        if (get_Saved_Domains_Name111!!.isNotEmpty()) {
                            texturlsViews.text = get_Saved_Domains_Name111

                        } else {
                            texturlsViews.text = "Select Custom Domain"
                        }

                    }

                } catch (e: Exception) {
                }
            }


        }
    }


    private fun initSyncTimmer() {

        val get_savedIntervals = myDownloadClass.getLong(Constants.getTimeDefined, 0)

        if (get_savedIntervals != 0L) {
            binding.textIntervalsSelect.text = get_savedIntervals.toString() + Minutes
            binding.textDisplaytime.text = get_savedIntervals.toString() + Minutes

        } else {
            binding.textIntervalsSelect.text = "Sync interval timer"
            binding.textDisplaytime.text = "Selected time : 00:55"

        }

    }


    private fun showSelectedSyncType() {
        val bindingCm: CustomSelectSyncTypeBinding = CustomSelectSyncTypeBinding.inflate(layoutInflater)
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setView(bindingCm.root)
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.setCancelable(true)

        // Set the background of the AlertDialog to be transparent
        if (alertDialog.window != null) {
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        alertDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation

        val imgZipSync = bindingCm.imgZipSync


        if (handlerMoveToWebviewPage != null) {
            handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
        }



        bindingCm.apply {

            // nit the right rado btn

            val getSyncMethods = sharedBiometric.getString(Constants.IMG_SELECTED_SYNC_METHOD, "").toString()
            if (getSyncMethods == Constants.USE_ZIP_SYNC) {
                // set only this one to be true
                imgZipSync.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_check_true_24)
                // set the rest to be false
                imgAPISync.background = ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_check_false_radio_btn_24
                )
                imgParsingSync.background = ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_check_false_radio_btn_24
                )

            } else if (getSyncMethods == Constants.USE_API_SYNC) {

                // set only this one to be true
                imgAPISync.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_check_true_24)
                // set the rest to be false
                imgZipSync.background = ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_check_false_radio_btn_24
                )
                imgParsingSync.background = ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_check_false_radio_btn_24
                )


            } else if (getSyncMethods == Constants.USE_PARSING_SYNC) {
                // set only this one to be true
                imgParsingSync.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_check_true_24)
                // set the rest to be false
                imgZipSync.background = ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_check_false_radio_btn_24
                )
                imgAPISync.background = ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_check_false_radio_btn_24
                )

            }


            closeBs.setOnClickListener {
                alertDialog.dismiss()
            }


            imageCrossClose.setOnClickListener {
                alertDialog.dismiss()
            }



            textZipSync.setOnClickListener {
                initZipSyncMethod()
                initToggleUseZipSyncOrApI()

                // set only this one to be true
                imgZipSync.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_check_true_24)
                // set the rest to be false
                imgAPISync.background = ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_check_false_radio_btn_24
                )
                imgParsingSync.background = ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_check_false_radio_btn_24
                )


                alertDialog.dismiss()
            }


            textApiSync.setOnClickListener {
                initAPISyncMode()
                initToggleUseZipSyncOrApI()

                // set only this one to be true
                imgAPISync.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_check_true_24)
                // set the rest to be false
                imgZipSync.background = ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_check_false_radio_btn_24
                )
                imgParsingSync.background = ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_check_false_radio_btn_24
                )


                alertDialog.dismiss()
            }

            textParsingSync.setOnClickListener {


                // set only this one to be true
                imgParsingSync.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_check_true_24)
                // set the rest to be false
                imgZipSync.background = ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_check_false_radio_btn_24
                )
                imgAPISync.background = ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_check_false_radio_btn_24
                )



                initParsingSyncMethod()
                initToggleUseZipSyncOrApI()
                alertDialog.dismiss()

            }


        }

        alertDialog.show()

    }


    private fun initAPISyncMode() {
        binding.apply {

            val editorDN = myDownloadClass.edit()
            editorDN.remove(Constants.SynC_Status)
            editorDN.remove(Constants.textDownladByes)
            editorDN.remove(Constants.progressBarPref)
            editorDN.remove(Constants.progressBarPref)
            editorDN.remove(Constants.filesChange)
            editorDN.remove(Constants.numberOfFiles)

            editorDN.apply()


            val editor = sharedBiometric.edit()
            hideKeyBoard(binding.editTextInputSynUrlZip)


            editor.putString(Constants.IMG_SELECTED_SYNC_METHOD, Constants.USE_API_SYNC)
            editor.apply()
            binding.textDownloadZipSyncOrApiSyncNow.text = "Connect API Sync"
            binding.editTextInputSynUrlZip.hint = "Input url  API Sync"


            binding.textDownloadZipSyncOrApiSyncNow.setBackgroundResource(R.drawable.light_background_blue_color)
            binding.textDownloadZipSyncOrApiSyncNow.setTextColor(
                ContextCompat.getColor(
                    applicationContext, R.color.deep_blue
                )
            )


        }
    }

    private fun initZipSyncMethod() {
        binding.apply {
            val editorDN = myDownloadClass.edit()
            editorDN.remove(Constants.SynC_Status)
            editorDN.remove(Constants.textDownladByes)
            editorDN.remove(Constants.progressBarPref)
            editorDN.remove(Constants.progressBarPref)
            editorDN.remove(Constants.filesChange)
            editorDN.remove(Constants.numberOfFiles)

            editorDN.apply()


            val editor = sharedBiometric.edit()
            hideKeyBoard(binding.editTextInputSynUrlZip)


            editor.putString(Constants.IMG_SELECTED_SYNC_METHOD, Constants.USE_ZIP_SYNC)
            editor.apply()
            editTextInputSynUrlZip.hint = "Input url  ZIP Sync"
            textDownloadZipSyncOrApiSyncNow.text = "Connect ZIP Sync"


            textDownloadZipSyncOrApiSyncNow.setBackgroundResource(R.drawable.card_design_buy_gift_card)
            textDownloadZipSyncOrApiSyncNow.setTextColor(
                ContextCompat.getColor(
                    applicationContext, R.color.white
                )
            )

        }
    }


    private fun initParsingSyncMethod() {
        binding.apply {
            val editorDN = myDownloadClass.edit()
            editorDN.remove(Constants.SynC_Status)
            editorDN.remove(Constants.textDownladByes)
            editorDN.remove(Constants.progressBarPref)
            editorDN.remove(Constants.progressBarPref)
            editorDN.remove(Constants.filesChange)
            editorDN.remove(Constants.numberOfFiles)

            editorDN.apply()


            val editor = sharedBiometric.edit()
            hideKeyBoard(binding.editTextInputSynUrlZip)

            editor.putString(Constants.IMG_SELECTED_SYNC_METHOD, Constants.USE_PARSING_SYNC)
            editor.apply()

            editTextInputSynUrlZip.hint = "Input url  Parsing Sync"
            textDownloadZipSyncOrApiSyncNow.text = "Connect Parsing Sync"

            textDownloadZipSyncOrApiSyncNow.setBackgroundResource(R.drawable.card_design_buy_gift_card)
            textDownloadZipSyncOrApiSyncNow.setTextColor(
                ContextCompat.getColor(
                    applicationContext, R.color.white
                )
            )
        }
    }


    private fun showSaveduserHistory() {

        customSavedDownloadDialog = Dialog(this)
        val bindingCm = CustomSavedHistoryLayoutBinding.inflate(LayoutInflater.from(this))
        customSavedDownloadDialog.setContentView(bindingCm.root)
        customSavedDownloadDialog.setCancelable(true)
        customSavedDownloadDialog.setCanceledOnTouchOutside(true)
        customSavedDownloadDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val consMainAlert_sub_layout = bindingCm.consMainAlertSubLayout
        val textTitle = bindingCm.textTitle
        val textErrorText = bindingCm.textErrorText
        val textClearAllData = bindingCm.textClearAllData
        val imgCloseDialog = bindingCm.imageCrossClose
        val close_bs = bindingCm.closeBs
        val divider21 = bindingCm.divider21


        val preferences =
            android.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)

        if (preferences.getBoolean("darktheme", false)) {
            consMainAlert_sub_layout.setBackgroundResource(R.drawable.card_design_account_number_dark_pop_layout)

            textTitle.setTextColor(resources.getColor(R.color.dark_light_gray_pop))
            textErrorText.setTextColor(resources.getColor(R.color.dark_light_gray_pop))
            textClearAllData.setTextColor(resources.getColor(R.color.dark_light_gray_pop))

            val drawable_close_bs =
                ContextCompat.getDrawable(applicationContext, R.drawable.ic_baseline_arrow)
            drawable_close_bs?.setColorFilter(
                ContextCompat.getColor(
                    applicationContext, R.color.dark_light_gray_pop
                ), PorterDuff.Mode.SRC_IN
            )
            close_bs.setImageDrawable(drawable_close_bs)

            val drawable_imageCrossClose =
                ContextCompat.getDrawable(applicationContext, R.drawable.ic_close_24)
            drawable_imageCrossClose?.setColorFilter(
                ContextCompat.getColor(
                    applicationContext, R.color.dark_light_gray_pop
                ), PorterDuff.Mode.SRC_IN
            )
            imgCloseDialog.setImageDrawable(drawable_imageCrossClose)

            divider21.setBackgroundColor(
                ContextCompat.getColor(
                    applicationContext, R.color.dark_light_gray_pop
                )
            )


        }


        if (handlerMoveToWebviewPage != null) {
            handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
        }


        bindingCm.apply {


            closeBs.setOnClickListener {
                customSavedDownloadDialog.dismiss()
            }


            imageCrossClose.setOnClickListener {
                customSavedDownloadDialog.dismiss()
            }

            textClearAllData.setOnClickListener {
                startActivity(Intent(applicationContext, FileExplorerActivity::class.java))
                customSavedDownloadDialog.dismiss()
            }



            handler.postDelayed(Runnable {
                recyclerSavedDownload.adapter = adapter
                recyclerSavedDownload.layoutManager = LinearLayoutManager(applicationContext)

                mUserViewModel.readAllData.observe(this@ReSyncActivity, Observer { user ->
                    adapter.setData(user)
                    if (user.isNotEmpty()) {
                        textErrorText.visibility = View.GONE
                        textClearAllData.visibility = View.VISIBLE
                    } else {
                        textClearAllData.visibility = View.GONE
                        textErrorText.visibility = View.VISIBLE
                    }
                })


            }, 100)
        }




        customSavedDownloadDialog.show()

    }


    @SuppressLint("InflateParams", "SuspiciousIndentation", "SetTextI18n")
    private fun definedTimeIntervals() {
        val bindingCm: CustomDefinedTimeIntervalsBinding =
            CustomDefinedTimeIntervalsBinding.inflate(
                layoutInflater
            )
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setView(bindingCm.root)
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.setCancelable(true)

        // Set the background of the AlertDialog to be transparent
        if (alertDialog.window != null) {
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }



        if (handlerMoveToWebviewPage != null) {
            handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
        }



        bindingCm.apply {


            imageCrossClose.setOnClickListener {
                alertDialog.dismiss()
            }

            closeBs.setOnClickListener {
                alertDialog.dismiss()
            }



            textTwoMinutes.setOnClickListener {
                binding.textIntervalsSelect.text = timeMinuetes22.toString() + " $Minutes"
                binding.textDisplaytime.text = "2 Minutes"
                getTimeDefined_Prime = timeMinuetes22.toString()
                alertDialog.dismiss()

                val editor = myDownloadClass.edit()
                editor.putLong(Constants.getTimeDefined, Constants.t_2min)
                editor.apply()

            }


            text55minutes.setOnClickListener {
                binding.textIntervalsSelect.text = timeMinuetes55.toString() + " $Minutes"
                binding.textDisplaytime.text = "5 Minutes"
                getTimeDefined_Prime = timeMinuetes55.toString()
                alertDialog.dismiss()

                val editor = myDownloadClass.edit()
                editor.putLong(Constants.getTimeDefined, Constants.t_5min)
                editor.apply()


            }

            text100minutes2.setOnClickListener {
                binding.textIntervalsSelect.text = timeMinuetes10.toString() + " $Minutes"
                binding.textDisplaytime.text = "10 Minutes"
                getTimeDefined_Prime = timeMinuetes10.toString()
                alertDialog.dismiss()

                val editor = myDownloadClass.edit()
                editor.putLong(Constants.getTimeDefined, Constants.t_10min)
                editor.apply()


            }


            text1500minutes.setOnClickListener {
                binding.textIntervalsSelect.text = timeMinuetes15.toString() + " $Minutes"
                binding.textDisplaytime.text = "15 Minutes"
                getTimeDefined_Prime = timeMinuetes15.toString()
                alertDialog.dismiss()

                val editor = myDownloadClass.edit()
                editor.putLong(Constants.getTimeDefined, Constants.t_15min)
                editor.apply()


            }

            text3000minutes2.setOnClickListener {
                binding.textIntervalsSelect.text = timeMinuetes30.toString() + " $Minutes"
                binding.textDisplaytime.text = "30 Minutes"
                getTimeDefined_Prime = timeMinuetes30.toString()
                alertDialog.dismiss()

                val editor = myDownloadClass.edit()
                editor.putLong(Constants.getTimeDefined, Constants.t_30min)
                editor.apply()


            }

            text6000minutes.setOnClickListener {
                binding.textIntervalsSelect.text = timeMinuetes60.toString() + " $Minutes"
                binding.textDisplaytime.text = "60 Minutes"
                getTimeDefined_Prime = timeMinuetes60.toString()
                alertDialog.dismiss()

                val editor = myDownloadClass.edit()
                editor.putLong(Constants.getTimeDefined, Constants.t_60min)
                editor.apply()


            }


            textOneTwentyMinutes.setOnClickListener {
                binding.textIntervalsSelect.text = timeMinuetes120.toString() + " $Minutes"
                binding.textDisplaytime.text = "2 Hours"
                getTimeDefined_Prime = timeMinuetes120.toString()
                alertDialog.dismiss()

                val editor = myDownloadClass.edit()
                editor.putLong(Constants.getTimeDefined, Constants.t_120min)
                editor.apply()


            }



            textOneEightThyMinutes2.setOnClickListener {
                binding.textIntervalsSelect.text = timeMinuetes180.toString() + " $Minutes"
                binding.textDisplaytime.text = "3 hours"
                getTimeDefined_Prime = timeMinuetes180.toString()
                alertDialog.dismiss()

                val editor = myDownloadClass.edit()
                editor.putLong(Constants.getTimeDefined, Constants.t_180min)
                editor.apply()


            }


            tex24000ThyMinutes.setOnClickListener {
                binding.textIntervalsSelect.text = timeMinuetes240.toString() + " $Minutes"
                binding.textDisplaytime.text = "4 hours"
                getTimeDefined_Prime = timeMinuetes240.toString()
                alertDialog.dismiss()

                val editor = myDownloadClass.edit()
                editor.putLong(Constants.getTimeDefined, Constants.t_240min)
                editor.apply()


            }


        }


        alertDialog.show()
    }


    @SuppressLint("InflateParams", "SuspiciousIndentation")
    private fun serVerOptionDialog() {
        val bindingCm: CustomApiHardCodedLayoutBinding =
            CustomApiHardCodedLayoutBinding.inflate(
                layoutInflater
            )
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setView(bindingCm.root)
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.setCancelable(true)

        // Set the background of the AlertDialog to be transparent
        if (alertDialog.window != null) {
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }


        val textApiServer = bindingCm.textApiServer
        val textCloudServer = bindingCm.textCloudServer



        bindingCm.apply {

            textApiServer.setOnClickListener {

                if (handlerMoveToWebviewPage != null) {
                    handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                }

                binding.texturlsViews.text = CP_server
                getUrlBasedOnSpinnerText = CP_server



                binding.texturlsSavedDownload.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.deep_blue
                    )
                )


                val editor = myDownloadClass.edit()
                editor.putString(Constants.Saved_Parthner_Name, CP_server)
                editor.apply()

                alertDialog.dismiss()
            }


            textCloudServer.setOnClickListener {
                if (handlerMoveToWebviewPage != null) {
                    handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                }


                binding.texturlsViews.text = API_Server
                getUrlBasedOnSpinnerText = API_Server


                binding.texturlsSavedDownload.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.deep_blue
                    )
                )



                val editor = myDownloadClass.edit()
                editor.putString(Constants.Saved_Parthner_Name, API_Server)
                editor.apply()

                alertDialog.dismiss()
            }


            imageCrossClose.setOnClickListener {

                if (handlerMoveToWebviewPage != null) {
                    handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                }
                alertDialog.dismiss()
            }

            closeBs.setOnClickListener {

                if (handlerMoveToWebviewPage != null) {
                    handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                }

                alertDialog.dismiss()
            }


        }


        alertDialog.show()
    }


    @SuppressLint("MissingInflatedId", "DiscouragedApi")
    private fun showSyncDialog() {

        //create dialog
        val syncDialog = AlertDialog.Builder(this).create()
        val inflater = this.layoutInflater
        val viewOptions: View = inflater.inflate(R.layout.time_picker_dialog, null)


        //widgets
        val timePicker = viewOptions.findViewById<TimePicker>(R.id.timePicker)
        val cancelBtn = viewOptions.findViewById<Button>(R.id.cancelBtn)
        val setBtn = viewOptions.findViewById<Button>(R.id.setBtn)


        //dialog props
        syncDialog.setView(viewOptions)
        syncDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
        syncDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))



        if (handlerMoveToWebviewPage != null) {
            handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
        }


        //initialize time picker
        timePicker.hour = 0
        timePicker.minute = 0
        timePicker.setIs24HourView(true)
        timePicker.setOnTimeChangedListener { view: TimePicker?, hourOfDay: Int, minute: Int ->

            //get time
            hour = hourOfDay
            min = minute

        }

        //cancel
        cancelBtn.setOnClickListener { v: View? ->
            //dismiss
            syncDialog.dismiss()
        }

        //grant access
        setBtn.setOnClickListener { v: View? ->

            //calculate minutes
            val totalMins: Int = hour * 60 + min


            if (totalMins < 1) {
                showToastMessage("Time cannot be less than 1 minute")
                syncDialog.dismiss()
                return@setOnClickListener // Abort further execution
            }


            //set on edt
            binding.textDisplaytime.text = getFormattedTime(hour, min)
            binding.textIntervalsSelect.text = getFormattedTime(hour, min)


            //reset temp data
            hour = 0
            min = 0


            val editor = myDownloadClass.edit()
            editor.putLong(Constants.getTimeDefined, totalMins.toLong())
            editor.apply()


            //close dialog
            syncDialog.dismiss()
        }

        //show dialog
        syncDialog.show()
    }


    private fun getFormattedTime(hour: Int, minute: Int): String {
        val totalMinutes = hour * 60 + minute

        return when {
            totalMinutes == 1 -> "1 minute"
            totalMinutes == 2 -> "2 minutes"
            totalMinutes % 60 == 0 -> "${totalMinutes / 60} hour(s)"
            else -> "$totalMinutes minutes"
        }
    }

    private fun funManulOrNotInteView() {
        binding.apply {
            // logic for use manual or not
            val imagUsemanualOrnotuseManual =
                sharedBiometric.getString(Constants.imagSwtichEnableManualOrNot, "")
            imagSwtichEnableManualOrNot.isChecked =
                imagUsemanualOrnotuseManual.equals(Constants.imagSwtichEnableManualOrNot)


            if (imagUsemanualOrnotuseManual.equals(Constants.imagSwtichEnableManualOrNot)) {
                textUseManual.text = "Use manual"

                editTextInputSynUrlZip.visibility = View.VISIBLE
                editTextInputIndexManual.visibility = View.VISIBLE
                // for clos
                editTextCLOpath.visibility = View.GONE
                editTextSubPathFolder.visibility = View.GONE


            } else {
                textUseManual.text = "Do not use manual"

                editTextInputSynUrlZip.visibility = View.GONE
                editTextInputIndexManual.visibility = View.GONE
                // for clos
                editTextCLOpath.visibility = View.VISIBLE
                editTextSubPathFolder.visibility = View.VISIBLE


            }

            imagSwtichEnableManualOrNot.setOnCheckedChangeListener { compoundButton, isValued -> // we are putting the values into SHARED PREFERENCE
                val editor = sharedBiometric.edit()
                hideKeyBoard(binding.editTextInputSynUrlZip)
                if (compoundButton.isChecked) {
                    editor.putString(
                        Constants.imagSwtichEnableManualOrNot, "imagSwtichEnableManualOrNot"
                    )
                    editor.apply()
                    textUseManual.text = "Use manual"

                    editTextInputSynUrlZip.visibility = View.VISIBLE
                    editTextInputIndexManual.visibility = View.VISIBLE
                    // for clos
                    editTextCLOpath.visibility = View.GONE
                    editTextSubPathFolder.visibility = View.GONE


                } else {

                    editor.remove("imagSwtichEnableManualOrNot")
                    editor.apply()
                    textUseManual.text = "Do not use manual"

                    editTextInputSynUrlZip.visibility = View.GONE
                    editTextInputIndexManual.visibility = View.GONE
                    // for clos
                    editTextCLOpath.visibility = View.VISIBLE
                    editTextSubPathFolder.visibility = View.VISIBLE

                }
            }

        }
    }


    private fun hideKeyBoard(editText: EditText) {
        try {
            editText.clearFocus()
            val imm =
                applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editText.windowToken, 0)
        } catch (ignored: java.lang.Exception) {
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun testAndDownLoadZipConnection() {

        binding.apply {
            val getFolderClo = editTextCLOpath.text.toString().trim()
            val getFolderSubpath = editTextSubPathFolder.text.toString().trim()
            val editor = myDownloadClass.edit()


            if (isNetworkAvailable()) {
                if (!imagSwtichEnableManualOrNot.isChecked) {


                    if (imagSwtichPartnerUrl.isChecked) {

                        if (getUrlBasedOnSpinnerText.isNotEmpty()) {

                            when (getUrlBasedOnSpinnerText) {
                                CP_server -> {
                                    if (getFolderClo.isNotEmpty() && getFolderSubpath.isNotEmpty()) {
                                        httpNetworkDownloadsMultiplePaths(getFolderClo,getFolderSubpath)
                                        editor.putString(Constants.getSavedCLOImPutFiled, getFolderClo)
                                        editor.putString(Constants.getSaveSubFolderInPutFiled, getFolderSubpath)
                                        editor.putString(Constants.get_ModifiedUrl, Constants.CUSTOM_CP_SERVER_DOMAIN)
                                        editor.apply()

                                    } else {
                                        editTextCLOpath.error = "Input a valid path e.g CLO"
                                        editTextSubPathFolder.error =
                                            "Input a valid path e.g DE_MO_2021000"
                                        showToastMessage("Fields can not be empty")
                                    }
                                }

                                API_Server -> {

                                    if (getFolderClo.isNotEmpty() && getFolderSubpath.isNotEmpty()) {
                                        httpNetworkDownloadsMultiplePaths(getFolderClo, getFolderSubpath)
                                        editor.putString(Constants.getSavedCLOImPutFiled, getFolderClo)
                                        editor.putString(Constants.getSaveSubFolderInPutFiled, getFolderSubpath)
                                        editor.putString(Constants.get_ModifiedUrl, Constants.CUSTOM_API_SERVER_DOMAIN)
                                        editor.apply()

                                    } else {
                                        editTextCLOpath.error = "Input a valid path e.g CLO"
                                        editTextSubPathFolder.error =
                                            "Input a valid path e.g DE_MO_2021000"
                                        showToastMessage("Fields can not be empty")
                                    }

                                }

                            }

                        } else {
                            showToastMessage("Select Partner Url")
                        }

                    } else {

                        val getFolderClo222 = binding.editTextCLOpath.text.toString().trim()
                        val getFolderSubpath22 = binding.editTextSubPathFolder.text.toString().trim()
                        var Saved_Domains_Urls = myDownloadClass.getString(Constants.Saved_Domains_Urls, "").toString()

                        if (Saved_Domains_Urls.isNotEmpty()) {

                            if (getFolderClo222.isNotEmpty() && getFolderSubpath22.isNotEmpty()) {
                                testAndDownLoad_My_API(getFolderClo222, getFolderSubpath22)
                                editor.putString(Constants.getSavedCLOImPutFiled, getFolderClo222)
                                editor.putString(Constants.getSaveSubFolderInPutFiled, getFolderSubpath22)
                                editor.apply()

                            } else {
                                editTextCLOpath.error = "Input a valid path e.g CLO"
                                editTextSubPathFolder.error =
                                    "Input a valid path e.g DE_MO_2021000"
                                showToastMessage("Fields can not be empty")
                            }


                        } else {
                            showToastMessage("Select Custom Domain")
                        }

                    }
                } else {

                    // When allowed to use manual

                    // for maunal download
                    val editInputUrl = editTextInputSynUrlZip.text.toString().trim()
                    // to luanch a live url manual
                    val editInputAppIndex = editTextInputIndexManual.text.toString().trim()

                    if (isUrlValid(editInputUrl) && isUrlValid(editInputAppIndex)) {

                        httpNetSingleDwonload(editInputUrl, editInputAppIndex)

                        editor.putString(Constants.getSavedEditTextInputSynUrlZip, editInputUrl)
                        editor.putString(Constants.getSaved_manaul_index_edit_url_Input, editInputAppIndex)
                        editor.apply()

                    } else {

                        if (!isUrlValid(editInputUrl)) {
                            binding.editTextInputSynUrlZip.error = "Invalid url format"
                        }

                        if (!isUrlValid(editInputAppIndex)) {
                            binding.editTextInputIndexManual.error = "Invalid url format"
                        }

                        showToastMessage("Invalid url format")

                    }

                }
            } else {
                showToastMessage("No Internet Connection")
            }

        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun httpNetworkDownloadsMultiplePaths(
        getFolderClo: String,
        getFolderSubpath: String,
    ) {
        handler.postDelayed(Runnable {



            val getSyncMethods = sharedBiometric.getString(Constants.IMG_SELECTED_SYNC_METHOD, "").toString()

            if (getSyncMethods == Constants.USE_ZIP_SYNC) {
                showCustomProgressDialog("Please wait")

                val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
                val CP_AP_MASTER_DOMAIN = myDownloadClass.getString(Constants.CP_OR_AP_MASTER_DOMAIN, "").toString()
                val baseDomain = CP_AP_MASTER_DOMAIN
                val baseUrl = "$baseDomain/$getFolderClo/$getFolderSubpath/Zip/App.zip"

                lifecycleScope.launch {
                    try {
                        val result = checkUrlExistence(baseUrl)
                        if (result) {
                            startMyDownlaodsMutiplesPath(
                                baseUrl,
                                getFolderClo,
                                getFolderSubpath,
                                Constants.Zip,
                                Constants.fileNmae_App_Zip,
                            )

                            // save also to room data base
                            val user = User(
                                CLO = getFolderClo,
                                DEMO = getFolderSubpath,
                                EditUrl = "",
                                EditUrlIndex = ""
                            )
                            mUserViewModel.addUser(user)

                            savePathServerUrl(CP_AP_MASTER_DOMAIN, getFolderClo, getFolderSubpath)

                        } else {
                            showPopsForMyConnectionTest(getFolderClo, getFolderSubpath, "Failed!")

                            customProgressDialog.dismiss()
                        }
                    } finally {

                    }
                }


            } else if (getSyncMethods == Constants.USE_API_SYNC) {

                showCustomProgressDialog("Please wait")

                val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
                val CP_AP_MASTER_DOMAIN = myDownloadClass.getString(Constants.CP_OR_AP_MASTER_DOMAIN, "").toString()

                //val baseDomain = Constants.CUSTOM_CP_SERVER_DOMAIN
                val baseDomain = CP_AP_MASTER_DOMAIN
                val myCSvEndPath = Constants.myCSvEndPath
                // val baseUrl = "$baseDomain/$getFolderClo/$getFolderSubpath/App/index.html"
                val baseUrl = "$baseDomain/$getFolderClo/$getFolderSubpath/$myCSvEndPath"



                lifecycleScope.launch {
                    try {
                        val result = checkUrlExistence(baseUrl)
                        if (result) {
                            startMyDownlaodsMutiplesPath(
                                baseUrl,
                                getFolderClo,
                                getFolderSubpath,
                                Constants.App,
                                "index.html",
                            )

                            val user = User(
                                CLO = getFolderClo,
                                DEMO = getFolderSubpath,
                                EditUrl = "",
                                EditUrlIndex = ""
                            )
                            mUserViewModel.addUser(user)

                            savePathServerUrl(CP_AP_MASTER_DOMAIN, getFolderClo, getFolderSubpath)

                        } else {
                            showPopsForMyConnectionTest(
                                getFolderClo, getFolderSubpath, "Failed!"
                            )
                            customProgressDialog.dismiss()
                        }
                    } finally {

                    }
                }

            } else if (getSyncMethods == Constants.USE_PARSING_SYNC) {

                showCustomProgressDialog("Please wait")

                val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
                val CP_AP_MASTER_DOMAIN = myDownloadClass.getString(Constants.CP_OR_AP_MASTER_DOMAIN, "").toString()
                val baseUrl = "$CP_AP_MASTER_DOMAIN/$getFolderClo/$getFolderSubpath/App/index.html"

                //  https://cp.cloudappserver.co.uk/app_base/public//CLO/DE_MO_2021001/App/index.html

                lifecycleScope.launch {
                    try {
                        val result = checkUrlExistence(baseUrl)
                        if (result) {
                            startMyDownlaodsMutiplesPath(
                                CP_AP_MASTER_DOMAIN,
                                getFolderClo,
                                getFolderSubpath,
                                Constants.Zip,
                                Constants.fileNmae_App_Zip,
                            )

                            // save also to room data base
                            val user = User(
                                CLO = getFolderClo,
                                DEMO = getFolderSubpath,
                                EditUrl = "",
                                EditUrlIndex = ""
                            )
                            mUserViewModel.addUser(user)

                            savePathServerUrl(CP_AP_MASTER_DOMAIN, getFolderClo, getFolderSubpath)

                        } else {
                            showPopsForMyConnectionTest(getFolderClo, getFolderSubpath, "Failed!")

                        }
                    } finally {

                    }
                }


            }




        }, 300)
    }



    @RequiresApi(Build.VERSION_CODES.Q)
    private fun httpNetSingleDwonload(editInputUrl: String, editInputAppIndex: String) {
        handler.postDelayed(Runnable {
            showCustomProgressDialog("Please wait")
            val lastString = editInputUrl.substringAfterLast("/")
            val fileNameWithoutExtension = lastString.substringBeforeLast(".")

            val getSyncMethods =
                sharedBiometric.getString(Constants.IMG_SELECTED_SYNC_METHOD, "").toString()

            if (getSyncMethods == Constants.USE_ZIP_SYNC) {

                lifecycleScope.launch {
                    try {
                        val result = checkUrlExistence(editInputUrl)
                        if (result) {
                            startDownloadSingles(
                                editInputUrl, Constants.Zip, Constants.fileNmae_App_Zip
                            )

                            val user = User(
                                CLO = "",
                                DEMO = "",
                                EditUrl = editInputUrl,
                                EditUrlIndex = editInputAppIndex
                            )
                            mUserViewModel.addUser(user)

                        } else {
                            showPopsForMyConnectionTest("CLO", fileNameWithoutExtension, "Failed!")
                            if (customProgressDialog != null){
                                customProgressDialog.dismiss()
                            }
                        }
                    } catch (e:java.lang.Exception){
                        if (customProgressDialog != null){
                            customProgressDialog.dismiss()
                        }
                    }
                }

            } else if (getSyncMethods == Constants.USE_API_SYNC) {

                lifecycleScope.launch {
                    try {
                        val result = checkUrlExistence(editInputUrl)
                        if (result) {
                            startDownloadSingles(
                                editInputUrl, Constants.Zip, Constants.fileNmae_App_Zip
                            )
                            val user = User(
                                CLO = "",
                                DEMO = "",
                                EditUrl = editInputUrl,
                                EditUrlIndex = editInputAppIndex
                            )
                            mUserViewModel.addUser(user)
                        } else {

                            showPopsForMyConnectionTest(
                                "CLO", fileNameWithoutExtension, "Failed!"
                            )

                            if (customProgressDialog != null){
                                customProgressDialog.dismiss()
                            }
                        }
                    } catch (e:Exception){
                        if (customProgressDialog != null){
                            customProgressDialog.dismiss()
                        }
                    }
                }

            } else if (getSyncMethods == Constants.USE_PARSING_SYNC) {

                lifecycleScope.launch {
                    try {
                        val result = checkUrlExistence(editInputUrl)
                        if (result) {
                            startDownloadSingles(
                                editInputUrl, Constants.Zip, Constants.fileNmae_App_Zip
                            )
                            val user = User(
                                CLO = "",
                                DEMO = "",
                                EditUrl = editInputUrl,
                                EditUrlIndex = editInputAppIndex
                            )
                            mUserViewModel.addUser(user)
                        } else {

                            showPopsForMyConnectionTest(
                                "CLO", fileNameWithoutExtension, "Failed!"
                            )

                            if (customProgressDialog != null){
                                customProgressDialog.dismiss()
                            }
                        }
                    } catch (e:Exception){
                        if (customProgressDialog != null){
                            customProgressDialog.dismiss()
                        }
                    }
                }

        }


        }, 300)
    }


    private fun testConnectionSetup_API_Test(getFolderClo: String, getFolderSubpath: String) {

        showCustomProgressDialog("Testing connection")

        binding.apply {

            var Saved_Domains_Urls = myDownloadClass.getString(Constants.Saved_Domains_Urls, "").toString()


            if (isNetworkAvailable()) {

                val get_ModifiedUrl = Saved_Domains_Urls
                val editor = myDownloadClass.edit()
                editor.putString(Constants.get_ModifiedUrl, get_ModifiedUrl)
                editor.apply()


                val getSyncMethods =
                    sharedBiometric.getString(Constants.IMG_SELECTED_SYNC_METHOD, "").toString()

                if (getSyncMethods == Constants.USE_ZIP_SYNC) {

                    val get_Full_url = Saved_Domains_Urls + "/$getFolderClo/$getFolderSubpath/Zip/App.zip"

                    showToastMessageLong(get_Full_url)

                    lifecycleScope.launch {
                        try {
                            val result = checkUrlExistence(get_Full_url)
                            if (result) {
                                showPopsForMyConnectionTest(
                                    getFolderClo, getFolderSubpath, "Successful"
                                )

                                val user = User(
                                    CLO = getFolderClo,
                                    DEMO = getFolderSubpath,
                                    EditUrl = "",
                                    EditUrlIndex = ""
                                )
                                mUserViewModel.addUser(user)

                                if (customProgressDialog != null){
                                    customProgressDialog.dismiss()
                                }

                                savePathServerUrl(get_ModifiedUrl, getFolderClo, getFolderSubpath)

                            } else {
                                showPopsForMyConnectionTest(
                                    getFolderClo, getFolderSubpath, "Failed!"
                                )

                                if (customProgressDialog != null){
                                    customProgressDialog.dismiss()
                                }

                            }
                        } catch (e:java.lang.Exception){
                            if (customProgressDialog != null){
                                customProgressDialog.dismiss()
                            }
                        }
                    }



                } else if (getSyncMethods == Constants.USE_API_SYNC) {
                    // val get_Full_url = Saved_Domains_Urls + "/$getFolderClo/$getFolderSubpath/App/index.html"

                    val myCSvEndPath = Constants.myCSvEndPath
                    val get_Full_url = "$Saved_Domains_Urls/$getFolderClo/$getFolderSubpath/$myCSvEndPath"

                    showToastMessageLong(get_Full_url)

                    lifecycleScope.launch {
                        try {
                            val result = checkUrlExistence(get_Full_url)
                            if (result) {
                                showPopsForMyConnectionTest(
                                    getFolderClo, getFolderSubpath, "Successful"
                                )

                                val user = User(
                                    CLO = getFolderClo,
                                    DEMO = getFolderSubpath,
                                    EditUrl = "",
                                    EditUrlIndex = ""
                                )
                                mUserViewModel.addUser(user)

                                if (customProgressDialog != null){
                                    customProgressDialog.dismiss()
                                }

                                savePathServerUrl(get_ModifiedUrl, getFolderClo, getFolderSubpath)

                            } else {
                                showPopsForMyConnectionTest(
                                    getFolderClo, getFolderSubpath, "Failed!"
                                )

                                if (customProgressDialog != null){
                                    customProgressDialog.dismiss()
                                }
                            }

                        }catch (e:java.lang.Exception){
                            if (customProgressDialog != null){
                                customProgressDialog.dismiss()
                            }
                        } }


                }else if (getSyncMethods == Constants.USE_PARSING_SYNC) {

                    val baseUrl = "${Saved_Domains_Urls}/$getFolderClo/$getFolderSubpath/App/index.html"
                    showToastMessageLong(baseUrl)

                    lifecycleScope.launch {
                        try {
                            val result = checkUrlExistence(baseUrl)
                            if (result) {

                                showPopsForMyConnectionTest(
                                    getFolderClo, getFolderSubpath, "Successful"
                                )

                                // save also to room data base
                                val user = User(
                                    CLO = getFolderClo,
                                    DEMO = getFolderSubpath,
                                    EditUrl = "",
                                    EditUrlIndex = ""
                                )
                                mUserViewModel.addUser(user)


                                if (customProgressDialog != null){
                                    customProgressDialog.dismiss()
                                }

                                savePathServerUrl(get_ModifiedUrl, getFolderClo, getFolderSubpath)

                            } else {
                                showPopsForMyConnectionTest(
                                    getFolderClo, getFolderSubpath, "Failed!"
                                )

                                if (customProgressDialog != null){
                                    customProgressDialog.dismiss()
                                }
                            }


                        } catch (e:Exception){

                            showPopsForMyConnectionTest(
                                getFolderClo, getFolderSubpath, "Failed!"
                            )

                            if (customProgressDialog != null){
                                customProgressDialog.dismiss()
                            }

                        }
                    }

                }


            } else {
                showToastMessage("No Internet Connection")
            }
        }


    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun testAndDownLoad_My_API(getFolderClo: String, getFolderSubpath: String) {

        binding.apply {

            var Saved_Domains_Urls = myDownloadClass.getString(Constants.Saved_Domains_Urls, "").toString()

            if (isNetworkAvailable()) {

                val get_ModifiedUrl = Saved_Domains_Urls
                val editor = myDownloadClass.edit()
                editor.putString(Constants.get_ModifiedUrl, get_ModifiedUrl)
                editor.apply()

                val getSyncMethods = sharedBiometric.getString(Constants.IMG_SELECTED_SYNC_METHOD, "").toString()

                if (getSyncMethods == Constants.USE_ZIP_SYNC) {
                    showCustomProgressDialog("Testing connection")

                    val get_Full_url = "$Saved_Domains_Urls/$getFolderClo/$getFolderSubpath/Zip/App.zip"

                    showToastMessageLong(get_Full_url)

                    lifecycleScope.launch {
                        try {
                            val result = checkUrlExistence(get_Full_url)
                            if (result) {
                                startMyDownlaodsMutiplesPath(get_Full_url, getFolderClo, getFolderSubpath, "Zip", "App.zip",)

                                // save also to room data base
                                val user = User(
                                    CLO = getFolderClo,
                                    DEMO = getFolderSubpath,
                                    EditUrl = "",
                                    EditUrlIndex = ""
                                )
                                mUserViewModel.addUser(user)

                                savePathServerUrl(get_ModifiedUrl, getFolderClo, getFolderSubpath)

                            } else {
                                showPopsForMyConnectionTest(getFolderClo, getFolderSubpath, "Failed!")

                                if (customProgressDialog != null){
                                    customProgressDialog.dismiss()
                                }

                            }
                        } catch (e:Exception){
                            if (customProgressDialog != null){
                                customProgressDialog.dismiss()
                            }
                        }
                    }



                } else if (getSyncMethods == Constants.USE_API_SYNC) {
                    showCustomProgressDialog("Testing connection")

                    //   val get_Full_url = Saved_Domains_Urls + "/$getFolderClo/$getFolderSubpath/App/index.html"

                    val myCSvEndPath = Constants.myCSvEndPath
                    val get_Full_url = Saved_Domains_Urls + "/$getFolderClo/$getFolderSubpath/$myCSvEndPath"


                    showToastMessageLong(get_Full_url)

                    lifecycleScope.launch {
                        try {
                            val result = checkUrlExistence(get_Full_url)
                            if (result) {
                                startMyDownlaodsMutiplesPath(
                                    get_Full_url,
                                    getFolderClo,
                                    getFolderSubpath,
                                    Constants.App,
                                    "index.html",
                                )

                                // save also to room data base
                                val user = User(
                                    CLO = getFolderClo,
                                    DEMO = getFolderSubpath,
                                    EditUrl = "",
                                    EditUrlIndex = ""
                                )
                                mUserViewModel.addUser(user)

                                savePathServerUrl(get_ModifiedUrl, getFolderClo, getFolderSubpath)

                            } else {
                                showPopsForMyConnectionTest(
                                    getFolderClo, getFolderSubpath, "Failed!"
                                )
                                if (customProgressDialog != null){
                                    customProgressDialog.dismiss()
                                }
                            }
                        }catch (e:Exception){
                            if (customProgressDialog != null){
                                customProgressDialog.dismiss()
                            }
                        }
                    }


                }else if (getSyncMethods == Constants.USE_PARSING_SYNC) {

                    showCustomProgressDialog("Testing connection")

                    val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
                    val CP_AP_MASTER_DOMAIN = myDownloadClass.getString(Constants.CP_OR_AP_MASTER_DOMAIN, "").toString()
                    val baseUrl = "$CP_AP_MASTER_DOMAIN/$getFolderClo/$getFolderSubpath/App/index.html"

                    //  https://cp.cloudappserver.co.uk/app_base/public//CLO/DE_MO_2021001/App/index.html

                    showToastMessageLong(baseUrl)

                    lifecycleScope.launch {
                        try {
                            val result = checkUrlExistence(baseUrl)
                            if (result) {
                                startMyDownlaodsMutiplesPath(
                                    CP_AP_MASTER_DOMAIN,
                                    getFolderClo,
                                    getFolderSubpath,
                                    Constants.Zip,
                                    Constants.fileNmae_App_Zip,
                                )

                                // save also to room data base
                                val user = User(
                                    CLO = getFolderClo,
                                    DEMO = getFolderSubpath,
                                    EditUrl = "",
                                    EditUrlIndex = ""
                                )
                                mUserViewModel.addUser(user)

                                savePathServerUrl(get_ModifiedUrl, getFolderClo, getFolderSubpath)

                            } else {
                                showPopsForMyConnectionTest(getFolderClo, getFolderSubpath, "Failed!")

                            }
                        } finally {

                        }
                    }


                }

            } else {
                showToastMessage("No Internet Connection")
            }
        }

    }


    @SuppressLint("MissingInflatedId", "SetTextI18n")
    private fun showPopsForMyConnectionTest(
        getFolderClo: String,
        getFolderSubpath: String,
        message: String,
    ) {

        val bindingCM: CustomContinueDownloadLayoutBinding =
            CustomContinueDownloadLayoutBinding.inflate(layoutInflater)
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(bindingCM.root)

        val alertDialog = alertDialogBuilder.create()
        alertDialog.setCancelable(true)

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val textSucessful = bindingCM.textSucessful
        val textYourUrlTest = bindingCM.textYourUrlTest
        val textContinuPassword2 = bindingCM.textContinuPassword2
        val imgCloseDialog = bindingCM.imgCloseDialog





        bindingCM.apply {

            if (message.equals(Constants.Invalid_Config_Url)) {
                textYourUrlTest.text = Constants.Invalid_Config_Url
            } else {
                val userPath = "Username-$getFolderClo\nlicense-$getFolderSubpath"
                textYourUrlTest.text = userPath
            }


            textSucessful.text = message

            textContinuPassword2.setOnClickListener {

                alertDialog.dismiss()
            }

            imgCloseDialog.setOnClickListener {
                alertDialog.dismiss()
            }

        }


        alertDialog.show()


    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun startMyDownlaodsMutiplesPath(
        baseUrl: String,
        getFolderClo: String,
        getFolderSubpath: String,
        Zip: String,
        fileName: String,
    ) {

        if (binding.imagSwtichEnableConfigFileOnline.isChecked && isCompltedAndReady == false) {
            check_for_valid_confileUrl()

        } else {
            binding.apply {

                //    customProgressDialog.dismiss()
                val threeFolderPath = "/$getFolderClo/$getFolderSubpath/$Zip"


                val Extracted = "${Constants.App}"


                val getSyncMethods = sharedBiometric.getString(Constants.IMG_SELECTED_SYNC_METHOD, "").toString()

                if (getSyncMethods == Constants.USE_ZIP_SYNC) {

                    download(baseUrl, getFolderClo, getFolderSubpath, Zip, fileName, Extracted, threeFolderPath)

                } else if (getSyncMethods == Constants.USE_API_SYNC) {

                    callApiClassActivity(
                        baseUrl,
                        getFolderClo,
                        getFolderSubpath,
                        Zip,
                        fileName,
                        Extracted,
                        threeFolderPath
                    )

                } else if (getSyncMethods == Constants.USE_PARSING_SYNC) {
                    startParsingActivity(
                        baseUrl,
                        getFolderClo,
                        getFolderSubpath,
                        Zip,
                        fileName,
                        Extracted,
                        threeFolderPath
                    )

                }else if (getSyncMethods == Constants.USE_DRIVE_SYNC){


                    download(baseUrl, getFolderClo, getFolderSubpath, Zip, fileName, Extracted, threeFolderPath)

                }


                /// similar but used on under second cancel downoad in danwload pager
                val editor = myDownloadClass.edit()
                editor.putString(Constants.getFolderClo, getFolderClo)
                editor.putString(Constants.getFolderSubpath, getFolderSubpath)
                editor.putString("Zip", Zip)
                editor.putString("fileName", fileName)
                editor.putString(Constants.Extracted, Extracted)
                editor.putString("baseUrl", baseUrl)


                val editText88 = sharedBiometric.edit()

                if (binding.imagSwtichEnableLaucngOnline.isChecked) {

                    editText88.putString(Constants.get_Launching_State_Of_WebView, Constants.launch_WebView_Online)
                    editText88.apply()

                } else {

                    editText88.putString(Constants.get_Launching_State_Of_WebView, Constants.launch_WebView_Offline)
                    editText88.apply()

                }

                editor.apply()
            }
        }


    }

    private fun startParsingActivity(
        baseUrl: String,
        getFolderClo: String,
        getFolderSubpath: String,
        Zip: String,
        fileNamy: String,
        Extracted: String,
        threeFolderPath: String,
    ) {


        val editior = myDownloadClass.edit()
        editior.putString(Constants.getFolderClo, getFolderClo)
        editior.putString(Constants.getFolderSubpath, getFolderSubpath)
        editior.putString("Zip", Zip)
        editior.putString("fileNamy", fileNamy)
        editior.putString(Constants.Extracted, Extracted)

        // used to control Sync Start from  set up page
        editior.remove(Constants.Manage_My_Sync_Start)


        val get_savedIntervals = myDownloadClass.getLong(Constants.getTimeDefined, 0)

        if (get_savedIntervals != 0L) {
            editior.putLong(Constants.getTimeDefined, get_savedIntervals)

        } else {
            editior.putLong(Constants.getTimeDefined, Constants.t_5min)

        }

        editior.apply()



        mFilesViewModel.deleteAllFiles()
        dnFailedViewModel.deleteAllFiles()
        dnViewModel.deleteAllFiles()
        parsingViewModel.deleteAllFiles()


        lifecycleScope.launch(Dispatchers.IO) {

            val Syn2AppLive = Constants.Syn2AppLive
            val saveMyFileToStorage = "/$Syn2AppLive/$getFolderClo/$getFolderSubpath/App/"

            // delete existing files first
            val directoryPath = Environment.getExternalStorageDirectory().absolutePath + "/Download/" + saveMyFileToStorage
            val myFile = File(directoryPath)
            delete(myFile)


            val saveMyFileToStorage_second = "/$Syn2AppLive/$getFolderClo/$getFolderSubpath/"
            val fileName = "/App/"
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), saveMyFileToStorage_second)
            val myFile_second = File(dir, fileName)
            delete(myFile_second)


            // delete tempoaray parsing folder

            val Demo_Parsing_Folder = Constants.TEMP_PARS_FOLDER
            val saveDemoStorage = "/$Syn2AppLive/$Demo_Parsing_Folder/$getFolderClo/$getFolderSubpath/App/"
            val directoryParsing = Environment.getExternalStorageDirectory().absolutePath + "/Download/" + saveDemoStorage
            val myFileParsing = File(directoryParsing)
            delete(myFileParsing)



            val parsingStorage_second = "/$Syn2AppLive/$Demo_Parsing_Folder/$getFolderClo/$getFolderSubpath/"
            val fileNameParsing = "/App/"
            val dirParsing = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), parsingStorage_second)
            val myFile_Parsing = File(dirParsing, fileNameParsing)
            delete(myFile_Parsing)



            withContext(Dispatchers.Main){
                InitParsingScanForDownload(baseUrl = baseUrl, getFolderClo = getFolderClo, getFolderSubpath = getFolderSubpath)
            }

        }

    }


    private fun callApiClassActivity(
        baseUrl: String,
        getFolderClo: String,
        getFolderSubpath: String,
        Zip: String,
        fileNamy: String,
        Extracted: String,
        threeFolderPath: String,

        ) {

        val editior = myDownloadClass.edit()
        editior.putString(Constants.getFolderClo, getFolderClo)
        editior.putString(Constants.getFolderSubpath, getFolderSubpath)
        editior.putString("Zip", Zip)
        editior.putString("fileNamy", fileNamy)
        editior.putString(Constants.Extracted, Extracted)

        // used to control Sync Start from  set up page
        editior.remove(Constants.Manage_My_Sync_Start)


        val get_savedIntervals = myDownloadClass.getLong(Constants.getTimeDefined, 0)

        if (get_savedIntervals != 0L) {
            editior.putLong(Constants.getTimeDefined, get_savedIntervals)

        } else {
            editior.putLong(Constants.getTimeDefined, Constants.t_5min)

        }

        editior.apply()


        InitdownloadTheApiCsvData()


    }


    private fun InitdownloadTheApiCsvData() {
        binding.apply {


            if (binding.imagSwtichEnableManualOrNot.isChecked) {
                handler.postDelayed(Runnable {

                    val getSavedEditTextInputSynUrlZip =
                        myDownloadClass.getString(Constants.getSavedEditTextInputSynUrlZip, "")
                            .toString()

                    if (getSavedEditTextInputSynUrlZip.contains(Constants.myCSvEndPath)) {
                        getDownloadMyCSVManual()
                    } else if (getSavedEditTextInputSynUrlZip.contains(Constants.myCSVUpdate1)) {
                        getDownloadMyCSVManual()
                    } else {

                        showToastMessage(Constants.Error_CSv_Message)
                        if (customProgressDialog != null){
                            customProgressDialog.dismiss()
                        }

                    }

                }, 500)

            } else {

                handler.postDelayed(Runnable {
                    getDownloadMyCSV()
                }, 500)
            }

        }
    }


    @SuppressLint("SetTextI18n")
    private fun getDownloadMyCSV() {

        lifecycleScope.launch(Dispatchers.IO) {
            val getFolderClo = myDownloadClass.getString(Constants.getFolderClo, "").toString()
            val getFolderSubpath =
                myDownloadClass.getString(Constants.getFolderSubpath, "").toString()
            val get_ModifiedUrl =
                myDownloadClass.getString(Constants.get_ModifiedUrl, "").toString()

            Log.d(TAG_RSYC, get_ModifiedUrl)

            val lastEnd = "Start/start1.csv"
            val csvDownloader = CSVDownloader()
            val csvData = csvDownloader.downloadCSV(
                get_ModifiedUrl, getFolderClo, getFolderSubpath, lastEnd
            )
            saveURLPairs(csvData)

            handler.postDelayed(Runnable {
                if (customProgressDialog != null){
                    customProgressDialog.dismiss()
                }
                val intent = Intent(applicationContext, DownloadApisFilesActivity::class.java)
                startActivity(intent)
                finish()


            }, 5000)


        }


    }


    @SuppressLint("SetTextI18n")
    private fun getDownloadMyCSVManual() {

        lifecycleScope.launch(Dispatchers.IO) {

            val get_getSavedEditTextInputSynUrlZip =
                myDownloadClass.getString(Constants.getSavedEditTextInputSynUrlZip, "")
                    .toString()


            val csvDownloader = CSVDownloader()
            val csvData =
                csvDownloader.downloadCSV(get_getSavedEditTextInputSynUrlZip, "", "", "")
            saveURLPairs(csvData)


            handler.postDelayed(Runnable {
                if (customProgressDialog != null){
                    customProgressDialog.dismiss()
                }
                val intent = Intent(applicationContext, DownloadApisFilesActivity::class.java)
                startActivity(intent)
                finish()


            }, 5000)


        }


    }


    private fun saveURLPairs(csvData: String) {

        mFilesViewModel.deleteAllFiles()
        dnFailedViewModel.deleteAllFiles()
        dnViewModel.deleteAllFiles()
        parsingViewModel.deleteAllFiles()
        val pairs = parseCSV(csvData)

        // Add files to Room Database
        lifecycleScope.launch(Dispatchers.IO) {
            for ((index, line) in pairs.withIndex()) {
                val parts = line.split(",").map { it.trim() }
                if (parts.size < 2) continue // Skip lines with insufficient data

                val sn = parts[0].toIntOrNull() ?: continue // Skip lines with invalid SN
                val folderAndFile = parts[1].split("/")

                val folderName = if (folderAndFile.size > 1) {
                    folderAndFile.dropLast(1).joinToString("/")
                } else {
                    "MyApiFolder" // Assuming default folder name
                }

                val fileName =
                    folderAndFile.lastOrNull() ?: continue // Skip lines with missing file name
                val status = "true" // Set your status here
                // val id =   System.currentTimeMillis()
                val files = FilesApi(
                    SN = sn.toString(),
                    FolderName = folderName,
                    FileName = fileName,
                    Status = status
                )
                mFilesViewModel.addFiles(files)

                val dnFailedApi = DnFailedApi(
                    SN = sn.toString(),
                    FolderName = folderName,
                    FileName = fileName,
                    Status = status
                )
                dnFailedViewModel.addFiles(dnFailedApi)

            }
        }

    }

    // for no need of comma CSV
    private fun parseCSV(csvData: String): List<String> {
        val pairs = mutableListOf<String>()
        val lines = csvData.split("\n")
        for (line in lines) {
            if (line.isNotBlank()) {
                pairs.add(line.trim())
            }
        }
        return pairs
    }


    /// Init Parsing
    private fun InitParsingScanForDownload(
        baseUrl: String,
        getFolderClo: String,
        getFolderSubpath: String,

        ) {

        binding.apply {


            if (binding.imagSwtichEnableManualOrNot.isChecked) {
                handler.postDelayed(Runnable {

                    val getSavedEditTextInputSynUrlZip = myDownloadClass.getString(Constants.getSavedEditTextInputSynUrlZip, "").toString()

                    if (getSavedEditTextInputSynUrlZip.contains(Constants.ifEndContainsIndexFileName)) {

                        showToastMessage(getSavedEditTextInputSynUrlZip)
                        getAllIndexUrls(getSavedEditTextInputSynUrlZip, baseUrl, getFolderClo, getFolderSubpath)
                        showToastMessage("Start Parsing")
                    } else {

                        showToastMessage(Constants.Error_Parsing_Message)

                        if (customProgressDialog!=null){
                            customProgressDialog.dismiss()
                        }
                    }

                }, 500)

            } else {

                handler.postDelayed(Runnable {

                    val parsingUrl = "$baseUrl/$getFolderClo/$getFolderSubpath/App/index.html"
                    getAllIndexUrls(parsingUrl, baseUrl,  getFolderClo, getFolderSubpath)

                }, 500)
            }

        }


    }


    @SuppressLint("SetTextI18n")
    private fun getAllIndexUrls(url: String, baseUrl: String,  getFolderClo: String, getFolderSubpath: String) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val urls = Utility.fetchUrlsFromHtml(url)
                filesToProcess = urls.size
                // Prepare a StringBuilder to accumulate URLs
                val builder = StringBuilder()

                withContext(Dispatchers.Main) {
                    var validCount = 0  // Counter for valid URLs

                    urls.forEach { it ->
                        Log.d(TAG_RSYC, "Fetched URL: $it")
                        filIst = builder.append("Fetched URL:$validCount ::: $it\n").toString()

                        // Check if the URL should be saved
                        if (shouldSaveUrl(it, baseUrl, getFolderClo, getFolderSubpath)) {

                            if (!isActive) {
                                Log.d(TAG_RSYC, "Process canceled.")
                                return@withContext
                            }

                            saveParsingURLPairs(validCount, it, urls.size)
                            validCount++  // Increment only for valid URLs
                        } else {
                            Log.d(TAG_RSYC, "Ignoring URL: $it")
                        }

                        mutex.withLock {
                            filesToProcess--

                            if (filesToProcess == 0) {
                                // All files are processed, trigger your action here .e sort files time stamp
                                handler.postDelayed(Runnable {
                                    onAllFilesProcessed()
                                }, 400)
                            }
                        }

                    }


                }
            }
        }catch (e:Exception){
            Log.d(TAG_RSYC, "getAllIndexUrls: ${e.message}")
            showToastMessage("Error, Something went wrong.... ${e.message}")
        }
    }

    // Function to determine if a URL should be saved
    private fun shouldSaveUrl(url: String, _baseUrl: String, getFolderClo: String, getFolderSubpath: String): Boolean {
        // Check if the URL ends with a slash
        if (url.endsWith("/")) return false

        // Extract the relative path after the base URL
        val baseUrl = "$_baseUrl/$getFolderClo/$getFolderSubpath/"
        val relativePath = url.removePrefix(baseUrl)

        // Check if there is a file name with a dot (.)
        val fileName = relativePath.substringAfterLast('/')
        return fileName.contains('.')
    }



    private fun saveParsingURLPairs(index: Int, url: String, totalFiles: Int) {
        val fullPath = extractFolderAndFile(url)
        if (fullPath.isEmpty()) return // Skip invalid URLs

        // Extract folder and file name
        val folderName = fullPath.substringBeforeLast("/", "") // Everything before the last "/"
        val fileName = fullPath.substringAfterLast("/", "") // The actual file name

        val status = "true"

        // Initialize filesToProcess only once
        if (filesToProcess == 0) {
            filesToProcess = totalFiles
        }


        val files = FilesApi(
            SN = index.toString(),
            FolderName = folderName,
            FileName = fileName,
            Status = status
        )

        val dnFailedApi = DnFailedApi(
            SN = index.toString(),
            FolderName = folderName,
            FileName = fileName,
            Status = status
        )



        // Add file to Room Database
        lifecycleScope.launch(Dispatchers.IO) {
            dnFailedViewModel.addFiles(dnFailedApi)
            mFilesViewModel.addFiles(files)
        }


    }


    private fun extractFolderAndFile(url: String): String {

        val get_tMaster: String = myDownloadClass.getString(Constants.get_ModifiedUrl, "").toString()
        val get_UserID: String = myDownloadClass.getString(Constants.getSavedCLOImPutFiled, "").toString()
        val get_LicenseKey: String = myDownloadClass.getString(Constants.getSaveSubFolderInPutFiled, "").toString()

        val originalUrl = "$get_tMaster/$get_UserID/$get_LicenseKey/"

        Log.d("SOLOMON", "extractFolderAndFile: $originalUrl")

        // val baseUrlPattern = "https?://cp\\.cloudappserver\\.co\\.uk/app_base/public/CLO/DE_MO_2021001/?".toRegex()

        val baseUrlPattern = originalUrl.toRegex()

        // Remove base URL (handling variations)
        val relativePath = url.replace(baseUrlPattern, "")

        // Ensure we are working within the "App" folder
        val appIndex = relativePath.indexOf("App/")
        if (appIndex == -1) return "" // Return empty if "App" folder is missing

        // Extract everything after "App/"
        val appRelativePath = relativePath.substring(appIndex)

        // Ensure it starts with "/"
        return "/$appRelativePath"
    }



    private fun onAllFilesProcessed() {

        dnFailedViewModel.readAllData.observe(this@ReSyncActivity, Observer { files ->
            if (files.isNotEmpty()) {
                Log.d(TAG_RSYC, "Fetched ${files.size} files.")
            } else {
                Log.d(TAG_RSYC, "No files found to process.")
            }
        })

        handler.postDelayed(Runnable {
            if (customProgressDialog != null){
                customProgressDialog.dismiss()
            }
            showSortingFilesPopUp()

        }, 500)


    }


    // init single Download
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun startDownloadSingles(baseUrl: String, Zip: String, fileNamy: String) {

        val Extracted = Constants.App
        val threeFolderPath = "/MANUAL/DEMO/$Zip"



        if (binding.imagSwtichEnableConfigFileOnline.isChecked && !isCompltedAndReady) {
            check_for_valid_confileUrl()

        } else {
            binding.apply {


                val getSyncMethods = sharedBiometric.getString(Constants.IMG_SELECTED_SYNC_METHOD, "").toString()

                if (getSyncMethods == Constants.USE_ZIP_SYNC) {

                    binding.apply {


                        download(baseUrl, "MANUAL", "DEMO", Zip, fileNamy, Extracted, threeFolderPath)

                        val editor = myDownloadClass.edit()
                        editor.putString(Constants.getFolderClo, "MANUAL")
                        editor.putString(Constants.getFolderSubpath, "DEMO")
                        editor.putString(Constants.Zip, Zip)
                        editor.putString(Constants.fileName, fileNamy)
                        editor.putString(Constants.Extracted, Extracted)
                        editor.putString(Constants.baseUrl, baseUrl)
                        editor.apply()

                    }


                } else if (getSyncMethods == Constants.USE_API_SYNC) {

                    callApiClassActivity(baseUrl, "CLO", "MANUAL/DEMO", Zip, fileNamy, Extracted, threeFolderPath)

                } else if (getSyncMethods == Constants.USE_PARSING_SYNC) {

                    startParsingActivity(
                        baseUrl,
                        "CLO",
                        "MANUAL/DEMO",
                        Zip,
                        fileNamy,
                        Extracted,
                        threeFolderPath
                    )


                }


                /// similar but used on under second cancel downoad in danwload pager
                val editor = myDownloadClass.edit()
                editor.putString(Constants.getFolderClo, "CLO")
                editor.putString(Constants.getFolderSubpath, "MANUAL/DEMO")
                editor.putString(Constants.Zip, Zip)
                editor.putString(Constants.fileName, fileNamy)
                editor.putString(Constants.Extracted, Extracted)
                editor.putString(Constants.baseUrl, baseUrl)


                val editText88 = sharedBiometric.edit()

                if (binding.imagSwtichEnableLaucngOnline.isChecked) {

                    editText88.putString(
                        Constants.get_Launching_State_Of_WebView,
                        Constants.launch_WebView_Online_Manual_Index
                    )
                    editText88.apply()

                } else {

                    editText88.putString(
                        Constants.get_Launching_State_Of_WebView,
                        Constants.launch_WebView_Offline_Manual_Index
                    )
                    editText88.apply()

                }




                editor.apply()
            }
        }


    }


    private fun showToastMessage(messages: String) {

        try {
            Toast.makeText(applicationContext, messages, Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
        }
    }

    private fun showToastMessageLong(messages: String) {

        try {
            Toast.makeText(applicationContext, messages, Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
        }
    }


    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }



    @RequiresApi(Build.VERSION_CODES.Q)
    private fun download(
        url: String,
        getFolderClo: String,
        getFolderSubpath: String,
        Zip: String,
        fileNamy: String,
        Extracted: String,
        threeFolderPath: String,
    ) {
        // 1. Safer delete logic using app-private path
        val baseDir = getExternalFilesDir(null)
        val deletePath = File(baseDir, "Syn2AppLive/$getFolderClo/$getFolderSubpath/$Zip/$fileNamy")
        delete(deletePath)

        handler.postDelayed({

            val finalFolderPath = "Syn2AppLive/$getFolderClo/$getFolderSubpath/$Zip"
            val zipFile = File(baseDir, "$finalFolderPath/$fileNamy")

            // 2. Save preferences
            with(myDownloadClass.edit()) {
                putString(Constants.getFolderClo, getFolderClo)
                putString(Constants.getFolderSubpath, getFolderSubpath)
                putString(Constants.Zip, Zip)
                putString("fileNamy", fileNamy)
                putString(Constants.Extracted, Extracted)
                remove(Constants.Manage_My_Sync_Start)

                val savedInterval = myDownloadClass.getLong(Constants.getTimeDefined, 0)
                putLong(Constants.getTimeDefined, if (savedInterval != 0L) savedInterval else Constants.t_5min)
                apply()
            }

            // 3. Ensure folders exist
            val folder = File(baseDir, finalFolderPath)
            if (!folder.exists()) folder.mkdirs()

            // 4. Download with safe destination URI
            val request = DownloadManager.Request(Uri.parse(url))
            request.setTitle(fileNamy)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationUri(Uri.fromFile(zipFile))  // ✅ safe private storage

            val managerDownload = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val downloadReferenceMain = managerDownload.enqueue(request)

            myDownloadClass.edit().putLong(Constants.downloadKey, downloadReferenceMain).apply()

            // 5. Move to unzip/next activity
            val intent = Intent(applicationContext, DownlodZipActivity::class.java).apply {
                putExtra(Constants.baseUrl, url)
                putExtra(Constants.getFolderClo, getFolderClo)
                putExtra(Constants.getFolderSubpath, getFolderSubpath)
                putExtra(Constants.Zip, Zip)
                putExtra(Constants.fileName, fileNamy)
                putExtra(Constants.Extracted, Extracted)
                putExtra(Constants.threeFolderPath, threeFolderPath)
            }

            startActivity(intent)
            finish()

            sharedBiometric.edit().apply()

            customProgressDialog?.dismiss()

        }, 3000)
    }




//    @RequiresApi(Build.VERSION_CODES.Q)
//    private fun download(
//        url: String,
//        getFolderClo: String,
//        getFolderSubpath: String,
//        Zip: String,
//        fileNamy: String,
//        Extracted: String,
//        threeFolderPath: String,
//    ) {
//
//
//        //  val DeleteFolderPath = "/$getFolderClo/$getFolderSubpath/"
//
//        val DeleteFolderPath = "/$getFolderClo/$getFolderSubpath/$Zip/$fileNamy"
//
//        val directoryPath =
//            Environment.getExternalStorageDirectory().absolutePath + "/Download/${Constants.Syn2AppLive}$DeleteFolderPath"
//        val file = File(directoryPath)
//        delete(file)
//
//
//
//        handler.postDelayed(Runnable {
//
//            val finalFolderPath = "/$getFolderClo/$getFolderSubpath/$Zip"
//            val Syn2AppLive = "Syn2AppLive"
//
//            val editior = myDownloadClass.edit()
//            editior.putString(Constants.getFolderClo, getFolderClo)
//            editior.putString(Constants.getFolderSubpath, getFolderSubpath)
//            editior.putString(Constants.Zip, Zip)
//            editior.putString("fileNamy", fileNamy)
//            editior.putString(Constants.Extracted, Extracted)
//
//            // used to control Sync Start from  set up page
//            editior.remove(Constants.Manage_My_Sync_Start)
//
//
//            val get_savedIntervals = myDownloadClass.getLong(Constants.getTimeDefined, 0)
//
//            if (get_savedIntervals != 0L) {
//                editior.putLong(Constants.getTimeDefined, get_savedIntervals)
//
//            } else {
//                editior.putLong(Constants.getTimeDefined, Constants.t_5min)
//
//            }
//
//            editior.apply()
//
//
//            val managerDownload = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
//
//            val folder = File(Environment.getExternalStorageDirectory()
//                    .toString() + "/Download/$Syn2AppLive/$finalFolderPath"
//            )
//
//            if (!folder.exists()) {
//                folder.mkdirs()
//            }
//
//            val request = DownloadManager.Request(Uri.parse(url))
//            //  request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
//            request.setTitle(fileNamy)
//            request.allowScanningByMediaScanner()
//            request.setDestinationInExternalPublicDir(
//                Environment.DIRECTORY_DOWNLOADS, "/$Syn2AppLive/$finalFolderPath/$fileNamy"
//            )
//            val downloadReferenceMain = managerDownload.enqueue(request)
//
//            val editor = myDownloadClass.edit()
//            editor.putLong(Constants.downloadKey, downloadReferenceMain)
//            editor.apply()
//
//
//            val intent = Intent(applicationContext, DownlodZipActivity::class.java)
//            intent.putExtra(Constants.baseUrl, url)
//            intent.putExtra(Constants.getFolderClo, getFolderClo)
//            intent.putExtra(Constants.getFolderSubpath, getFolderSubpath)
//            intent.putExtra(Constants.Zip, Zip)
//            intent.putExtra(Constants.fileName, fileNamy)
//            intent.putExtra(Constants.Extracted, Extracted)
//
//            intent.putExtra(Constants.threeFolderPath, threeFolderPath)
//            intent.putExtra(Constants.baseUrl, url)
//            startActivity(intent)
//            finish()
//
//
//            val editor222 = sharedBiometric.edit()
//            //  editor222.putString(Constants.showDownloadSyncStatus, "showDownloadSyncStatus")
//            editor222.apply()
//
//            if (customProgressDialog != null){
//                customProgressDialog.dismiss()
//            }
//
//        }, 3000)
//
//
//    }


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


    override fun onItemClicked(photo: User) {


        val clo = photo.CLO.toString()
        val demo = photo.DEMO
        val editurl = photo.EditUrl
        val editurlAppIndex = photo.EditUrlIndex

        if (!clo.isNullOrEmpty()) {
            binding.editTextCLOpath.setText(clo)
            fil_CLO = clo

            binding.textLauncheSaveDownload.visibility = View.VISIBLE

        }

        if (!demo.isNullOrEmpty()) {
            binding.editTextSubPathFolder.setText(demo)
            fil_DEMO = demo

            binding.textLauncheSaveDownload.visibility = View.VISIBLE

        }




        if (!editurl.isNullOrEmpty()) {
            binding.editTextInputSynUrlZip.setText(editurl)
            fil_baseUrl = editurl

            binding.textLauncheSaveDownload.visibility = View.VISIBLE
        }




        if (!editurlAppIndex.isNullOrEmpty()) {
            binding.editTextInputIndexManual.setText(editurlAppIndex)
            fil_appIndex = editurlAppIndex

            binding.textLauncheSaveDownload.visibility = View.VISIBLE
        }

        handler.postDelayed(Runnable {
            InitWebviewIndexFileState()
        },200)


        customSavedDownloadDialog.dismiss()
    }




    private fun InitWebviewIndexFileState() {
        val filename = "/index.html"
        lifecycleScope.launch {
            loadIndexFileIfExist(fil_CLO, fil_DEMO, filename)
        }

    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun loadIndexFileIfExist(
        CLO: String,
        DEMO: String,
        fileName: String
    ) {
        lifecycleScope.launch {
            try {

                val filePath = withContext(Dispatchers.IO) {
                    try {
                        getFilePath(CLO, DEMO, fileName)
                    } catch (e: Exception) {
                        showToastMessage("You need to Sync Files for Offline Usage")
                        null
                    }
                }

                // Now back on the main thread to update the UI
                isIndexFileAvaliable = filePath != null


            } catch (e: Exception) {
                showToastMessage("You need to Sync Files for Offline Usage")
            }
        }
    }


    private fun getFilePath(CLO: String, DEMO: String, filename: String): String? {
        val baseDir = getExternalFilesDir(null)  // ✅ App-private scoped external storage
        val relativePath = "${Constants.Syn2AppLive}/$CLO/$DEMO/${Constants.App}"
        val destinationFolder = File(baseDir, relativePath)
        val myFile = File(destinationFolder, filename)

        return if (myFile.exists()) {
         //   myFile.toURI().toString()  // Use proper file URI (e.g. file:///...)
            myFile.toURI().toURL().toString()
        } else {
            null
        }
    }




    @SuppressLint("MissingInflatedId", "SetTextI18n")
    private fun showPopForLaunch_Oblin_offline() {
        val bindingCM: CustomSelectLauncOrOfflinePopLayoutBinding =
            CustomSelectLauncOrOfflinePopLayoutBinding.inflate(
                layoutInflater
            )
        val builder = AlertDialog.Builder(this)
        builder.setView(bindingCM.root)
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setCancelable(false)
        if (alertDialog.window != null) {
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        val textLaunchMyOnline: TextView = bindingCM.textLaunchMyOnline
        val textLaunchMyOffline: TextView = bindingCM.textLaunchMyOffline
        val textErrorUnableTo: TextView = bindingCM.textErrorUnableTo
        val imgCloseDialog: ImageView = bindingCM.imageCrossClose
        val close_bs: ImageView = bindingCM.closeBs

        val userPath = "Username-$fil_CLO\nlicense-$fil_DEMO"

        bindingCM.textDescription.text = userPath

        if (isIndexFileAvaliable){
            textErrorUnableTo.visibility = View.GONE
            textLaunchMyOnline.visibility = View.VISIBLE
            textLaunchMyOffline.visibility = View.VISIBLE
        }else{
            textLaunchMyOffline.visibility = View.GONE
            textErrorUnableTo.visibility = View.VISIBLE
            textLaunchMyOnline.visibility = View.VISIBLE
        }

        textLaunchMyOnline.setOnClickListener {


            binding.apply {

                textLunchOnline.text = "Online"


                val editor = myDownloadClass.edit()
                val imagSwtichEnableManualOrNot =
                    sharedBiometric.getString(Constants.imagSwtichEnableManualOrNot, "")
                        .toString()

                if (imagSwtichEnableManualOrNot.equals(Constants.imagSwtichEnableManualOrNot)) {

                    // Use manual

                    if (fil_baseUrl.isNotEmpty() && fil_appIndex.isNotEmpty()) {

                        val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
                        val CP_AP_MASTER_DOMAIN = myDownloadClass.getString(Constants.CP_OR_AP_MASTER_DOMAIN, "").toString()

                        // this url does not affect the Outcome of Master Domain
                        val url = "${CP_AP_MASTER_DOMAIN}/$fil_CLO/$fil_DEMO/App/index.html"

                        imagSwtichEnableLaucngOnline.isChecked = true


                        //   editor.putString(Constants.imgAllowLunchFromOnline, "imgAllowLunchFromOnline")

                        editor.putString(Constants.getSavedEditTextInputSynUrlZip, fil_baseUrl)
                        editor.putString(
                            Constants.getSaved_manaul_index_edit_url_Input, fil_appIndex
                        )
                        editor.putString(Constants.syncUrl, url)
                        editor.putString(
                            Constants.Tapped_OnlineORoffline, Constants.tapped_launchOnline
                        )
                        editor.apply()


                        val editText88 = sharedBiometric.edit()
                        editText88.putString(
                            Constants.get_Launching_State_Of_WebView,
                            Constants.launch_WebView_Online_Manual_Index
                        )
                        editText88.apply()


                        val intent = Intent(applicationContext, WebViewPage::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        showToastMessage("Select Saved Download Path")
                    }


                } else {

                    // Do Not use Manual


                    if (fil_CLO.isNotEmpty() && fil_DEMO.isNotEmpty()) {

                        val myDownloadClass =
                            getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
                        val CP_AP_MASTER_DOMAIN =
                            myDownloadClass.getString(Constants.CP_OR_AP_MASTER_DOMAIN, "")
                                .toString()

                        // this url does not affect the Outcome of Master Domain
                        val url = "${CP_AP_MASTER_DOMAIN}/$fil_CLO/$fil_DEMO/App/index.html"

                        imagSwtichEnableLaucngOnline.isChecked = true


                        //  editor.putString(Constants.imgAllowLunchFromOnline, "imgAllowLunchFromOnline")

                        editor.putString(Constants.getFolderClo, fil_CLO)
                        editor.putString(Constants.getFolderSubpath, fil_DEMO)
                        editor.putString(Constants.syncUrl, url)
                        editor.putString(
                            Constants.Tapped_OnlineORoffline, Constants.tapped_launchOnline
                        )
                        editor.apply()


                        val editText88 = sharedBiometric.edit()
                        editText88.putString(
                            Constants.get_Launching_State_Of_WebView,
                            Constants.launch_WebView_Online
                        )
                        editText88.apply()


                        val intent = Intent(applicationContext, WebViewPage::class.java)
                        startActivity(intent)
                        finish()


                    } else {
                        showToastMessage("Select Saved Download Path")
                    }


                }



                alertDialog.dismiss()
            }

        }



        textLaunchMyOffline.setOnClickListener {

            binding.apply {

                textLunchOnline.text = "Offline"

                val editor = myDownloadClass.edit()

                val imagSwtichEnableManualOrNot =
                    sharedBiometric.getString(Constants.imagSwtichEnableManualOrNot, "")
                        .toString()

                if (imagSwtichEnableManualOrNot.equals(Constants.imagSwtichEnableManualOrNot)) {


                    if (fil_baseUrl.isNotEmpty() && fil_appIndex.isNotEmpty()) {

                        val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
                        val CP_AP_MASTER_DOMAIN = myDownloadClass.getString(Constants.CP_OR_AP_MASTER_DOMAIN, "").toString()

                        // this url does not affect the Outcome of Master Domain
                        val url = "${CP_AP_MASTER_DOMAIN}/$fil_CLO/$fil_DEMO/App/index.html"

                        imagSwtichEnableLaucngOnline.isChecked = false

                        // editor.putString(Constants.imgAllowLunchFromOnline, "imgAllowLunchFromOnline")

                        editor.putString(Constants.getSavedEditTextInputSynUrlZip, fil_baseUrl)
                        editor.putString(
                            Constants.getSaved_manaul_index_edit_url_Input, fil_appIndex
                        )
                        editor.putString(Constants.syncUrl, url)
                        editor.putString(
                            Constants.Tapped_OnlineORoffline, Constants.tapped_launchOffline
                        )
                        editor.apply()


                        val editText88 = sharedBiometric.edit()
                        editText88.putString(
                            Constants.get_Launching_State_Of_WebView,
                            Constants.launch_WebView_Offline
                        )
                        editText88.apply()


                        val intent = Intent(applicationContext, WebViewPage::class.java)
                        startActivity(intent)
                        finish()


                    } else {
                        showToastMessage("Select Saved Download Path")
                    }


                } else {

                    // Do not use manual
                    if (fil_CLO.isNotEmpty() && fil_DEMO.isNotEmpty()) {

                        val myDownloadClass =
                            getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
                        val CP_AP_MASTER_DOMAIN =
                            myDownloadClass.getString(Constants.CP_OR_AP_MASTER_DOMAIN, "")
                                .toString()

                        // this url does not affect the Outcome of Master Domain
                        val url = "${CP_AP_MASTER_DOMAIN}/$fil_CLO/$fil_DEMO/App/index.html"

                        imagSwtichEnableLaucngOnline.isChecked = false

                        //  editor.putString(Constants.imgAllowLunchFromOnline, "imgAllowLunchFromOnline")

                        editor.putString(Constants.getFolderClo, fil_CLO)
                        editor.putString(Constants.getFolderSubpath, fil_DEMO)
                        editor.putString(Constants.syncUrl, url)
                        editor.putString(Constants.Tapped_OnlineORoffline, Constants.tapped_launchOffline)
                        editor.apply()

                        val editText88 = sharedBiometric.edit()
                        editText88.putString(Constants.get_Launching_State_Of_WebView, Constants.launch_WebView_Offline_Manual_Index)
                        editText88.apply()


                        val intent = Intent(applicationContext, WebViewPage::class.java)
                        startActivity(intent)
                        finish()


                    } else {
                        showToastMessage("Select Saved Download Path")
                    }


                }



                alertDialog.dismiss()
            }
        }


        imgCloseDialog.setOnClickListener { alertDialog.dismiss() }

        close_bs.setOnClickListener { alertDialog.dismiss() }

        alertDialog.show()
    }


    @SuppressLint("SetTextI18n")
    private fun show_API_Urls() {
        custom_ApI_Dialog = Dialog(this)
        val bindingCm = CustomApiUrlLayoutBinding.inflate(LayoutInflater.from(this))
        custom_ApI_Dialog.setContentView(bindingCm.root)
        custom_ApI_Dialog.setCancelable(true)
        custom_ApI_Dialog.setCanceledOnTouchOutside(true)
        custom_ApI_Dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val textErrorText = bindingCm.textErrorText
        val textTryAgin = bindingCm.textTryAgin
        val progressBar2 = bindingCm.progressBar2


        if (handlerMoveToWebviewPage != null) {
            handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
        }


        if (isNetworkAvailable()) {
            bindingCm.progressBar2.visibility = View.VISIBLE
            bindingCm.textTryAgin.visibility = View.GONE
            bindingCm.textErrorText.visibility = View.GONE


            val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
            val CP_AP_MASTER_DOMAIN = myDownloadClass.getString(Constants.CP_OR_AP_MASTER_DOMAIN, "").toString()
            val getFolderClo = myDownloadClass.getString(Constants.getFolderClo, "").toString()
            val getFolderSubpath = myDownloadClass.getString(Constants.getFolderSubpath, "").toString()
            val customJsonUrl = "$CP_AP_MASTER_DOMAIN/$getFolderClo/$getFolderSubpath/DOM/Custom.Json/"

            mApiViewModel.fetchApiUrls(customJsonUrl)

        } else {
            bindingCm.apply {
                progressBar2.visibility = View.GONE
                textErrorText.visibility = View.VISIBLE
                textTryAgin.visibility = View.VISIBLE
                textErrorText.text = "No Internet Connection"
            }

        }

        bindingCm.apply {


            recyclerApi.adapter = adapterApi
            recyclerApi.layoutManager = LinearLayoutManager(applicationContext)

            mApiViewModel.apiUrls.observe(this@ReSyncActivity, Observer { apiUrls ->
                apiUrls?.let {
                    adapterApi.setData(it.DomainUrls)

                    if (it.DomainUrls.isNotEmpty()) {
                        textErrorText.visibility = View.GONE
                        progressBar2.visibility = View.GONE
                        textTryAgin.visibility = View.GONE

                    } else {
                        textErrorText.visibility = View.VISIBLE
                        textTryAgin.visibility = View.VISIBLE
                        textErrorText.text = "Opps! No Data Found"
                    }

                }
            })


            imageCrossClose.setOnClickListener {
                custom_ApI_Dialog.dismiss()
            }

            closeBs.setOnClickListener {
                custom_ApI_Dialog.dismiss()
            }


            textTryAgin.setOnClickListener {
                if (isNetworkAvailable()) {
                    bindingCm.progressBar2.visibility = View.VISIBLE
                    bindingCm.textTryAgin.visibility = View.GONE
                    bindingCm.textErrorText.visibility = View.GONE

                    val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
                    val CP_AP_MASTER_DOMAIN = myDownloadClass.getString(Constants.CP_OR_AP_MASTER_DOMAIN, "").toString()
                    val getFolderClo = myDownloadClass.getString(Constants.getFolderClo, "").toString()
                    val getFolderSubpath = myDownloadClass.getString(Constants.getFolderSubpath, "").toString()
                    val customJsonUrl = "$CP_AP_MASTER_DOMAIN/$getFolderClo/$getFolderSubpath/DOM/Custom.Json/"

                    mApiViewModel.fetchApiUrls(customJsonUrl)

                } else {
                    bindingCm.apply {
                        progressBar2.visibility = View.GONE
                        textErrorText.visibility = View.VISIBLE
                        textTryAgin.visibility = View.VISIBLE
                        textErrorText.text = "No Internet Connection"
                        showToastMessage("No Internet Connection")
                    }

                }
            }

        }


        custom_ApI_Dialog.show()

    }

    override fun onItemClicked(domainUrl: DomainUrl) {

        val name = domainUrl.name + ""
        val urls = domainUrl.url + ""
        if (name.isNotEmpty()) {
            binding.texturlsViews.text = name
        }

        // Note - later you can use the url as well , the  name is displayed on textview
        if (name.isNotEmpty() && urls.isNotEmpty()) {
            val editor = myDownloadClass.edit()
            editor.putString(Constants.Saved_Domains_Name, name)
            editor.putString(Constants.Saved_Domains_Urls, urls)
            editor.apply()

            if (handlerMoveToWebviewPage != null) {
                handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
            }

        }


        custom_ApI_Dialog.dismiss()
    }

    private fun second_cancel_download() {
        try {

            val download_ref: Long = myDownloadClass.getLong(Constants.downloadKey, -15)

            val getFolderClo = myDownloadClass.getString(Constants.getFolderClo, "").toString()
            val getFolderSubpath =
                myDownloadClass.getString(Constants.getFolderSubpath, "").toString()
            val Zip = myDownloadClass.getString(Constants.Zip, "").toString()
            val fileName = myDownloadClass.getString(Constants.fileName, "").toString()


            val finalFolderPath = "/$getFolderClo/$getFolderSubpath/$Zip/$fileName"

            val directoryPath =
                Environment.getExternalStorageDirectory().absolutePath + "/Download/${Constants.Syn2AppLive}/" + finalFolderPath

            val myFile = File(directoryPath, fileName)

            delete(myFile)


            if (download_ref != -15L) {
                val query = DownloadManager.Query()
                query.setFilterById(download_ref)
                val c =
                    (applicationContext.getSystemService(DOWNLOAD_SERVICE) as DownloadManager).query(
                        query
                    )
                if (c.moveToFirst()) {
                    manager!!.remove(download_ref)
                    val editor: SharedPreferences.Editor = myDownloadClass.edit()
                    editor.remove(Constants.downloadKey)
                    editor.apply()
                }

            }

        } catch (ignored: java.lang.Exception) {
        }
    }


    @SuppressLint("InflateParams", "SuspiciousIndentation", "SetTextI18n")
    private fun showSortingFilesPopUp() {
        val bindingCm: CustomSortFilesLayoutBinding = CustomSortFilesLayoutBinding.inflate(layoutInflater)
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setView(bindingCm.root)
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setCancelable(false)

        // Set the background of the AlertDialog to be transparent
        if (alertDialog.window != null) {
            alertDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }


        val textLoading = bindingCm.textLoading
        val textFilesCounts = bindingCm.textFilesCount
        val textFileInitCount = bindingCm.textFileInitCount
        val imgCloseDialog = bindingCm.imgCloseDialog
        val textPleaseWait = bindingCm.textPleaseWait
        val progressbar = bindingCm.progressBar2
        val progressBarInter = bindingCm.progressBarInter

        textLoading.visibility = View.INVISIBLE
        textFilesCounts.visibility = View.INVISIBLE
        textFileInitCount.visibility = View.INVISIBLE
        textPleaseWait.visibility = View.INVISIBLE
        imgCloseDialog.visibility = View.INVISIBLE
        imgCloseDialog.visibility = View.INVISIBLE


        bindingCm.apply {
            handler.postDelayed({
                val files = dnFailedViewModel.readAllData.value ?: emptyList()
                if (files.isEmpty()) {
                    textLoading.text = "No Files found"
                    textLoading.visibility = View.VISIBLE
                    imgCloseDialog.visibility = View.VISIBLE
                    progressBarInter.visibility = View.VISIBLE

                    Log.d("SyncProcess", "No files to process on button click.")
                } else {
                    textLoading.visibility = View.VISIBLE
                    textPleaseWait.visibility = View.VISIBLE
                    imgCloseDialog.visibility = View.VISIBLE
                    progressbar.visibility = View.VISIBLE
                    progressBarInter.visibility = View.INVISIBLE

                    processingJob = lifecycleScope.launch {
                        processFilesSequentially(progressbar, alertDialog, textFilesCounts, textFileInitCount, files)
                    }
                }
            }, 2000)

            imgCloseDialog.setOnClickListener {
                processingJob?.cancel()
                alertDialog.dismiss()
            }
        }

        alertDialog.show()
    }

    @SuppressLint("SetTextI18n")
    private suspend fun processFilesSequentially(
        progressbar:ProgressBar,
        alertDialog: androidx.appcompat.app.AlertDialog,
        textFilesCounts: TextView,
        textFileInitCount: TextView,
        files: List<DnFailedApi>
    ) {

        try {
            Log.d("SyncProcess", "Starting file synchronization process...")
            withContext(Dispatchers.IO) {
                for ((index, file) in files.withIndex()) {
                    if (!isActive) {
                        Log.d("SyncProcess", "Process canceled.")
                        return@withContext
                    }

                    processFile(file)

                    val percent = ((index + 1).toFloat() / files.size.toFloat() * 100).toInt()
                    val fileInts = "File ${index + 1} of ${files.size}"
                    withContext(Dispatchers.Main) {
                        textFilesCounts.text = "$percent%"
                        progressbar.progress = percent
                        textFileInitCount.text = fileInts
                        textFilesCounts.visibility = View.VISIBLE
                        textFileInitCount.visibility = View.VISIBLE
                    }

                    delay(500) // Simulate file processing
                }
            }

            withContext(Dispatchers.Main) {
                textFilesCounts.text = "100%"
                progressbar.progress = 100

                handler.postDelayed(Runnable {
                    val intent = Intent(applicationContext, DownloadApisFilesActivityParsing::class.java)
                    startActivity(intent)
                    finish()
                    alertDialog.dismiss()

                }, 1000)

                Log.d("SyncProcess", "File synchronization completed!")
            }
        }catch (e:Exception){
            Log.d(TAG_RSYC, "processFilesSequentially: ${e.message}")
        }
    }



    private suspend fun processFile(file: DnFailedApi) {
        try {
            val get_tMaster: String = myDownloadClass.getString(Constants.get_ModifiedUrl, "").toString()
            val get_UserID: String = myDownloadClass.getString(Constants.getSavedCLOImPutFiled, "").toString()
            val get_LicenseKey: String = myDownloadClass.getString(Constants.getSaveSubFolderInPutFiled, "").toString()

            val url = "$get_tMaster//$get_UserID/$get_LicenseKey/${file.FolderName}/${file.FileName}"
            val serverTimestamp = fetchServerTimestamp(url)
            val erro1 = "Failed to check file"
            if (serverTimestamp != erro1) {
                saveFile(file, serverTimestamp.toString())
            } else {
                saveFile(file, "Unable to fecth time")
            }
        }catch (e:Exception){
            Log.d(TAG_RSYC, "processFile: ${e.message}")
        }
    }

    private suspend fun fetchServerTimestamp(url: String): String? {
        return withContext(Dispatchers.IO) {
            FileChecker(url).checkFileChange()
        }
    }

    private suspend fun saveFile(file: DnFailedApi, fileTimeStamp: String) {
        try {
            withContext(Dispatchers.IO) {
                val newParsingApi = ParsingApi(
                    SN = file.SN,
                    FolderName = file.FolderName,
                    FileName = file.FileName,
                    FileTimeStamp = fileTimeStamp,
                    Status = "true"
                )

                parsingViewModel.addFiles(newParsingApi)
                Log.d(TAG_RSYC, "File saved: ${file.SN} :: ${file.FileName} with timestamp $fileTimeStamp")
            }
        }catch (e:Exception){
            Log.d(TAG_RSYC, "saveFile: ${e.message}")
        }
    }




    private fun loadBackGroundImage() {
        val sharedP = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
        val getFolderClo = sharedP.getString(Constants.getFolderClo, "").toString()
        val getFolderSubpath = sharedP.getString(Constants.getFolderSubpath, "").toString()

        val baseDir = getExternalFilesDir(null) // App-private external storage
        val relativePath = "Syn2AppLive/$getFolderClo/$getFolderSubpath/${Constants.App}/Config"
        val folder = File(baseDir, relativePath)
        val fileTypes = "app_background.png"
        val file = File(folder, fileTypes)

        if (file.exists()) {
            Glide.with(this).load(file).centerCrop().into(binding.backgroundImage)
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

