package sync2app.com.syncapplive.additionalSettings

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sync2app.com.syncapplive.R
import sync2app.com.syncapplive.WebViewPage
import sync2app.com.syncapplive.additionalSettings.autostartAppOncrash.Methods
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.additionalSettings.utils.Utility
import sync2app.com.syncapplive.databinding.ActivityDownlodPaggerBinding
import sync2app.com.syncapplive.databinding.LaucnOnlineDonloadPaggerBinding
import sync2app.com.syncapplive.databinding.ProgressDialogLayoutBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Objects
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


class DownlodZipActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDownlodPaggerBinding

    private var isValid = false


    var manager: DownloadManager? = null

    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private val myHandler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }


    private val sharedBiometric: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_BIOMETRIC,
            Context.MODE_PRIVATE
        )
    }

    private var customProgressDialog: Dialog? = null

    private var powerManager: PowerManager? = null
    private var wakeLock: PowerManager.WakeLock? = null


    private val sharedP: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.MY_DOWNLOADER_CLASS,
            Context.MODE_PRIVATE
        )
    }

    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
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
        binding = ActivityDownlodPaggerBinding.inflate(layoutInflater)
        setContentView(binding.root)


        applyOritenation()


        setUpFullScreenWindows()



        val get_imgToggleImageBackground = sharedBiometric.getString(Constants.imgToggleImageBackground, "").toString()
        val get_imageUseBranding = sharedBiometric.getString(Constants.imageUseBranding, "")
        if (get_imgToggleImageBackground.equals(Constants.imgToggleImageBackground) && get_imageUseBranding.equals(Constants.imageUseBranding) ){
            loadBackGroundImage()
        }


        //add exception
        Methods.addExceptionHandler(this)

        powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager!!.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YourApp::MyWakelockTag")
        wakeLock!!.acquire()

        manager = getApplicationContext().getSystemService(DOWNLOAD_SERVICE) as DownloadManager


        binding.apply {
            closeBs.setOnClickListener {
                second_cancel_download()

            }




            binding.textLaunchApplication.setOnClickListener {

                showPopLauchOnline()
            }



            imagePauseDownload.setOnClickListener {
                pauseDownload()
                showToastMessage("Paused")
            }


            imageResumeDownload.setOnClickListener {
                resumeDownload()
                showToastMessage("Please wait")

            }


            textCancelBtn.setOnClickListener {
                second_cancel_download()

            }




            val getFolderClo = sharedP.getString("getFolderClo", "").toString()
            val getFolderSubpath = sharedP.getString("getFolderSubpath", "").toString()
            val Zip = sharedP.getString("Zip", "").toString()
            val fileName = sharedP.getString("fileName", "").toString()
            val Extracted = sharedP.getString("Extracted", "").toString()


            val threeFolderPath = intent.getStringExtra("threeFolderPath").toString()
            val baseUrl = intent.getStringExtra("baseUrl").toString()

            if (threeFolderPath !=null &&baseUrl !=null){
                textTitleFileName.text = fileName.toString()
                textPathFolderName.text = "$getFolderClo/$getFolderSubpath"
            }

            textRetryBtn.setOnClickListener {
                showToastMessage("Please wait..")
                if (baseUrl != null && fileName != null){
                    download(baseUrl, getFolderClo.toString(), getFolderSubpath.toString(), Zip.toString(), fileName.toString(), Extracted.toString(), threeFolderPath.toString())
                }else{
                    //   onBackPressed()
                    startActivity(Intent(applicationContext, ReSyncActivity::class.java))
                    finish()
                }
            }


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



    private val runnable: Runnable = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun run() {
            getDownloadStatus()
            myHandler.postDelayed(this, 500)
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SetTextI18n")
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
                val dl_progress = (bytes_downloaded.toDouble() / bytes_total.toDouble() * 100f).toInt()

                binding.progressBarPref.setProgress(dl_progress)

                binding.downloadBytes.setText(bytesIntoHumanReadable(
                    bytes_downloaded.toString().toLong()
                ) + "/" + bytesIntoHumanReadable(bytes_total.toString().toLong())
                )


                if (c == null) {
                    binding.textView10.setText(statusMessage(c))
                } else {
                    c.moveToFirst()
                    binding.textView10.setText(statusMessage(c))
                }
            }
        } catch (ignored: java.lang.Exception) {
        }
    }

    @SuppressLint("InflateParams", "SuspiciousIndentation")
    private fun showPopLauchOnline() {
        val bindingCm: LaucnOnlineDonloadPaggerBinding =
            LaucnOnlineDonloadPaggerBinding.inflate(
                layoutInflater
            )
        val builder = AlertDialog.Builder(this@DownlodZipActivity)
        builder.setView(bindingCm.getRoot())
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.setCancelable(true)

        if (alertDialog.window != null) {
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
        }


        val textCancel = bindingCm.textCancel
        val textYesButton = bindingCm.textYesButton


        bindingCm.apply {

            textCancel.setOnClickListener {
                alertDialog.dismiss()
            }


            textYesButton.setOnClickListener {
                val editor = sharedP.edit()
                editor.putString(Constants.imgAllowLunchFromOnline, "imgAllowLunchFromOnline")
                editor.apply()
                stratLauncOnline()
                alertDialog.dismiss()
            }


        }

        alertDialog.show()
    }






    @SuppressLint("Range", "SetTextI18n")
    private fun statusMessage(c: Cursor): String? {
        var msg: String
        when (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
            DownloadManager.STATUS_PENDING -> {
                msg = "Pending.."
                binding.imagePauseDownload.visibility = View.VISIBLE
                binding.imageResumeDownload.visibility = View.INVISIBLE
                isValid = true
            }

            DownloadManager.STATUS_RUNNING -> {
                msg = "Downloading.."
                binding.imagePauseDownload.visibility = View.VISIBLE
                binding.imageResumeDownload.visibility = View.INVISIBLE
                //  binding.textContinuPassword222222.isEnabled = false

                isValid = true

            }

            DownloadManager.STATUS_PAUSED -> {
                // msg = "Resume"
                msg = "Paused"
                binding.imagePauseDownload.visibility = View.INVISIBLE
                binding.imageResumeDownload.visibility = View.VISIBLE
                isValid = true
            }

            DownloadManager.STATUS_SUCCESSFUL -> {
                msg = "File fully downloaded"
                binding.imagePauseDownload.visibility = View.INVISIBLE
                binding.imageResumeDownload.visibility = View.VISIBLE
                binding.imagePauseDownload.isEnabled = false
                binding.imageResumeDownload.isEnabled = false

                val get_value_if_Api_is_required = sharedBiometric.getString(Constants.IMG_SELECTED_SYNC_METHOD, "").toString()

                if (isValid == true) {
                    isValid = false
                    showCustomProgressDialog("Please wait! \n Download in Progress")
                    handler.postDelayed(Runnable {
                        if (get_value_if_Api_is_required.equals(Constants.USE_ZIP_SYNC)){
                            funUnZipFile()
                        }else{
                            stratMyACtivity()
                            showToastMessage("Api Completed")
                        }


                    }, 250)
                } else {
                   // showToastMessage("Something went wrong")

                }


            }

            DownloadManager.STATUS_FAILED -> {
                msg = "Failed!, Retry.."
                isValid = false
            }

            else -> {
                msg = "failed! , try again.. "

            }
        }
        return msg
    }


    private fun showCustomProgressDialog(message: String) {
        try {
            customProgressDialog = Dialog(this)
            val binding: ProgressDialogLayoutBinding = ProgressDialogLayoutBinding.inflate(LayoutInflater.from(this))
            customProgressDialog!!.setContentView(binding.getRoot())
            customProgressDialog!!.setCancelable(false)
            customProgressDialog!!.setCanceledOnTouchOutside(false)
            customProgressDialog!!.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            customProgressDialog!!.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation

            binding.textLoading.text = "$message"



            customProgressDialog!!.show()
        } catch (_: Exception) {
        }
    }



    private fun bytesIntoHumanReadable(bytes: Long): String? {
        val kilobyte: Long = 1024
        val megabyte = kilobyte * 1024
        val gigabyte = megabyte * 1024
        val terabyte = gigabyte * 1024
        return if (bytes >= 0 && bytes < kilobyte) {
            "$bytes B"
        } else if (bytes >= kilobyte && bytes < megabyte) {
            (bytes / kilobyte).toString() + " KB"
        } else if (bytes >= megabyte && bytes < gigabyte) {
            (bytes / megabyte).toString() + " MB"
        } else if (bytes >= gigabyte && bytes < terabyte) {
            (bytes / gigabyte).toString() + " GB"
        } else if (bytes >= terabyte) {
            (bytes / terabyte).toString() + " TB"
        } else {
            bytes.toString() + ""
        }
    }




    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()

        try {
            if (myHandler != null) {
                myHandler!!.removeCallbacks(runnable)
            }

            getDownloadStatus()


            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);



        } catch (ignored: java.lang.Exception) {
        }

        if (myHandler != null) {
            myHandler.postDelayed(runnable, 500)
        }


    }


    override fun onPause() {
        super.onPause()
        try {
            if (myHandler != null) {
                myHandler.removeCallbacks(runnable)
            }
        } catch (ignored: java.lang.Exception) {
        }
    }


    override fun onStop() {
        super.onStop()
        try {
            if (myHandler != null) {
                myHandler!!.removeCallbacks(runnable)
            }

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
            if (myHandler != null) {
                myHandler!!.removeCallbacks(runnable)
            }


            val get_value_if_Api_is_required = sharedBiometric.getString(Constants.imagSwtichEnableSyncFromAPI, "")
            if (get_value_if_Api_is_required.equals(Constants.imagSwtichEnableSyncFromAPI)){
              //  second_cancel_download()
            }

            if (wakeLock != null && wakeLock!!.isHeld) {
                wakeLock!!.release()
            }


        } catch (e:Exception) {

            Log.d("DOOOM", "second_cancel_download2222: ${e.message.toString()}")
        }
    }



    private fun funUnZipFile() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val getFolderClo = sharedP.getString(Constants.getFolderClo, "").toString()
                val getFolderSubpath = sharedP.getString(Constants.getFolderSubpath, "").toString()
                val zipFileName = sharedP.getString("Zip", "").toString()
                val fileName = sharedP.getString("fileNamy", "").toString()
                val extractedFolder = sharedP.getString(Constants.Extracted, "").toString()

                val finalFolderPath = "/$getFolderClo/$getFolderSubpath/$zipFileName"
                val finalFolderPathDesired = "/$getFolderClo/$getFolderSubpath/$extractedFolder"

                val directoryPathString = Environment.getExternalStorageDirectory().absolutePath + "/Download/${Constants.Syn2AppLive}/" + finalFolderPath
                val destinationFolder = File(Environment.getExternalStorageDirectory().absolutePath + "/Download/${Constants.Syn2AppLive}/" + finalFolderPathDesired)

                if (!destinationFolder.exists()) {
                    destinationFolder.mkdirs()
                }

                val myFile = File(directoryPathString, File.separator + fileName)
                if (myFile.exists()) {
                    extractZip(myFile.absolutePath, destinationFolder.absolutePath)
                } else {
                    withContext(Dispatchers.Main) {
                        showToastMessage("ZIP file not found: $directoryPathString")
                        allExtractionCompleted()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showToastMessage("An error occurred: ${e.localizedMessage}")
                }
            }
        }
    }


    private fun extractZip(zipFilePath: String, destinationPath: String) {

        binding.textTitleFileName.text = Constants.Extracting

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val zipFile = File(zipFilePath)
                val totalSize = zipFile.length() // Total size of the ZIP file in bytes
                var processedSize = 0L // Bytes processed so far

                val buffer = ByteArray(1024)
                val zipInputStream = ZipInputStream(FileInputStream(zipFile))

                var entry: ZipEntry? = zipInputStream.nextEntry
                while (entry != null) {
                    val entryFile = File(destinationPath, entry.name)
                    if (entry.isDirectory) {
                        entryFile.mkdirs()
                    } else {
                        val parentDir = entryFile.parentFile
                        if (!parentDir.exists()) parentDir.mkdirs()

                        FileOutputStream(entryFile).use { outputStream ->
                            var len: Int
                            while (zipInputStream.read(buffer).also { len = it } > 0) {
                                outputStream.write(buffer, 0, len)
                                processedSize += len

                                // Update progress on the main thread
                                val progress = ((processedSize.toDouble() / totalSize) * 100).toInt()
                                withContext(Dispatchers.Main) {
                                    binding.progressBarPref.progress = progress
                                }
                            }
                        }
                    }

                    MediaScannerConnection.scanFile(applicationContext, arrayOf(entryFile.absolutePath), null) { path, uri ->
                        Log.d("MediaScanner", "Scanned $path -> $uri")
                        runOnUiThread {
                            binding.textPathFolderName.text ="$uri"
                        }
                    }

                    entry = zipInputStream.nextEntry
                }

                zipInputStream.close()

                withContext(Dispatchers.Main) {
                    showToastMessage("Extraction completed successfully.")
                    allExtractionCompleted()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showToastMessage("Error during extraction: ${e.localizedMessage}")
                    allExtractionCompleted()
                }
            }
        }
    }



    private fun allExtractionCompleted() {
        binding.progressBarPref.progress = 100
        showToastMessage(Constants.media_ready)
        stratMyACtivity()
    }


    private fun stratMyACtivity() {
        try {
            handler.postDelayed(Runnable {
                if (customProgressDialog !=null){
                    customProgressDialog!!.dismiss()
                }

                val editor = sharedBiometric.edit()
                editor.remove(Constants.CALL_RE_SYNC_MANGER)
                editor.apply()

                val intent = Intent(applicationContext, WebViewPage::class.java)
                startActivity(intent)
                finish()
            }, 1000)


        }catch (_:Exception){}
    }

    @SuppressLint("SuspiciousIndentation")
    private fun stratLauncOnline() {
        try {


            val getFolderClo = sharedP.getString("getFolderClo", "").toString()
            val getFolderSubpath = sharedP.getString("getFolderSubpath", "").toString()

            val editor = sharedP.edit()


            editor.putString(Constants.getFolderClo, getFolderClo)
            editor.putString(Constants.getFolderSubpath, getFolderSubpath)
            editor.apply()

            val imagSwtichEnableManualOrNot = sharedBiometric.getString(Constants.imagSwtichEnableManualOrNot, "")
            if (imagSwtichEnableManualOrNot.equals(Constants.imagSwtichEnableManualOrNot)) {
                val editText88 = sharedBiometric.edit()
                editText88.putString(Constants.get_Launching_State_Of_WebView, Constants.launch_WebView_Online_Manual_Index)
                editText88.apply()
            }else{
                val editText88 = sharedBiometric.edit()
                editText88.putString(Constants.get_Launching_State_Of_WebView, Constants.launch_WebView_Online)
                editText88.apply()
            }



            lifecycleScope.launch(Dispatchers.IO) {
                val getFolderClo = sharedP.getString("getFolderClo", "").toString()
                val getFolderSubpath = sharedP.getString("getFolderSubpath", "").toString()
                val Zip = sharedP.getString("Zip", "").toString()
                val fileName = sharedP.getString("fileName", "").toString()

                val finalFolderPath = "/$getFolderClo/$getFolderSubpath/$Zip/$fileName"
                val directoryPath = Environment.getExternalStorageDirectory().absolutePath + "/Download/Syn2AppLive/" + finalFolderPath

                val myFile = File(directoryPath, fileName.toString())
                delete(myFile)

                withContext(Dispatchers.Main){

                    val editor = sharedBiometric.edit()
                    editor.remove(Constants.CALL_RE_SYNC_MANGER)
                    editor.apply()

                    val intent = Intent(applicationContext, WebViewPage::class.java)
                    startActivity(intent)
                    finish()
                }
            }


        }catch (_:Exception){}
    }


    private fun showToastMessage(messages: String) {

        try {
            Toast.makeText(applicationContext, messages, Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
        }
    }


    private fun pauseDownload(): Boolean {
        var updatedRow = 0
        val contentValues = ContentValues()
        contentValues.put("control", 1)
        try {
            val fileName = sharedP.getString("fileName", "")
            updatedRow = Objects.requireNonNull<Context>(applicationContext).contentResolver.update(
                Uri.parse("content://downloads/my_downloads"),
                contentValues,
                "title=?",
                arrayOf<String>(fileName.toString())
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return 0 < updatedRow
    }


    private fun resumeDownload(): Boolean {
        var updatedRow = 0
        val contentValues = ContentValues()
        contentValues.put("control", 0)
        try {
            val fileName = sharedP.getString("fileName", "")
            updatedRow = Objects.requireNonNull<Context>(applicationContext).contentResolver.update(
                Uri.parse("content://downloads/my_downloads"),
                contentValues,
                "title=?",
                arrayOf<String>(fileName.toString())
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return 0 < updatedRow
    }


    private fun second_cancel_download() {
        try {


            lifecycleScope.launch(Dispatchers.IO) {
                val getFolderClo = sharedP.getString("getFolderClo", "").toString()
                val getFolderSubpath = sharedP.getString("getFolderSubpath", "").toString()
                val Zip = sharedP.getString("Zip", "").toString()
                val fileName = sharedP.getString("fileName", "").toString()

                val finalFolderPath = "/$getFolderClo/$getFolderSubpath/$Zip/$fileName"
                val directoryPath = Environment.getExternalStorageDirectory().absolutePath + "/Download/Syn2AppLive/" + finalFolderPath

                val myFile = File(directoryPath, fileName.toString())
                delete(myFile)

                withContext(Dispatchers.Main){
                    startActivity(Intent(applicationContext, ReSyncActivity::class.java))
                    finish()
                }
            }



        } catch (ignored: java.lang.Exception) {
        }
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


        val DeleteFolderPath = "/$getFolderClo/$getFolderSubpath/"
        val directoryPath = Environment.getExternalStorageDirectory().absolutePath + "/Download/Syn2AppLive$DeleteFolderPath"
        val file = File(directoryPath)
        delete(file)



        handler.postDelayed(Runnable {

            val finalFolderPath = "/$getFolderClo/$getFolderSubpath/$Zip"
            val Syn2AppLive = "Syn2AppLive"

            val editior = sharedP.edit()
            editior.putString(Constants.getFolderClo, getFolderClo)
            editior.putString(Constants.getFolderSubpath, getFolderSubpath)
            editior.putString("Zip", Zip)
            editior.putString("fileNamy", fileNamy)
            editior.putString("Extracted", Extracted)
            editior.putString(Constants.baseUrl, url)

            editior.remove(Constants.PASS_URL)
            editior.apply()


            val managerDownload = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

            val folder = File(Environment.getExternalStorageDirectory().toString() + "/Download/$Syn2AppLive/$finalFolderPath")

            if (!folder.exists()) {
                folder.mkdirs()
            }

            val request = DownloadManager.Request(Uri.parse(url))
            //  request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            request.setTitle(fileNamy)
            request.allowScanningByMediaScanner()
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS, "/$Syn2AppLive/$finalFolderPath/$fileNamy"
            )
            val downloadReferenceMain = managerDownload.enqueue(request)

            val editor = sharedP.edit()
            editor.putLong(Constants.downloadKey, downloadReferenceMain)
            editor.apply()


        }, 1000)


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




    override fun onBackPressed() {
        second_cancel_download()
        super.onBackPressed()
    }

    private fun loadBackGroundImage() {

        val fileTypes = "app_background.png"
        val getFolderClo = sharedP.getString(Constants.getFolderClo, "").toString()
        val getFolderSubpath = sharedP.getString(Constants.getFolderSubpath, "").toString()

        val pathFolder = "/" + getFolderClo + "/" + getFolderSubpath + "/" + Constants.App + "/" + "Config"
        val folder =
            Environment.getExternalStorageDirectory().absolutePath + "/Download/${Constants.Syn2AppLive}/" + pathFolder
        val file = File(folder, fileTypes)

        if (file.exists()) {
            Glide.with(this).load(file).centerCrop().into(binding.backgroundImage)

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