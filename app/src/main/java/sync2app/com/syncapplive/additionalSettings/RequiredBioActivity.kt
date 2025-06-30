package sync2app.com.syncapplive.additionalSettings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import sync2app.com.syncapplive.additionalSettings.utils.Constants

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import sync2app.com.syncapplive.SplashKT
import sync2app.com.syncapplive.additionalSettings.autostartAppOncrash.Methods
import sync2app.com.syncapplive.additionalSettings.utils.Utility

import sync2app.com.syncapplive.databinding.ActivityRequiredBioBinding
import java.io.File


class RequiredBioActivity : AppCompatActivity() {

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

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var binding: ActivityRequiredBioBinding

    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }
    @SuppressLint("SourceLockedOrientationActivity", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequiredBioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("InitWebvIewloadStates", "Requireb Bio: Page ")
        applyOritenation()


        Utility.hideSystemBars(window)


        Methods.addExceptionHandler(this)

        if (shouldLoadBackgroundImage()) {
            loadBackGroundImage()
        }

        setupBiometricAuthentication()
    }


    private fun shouldLoadBackgroundImage(): Boolean {
        val get_imgToggleImageBackground = sharedBiometric.getString(Constants.imgToggleImageBackground, "")
        val get_imageUseBranding = sharedBiometric.getString(Constants.imageUseBranding, "")
        return get_imgToggleImageBackground == Constants.imgToggleImageBackground && get_imageUseBranding == Constants.imageUseBranding
    }

    private fun setupBiometricAuthentication() {
        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.d("BiometricPrompt", "Authentication error: $errString")
                displayMessage("Authentication error: $errString")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d("BiometricPrompt", "Authentication succeeded")
                displayMessage("Authentication succeeded!")
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d("BiometricPrompt", "Authentication failed")
                displayMessage("Authentication failed")
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

        binding.imageView3.setOnClickListener {
            Log.d("RequiredBioActivity", "imageView3 clicked")

            val biometricManager = BiometricManager.from(this)
            when (biometricManager.canAuthenticate()) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    try {
                        biometricPrompt.authenticate(promptInfo)
                    } catch (e: Exception) {
                        Log.e("BiometricPrompt", "Error initiating biometric authentication", e)
                        displayMessage("Error initiating biometric authentication: ${e.message}")
                    }
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> displayMessage("This device doesn't support biometric authentication")
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> displayMessage("Biometric authentication is currently unavailable")
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    redirectToEnrollBiometrics()
                }
            }
        }
    }

    private fun displayMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        if (message == "Authentication succeeded!") {
            val intent = Intent(this, SplashKT::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun redirectToEnrollBiometrics() {
        Toast.makeText(this, "Please enroll biometric credentials in your device settings.", Toast.LENGTH_LONG).show()
        val enrollIntent = Intent(Settings.ACTION_SECURITY_SETTINGS)
        startActivity(enrollIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        val getValue = sharedBiometric.getString(Constants.imgAllowFingerPrint, "")
        if (getValue != Constants.imgAllowFingerPrint) {
            startActivity(Intent(applicationContext, SplashKT::class.java))
            finish()
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
