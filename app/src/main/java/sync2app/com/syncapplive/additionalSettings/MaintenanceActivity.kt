package sync2app.com.syncapplive.additionalSettings

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import sync2app.com.syncapplive.R
import sync2app.com.syncapplive.SettingsActivityKT
import sync2app.com.syncapplive.additionalSettings.autostartAppOncrash.Methods
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.databinding.ActivityMaintenanceBinding
import sync2app.com.syncapplive.databinding.CustomCrashReportBinding
import sync2app.com.syncapplive.databinding.CustomDefineRefreshTimeBinding
import java.io.File
import sync2app.com.syncapplive.WebViewPage
import sync2app.com.syncapplive.additionalSettings.utils.Utility
import sync2app.com.syncapplive.databinding.CustomSetOritentaionBinding


class MaintenanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMaintenanceBinding



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



    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }


    private val sharedTVAPPModePreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_TV_APP_MODE, Context.MODE_PRIVATE
        )
    }

    private var getTimeDefined_Prime = ""
    private var Refresh_Time = "Refresh Time: "
    private var T_3_HR = 3L
    private var T_4_HR = 4L
    private var T_5_HR = 5L
    private var T_6_HR = 6L
    private var T_7_HR = 7L
    private var T_8_HR = 8L
    private var T_9_HR = 9L
    private var T_10_HR = 10L
    private var T_11_HR = 11L
    private var T_12_HR = 12L


    private val FILE_MANAGER_REQUEST_CODE = 1001
    
    @SuppressLint("SetTextI18n", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaintenanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpFullScreenWindows()


        val editor = sharedBiometric.edit()

        Handler(Looper.getMainLooper()).postDelayed({
            getCarshReports()
        }, 700)


        val get_imgToggleImageBackground = sharedBiometric.getString(Constants.imgToggleImageBackground, "")
        val get_imageUseBranding = sharedBiometric.getString(Constants.imageUseBranding, "")
        if (get_imgToggleImageBackground.equals(Constants.imgToggleImageBackground) && get_imageUseBranding.equals(Constants.imageUseBranding) ){
            loadBackGroundImage()
        }





        setTextForOrientation()

        applyOritenation()


        binding.textNavigateToSeverEngine.setOnClickListener {
            val intent = Intent(applicationContext, DE_MO_202100::class.java)
            startActivity(intent)
            finish()
        }




        binding.apply {

            //set orientation
            textSetOrientationMode.setOnClickListener {
                showSelectedSyncType()
            }





            //  imgSetUserAgent

            // Set an OnClickListener to toggle orientation mode
            imgSetUserAgent.setOnCheckedChangeListener { compoundButton, isValued -> // we are putting the values into SHARED PREFERENCE
                if (compoundButton.isChecked) {
                    binding.textSetUserAgents.text = "Use Desktop Mode"

                    // Save the landscape mode setting
                    editor.putString(Constants.ENABLE_USER_AGENT, Constants.ENABLE_USER_AGENT)
                    editor.apply()


                } else {

                    binding.textSetUserAgents.text = "Use MobileMode"

                    // Remove the landscape mode setting
                    editor.remove(Constants.ENABLE_USER_AGENT)
                    editor.apply()

                }
            }

            val get_imgSetUserAgent = sharedBiometric.getString(Constants.ENABLE_USER_AGENT, "").toString()

            imgSetUserAgent.isChecked = get_imgSetUserAgent.equals(Constants.ENABLE_USER_AGENT)

            if (get_imgSetUserAgent.equals(Constants.ENABLE_USER_AGENT)) {

                binding.textSetUserAgents.text = "Use Desktop Mode"

            } else {

                binding.textSetUserAgents.text = "Use MobileMode"

            }

        }


        binding.textOpenSystemFiles.setOnClickListener {
            openFileManager()
        }


        binding.textOpenChromeBrowser.setOnClickListener {
           openFileInChrome()
        }


        binding.textBranding.setOnClickListener {
            startActivity(Intent(applicationContext, BrandingActivity::class.java))
            finish()
        }


        //add exception
        Methods.addExceptionHandler(this)


        // show online Status

        binding.apply {


            imagShowOnlineStatus.setOnCheckedChangeListener { compoundButton, isValued -> // we are putting the values into SHARED PREFERENCE
                if (compoundButton.isChecked) {
                    editor.remove(Constants.imagShowOnlineStatus)
                    editor.apply()
                    binding.textShowOnlineStatus.text = "Show Online Indicator"

                } else {
                    editor.putString(Constants.imagShowOnlineStatus, "imagShowOnlineStatus")
                    editor.apply()
                    binding.textShowOnlineStatus.text = "Hide Online Indicator"
                }
            }




            val get_indicator_satate = sharedBiometric.getString(Constants.img_Make_OnlineIndicator_Default_visible, "").toString()

            if (get_indicator_satate.isNullOrEmpty()){
                editor.putString(Constants.img_Make_OnlineIndicator_Default_visible, Constants.img_Make_OnlineIndicator_Default_visible)
                editor.remove(Constants.imagShowOnlineStatus)
                editor.apply()
                binding.textShowOnlineStatus.text = "Show Online Indicator"
                binding.imagShowOnlineStatus.isChecked = true

            }else{

                val get_imagShowOnlineStatus = sharedBiometric.getString(Constants.imagShowOnlineStatus, "").toString()

                imagShowOnlineStatus.isChecked = get_imagShowOnlineStatus.equals(Constants.imagShowOnlineStatus)

                if (get_imagShowOnlineStatus.equals(Constants.imagShowOnlineStatus)) {

                    binding.textShowOnlineStatus.text = "Hide Online Indicator"
                    binding.imagShowOnlineStatus.isChecked = false

                } else {
                    binding.textShowOnlineStatus.text = "Show Online Indicator"
                    binding.imagShowOnlineStatus.isChecked = true


                }



            }


        }




        binding.apply {

            /// enable the Auto Boot
            imagEnableDownloadStatus.setOnCheckedChangeListener { compoundButton, isValued -> // we are putting the values into SHARED PREFERENCE
                if (compoundButton.isChecked) {

                    editor.putString(Constants.showDownloadSyncStatus, Constants.showDownloadSyncStatus)
                    editor.apply()
                    binding.textCheckDownloadStatus2.text = "Show Download Status"

                } else {
                    editor.remove(Constants.showDownloadSyncStatus)
                    editor.apply()
                    binding.textCheckDownloadStatus2.text = "Hide Download Status"
                }

            }


            val get_imagEnableDownloadStatus = sharedBiometric.getString(Constants.showDownloadSyncStatus, "").toString()


            imagEnableDownloadStatus.isChecked = get_imagEnableDownloadStatus.equals(Constants.showDownloadSyncStatus)

            if (get_imagEnableDownloadStatus.equals(Constants.showDownloadSyncStatus)) {

                binding.textCheckDownloadStatus2.text = "Show Download Status"

            } else {

                binding.textCheckDownloadStatus2.text = "Hide Download Status"

            }


            binding.closeBs.setOnClickListener {
                val get_navigationS2222 = sharedBiometric.getString(Constants.SAVE_NAVIGATION, "").toString()

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
                    val intent =
                        Intent(applicationContext, WebViewPage::class.java)
                    startActivity(intent)
                    finish()
                }

            }



            textHardwarePage.setOnClickListener {
                val intent = Intent(applicationContext, SystemInfoActivity::class.java)
                startActivity(intent)


            }




            textFileManger.setOnClickListener {
                startActivity(Intent(applicationContext, FileExplorerActivity::class.java))
            }



            textCrashPage.setOnClickListener {

                val sharedCrashReport = getSharedPreferences(Constants.SHARED_SAVED_CRASH_REPORT, MODE_PRIVATE)
                val crashInfo = sharedCrashReport.getString(Constants.crashInfo, "")
                if (!crashInfo.isNullOrEmpty()) {
                    showPopCrashReport(crashInfo.toString())
                } else {
                    showToastMessage("No crash report yet")
                }


            }

        }

        binding.apply {


            // Restart app on Tv or Mobile mode
            imgStartAppRestartOnTvMode.setOnCheckedChangeListener { compoundButton, isValued -> // we are putting the values into SHARED PREFERENCE
                if (compoundButton.isChecked) {

                    editor.putString(Constants.imgStartAppRestartOnTvMode, Constants.imgStartAppRestartOnTvMode)
                    editor.apply()
                    binding.textShowAppRestartTvMode.text = "Restart On Crash Enabled"

                } else {
                    editor.remove(Constants.imgStartAppRestartOnTvMode)
                    editor.apply()
                    binding.textShowAppRestartTvMode.text = "Restart On Crash Disabled"
                }
            }


            val get_imgStartAppRestartOnTvMode = sharedBiometric.getString(Constants.imgStartAppRestartOnTvMode, "")


            imgStartAppRestartOnTvMode.isChecked = get_imgStartAppRestartOnTvMode.equals(Constants.imgStartAppRestartOnTvMode)

            if (get_imgStartAppRestartOnTvMode.equals(Constants.imgStartAppRestartOnTvMode)) {

                binding.textShowAppRestartTvMode.text = "Restart On Crash Enabled"

            } else {

                binding.textShowAppRestartTvMode.text = "Restart On Crash Disabled"
            }





            textHardwarePage.setOnClickListener {
                val intent = Intent(applicationContext, SystemInfoActivity::class.java)
                startActivity(intent)


            }




            textFileManger.setOnClickListener {
                startActivity(Intent(applicationContext, FileExplorerActivity::class.java))
            }



            textCrashPage.setOnClickListener {

                val sharedCrashReport =
                    getSharedPreferences(Constants.SHARED_SAVED_CRASH_REPORT, MODE_PRIVATE)
                val crashInfo = sharedCrashReport.getString(Constants.crashInfo, "")
                if (!crashInfo.isNullOrEmpty()) {
                    showPopCrashReport(crashInfo.toString())
                } else {
                    showToastMessage("No crash report yet")
                }


            }

        }




        binding.textRefreshTimer.setOnClickListener {
            definedTimeIntervalsForRefresh()
        }


        // Init Refresh Time
        val d_time = myDownloadClass.getLong(Constants.get_Refresh_Timer, 0L)
        d_time.let {itLong->
            if (itLong != 0L) {
                binding.textRefreshTimer.text ="Refresh Time : $itLong Hours"
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



    private fun openFileManager() {
        try {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*" // Filter by file type, e.g., "image/*", "application/pdf"
            intent.addCategory(Intent.CATEGORY_OPENABLE) // Ensure only openable files are shown

            // Start the activity and handle the result in onActivityResult or ActivityResult API
            startActivityForResult(Intent.createChooser(intent, "Choose a file"), FILE_MANAGER_REQUEST_CODE)
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_MANAGER_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                // Use the Uri to access the selected file
                Toast.makeText(this, "Selected file: $uri", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun openFileInChrome() {
        try {
            val url ="https://www.google.com/"
            val fileUri: Uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(fileUri, "text/html")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            // Check if Chrome is installed and use it explicitly
            intent.setPackage("com.android.chrome")

            startActivity(intent)

        } catch (e: Exception) {
            // Fallback: Try opening with any browser
            intent.setPackage(null)
            startActivity(Intent.createChooser(intent, "Open file with"))
            Toast.makeText(applicationContext, "Error${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    @SuppressLint("InflateParams", "SuspiciousIndentation", "SetTextI18n")
    private fun definedTimeIntervalsForRefresh() {
        val bindingCm: CustomDefineRefreshTimeBinding = CustomDefineRefreshTimeBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
        builder.setView(bindingCm.getRoot())
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.setCancelable(true)

        // Set the background of the AlertDialog to be transparent
        if (alertDialog.window != null) {
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
        }







        bindingCm.apply {

            imageCrossClose.setOnClickListener {
                alertDialog.dismiss()
            }



            closeBs.setOnClickListener {
                alertDialog.dismiss()
            }



            textTwoMinutes.setOnClickListener {

                binding.textRefreshTimer.text = "$Refresh_Time$T_3_HR Hours"
                val editor = myDownloadClass.edit()
                editor.putLong(Constants.get_Refresh_Timer, Constants.T_3_HR)
                editor.apply()
                alertDialog.dismiss()

            }


            text55minutes.setOnClickListener {

                binding.textRefreshTimer.text = "$Refresh_Time$T_4_HR Hours"
                val editor = myDownloadClass.edit()
                editor.putLong(Constants.get_Refresh_Timer, Constants.T_4_HR)
                editor.apply()
                alertDialog.dismiss()

            }

            text100minutes2.setOnClickListener {

                binding.textRefreshTimer.text = "$Refresh_Time$T_5_HR Hours"
                val editor = myDownloadClass.edit()
                editor.putLong(Constants.get_Refresh_Timer, Constants.T_5_HR)
                editor.apply()
                alertDialog.dismiss()

            }


            text1500minutes.setOnClickListener {

                binding.textRefreshTimer.text = "$Refresh_Time$T_6_HR Hours"
                val editor = myDownloadClass.edit()
                editor.putLong(Constants.get_Refresh_Timer, Constants.T_6_HR)
                editor.apply()
                alertDialog.dismiss()

            }



            text3000minutes2.setOnClickListener {

                binding.textRefreshTimer.text = "$Refresh_Time$T_7_HR Hours"
                val editor = myDownloadClass.edit()
                editor.putLong(Constants.get_Refresh_Timer, Constants.T_7_HR)
                editor.apply()

                alertDialog.dismiss()
            }



            text6000minutes.setOnClickListener {

                    binding.textRefreshTimer.text = "$Refresh_Time$T_8_HR Hours"
                    val editor = myDownloadClass.edit()
                    editor.putLong(Constants.get_Refresh_Timer, Constants.T_8_HR)
                    editor.apply()

                    alertDialog.dismiss()
            }


            textOneTwentyMinutes.setOnClickListener {

                binding.textRefreshTimer.text = "$Refresh_Time$T_9_HR Hours"
                val editor = myDownloadClass.edit()
                editor.putLong(Constants.get_Refresh_Timer, Constants.T_9_HR)
                editor.apply()


                alertDialog.dismiss()

            }



            text10Hours.setOnClickListener {

                binding.textRefreshTimer.text = "$Refresh_Time$T_10_HR Hours"
                val editor = myDownloadClass.edit()
                editor.putLong(Constants.get_Refresh_Timer, Constants.T_10_HR)
                editor.apply()

                alertDialog.dismiss()


            }


            text11Hours.setOnClickListener {
                binding.textRefreshTimer.text = "$Refresh_Time$T_11_HR Hours"
                val editor = myDownloadClass.edit()
                editor.putLong(Constants.get_Refresh_Timer, Constants.T_11_HR)
                editor.apply()
                alertDialog.dismiss()

            }

            text12Hours.setOnClickListener {

                binding.textRefreshTimer.text = "$Refresh_Time$T_12_HR Hours"
                val editor = myDownloadClass.edit()
                editor.putLong(Constants.get_Refresh_Timer, Constants.T_12_HR)
                editor.apply()

                alertDialog.dismiss()

            }


        }


        alertDialog.show()
    }







    private fun getCarshReports() {
        val sharedCrashReport =
            getSharedPreferences(Constants.SHARED_SAVED_CRASH_REPORT, MODE_PRIVATE)
        val crashInfo = sharedCrashReport.getString(Constants.crashInfo, "")
        val crashCalled = sharedCrashReport.getString(Constants.crashCalled, "")
        if (!crashCalled.isNullOrEmpty()) {
            showPopCrashReport(crashInfo + "")
        }
    }


    @SuppressLint("MissingInflatedId")
    private fun showPopCrashReport(message: String) {
        val bindingCm: CustomCrashReportBinding =
            CustomCrashReportBinding.inflate(LayoutInflater.from(this))
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(bindingCm.getRoot())

        val alertDialog = alertDialogBuilder.create()
        // Set the background of the AlertDialog to be transparent
        if (alertDialog.window != null) {
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
        }

        alertDialog.setCanceledOnTouchOutside(true)



        bindingCm.textDisplayResults.setText(message)
        bindingCm.textContinuPassword2.setOnClickListener { view ->
            val sharedCrashReport = getSharedPreferences(
                Constants.SHARED_SAVED_CRASH_REPORT,
                MODE_PRIVATE
            )
            val editor = sharedCrashReport.edit()
            editor.remove(Constants.crashCalled)
            editor.apply()
            sendCrashReport(message)
            alertDialog.dismiss()
        }

        // Show the AlertDialog
        alertDialog.show()
    }


    private fun showToastMessage(messages: String) {

        try {
            Toast.makeText(applicationContext, messages, Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
        }
    }

    fun sendCrashReport(crashMessage: String?) {
        //  val email = "kola@moreadvice.co.uk"
        val email = "support@syn2app.com"
        val subject = "Crash Report"

        val uriText = "mailto:" + Uri.encode(email) +
                "?subject=" + Uri.encode(subject) +
                "&body=" + Uri.encode(crashMessage)

        val uri = Uri.parse(uriText)

        // Intent for sending text
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, crashMessage)
        sendIntent.type = "text/plain"

        // Intent for sending email
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = uri

        // Create a chooser with both intents
        val chooserIntent = Intent.createChooser(sendIntent, "Send message via:")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(emailIntent))

        try {
            startActivity(chooserIntent)
        } catch (e: ActivityNotFoundException) {
            // Handle case where no email app is available
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }


    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val get_navigationS2222 = sharedBiometric.getString(Constants.SAVE_NAVIGATION, "")

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
            val intent =
                Intent(applicationContext, WebViewPage::class.java)
            startActivity(intent)
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


    /////  The ..

    private fun showSelectedSyncType() {
        val bindingCm: CustomSetOritentaionBinding = CustomSetOritentaionBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
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




        bindingCm.apply {

            // nit the right rado btn

            val getSyncMethods = sharedBiometric.getString(Constants.IMG_TOGGLE_FOR_ORIENTATION, "").toString()
            if (getSyncMethods == Constants.USE_POTRAIT) {
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


            } else if (getSyncMethods == Constants.USE_LANDSCAPE) {

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


            } else if (getSyncMethods == Constants.USE_UNSEPECIFIED) {
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
                setTextForOrientation()

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
                setTextForOrientation()

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
                setTextForOrientation()

                alertDialog.dismiss()

            }


        }

        alertDialog.show()

    }


    private fun initAPISyncMode() {
        binding.apply {

            val editor = sharedBiometric.edit()
            editor.putString(Constants.IMG_TOGGLE_FOR_ORIENTATION, Constants.USE_LANDSCAPE)
            editor.apply()

        }
    }



    private fun initZipSyncMethod() {
        binding.apply {
            val editor = sharedBiometric.edit()
            editor.putString(Constants.IMG_TOGGLE_FOR_ORIENTATION, Constants.USE_POTRAIT)
            editor.apply()

        }
    }


    private fun initParsingSyncMethod() {
        binding.apply {
            val editor = sharedBiometric.edit()
            editor.putString(Constants.IMG_TOGGLE_FOR_ORIENTATION, Constants.USE_UNSEPECIFIED)
            editor.apply()
        }
    }


    @SuppressLint("SetTextI18n", "SuspiciousIndentation", "SourceLockedOrientationActivity")
    private fun setTextForOrientation() {
        binding.apply {
            val getSyncMethods = sharedBiometric.getString(Constants.IMG_TOGGLE_FOR_ORIENTATION, "").toString()

                if (getSyncMethods == Constants.USE_POTRAIT) {

                    textSetOrientationMode.text = "Portrait Mode Enabled"
                    requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                } else if (getSyncMethods == Constants.USE_LANDSCAPE) {

                    textSetOrientationMode.text = "Landscape Mode Enabled"
                    requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                } else if (getSyncMethods == Constants.USE_UNSEPECIFIED) {
                    textSetOrientationMode.text = "Unspecified Mode"
                    requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
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