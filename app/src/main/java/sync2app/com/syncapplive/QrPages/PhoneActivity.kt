package sync2app.com.syncapplive.QrPages

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import sync2app.com.syncapplive.WebViewPage
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.databinding.ActivityPhoneBinding
import java.io.File

class PhoneActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPhoneBinding

    private val sharedBiometric: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_BIOMETRIC,
            Context.MODE_PRIVATE
        )
    }

    private val myDownloadClass: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.MY_DOWNLOADER_CLASS,
            Context.MODE_PRIVATE
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)



        val get_imgToggleImageBackground = sharedBiometric.getString(Constants.imgToggleImageBackground, "")
        val get_imageUseBranding = sharedBiometric.getString(Constants.imageUseBranding, "")
        if (get_imgToggleImageBackground.equals(Constants.imgToggleImageBackground) && get_imageUseBranding.equals(Constants.imageUseBranding) ){
            loadBackGroundImage()
        }



        val phoneNumber: String = sharedBiometric.getString("phoneNumber", "").toString()

        binding.textBodyMessage.text = phoneNumber


        binding.closeBs.setOnClickListener {
            endMyActivity()
        }


        binding.textDoNothing.setOnClickListener {
            endMyActivity()
        }

        binding.textCallNumber.setOnClickListener {
            makeCall(phoneNumber)
        }



    }

    private fun loadBackGroundImage() {
        val sharedP = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
        val getFolderClo = sharedP.getString(Constants.getFolderClo, "").toString()
        val getFolderSubpath = sharedP.getString(Constants.getFolderSubpath, "").toString()

        val baseDir = getExternalFilesDir(null) // App-private external storage
        val relativePath = "${Constants.Syn2AppLive}/$getFolderClo/$getFolderSubpath/${Constants.App}/Config"
        val folder = File(baseDir, relativePath)
        val fileTypes = "app_background.png"
        val file = File(folder, fileTypes)

        if (file.exists()) {
            Glide.with(this).load(file).centerCrop().into(binding.backgroundImage)
        }

    }



    private  fun makeCall(phoneNumber: String?) {


        // Create intent to dial the phone number
        val callIntent = Intent(Intent.ACTION_DIAL)

        // Set the phone number in the data field of the intent
        callIntent.data = Uri.parse("tel:" + Uri.encode(phoneNumber))
        try {
            // Start the activity to initiate the call
            startActivity(callIntent)
            finish()
        } catch (e: ActivityNotFoundException) {
            // Handle case where no dialer app is available
            Toast.makeText(this, "No dialer app found", Toast.LENGTH_SHORT).show()
        }
    }


    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        endMyActivity()
    }

    private fun endMyActivity() {
        val editor = sharedBiometric.edit()
        editor.remove("phoneNumber")
        editor.apply()
        startActivity(Intent(applicationContext, WebViewPage::class.java))
        finish()
    }
}