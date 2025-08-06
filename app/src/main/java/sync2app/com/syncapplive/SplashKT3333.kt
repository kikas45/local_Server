package sync2app.com.syncapplive
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import io.paperdb.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sync2app.com.syncapplive.AppNetworkModule.RemoteConfig
import sync2app.com.syncapplive.AppNetworkModule.RemoteConfigViewModel
import sync2app.com.syncapplive.additionalSettings.ApITVorAppMode.DomainTVModeSettings
import sync2app.com.syncapplive.additionalSettings.ApITVorAppMode.RetrofitInstanceTVMode
import sync2app.com.syncapplive.additionalSettings.InformationActivity
import sync2app.com.syncapplive.additionalSettings.ReSyncActivity
import sync2app.com.syncapplive.additionalSettings.autostartAppOncrash.Methods
import sync2app.com.syncapplive.additionalSettings.cloudAppsync.util.Common
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.additionalSettings.utils.Utility
import sync2app.com.syncapplive.additionalSettings.utils.isInternetAvailableOnBing
import sync2app.com.syncapplive.databinding.ActivitySplashBinding
import sync2app.com.syncapplive.databinding.CustomHelperLayoutBinding
import java.io.File
import java.net.URI
import java.net.URISyntaxException

class SplashKT3333 : AppCompatActivity() {

    private val viewModel by viewModels<RemoteConfigViewModel>()

    var ServerUrl: String? = null
    var infotext: TextView? = null
    var progressBar: ProgressBar? = null
    var retryBtn: TextView? = null
    var go_settings_Btn: TextView? = null
    var gotWifisettings: TextView? = null
    var goConnection: TextView? = null
    var img_swipe_reload: ImageView? = null
    var imagwifi: ImageView? = null
    var img_settings: ImageView? = null
    var imagwifi2: ImageView? = null

    var splash_image: ImageView? = null
    var backgroundImage: ImageView? = null
    var imageHelper: ImageView? = null

    var splash: ConstraintLayout? = null

    var clickcount = 0

    var handler: Handler? = null
    private var connectivityReceiver: ConnectivityReceiver? = null
    private val preferences: SharedPreferences by lazy {
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    private lateinit var binding: ActivitySplashBinding

    private var isTvModeSettingsReady = false
    private var isJsonAPICallReady = false
    private var should_My_App_Use_TV_Mode = false


    private var isCallingStart = true
    private var isMyActivityRunning = false

    private var isRetryBTN = false

    private val sharedTVAPPModePreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_TV_APP_MODE, Context.MODE_PRIVATE
        )
    }
    private val sharedBiometric: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_BIOMETRIC,
            Context.MODE_PRIVATE
        )
    }


    @SuppressLint("SourceLockedOrientationActivity", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observeViewModel()

        //=====SETUP THE SEVER URL ==
        setUpSeverUrl()

        applyOritenation()

        setUpInternetAmination()

        val handlerd = Handler(Looper.getMainLooper())
        handlerd.postDelayed(Runnable {
            binding.splash.visibility = View.VISIBLE
        }, 1000)


        try {
            //add exception
            Methods.addExceptionHandler(this)
        } catch (e: Exception) {
        }


        val sharedBiometric = getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)
        val editor22 = sharedBiometric.edit()
        editor22.remove(Constants.img_Let_offline_load_Listner)
        editor22.apply()



        infotext = binding.splashSub
        progressBar = binding.splashProgress
        retryBtn = binding.retryntn
        go_settings_Btn = binding.goSettingsBtn
        gotWifisettings = binding.gotWifisettings
        goConnection = binding.goConnection


        img_swipe_reload = binding.imageView34
        imagwifi = binding.imagwifi
        img_settings = binding.imageView35
        imagwifi2 = binding.imagwifi2
        splash_image = binding.splashImage
        imageHelper = binding.imageHelper
        splash = binding.splash


        val get_imgToggleImageBackground =
            sharedBiometric.getString(Constants.imgToggleImageBackground, "").toString()
        val get_imageUseBranding =
            sharedBiometric.getString(Constants.imageUseBranding, "").toString()
        if (get_imgToggleImageBackground == Constants.imgToggleImageBackground && get_imageUseBranding == Constants.imageUseBranding) {
            loadBackGroundImage()
        }

        if (get_imageUseBranding == Constants.imageUseBranding) {
            loadImage()
        }


        handler = Handler(Looper.getMainLooper())
        imageHelper?.setOnClickListener(View.OnClickListener {
            showToolHelpPiopUp()
            //   Toast.makeText(Splash.this, "Please wait", Toast.LENGTH_SHORT).show();
        })


        val deepBlue = resources.getColor(R.color.white)
        val deepRed = resources.getColor(R.color.red)


        // Create ObjectAnimator for text color change
        val colorAnimator = ObjectAnimator.ofInt(goConnection, "textColor", deepBlue, deepRed)


        colorAnimator.setEvaluator(ArgbEvaluator())
        colorAnimator.repeatCount = ValueAnimator.INFINITE
        colorAnimator.repeatMode = ValueAnimator.REVERSE
        colorAnimator.duration = 900 // Adjust the duration as needed


        colorAnimator.start()


        // Create ObjectAnimator for color change
        val colorAnimator22 = ObjectAnimator.ofInt(imagwifi2, "colorFilter", deepBlue, deepRed)


        colorAnimator22.setEvaluator(ArgbEvaluator())
        colorAnimator22.repeatCount = ValueAnimator.INFINITE
        colorAnimator22.repeatMode = ValueAnimator.REVERSE
        colorAnimator22.duration = 900 // Adjust the duration as needed

        colorAnimator22.start()


        go_settings_Btn?.setOnClickListener(View.OnClickListener {
            startActivity(Intent(applicationContext, SettingsActivityKT::class.java))
            finish()
            Toast.makeText(applicationContext, "Please wait", Toast.LENGTH_SHORT).show()
        })


        gotWifisettings?.setOnClickListener(View.OnClickListener {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
            Toast.makeText(applicationContext, "Please wait", Toast.LENGTH_SHORT).show()
        })




        binding.retryntn.setOnClickListener {
            lifecycleScope.launch {
                if (isInternetAvailableOnBing()) {
                    binding.retryntn.isEnabled = false
                    isRetryBTN = true
                    btnFunRetryAPiCall()
                    if (!isTvModeSettingsReady) {
                        fetchApiSettings()
                    }
                } else {
                    binding.retryntn.isEnabled = true
                    isRetryBTN = false

                    val cachedConfig = loadRemoteConfigFromPrefs(applicationContext)
                    if (cachedConfig != null) {
                        getRemoteValuesOffline(cachedConfig)
                    } else {
                        manageUIStateOnNetworkIssuesForRetry()

                    }

                }
            }

        }


    }


    private fun setUpSeverUrl() {
        val simpleSavedPassword = getSharedPreferences(Constants.SIMPLE_SAVED_PASSWORD, MODE_PRIVATE)
        val get_UserID = simpleSavedPassword.getString(Constants.get_UserID, "").toString()
        val get_LicenseKey = simpleSavedPassword.getString(Constants.get_LicenseKey, "").toString()
        val get_editTextMaster = simpleSavedPassword.getString(Constants.get_editTextMaster, "").toString()

        ServerUrl= "$get_editTextMaster/$get_UserID/$get_LicenseKey/${Constants.SEVER_APP_CONFIG_END_POINT}"

        Log.d("GET_ServerUrl", "onCreate 111: $ServerUrl")

        var preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val name = preferences.getString(Constants.surl, "").toString()


        if (name == "") {
        } else {
            if (name!!.startsWith("http://") or (name.startsWith("https://") and name.endsWith("json"))) {
                ServerUrl = name
                Log.d("GET_ServerUrl", "onCreate 222: $name")
            } else {
                Log.d("GET_ServerUrl", "onCreate 333: $name")

            }
        }
    }


    private fun setUpInternetAmination() {

        binding.splashImage.setOnClickListener {
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
        }


        if (!Utility.isNetworkAvailable(applicationContext)) {

            isCallingStart = false

            val cachedConfig = loadRemoteConfigFromPrefs(applicationContext)
            if (cachedConfig != null) {
                getRemoteValuesOffline(cachedConfig)
            } else {
                InitWebviewIndexFileState()
            }

            Log.d("MAMMA", "No internet casll Screen")
        } else {
            lifecycleScope.launch {
                if (isInternetAvailableOnBing()) {
                    binding.texttConnection?.visibility = View.GONE
                }else{
                    binding.texttConnection?.visibility = View.VISIBLE
                    binding.texttConnection.visibility = View.VISIBLE

                    val cachedConfig = loadRemoteConfigFromPrefs(applicationContext)
                    if (cachedConfig != null) {
                        getRemoteValuesOffline(cachedConfig)
                    } else {
                        InitWebviewIndexFileState()

                    }
                    isCallingStart = false

                }
            }

        }



        binding.texttConnection?.setOnClickListener {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
        }

        binding.splashImage.setOnClickListener {
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
        }


        val deepBlue = resources.getColor(R.color.white)
        val deepRed = resources.getColor(R.color.red)

        val colorAnimator = ObjectAnimator.ofInt(binding.texttConnection, "textColor", deepBlue, deepRed)
        colorAnimator.setEvaluator(ArgbEvaluator())
        colorAnimator.repeatCount = ValueAnimator.INFINITE
        colorAnimator.repeatMode = ValueAnimator.REVERSE
        colorAnimator.duration = 900
        colorAnimator.start()
    }

    private fun loadRemoteConfigFromPrefs(context: Context): RemoteConfig? {
        val prefs = context.getSharedPreferences("AppConfigPrefs", Context.MODE_PRIVATE)
        val json = prefs.getString("remote_config", null)
        return json?.let {
            try {
                Gson().fromJson(it, RemoteConfig::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }



    @SuppressLint("SetTextI18n")
    private fun getRemoteValuesOffline(config: RemoteConfig?) {
        showToastMessage("Loading from offline database")
        infotext?.setText("Loading from offline database")

        if (config == null){
            val cachedConfig = loadRemoteConfigFromPrefs(applicationContext)
            if (cachedConfig != null) {
                getRemoteValuesOffline(cachedConfig)
            } else {
                isCallingStart = false
               InitWebviewIndexFileState()
            }

            return
        }

        // home remote data
        val homeurl = config.homeUrl


        // Bottom Bar
        constants.ShowBottomBar = config.ShowBottomBar
        constants.ChangeBottombarBgColor = config.ChangeBottomBarBgColor
        constants.bottomBarBgColor = config.bottomBarBackgroundColor

        // Bottom Menu URLs
        constants.bottomUrl1 = config.bottom1
        constants.bottomUrl2 = config.bottom2
        constants.bottomUrl3 = config.bottom3
        constants.bottomUrl4 = config.bottom4
        constants.bottomUrl5 = config.bottom5
        constants.bottomUrl6 = config.bottom6

        // Bottom Menu Images
        constants.bottomBtn1ImgUrl = config.bottom1_img_url
        constants.bottomBtn2ImgUrl = config.bottom2_img_url
        constants.bottomBtn3ImgUrl = config.bottom3_img_url
        constants.bottomBtn4ImgUrl = config.bottom4_img_url
        constants.bottomBtn5ImgUrl = config.bottom5_img_url
        constants.bottomBtn6ImgUrl = config.bottom6_img_url

        // Drawer Menu
        constants.ChangeDrawerHeaderBgColor = config.ChangeDrawerHeaderColor
        constants.ChangeHeaderTextColor = config.ChangeDrawerHeaderTextColor
        constants.ShowDrawer = config.ShowDrawerMenu
        constants.drawerMenuBtnUrl = config.DrawerMenuUrl
        constants.drawerMenuImgUrl = config.DrawerMenuImgUrl
        constants.drawerMenuItem1ImgUrl = config.DrawerMenuImg1Url
        constants.drawerMenuItem2ImgUrl = config.DrawerMenuImg2Url
        constants.drawerMenuItem3ImgUrl = config.DrawerMenuImg3Url
        constants.drawerMenuItem4ImgUrl = config.DrawerMenuImg4Url
        constants.drawerMenuItem5ImgUrl = config.DrawerMenuImg5Url
        constants.drawerMenuItem6ImgUrl = config.DrawerMenuImg6Url
        constants.drawerMenuItem1Url = config.DrawerMenuItem1Url
        constants.drawerMenuItem2Url = config.DrawerMenuItem2Url
        constants.drawerMenuItem3Url = config.DrawerMenuItem3Url
        constants.drawerMenuItem4Url = config.DrawerMenuItem4Url
        constants.drawerMenuItem5Url = config.DrawerMenuItem5Url
        constants.drawerMenuItem6Url = config.DrawerMenuItem6Url
        constants.drawerMenuItem1Text = config.DrawerMenuItem1Title
        constants.drawerMenuItem2Text = config.DrawerMenuItem2Title
        constants.drawerMenuItem3Text = config.DrawerMenuItem3Title
        constants.drawerMenuItem4Text = config.DrawerMenuItem4Title
        constants.drawerMenuItem5Text = config.DrawerMenuItem5Title
        constants.drawerMenuItem6Text = config.DrawerMenuItem6Title
        constants.drawerHeaderImgUrl = config.DrawerHeaderImgUrl
        constants.drawerHeaderText = config.DrawerHeaderText
        constants.drawerHeaderImgCommand = config.DrawerHeaderImgCommand
        constants.drawerHeaderBgColor = config.DrawerHeaderBgColor
        constants.drawerHeaderTextColor = config.DrawerHeaderTextColor

        // Toolbar
        constants.ShowToolbar = config.ShowToolbar
        constants.ToolbarTitleText = config.ToolbarTitleText
        constants.ToolbarTitleTextColor = config.ToolbarTitleTextColor
        constants.ToolbarBgColor = config.ToolbarBgColor
        constants.ChangeToolbarBgColor = config.ChangeToolbarBgColor
        constants.ChangeTittleTextColor = config.ChangeToolbarTitleTextColor

        // Floating Button
        constants.Web_button_link = config.webBtnUrl
        constants.Web_button_Img_link = config.webBtnImgUrl

        // Ads
        constants.ShowAdmobBanner = config.admobBanner
        constants.ShowAdmobInterstitial = config.admobInter

        // Notifications
        constants.OnesigID = config.onesigID
        constants.splashUrl = config.splashUrl
        constants.Notifx_service = config.NotifXService

        // Server URL Setup
        constants.ShowServerUrlSetUp = config.AllowChangingServerUrl
        constants.AllowOnlyHostUrlInApp = config.allowOnlyHostUrl

        // Update
        constants.UpdateAvailable = config.UpdateAvailable
        constants.ForceUpdate = config.ForceUpdate
        constants.UpdateTitle = config.Updatetitle
        constants.UpdateMessage = config.UpdateMsg
        constants.UpdateUrl = config.UpdateUrl
        constants.NewVersion = config.NewVersion

        // Welcome Screen
        constants.EnableWelcomeSlider = config.AllowWelcomeSlider

        // Welcome Screen Titles
        constants.screen1TitleText = config.Screen1Title
        constants.screen2TitleText = config.Screen2Title
        constants.screen3TitleText = config.Screen3Title
        constants.screen4TitleText = config.Screen4Title

        // Welcome Screen Descriptions
        constants.screen1Desc = config.screen1Desc
        constants.screen2Desc = config.screen2Desc
        constants.screen3Desc = config.screen3Desc
        constants.screen4Desc = config.screen4Desc

        // Welcome Screen Background Colors
        constants.screen1BgColor = config.Screen1bgColor
        constants.screen2BgColor = config.Screen2bgColor
        constants.screen3BgColor = config.Screen3bgColor
        constants.screen4BgColor = config.Screen4bgColor

        // Welcome Screen Text Colors
        constants.screen1TextColor = config.Screen1TxtColor
        constants.screen2TextColor = config.Screen2TxtColor
        constants.screen3TextColor = config.Screen3TxtColor
        constants.screen4TextColor = config.Screen4TxtColor

        // Welcome Screen Images
        constants.screen1Img = config.Screen1ImgUrl
        constants.screen2Img = config.Screen2ImgUrl
        constants.screen3Img = config.Screen3ImgUrl
        constants.screen4Img = config.Screen4ImgUrl

        setUpNavigationMethod(homeurl)

    }


    private fun loadImage() {
        splash_image = findViewById(R.id.splash_image)
        val sharedP = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
        val getFolderClo = sharedP.getString(Constants.getFolderClo, "").toString()
        val getFolderSubpath = sharedP.getString(Constants.getFolderSubpath, "").toString()

        val baseDir = getExternalFilesDir(null) // App-private external storage
        val relativePath = "Syn2AppLive/$getFolderClo/$getFolderSubpath/${Constants.App}/Config"
        val folder = File(baseDir, relativePath)
        val fileTypes = "app_logo.png"
        val file = File(folder, fileTypes)

        if (file.exists()) {
            Glide.with(this).load(file).centerCrop().into(binding.splashImage)
        }
    }


    private fun loadBackGroundImage() {
        backgroundImage = findViewById(R.id.backgroundImage)
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





    private fun observeViewModel() {
        viewModel.configLiveData.observe(this) { result ->
            result
                .onSuccess { response ->
                    getRemoteValues(response.remoteConfig)
                }
                .onFailure { error ->
                    val cachedConfig = loadRemoteConfigFromPrefs(applicationContext)
                    if (cachedConfig != null) {
                        getRemoteValuesOffline(cachedConfig)
                    } else {
                        showErrorUI(error.toString())
                    }
                }
        }
    }



   private fun getRemoteValues(config: RemoteConfig?) {
        infotext?.setText(R.string.initializing)

        if (config == null){
            showErrorUI("Something went wrong, Invalid Remote data or Malformed AppConfig JSON")
            return
        }

        // home remote data
        val homeurl = config.homeUrl


        // Bottom Bar
        constants.ShowBottomBar = config.ShowBottomBar
        constants.ChangeBottombarBgColor = config.ChangeBottomBarBgColor
        constants.bottomBarBgColor = config.bottomBarBackgroundColor

        // Bottom Menu URLs
        constants.bottomUrl1 = config.bottom1
        constants.bottomUrl2 = config.bottom2
        constants.bottomUrl3 = config.bottom3
        constants.bottomUrl4 = config.bottom4
        constants.bottomUrl5 = config.bottom5
        constants.bottomUrl6 = config.bottom6

        // Bottom Menu Images
        constants.bottomBtn1ImgUrl = config.bottom1_img_url
        constants.bottomBtn2ImgUrl = config.bottom2_img_url
        constants.bottomBtn3ImgUrl = config.bottom3_img_url
        constants.bottomBtn4ImgUrl = config.bottom4_img_url
        constants.bottomBtn5ImgUrl = config.bottom5_img_url
        constants.bottomBtn6ImgUrl = config.bottom6_img_url

        // Drawer Menu
        constants.ChangeDrawerHeaderBgColor = config.ChangeDrawerHeaderColor
        constants.ChangeHeaderTextColor = config.ChangeDrawerHeaderTextColor
        constants.ShowDrawer = config.ShowDrawerMenu
        constants.drawerMenuBtnUrl = config.DrawerMenuUrl
        constants.drawerMenuImgUrl = config.DrawerMenuImgUrl
        constants.drawerMenuItem1ImgUrl = config.DrawerMenuImg1Url
        constants.drawerMenuItem2ImgUrl = config.DrawerMenuImg2Url
        constants.drawerMenuItem3ImgUrl = config.DrawerMenuImg3Url
        constants.drawerMenuItem4ImgUrl = config.DrawerMenuImg4Url
        constants.drawerMenuItem5ImgUrl = config.DrawerMenuImg5Url
        constants.drawerMenuItem6ImgUrl = config.DrawerMenuImg6Url
        constants.drawerMenuItem1Url = config.DrawerMenuItem1Url
        constants.drawerMenuItem2Url = config.DrawerMenuItem2Url
        constants.drawerMenuItem3Url = config.DrawerMenuItem3Url
        constants.drawerMenuItem4Url = config.DrawerMenuItem4Url
        constants.drawerMenuItem5Url = config.DrawerMenuItem5Url
        constants.drawerMenuItem6Url = config.DrawerMenuItem6Url
        constants.drawerMenuItem1Text = config.DrawerMenuItem1Title
        constants.drawerMenuItem2Text = config.DrawerMenuItem2Title
        constants.drawerMenuItem3Text = config.DrawerMenuItem3Title
        constants.drawerMenuItem4Text = config.DrawerMenuItem4Title
        constants.drawerMenuItem5Text = config.DrawerMenuItem5Title
        constants.drawerMenuItem6Text = config.DrawerMenuItem6Title
        constants.drawerHeaderImgUrl = config.DrawerHeaderImgUrl
        constants.drawerHeaderText = config.DrawerHeaderText
        constants.drawerHeaderImgCommand = config.DrawerHeaderImgCommand
        constants.drawerHeaderBgColor = config.DrawerHeaderBgColor
        constants.drawerHeaderTextColor = config.DrawerHeaderTextColor

        // Toolbar
        constants.ShowToolbar = config.ShowToolbar
        constants.ToolbarTitleText = config.ToolbarTitleText
        constants.ToolbarTitleTextColor = config.ToolbarTitleTextColor
        constants.ToolbarBgColor = config.ToolbarBgColor
        constants.ChangeToolbarBgColor = config.ChangeToolbarBgColor
        constants.ChangeTittleTextColor = config.ChangeToolbarTitleTextColor

        // Floating Button
        constants.Web_button_link = config.webBtnUrl
        constants.Web_button_Img_link = config.webBtnImgUrl

        // Ads
        constants.ShowAdmobBanner = config.admobBanner
        constants.ShowAdmobInterstitial = config.admobInter

        // Notifications
        constants.OnesigID = config.onesigID
        constants.splashUrl = config.splashUrl
        constants.Notifx_service = config.NotifXService

        // Server URL Setup
        constants.ShowServerUrlSetUp = config.AllowChangingServerUrl
        constants.AllowOnlyHostUrlInApp = config.allowOnlyHostUrl

        // Update
        constants.UpdateAvailable = config.UpdateAvailable
        constants.ForceUpdate = config.ForceUpdate
        constants.UpdateTitle = config.Updatetitle
        constants.UpdateMessage = config.UpdateMsg
        constants.UpdateUrl = config.UpdateUrl
        constants.NewVersion = config.NewVersion

        // Welcome Screen
        constants.EnableWelcomeSlider = config.AllowWelcomeSlider

        // Welcome Screen Titles
        constants.screen1TitleText = config.Screen1Title
        constants.screen2TitleText = config.Screen2Title
        constants.screen3TitleText = config.Screen3Title
        constants.screen4TitleText = config.Screen4Title

        // Welcome Screen Descriptions
        constants.screen1Desc = config.screen1Desc
        constants.screen2Desc = config.screen2Desc
        constants.screen3Desc = config.screen3Desc
        constants.screen4Desc = config.screen4Desc

        // Welcome Screen Background Colors
        constants.screen1BgColor = config.Screen1bgColor
        constants.screen2BgColor = config.Screen2bgColor
        constants.screen3BgColor = config.Screen3bgColor
        constants.screen4BgColor = config.Screen4bgColor

        // Welcome Screen Text Colors
        constants.screen1TextColor = config.Screen1TxtColor
        constants.screen2TextColor = config.Screen2TxtColor
        constants.screen3TextColor = config.Screen3TxtColor
        constants.screen4TextColor = config.Screen4TxtColor

        // Welcome Screen Images
        constants.screen1Img = config.Screen1ImgUrl
        constants.screen2Img = config.Screen2ImgUrl
        constants.screen3Img = config.Screen3ImgUrl
        constants.screen4Img = config.Screen4ImgUrl


        saveRemoteConfigToPrefs(applicationContext, config)
        setUpNavigationMethod(homeurl)

    }


  private  fun saveRemoteConfigToPrefs(context: Context, config: RemoteConfig) {
        val prefs = context.getSharedPreferences("AppConfigPrefs", Context.MODE_PRIVATE)
        val json = Gson().toJson(config)
        prefs.edit().putString("remote_config", json).apply()
    }



    private fun showErrorUI(error: String) {
        infotext!!.text = "Error occurred! =$error"
        manageUIStateOnNetworkIssuesForRetry()
        isJsonAPICallReady = false
        progressBar!!.visibility = View.GONE
        InitWebviewIndexFileState()
        isCallingStart = false
    }




    private fun ApiCall(url: String?) {

        if (isCallingStart) {
            infotext!!.setText(R.string.connecting)
            progressBar!!.visibility = View.VISIBLE

            url?.let { viewModel.getRemoteConfig(it) }

        } else {
            showToastMessage("Slow internet connection")
            isCallingStart = false

            val cachedConfig = loadRemoteConfigFromPrefs(applicationContext)
            if (cachedConfig != null) {
                getRemoteValuesOffline(cachedConfig)
            } else {
                InitWebviewIndexFileState()
            }


        }
    }



    private fun setUpNavigationMethod(homeurl: String) {
        isJsonAPICallReady = true

        val sharedBiometric = getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)
        val get_INSTALL_TV_JSON_USER_CLICKED = sharedTVAPPModePreferences.getString(Constants.INSTALL_TV_JSON_USER_CLICKED, "").orEmpty()
        val getFirstMode = sharedTVAPPModePreferences.getString(Constants.installTVModeForFirstTime, "").orEmpty()
        val getTvMode = sharedBiometric.getString(Constants.CALL_RE_SYNC_MANGER, "").orEmpty()

        if (!URLUtil.isValidUrl(homeurl)) {
            showOfflineErrorUI()
            return
        }

        constants.jsonUrl = homeurl
        Log.d("PETER", "InitWebvIewloadStates: Splash K  The JSON_MAIN_URl $homeurl")

        try {
            val uri = URI(homeurl)
            constants.filterdomain = uri.host
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }

        lifecycleScope.launch {
            delay(1500L)

            if (get_INSTALL_TV_JSON_USER_CLICKED == Constants.INSTALL_TV_JSON_USER_CLICKED) {
                if (should_My_App_Use_TV_Mode) {
                    sharedBiometric.edit().apply {
                        putString(Constants.get_Launching_State_Of_WebView, Constants.launch_WebView_Offline)
                        putString(Constants.PROTECT_PASSWORD, Constants.PROTECT_PASSWORD)
                        apply()
                    }
                    preferences.edit().putBoolean(Constants.swiperefresh, false).apply()

                    when (sharedBiometric.getString(Constants.FIRST_INFORMATION_PAGE_COMPLETED, "").orEmpty()) {
                        Constants.FIRST_INFORMATION_PAGE_COMPLETED -> {
                            if (getFirstMode != Constants.installTVModeForFirstTime) {
                                startActivity(Intent(applicationContext, ReSyncActivity::class.java).apply {
                                    putExtra("url", constants.jsonUrl)
                                })
                            } else {
                                startActivity(Intent(applicationContext, WebViewPage::class.java).apply {
                                    putExtra("url", constants.jsonUrl)
                                })
                            }
                        }
                        else -> {
                            startActivity(Intent(applicationContext, InformationActivity::class.java))
                        }
                    }

                } else {
                    sharedBiometric.edit().apply {
                        putString(Constants.get_Launching_State_Of_WebView, Constants.launch_Default_WebView_url)
                        remove(Constants.PROTECT_PASSWORD)
                        apply()
                    }
                    preferences.edit().putBoolean(Constants.swiperefresh, true).apply()

                    when (sharedBiometric.getString(Constants.FIRST_INFORMATION_PAGE_COMPLETED, "").orEmpty()) {
                        Constants.FIRST_INFORMATION_PAGE_COMPLETED -> {
                            startActivity(Intent(applicationContext, WebViewPage::class.java).apply {
                                putExtra("url", constants.jsonUrl)
                            })
                        }
                        else -> {
                            startActivity(Intent(applicationContext, InformationActivity::class.java))
                        }
                    }
                }

            } else {
                when (sharedBiometric.getString(Constants.FIRST_INFORMATION_PAGE_COMPLETED, "").orEmpty()) {
                    Constants.FIRST_INFORMATION_PAGE_COMPLETED -> {
                        if (getTvMode == Constants.CALL_RE_SYNC_MANGER) {
                            startActivity(Intent(applicationContext, ReSyncActivity::class.java).apply {
                                putExtra("url", constants.jsonUrl)
                            })
                        } else {
                            startActivity(Intent(applicationContext, WebViewPage::class.java).apply {
                                putExtra("url", constants.jsonUrl)
                            })
                        }
                    }
                    else -> {
                        startActivity(Intent(applicationContext, InformationActivity::class.java))
                    }
                }
            }

            finish()
        }
    }
    private fun showOfflineErrorUI() {
        infotext?.setText(R.string.invalide_remote_data)
        progressBar?.visibility = View.GONE

        listOf(
            retryBtn,
            go_settings_Btn,
            gotWifisettings,
            goConnection,
            img_swipe_reload,
            imagwifi,
            img_settings,
            imagwifi2,
            imageHelper
        ).forEach { it?.visibility = View.VISIBLE }
    }



    private fun btnFunRetryAPiCall() {
        clickcount++
        if (clickcount == 3) {

            val myactivity = Intent(applicationContext, SettingsActivityKT::class.java)
            myactivity.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(myactivity)
            finish()
            constants.ShowServerUrlSetUp = true
        } else {

            if (!isJsonAPICallReady) {
                ApiCall(ServerUrl)
            }
            manageUIStateOnNetworkIssues()
        }
    }


    private fun manageUIStateOnNetworkIssues() {
        if (retryBtn!!.visibility == View.VISIBLE) {
            retryBtn!!.visibility = View.GONE
        }
        if (go_settings_Btn!!.visibility == View.VISIBLE) {
            go_settings_Btn!!.visibility = View.GONE
        }
        if (gotWifisettings!!.visibility == View.VISIBLE) {
            gotWifisettings!!.visibility = View.GONE
        }
        if (goConnection!!.visibility == View.VISIBLE) {
            goConnection!!.visibility = View.GONE
        }
        if (img_swipe_reload!!.visibility == View.VISIBLE) {
            img_swipe_reload!!.visibility = View.GONE
        }
        if (imagwifi!!.visibility == View.VISIBLE) {
            imagwifi!!.visibility = View.GONE
        }
        if (img_settings!!.visibility == View.VISIBLE) {
            img_settings!!.visibility = View.GONE
        }
        if (imagwifi2!!.visibility == View.VISIBLE) {
            imagwifi2!!.visibility = View.GONE
        }
        if (imageHelper!!.visibility == View.VISIBLE) {
            imageHelper!!.visibility = View.GONE
        }

    }

    private fun manageUIStateOnNetworkIssuesForRetry() {
        if (retryBtn!!.visibility == View.GONE) {
            retryBtn!!.visibility = View.VISIBLE
        }
        if (go_settings_Btn!!.visibility == View.GONE) {
            go_settings_Btn!!.visibility = View.VISIBLE
        }
        if (gotWifisettings!!.visibility == View.GONE) {
            gotWifisettings!!.visibility = View.VISIBLE
        }
        if (goConnection!!.visibility == View.GONE) {
            goConnection!!.visibility = View.VISIBLE
        }
        if (img_swipe_reload!!.visibility == View.GONE) {
            img_swipe_reload!!.visibility = View.VISIBLE
        }
        if (imagwifi!!.visibility == View.GONE) {
            imagwifi!!.visibility = View.VISIBLE
        }
        if (img_settings!!.visibility == View.GONE) {
            img_settings!!.visibility = View.VISIBLE
        }
        if (imagwifi2!!.visibility == View.GONE) {
            imagwifi2!!.visibility = View.VISIBLE
        }
        if (imageHelper!!.visibility == View.GONE) {
            imageHelper!!.visibility = View.VISIBLE
        }

    }


    @SuppressLint("MissingInflatedId", "UseCompatLoadingForDrawables")
    private fun showToolHelpPiopUp() {
        val binding: CustomHelperLayoutBinding = CustomHelperLayoutBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
        builder.setView(binding.getRoot())
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.setCancelable(true)
        if (alertDialog.window != null) {
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        alertDialog.show()
    }


    override fun onResume() {
        super.onResume()

        connectivityReceiver = ConnectivityReceiver()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            registerReceiver(connectivityReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(connectivityReceiver, intentFilter)
        }


        isMyActivityRunning = true


    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(connectivityReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        //  unregisterReceiver(connectivityReceiver);
        isMyActivityRunning = false
    }

    private var isNetWorkCallingApi = true

    inner class ConnectivityReceiver : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            try {
                val connectivityManager =
                    context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {

                    if (isNetWorkCallingApi) {
                        isNetWorkCallingApi = false
                        lifecycleScope.launch {
                            if (isInternetAvailableOnBing()) {
                                loadUiOnNetWorksReturn()
                                isNetWorkCallingApi = true
                            } else {
                                // No internet Connection
                                loadUiWhenNetWorkIsDown()
                                isNetWorkCallingApi = true
                            }
                        }

                    }

                } else {
                    // No internet Connection
                    loadUiWhenNetWorkIsDown()
                }
                // No internet Connection
            } catch (ignored: java.lang.Exception) {
            }
        }
    }

    private fun loadUiOnNetWorksReturn() {
        binding.apply {
            try {
                isCallingStart = true

                val SPLASH_TIME_OUT = 1300
                Handler().postDelayed({
                    try {

                        fetchApiSettings()
                        binding.texttConnection?.visibility = View.GONE
                        progressBar?.setVisibility(View.VISIBLE)
                        retryBtn?.setVisibility(View.GONE)
                        go_settings_Btn?.setVisibility(View.GONE)
                        gotWifisettings?.setVisibility(View.GONE)
                        goConnection?.setVisibility(View.GONE)
                        img_swipe_reload?.setVisibility(View.GONE)
                        imagwifi?.setVisibility(View.GONE)
                        img_settings?.setVisibility(View.GONE)
                        imagwifi2?.setVisibility(View.GONE)
                        splash_image?.setVisibility(View.VISIBLE)
                        imageHelper?.setVisibility(View.GONE)
                    } catch (e: java.lang.Exception) {
                    }
                }, SPLASH_TIME_OUT.toLong())
            } catch (ignored: java.lang.Exception) {
            }
        }
    }

    private fun loadUiWhenNetWorkIsDown() {
        binding.apply {
            try {
                isCallingStart = false

                binding.texttConnection?.visibility = View.VISIBLE
                infotext?.setText("No Internet Connection")
                progressBar?.setVisibility(View.GONE)
                retryBtn?.setVisibility(View.VISIBLE)
                go_settings_Btn?.setVisibility(View.VISIBLE)
                gotWifisettings?.setVisibility(View.VISIBLE)
                goConnection?.setVisibility(View.VISIBLE)
                img_swipe_reload?.setVisibility(View.VISIBLE)
                imagwifi?.setVisibility(View.VISIBLE)
                img_settings?.setVisibility(View.VISIBLE)
                imagwifi2?.setVisibility(View.VISIBLE)
                splash_image?.setVisibility(View.VISIBLE)
                imageHelper?.setVisibility(View.VISIBLE)
            } catch (e: java.lang.Exception) {
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun fetchApiSettings() {
        if (!isCallingStart) {
            showToastMessage("Slow internet connection")
            loadOfflineOrInitWebview()
            return
        }

        val prefs = getSharedPreferences(Constants.SIMPLE_SAVED_PASSWORD, MODE_PRIVATE)
        val get_tMaster = prefs.getString(Constants.get_editTextMaster, "") ?: ""
        val get_UserID = prefs.getString(Constants.get_UserID, "") ?: ""
        val get_LicenseKey = prefs.getString(Constants.get_LicenseKey, "") ?: ""
        val path = "$get_UserID/$get_LicenseKey/${Constants.END_PATH_OF_TV_MODE_URL}"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val apiService = RetrofitInstanceTVMode.createApiService(get_tMaster)
                val response = apiService.getAppConfig(path)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.InstallAppSettings?.let {
                            applyAppModeSettings(it)
                            isTvModeSettingsReady = true

                            if (!isJsonAPICallReady) {
                                ApiCall(ServerUrl)
                            }
                        } ?: run {
                            handleFailedApiCall("Malformed AppConfig JSON or Empty Response")
                        }
                    } else {
                        handleFailedApiCall("API Response Error: ${response.message()}")
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    handleFailedApiCall("Exception: ${e.localizedMessage}")
                }
            }
        }
    }


    private fun applyAppModeSettings(settings: DomainTVModeSettings) {
        sharedTVAPPModePreferences.edit().apply {
            putBoolean(Constants.installTVMode, settings.install_TV_mode)
            putBoolean(Constants.hide_TV_Mode_Label, settings.hide_TV_mode_label)
            putBoolean(Constants.fullScreen_APP, settings.full_Screen)
            putBoolean(Constants.hide_Full_ScreenLabel, settings.hide_Full_Screen_Label)
            putBoolean(Constants.immersive_Mode_APP, settings.immersive_Mode)
            putBoolean(Constants.hide_Immersive_ModeLabel, settings.hide_Immersive_Mode_Label)
            putBoolean(Constants.hide_BottomBar_APP, settings.hide_Bottom_Bar)
            putBoolean(Constants.hide_Bottom_Bar_Label_APP, settings.hide_Bottom_Bar_Label)
            putBoolean(Constants.hideBottom_MenuIcon_APP, settings.hide_Bottom_Menu_Icon)
            putBoolean(Constants.hide_Bottom_MenuIconLabel_APP, settings.hide_Bottom_Menu_Icon_Label)
            putBoolean(Constants.hide_Floating_Button_APP, settings.hide_Floating_Button)
            putBoolean(Constants.hide_Floating_ButtonLabel_APP, settings.hide_Floating_Button_Label)
            putBoolean(Constants.use_local_schedule_APP, settings.use_local_schedule)
            putBoolean(Constants.show_local_schedule_label, settings.show_local_schedule_label)
            apply()
        }

        // Save mode flag
        val mode = if (settings.install_TV_mode) Constants.TV_Mode else Constants.App
        sharedBiometric.edit().putString(Constants.MY_TV_OR_APP_MODE, mode).apply()

        // Save schedule preference in Paper
        Paper.book().write(
            Common.set_schedule_key,
            if (settings.use_local_schedule) Common.schedule_offline else Common.schedule_online
        )
    }


    private fun handleFailedApiCall(message: String) {
        Log.e("API_FAIL", message)
        isTvModeSettingsReady = false
        isCallingStart = false
        infotext?.text = "Error: $message"
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()

        val cachedConfig = loadRemoteConfigFromPrefs(applicationContext)
        if (cachedConfig != null) {
            getRemoteValuesOffline(cachedConfig)
        } else {
            manageUIStateOnNetworkIssuesForRetry()
            InitWebviewIndexFileState()
        }
    }

    private fun loadOfflineOrInitWebview() {
        isCallingStart = false
        val cachedConfig = loadRemoteConfigFromPrefs(applicationContext)
        if (cachedConfig != null) {
            getRemoteValuesOffline(cachedConfig)
        } else {
            InitWebviewIndexFileState()
        }
    }


    @SuppressLint("SourceLockedOrientationActivity")
    private fun applyOritenation() {

        // make screen to be full screen
        //  window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        // stop all service

        Utility.hideSystemBars(window)


        val getState = sharedBiometric.getString(Constants.IMG_TOGGLE_FOR_ORIENTATION, "").toString()

        if (getState == Constants.USE_POTRAIT) {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        } else if (getState == Constants.USE_LANDSCAPE) {

            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        } else if (getState == Constants.USE_UNSEPECIFIED) {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        }

    }


    private fun InitWebviewIndexFileState() {

        // get input paths to device storage
        val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
        val fil_CLO = myDownloadClass.getString(Constants.getFolderClo, "").toString()
        val fil_DEMO = myDownloadClass.getString(Constants.getFolderSubpath, "").toString()

        val filename = "/index.html"
        lifecycleScope.launch {
            loadIndexFileIfExist(fil_CLO, fil_DEMO, filename)

            Log.d("MAMMA", "No internet casll Screen CLO: $fil_CLO   DEMO: $fil_DEMO")
        }

    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun loadIndexFileIfExist(
        CLO: String,
        DEMO: String,
        fileName: String
    ) {
        lifecycleScope.launch {
            try {

                val filePath = withContext(Dispatchers.IO) {
                    try {
                        getFilePath(CLO, DEMO, fileName)
                    } catch (e: Exception) {
                        // showToastMessage("You need to Sync Files for Offline Usage")
                        null
                    }
                }

                // Now back on the main thread to update the UI
                if (filePath != null) {

                    Log.d("MAMMA", "No internet casll Screen  File !=null")
                    if (!isCallingStart && isMyActivityRunning) {

                        Log.d("MAMMA", "My activoitu is runing")

                        val sharedBiometric: SharedPreferences =
                            applicationContext.getSharedPreferences(
                                Constants.SHARED_BIOMETRIC,
                                MODE_PRIVATE
                            )
                        val get_TV_or_App_Mode =
                            sharedBiometric.getString(Constants.MY_TV_OR_APP_MODE, "").toString()
                        val JSON_MAIN_URL =
                            sharedBiometric.getString(Constants.JSON_MAIN_URL, "").toString()

                        Log.d("MAMMA", "App_State  :::$get_TV_or_App_Mode")

                        if (get_TV_or_App_Mode == Constants.TV_Mode) {
                            val editText88 = sharedBiometric.edit()
                            editText88.putString(
                                Constants.get_Launching_State_Of_WebView,
                                Constants.launch_WebView_Offline
                            )
                            editText88.apply()

                            val myActivity = Intent(applicationContext, WebViewPage::class.java)
                            myActivity.putExtra(
                                Constants.USE_TEMP_OFFLINE_WEB_VIEW_PAGE,
                                Constants.USE_TEMP_OFFLINE_WEB_VIEW_PAGE
                            )
                            startActivity(myActivity)
                            finish()
                            Log.d("MAMMA", "TV: Splash Screen")

                        } else {

                            Log.d("MAMMA", "Appp: TV_MODE_")

                            val editText88 = sharedBiometric.edit()
                            editText88.putString(
                                Constants.get_Launching_State_Of_WebView,
                                Constants.launch_Default_WebView_url
                            )
                            editText88.apply()

                            val myActivity = Intent(applicationContext, WebViewPage::class.java)
                            myActivity.putExtra(
                                Constants.USE_TEMP_OFFLINE_WEB_VIEW_PAGE,
                                Constants.USE_TEMP_OFFLINE_WEB_VIEW_PAGE
                            )

                            val urlPath =
                                "${Constants.CUSTOM_CP_SERVER_DOMAIN}/$CLO/$DEMO/App/$fileName"

                            if (JSON_MAIN_URL != null) {
                                myActivity.putExtra("url", JSON_MAIN_URL)
                                Log.d("MAMMA", "Appp: $JSON_MAIN_URL")
                            } else {
                                myActivity.putExtra("url", urlPath)
                                Log.d("MAMMA", "Appp: $urlPath")
                            }

                            startActivity(myActivity)
                            finish()


                        }

                    } else {
                        Log.d("MAMMA", "Pull out: Splash Screen")
                    }
                } else {
                    ///  showToastMessage("You need to Sync Files for Offline Usage")

                    Log.d("MAMMA", "No files: Splash Screen")
                }


            } catch (e: Exception) {
                //  showToastMessage("You need to Sync Files for Offline Usage")
            }
        }
    }


    private fun getFilePath(CLO: String, DEMO: String, filename: String): String? {
        val baseDir = getExternalFilesDir(null)  // ✅ App-private scoped external storage
        val relativePath = "${Constants.Syn2AppLive}/$CLO/$DEMO/${Constants.App}"
        val destinationFolder = File(baseDir, relativePath)
        val myFile = File(destinationFolder, filename)

        return if (myFile.exists()) {
            /// myFile.toURI().toString()  // Use proper file URI (e.g. file:///...)
            myFile.toURI().toURL().toString()
        } else {
            null
        }
    }


    private fun showToastMessage(message: String) {
        try {
            runOnUiThread {
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            }
        } catch (e: java.lang.Exception) {
        }
    }


}