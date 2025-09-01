package sync2app.com.syncapplive

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.DownloadManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import sync2app.com.syncapplive.additionalSettings.AdditionalSettingsActivity
import sync2app.com.syncapplive.additionalSettings.InformationActivity
import sync2app.com.syncapplive.additionalSettings.MainHelpers.GMailSender
import sync2app.com.syncapplive.additionalSettings.MaintenanceActivity
import sync2app.com.syncapplive.additionalSettings.PasswordActivity
import sync2app.com.syncapplive.additionalSettings.ReSyncActivity
import sync2app.com.syncapplive.additionalSettings.TvActivityOrAppMode
import sync2app.com.syncapplive.additionalSettings.autostartAppOncrash.Methods
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.additionalSettings.utils.Utility
import sync2app.com.syncapplive.databinding.ActivitySettingsBinding
import sync2app.com.syncapplive.databinding.CustomConfirmExitDialogBinding
import sync2app.com.syncapplive.databinding.CustomContactAdminBinding
import sync2app.com.syncapplive.databinding.CustomEmailSucessLayoutBinding
import sync2app.com.syncapplive.databinding.CustomFailedLayoutBinding
import sync2app.com.syncapplive.databinding.CustomForgetPasswordEmailLayoutBinding
import sync2app.com.syncapplive.databinding.CustomRedirectEmailLayoutBinding
import sync2app.com.syncapplive.databinding.CustomServerUrlLayoutBinding
import sync2app.com.syncapplive.databinding.ProgressDialogLayoutBinding
import java.io.File
import java.security.SecureRandom
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.regex.Pattern


class SettingsActivityKT : AppCompatActivity() {

    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    private val sharedBiometric: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_BIOMETRIC, Context.MODE_PRIVATE
        )
    }

    private val myDownloadMangerClass: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.MY_DOWNLOADER_CLASS, Context.MODE_PRIVATE
        )
    }

    private val sharedTVAPPModePreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_TV_APP_MODE, Context.MODE_PRIVATE
        )
    }


    private val simpleSavedPassword: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SIMPLE_SAVED_PASSWORD,
            Context.MODE_PRIVATE
        )
    }


    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }


    private val handlerMoveToWebviewPage: Handler by lazy {
        Handler(Looper.getMainLooper())
    }


    private var customProgressDialog: Dialog? = null

    private lateinit var binding: ActivitySettingsBinding

    private val PREFS_NAME = "app_prefs"
    private val LAST_URL_KEY = "last_url"


    @SuppressLint("SourceLockedOrientationActivity", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)



        applyOritenation()

        setUpFullScreenWindows()

        handlerMoveToWebviewPage.postDelayed(Runnable {
            val getInfoPageState =
                sharedBiometric.getString(Constants.FIRST_INFORMATION_PAGE_COMPLETED, "").toString()
            if (getInfoPageState == Constants.FIRST_INFORMATION_PAGE_COMPLETED) {
                startActivity(Intent(applicationContext, WebViewPage::class.java))
                finish()
            } else {
                startActivity(Intent(applicationContext, InformationActivity::class.java))
                finish()
            }

        }, Constants.MOVE_BK_WEBVIEW_TIME)


        binding.apply {

            try {

                handler.postDelayed(Runnable {
                    showExitConfirmationDialog()
                }, 200)

            } catch (e: Exception) {
            }


            try {

                //add exception
                Methods.addExceptionHandler(this@SettingsActivityKT)

            } catch (e: Exception) {
            }

            val get_imgToggleImageBackground =
                sharedBiometric.getString(Constants.imgToggleImageBackground, "")
            val get_imageUseBranding = sharedBiometric.getString(Constants.imageUseBranding, "")
            if (get_imgToggleImageBackground.equals(Constants.imgToggleImageBackground) && get_imageUseBranding.equals(
                    Constants.imageUseBranding
                )
            ) {
                loadBackGroundImage()
            }




            closeBs.setOnClickListener {

                val getInfoPageState =
                    sharedBiometric.getString(Constants.FIRST_INFORMATION_PAGE_COMPLETED, "")
                        .toString()
                if (getInfoPageState == Constants.FIRST_INFORMATION_PAGE_COMPLETED) {
                    val intent = Intent(applicationContext, WebViewPage::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    startActivity(Intent(applicationContext, InformationActivity::class.java))
                    finish()
                }

            }


            textAdditionalSettingsKey.setOnClickListener {
                val editor = sharedBiometric.edit()
                editor.putString(
                    Constants.Did_User_Input_PassWord,
                    Constants.Did_User_Input_PassWord
                )
                editor.putString(Constants.SAVE_NAVIGATION, Constants.SettingsPage)
                editor.apply()

                val intent = Intent(applicationContext, AdditionalSettingsActivity::class.java)
                startActivity(intent)
                finish()

            }

            if (preferences.getBoolean("darktheme", false)) {

                parentContainer.setBackgroundColor(resources.getColor(R.color.dark_layout_for_ui))
                // set text view
                textTitle.setTextColor(resources.getColor(R.color.white))

            }



            init_viraibles()


        }


        /// Manage Control Label for Tv or App mode JSON

        controlToggleUIVisibilityForJson()


        initializeCache()

        binding.textEnableCacheMode.setOnClickListener {
            clearCache()
        }


        // 🔹 Load last saved URL into EditText
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val lastUrl = prefs.getString(LAST_URL_KEY, "")
        if (!lastUrl.isNullOrEmpty()) {
            binding.editTextUserID.setText(lastUrl)
        }


        binding.textLaunch.setOnClickListener {
            val url = binding.editTextUserID.text.toString().trim()
            if (url.startsWith("http://") || url.startsWith("https://")) {
                // 🔹 Save the URL for next time
                prefs.edit().putString(LAST_URL_KEY, url).apply()

                val intent = Intent(applicationContext, WebViewPage::class.java)
                intent.putExtra("url_launch", url)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(applicationContext, "Enter a valid URL (http/https)", Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun clearCache() {
        val builder = AlertDialog.Builder(this@SettingsActivityKT)
        builder.setMessage("Are you sure want to clear cache?")
        builder.setPositiveButton("Yes") { dialog, which ->

            // FileUtils.deleteQuietly(cacheDir)
            // FileUtils.deleteQuietly(externalCacheDir)

            binding.textEnableCacheMode.text = "Free up" + " 0 Bytes " + "of space"
            Snackbar.make(
                findViewById(android.R.id.content),
                "cache has been cleared",
                Snackbar.LENGTH_SHORT
            ).show()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun initializeCache() {
        val totalSize = 0 + getDirSize(cacheDir) + getDirSize(externalCacheDir!!)
        binding.textEnableCacheMode.text =
            "Free up" + " " + readableFileSize(totalSize) + " " + "of space"
    }

    fun getDirSize(dir: File): Long {
        var size: Long = 0
        dir.listFiles()?.forEach { file ->
            size += if (file.isDirectory) getDirSize(file)
            else if (file.isFile) file.length() else 0
        }
        return size
    }

    fun readableFileSize(size: Long): String {
        if (size <= 0) return "0 Bytes"
        val units = arrayOf("Bytes", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        val formattedSize =
            DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble()))
        return "$formattedSize ${units[digitGroups]}"
    }


    private fun setUpFullScreenWindows() {
        val get_INSTALL_TV_JSON_USER_CLICKED =
            sharedTVAPPModePreferences.getString(Constants.INSTALL_TV_JSON_USER_CLICKED, "")
                .toString()
        if (get_INSTALL_TV_JSON_USER_CLICKED != Constants.INSTALL_TV_JSON_USER_CLICKED) {
            val img_imgImmesriveModeToggle = preferences.getBoolean(Constants.immersive_mode, false)
            if (img_imgImmesriveModeToggle) {
                Utility.hideSystemBars(window)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }


        } else {

            val immersive_Mode_APP =
                sharedTVAPPModePreferences.getBoolean(Constants.immersive_Mode_APP, false)
            if (immersive_Mode_APP) {
                Utility.hideSystemBars(window)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }

        }
    }


    private fun controlToggleUIVisibilityForJson() {
        binding.apply {
            val get_INSTALL_TV_JSON_USER_CLICKED =
                sharedTVAPPModePreferences.getString(Constants.INSTALL_TV_JSON_USER_CLICKED, "")
                    .toString()
            val hide_TV_Mode_Label =
                sharedTVAPPModePreferences.getBoolean(Constants.hide_TV_Mode_Label, false)
            val hideFull_ScreenLabel =
                sharedTVAPPModePreferences.getBoolean(Constants.hide_Full_ScreenLabel, false)
            val hide_Immersive_ModeLabel =
                sharedTVAPPModePreferences.getBoolean(Constants.hide_Immersive_ModeLabel, false)
            val hide_Bottom_Bar_Label_APP =
                sharedTVAPPModePreferences.getBoolean(Constants.hide_Bottom_Bar_Label_APP, false)
            val hide_Floating_ButtonLabel_APP =
                sharedTVAPPModePreferences.getBoolean(Constants.hide_Floating_Button_APP, false)
            val hide_Bottom_MenuIconLabel_APP = sharedTVAPPModePreferences.getBoolean(
                Constants.hide_Bottom_MenuIconLabel_APP,
                false
            )


            if (get_INSTALL_TV_JSON_USER_CLICKED == Constants.INSTALL_TV_JSON_USER_CLICKED) {

                if (hide_TV_Mode_Label) {
                    imgTvOrAppModeMode.visibility = View.GONE
                    textTvOrAppMode.visibility = View.GONE
                    imageViewAppModeOrTvMode.visibility = View.GONE
                    divider78.visibility = View.GONE
                }


                if (hideFull_ScreenLabel) {
                    imgFullScreenToggle.visibility = View.GONE
                    textFullScreen.visibility = View.GONE
                    imageView1.visibility = View.GONE
                    divider8.visibility = View.GONE
                }


                if (hide_Immersive_ModeLabel) {
                    imgImmesriveModeToggle.visibility = View.GONE
                    textimmersiveMode.visibility = View.GONE
                    imageViewImmersiveMode.visibility = View.GONE
                    dividerimmservise.visibility = View.GONE
                }

                if (hide_Bottom_Bar_Label_APP) {
                    imgHidebottombar.visibility = View.GONE
                    textHidebottombar.visibility = View.GONE
                    imageViewHidebottombar.visibility = View.GONE
                    divider61.visibility = View.GONE
                }


                if (hide_Floating_ButtonLabel_APP) {
                    imgShwoFloatingButton.visibility = View.GONE
                    textShwoFloatingButton.visibility = View.GONE
                    imageViewShwoFloatingButton.visibility = View.GONE
                    divider68.visibility = View.GONE
                }


                if (hide_Bottom_MenuIconLabel_APP) {
                    imgHideDrawerIcon.visibility = View.GONE
                    textHideDrawerIcon.visibility = View.GONE
                    imageViewHideDrawerIcon.visibility = View.GONE
                    // divider62.visibility = View.GONE
                }


            }
        }
    }

    override fun onResume() {
        super.onResume()
        reomvePrefilledPassowrd()

    }

    override fun onDestroy() {
        super.onDestroy()
        if (handlerMoveToWebviewPage != null) {
            handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
        }
    }

    private fun get_Current_Time_State_for_Password(
        editText: EditText,
        imgToggle: ImageView,
        imgToggleNzotVisible: ImageView
    ) {
        val getPrefilledPassword =
            simpleSavedPassword.getString(Constants.passowrdPrefeilled, "").toString()

        if (getPrefilledPassword == Constants.passowrdPrefeilled) {
            val futureTime = getSavedFutureTime()
            if (futureTime != null) {
                val currentTime = Calendar.getInstance().time
                if (currentTime.after(futureTime)) {

                    val editor = simpleSavedPassword.edit()
                    editor.remove(Constants.passowrdPrefeilled)
                    editor.remove(Constants.Did_User_Input_PassWord)
                    editor.apply()

                    editText.isEnabled = true
                    editText.setText("")

                    imgToggle.visibility = View.VISIBLE
                    imgToggleNzotVisible.visibility = View.INVISIBLE

                }
            }
        }

    }

    private fun reomvePrefilledPassowrd() {
        val getPrefilledPassword =
            simpleSavedPassword.getString(Constants.passowrdPrefeilled, "").toString()

        if (getPrefilledPassword == Constants.passowrdPrefeilled) {
            val futureTime = getSavedFutureTime()
            if (futureTime != null) {
                val currentTime = Calendar.getInstance().time
                if (currentTime.after(futureTime)) {

                    val editor = simpleSavedPassword.edit()
                    editor.remove(Constants.passowrdPrefeilled)
                    editor.remove(Constants.Did_User_Input_PassWord)
                    editor.apply()

                }
            }
        }

    }


    private fun getSavedFutureTime(): Date? {
        val futureTimeString = simpleSavedPassword.getString(Constants.KEY_FUTURE_TIME, null)
        return if (futureTimeString != null) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            try {
                sdf.parse(futureTimeString)
            } catch (e: ParseException) {
                null
            }
        } else {
            null
        }
    }


    private fun init_viraibles() {

        // Tv or App Mode
        // Tv or App Mode
        val editorShared = sharedBiometric.edit()
        val editorTVJSON = sharedTVAPPModePreferences.edit()
        val editor = preferences.edit()
        binding.apply {
            val get_INSTALL_TV_JSON_USER_CLICKED =
                sharedTVAPPModePreferences.getString(Constants.INSTALL_TV_JSON_USER_CLICKED, "")
                    .toString()
            val get_installTVMode =
                sharedTVAPPModePreferences.getBoolean(Constants.installTVMode, false)


            if (get_INSTALL_TV_JSON_USER_CLICKED == Constants.INSTALL_TV_JSON_USER_CLICKED) {

                if (get_installTVMode) {

                    imgTvOrAppModeMode.isChecked = true
                    textTvOrAppMode.text = "Install TV Mode"
                    editorShared.putString(Constants.MY_TV_OR_APP_MODE, Constants.TV_Mode)
                    editorShared.apply()


                } else {

                    imgTvOrAppModeMode.isChecked = false
                    textTvOrAppMode.text = "Install Mobile Mode"
                    editorShared.putString(Constants.MY_TV_OR_APP_MODE, Constants.App_Mode)
                    editorShared.apply()

                }
            } else {
                val get_AppMode =
                    sharedBiometric.getString(Constants.MY_TV_OR_APP_MODE, "").toString()
                if (get_AppMode == Constants.TV_Mode) {

                    imgTvOrAppModeMode.isChecked = true
                    textTvOrAppMode.text = "Install TV Mode"
                    editorShared.putString(Constants.MY_TV_OR_APP_MODE, Constants.TV_Mode)
                    editorShared.apply()


                } else {

                    imgTvOrAppModeMode.isChecked = false
                    textTvOrAppMode.text = "Install Mobile Mode"
                    editorShared.putString(Constants.MY_TV_OR_APP_MODE, Constants.App_Mode)
                    editorShared.apply()

                }


            }



            imgTvOrAppModeMode.setOnCheckedChangeListener { compoundButton, isValued -> // we are putting the values into SHARED PREFERENCE

                if (compoundButton.isChecked) {

                    textTvOrAppMode.text = "Install TV Mode"
                    editorShared.putString(
                        Constants.imgStartAppRestartOnTvMode,
                        Constants.imgStartAppRestartOnTvMode
                    )
                    editorShared.putString(Constants.MY_TV_OR_APP_MODE, Constants.TV_Mode)

                    editorShared.putString(
                        Constants.get_Launching_State_Of_WebView,
                        Constants.launch_WebView_Offline
                    )
                    editorShared.putString(Constants.PROTECT_PASSWORD, Constants.PROTECT_PASSWORD)
                    editorShared.apply()

                    // for pasowrod
                    imgProtrectPassoword.isChecked = true

                    //for Tv mode
                    imgFullScreenToggle.isChecked = true
                    imgImmesriveModeToggle.isChecked = true
                    imgHidebottombar.isChecked = true
                    imgShwoFloatingButton.isChecked = true

                    imgHideDrawerIcon.isChecked = false

                    editor!!.putBoolean(Constants.hidebottombar, true)
                    editor.putBoolean(Constants.fullscreen, true)
                    editor.putBoolean(Constants.immersive_mode, true)
                    editor.putBoolean(Constants.shwoFloatingButton, true)
                    editor.apply()

                    setupBackgroudColorTvMode_OFF()

                    // for our new Json TV or Aoo Mode
                    editorTVJSON.putBoolean(Constants.installTVMode, true)
                    editorTVJSON.apply()


                } else {

                    textTvOrAppMode.text = "Install Mobile Mode"
                    editorShared.putString(Constants.MY_TV_OR_APP_MODE, Constants.App_Mode)
                    editorShared.remove(Constants.imgStartAppRestartOnTvMode)
                    editorShared.putString(
                        Constants.get_Launching_State_Of_WebView,
                        Constants.launch_Default_WebView_url
                    )
                    editorShared.remove(Constants.PROTECT_PASSWORD)
                    editorShared.apply()


                    //forassword

                    imgProtrectPassoword.isChecked = false

                    // for App Mode
                    imgFullScreenToggle.isChecked = false
                    imgImmesriveModeToggle.isChecked = false
                    imgHidebottombar.isChecked = false
                    imgShwoFloatingButton.isChecked = false

                    imgHideDrawerIcon.isChecked = true

                    editor!!.putBoolean(Constants.hidebottombar, false)
                    editor.putBoolean(Constants.fullscreen, false)
                    editor.putBoolean(Constants.immersive_mode, false)
                    editor.putBoolean(Constants.shwoFloatingButton, false)
                    editor.apply()

                    setupBackgroudColorTvMode_ON()


                    // for our new Json TV or Aoo Mode
                    editorTVJSON.putBoolean(Constants.installTVMode, false)
                    editorTVJSON.apply()


                }
            }


            // chnage back color ofr Tv mode toglles

            // set up offline toggle
            imgUseOfflineFolderOrNot.setOnCheckedChangeListener { compoundButton, isValued -> // we are putting the values into SHARED PREFERENCE
                if (compoundButton.isChecked) {
                    textUseOfflineFolderOrNot.text = "Use local Files if Offline (ON)"
                    editorShared.putString(
                        Constants.USE_OFFLINE_FOLDER,
                        Constants.USE_OFFLINE_FOLDER
                    )
                    editorShared.apply()

                } else {
                    textUseOfflineFolderOrNot.text = "Use local Files if Offline (OFF)"
                    editorShared.remove(Constants.USE_OFFLINE_FOLDER)
                    editorShared.apply()

                }
            }


            val get_useOfflineFolderOrNot =
                sharedBiometric.getString(Constants.USE_OFFLINE_FOLDER, "").toString()
            if (get_useOfflineFolderOrNot == Constants.USE_OFFLINE_FOLDER) {
                imgUseOfflineFolderOrNot.isChecked = true
                textUseOfflineFolderOrNot.text = "Use local Files if Offline"
            } else {
                imgUseOfflineFolderOrNot.isChecked = false
                textUseOfflineFolderOrNot.text = "Use local Files if Offline"
            }
        }


        // init toggle Full Screen
        binding.apply {

            val get_INSTALL_TV_JSON_USER_CLICKED =
                sharedTVAPPModePreferences.getString(Constants.INSTALL_TV_JSON_USER_CLICKED, "")
                    .toString()

            imgFullScreenToggle.setOnCheckedChangeListener { compoundButton, isValued ->
                if (compoundButton.isChecked) {
                    editor.putBoolean(Constants.fullscreen, true)
                    editor.apply()

                    editorTVJSON.putBoolean(Constants.fullScreen_APP, true)
                    editorTVJSON.apply()


                    textFullScreen.setTextColor(resources.getColor(R.color.dark_light_gray))
                    val drawable_imageView1 =
                        ContextCompat.getDrawable(applicationContext, R.drawable.ic_full_screen)
                    drawable_imageView1?.setColorFilter(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.pref_icons_color
                        ), PorterDuff.Mode.SRC_IN
                    )
                    imageView1.setImageDrawable(drawable_imageView1)


                } else {
                    editor.putBoolean(Constants.fullscreen, false)
                    editor.apply()

                    editorTVJSON.putBoolean(Constants.fullScreen_APP, false)
                    editorTVJSON.apply()


                    textFullScreen.setTextColor(resources.getColor(R.color.logo_green))
                    val drawable_imageView1 =
                        ContextCompat.getDrawable(applicationContext, R.drawable.ic_full_screen)
                    drawable_imageView1?.setColorFilter(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.logo_green
                        ), PorterDuff.Mode.SRC_IN
                    )
                    imageView1.setImageDrawable(drawable_imageView1)

                }
            }


            val fullScreen_APP =
                sharedTVAPPModePreferences.getBoolean(Constants.fullScreen_APP, false)
            if (get_INSTALL_TV_JSON_USER_CLICKED == Constants.INSTALL_TV_JSON_USER_CLICKED) {

                imgFullScreenToggle.isChecked = fullScreen_APP == true

            } else {
                val imgFullScreen = preferences.getBoolean(Constants.fullscreen, false)
                imgFullScreenToggle.isChecked = imgFullScreen == true
            }


        }


        // immersive_mode Mode
        binding.apply {


            val get_INSTALL_TV_JSON_USER_CLICKED =
                sharedTVAPPModePreferences.getString(Constants.INSTALL_TV_JSON_USER_CLICKED, "")
                    .toString()

            imgImmesriveModeToggle.setOnCheckedChangeListener { compoundButton, isValued ->
                if (compoundButton.isChecked) {
                    editor.putBoolean(Constants.immersive_mode, true)
                    editor.apply()


                    editorTVJSON.putBoolean(Constants.immersive_Mode_APP, true)
                    editorTVJSON.apply()

                    textimmersiveMode.setTextColor(resources.getColor(R.color.dark_light_gray))
                    val drawable_imageViewImmersive_mode = ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_immersive_fullscreen
                    )
                    drawable_imageViewImmersive_mode?.setColorFilter(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.pref_icons_color
                        ), PorterDuff.Mode.SRC_IN
                    )
                    imageViewImmersiveMode.setImageDrawable(drawable_imageViewImmersive_mode)


                } else {
                    editor.putBoolean(Constants.immersive_mode, false)
                    editor.apply()

                    editorTVJSON.putBoolean(Constants.immersive_Mode_APP, false)
                    editorTVJSON.apply()

                    textimmersiveMode.setTextColor(resources.getColor(R.color.logo_green))
                    val drawable_imageViewImmersive_mode = ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_immersive_fullscreen
                    )
                    drawable_imageViewImmersive_mode?.setColorFilter(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.logo_green
                        ), PorterDuff.Mode.SRC_IN
                    )
                    imageViewImmersiveMode.setImageDrawable(drawable_imageViewImmersive_mode)

                }
            }


            val immersive_Mode_APP =
                sharedTVAPPModePreferences.getBoolean(Constants.immersive_Mode_APP, false)
            if (get_INSTALL_TV_JSON_USER_CLICKED == Constants.INSTALL_TV_JSON_USER_CLICKED) {

                imgImmesriveModeToggle.isChecked = immersive_Mode_APP == true
            } else {
                val img_imgImmesriveModeToggle =
                    preferences.getBoolean(Constants.immersive_mode, false)
                imgImmesriveModeToggle.isChecked = img_imgImmesriveModeToggle == true
            }


        }


        // img_geolocation Mode
        binding.apply {

            imgGeolocation.setOnCheckedChangeListener { compoundButton, isValued ->
                if (compoundButton.isChecked) {
                    editor.putBoolean(Constants.geolocation, true)
                    editor.apply()

                } else {
                    editor.putBoolean(Constants.geolocation, false)
                    editor.apply()
                }
            }

            val img_imgGeolocation = preferences.getBoolean(Constants.geolocation, false)
            imgGeolocation.isChecked = img_imgGeolocation == true
        }


        // darktheme Mode
        binding.apply {

            imgDarktheme.setOnCheckedChangeListener { compoundButton, isValued ->
                if (compoundButton.isChecked) {
                    editor.putBoolean(Constants.darktheme, true)
                    editor.apply()

                } else {
                    editor.putBoolean(Constants.darktheme, false)
                    editor.apply()
                }
            }

            val img_imgDarktheme = preferences.getBoolean(Constants.darktheme, false)
            imgDarktheme.isChecked = img_imgDarktheme == true
        }


        // nightmode Mode
        binding.apply {

            imgNightmode.setOnCheckedChangeListener { compoundButton, isValued ->
                if (compoundButton.isChecked) {
                    editor.putBoolean(Constants.nightmode, true)
                    editor.apply()

                } else {
                    editor.putBoolean(Constants.nightmode, false)
                    editor.apply()
                }
            }

            val img_imgNightmode = preferences.getBoolean(Constants.nightmode, false)
            imgNightmode.isChecked = img_imgNightmode == true
        }


        // swiperefresh Mode
        binding.apply {

            imgSwiperefresh.setOnCheckedChangeListener { compoundButton, isValued ->
                if (compoundButton.isChecked) {
                    editor.putBoolean(Constants.swiperefresh, true)
                    editor.apply()

                } else {
                    editor.putBoolean(Constants.swiperefresh, false)
                    editor.apply()
                }
            }

            val img_imgSwiperefresh = preferences.getBoolean(Constants.swiperefresh, false)
            imgSwiperefresh.isChecked = img_imgSwiperefresh == true
        }


        // hide QR Code Mode
        binding.apply {

            imgHideQRCode.setOnCheckedChangeListener { compoundButton, isValued ->
                if (compoundButton.isChecked) {
                    textHideQRCode.text = "Hide QR Code"
                    editor.putBoolean(Constants.hideQRCode, true)
                    editor.apply()

                } else {
                    textHideQRCode.text = "Show QR Code"
                    editor.putBoolean(Constants.hideQRCode, false)
                    editor.apply()
                }
            }

            val img_imgHideQRCode = preferences.getBoolean(Constants.hideQRCode, false)
            imgHideQRCode.isChecked = img_imgHideQRCode == true

            if (img_imgHideQRCode == true) {
                textHideQRCode.text = "Hide QR Code"
            } else {
                textHideQRCode.text = "Show QR Code"
            }


        }


        // hidebottombar Mode
        binding.apply {
            val get_INSTALL_TV_JSON_USER_CLICKED =
                sharedTVAPPModePreferences.getString(Constants.INSTALL_TV_JSON_USER_CLICKED, "")
                    .toString()
            imgHidebottombar.setOnCheckedChangeListener { compoundButton, isValued ->
                if (compoundButton.isChecked) {
                    textHidebottombar.text = "Hide Bottom Bar"
                    editor.putBoolean(Constants.hidebottombar, true)
                    editor.apply()

                    // setting for new json
                    editorTVJSON.putBoolean(Constants.hide_BottomBar_APP, true)
                    editorTVJSON.apply()


                    textHidebottombar.setTextColor(resources.getColor(R.color.dark_light_gray))
                    val drawable_imageViewHidebottombar =
                        ContextCompat.getDrawable(applicationContext, R.drawable.ic_toolbar_bottom)
                    drawable_imageViewHidebottombar?.setColorFilter(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.pref_icons_color
                        ), PorterDuff.Mode.SRC_IN
                    )
                    imageViewHidebottombar.setImageDrawable(drawable_imageViewHidebottombar)


                } else {
                    textHidebottombar.text = "Show Bottom Bar"
                    editor.putBoolean(Constants.hidebottombar, false)
                    editor.apply()

                    // setting for new json
                    editorTVJSON.putBoolean(Constants.hide_BottomBar_APP, false)
                    editorTVJSON.apply()


                    textHidebottombar.setTextColor(resources.getColor(R.color.logo_green))
                    val drawable_imageViewHidebottombar =
                        ContextCompat.getDrawable(applicationContext, R.drawable.ic_toolbar_bottom)
                    drawable_imageViewHidebottombar?.setColorFilter(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.logo_green
                        ), PorterDuff.Mode.SRC_IN
                    )
                    imageViewHidebottombar.setImageDrawable(drawable_imageViewHidebottombar)


                }
            }

            val show_BottomBar_APP =
                sharedTVAPPModePreferences.getBoolean(Constants.hide_BottomBar_APP, false)
            if (get_INSTALL_TV_JSON_USER_CLICKED == Constants.INSTALL_TV_JSON_USER_CLICKED) {

                imgHidebottombar.isChecked = show_BottomBar_APP == true

                if (show_BottomBar_APP) {
                    textHidebottombar.text = "Hide Bottom Bar"
                } else {
                    textHidebottombar.text = "Show Bottom Bar"
                }

            } else {
                val img_imgHidebottombar = preferences.getBoolean(Constants.hidebottombar, false)
                imgHidebottombar.isChecked = img_imgHidebottombar == true

                if (img_imgHidebottombar) {
                    textHidebottombar.text = "Hide Bottom Bar"
                } else {
                    textHidebottombar.text = "Show Bottom Bar"
                }
            }


        }


        // hide_drawer_icon Mode
        binding.apply {

            val get_INSTALL_TV_JSON_USER_CLICKED =
                sharedTVAPPModePreferences.getString(Constants.INSTALL_TV_JSON_USER_CLICKED, "")
                    .toString()
            imgHideDrawerIcon.setOnCheckedChangeListener { compoundButton, isValued ->
                if (compoundButton.isChecked) {
                    textHideDrawerIcon.text = "Show Bottom Menu Icon"
                    editor.putBoolean(Constants.hide_drawer_icon, true)
                    editor.apply()


                    editorTVJSON.putBoolean(Constants.hideBottom_MenuIcon_APP, true)
                    editorTVJSON.apply()


                } else {
                    textHideDrawerIcon.text = "Hide Bottom Menu Icon"
                    editor.putBoolean(Constants.hide_drawer_icon, false)
                    editor.apply()


                    editorTVJSON.putBoolean(Constants.hideBottom_MenuIcon_APP, false)
                    editorTVJSON.apply()

                }
            }


            val hideBottom_MenuIcon_APP =
                sharedTVAPPModePreferences.getBoolean(Constants.hideBottom_MenuIcon_APP, false)
            if (get_INSTALL_TV_JSON_USER_CLICKED == Constants.INSTALL_TV_JSON_USER_CLICKED) {

                imgHideDrawerIcon.isChecked = hideBottom_MenuIcon_APP == true


                if (hideBottom_MenuIcon_APP) {
                    textHideDrawerIcon.text = "Show Bottom Menu Icon"
                } else {

                    textHideDrawerIcon.text = "Hide Bottom Menu Icon"
                }

            } else {
                val img_imgHideDrawerIcon =
                    preferences.getBoolean(Constants.hide_drawer_icon, false)
                imgHideDrawerIcon.isChecked = img_imgHideDrawerIcon == true

                if (img_imgHideDrawerIcon) {
                    textHideDrawerIcon.text = "Show Bottom Menu Icon"
                } else {
                    textHideDrawerIcon.text = "Hide Bottom Menu Icon"
                }

            }


        }


        // Floating Action Button  Mode
        binding.apply {

            val get_INSTALL_TV_JSON_USER_CLICKED =
                sharedTVAPPModePreferences.getString(Constants.INSTALL_TV_JSON_USER_CLICKED, "")
                    .toString()
            imgShwoFloatingButton.setOnCheckedChangeListener { compoundButton, isValued ->
                if (compoundButton.isChecked) {
                    textShwoFloatingButton.text = "Hide Floating Button"
                    editor.putBoolean(Constants.shwoFloatingButton, true)
                    editor.apply()

                    // setting for new json
                    editorTVJSON.putBoolean(Constants.hide_Floating_Button_APP, true)
                    editorTVJSON.apply()


                    textShwoFloatingButton.setTextColor(resources.getColor(R.color.dark_light_gray))

                    val drawable_imageViewHidebottombar = ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_floating_button_24
                    )
                    drawable_imageViewHidebottombar?.setColorFilter(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.pref_icons_color
                        ), PorterDuff.Mode.SRC_IN
                    )
                    imageViewShwoFloatingButton.setImageDrawable(drawable_imageViewHidebottombar)


                } else {
                    textShwoFloatingButton.text = "Show Floating Button"
                    editor.putBoolean(Constants.shwoFloatingButton, false)
                    editor.apply()


                    // setting for new json
                    editorTVJSON.putBoolean(Constants.hide_Floating_Button_APP, false)
                    editorTVJSON.apply()


                    textShwoFloatingButton.setTextColor(resources.getColor(R.color.logo_green))

                    val drawable_imageViewHidebottombar = ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_floating_button_24
                    )
                    drawable_imageViewHidebottombar?.setColorFilter(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.logo_green
                        ), PorterDuff.Mode.SRC_IN
                    )
                    imageViewShwoFloatingButton.setImageDrawable(drawable_imageViewHidebottombar)


                }
            }


            val showFloating_Button_APP =
                sharedTVAPPModePreferences.getBoolean(Constants.hide_Floating_Button_APP, false)
            if (get_INSTALL_TV_JSON_USER_CLICKED == Constants.INSTALL_TV_JSON_USER_CLICKED) {

                imgShwoFloatingButton.isChecked = showFloating_Button_APP == true

                if (showFloating_Button_APP) {
                    textShwoFloatingButton.text = "Hide Floating Button"
                } else {

                    textShwoFloatingButton.text = "Show Floating Button"
                }

            } else {
                val img_imgShwoFloatingButton =
                    preferences.getBoolean(Constants.shwoFloatingButton, false)
                imgShwoFloatingButton.isChecked = img_imgShwoFloatingButton == true

                if (img_imgShwoFloatingButton) {
                    textShwoFloatingButton.text = "Hide Floating Button"
                } else {
                    textShwoFloatingButton.text = "Show Floating Button"
                }

            }


        }


        // Auto Hide Tool Bar  Mode
        binding.apply {

            imgAutohideToolbar.setOnCheckedChangeListener { compoundButton, isValued ->
                if (compoundButton.isChecked) {
                    textAutohideToolbar.text = "Auto Hide Toolbar ON"
                    editor.putBoolean(Constants.autohideToolbar, true)
                    editor.apply()


                } else {
                    textAutohideToolbar.text = "Auto Hide Toolbar Off"
                    editor.putBoolean(Constants.autohideToolbar, false)
                    editor.apply()

                }
            }

            val img_imgAutohideToolbar = preferences.getBoolean(Constants.autohideToolbar, false)
            imgAutohideToolbar.isChecked = img_imgAutohideToolbar == true

            if (img_imgAutohideToolbar) {
                textAutohideToolbar.text = "Auto Hide Toolbar ON"
            } else {
                textAutohideToolbar.text = "Auto Hide Toolbar Off"
            }


        }


        // Enable Cache   Mode
        // Enable Cache   Mode
        // Enable Cache   Mode
        // Enable Cache   Mode
        // Enable Cache   Mode


        // 1 permissions_switch Mode
        binding.apply {
            imgPermission.setOnCheckedChangeListener { compoundButton, isValued ->
                if (compoundButton.isChecked) {
                    editor.putBoolean(Constants.permission_query, true)
                    editor.apply()

                } else {
                    editor.putBoolean(Constants.permission_query, false)
                    editor.apply()
                }
            }

            val img_imgPermission = preferences.getBoolean(Constants.permission_query, false)
            imgPermission.isChecked = img_imgPermission == true


            // 2 img_nativeload Mode
            imgNativeload.setOnCheckedChangeListener { compoundButton, isValued ->
                if (compoundButton.isChecked) {
                    editor.putBoolean(Constants.nativeload, true)
                    editor.apply()

                } else {
                    editor.putBoolean(Constants.nativeload, false)
                    editor.apply()
                }
            }

            val img_imgNativeload = preferences.getBoolean(Constants.nativeload, false)
            imgNativeload.isChecked = img_imgNativeload == true


            // 3 imgLoadLastUrl Mode
            imgLoadLastUrl.setOnCheckedChangeListener { compoundButton, isValued ->
                if (compoundButton.isChecked) {
                    editor.putBoolean(Constants.loadLastUrl, true)
                    editor.apply()

                } else {
                    editor.putBoolean(Constants.loadLastUrl, false)
                    editor.apply()
                }
            }

            val img_imgLoadLastUrl = preferences.getBoolean(Constants.loadLastUrl, false)
            imgLoadLastUrl.isChecked = img_imgLoadLastUrl == true


            // 4 img_blockAds Mode
            imgBlockAds.setOnCheckedChangeListener { compoundButton, isValued ->
                if (compoundButton.isChecked) {
                    editor.putBoolean(Constants.blockAds, true)
                    editor.apply()

                } else {
                    editor.putBoolean(Constants.blockAds, false)
                    editor.apply()
                }
            }

            val img_imgBlockAds = preferences.getBoolean(Constants.blockAds, false)
            imgBlockAds.isChecked = img_imgBlockAds == true


            // 4 server Mode

            binding.textSurl.setOnClickListener {

                showPopForserverUrl()
            }


        }


        binding.apply {
            /// init default state of the toggle

            if (!constants.ShowServerUrlSetUp) {
                try {
                    imageView26.visibility = View.GONE
                    textSurl.visibility = View.GONE
                    imageViewSurl.visibility = View.GONE
                    divider75.visibility = View.GONE
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
            if (!constants.ShowToolbar) {
                try {
                    textAutohideToolbar.visibility = View.GONE
                    imageViewAutohideToolbar.visibility = View.GONE
                    imgAutohideToolbar.visibility = View.GONE

                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                if (!constants.ShowBottomBar) {
                    try {
                        imgHidebottombar.visibility = View.GONE
                        textHidebottombar.visibility = View.GONE
                        imageViewHidebottombar.visibility = View.GONE
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
            }

        }

        // proctec password
        binding.apply {

            // Set an OnClickListener to toggle orientation mode
            imgProtrectPassoword.setOnCheckedChangeListener { compoundButton, isValued ->
                if (compoundButton.isChecked) {

                    binding.textProctPassowrd.text = "Protect Exit"

                    editorShared.putString(Constants.PROTECT_PASSWORD, Constants.PROTECT_PASSWORD)
                    editorShared.apply()

                } else {
                    binding.textProctPassowrd.text = "Do not Protect Exit"

                    editorShared.remove(Constants.PROTECT_PASSWORD)
                    editorShared.apply()

                }
            }


            val get_ProtectPassowrd =
                sharedBiometric.getString(Constants.PROTECT_PASSWORD, "").toString()
            imgProtrectPassoword.isChecked = get_ProtectPassowrd == Constants.PROTECT_PASSWORD

            if (get_ProtectPassowrd == Constants.PROTECT_PASSWORD) {
                binding.textProctPassowrd.text = "Protect Exit"
            } else {

                binding.textProctPassowrd.text = "Do not Protect Exit"

            }

        }


    }


    @SuppressLint("MissingInflatedId")
    private fun showPopForserverUrl() {
        val bindingCM: CustomServerUrlLayoutBinding = CustomServerUrlLayoutBinding.inflate(
            layoutInflater
        )
        val builder = AlertDialog.Builder(this)
        builder.setView(bindingCM.getRoot())
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setCancelable(false)
        if (alertDialog.window != null) {
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        val textOkayBtn: TextView = bindingCM.textOkayBtn
        val textCancelBtn: TextView = bindingCM.textCancelBtn
        val edittextInput: EditText = bindingCM.editTextEmail
        val imgCloseDialog2: ImageView = bindingCM.imgCloseDialogForegetPassword


        val get_surl = preferences.getString(Constants.surl, "").toString()
        if (get_surl.isNotEmpty()) {
            edittextInput.setText(get_surl)
        }



        textOkayBtn.setOnClickListener {
            val getEditTextValue = edittextInput.text.toString().trim()
            val editor = preferences.edit()
            editor.putString(Constants.surl, getEditTextValue)
            editor.apply()
            alertDialog.dismiss()

        }


        textCancelBtn.setOnClickListener {
            alertDialog.dismiss()
        }



        imgCloseDialog2.setOnClickListener {
            alertDialog.dismiss()
        }





        alertDialog.show()
    }


    private fun setupBackgroudColorTvMode_OFF() {
        binding.apply {

            textFullScreen.setTextColor(resources.getColor(R.color.dark_light_gray))
            val drawable_imageView1 =
                ContextCompat.getDrawable(applicationContext, R.drawable.ic_full_screen)
            drawable_imageView1?.setColorFilter(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.pref_icons_color
                ), PorterDuff.Mode.SRC_IN
            )
            imageView1.setImageDrawable(drawable_imageView1)


            textimmersiveMode.setTextColor(resources.getColor(R.color.dark_light_gray))
            val drawable_imageViewImmersive_mode =
                ContextCompat.getDrawable(applicationContext, R.drawable.ic_immersive_fullscreen)
            drawable_imageViewImmersive_mode?.setColorFilter(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.pref_icons_color
                ), PorterDuff.Mode.SRC_IN
            )
            imageViewImmersiveMode.setImageDrawable(drawable_imageViewImmersive_mode)




            textHidebottombar.setTextColor(resources.getColor(R.color.dark_light_gray))
            val drawable_imageViewHidebottombar =
                ContextCompat.getDrawable(applicationContext, R.drawable.ic_toolbar_bottom)
            drawable_imageViewHidebottombar?.setColorFilter(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.pref_icons_color
                ), PorterDuff.Mode.SRC_IN
            )
            imageViewHidebottombar.setImageDrawable(drawable_imageViewHidebottombar)


        }
    }


    @SuppressLint("SuspiciousIndentation")
    private fun setupBackgroudColorTvMode_ON() {
        binding.apply {

            textFullScreen.setTextColor(resources.getColor(R.color.logo_green))
            val drawable_imageView1 =
                ContextCompat.getDrawable(applicationContext, R.drawable.ic_full_screen)
            drawable_imageView1?.setColorFilter(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.logo_green
                ), PorterDuff.Mode.SRC_IN
            )
            imageView1.setImageDrawable(drawable_imageView1)




            textimmersiveMode.setTextColor(resources.getColor(R.color.logo_green))
            val drawable_imageViewImmersive_mode =
                ContextCompat.getDrawable(applicationContext, R.drawable.ic_immersive_fullscreen)
            drawable_imageViewImmersive_mode?.setColorFilter(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.logo_green
                ), PorterDuff.Mode.SRC_IN
            )
            imageViewImmersiveMode.setImageDrawable(drawable_imageViewImmersive_mode)




            textHidebottombar.setTextColor(resources.getColor(R.color.logo_green))
            val drawable_imageViewHidebottombar =
                ContextCompat.getDrawable(applicationContext, R.drawable.ic_toolbar_bottom)
            drawable_imageViewHidebottombar?.setColorFilter(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.logo_green
                ), PorterDuff.Mode.SRC_IN
            )
            imageViewHidebottombar.setImageDrawable(drawable_imageViewHidebottombar)


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

    private fun setDrawableColor(imageView: ImageView, drawableId: Int, colorId: Int) {
        val drawable = ContextCompat.getDrawable(applicationContext, drawableId)
        if (drawable != null) {
            drawable.setColorFilter(
                ContextCompat.getColor(applicationContext, colorId),
                PorterDuff.Mode.SRC_IN
            )
            imageView.setImageDrawable(drawable)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun showCustomProgressDialog(message: String) {
        try {
            customProgressDialog = Dialog(this)
            val binding: ProgressDialogLayoutBinding =
                ProgressDialogLayoutBinding.inflate(LayoutInflater.from(this))
            customProgressDialog!!.setContentView(binding.getRoot())
            customProgressDialog!!.setCancelable(false)
            customProgressDialog!!.setCanceledOnTouchOutside(false)
            customProgressDialog!!.getWindow()!!
                .setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            customProgressDialog!!.window!!.attributes.windowAnimations =
                R.style.PauseDialogAnimation

            binding.textLoading.setText(message)
            binding.imgCloseDialog.setVisibility(View.GONE)

            customProgressDialog!!.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("InflateParams", "SuspiciousIndentation")
    private fun showExitConfirmationDialog() {
        try {
            val binding: CustomConfirmExitDialogBinding =
                CustomConfirmExitDialogBinding.inflate(layoutInflater)
            val builder = AlertDialog.Builder(this)
            builder.setView(binding.getRoot())
            val alertDialog = builder.create()
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.setCancelable(false)

            // Set the background of the AlertDialog to be transparent
            if (alertDialog.window != null) {
                alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                alertDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
            }

            val editTextText2: EditText = binding.editTextText2
            val textHome: TextView = binding.textHome
            val textView4: TextView = binding.textView4
            val textContinueLogin: TextView = binding.textLoginAdmin2
            val textLogoutButton: TextView = binding.textLogoutButton
            val textExit: TextView = binding.textExit
            val textSettings: TextView = binding.textAppSettings
            val textAppAdmin: TextView = binding.textAppAdmin
            val textReSync: TextView = binding.textReSync
            val btnMobilAppSettings: TextView = binding.btnMobilAppSettings
            val btnMobilAppAdmin: TextView = binding.btnMobilAppAdmin
            val textLaunchOnline: TextView = binding.textLaunchOnline
            val textLaunchOffline: TextView = binding.textLaunchOffline
            val textForgetPassword: TextView = binding.textForgetPasswordHome
            val textCanCellDialog: TextView = binding.textCanCellDialog
            val textAppSettings: TextView = binding.textAppSettings
            val textForgetPasswordHome: TextView = binding.textForgetPasswordHome
            val imagePassowrdSettings: ImageView = binding.imagePassowrdSettings
            val imgClearCatch: ImageView = binding.imgClearCatch
            val imgWifi: ImageView = binding.imgWifi
            val imgMaintainace: ImageView = binding.imgMaintainace
            val divider2: View = binding.divider2
            val consMainAlert_sub_layout: ConstraintLayout = binding.consMainAlertSubLayout
            val imgToggle: ImageView = binding.imgToggle
            val imgToggleNzotVisible: ImageView = binding.imgToggleNzotVisible


            val preferences =
                android.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)


            // Hide Some Buttons for Mobile Mode
            val get_INSTALL_TV_JSON_USER_CLICKED =
                sharedTVAPPModePreferences.getString(Constants.INSTALL_TV_JSON_USER_CLICKED, "")
                    .toString()
            val installTVMode =
                sharedTVAPPModePreferences.getBoolean(Constants.installTVMode, false)
            if (get_INSTALL_TV_JSON_USER_CLICKED == Constants.INSTALL_TV_JSON_USER_CLICKED) {
                if (installTVMode) {

                    btnMobilAppSettings.visibility = View.GONE
                    btnMobilAppAdmin.visibility = View.GONE

                    textReSync.visibility = View.VISIBLE
                    textLaunchOnline.visibility = View.VISIBLE
                    textLaunchOffline.visibility = View.VISIBLE
                    textAppSettings.visibility = View.VISIBLE
                    textAppAdmin.visibility = View.VISIBLE
                } else {
                    btnMobilAppSettings.visibility = View.VISIBLE
                    btnMobilAppAdmin.visibility = View.VISIBLE


                    textReSync.visibility = View.GONE
                    textLaunchOnline.visibility = View.GONE
                    textLaunchOffline.visibility = View.GONE
                    textAppSettings.visibility = View.GONE
                    textAppAdmin.visibility = View.GONE
                }
            }


            /// use previous json
            /// use previous json
            // Hide Some Buttons for Mobile Mode
            if (get_INSTALL_TV_JSON_USER_CLICKED != Constants.INSTALL_TV_JSON_USER_CLICKED) {
                val sharedBiometricPref =
                    getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)
                val get_AppMode =
                    sharedBiometricPref.getString(Constants.MY_TV_OR_APP_MODE, "").toString()
                if (get_AppMode == Constants.TV_Mode) {

                    btnMobilAppSettings.visibility = View.GONE
                    btnMobilAppAdmin.visibility = View.GONE

                    textReSync.visibility = View.VISIBLE
                    textLaunchOnline.visibility = View.VISIBLE
                    textLaunchOffline.visibility = View.VISIBLE
                    textAppSettings.visibility = View.VISIBLE
                    textAppAdmin.visibility = View.VISIBLE

                } else {


                    btnMobilAppSettings.visibility = View.VISIBLE
                    btnMobilAppAdmin.visibility = View.VISIBLE


                    textReSync.visibility = View.GONE
                    textLaunchOnline.visibility = View.GONE
                    textLaunchOffline.visibility = View.GONE
                    textAppSettings.visibility = View.GONE
                    textAppAdmin.visibility = View.GONE

                }
            }

            /// end part of  use previous json
            /// end part of  use previous json


            // Load the shake animation
            val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)


            imgToggle.setOnClickListener {
                imgToggle.visibility = View.INVISIBLE
                imgToggleNzotVisible.visibility = View.VISIBLE
                editTextText2.transformationMethod = null
                editTextText2.setSelection(editTextText2.length())
            }

            imgToggleNzotVisible.setOnClickListener {
                imgToggle.visibility = View.VISIBLE
                imgToggleNzotVisible.visibility = View.INVISIBLE
                editTextText2.transformationMethod = PasswordTransformationMethod.getInstance()
                editTextText2.setSelection(editTextText2.length())
            }


            textCanCellDialog.setOnClickListener {
                if (handlerMoveToWebviewPage != null) {
                    handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                }

                alertDialog.dismiss()

                handler.postDelayed(Runnable {
                    val getInfoPageState =
                        sharedBiometric.getString(Constants.FIRST_INFORMATION_PAGE_COMPLETED, "")
                            .toString()
                    if (getInfoPageState == Constants.FIRST_INFORMATION_PAGE_COMPLETED) {
                        startActivity(Intent(applicationContext, WebViewPage::class.java))
                        finish()
                    } else {
                        startActivity(Intent(applicationContext, InformationActivity::class.java))
                        finish()
                    }
                }, 500)

            }


            ///  Logic To remove Password
            get_Current_Time_State_for_Password(editTextText2, imgToggle, imgToggleNzotVisible)


            // remove password with Time
            val getPrefilledPassword =
                simpleSavedPassword.getString(Constants.passowrdPrefeilled, "").toString()
            val getPassTimeInt = simpleSavedPassword.getInt(Constants.REFRESH_PASSWORD, 1).toInt()
            if (getPrefilledPassword == Constants.passowrdPrefeilled) {

                imgToggle.visibility = View.INVISIBLE
                imgToggleNzotVisible.visibility = View.INVISIBLE

                val timeStamp = getPassTimeInt * 70 * 1000L

                handler.postDelayed(Runnable {
                    get_Current_Time_State_for_Password(
                        editTextText2,
                        imgToggle,
                        imgToggleNzotVisible
                    )
                }, timeStamp)

            }


            val getDidUserInputPassowrd222 =
                simpleSavedPassword.getString(Constants.Did_User_Input_PassWord, "").toString()
            val getPasswordPrefilled222 =
                simpleSavedPassword.getString(Constants.passowrdPrefeilled, "").toString()
            val getSimpleAdminPassword222 =
                simpleSavedPassword.getString(Constants.mySimpleSavedPassword, "").toString()

            val smPassowrd = getSimpleAdminPassword222

            textDetctor(smPassowrd, editTextText2, divider2)


            if (getPasswordPrefilled222 == Constants.passowrdPrefeilled) {
                editTextText2.setText(getSimpleAdminPassword222)
                editTextText2.isEnabled = false
            } else if (getDidUserInputPassowrd222 == Constants.Did_User_Input_PassWord) {
                editTextText2.isEnabled = true
                editTextText2.setText(getSimpleAdminPassword222)
            } else {
                editTextText2.isEnabled = true
            }




            textReSync.setOnClickListener {
                if (handlerMoveToWebviewPage != null) {
                    handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                }

                val getPasswordPrefilled =
                    simpleSavedPassword.getString(Constants.passowrdPrefeilled, "").toString()
                val getSimpleAdminPassword =
                    simpleSavedPassword.getString(Constants.mySimpleSavedPassword, "").toString()
                val editor = simpleSavedPassword.edit()

                val editTextText = editTextText2.text.toString().trim { it <= ' ' }
                if (getPasswordPrefilled == Constants.passowrdPrefeilled || editTextText == getSimpleAdminPassword) {
                    hideKeyBoard(editTextText2)
                    if (getPasswordPrefilled == Constants.passowrdPrefeilled) {
                        editor.putString(
                            Constants.Did_User_Input_PassWord,
                            Constants.Did_User_Input_PassWord
                        )
                        editor.apply()
                    }

                    val editor333 = sharedBiometric.edit()
                    editor333.putString(Constants.SAVE_NAVIGATION, Constants.SettingsPage)
                    editor333.apply()

                    startActivity(Intent(applicationContext, ReSyncActivity::class.java))
                    finish()

                    alertDialog.dismiss()

                } else {
                    hideKeyBoard(editTextText2)
                    showPop_For_wrong_Password("Wrong password")
                    editTextText2.error = "Wrong password"
                    editTextText2.setTextColor(resources.getColor(R.color.red))
                    editTextText2.setHintTextColor(resources.getColor(R.color.red))
                    editTextText2.startAnimation(shakeAnimation)
                    divider2.startAnimation(shakeAnimation)
                    divider2.setBackgroundColor(resources.getColor(R.color.red))
                }
            }



            imagePassowrdSettings.setOnClickListener {
                if (handlerMoveToWebviewPage != null) {
                    handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                }

                val getPasswordPrefilled =
                    simpleSavedPassword.getString(Constants.passowrdPrefeilled, "").toString()
                val getSimpleAdminPassword =
                    simpleSavedPassword.getString(Constants.mySimpleSavedPassword, "").toString()


                val editTextText = editTextText2.text.toString().trim { it <= ' ' }
                if (getPasswordPrefilled == Constants.passowrdPrefeilled || editTextText == getSimpleAdminPassword) {

                    val editor = simpleSavedPassword.edit()
                    if (getPasswordPrefilled == Constants.passowrdPrefeilled) {
                        editor.putString(
                            Constants.Did_User_Input_PassWord,
                            Constants.Did_User_Input_PassWord
                        )
                        editor.apply()
                    }

                    val editor333 = sharedBiometric.edit()
                    editor333.putString(Constants.SAVE_NAVIGATION, Constants.SettingsPage)
                    editor333.apply()


                    startActivity(Intent(applicationContext, PasswordActivity::class.java))
                    finish()

                    hideKeyBoard(editTextText2)
                    alertDialog.dismiss();

                } else {
                    hideKeyBoard(editTextText2)
                    showPop_For_wrong_Password("Wrong password")
                    editTextText2.error = "Wrong password"
                    editTextText2.setTextColor(resources.getColor(R.color.red))
                    editTextText2.setHintTextColor(resources.getColor(R.color.red))
                    editTextText2.startAnimation(shakeAnimation)
                    divider2.startAnimation(shakeAnimation)
                    divider2.setBackgroundColor(resources.getColor(R.color.red))
                }
            }


            imgWifi.setOnClickListener {
                if (handlerMoveToWebviewPage != null) {
                    handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                }
                val getPasswordPrefilled =
                    simpleSavedPassword.getString(Constants.passowrdPrefeilled, "").toString()
                val getSimpleAdminPassword =
                    simpleSavedPassword.getString(Constants.mySimpleSavedPassword, "").toString()
                val editor = simpleSavedPassword.edit()

                val editTextText = editTextText2.text.toString().trim { it <= ' ' }
                if (getPasswordPrefilled == Constants.passowrdPrefeilled || editTextText == getSimpleAdminPassword) {
                    hideKeyBoard(editTextText2)
                    val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                    startActivity(intent)
                    if (getPasswordPrefilled == Constants.passowrdPrefeilled) {
                        editor.putString(
                            Constants.Did_User_Input_PassWord,
                            Constants.Did_User_Input_PassWord
                        )
                        editor.apply()
                    }
                    // alertDialog.dismiss();
                } else {
                    hideKeyBoard(editTextText2)
                    showPop_For_wrong_Password("Wrong password")
                    editTextText2.error = "Wrong password"
                    editTextText2.setTextColor(resources.getColor(R.color.red))
                    editTextText2.setHintTextColor(resources.getColor(R.color.red))
                    editTextText2.startAnimation(shakeAnimation)
                    divider2.startAnimation(shakeAnimation)
                    divider2.setBackgroundColor(resources.getColor(R.color.red))
                }
            }



            imgClearCatch.setOnClickListener {
                if (handlerMoveToWebviewPage != null) {
                    handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                }
                val getPasswordPrefilled =
                    simpleSavedPassword.getString(Constants.passowrdPrefeilled, "").toString()
                val getSimpleAdminPassword =
                    simpleSavedPassword.getString(Constants.mySimpleSavedPassword, "").toString()
                val editor = simpleSavedPassword.edit()

                val editTextText = editTextText2.text.toString().trim { it <= ' ' }
                if (getPasswordPrefilled == Constants.passowrdPrefeilled || editTextText == getSimpleAdminPassword) {
                    hideKeyBoard(editTextText2)
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                    if (getPasswordPrefilled == Constants.passowrdPrefeilled) {
                        editor.putString(
                            Constants.Did_User_Input_PassWord,
                            Constants.Did_User_Input_PassWord
                        )
                        editor.apply()
                    }

                    //  alertDialog.dismiss();
                } else {
                    hideKeyBoard(editTextText2)
                    showPop_For_wrong_Password("Wrong password")
                    editTextText2.error = "Wrong password"
                    editTextText2.setTextColor(resources.getColor(R.color.red))
                    editTextText2.setHintTextColor(resources.getColor(R.color.red))
                    editTextText2.startAnimation(shakeAnimation)
                    divider2.startAnimation(shakeAnimation)
                    divider2.setBackgroundColor(resources.getColor(R.color.red))
                }
            }




            textSettings.setOnClickListener {
                if (handlerMoveToWebviewPage != null) {
                    handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                }

                val getPasswordPrefilled =
                    simpleSavedPassword.getString(Constants.passowrdPrefeilled, "").toString()
                val getSimpleAdminPassword =
                    simpleSavedPassword.getString(Constants.mySimpleSavedPassword, "").toString()
                val editor = simpleSavedPassword.edit()

                val editTextText = editTextText2.text.toString().trim { it <= ' ' }
                if (getPasswordPrefilled == Constants.passowrdPrefeilled || editTextText == getSimpleAdminPassword) {

                    hideKeyBoard(editTextText2)
                    if (getPasswordPrefilled == Constants.passowrdPrefeilled) {
                        editor.putString(
                            Constants.Did_User_Input_PassWord,
                            Constants.Did_User_Input_PassWord
                        )
                        editor.apply()
                    }

                    alertDialog.dismiss()

                } else {
                    hideKeyBoard(editTextText2)
                    showPop_For_wrong_Password("Wrong password")
                    editTextText2.error = "Wrong password"
                    editTextText2.setTextColor(resources.getColor(R.color.red))
                    editTextText2.setHintTextColor(resources.getColor(R.color.red))
                    editTextText2.startAnimation(shakeAnimation)
                    divider2.startAnimation(shakeAnimation)
                    divider2.setBackgroundColor(resources.getColor(R.color.red))
                }
            }

            btnMobilAppSettings.setOnClickListener {
                if (handlerMoveToWebviewPage != null) {
                    handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                }
                val getPasswordPrefilled =
                    simpleSavedPassword.getString(Constants.passowrdPrefeilled, "").toString()
                val getSimpleAdminPassword =
                    simpleSavedPassword.getString(Constants.mySimpleSavedPassword, "").toString()
                val editor = simpleSavedPassword.edit()

                val editTextText = editTextText2.text.toString().trim { it <= ' ' }
                if (getPasswordPrefilled == Constants.passowrdPrefeilled || editTextText == getSimpleAdminPassword) {
                    hideKeyBoard(editTextText2)

                    if (getPasswordPrefilled == Constants.passowrdPrefeilled) {
                        editor.putString(
                            Constants.Did_User_Input_PassWord,
                            Constants.Did_User_Input_PassWord
                        )
                        editor.apply()
                    }

                    alertDialog.dismiss()

                } else {
                    hideKeyBoard(editTextText2)
                    showPop_For_wrong_Password("Wrong password")
                    editTextText2.error = "Wrong password"
                    editTextText2.setTextColor(resources.getColor(R.color.red))
                    editTextText2.setHintTextColor(resources.getColor(R.color.red))
                    editTextText2.startAnimation(shakeAnimation)
                    divider2.startAnimation(shakeAnimation)
                    divider2.setBackgroundColor(resources.getColor(R.color.red))
                }
            }



            textAppAdmin.setOnClickListener {
                if (handlerMoveToWebviewPage != null) {
                    handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                }
                val getPasswordPrefilled =
                    simpleSavedPassword.getString(Constants.passowrdPrefeilled, "").toString()
                val getSimpleAdminPassword =
                    simpleSavedPassword.getString(Constants.mySimpleSavedPassword, "").toString()
                val editor = simpleSavedPassword.edit()

                val editTextText = editTextText2.text.toString().trim { it <= ' ' }
                if (getPasswordPrefilled == Constants.passowrdPrefeilled || editTextText == getSimpleAdminPassword) {
                    hideKeyBoard(editTextText2)

                    if (getPasswordPrefilled == Constants.passowrdPrefeilled) {
                        editor.putString(
                            Constants.Did_User_Input_PassWord,
                            Constants.Did_User_Input_PassWord
                        )
                        editor.apply()
                    }

                    val editor333 = sharedBiometric.edit()
                    editor333.putString(Constants.SAVE_NAVIGATION, Constants.SettingsPage)
                    editor333.apply()


                    val myactivity =
                        Intent(this@SettingsActivityKT, AdditionalSettingsActivity::class.java)
                    startActivity(myactivity)
                    finish()


                    alertDialog.dismiss()
                } else {
                    hideKeyBoard(editTextText2)
                    showPop_For_wrong_Password("Wrong password")
                    editTextText2.error = "Wrong password"
                    editTextText2.setTextColor(resources.getColor(R.color.red))
                    editTextText2.setHintTextColor(resources.getColor(R.color.red))
                    editTextText2.startAnimation(shakeAnimation)
                    divider2.startAnimation(shakeAnimation)
                    divider2.setBackgroundColor(resources.getColor(R.color.red))
                }
            }

            btnMobilAppAdmin.setOnClickListener {
                if (handlerMoveToWebviewPage != null) {
                    handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                }
                val getPasswordPrefilled =
                    simpleSavedPassword.getString(Constants.passowrdPrefeilled, "").toString()
                val getSimpleAdminPassword =
                    simpleSavedPassword.getString(Constants.mySimpleSavedPassword, "").toString()
                val editor = simpleSavedPassword.edit()

                val editTextText = editTextText2.text.toString().trim { it <= ' ' }
                if (getPasswordPrefilled == Constants.passowrdPrefeilled || editTextText == getSimpleAdminPassword) {
                    hideKeyBoard(editTextText2)
                    if (getPasswordPrefilled == Constants.passowrdPrefeilled) {
                        editor.putString(
                            Constants.Did_User_Input_PassWord,
                            Constants.Did_User_Input_PassWord
                        )
                        editor.apply()
                    }


                    val editor333 = sharedBiometric.edit()
                    editor333.putString(Constants.SAVE_NAVIGATION, Constants.SettingsPage)
                    editor333.apply()


                    val myactivity =
                        Intent(this@SettingsActivityKT, AdditionalSettingsActivity::class.java)
                    startActivity(myactivity)
                    finish()


                    alertDialog.dismiss()
                } else {
                    hideKeyBoard(editTextText2)
                    showPop_For_wrong_Password("Wrong password")
                    editTextText2.error = "Wrong password"
                    editTextText2.setTextColor(resources.getColor(R.color.red))
                    editTextText2.setHintTextColor(resources.getColor(R.color.red))
                    editTextText2.startAnimation(shakeAnimation)
                    divider2.startAnimation(shakeAnimation)
                    divider2.setBackgroundColor(resources.getColor(R.color.red))
                }
            }





            imgMaintainace.setOnClickListener {
                if (handlerMoveToWebviewPage != null) {
                    handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                }
                val getPasswordPrefilled =
                    simpleSavedPassword.getString(Constants.passowrdPrefeilled, "").toString()
                val getSimpleAdminPassword =
                    simpleSavedPassword.getString(Constants.mySimpleSavedPassword, "").toString()
                val editor = simpleSavedPassword.edit()

                val editTextText = editTextText2.text.toString().trim { it <= ' ' }
                if (getPasswordPrefilled == Constants.passowrdPrefeilled || editTextText == getSimpleAdminPassword) {
                    hideKeyBoard(editTextText2)
                    if (getPasswordPrefilled == Constants.passowrdPrefeilled) {
                        editor.putString(
                            Constants.Did_User_Input_PassWord,
                            Constants.Did_User_Input_PassWord
                        )
                        editor.apply()
                    }

                    val editor333 = sharedBiometric.edit()
                    editor333.putString(Constants.SAVE_NAVIGATION, Constants.SettingsPage)
                    editor333.apply()

                    val myactivity =
                        Intent(this@SettingsActivityKT, MaintenanceActivity::class.java)
                    startActivity(myactivity)
                    finish()

                    alertDialog.dismiss();

                } else {
                    hideKeyBoard(editTextText2)
                    showPop_For_wrong_Password("Wrong password")
                    editTextText2.error = "Wrong password"
                    editTextText2.setTextColor(resources.getColor(R.color.red))
                    editTextText2.setHintTextColor(resources.getColor(R.color.red))
                    editTextText2.startAnimation(shakeAnimation)
                    divider2.startAnimation(shakeAnimation)
                    divider2.setBackgroundColor(resources.getColor(R.color.red))
                }
            }



            textExit.setOnClickListener {
                if (handlerMoveToWebviewPage != null) {
                    handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                }

                hideKeyBoard(editTextText2)

                val getPasswordPrefilled =
                    simpleSavedPassword.getString(Constants.passowrdPrefeilled, "").toString()
                val getSimpleAdminPassword =
                    simpleSavedPassword.getString(Constants.mySimpleSavedPassword, "").toString()

                val editTextText = editTextText2.text.toString().trim { it <= ' ' }

                if (getPasswordPrefilled == Constants.passowrdPrefeilled || editTextText == getSimpleAdminPassword) {

                    val lockDown =
                        sharedBiometric.getString(Constants.imgEnableLockScreen, "").toString()

                    if (lockDown == Constants.imgEnableLockScreen) {

                        showToastMessage("Kindly Remove App from Lock down mode")

                    } else {

                        val editor = myDownloadMangerClass.edit()
                        editor.remove(Constants.SynC_Status)
                        editor.apply()
                        second_cancel_download()

                        val editor22 = simpleSavedPassword.edit()
                        editor22.remove(Constants.Did_User_Input_PassWord)
                        editor22.apply()


                        alertDialog.dismiss()

                        showToastMessage("Please wait...")
                        handler.postDelayed({
                            finishAndRemoveTask()
                            Process.killProcess(Process.myPid())

                            //  finishAffinity()
                            // System.exit(0)

                        }, 700)

                    }

                } else {

                    hideKeyBoard(editTextText2)
                    showPop_For_wrong_Password("Wrong password")
                    editTextText2.error = "Wrong password"
                    editTextText2.setTextColor(resources.getColor(R.color.red))
                    editTextText2.setHintTextColor(resources.getColor(R.color.red))
                    editTextText2.startAnimation(shakeAnimation)
                    divider2.startAnimation(shakeAnimation)
                    divider2.setBackgroundColor(resources.getColor(R.color.red))

                }


            }



            textForgetPassword.setOnClickListener {
                if (handlerMoveToWebviewPage != null) {
                    handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                }
                val isSavedEmail =
                    simpleSavedPassword.getString(Constants.isSavedEmail, "").toString()
                hideKeyBoard(editTextText2)
                if (isSavedEmail.isNotEmpty() && isValidEmail(isSavedEmail)) {

                    showPopChangePassowrdDialog()
                    alertDialog.dismiss()

                } else {
                    showPopRedirectuser()
                    alertDialog.dismiss()
                }
            }





            textLogoutButton.setOnClickListener {
                if (handlerMoveToWebviewPage != null) {
                    handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                }
                val getPasswordPrefilled =
                    simpleSavedPassword.getString(Constants.passowrdPrefeilled, "").toString()
                val getSimpleAdminPassword =
                    simpleSavedPassword.getString(Constants.mySimpleSavedPassword, "").toString()

                val editTextText = editTextText2.text.toString().trim { it <= ' ' }

                if (getPasswordPrefilled == Constants.passowrdPrefeilled || editTextText == getSimpleAdminPassword) {

                    val sharedBiometric =
                        getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)
                    val editor_sharedBiometric = sharedBiometric.edit()
                    editor_sharedBiometric.remove(Constants.MY_TV_OR_APP_MODE)
                    editor_sharedBiometric.remove(Constants.FIRST_TIME_APP_START)
                    editor_sharedBiometric.remove(Constants.Did_User_Input_PassWord)
                    editor_sharedBiometric.apply()

                    second_cancel_download()
                    hideKeyBoard(editTextText2)

                    alertDialog.dismiss()

                    val handler1 = Handler(Looper.getMainLooper())
                    handler1.postDelayed({
                        val myactivity =
                            Intent(this@SettingsActivityKT, TvActivityOrAppMode::class.java)
                        startActivity(myactivity)
                        finish()
                    }, 200)


                } else {
                    hideKeyBoard(editTextText2)
                    showPop_For_wrong_Password("Wrong password")
                    editTextText2.error = "Wrong password"
                    editTextText2.setTextColor(resources.getColor(R.color.red))
                    editTextText2.setHintTextColor(resources.getColor(R.color.red))
                    editTextText2.startAnimation(shakeAnimation)
                    divider2.startAnimation(shakeAnimation)
                    divider2.setBackgroundColor(resources.getColor(R.color.red))
                }


            }






            textLaunchOnline.setOnClickListener {
                if (handlerMoveToWebviewPage != null) {
                    handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                }
                hideKeyBoard(editTextText2)
                val getPasswordPrefilled =
                    simpleSavedPassword.getString(Constants.passowrdPrefeilled, "").toString()
                val getSimpleAdminPassword =
                    simpleSavedPassword.getString(Constants.mySimpleSavedPassword, "").toString()
                val editor = simpleSavedPassword.edit()

                val editTextText = editTextText2.text.toString().trim { it <= ' ' }

                val getTvMode =
                    sharedBiometric.getString(Constants.MY_TV_OR_APP_MODE, "").toString()
                if (getPasswordPrefilled == Constants.passowrdPrefeilled || editTextText == getSimpleAdminPassword) {
                    if (getTvMode == Constants.TV_Mode) {
                        if (getPasswordPrefilled == Constants.passowrdPrefeilled) {
                            editor.putString(
                                Constants.Did_User_Input_PassWord,
                                Constants.Did_User_Input_PassWord
                            )
                        }
                        editor.putString(
                            Constants.imgAllowLunchFromOnline,
                            "imgAllowLunchFromOnline"
                        )
                        editor.apply()

                        val imagSwtichEnableManualOrNot =
                            sharedBiometric.getString(Constants.imagSwtichEnableManualOrNot, "")
                                .toString()
                        if (imagSwtichEnableManualOrNot.equals(Constants.imagSwtichEnableManualOrNot)) {
                            val editText88 = sharedBiometric.edit()
                            editText88.putString(
                                Constants.get_Launching_State_Of_WebView,
                                Constants.launch_WebView_Online_Manual_Index
                            )
                            editText88.apply()
                        } else {
                            val editText88 = sharedBiometric.edit()
                            editText88.putString(
                                Constants.get_Launching_State_Of_WebView,
                                Constants.launch_WebView_Online
                            )
                            editText88.apply()
                        }


                        val intent = Intent(applicationContext, WebViewPage::class.java)
                        startActivity(intent)
                        finish()


                        alertDialog.dismiss()

                    } else {

                        if (getPasswordPrefilled == Constants.passowrdPrefeilled) {
                            editor.putString(
                                Constants.Did_User_Input_PassWord,
                                Constants.Did_User_Input_PassWord
                            )
                        }
                        editor.remove(Constants.imgAllowLunchFromOnline)
                        editor.apply()


                        val editText88 = sharedBiometric.edit()
                        editText88.putString(
                            Constants.get_Launching_State_Of_WebView,
                            Constants.launch_Default_WebView_url
                        )
                        editText88.apply()


                        val intent = Intent(applicationContext, SplashKT::class.java)
                        startActivity(intent)
                        finish()


                        alertDialog.dismiss()


                    }
                } else {
                    hideKeyBoard(editTextText2)
                    showPop_For_wrong_Password("Wrong password")
                    editTextText2.error = "Wrong password"
                    editTextText2.setTextColor(resources.getColor(R.color.red))
                    editTextText2.setHintTextColor(resources.getColor(R.color.red))
                    editTextText2.startAnimation(shakeAnimation)
                    divider2.startAnimation(shakeAnimation)
                    divider2.setBackgroundColor(resources.getColor(R.color.red))
                }
            }


            textLaunchOffline.setOnClickListener {
                if (handlerMoveToWebviewPage != null) {
                    handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                }
                hideKeyBoard(editTextText2)
                val getPasswordPrefilled =
                    simpleSavedPassword.getString(Constants.passowrdPrefeilled, "").toString()
                val getSimpleAdminPassword =
                    simpleSavedPassword.getString(Constants.mySimpleSavedPassword, "").toString()
                val editor = simpleSavedPassword.edit()

                val editTextText = editTextText2.text.toString().trim { it <= ' ' }
                if (getPasswordPrefilled == Constants.passowrdPrefeilled || editTextText == getSimpleAdminPassword) {

                    if (getPasswordPrefilled == Constants.passowrdPrefeilled) {
                        editor.putString(
                            Constants.Did_User_Input_PassWord,
                            Constants.Did_User_Input_PassWord
                        )
                    }

                    editor.remove(Constants.imgAllowLunchFromOnline)
                    editor.apply()


                    val imagSwtichEnableManualOrNot =
                        sharedBiometric.getString(Constants.imagSwtichEnableManualOrNot, "")
                            .toString()
                    if (imagSwtichEnableManualOrNot.equals(Constants.imagSwtichEnableManualOrNot)) {
                        val editText88 = sharedBiometric.edit()
                        editText88.putString(
                            Constants.get_Launching_State_Of_WebView,
                            Constants.launch_WebView_Offline_Manual_Index
                        )
                        editText88.apply()
                    } else {
                        val editText88 = sharedBiometric.edit()
                        editText88.putString(
                            Constants.get_Launching_State_Of_WebView,
                            Constants.launch_WebView_Offline
                        )
                        editText88.apply()
                    }


                    val intent = Intent(applicationContext, WebViewPage::class.java)
                    startActivity(intent)
                    finish()


                    alertDialog.dismiss()


                } else {
                    hideKeyBoard(editTextText2)
                    showPop_For_wrong_Password("Wrong password")
                    editTextText2.error = "Wrong password"
                    editTextText2.setTextColor(resources.getColor(R.color.red))
                    editTextText2.setHintTextColor(resources.getColor(R.color.red))
                    editTextText2.startAnimation(shakeAnimation)
                    divider2.startAnimation(shakeAnimation)
                    divider2.setBackgroundColor(resources.getColor(R.color.red))
                }
            }



            textHome.setOnClickListener {
                if (handlerMoveToWebviewPage != null) {
                    handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                }
                val getPasswordPrefilled =
                    simpleSavedPassword.getString(Constants.passowrdPrefeilled, "").toString()
                val getSimpleAdminPassword =
                    simpleSavedPassword.getString(Constants.mySimpleSavedPassword, "").toString()

                val editTextText = editTextText2.text.toString().trim { it <= ' ' }
                if (getPasswordPrefilled == Constants.passowrdPrefeilled || editTextText == getSimpleAdminPassword) {

                    hideKeyBoard(editTextText2)
                    moveTaskToBack(true)


                } else {
                    hideKeyBoard(editTextText2)
                    // showToastMessage("Wrong password")
                    showPop_For_wrong_Password("Wrong password")
                    editTextText2.error = "Wrong password"
                    editTextText2.setTextColor(resources.getColor(R.color.red))
                    editTextText2.setHintTextColor(resources.getColor(R.color.red))
                    editTextText2.startAnimation(shakeAnimation)
                    divider2.startAnimation(shakeAnimation)
                    divider2.setBackgroundColor(resources.getColor(R.color.red))
                }

            }



            textContinueLogin.setOnClickListener {
                if (handlerMoveToWebviewPage != null) {
                    handlerMoveToWebviewPage.removeCallbacksAndMessages(null)
                }
                val getPasswordPrefilled =
                    simpleSavedPassword.getString(Constants.passowrdPrefeilled, "").toString()
                val getSimpleAdminPassword =
                    simpleSavedPassword.getString(Constants.mySimpleSavedPassword, "").toString()
                val editor = simpleSavedPassword.edit()

                val editTextText = editTextText2.text.toString().trim { it <= ' ' }

                if (getPasswordPrefilled == Constants.passowrdPrefeilled || editTextText == getSimpleAdminPassword) {
                    hideKeyBoard(editTextText2)
                    if (getPasswordPrefilled == Constants.passowrdPrefeilled) {
                        editor.putString(
                            Constants.Did_User_Input_PassWord,
                            Constants.Did_User_Input_PassWord
                        )
                        editor.apply()
                    }


                    alertDialog.dismiss()

                } else {
                    hideKeyBoard(editTextText2)
                    showPop_For_wrong_Password("Wrong password")
                    editTextText2.error = "Wrong password"
                    editTextText2.setTextColor(resources.getColor(R.color.red))
                    editTextText2.setHintTextColor(resources.getColor(R.color.red))
                    editTextText2.startAnimation(shakeAnimation)
                    divider2.startAnimation(shakeAnimation)
                    divider2.setBackgroundColor(resources.getColor(R.color.red))
                }
            }

            alertDialog.show()
        } catch (e: Exception) {
            Log.d(TAG, "showExitConfirmationDialog: Erro ${e.message}")
        }
    }

    private fun textDetctor(smPassowrd: String, editTextText2: EditText, divider2: View) {
        try {


            editTextText2.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    try {

                        val passowrd = editTextText2.text.toString().trim()

                        if (smPassowrd == passowrd) {
                            editTextText2.setBackgroundColor(resources.getColor(R.color.zxing_transparent))
                            editTextText2.setTextColor(resources.getColor(R.color.deep_green))
                            divider2.setBackgroundColor(resources.getColor(R.color.deep_green))
                        } else {
                            editTextText2.setBackgroundColor(resources.getColor(R.color.zxing_transparent))
                            editTextText2.setTextColor(resources.getColor(R.color.red))
                            divider2.setBackgroundColor(resources.getColor(R.color.red))

                        }


                    } catch (_: Exception) {
                    }
                }
            })


        } catch (e: Exception) {
        }
    }


    @SuppressLint("MissingInflatedId")
    private fun showPop_For_wrong_Password(message: String) {

        val binding: CustomFailedLayoutBinding = CustomFailedLayoutBinding.inflate(layoutInflater)
        val alertDialogBuilder = android.app.AlertDialog.Builder(this)
        alertDialogBuilder.setView(binding.root)

        val alertDialog = alertDialogBuilder.create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation

        binding.apply {

            textView9.text = message

            textContinuPassword2.setOnClickListener {

                if (Constants.IN_VALID_EMAIL == message) {
                    showPopRedirectuser()
                }

                alertDialog.dismiss()
            }

        }


        alertDialog.show()


    }


    @SuppressLint("MissingInflatedId")
    private fun showPopRedirectuser() {
        val bindingCM: CustomRedirectEmailLayoutBinding = CustomRedirectEmailLayoutBinding.inflate(
            layoutInflater
        )
        val builder = AlertDialog.Builder(this)
        builder.setView(bindingCM.getRoot())
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setCancelable(false)
        if (alertDialog.window != null) {
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
        }

        val textClickHere: TextView = bindingCM.textClickHere
        val textOkayBtn: TextView = bindingCM.textOkayBtn
        val imgCloseDialog2: ImageView = bindingCM.imgCloseDialogForegetPassword


        textOkayBtn.setOnClickListener {
            if (isConnected()) {
                val simpleAdminPassword =
                    simpleSavedPassword.getString(Constants.mySimpleSavedPassword, "").toString()
                val editTextText = bindingCM.editTextEmail.text.toString().trim { it <= ' ' }
                if (editTextText.isNotEmpty() && isValidEmail(editTextText)) {
                    val editor = simpleSavedPassword.edit()
                    editor.remove(Constants.Did_User_Input_PassWord)
                    editor.putString(Constants.isSavedEmail, editTextText)
                    editor.apply()
                    sendMessage(editTextText, simpleAdminPassword)

                    alertDialog.dismiss()
                } else {
                    showPop_For_wrong_Password(Constants.IN_VALID_EMAIL)
                    alertDialog.dismiss()
                }
            } else {
                showToastMessage("No internet Connection")

            }

        }




        textClickHere.setOnClickListener {

            if (isConnected()) {

                val name = simpleSavedPassword.getString(Constants.USER_NAME, "").toString()
                val phone = simpleSavedPassword.getString(Constants.USER_PHONE, "").toString()
                val isSavedEmail =
                    simpleSavedPassword.getString(Constants.isSavedEmail, "").toString()
                val companyName =
                    simpleSavedPassword.getString(Constants.USER_COMPANY_NAME, "").toString()
                val countryName =
                    simpleSavedPassword.getString(Constants.COUNTRY_NAME, "").toString()
                val countryCode =
                    simpleSavedPassword.getString(Constants.COUNTRY_CODE, "").toString()

                // Generate a random numeric password with a maximum length of 5 characters
                val password = generateRandomNumericPassword(5)

                // Retrieve additional information
                val getFolderClo =
                    myDownloadMangerClass.getString(Constants.getFolderClo, "").toString()
                val getFolderSubpath =
                    myDownloadMangerClass.getString(Constants.getFolderSubpath, "").toString()

                // Check for null or empty email and phone number
                var _name = if (name.isEmpty()) "The User Name is Empty" else name
                var _mCompanyName =
                    if (companyName.isEmpty()) "The User Company Name is Empty" else companyName
                var _mEmail =
                    if (isSavedEmail.isEmpty()) "The User Email is Empty" else isSavedEmail
                var _mPhone = if (phone.isEmpty()) "The User Phone Number is Empty" else phone

                // Using StringBuilder to construct the user details
                val userDetailsBuilder = StringBuilder()
                userDetailsBuilder.append("Name: ").append(_name).append("\n")
                userDetailsBuilder.append("Company Name: ").append(_mCompanyName).append("\n")
                userDetailsBuilder.append("Country Name: ").append(countryName).append("\n")
                userDetailsBuilder.append("Country Code: ").append(countryCode).append("\n")
                userDetailsBuilder.append("Phone Number: ").append(_mPhone).append("\n")
                userDetailsBuilder.append("Email: ").append(_mEmail).append("\n")
                userDetailsBuilder.append("Password: ").append(password).append("\n")
                userDetailsBuilder.append("Company/User ID: ").append(getFolderClo).append("\n")
                userDetailsBuilder.append("License Key: ").append(getFolderSubpath).append("\n")

                // Convert StringBuilder to String
                val userDetails = userDetailsBuilder.toString()

                // Using StringBuilder to construct the device information
                val deviceInfoBuilder = StringBuilder()
                deviceInfoBuilder.append("Device Name: ").append(Build.DEVICE).append("\n")
                deviceInfoBuilder.append("Model: ").append(Build.MODEL).append("\n")
                deviceInfoBuilder.append("Manufacturer: ").append(Build.MANUFACTURER).append("\n")
                deviceInfoBuilder.append("Brand: ").append(Build.BRAND).append("\n")
                deviceInfoBuilder.append("OS Version: ").append(Build.VERSION.RELEASE).append("\n")
                deviceInfoBuilder.append("SDK Version: ").append(Build.VERSION.SDK_INT).append("\n")
                deviceInfoBuilder.append("Build Number: ").append(Build.DISPLAY).append("\n")

                // Convert StringBuilder to String
                val deviceInformation = deviceInfoBuilder.toString()

                // Combine user details and device information
                val emailContent = "$userDetails\n\nDevice Information\n\n$deviceInformation"

                val email = Constants.COMPANY_EMAIL

                // save the password
                val editor22 = simpleSavedPassword.edit()
                editor22.remove(Constants.Did_User_Input_PassWord)
                editor22.putString(Constants.mySimpleSavedPassword, password)
                editor22.apply()

                // send the email s data
                sendMessage(email, emailContent)
                alertDialog.dismiss()
            } else {
                showToastMessage("No internet Connection")
            }
        }




        imgCloseDialog2.setOnClickListener {

            showExitConfirmationDialog()
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    private fun generateRandomNumericPassword(length: Int): String {
        require(length in 1..5) { "Password length must be between 1 and 5" }

        val digits = "0123456789"
        val random = SecureRandom()
        return (1..length)
            .map { digits[random.nextInt(digits.length)] }
            .joinToString("")
    }


    @SuppressLint("MissingInflatedId", "SetTextI18n")
    private fun showPopChangePassowrdDialog() {
        val binding: CustomForgetPasswordEmailLayoutBinding =
            CustomForgetPasswordEmailLayoutBinding.inflate(
                layoutInflater
            )
        val builder = AlertDialog.Builder(this)
        builder.setView(binding.getRoot())
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setCancelable(false)
        if (alertDialog.window != null) {
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
        }
        val editTextInputUrl: TextView = binding.eitTextEnterPassword
        val textContinuPassword: TextView = binding.textContinuPassword
        val textClickHere: TextView = binding.textClickHere
        val imgCloseDialog2: ImageView = binding.imgCloseDialogForegetPassword
        val divider2: View = binding.divider2


        val imgIsemailVisbile =
            simpleSavedPassword.getString(Constants.imagEnableEmailVisisbility, "").toString()
        val simpleAdminPassword =
            simpleSavedPassword.getString(Constants.mySimpleSavedPassword, "").toString()
        val isSavedEmail = simpleSavedPassword.getString(Constants.isSavedEmail, "").toString()


        if (imgIsemailVisbile == Constants.imagEnableEmailVisisbility) {
            if (isSavedEmail.isNotEmpty()) {
                editTextInputUrl.text = isSavedEmail + ""
                divider2.visibility = View.VISIBLE
                editTextInputUrl.visibility = View.VISIBLE
            }
        } else {
            editTextInputUrl.isEnabled = true
            divider2.visibility = View.VISIBLE
            editTextInputUrl.text = "******************"
        }

        textContinuPassword.setOnClickListener {
            if (isSavedEmail.isNotEmpty() && isValidEmail(isSavedEmail)) {
                if (isConnected()) {

                    val editor = simpleSavedPassword.edit()
                    editor.remove(Constants.Did_User_Input_PassWord)
                    editor.apply()

                    sendMessage(isSavedEmail, simpleAdminPassword)
                    alertDialog.dismiss()

                } else {
                    showToastMessage("No internet Connection")
                }
            } else {
                showPopRedirectuser()
                alertDialog.dismiss()
            }
        }






        textClickHere.setOnClickListener {

            if (isConnected()) {

                val name = simpleSavedPassword.getString(Constants.USER_NAME, "").toString()
                val phone = simpleSavedPassword.getString(Constants.USER_PHONE, "").toString()
                val isSavedEmail =
                    simpleSavedPassword.getString(Constants.isSavedEmail, "").toString()
                val companyName =
                    simpleSavedPassword.getString(Constants.USER_COMPANY_NAME, "").toString()
                val countryName =
                    simpleSavedPassword.getString(Constants.COUNTRY_NAME, "").toString()
                val countryCode =
                    simpleSavedPassword.getString(Constants.COUNTRY_CODE, "").toString()

                // Generate a random numeric password with a maximum length of 5 characters
                val password = generateRandomNumericPassword(5)

                // Retrieve additional information
                val getFolderClo =
                    myDownloadMangerClass.getString(Constants.getFolderClo, "").toString()
                val getFolderSubpath =
                    myDownloadMangerClass.getString(Constants.getFolderSubpath, "").toString()

                // Check for null or empty email and phone number
                var _name = if (name.isEmpty()) "The User Name is Empty" else name
                var _mCompanyName =
                    if (companyName.isEmpty()) "The User Company Name is Empty" else companyName
                var _mEmail =
                    if (isSavedEmail.isEmpty()) "The User Email is Empty" else isSavedEmail
                var _mPhone = if (phone.isEmpty()) "The User Phone Number is Empty" else phone

                // Using StringBuilder to construct the user details
                val userDetailsBuilder = StringBuilder()
                userDetailsBuilder.append("Name: ").append(_name).append("\n")
                userDetailsBuilder.append("Company Name: ").append(_mCompanyName).append("\n")
                userDetailsBuilder.append("Country Name: ").append(countryName).append("\n")
                userDetailsBuilder.append("Country Code: ").append(countryCode).append("\n")
                userDetailsBuilder.append("Phone Number: ").append(_mPhone).append("\n")
                userDetailsBuilder.append("Email: ").append(_mEmail).append("\n")
                userDetailsBuilder.append("Password: ").append(password).append("\n")
                userDetailsBuilder.append("Company/User ID: ").append(getFolderClo).append("\n")
                userDetailsBuilder.append("License Key: ").append(getFolderSubpath).append("\n")

                // Convert StringBuilder to String
                val userDetails = userDetailsBuilder.toString()

                // Using StringBuilder to construct the device information
                val deviceInfoBuilder = StringBuilder()
                deviceInfoBuilder.append("Device Name: ").append(Build.DEVICE).append("\n")
                deviceInfoBuilder.append("Model: ").append(Build.MODEL).append("\n")
                deviceInfoBuilder.append("Manufacturer: ").append(Build.MANUFACTURER).append("\n")
                deviceInfoBuilder.append("Brand: ").append(Build.BRAND).append("\n")
                deviceInfoBuilder.append("OS Version: ").append(Build.VERSION.RELEASE).append("\n")
                deviceInfoBuilder.append("SDK Version: ").append(Build.VERSION.SDK_INT).append("\n")
                deviceInfoBuilder.append("Build Number: ").append(Build.DISPLAY).append("\n")

                // Convert StringBuilder to String
                val deviceInformation = deviceInfoBuilder.toString()

                // Combine user details and device information
                val emailContent = "$userDetails\n\nDevice Information\n\n$deviceInformation"

                val email = Constants.COMPANY_EMAIL

                // save the password
                val editor22 = simpleSavedPassword.edit()
                editor22.remove(Constants.Did_User_Input_PassWord)
                editor22.putString(Constants.mySimpleSavedPassword, password)
                editor22.apply()

                // send the email s data
                sendMessage(email, emailContent)
                alertDialog.dismiss()
            } else {
                showToastMessage("No internet Connection")
            }
        }




        imgCloseDialog2.setOnClickListener {
            showExitConfirmationDialog()
            alertDialog.dismiss()
        }
        alertDialog.show()
    }


    @SuppressLint("MissingInflatedId")
    private fun showPopContactAdmin() {
        val bindingCM: CustomContactAdminBinding = CustomContactAdminBinding.inflate(
            layoutInflater
        )
        val builder = AlertDialog.Builder(this)
        builder.setView(bindingCM.getRoot())
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setCancelable(false)
        if (alertDialog.window != null) {
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
        }

        val textOkayBtn: TextView = bindingCM.textOkayBtn
        val imgCloseDialog2: ImageView = bindingCM.imgCloseDialogForegetPassword



        textOkayBtn.setOnClickListener {
            showExitConfirmationDialog()
            alertDialog.dismiss()

        }



        imgCloseDialog2.setOnClickListener {

            showExitConfirmationDialog()
            alertDialog.dismiss()
        }
        alertDialog.show()
    }


    @SuppressLint("MissingSuperCall")
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        val getInfoPageState =
            sharedBiometric.getString(Constants.FIRST_INFORMATION_PAGE_COMPLETED, "").toString()
        if (getInfoPageState == Constants.FIRST_INFORMATION_PAGE_COMPLETED) {
            val intent = Intent(applicationContext, WebViewPage::class.java)
            startActivity(intent)
            finish()
        } else {
            startActivity(Intent(applicationContext, InformationActivity::class.java))
            finish()
        }
    }

    private fun isConnected(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        var networkInfo: NetworkInfo? = null
        if (connectivityManager != null) {
            networkInfo = connectivityManager.activeNetworkInfo
        }
        return networkInfo != null && networkInfo.isConnected
    }


    private fun sendMessage(reciverEmail: String, myMessage: String) {
        showCustomProgressDialog("Sending Email")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val sender = GMailSender(
                    Constants.Sender_email_Address,
                    Constants.Sender_email_Password
                )

                if (reciverEmail == Constants.COMPANY_EMAIL) {
                    sender.sendMail(

                        Constants.Subject, "USER DETAILS ARE: \n\n$myMessage",
                        Constants.Sender_name,
                        reciverEmail
                    )
                } else {
                    sender.sendMail(

                        Constants.Subject, "YOUR PASSWORD IS: \n\n$myMessage",
                        Constants.Sender_name,
                        reciverEmail
                    )
                }


                Log.d("mylog", "Email Sent Successfully")

                // Update UI on the Main thread
                withContext(Dispatchers.Main) {

                    if (Constants.COMPANY_EMAIL == reciverEmail) {
                        showPopContactAdmin()
                    } else {
                        show_Pop_Up_Email_Sent_Sucessful(
                            "Email sent",
                            "Kindly check email to view password"
                        )
                    }

                    customProgressDialog?.dismiss()
                }
            } catch (e: Exception) {
                Log.e("mylog", "Error: ${e.message}")

                // Update UI on the Main thread
                withContext(Dispatchers.Main) {

                    if (Constants.COMPANY_EMAIL == reciverEmail) {
                        showPopContactAdmin()
                    } else {
                        show_Pop_Up_Email_Sent_Sucessful("Failed!", "Unable to send email")
                    }

                    customProgressDialog?.dismiss()
                }
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Start the WebviewActivity
                val intent = Intent(this, WebViewPage::class.java)
                startActivity(intent)
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun isValidEmail(email: String?): Boolean {
        val emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(\\.[a-zA-Z]{2,})?"
        val pattern = Pattern.compile(emailPattern)
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }


    private fun second_cancel_download() {
        try {
            val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
            val download_ref = myDownloadClass.getLong(Constants.downloadKey, -15)
            if (download_ref != -15L) {
                val query = DownloadManager.Query()
                query.setFilterById(download_ref)
                val c = (getSystemService(DOWNLOAD_SERVICE) as DownloadManager).query(query)
                if (c.moveToFirst()) {
                    val manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                    manager.remove(download_ref)
                    val editor = myDownloadClass.edit()
                    editor.remove(Constants.downloadKey)
                    editor.apply()
                }
            }
        } catch (ignored: java.lang.Exception) {
        }
    }


    @SuppressLint("MissingInflatedId")
    private fun show_Pop_Up_Email_Sent_Sucessful(title: String, body: String) {
        // Inflate the custom layout
        val binding: CustomEmailSucessLayoutBinding =
            CustomEmailSucessLayoutBinding.inflate(layoutInflater)

        // Create AlertDialog Builder
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(binding.getRoot())
        alertDialogBuilder.setCancelable(false)

        // Create the AlertDialog
        val alertDialog = alertDialogBuilder.create()

        // Set background drawable to be transparent
        if (alertDialog.window != null) {
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
        }

        binding.textEmailSendOkayBtn.setOnClickListener { view ->
            showExitConfirmationDialog()
            alertDialog.dismiss()
        }

        binding.textSucessful.text = title
        binding.textBodyMessage.text = body

        // Show the AlertDialog
        alertDialog.show()
    }


    private fun showToastMessage(messages: String) {
        try {
            Toast.makeText(applicationContext, messages, Toast.LENGTH_SHORT).show()
        } catch (ignored: java.lang.Exception) {
        }
    }

    private fun hideKeyBoard(editText: EditText) {
        try {
            editText.clearFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editText.windowToken, 0)
        } catch (ignored: java.lang.Exception) {
        }
    }


    @SuppressLint("SourceLockedOrientationActivity")
    private fun applyOritenation() {
        val getState =
            sharedBiometric.getString(Constants.IMG_TOGGLE_FOR_ORIENTATION, "").toString()

        if (getState == Constants.USE_POTRAIT) {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        } else if (getState == Constants.USE_LANDSCAPE) {

            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        } else if (getState == Constants.USE_UNSEPECIFIED) {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        }

    }


}