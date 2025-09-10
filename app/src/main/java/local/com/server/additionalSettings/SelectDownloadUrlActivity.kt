package local.com.server.additionalSettings

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import local.com.server.additionalSettings.urlchecks.checkUrlExistence
import local.com.server.additionalSettings.utils.Constants
import local.com.server.additionalSettings.utils.Utility
import local.com.server.databinding.ActivitySelectDownloadUrlBinding
import local.com.server.databinding.ProgressValidateUserDialogLayoutBinding

class SelectDownloadUrlActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectDownloadUrlBinding
    private lateinit var customProgressDialog: Dialog

    private val sharedBiometric: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_BIOMETRIC,
            Context.MODE_PRIVATE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectDownloadUrlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.closeBs.setOnClickListener {
            navigateBack()
        }

        val baseUrl = sharedBiometric.getString("${Constants.baseUrl}", "").toString()


        lifecycleScope.launch {
            delay(300)
            if (baseUrl.isNotEmpty()){
                binding.editTextUrlZip.setText(baseUrl)
            }
        }


        binding.textStartDownload.setOnClickListener {
            binding.textStartDownload.isEnabled = false
            initDownload()
        }

    }


    @SuppressLint("SetTextI18n")
    private fun showCustomProgressDialog() {
        customProgressDialog = Dialog(this)
        val bindingVM = ProgressValidateUserDialogLayoutBinding.inflate(LayoutInflater.from(this))
        customProgressDialog.setContentView(bindingVM.root)
        customProgressDialog.setCancelable(true)
        customProgressDialog.setCanceledOnTouchOutside(false)
        customProgressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        customProgressDialog.show()

        bindingVM.textLoading.text = "Connecting.."

    }

    private fun initDownload(){
        val baseUrl = binding.editTextUrlZip.text.toString().trim()
        if (Utility.isNetworkAvailable(applicationContext) && baseUrl.isNotEmpty()) {
            showCustomProgressDialog()
            lifecycleScope.launch {
                val result = checkUrlExistence(baseUrl)
                if (result) {
                    withContext(Dispatchers.Main){
                        binding.textStartDownload.isEnabled = true
                        if (::customProgressDialog.isInitialized) {
                            customProgressDialog.dismiss()
                        }

                        val editor = sharedBiometric.edit()
                        editor.putString("${Constants.baseUrl}" , baseUrl)
                        editor.apply()

                        startActivity(Intent(applicationContext, DownlodZipActivity::class.java))
                        finish()
                    }

                } else {
                    binding.textStartDownload.isEnabled = true
                    showToastMessage("Invalid URL")
                    if (::customProgressDialog.isInitialized) {
                        customProgressDialog.dismiss()
                    }
                }
            }


        } else {
            binding.textStartDownload.isEnabled = true
            showToastMessage("Invalid URL or Internet Connection")
        }
    }

    private fun showToastMessage(message: String) {
        runOnUiThread {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Use OnBackPressedDispatcher instead")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        navigateBack()
    }


    private fun navigateBack() {
        startActivity(Intent(applicationContext, SeverEngineActivity::class.java))
        finish()
    }

}