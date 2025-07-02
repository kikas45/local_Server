package sync2app.com.syncapplive.additionalSettings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import sync2app.com.syncapplive.MyApplication
import sync2app.com.syncapplive.R
import sync2app.com.syncapplive.additionalSettings.autostartAppOncrash.Methods
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.additionalSettings.utils.Utility
import sync2app.com.syncapplive.databinding.ActivityBrandingBinding
import java.io.File


class BrandingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBrandingBinding
    private val sharedBiometric: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_BIOMETRIC,
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



    @SuppressLint("SuspiciousIndentation", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBrandingBinding.inflate(layoutInflater)
        setContentView(binding.root)



        applyOritenation()

        // set up
        setUpFullScreenWindows()


        //add exception
        Methods.addExceptionHandler(this)


        val get_imgToggleImageBackground = sharedBiometric.getString(Constants.imgToggleImageBackground, "")
        val get_imageUseBranding = sharedBiometric.getString(Constants.imageUseBranding, "")
        if (get_imgToggleImageBackground.equals(Constants.imgToggleImageBackground) && get_imageUseBranding.equals(Constants.imageUseBranding) ){
            loadBackGroundImage()
        }




        // set toggle for branding which control the branding set up
        binding.apply {


            imageUseBranding.setOnCheckedChangeListener { compoundButton, isValued ->
                if (compoundButton.isChecked) {
                    val editor = sharedBiometric.edit()

                    editor.putString(Constants.imageUseBranding, Constants.imageUseBranding)
                    editor.apply()
                    binding.textUseBranding.text = "Use Branding"

                    useBackGroundImage()
                    useVideoonSplahsScreen()


                    // Ui
                    textUseImageOrVideoSplashScreen.visibility = View.VISIBLE
                    imgToggleImageSplashOrVideoSplash.visibility = View.VISIBLE
                    imageView38.visibility = View.VISIBLE

                    textUseImageForBranding.visibility = View.VISIBLE
                    imgToggleImageBackground.visibility = View.VISIBLE
                    imageView42.visibility = View.VISIBLE

                    divider45.visibility = View.VISIBLE
                    divider47.visibility = View.VISIBLE
                    divider46.visibility = View.VISIBLE


                } else {
                    val editor = sharedBiometric.edit()

                    editor.remove(Constants.imageUseBranding)
                    editor.apply()
                    binding.textUseBranding.text = "Do not use Branding"


                    removeBackgroundImage()
                    removeVideoOnSplashScreen()

                    // Ui
                    textUseImageOrVideoSplashScreen.visibility = View.INVISIBLE
                    imgToggleImageSplashOrVideoSplash.visibility = View.INVISIBLE
                    imageView38.visibility = View.INVISIBLE

                    textUseImageForBranding.visibility = View.INVISIBLE
                    imgToggleImageBackground.visibility = View.INVISIBLE
                    imageView42.visibility = View.INVISIBLE

                    divider45.visibility = View.INVISIBLE
                    divider47.visibility = View.INVISIBLE
                    divider46.visibility = View.INVISIBLE

                }
            }


            val get_imageUseBranding = sharedBiometric.getString(Constants.imageUseBranding, "")


            imageUseBranding.isChecked = get_imageUseBranding.equals(Constants.imageUseBranding)

            if (get_imageUseBranding.equals(Constants.imageUseBranding)) {

                binding.textUseBranding.text = "Use Branding"

            } else {

                binding.textUseBranding.text = "Do not use Branding"


                // Ui
                textUseImageOrVideoSplashScreen.visibility = View.INVISIBLE
                imgToggleImageSplashOrVideoSplash.visibility = View.INVISIBLE
                imageView38.visibility = View.INVISIBLE

                textUseImageForBranding.visibility = View.INVISIBLE
                imgToggleImageBackground.visibility = View.INVISIBLE
                imageView42.visibility = View.INVISIBLE

                divider45.visibility = View.INVISIBLE
                divider47.visibility = View.INVISIBLE
                divider46.visibility = View.INVISIBLE


            }



            closeBs.setOnClickListener {
                val intent = Intent(applicationContext, MaintenanceActivity::class.java)
                startActivity(intent)
                finish()
            }

        }




        // set on use branding image
        binding.apply {


            // Restart app on Tv or Mobile mode
            imgToggleImageBackground.setOnCheckedChangeListener { compoundButton, isValued ->
                if (compoundButton.isChecked) {
                    useBackGroundImage()

                } else {

                    removeBackgroundImage()
                }
            }


            val get_iimgToggleImageBackground = sharedBiometric.getString(Constants.imgToggleImageBackground, "")


            imgToggleImageBackground.isChecked = get_iimgToggleImageBackground.equals(Constants.imgToggleImageBackground)

            if (get_iimgToggleImageBackground.equals(Constants.imgToggleImageBackground)) {

                binding.textUseImageForBranding.text = "Use Image Background"

            } else {

                binding.textUseImageForBranding.text = "Do not use Image Background"

            }


            closeBs.setOnClickListener {
                val intent = Intent(applicationContext, MaintenanceActivity::class.java)
                startActivity(intent)
                finish()
            }

        }




        /// set up Splash toggle for video or image Splash Screen
        binding.apply {


            // Restart app on Tv or Mobile mode
            imgToggleImageSplashOrVideoSplash.setOnCheckedChangeListener { compoundButton, isValued ->
                if (compoundButton.isChecked) {

                    useVideoonSplahsScreen()

                } else {
                   removeVideoOnSplashScreen()
                }
            }


            val get_imgStartAppRestartOnTvMode = sharedBiometric.getString(Constants.imgToggleImageSplashOrVideoSplash, "")


            imgToggleImageSplashOrVideoSplash.isChecked = get_imgStartAppRestartOnTvMode.equals(Constants.imgToggleImageSplashOrVideoSplash)

            if (get_imgStartAppRestartOnTvMode.equals(Constants.imgToggleImageSplashOrVideoSplash)) {

                binding.textUseImageOrVideoSplashScreen.text = "Use Video For Splash Screen"

            } else {

                binding.textUseImageOrVideoSplashScreen.text = "Use Image For Splash Screen"

            }


            closeBs.setOnClickListener {
                val intent = Intent(applicationContext, MaintenanceActivity::class.java)
                startActivity(intent)
                finish()
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



    private fun removeVideoOnSplashScreen() {
        binding.apply {
            val editor = sharedBiometric.edit()

            editor.remove(Constants.imgToggleImageSplashOrVideoSplash)
            editor.apply()
            binding.textUseImageOrVideoSplashScreen.text = "Use Image For Splash Screen"

            binding.imgToggleImageSplashOrVideoSplash.isChecked = false
        }
    }

    private fun useVideoonSplahsScreen() {
        binding.apply {
            val editor = sharedBiometric.edit()

            editor.putString(Constants.imgToggleImageSplashOrVideoSplash, Constants.imgToggleImageSplashOrVideoSplash)
            editor.apply()
            binding.textUseImageOrVideoSplashScreen.text = "Use Video For Splash Screen"

            binding.imgToggleImageSplashOrVideoSplash.isChecked = true
        }
    }

    private fun removeBackgroundImage() {
        binding.apply {
            val editor = sharedBiometric.edit()

            editor.remove(Constants.imgToggleImageBackground)
            editor.apply()
            binding.textUseImageForBranding.text = "Do not use Image for Background"
            Glide.with(applicationContext).load("").centerCrop().into(binding.backgroundImage)
            binding.imgToggleImageBackground.isChecked = false
        }
    }

    private fun useBackGroundImage() {

        binding.apply {
            val editor = sharedBiometric.edit()

            editor.putString(Constants.imgToggleImageBackground, Constants.imgToggleImageBackground)
            editor.apply()
            binding.textUseImageForBranding.text = "Use Image for Background"

            val get_imageUseBranding = sharedBiometric.getString(Constants.imageUseBranding, "")
            if (get_imageUseBranding.equals(Constants.imageUseBranding)){
                loadBackGroundImage()
            }

            binding.imgToggleImageBackground.isChecked = true
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



    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        startActivity(Intent(applicationContext, MaintenanceActivity::class.java))
        finish()
    }
}