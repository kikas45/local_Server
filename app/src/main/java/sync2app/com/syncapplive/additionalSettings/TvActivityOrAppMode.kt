package sync2app.com.syncapplive.additionalSettings

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import io.paperdb.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sync2app.com.syncapplive.R
import sync2app.com.syncapplive.SplashKT
import sync2app.com.syncapplive.additionalSettings.ApiUrls.ApiUrlViewModel
import sync2app.com.syncapplive.additionalSettings.ApiUrls.DomainUrl
import sync2app.com.syncapplive.additionalSettings.ApiUrls.SavedApiAdapter
import sync2app.com.syncapplive.additionalSettings.autostartAppOncrash.Methods
import sync2app.com.syncapplive.additionalSettings.cloudAppsync.util.Common
import sync2app.com.syncapplive.additionalSettings.cloudAppsync.util.MethodsSchedule
import sync2app.com.syncapplive.additionalSettings.savedDownloadHistory.User
import sync2app.com.syncapplive.additionalSettings.savedDownloadHistory.UserViewModel
import sync2app.com.syncapplive.additionalSettings.urlchecks.checkStoragePermission
import sync2app.com.syncapplive.additionalSettings.urlchecks.checkUrlExistence
import sync2app.com.syncapplive.additionalSettings.urlchecks.requestStoragePermission
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.additionalSettings.utils.Utility
import sync2app.com.syncapplive.databinding.ActivityTvOrAppModePageBinding
import sync2app.com.syncapplive.databinding.CustomApiHardCodedLayoutBinding
import sync2app.com.syncapplive.databinding.CustomApiUrlLayoutBinding
import sync2app.com.syncapplive.databinding.CustomContinueDownloadLayoutBinding
import sync2app.com.syncapplive.databinding.CustomGrantAccessPageBinding
import sync2app.com.syncapplive.databinding.CustomPopDisplayOverAppsBinding
import sync2app.com.syncapplive.databinding.CustomeAllowAppWriteSystemBinding
import sync2app.com.syncapplive.databinding.ProgressValidateUserDialogLayoutBinding
import java.io.File
import java.util.Objects


class TvActivityOrAppMode : AppCompatActivity(), SavedApiAdapter.OnItemClickListener {
    private lateinit var binding: ActivityTvOrAppModePageBinding

    private val prefs by lazy { getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }

    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }


    private val adapterApi by lazy {
        SavedApiAdapter(this)
    }


    private var isDialogPermissionShown = false
    private var isInitPermissionOnNetworkCall  = false

    /// for branindg
    private var downloadId: Long = -1
    private val fileNameOne = "app_background.png"
    private val fileNameTwo = "Splash.mp4"
    private val fileNameThree = "app_logo.png"
    private var file1 = false
    private var file2 = false
    private var file3 = false
    private var isCalledToast = false
    private var isBrandindImagesFound = false

    private var isMyActivityRunning = false

    /// for internet
    private var connectivityReceiver: ConnectivityReceiver? = null


    private val multiplePermissionId = 14
    private val multiplePermissionNameList = if (Build.VERSION.SDK_INT >= 33) {
        arrayListOf(
            android.Manifest.permission.READ_MEDIA_AUDIO,
            android.Manifest.permission.READ_MEDIA_VIDEO,
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.POST_NOTIFICATIONS,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
            android.Manifest.permission.RECORD_AUDIO
        )
    } else {
        arrayListOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
            android.Manifest.permission.RECORD_AUDIO
        )
    }


    private var btnisClicked = false
    private var navigateTVMode = false;
    private var navigateAppMolde = false;
    private val TAG = "TvActivityOrAppMode"
    private var getUrlBasedOnSpinnerText = ""

    private var API_Server = "CP 2-Cloud App Server"
    private var CP_server = "CP 1-Cloud App Server"

    private val mApiViewModel by viewModels<ApiUrlViewModel>()

    private val mUserViewModel by viewModels<UserViewModel>()

    private lateinit var custom_ApI_Dialog: Dialog
    private lateinit var customProgressDialog: Dialog

    private val simpleSavedPassword: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SIMPLE_SAVED_PASSWORD,
            Context.MODE_PRIVATE
        )
    }


    private val myDownloadClass: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.MY_DOWNLOADER_CLASS,
            Context.MODE_PRIVATE
        )
    }


    private val sharedBiometric: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_BIOMETRIC,
            Context.MODE_PRIVATE
        )
    }


    private val sharedTVAPPModePreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_TV_APP_MODE, Context.MODE_PRIVATE
        )
    }

   // private var preferences: SharedPreferences? = null

    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    @SuppressLint("CommitPrefEdits", "SetTextI18n", "SourceLockedOrientationActivity",
        "UnspecifiedRegisterReceiverFlag"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTvOrAppModePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Register the broadcast receiver dynamically
        registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))


        handler.postDelayed(kotlinx.coroutines.Runnable {
            autoSetPrefilledPaths()
        },1500)



        setUpInternetAmination()

       // setUpdarkUITheme()  ///   for dark theme, enable it latter if needed

        applyOritenation()

        //add exception
        Methods.addExceptionHandler(this)


        // initialize schedule settings
        MethodsSchedule.setPersistentDefaults()


        val get_imgToggleImageBackground = sharedBiometric.getString(Constants.imgToggleImageBackground, "").toString()
        val get_imageUseBranding = sharedBiometric.getString(Constants.imageUseBranding, "").toString()
        if (get_imgToggleImageBackground.equals(Constants.imgToggleImageBackground) && get_imageUseBranding.equals(
                Constants.imageUseBranding
            )
        ) {
            loadBackGroundImage()
        }

        if (get_imageUseBranding == Constants.imageUseBranding) {
            loadImage()
        }


        val CheckForPassword =
            simpleSavedPassword.getString(Constants.onCreatePasswordSaved, "").toString()

        if (CheckForPassword.isNullOrEmpty()) {
            val editor = simpleSavedPassword.edit()
            editor.putString(Constants.onCreatePasswordSaved, "onCreatePasswordSaved")
            editor.putString(
                Constants.mySimpleSavedPassword,
                "1234_1234_1234_E-***#^8678488_587377_73784#GGGkkk***2345_KING"
            )
            editor.apply()

        }


        val sharedBiometric: SharedPreferences = applicationContext.getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)
        val get_useOfflineOrOnline = sharedBiometric.getString(Constants.imgAllowLunchFromOnline, "").toString()
        val get_TV_or_App_Mode = sharedBiometric.getString(Constants.MY_TV_OR_APP_MODE, "").toString()


        val editor = sharedBiometric.edit()
        if (get_useOfflineOrOnline.equals(Constants.imgAllowLunchFromOnline) || get_TV_or_App_Mode.equals(Constants.TV_Mode)
        ) {
            editor.putString(Constants.FIRST_TIME_APP_START, Constants.FIRST_TIME_APP_START)
            editor.apply()

            startActivity(Intent(applicationContext, SplashKT::class.java))
            finish()

        }


        val editoreedde = simpleSavedPassword.edit()
        getUrlBasedOnSpinnerText = ""
        editoreedde.remove(Constants.Saved_Domains_Name)
        editoreedde.remove(Constants.Saved_Domains_Urls)
        editoreedde.apply()


        handler.postDelayed(Runnable {
            val editoreee = simpleSavedPassword.edit()
            binding.texturlsSavedDownload.text = "Select Partner Url"

            binding.texturlsSavedDownload.setTextColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.white_wash_dim_bit
                )
            )

            binding.textPartnerUrlLunch.text = "Select Partner Url"

            editoreee.remove(Constants.Saved_Domains_Name)
            editoreee.remove(Constants.Saved_Domains_Urls)

            editoreee.putString(Constants.imagSwtichPartnerUrl, Constants.imagSwtichPartnerUrl)
            editoreee.apply()

            binding.imgUserMasterDomainORCustom.isChecked = true

        }, 300)



        binding.imgUserMasterDomainORCustom.setOnCheckedChangeListener { compoundButton, isValued ->

            Utility.hideKeyBoard(applicationContext, binding.editTextUserID)
            if (compoundButton.isChecked) {
                val editoreee = simpleSavedPassword.edit()
                binding.texturlsSavedDownload.text = "Select Partner Url"

                binding.texturlsSavedDownload.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.white_wash_dim_bit
                    )
                )

                binding.textPartnerUrlLunch.text = "Select Partner Url"

                editoreee.remove(Constants.Saved_Domains_Name)
                editoreee.remove(Constants.Saved_Domains_Urls)

                editoreee.putString(Constants.imagSwtichPartnerUrl, Constants.imagSwtichPartnerUrl)

                editoreee.apply()


            } else {

                binding.textPartnerUrlLunch.text = "Select Custom Domain"
                binding.texturlsSavedDownload.text = "Select Custom Domain"

                binding.texturlsSavedDownload.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.white_wash_dim_bit
                    )
                )
                getUrlBasedOnSpinnerText = ""

                val editoreee = simpleSavedPassword.edit()
                editoreee.putString(Constants.imagSwtichPartnerUrl, Constants.imagSwtichPartnerUrl)
                editoreee.apply()
            }
        }


        binding.constraintLayoutSlectDomain.setOnClickListener {
            if (binding.imgUserMasterDomainORCustom.isChecked) {
                serVerOptionDialog()
            } else {
                show_API_Urls()
            }
        }





        binding.apply {


            textAppMode.setOnClickListener {
                btnisClicked = true

                Utility.hideKeyBoard(applicationContext, binding.editTextUserID)

                // Save Launch State
                val editText88 = sharedBiometric.edit()
                editText88.putString(
                    Constants.get_Launching_State_Of_WebView,
                    Constants.launch_Default_WebView_url
                )
                editText88.putString(
                    Constants.imgStartAppRestartOnTvMode,
                    Constants.imgStartAppRestartOnTvMode
                )
                editText88.remove(Constants.imgEnableAutoBoot)
                editText88.apply()


                navigateAppMolde = true
                navigateTVMode = false


                ///  Shared pref for app Settings
                // used for App settings Shared Prefernce
                val editorPref = preferences.edit()
                editorPref!!.putBoolean(Constants.hidebottombar, false)
                editorPref.putBoolean(Constants.fullscreen, false)
                editorPref.putBoolean(Constants.immersive_mode, false)
                editorPref.putBoolean(Constants.shwoFloatingButton, false)
                editorPref.putBoolean(Constants.swiperefresh, true)
                editorPref.apply()


                // remove Json Data for Tv Setting
                val editorTV = sharedTVAPPModePreferences.edit()
                editorTV.remove(Constants.INSTALL_TV_JSON_USER_CLICKED)
                editorTV.remove(Constants.installTVModeForFirstTime)
                editorTV.apply()


                handleFormVerification()

            }






            textTvMode.setOnClickListener {
                btnisClicked = true



                Utility.hideKeyBoard(applicationContext, binding.editTextUserID)

                // Save Launch State
                val editText88 = sharedBiometric.edit()
                editText88.putString(
                    Constants.get_Launching_State_Of_WebView,
                    Constants.launch_WebView_Offline
                )
                editText88.putString(Constants.imgEnableAutoBoot, Constants.imgEnableAutoBoot)
                editText88.putString(
                    Constants.imgStartAppRestartOnTvMode,
                    Constants.imgStartAppRestartOnTvMode
                )
                editText88.putString(Constants.PROTECT_PASSWORD, Constants.PROTECT_PASSWORD)
                editText88.apply()


                navigateAppMolde = false
                navigateTVMode = true


                // Shared pref for app Settings
                // used for App settings Shared Preference
                val editorPref = preferences.edit()
                editorPref!!.putBoolean(Constants.hidebottombar, true)
                editorPref.putBoolean(Constants.fullscreen, true)
                editorPref.putBoolean(Constants.immersive_mode, true)
                editorPref.putBoolean(Constants.shwoFloatingButton, true)
                editorPref.putBoolean(Constants.swiperefresh, false)
                editorPref.apply()


                // remove Json Data for Tv Setting
                val editorTV = sharedTVAPPModePreferences.edit()
                editorTV.remove(Constants.INSTALL_TV_JSON_USER_CLICKED)
                editorTV.remove(Constants.installTVModeForFirstTime)
                editorTV.apply()


                handleFormVerification()

            }


            textDefaultMode.setOnClickListener {
                btnisClicked = true

                Utility.hideKeyBoard(applicationContext, binding.editTextUserID)

                navigateAppMolde = false
                navigateTVMode = true


                // remove Json Data for Tv Setting
                val editorTV = sharedTVAPPModePreferences.edit()
                editorTV.putString(
                    Constants.INSTALL_TV_JSON_USER_CLICKED,
                    Constants.INSTALL_TV_JSON_USER_CLICKED
                )
                editorTV.remove(Constants.installTVModeForFirstTime)
                editorTV.apply()


                // Save Launch State
                val editText88 = sharedBiometric.edit()
                editText88.putString(
                    Constants.imgStartAppRestartOnTvMode,
                    Constants.imgStartAppRestartOnTvMode
                )
                editText88.apply()

                handleFormVerification()

            }


        }


    }

    private fun autoSetPrefilledPaths() {
        // set path
        binding.editTextUserID.setText("CLO")
        binding.editTextLicenseKey.setText("DE_MO_2021000")

        binding.texturlsSavedDownload.text = CP_server
        getUrlBasedOnSpinnerText = CP_server

        binding.texturlsSavedDownload.setTextColor(
            ContextCompat.getColor(
                applicationContext,
                R.color.deep_blue
            )
        )


        val editor = myDownloadClass.edit()
        editor.putString(Constants.Saved_Parthner_Name, CP_server)
        editor.putString(
            Constants.CP_OR_AP_MASTER_DOMAIN,
            Constants.CUSTOM_CP_SERVER_DOMAIN
        )
        editor.apply()

    }



    private fun setUpInternetAmination() {

        if (Utility.isNetworkAvailable(applicationContext)) {
            binding.texttConnection.visibility = View.GONE
        } else {
            binding.texttConnection.visibility = View.VISIBLE

        }


        binding.texttConnection.setOnClickListener {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
        }

        binding.imageView2.setOnClickListener {
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
        }



        connectivityReceiver = ConnectivityReceiver()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(connectivityReceiver, intentFilter)


        val deepBlue = resources.getColor(R.color.white)
        val deepRed = resources.getColor(R.color.red)

        val colorAnimator =
            ObjectAnimator.ofInt(binding.texttConnection, "textColor", deepBlue, deepRed)
        colorAnimator.setEvaluator(ArgbEvaluator())
        colorAnimator.repeatCount = ValueAnimator.INFINITE
        colorAnimator.repeatMode = ValueAnimator.REVERSE
        colorAnimator.duration = 900
        colorAnimator.start()
    }


    // now handle verification from cloud

    private fun handleFormVerification() {
        val get_UserID = binding.editTextUserID.text.toString().trim()
        val get_LicenseKey = binding.editTextLicenseKey.text.toString().trim()

        Utility.hideKeyBoard(applicationContext, binding.editTextUserID)

        if (Utility.isNetworkAvailable(this)) {


            val get_Saved_Domains_Urls = simpleSavedPassword.getString(Constants.Saved_Domains_Urls, "").toString().trim()

            if (binding.imgUserMasterDomainORCustom.isChecked) {
                // add the toggle check for Sync Page
                val editor = sharedBiometric.edit()
                editor.putString(Constants.imagSwtichPartnerUrl, Constants.imagSwtichPartnerUrl)
                editor.apply()


                if (getUrlBasedOnSpinnerText.isNotEmpty()) {
                    when (getUrlBasedOnSpinnerText) {
                        CP_server -> {

                            /// val customDomainUrl = "https://cp.cloudappserver.co.uk/app_base/public/"
                            val get_editTextMaster = Constants.CUSTOM_CP_SERVER_DOMAIN
                            checkMyConnection(get_UserID, get_LicenseKey, get_editTextMaster)
                        }

                        API_Server -> {

                            val get_editTextMaster = Constants.CUSTOM_API_SERVER_DOMAIN
                            checkMyConnection(get_UserID, get_LicenseKey, get_editTextMaster)

                        }

                    }


                } else {
                    showToastMessage("Select Partner Url")
                }

            } else if (get_Saved_Domains_Urls.isNotEmpty()) {

                // remove the toggle check for Sync Page
                val editor = sharedBiometric.edit()
                editor.remove(Constants.imagSwtichPartnerUrl)
                editor.apply()
                // test my connection
                checkMyConnection(get_UserID, get_LicenseKey, get_Saved_Domains_Urls)


            } else {
                showToastMessage("Select Custom Domain")
            }

        } else {
            showToastMessage("No Internet Connection")
        }
    }


    @SuppressLint("InflateParams", "SuspiciousIndentation")
    private fun serVerOptionDialog() {
        val bindingCm: CustomApiHardCodedLayoutBinding =
            CustomApiHardCodedLayoutBinding.inflate(layoutInflater)
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setView(bindingCm.getRoot())
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.setCancelable(true)


        // Set the background of the AlertDialog to be transparent
        if (alertDialog.window != null) {
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
        }



        val textApiServer = bindingCm.textApiServer
        val textCloudServer = bindingCm.textCloudServer


        bindingCm.apply {

            textApiServer.setOnClickListener {
                binding.texturlsSavedDownload.text = CP_server
                getUrlBasedOnSpinnerText = CP_server

                binding.texturlsSavedDownload.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.deep_blue
                    )
                )


                val editor = myDownloadClass.edit()
                editor.putString(Constants.Saved_Parthner_Name, CP_server)
                editor.putString(
                    Constants.CP_OR_AP_MASTER_DOMAIN,
                    Constants.CUSTOM_CP_SERVER_DOMAIN
                )
                editor.apply()

                alertDialog.dismiss()
            }


            textCloudServer.setOnClickListener {
                binding.texturlsSavedDownload.text = API_Server
                getUrlBasedOnSpinnerText = API_Server

                binding.texturlsSavedDownload.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.deep_blue
                    )
                )

                val editor = myDownloadClass.edit()
                editor.putString(Constants.Saved_Parthner_Name, API_Server)
                editor.putString(Constants.CP_OR_AP_MASTER_DOMAIN, Constants.CUSTOM_API_SERVER_DOMAIN)
                editor.apply()

                alertDialog.dismiss()
            }


            imageCrossClose.setOnClickListener {
                alertDialog.dismiss()
            }
            closeBs.setOnClickListener {
                alertDialog.dismiss()
            }


        }


        alertDialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun show_API_Urls() {
        custom_ApI_Dialog = Dialog(this)
        val bindingCm = CustomApiUrlLayoutBinding.inflate(LayoutInflater.from(this))
        custom_ApI_Dialog.setContentView(bindingCm.root)
        custom_ApI_Dialog.setCancelable(true)
        custom_ApI_Dialog.setCanceledOnTouchOutside(true)

        // Set the background of the AlertDialog to be transparent
        if (custom_ApI_Dialog.window != null) {
            custom_ApI_Dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            custom_ApI_Dialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
        }


        val textErrorText = bindingCm.textErrorText
        val textTryAgin = bindingCm.textTryAgin
        val progressBar2 = bindingCm.progressBar2




        if (Utility.isNetworkAvailable(this@TvActivityOrAppMode)) {
            bindingCm.progressBar2.visibility = View.VISIBLE
            bindingCm.textTryAgin.visibility = View.GONE
            bindingCm.textErrorText.visibility = View.GONE

            mApiViewModel.fetchApiUrls(Constants.BASE_URL_OF_MASTER_DOMAIN)

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

            mApiViewModel.apiUrls.observe(this@TvActivityOrAppMode, Observer { apiUrls ->
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
                if (Utility.isNetworkAvailable(this@TvActivityOrAppMode)) {
                    bindingCm.progressBar2.visibility = View.VISIBLE
                    bindingCm.textTryAgin.visibility = View.GONE
                    bindingCm.textErrorText.visibility = View.GONE

                    mApiViewModel.fetchApiUrls(Constants.BASE_URL_OF_MASTER_DOMAIN)

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
            binding.texturlsSavedDownload.text = name

            binding.texturlsSavedDownload.setTextColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.white
                )
            )

        }

        // Note - later you can use the url as well , the  name is displayed on textview
        if (name.isNotEmpty() && urls.isNotEmpty()) {
            val editor = simpleSavedPassword.edit()
            editor.putString(Constants.Saved_Domains_Name, name)
            editor.putString(Constants.Saved_Domains_Urls, urls)
            editor.apply()


            val editorDowbload = myDownloadClass.edit()
            editorDowbload.putString(Constants.Saved_Domains_Name, name)
            editorDowbload.putString(Constants.Saved_Domains_Urls, urls)
            editorDowbload.apply()


        }


        custom_ApI_Dialog.dismiss()
    }


    private fun checkMyConnection(
        get_UserID: String,
        get_LicenseKey: String,
        get_editTextMaster: String,
    ) {
        if (get_UserID.isNotEmpty() && get_LicenseKey.isNotEmpty() && get_editTextMaster.isNotEmpty()) {

            val baseUrl = "$get_editTextMaster/$get_UserID/$get_LicenseKey/App/Config/appConfig.json"

            if (get_editTextMaster.startsWith("https://") || get_editTextMaster.startsWith("http://")) {
                showCustomProgressDialog()

                val myDownloadClass =
                    getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
                val CP_AP_MASTER_DOMAIN =
                    myDownloadClass.getString(Constants.CP_OR_AP_MASTER_DOMAIN, "").toString()


                var isCallingStart = true


                handler.postDelayed(Runnable {
                    if (isInitPermissionOnNetworkCall){
                        binding.texttConnection.visibility = View.GONE
                    }else{
                        binding.texttConnection.visibility = View.VISIBLE
                    }

                    if (customProgressDialog != null) {
                        customProgressDialog.dismiss()
                    }

                    isCallingStart = false

                }, Constants.timeForConnection)


                lifecycleScope.launch {
                    try {

                        val result = checkUrlExistence(baseUrl)
                        if (result) {
                            if (isCallingStart) {
                                prefs.edit { putBoolean("button_clicked", true) }
                                startPermissionProcess()

                                val editorValue = simpleSavedPassword.edit()
                                editorValue.putString(Constants.get_masterDomain, baseUrl)
                                editorValue.putString(Constants.get_UserID, get_UserID)
                                editorValue.putString(Constants.get_LicenseKey, get_LicenseKey)
                                editorValue.putString(
                                    Constants.get_editTextMaster,
                                    get_editTextMaster
                                )
                                editorValue.apply()


                                /// used for the getting first path which will be prefilled on Sync-page
                                val editorValueBio = myDownloadClass.edit()
                                editorValueBio.putString(
                                    Constants.getSavedCLOImPutFiled,
                                    get_UserID
                                )
                                editorValueBio.putString(
                                    Constants.getSaveSubFolderInPutFiled,
                                    get_LicenseKey
                                )


                                editorValueBio.putString(Constants.getFolderClo, get_UserID)
                                editorValueBio.putString(Constants.getFolderSubpath, get_LicenseKey)

                                editorValueBio.remove(Constants.SynC_Status)

                                editorValueBio.apply()

                                val user = User(
                                    CLO = get_UserID,
                                    DEMO = get_LicenseKey,
                                    EditUrl = "",
                                    EditUrlIndex = ""
                                )
                                mUserViewModel.addUser(user)


                                // use Paper Book to Save Use online CSv or Local CSv
                                Paper.book().write(Common.set_schedule_key, Common.schedule_online)

                                val editText88 = sharedBiometric.edit()
                                editText88.putString(
                                    Constants.imagSwtichEnableSyncFromAPI,
                                    Constants.imagSwtichEnableSyncFromAPI
                                )
                                editText88.putString(
                                    Constants.IMG_SELECTED_SYNC_METHOD,
                                    Constants.USE_ZIP_SYNC
                                )
                                editText88.apply()

                                val editText88Dn = myDownloadClass.edit()
                                editText88Dn.putString(
                                    Constants.IMG_SELECTED_SYNC_METHOD,
                                    Constants.USE_ZIP_SYNC
                                )
                                editText88Dn.apply()

                                // check if Api or Custom server
                                if (binding.imgUserMasterDomainORCustom.isChecked) {
                                    // save the modified url
                                    val editorDownload = myDownloadClass.edit()
                                    editorDownload.putString(
                                        Constants.get_ModifiedUrl,
                                        get_editTextMaster
                                    )
                                    editorDownload.apply()
                                } else {
                                    // save the custom url
                                    val editorDownload = myDownloadClass.edit()
                                    editorDownload.putString(
                                        Constants.get_ModifiedUrl,
                                        CP_AP_MASTER_DOMAIN
                                    )
                                    editorDownload.apply()
                                }

                            } else {
                                showToastMessage("Slow internet connection")
                            }
                        } else {

                            if (isCallingStart) {
                                showPopsForMyConnectionTest(
                                    get_UserID,
                                    get_LicenseKey,
                                    "Invalid User!"
                                )

                            } else {
                                showToastMessage("Slow internet connection")
                            }
                            if (customProgressDialog != null) {
                                customProgressDialog.dismiss()
                            }

                        }
                    } finally {
                        if (customProgressDialog != null) {
                            customProgressDialog.dismiss()
                        }

                    }
                }


            } else {

                showToastMessage("Invalid Master Url Format")

            }


        } else {
            if (get_UserID.isEmpty()) {
                binding.editTextUserID.error = "Input User Id"
            }

            if (get_LicenseKey.isEmpty()) {
                binding.editTextLicenseKey.error = "Input LicenseKey"
            }
        }
    }

    private fun showCustomProgressDialog() {
        try {
            customProgressDialog = Dialog(this)
            val binding = ProgressValidateUserDialogLayoutBinding.inflate(LayoutInflater.from(this))
            customProgressDialog.setContentView(binding.root)
            customProgressDialog.setCancelable(true)
            customProgressDialog.setCanceledOnTouchOutside(false)
            customProgressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            customProgressDialog.show()
        } catch (_: Exception) {
        }
    }

    private fun isReadToMove_All_Permission() {
        binding.apply {

            if (customProgressDialog != null) {
                customProgressDialog.dismiss()
            }


            val editor = sharedBiometric.edit()
            if (navigateAppMolde) {


                editor.putString(Constants.MY_TV_OR_APP_MODE, Constants.App_Mode)
                editor.putString(Constants.FIRST_TIME_APP_START, Constants.FIRST_TIME_APP_START)
                editor.putString(Constants.IMG_TOGGLE_FOR_ORIENTATION, Constants.USE_POTRAIT)
                editor.apply()
                startActivity(Intent(applicationContext, RequiredBioActivity::class.java))
                finish()

                showToastMessage("Please wait")

            }


            if (navigateTVMode) {

                editor.putString(Constants.MY_TV_OR_APP_MODE, Constants.TV_Mode)
                editor.putString(Constants.CALL_RE_SYNC_MANGER, Constants.CALL_RE_SYNC_MANGER)
                editor.putString(Constants.FIRST_TIME_APP_START, Constants.FIRST_TIME_APP_START)
                editor.putString(Constants.IMG_TOGGLE_FOR_ORIENTATION, Constants.USE_UNSEPECIFIED)
                editor.apply()
                startActivity(Intent(applicationContext, RequiredBioActivity::class.java))
                finish()

                showToastMessage("Please wait")

            }

        }
    }


    override fun onResume() {
        super.onResume()
       isMyActivityRunning = true
        if (btnisClicked) {
            if (prefs.getBoolean("button_clicked", false)) {
                startPermissionProcess()
            }
        }


        val first_time_app_start =
            sharedBiometric.getString(Constants.FIRST_TIME_APP_START, "").toString()
        if (first_time_app_start.equals(Constants.FIRST_TIME_APP_START)) {
            startActivity(Intent(applicationContext, RequiredBioActivity::class.java))
            finish()
        }

        val get_savedDominName =
            simpleSavedPassword.getString(Constants.Saved_Domains_Name, "").toString()
        if (!get_savedDominName.isNullOrEmpty()) {
            binding.texturlsSavedDownload.text = get_savedDominName
        }


    }


/// later use the permission required of it

    private fun startPermissionProcess() {

         isInitPermissionOnNetworkCall = true

        if (Build.VERSION.SDK_INT >= 30) {
            when {
                !isIgnoringBatteryOptimizations(this, packageName) -> {
                    requestIgnoreBatteryOptimizations()
                }

                !Settings.System.canWrite(applicationContext) -> {
                    showPopAllowAppToWriteSystem()
                }

                !Settings.canDrawOverlays(this) -> {
                    showPop_For_Allow_Display_Over_Apps()
                }

                !checkStoragePermission(this) -> {
                    showPop_For_Grant_Permsiion()
                }

                else -> {

                    if (!isDialogPermissionShown) {

                        handler.postDelayed(Runnable {

                            checkMultiplePermissions()

                        }, 500)
                    }
                }
            }

        } else {

            when {
                !checkStoragePermission(this) -> {
                    showPop_For_Grant_Permsiion()
                }

                else -> {

                    if (!isDialogPermissionShown) {

                        handler.postDelayed(Runnable {

                            checkMultiplePermissions()

                        }, 500)
                    }
                }
            }
        }

    }


    @SuppressLint("BatteryLife")
    private fun requestIgnoreBatteryOptimizations() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }

    private fun isIgnoringBatteryOptimizations(context: Context, packageName: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.isIgnoringBatteryOptimizations(packageName)
        } else {
            false
        }
    }

    private fun requestWriteSettingsPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }


    private fun checkOverlayBackground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }


    @SuppressLint("MissingInflatedId")
    private fun showPopAllowAppToWriteSystem() {
        val bindingCM: CustomeAllowAppWriteSystemBinding =
            CustomeAllowAppWriteSystemBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
        builder.setView(bindingCM.root)
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.setCancelable(true)

        // Set the background of the AlertDialog to be transparent
        if (alertDialog.window != null) {
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
        }


        val permissionButton: TextView = bindingCM.textContinuPassword2
        val imgCloseDialog: ImageView = bindingCM.imgCloseDialog

        Utility.startPulseAnimationForText(bindingCM.imagSucessful)


        permissionButton.setOnClickListener {
            requestWriteSettingsPermission()
            alertDialog.dismiss()
        }

        imgCloseDialog.setOnClickListener {
            alertDialog.dismiss()
        }



        alertDialog.show()
    }


    @SuppressLint("MissingInflatedId")
    private fun showPop_For_Grant_Permsiion() {
        val bindingCM: CustomGrantAccessPageBinding =
            CustomGrantAccessPageBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
        builder.setView(bindingCM.root)
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.setCancelable(true)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation

        val permissionButton: TextView = bindingCM.textContinuPassword2
        val imgCloseDialog: ImageView = bindingCM.imgCloseDialog

        Utility.startPulseAnimationForText(bindingCM.imagSucessful)


        permissionButton.setOnClickListener {
            requestStoragePermission(this@TvActivityOrAppMode)
            alertDialog.dismiss()
        }

        imgCloseDialog.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }


    @SuppressLint("MissingInflatedId")
    private fun showPop_For_Allow_Display_Over_Apps() {
        val bindingCM: CustomPopDisplayOverAppsBinding =
            CustomPopDisplayOverAppsBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
        builder.setView(bindingCM.root)
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.setCancelable(true)
        // Set the background of the AlertDialog to be transparent
        if (alertDialog.window != null) {
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
        }


        val permissionButton: TextView = bindingCM.textContinuPassword2
        val imgCloseDialog: ImageView = bindingCM.imgCloseDialog

        Utility.startPulseAnimationForText(bindingCM.imagSucessful)

        permissionButton.setOnClickListener {
            checkOverlayBackground()
            alertDialog.dismiss()
        }

        imgCloseDialog.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun showPermissionDeniedDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle("Permission Required")
        builder.setMessage("Please grant the required permissions in the app settings to proceed.")

        isDialogPermissionShown = true

        builder.setPositiveButton("Go to Settings") { dialog: DialogInterface?, which: Int ->
            openAppSettings()
            dialog?.dismiss()
            isDialogPermissionShown = false
        }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface?, which: Int ->
            showToastMessage("Permission Denied!")
            isDialogPermissionShown = false
        }
        builder.show()
    }


    private fun checkMultiplePermissions() {
        val listPermissionNeeded = arrayListOf<String>()
        for (permission in multiplePermissionNameList) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                listPermissionNeeded.add(permission)
            }
        }
        if (listPermissionNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionNeeded.toTypedArray(),
                multiplePermissionId
            )
        } else {
            onAllPermissionsGranted()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == multiplePermissionId) {
            if (grantResults.isNotEmpty()) {
                var isGranted = true
                for (result in grantResults) {
                    if (result == PackageManager.PERMISSION_DENIED) {
                        isGranted = false
                        break
                    }
                }
                if (isGranted) {
                    onAllPermissionsGranted()
                } else {
                    if (!isDialogPermissionShown) {
                        isDialogPermissionShown = true
                        showPermissionDeniedDialog()
                    }
                }
            }
        }
    }


    private fun onAllPermissionsGranted() {
        // Toast.makeText(this, "All permissions finally granted!", Toast.LENGTH_SHORT).show()
        btnisClicked = false
        showCustomProgressDialog()
        handler.postDelayed(Runnable {

            if (isBrandindImagesFound) {
                isReadToMove_All_Permission()
            } else {
                cleanUpFolder()
            }
        }, 1000)
    }


    private fun cleanUpFolder() {

        val all_folder_delete =
            sharedBiometric.getString(Constants.ALL_FOLDER_DELETE, "").toString()
        if (all_folder_delete != Constants.ALL_FOLDER_DELETE) {

            lifecycleScope.launch(Dispatchers.IO) {
                var isCleaned = false
                val directoryPath =
                    Environment.getExternalStorageDirectory().absolutePath + "/Download/${Constants.Syn2AppLive}/"
                val file = File(directoryPath)
                delete(file)
                val editor = sharedBiometric.edit()
                editor.putString(Constants.ALL_FOLDER_DELETE, Constants.ALL_FOLDER_DELETE)
                editor.apply()

                withContext(Dispatchers.Main) {
                    if (!isCleaned) {

                        handler.postDelayed(Runnable {
                            isCleaned = true
                            loadBackGroundImageIfExist()
                        }, 1000)
                    }
                }
            }

        } else {
            loadBackGroundImageIfExist()
        }

    }


    private fun showToastMessage(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", this.packageName, null)
        intent.data = uri
        startActivity(intent)
    }


    private fun loadImage() {

        val getFolderClo = myDownloadClass.getString(Constants.getFolderClo, "").toString()
        val getFolderSubpath = myDownloadClass.getString(Constants.getFolderSubpath, "").toString()
        val pathFolder = "/" + getFolderClo + "/" + getFolderSubpath + "/" + Constants.App + "/" + "Config"
        val folder = Environment.getExternalStorageDirectory().absolutePath + "/Download/" + Constants.Syn2AppLive + "/" + pathFolder
        val fileTypes = "app_logo.png"
        val file = File(folder, fileTypes)
        if (file.exists()) {
            Glide.with(this).load(file).centerCrop().into(binding.imageView2)
        }


    }


    private fun loadBackGroundImage() {

        val fileTypes = "app_background.png"
        val getFolderClo = myDownloadClass.getString(Constants.getFolderClo, "").toString()
        val getFolderSubpath = myDownloadClass.getString(Constants.getFolderSubpath, "").toString()

        val pathFolder =
            "/" + getFolderClo + "/" + getFolderSubpath + "/" + Constants.App + "/" + "Config"
        val folder =
            Environment.getExternalStorageDirectory().absolutePath + "/Download/${Constants.Syn2AppLive}/" + pathFolder
        val file = File(folder, fileTypes)

        if (file.exists()) {
            Glide.with(this).load(file).centerCrop().into(binding.backgroundImage)

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


    private fun loadBackGroundImageIfExist() {

        file1 = false
        file2 = false
        file3 = false
        isCalledToast = false

        val fileTypes = "app_background.png"
        val get_UserID = binding.editTextUserID.text.toString().trim()
        val get_LicenseKey = binding.editTextLicenseKey.text.toString().trim()

        val pathFolder =
            "/" + get_UserID + "/" + get_LicenseKey + "/" + Constants.App + "/" + "Config"
        val folder =
            Environment.getExternalStorageDirectory().absolutePath + "/Download/${Constants.Syn2AppLive}/" + pathFolder
        val file = File(folder, fileTypes)

        if (file.exists()) {
            isBrandindImagesFound = true
            onAllPermissionsGranted()

        } else {
            checkForValidConfileurl()
        }
    }


    private fun checkForValidConfileurl() {

        val get_tMaster = simpleSavedPassword.getString(Constants.get_editTextMaster, "").toString()
        val get_UserID = binding.editTextUserID.text.toString().trim()
        val get_LicenseKey = binding.editTextLicenseKey.text.toString().trim()

        val ServerUrl = "$get_tMaster/$get_UserID/$get_LicenseKey/App/Config/$fileNameOne"

        var isCalled = false

        lifecycleScope.launch(Dispatchers.IO) {

            val Syn2AppLive = Constants.Syn2AppLive
            val innerFolder = "/App/Config/"
            val saveDemoStorage = "/$Syn2AppLive/$get_UserID/$get_LicenseKey/$innerFolder"
            val directoryParsing =
                Environment.getExternalStorageDirectory().absolutePath + "/Download/" + saveDemoStorage
            val myFileParsing = File(directoryParsing)
            delete(myFileParsing)

            withContext(Dispatchers.Main) {

                if (!isCalled) {
                    isCalled = true
                    if (!file1) {
                        file1 = true
                        handler.postDelayed(Runnable {
                            startDownload(get_UserID, get_LicenseKey, ServerUrl, fileNameOne)
                            showToastMessage("Initializing settings")
                        }, 1000)
                    }

                }
            }
        }
    }


    private fun startDownload(
        getFolderClo: String,
        getFolderSubpath: String,
        ServerUrl: String,
        fileName: String
    ) {

        val Syn2AppLive = Constants.Syn2AppLive
        val innerFolder = "/App/Config/"
        val saveMyFileToStorage = "/$Syn2AppLive/$getFolderClo/$getFolderSubpath/$innerFolder"



        lifecycleScope.launch {
            val result = checkUrlExistence(ServerUrl)
            if (result) {

                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    saveMyFileToStorage
                )
                if (!dir.exists()) {
                    dir.mkdirs()
                }

                // save files to this folder
                val folder = File(
                    Environment.getExternalStorageDirectory()
                        .toString() + "/Download/$saveMyFileToStorage"
                )

                if (!folder.exists()) {
                    folder.mkdirs()
                }

                val request = DownloadManager.Request(Uri.parse(ServerUrl))
                request.setTitle(fileName)
                request.allowScanningByMediaScanner()
                request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS, "/$saveMyFileToStorage/$fileName"
                )
                val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadId = downloadManager.enqueue(request)


            } else {
                showPopsForMyConnectionTest(getFolderClo, getFolderSubpath, "Invalid User!")

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


    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            try {
                val receivedId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (receivedId == downloadId) {

                    val get_tMaster =
                        simpleSavedPassword.getString(Constants.get_editTextMaster, "").toString()
                    val get_UserID = binding.editTextUserID.text.toString().trim()
                    val get_LicenseKey = binding.editTextLicenseKey.text.toString().trim()

                    if (file1 && !file2 && !file3) {
                        file1 = true
                        file2 = true

                        handler.postDelayed(Runnable {
                            val ServerUrl =
                                "$get_tMaster/$get_UserID/$get_LicenseKey/App/Config/$fileNameTwo"
                            startDownload(get_UserID, get_LicenseKey, ServerUrl, fileNameTwo)
                            showToastMessage("Almost three")

                        }, 1200)

                    }

                    if (file1 && file2 && !file3) {
                        file1 = true
                        file2 = true
                        file3 = true
                        handler.postDelayed(Runnable {
                            val ServerUrl =
                                "$get_tMaster/$get_UserID/$get_LicenseKey/App/Config/$fileNameThree"
                            startDownload(get_UserID, get_LicenseKey, ServerUrl, fileNameThree)

                            showToastMessage("Finalizing settings")
                        }, 1200)
                    }

                    if (!isCalledToast) {
                        isCalledToast = true
                        if (file1 && file2 && file3) {
                            handler.postDelayed(Runnable {

                                if (customProgressDialog != null) {
                                    customProgressDialog.cancel()
                                }
                                isReadToMove_All_Permission()

                            }, 1200)
                        }
                    }

                }

            } catch (e: java.lang.Exception) {
                Log.d(TAG, "onReceive: ${e.message}")
            }
        }
    }

    inner class ConnectivityReceiver : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            try {
                val connectivityManager =
                    context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                    try {

                        handler.postDelayed(kotlinx.coroutines.Runnable {
                            binding.texttConnection.visibility = View.GONE

                        }, 1300)

                    } catch (ignored: java.lang.Exception) {
                    }
                } else {

                    // No internet Connection
                    try {
                        binding.texttConnection.visibility = View.VISIBLE
                    } catch (e: java.lang.Exception) {
                    }
                }

                // No internet Connection
            } catch (ignored: java.lang.Exception) {
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()


        try {

            isMyActivityRunning = false

            if (downloadReceiver != null) {
                unregisterReceiver(downloadReceiver)
            }


            if (connectivityReceiver != null) {
                unregisterReceiver(connectivityReceiver)
            }


        } catch (e: Exception) {
            Log.d(TAG, "onDestroy: ${e.message}")
        }
    }

}
