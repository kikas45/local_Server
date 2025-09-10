package local.com.server.additionalSettings

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import local.com.server.R
import local.com.server.additionalSettings.utils.Constants
import local.com.server.databinding.ActivitySampleDemoV2Binding
import local.com.server.databinding.ProgressDialogLayoutBinding
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Objects
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ExtractZipActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySampleDemoV2Binding

    private var selectedZipUri: Uri? = null

    private val getFolderClo = "CLO"
    private val getFolderSubpath = "USER"
    private var customProgressDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySampleDemoV2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.closeBs.setOnClickListener {
            navigateBack()
        }

        binding.textPickZipFolder.setOnClickListener {
            pickZipFileLauncher.launch(arrayOf("application/zip"))
        }

        binding.textPickAnotherFile.setOnClickListener {
            pickZipFileLauncher.launch(arrayOf("application/zip"))
        }


        binding.textExtractZipFile.setOnClickListener {
            showCustomProgressDialog("Unpacking Media ... ")
            binding.contentLayoutHolder.visibility = View.VISIBLE
            binding.textDescription.visibility = View.INVISIBLE
            binding.textPickAnotherFile.visibility = View.GONE

            lifecycleScope.launch(Dispatchers.IO) {
                val baseDir = getExternalFilesDir(null)
                val extractToDir = File(
                    baseDir, "${Constants.Syn2AppLive}/$getFolderClo/$getFolderSubpath/${Constants.App}")

                // delete old folder if exists
                if (extractToDir.exists()) {
                    delete(extractToDir)
                }

                delay(1000) // still on IO thread
                funUnZipFile() // directly suspending call
            }
        }

    }



    @SuppressLint("MissingSuperCall")
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        navigateBack()
    }

    private fun navigateBack() {
        startActivity(Intent(this, SeverEngineActivity::class.java))
        finish()
    }


    //  * SAF picker for ZIP files.
    //  * Works on Android 10–14.
    private val pickZipFileLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                selectedZipUri = uri

                // Persist permission so the URI is usable even after process death
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    Log.w("ZipPicker", "Could not persist URI permission: ${e.message}")
                }

                binding.textTitleFileName.text = "Selected: $uri"
                binding.textTitleFileName.visibility = View.VISIBLE
            } else {
                showToastMessage("No file selected.")
            }
        }


    // Helper function to extract file name from URI
    private fun getFileNameFromUri(uri: Uri): String? {
        // 1. Try SAF metadata first
        var name: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }

        // 2. Fallback: extract from URI path
        if (name == null) {
            val path = uri.path
            if (path != null) {
                val cut = path.lastIndexOf('/')
                if (cut != -1) {
                    name = path.substring(cut + 1)
                }
            }
        }

        return name
    }


    //  * Starts extraction of the selected ZIP file.
    private fun funUnZipFile() {
        val uri = selectedZipUri
        if (uri == null) {
            showToastMessage("Please select a ZIP file first.")
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val baseDir = getExternalFilesDir(null)
                val extractToDir = File(
                    baseDir,
                    "${Constants.Syn2AppLive}/$getFolderClo/$getFolderSubpath/${Constants.App}"
                )
                if (!extractToDir.exists()) extractToDir.mkdirs()

                contentResolver.openInputStream(uri)?.use { inputStream ->
                    extractZip(inputStream, extractToDir.absolutePath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showToastMessage("Error: ${e.localizedMessage}")
                }
            }
        }
    }


    //  * Extracts a ZIP InputStream into destinationPath.

    private suspend fun extractZip(inputStream: InputStream, destinationPath: String) {
        withContext(Dispatchers.Main) {
            binding.textTitleFileName.text = Constants.Extracting
            binding.progressBarPref.progress = 0
            binding.progressBarPref.visibility = View.VISIBLE
            binding.textTitleFileName.visibility = View.VISIBLE
            binding.textPathFolderName.visibility = View.VISIBLE
            binding.textSingleFileName.visibility = View.VISIBLE
        }

        try {
            val buffer = ByteArray(1024)

            // --- Pass 1: Calculate total size of all entries ---
            var totalSize: Long = 0
            ZipInputStream(inputStream).use { zipInputStream ->
                var entry: ZipEntry? = zipInputStream.nextEntry
                while (entry != null) {
                    totalSize += entry.size.takeIf { it > 0 } ?: 0
                    entry = zipInputStream.nextEntry
                }
            }



            // Reset inputStream since we consumed it in first pass
            val newInputStream = contentResolver.openInputStream(selectedZipUri!!) ?: return

            // --- Pass 2: Actual extraction with progress ---
            var processedSize: Long = 0
            ZipInputStream(newInputStream).use { zipInputStream ->
                var entry: ZipEntry? = zipInputStream.nextEntry
                while (entry != null) {
                    val entryFile = File(destinationPath, entry!!.name)
                    if (entry!!.isDirectory) {
                        entryFile.mkdirs()
                    } else {
                        entryFile.parentFile?.takeIf { !it.exists() }?.mkdirs()

                        FileOutputStream(entryFile).use { outputStream ->
                            var len: Int
                            while (zipInputStream.read(buffer).also { len = it } > 0) {
                                outputStream.write(buffer, 0, len)
                                processedSize += len

                                if (totalSize > 0) {
                                    val progress =
                                        ((processedSize.toDouble() / totalSize) * 100).toInt()
                                    withContext(Dispatchers.Main) {
                                        binding.progressBarPref.progress = progress
                                    }
                                }
                            }
                        }
                    }

                    // ✅ Show file name on UI (on main thread)
                    withContext(Dispatchers.Main) {
                        binding.textSingleFileName.text = "Extracted: ${entry!!.name}"
                        binding.textExtractZipFile.text = "Running.."
                    }

                    // Optional: update MediaStore so file appears in gallery
                    MediaScannerConnection.scanFile(
                        applicationContext,
                        arrayOf(entryFile.absolutePath),
                        null
                    ) { path, uri ->
                        Log.d("MediaScanner", "Scanned $path -> $uri")
                    }

                    entry = zipInputStream.nextEntry
                }
            }

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


    private fun allExtractionCompleted() {
        binding.progressBarPref.progress = 100
        binding.textExtractZipFile.text = "Completed"
        showToastMessage(Constants.media_ready)

        lifecycleScope.launch {
            startTheActivity()
        }

    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        val uri = selectedZipUri
        if (uri == null) {
            binding.textPickZipFolder.visibility = View.VISIBLE
            binding.textExtractZipFile.visibility = View.INVISIBLE
            binding.contentLayoutHolder.visibility = View.INVISIBLE
            binding.textDescription.visibility = View.VISIBLE
            binding.textPickAnotherFile.visibility = View.GONE

        } else {

            // ✅ Get the display name instead of showing full URI
            val fileName = getFileNameFromUri(uri) ?: uri.lastPathSegment ?: "Unknown file"


            binding.textPickZipFolder.visibility = View.INVISIBLE
            binding.textExtractZipFile.visibility = View.VISIBLE
            binding.textDescription.text = """
            Great! You’ve selected a ZIP file called:
            
            $fileName
            
            Now, simply press the **Extract** button to unpack your media files, or Pick a New File.
        """.trimIndent()
            binding.textDescription.visibility = View.VISIBLE
            binding.textPickAnotherFile.visibility = View.VISIBLE
        }
    }


    private fun startTheActivity() {
        lifecycleScope.launch {
            delay(1000)
            if (customProgressDialog != null) {
                customProgressDialog!!.dismiss()
            }

            val intent = Intent(applicationContext, DemoWebActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun showCustomProgressDialog(message: String) {
        customProgressDialog = Dialog(this)
        val bindingVM: ProgressDialogLayoutBinding =
            ProgressDialogLayoutBinding.inflate(LayoutInflater.from(this))
        customProgressDialog!!.setContentView(bindingVM.getRoot())
        customProgressDialog!!.setCancelable(false)
        customProgressDialog!!.setCanceledOnTouchOutside(false)
        customProgressDialog!!.getWindow()!!
            .setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        customProgressDialog!!.window!!.attributes.windowAnimations =
            R.style.PauseDialogAnimation
        bindingVM.textLoading.text = "$message"
        customProgressDialog!!.show()
        bindingVM.imgCloseDialog.visibility = View.GONE

    }


    private fun showToastMessage(messages: String) {
        try {
            Toast.makeText(applicationContext, messages, Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
        }
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


}
