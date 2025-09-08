package smarty.com.excel.additionalSettings

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
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import smarty.com.excel.R
import smarty.com.excel.additionalSettings.utils.Constants
import smarty.com.excel.databinding.ActivityDownlodPaggerBinding
import smarty.com.excel.databinding.ProgressDialogLayoutBinding
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

    private var hasUnzipped = false   // <-- add this at class level


    private var customProgressDialog: Dialog? = null

    private var powerManager: PowerManager? = null
    private var wakeLock: PowerManager.WakeLock? = null


    private val sharedBiometric: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_BIOMETRIC,
            Context.MODE_PRIVATE
        )
    }


    val getFolderClo = "CLO"
    val getFolderSubpath = "USER"
    val Zip = "Zip"
    val fileName = "App.zip"



    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("SetTextI18n", "WakelockTimeout", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownlodPaggerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)


        powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager!!.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YourApp::MyWakelockTag")
        wakeLock!!.acquire()

        manager = getApplicationContext().getSystemService(DOWNLOAD_SERVICE) as DownloadManager


        binding.linearLayout2.visibility = View.INVISIBLE

        binding.apply {
            closeBs.setOnClickListener {
                second_cancel_download()

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


            val baseUrl = sharedBiometric.getString("${Constants.baseUrl}", "").toString()
            download(baseUrl, getFolderClo.toString(), getFolderSubpath.toString(), Zip, fileName.toString())

            Log.d("download", "onCreate: $baseUrl")

            textRetryBtn.setOnClickListener {
                showToastMessage("Please wait..")
                download(baseUrl, getFolderClo.toString(), getFolderSubpath.toString(), Zip.toString(), fileName.toString())

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

            val download_ref = sharedBiometric.getLong(Constants.downloadKey, -15)
            val query = DownloadManager.Query()
            query.setFilterById(download_ref)
            val c = (applicationContext.getSystemService(DOWNLOAD_SERVICE) as DownloadManager).query(query)
            if (c!!.moveToFirst()) { @SuppressLint("Range") val bytes_downloaded = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)).toLong()
                @SuppressLint("Range") val bytes_total = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)).toLong()
                val dl_progress = (bytes_downloaded.toDouble() / bytes_total.toDouble() * 100f).toInt()
                binding.progressBarPref.setProgress(dl_progress)

                binding.downloadBytes.setText(bytesIntoHumanReadable(bytes_downloaded.toString().toLong()) + "/" + bytesIntoHumanReadable(bytes_total.toString().toLong()))
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

                if (!hasUnzipped) {   // ✅ only run once
                    hasUnzipped = true
                    showCustomProgressDialog("Unpacking Media ... ")
                    lifecycleScope.launch(Dispatchers.IO) {
                        val baseDir = getExternalFilesDir(null)
                        val extractToDir = File(
                            baseDir, "${Constants.Syn2AppLive}/$getFolderClo/$getFolderSubpath/${Constants.App}")

                        // delete old folder if exists
                        if (extractToDir.exists()) {
                            delete(extractToDir)
                        }

                        delay(5000) // still on IO thread
                        funUnZipFile() // directly suspending call
                    }

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


    private fun showCustomProgressDialog(message: String) {
        customProgressDialog = Dialog(this)
        val binding: ProgressDialogLayoutBinding = ProgressDialogLayoutBinding.inflate(LayoutInflater.from(this))
        customProgressDialog!!.setContentView(binding.getRoot())
        customProgressDialog!!.setCancelable(false)
        customProgressDialog!!.setCanceledOnTouchOutside(false)
        customProgressDialog!!.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        customProgressDialog!!.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
        binding.textLoading.text = "$message"
        customProgressDialog!!.show()

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

            if (wakeLock != null && wakeLock!!.isHeld) {
                wakeLock!!.release()
            }


        } catch (e:Exception) {

            Log.d("DOOOM", "second_cancel_download2222: ${e.message.toString()}")
        }
    }



    private fun startTheActivity() {
        lifecycleScope.launch {
            delay(1000)
            if (customProgressDialog !=null){
                customProgressDialog!!.dismiss()
            }

            val intent = Intent(applicationContext, DemoWebActivity::class.java)
            startActivity(intent)
            finish()
        }

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

            updatedRow = Objects.requireNonNull<Context>(applicationContext).contentResolver.update(
                Uri.parse("content://downloads/my_downloads"),
                contentValues,
                "title=?",
                arrayOf<String>("${Constants.fileNmae_App_Zip}")
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

            updatedRow = Objects.requireNonNull<Context>(applicationContext).contentResolver.update(
                Uri.parse("content://downloads/my_downloads"),
                contentValues,
                "title=?",
                arrayOf<String>("${Constants.fileNmae_App_Zip}")
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return 0 < updatedRow
    }



    private fun second_cancel_download() {
        try {
            lifecycleScope.launch(Dispatchers.IO) {
                val baseDir = getExternalFilesDir(null)
                val finalPath = File(baseDir, "Syn2AppLive/${Constants.CLO}/${Constants.USER}/${Constants.Zip}/${Constants.fileNmae_App_Zip}")

                if (finalPath.exists()) {
                    finalPath.delete()
                }

                withContext(Dispatchers.Main) {
                    startActivity(Intent(applicationContext, DE_MO_202100::class.java))
                    finish()
                }
            }

        } catch (ignored: Exception) {
        }
    }





    @RequiresApi(Build.VERSION_CODES.Q)
    private fun download(
        url: String,
        getFolderClo: String,
        getFolderSubpath: String,
        Zip: String,
        fileNamy: String,
    ) {
        val baseFolder = getExternalFilesDir(null)
        val targetDir = File(baseFolder, "${Constants.Syn2AppLive}/$getFolderClo/$getFolderSubpath/$Zip/")

        // Reset directory
        if (targetDir.exists()) {
            targetDir.deleteRecursively()
        }
        targetDir.mkdirs()

        handler.postDelayed({
            val zipFilePath = File(targetDir, fileNamy)  // ✅ Save as App.zip

            val request = DownloadManager.Request(Uri.parse(url))
            request.setTitle(fileNamy)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationUri(Uri.fromFile(zipFilePath))

            val managerDownload = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val downloadReferenceMain = managerDownload.enqueue(request)

            sharedBiometric.edit().putLong(Constants.downloadKey, downloadReferenceMain).apply()
        }, 1000)
    }



    private fun funUnZipFile() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val baseDir = getExternalFilesDir(null)
                val zipFilePath = File(baseDir, "${Constants.Syn2AppLive}/$getFolderClo/$getFolderSubpath/${Constants.Zip}/${Constants.fileNmae_App_Zip}")
                val extractToDir = File(baseDir, "${Constants.Syn2AppLive}/$getFolderClo/$getFolderSubpath/${Constants.App}")

                if (!extractToDir.exists()) extractToDir.mkdirs()

                if (zipFilePath.exists()) {
                    extractZip(zipFilePath.absolutePath, extractToDir.absolutePath)
                } else {
                    withContext(Dispatchers.Main) {
                        showToastMessage("ZIP file not found: ${zipFilePath.absolutePath}")
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

        lifecycleScope.launch {
            startTheActivity()
        }

    }


    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        second_cancel_download()
    }


}