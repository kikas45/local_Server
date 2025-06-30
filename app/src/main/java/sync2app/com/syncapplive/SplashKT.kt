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
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.HttpException
import io.paperdb.Paper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import sync2app.com.syncapplive.additionalSettings.ApITVorAppMode.RetrofitInstanceTVMode
import sync2app.com.syncapplive.additionalSettings.InformationActivity
import sync2app.com.syncapplive.additionalSettings.ReSyncActivity
import sync2app.com.syncapplive.additionalSettings.autostartAppOncrash.Methods
import sync2app.com.syncapplive.additionalSettings.cloudAppsync.util.Common
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.additionalSettings.utils.Utility
import sync2app.com.syncapplive.databinding.ActivitySplashBinding
import sync2app.com.syncapplive.databinding.CustomHelperLayoutBinding
import java.io.File
import java.net.URI
import java.net.URISyntaxException

class SplashKT : AppCompatActivity() {


    var ServerUrl: String? = null
    //   String Jsonurl = ServerUrl;

    //   String Jsonurl = ServerUrl;
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

    private  var isRetryBTN = false

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

        applyOritenation()

        setUpInternetAmination()

        val handlerd = Handler(Looper.getMainLooper())
        handlerd.postDelayed(Runnable {
            binding.splash.visibility = View.VISIBLE
        }, 1000)


        val sharedLicenseKeys = getSharedPreferences(Constants.SIMPLE_SAVED_PASSWORD, MODE_PRIVATE)
        ServerUrl = sharedLicenseKeys.getString(Constants.get_masterDomain, "").toString()

        Log.d("ServerUrl", "onCreate: $ServerUrl")

        var preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val name = preferences.getString(Constants.surl, "").toString()

        if (name == "") {
        } else {
            if (name!!.startsWith("http://") or (name.startsWith("https://") and name.endsWith("json"))) {
                ServerUrl = name
                Log.d("Remote Execution", "Using custom server address")
            } else {
                Log.d("Remote Execution", "Invalid server url$name")
            }
        }

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
            if (Utility.isNetworkAvailable(applicationContext)) {
                isRetryBTN = true
                btnFunRetryAPiCall()
                if (!isTvModeSettingsReady) {
                    fetchApiSettings()
                }
            }else{

                val handler2000 = Handler(Looper.getMainLooper())
                handler2000.postDelayed(Runnable {
                    manageUIStateOnNetworkIssuesForRetry()
                    isRetryBTN = false
                }, Constants.timeForConnection)


            }
        }

    }



    private fun setUpInternetAmination() {

        binding.splashImage.setOnClickListener {
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
        }


        if (!Utility.isNetworkAvailable(applicationContext)) {
            InitWebviewIndexFileState()
            isCallingStart = false

            Log.d("MAMMA", "No internet casll Screen")
        }


        val handler2000 = Handler(Looper.getMainLooper())
        handler2000.postDelayed(Runnable {
            binding.texttConnection.visibility = View.VISIBLE
            InitWebviewIndexFileState()
            isCallingStart = false
        }, Constants.timeForConnection)


        if (Utility.isNetworkAvailable(applicationContext)) {
            binding.texttConnection?.visibility = View.GONE
        } else {
            binding.texttConnection?.visibility = View.VISIBLE

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

        val colorAnimator =
            ObjectAnimator.ofInt(binding.texttConnection, "textColor", deepBlue, deepRed)
        colorAnimator.setEvaluator(ArgbEvaluator())
        colorAnimator.repeatCount = ValueAnimator.INFINITE
        colorAnimator.repeatMode = ValueAnimator.REVERSE
        colorAnimator.duration = 900
        colorAnimator.start()
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






    fun ApiCall(context: Context?, url: String?) {

        if (isCallingStart) {

            infotext!!.setText(R.string.connecting)
            progressBar!!.visibility = View.VISIBLE
            val queue = Volley.newRequestQueue(context)
            val stringRequest = StringRequest(
                Request.Method.GET, url, { response ->
                    infotext!!.setText(R.string.initializing)
                    try {
                        val jsonObject = JSONObject(response)
                        val remoteJson = jsonObject.getJSONObject("remoteConfig")
                        val homeurl = remoteJson.getString("homeUrl")

                        //BOTTOM BAR
                        constants.ShowBottomBar = remoteJson.getBoolean("ShowBottomBar")
                        constants.ChangeBottombarBgColor =
                            remoteJson.getBoolean("ChangeBottomBarBgColor")
                        constants.bottomBarBgColor =
                            remoteJson.getString("bottomBarBackgroundColor")

                        //Bottom Menu Actions
                        constants.bottomUrl1 = remoteJson.getString("bottom1")
                        constants.bottomUrl2 = remoteJson.getString("bottom2")
                        constants.bottomUrl3 = remoteJson.getString("bottom3")
                        constants.bottomUrl4 = remoteJson.getString("bottom4")
                        constants.bottomUrl5 = remoteJson.getString("bottom5")
                        constants.bottomUrl6 = remoteJson.getString("bottom6")

                        //Bottom Menu icons
                        constants.bottomBtn1ImgUrl = remoteJson.getString("bottom1_img_url")
                        constants.bottomBtn2ImgUrl = remoteJson.getString("bottom2_img_url")
                        constants.bottomBtn3ImgUrl = remoteJson.getString("bottom3_img_url")
                        constants.bottomBtn4ImgUrl = remoteJson.getString("bottom4_img_url")
                        constants.bottomBtn5ImgUrl = remoteJson.getString("bottom5_img_url")
                        constants.bottomBtn6ImgUrl = remoteJson.getString("bottom6_img_url")

                        //DRAWER MENU
                        constants.ChangeDrawerHeaderBgColor =
                            remoteJson.getBoolean("ChangeDrawerHeaderColor")
                        constants.ChangeHeaderTextColor =
                            remoteJson.getBoolean("ChangeDrawerHeaderTextColor")
                        constants.ShowDrawer = remoteJson.getBoolean("ShowDrawerMenu")
                        constants.drawerMenuBtnUrl = remoteJson.getString("DrawerMenuUrl")
                        constants.drawerMenuImgUrl = remoteJson.getString("DrawerMenuImgUrl")
                        constants.drawerMenuItem1ImgUrl = remoteJson.getString("DrawerMenuImg1Url")
                        constants.drawerMenuItem2ImgUrl = remoteJson.getString("DrawerMenuImg2Url")
                        constants.drawerMenuItem3ImgUrl = remoteJson.getString("DrawerMenuImg3Url")
                        constants.drawerMenuItem4ImgUrl = remoteJson.getString("DrawerMenuImg4Url")
                        constants.drawerMenuItem5ImgUrl = remoteJson.getString("DrawerMenuImg5Url")
                        constants.drawerMenuItem6ImgUrl = remoteJson.getString("DrawerMenuImg6Url")
                        constants.drawerMenuItem1Url = remoteJson.getString("DrawerMenuItem1Url")
                        constants.drawerMenuItem2Url = remoteJson.getString("DrawerMenuItem2Url")
                        constants.drawerMenuItem3Url = remoteJson.getString("DrawerMenuItem3Url")
                        constants.drawerMenuItem4Url = remoteJson.getString("DrawerMenuItem4Url")
                        constants.drawerMenuItem5Url = remoteJson.getString("DrawerMenuItem5Url")
                        constants.drawerMenuItem6Url = remoteJson.getString("DrawerMenuItem6Url")
                        constants.drawerMenuItem1Text = remoteJson.getString("DrawerMenuItem1Title")
                        constants.drawerMenuItem2Text = remoteJson.getString("DrawerMenuItem2Title")
                        constants.drawerMenuItem3Text = remoteJson.getString("DrawerMenuItem3Title")
                        constants.drawerMenuItem4Text = remoteJson.getString("DrawerMenuItem4Title")
                        constants.drawerMenuItem5Text = remoteJson.getString("DrawerMenuItem5Title")
                        constants.drawerMenuItem6Text = remoteJson.getString("DrawerMenuItem6Title")
                        constants.drawerHeaderImgUrl = remoteJson.getString("DrawerHeaderImgUrl")
                        constants.drawerHeaderText = remoteJson.getString("DrawerHeaderText")
                        constants.drawerHeaderImgCommand =
                            remoteJson.getString("DrawerHeaderImgCommand")
                        constants.drawerHeaderBgColor = remoteJson.getString("DrawerHeaderBgColor")
                        constants.drawerHeaderTextColor =
                            remoteJson.getString("DrawerHeaderTextColor")


                        //TOOLBAR
                        constants.ShowToolbar = remoteJson.getBoolean("ShowToolbar")
                        constants.ToolbarTitleText = remoteJson.getString("ToolbarTitleText")
                        constants.ToolbarTitleTextColor =
                            remoteJson.getString("ToolbarTitleTextColor")
                        constants.ToolbarBgColor = remoteJson.getString("ToolbarBgColor")
                        constants.ChangeToolbarBgColor =
                            remoteJson.getBoolean("ChangeToolbarBgColor")
                        constants.ChangeTittleTextColor =
                            remoteJson.getBoolean("ChangeToolbarTitleTextColor")


                        //FLOATING BUTTON
                        constants.Web_button_link = remoteJson.getString("webBtnUrl")
                        constants.Web_button_Img_link = remoteJson.getString("webBtnImgUrl")
                       // constants.ShowWebBtn = remoteJson.getBoolean("ShowWebBtn")


                        //ADS
                        constants.ShowAdmobBanner = remoteJson.getBoolean("admobBanner")
                        constants.ShowAdmobInterstitial = remoteJson.getBoolean("admobInter")

                        //Notifications
                        constants.OnesigID = remoteJson.getString("onesigID")
                        constants.splashUrl = remoteJson.getString("splashUrl")
                        constants.Notifx_service = remoteJson.getBoolean("NotifXService")

                        //MORE
                        constants.ShowServerUrlSetUp =
                            remoteJson.getBoolean("AllowChangingServerUrl")
                        constants.AllowOnlyHostUrlInApp = remoteJson.getBoolean("allowOnlyHostUrl")


                        //App Update
                        constants.UpdateAvailable = remoteJson.getBoolean("UpdateAvailable")
                        constants.ForceUpdate = remoteJson.getBoolean("ForceUpdate")
                        constants.UpdateTitle = remoteJson.getString("Updatetitle")
                        constants.UpdateMessage = remoteJson.getString("UpdateMsg")
                        constants.UpdateUrl = remoteJson.getString("UpdateUrl")
                        constants.NewVersion = remoteJson.getString("NewVersion")

                        //                            WELCOME SCREEN
                        constants.EnableWelcomeSlider = remoteJson.getBoolean("AllowWelcomeSlider")

                        //screen title texts
                        constants.screen1TitleText = remoteJson.getString("Screen1Title")
                        constants.screen2TitleText = remoteJson.getString("Screen2Title")
                        constants.screen3TitleText = remoteJson.getString("Screen3Title")
                        constants.screen4TitleText = remoteJson.getString("Screen4Title")

                        //screen desc texts
                        constants.screen1Desc = remoteJson.getString("screen1Desc")
                        constants.screen2Desc = remoteJson.getString("screen2Desc")
                        constants.screen3Desc = remoteJson.getString("screen3Desc")
                        constants.screen4Desc = remoteJson.getString("screen4Desc")

                        //screen BG colors
                        constants.screen1BgColor = remoteJson.getString("Screen1bgColor")
                        constants.screen2BgColor = remoteJson.getString("Screen2bgColor")
                        constants.screen3BgColor = remoteJson.getString("Screen3bgColor")
                        constants.screen4BgColor = remoteJson.getString("Screen4bgColor")

                        //screen Text colors
                        constants.screen1TextColor = remoteJson.getString("Screen1TxtColor")
                        constants.screen2TextColor = remoteJson.getString("Screen2TxtColor")
                        constants.screen3TextColor = remoteJson.getString("Screen3TxtColor")
                        constants.screen4TextColor = remoteJson.getString("Screen4TxtColor")

                        //screen Text colors
                        constants.screen1Img = remoteJson.getString("Screen1ImgUrl")
                        constants.screen2Img = remoteJson.getString("Screen2ImgUrl")
                        constants.screen3Img = remoteJson.getString("Screen3ImgUrl")
                        constants.screen4Img = remoteJson.getString("Screen4ImgUrl")

                        isJsonAPICallReady = true

                        val sharedBiometric = getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)
                        val get_INSTALL_TV_JSON_USER_CLICKED = sharedTVAPPModePreferences.getString(Constants.INSTALL_TV_JSON_USER_CLICKED, "").toString()
                        val getFirstMode = sharedTVAPPModePreferences.getString(Constants.installTVModeForFirstTime, "").toString()
                        val getTvMode = sharedBiometric.getString(Constants.CALL_RE_SYNC_MANGER, "").toString()


                        if (URLUtil.isValidUrl(homeurl)) {
                            constants.jsonUrl = homeurl


                            Log.d("PETER", "InitWebvIewloadStates: Splash K  The JSON_MAIN_URl $homeurl")

                            try {
                                val uri = URI(homeurl)
                                val domain = uri.host
                                constants.filterdomain = domain
                            } catch (e: URISyntaxException) {
                                e.printStackTrace()
                            }
                            if (constants.EnableWelcomeSlider) {
                                Log.d("InitWebvIewloadStates", "Slidder is enabled")
                                //  Toast.makeText(applicationContext, "Slidder is enabled", Toast.LENGTH_SHORT).show()
                                handler?.postDelayed(Runnable {

                                    if (get_INSTALL_TV_JSON_USER_CLICKED == Constants.INSTALL_TV_JSON_USER_CLICKED) {
                                        if (should_My_App_Use_TV_Mode) {
                                            // saving launch state
                                            val editText88 = sharedBiometric.edit()
                                            editText88.putString(Constants.get_Launching_State_Of_WebView, Constants.launch_WebView_Offline)
                                            editText88.apply()

                                            val editor = preferences.edit()
                                            editor.putBoolean(Constants.swiperefresh, false)
                                            editor.apply()

                                        } else {
                                            // saving launch state
                                            val editText88 = sharedBiometric.edit()
                                            editText88.putString(Constants.get_Launching_State_Of_WebView, Constants.launch_Default_WebView_url)
                                            editText88.apply()

                                            val editor = preferences.edit()
                                            editor.putBoolean(Constants.swiperefresh, true)
                                            editor.apply()

                                        }
                                    }

                                    val getInfoPageState = sharedBiometric.getString(
                                        Constants.FIRST_INFORMATION_PAGE_COMPLETED,
                                        ""
                                    ).toString()
                                    if (getInfoPageState == Constants.FIRST_INFORMATION_PAGE_COMPLETED) {
                                        val myactivity = Intent(applicationContext, WelcomeSliderKT::class.java)
                                        startActivity(myactivity)
                                        finish()

                                    } else {
                                        /////
                                        finish()
                                        startActivity(Intent(applicationContext, InformationActivity::class.java))

                                    }


                                }, 1500)

                            } else {
                                handler?.postDelayed(Runnable {
                                    Log.d(
                                        "InitWebvIewloadStates",
                                        "SplashScreen: Slidder Not Not enabled "
                                    )
                                    ///  if (should_My_App_Use_TV_Mode && !getFirstMode.equals(Constants.installTVModeForFirstTime)){
                                    //  Toast.makeText(applicationContext, "Slidder Not Not enabled", Toast.LENGTH_SHORT).show()
                                    if (get_INSTALL_TV_JSON_USER_CLICKED == Constants.INSTALL_TV_JSON_USER_CLICKED) {
                                        if (should_My_App_Use_TV_Mode) {
                                            // saving launch state
                                            val editText88 = sharedBiometric.edit()
                                            editText88.putString(Constants.get_Launching_State_Of_WebView, Constants.launch_WebView_Offline)
                                            editText88.putString(Constants.PROTECT_PASSWORD, Constants.PROTECT_PASSWORD)
                                            editText88.apply()

                                            Log.d(
                                                "InitWebvIewloadStates",
                                                "SplashScreen: launch_WebView_Offline"
                                            )

                                            val getInfoPageState = sharedBiometric.getString(Constants.FIRST_INFORMATION_PAGE_COMPLETED, "").toString()
                                            if (getInfoPageState == Constants.FIRST_INFORMATION_PAGE_COMPLETED) {

                                                if (!getFirstMode.equals(Constants.installTVModeForFirstTime)) {
                                                    val myactivity = Intent(applicationContext, ReSyncActivity::class.java)
                                                    myactivity.putExtra("url", constants.jsonUrl)
                                                    startActivity(myactivity)
                                                    finish()
                                                } else {
                                                    val myactivity = Intent(
                                                        applicationContext,
                                                        WebViewPage::class.java
                                                    )
                                                    myactivity.putExtra("url", constants.jsonUrl)
                                                    startActivity(myactivity)
                                                    finish()

                                                }

                                            } else {

                                                /////
                                                startActivity(
                                                    Intent(
                                                        applicationContext,
                                                        InformationActivity::class.java
                                                    )
                                                )
                                                finish()

                                            }


                                        } else {

                                            // saving launch state
                                            val editText88 = sharedBiometric.edit()
                                            editText88.putString(
                                                Constants.get_Launching_State_Of_WebView,
                                                Constants.launch_Default_WebView_url
                                            )
                                            editText88.remove(Constants.PROTECT_PASSWORD)
                                            editText88.apply()

                                            Log.d(
                                                "InitWebvIewloadStates",
                                                "SplashScreen: launch_Default_WebView_url"
                                            )


                                            val getInfoPageState = sharedBiometric.getString(
                                                Constants.FIRST_INFORMATION_PAGE_COMPLETED,
                                                ""
                                            ).toString()
                                            if (getInfoPageState == Constants.FIRST_INFORMATION_PAGE_COMPLETED) {

                                                val myactivity = Intent(
                                                    applicationContext,
                                                    WebViewPage::class.java
                                                )
                                                myactivity.putExtra("url", constants.jsonUrl)
                                                startActivity(myactivity)
                                                finish()


                                            } else {

                                                /////
                                                startActivity(
                                                    Intent(
                                                        applicationContext,
                                                        InformationActivity::class.java
                                                    )
                                                )
                                                finish()

                                            }


                                        }

                                    } else {

                                        val getInfoPageState = sharedBiometric.getString(
                                            Constants.FIRST_INFORMATION_PAGE_COMPLETED,
                                            ""
                                        ).toString()
                                        if (getInfoPageState == Constants.FIRST_INFORMATION_PAGE_COMPLETED) {

                                            if (getTvMode == Constants.CALL_RE_SYNC_MANGER) {
                                                val myactivity = Intent(
                                                    applicationContext,
                                                    ReSyncActivity::class.java
                                                )
                                                myactivity.putExtra("url", constants.jsonUrl)
                                                startActivity(myactivity)
                                                finish()

                                            } else {
                                                // Intent myactivity = new Intent(Splash.this, WebActivity.class);
                                                val myactivity = Intent(
                                                    applicationContext,
                                                    WebViewPage::class.java
                                                )
                                                myactivity.putExtra("url", constants.jsonUrl)
                                                startActivity(myactivity)
                                                finish()

                                            }

                                        } else {

                                            /////
                                            startActivity(
                                                Intent(
                                                    applicationContext,
                                                    InformationActivity::class.java
                                                )
                                            )
                                            finish()

                                        }

                                    }

                                }, 1500)
                            }

                        } else {
                            infotext!!.setText(R.string.invalide_remote_data)
                            progressBar!!.visibility = View.GONE
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
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        infotext!!.text = e.message
                        manageUIStateOnNetworkIssuesForRetry()
                    }
                }) { error ->
                infotext!!.text = "Error occurred! =$error"
                manageUIStateOnNetworkIssuesForRetry()
                isJsonAPICallReady = false
                progressBar!!.visibility = View.GONE

            }


// Add the request to the RequestQueue.
            queue.add(stringRequest)
            queue.addRequestFinishedListener<Any> { queue.cache.clear() }
        } else {
            showToastMessage("Slow internet connection")
        }
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
                ApiCall(applicationContext, ServerUrl)
            }
            manageUIStateOnNetworkIssues()
        }
    }


    private fun manageUIStateOnNetworkIssues(){
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

    private fun manageUIStateOnNetworkIssuesForRetry(){
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


        // TextView textDescription = binding.textDescription;

        //  textDescription.setText(message);
        alertDialog.show()
    }


    override fun onResume() {
        super.onResume()
        //  connectivityReceiver = ConnectivityReceiver()
        //  val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        //  registerReceiver(connectivityReceiver, intentFilter)

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

    inner class ConnectivityReceiver : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            try {
                val connectivityManager =
                    context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                    try {
                        isCallingStart = true

                        val SPLASH_TIME_OUT = 1300
                        Handler().postDelayed({
                            try {


                                fetchApiSettings()

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
                } else {

                    isCallingStart = false

                    // No internet Connection
                    try {
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

                // No internet Connection
            } catch (ignored: java.lang.Exception) {
            }
        }
    }

    @SuppressLint("SetTextI18n", "CommitPrefEdits")
    private fun fetchApiSettings() {

        if (isCallingStart) {

            CoroutineScope(Dispatchers.IO).launch {
                try {

                    val simpleSavedPassword =
                        getSharedPreferences(Constants.SIMPLE_SAVED_PASSWORD, MODE_PRIVATE)

                    val get_tMaster =
                        simpleSavedPassword.getString(Constants.get_editTextMaster, "").toString()
                    val get_UserID =
                        simpleSavedPassword.getString(Constants.get_UserID, "").toString()
                    val get_LicenseKey =
                        simpleSavedPassword.getString(Constants.get_LicenseKey, "").toString()

                    val path = "$get_UserID/$get_LicenseKey/${Constants.END_PATH_OF_TV_MODE_URL}"

                    val apiService = RetrofitInstanceTVMode.createApiService(get_tMaster)
                    val response = apiService.getAppConfig(path)

                    if (response.isSuccessful) {
                        val settings = response.body()?.InstallAppSettings
                        withContext(Dispatchers.Main) {
                            settings?.let {
                                // Extracting individual values
                                val installTVMode = it.install_TV_mode
                                val hideTvModeLabel = it.hide_TV_mode_label
                                val fullScreen = it.full_Screen
                                val hideFullScreenLabel = it.hide_Full_Screen_Label
                                val immersiveMode = it.immersive_Mode
                                val hideImmersiveModeLabel = it.hide_Immersive_Mode_Label
                                val hideBottomBar = it.hide_Bottom_Bar
                                val hideBottomBarLabel = it.hide_Bottom_Bar_Label
                                val hideBottomMenuIcon = it.hide_Bottom_Menu_Icon
                                val hideBottomMenuIconLabel = it.hide_Bottom_Menu_Icon_Label
                                val hideFloatingButton = it.hide_Floating_Button
                                val hideFloatingButtonLabel = it.hide_Floating_Button_Label

                                val use_local_schedule = it.use_local_schedule

                                val show_local_schedule_label = it.show_local_schedule_label


                                Log.d("USE_DAVID", "fetchApiSettings: $use_local_schedule")


                                // Logging the values
                                //Log.d("ApiResponse", "Install TV Mode: $installTVMode")

                                val editor = sharedTVAPPModePreferences.edit()
                                editor.putBoolean(Constants.installTVMode, installTVMode)
                                editor.putBoolean(Constants.hide_TV_Mode_Label, hideTvModeLabel)
                                editor.putBoolean(Constants.fullScreen_APP, fullScreen)
                                editor.putBoolean(Constants.hide_Full_ScreenLabel, hideFullScreenLabel)
                                editor.putBoolean(Constants.immersive_Mode_APP, immersiveMode)
                                editor.putBoolean(Constants.hide_Immersive_ModeLabel, hideImmersiveModeLabel)
                                editor.putBoolean(Constants.hide_BottomBar_APP, hideBottomBar)
                                editor.putBoolean(Constants.hide_Bottom_Bar_Label_APP, hideBottomBarLabel)
                                editor.putBoolean(Constants.hideBottom_MenuIcon_APP, hideBottomMenuIcon)
                                editor.putBoolean(Constants.hide_Bottom_MenuIconLabel_APP, hideBottomMenuIconLabel)
                                editor.putBoolean(Constants.hide_Floating_Button_APP, hideFloatingButton)
                                editor.putBoolean(Constants.hide_Floating_ButtonLabel_APP, hideFloatingButtonLabel)

                                // newly added
                                editor.putBoolean(Constants.use_local_schedule_APP, use_local_schedule)
                                editor.putBoolean(Constants.show_local_schedule_label, show_local_schedule_label)
                                editor.apply()


                                if (installTVMode) {
                                    should_My_App_Use_TV_Mode = true
                                    val editorrr = sharedBiometric.edit()
                                    editorrr.putString(Constants.MY_TV_OR_APP_MODE, Constants.TV_Mode)
                                    editorrr.apply()
                                } else {
                                    val editorrr = sharedBiometric.edit()
                                    editorrr.putString(Constants.MY_TV_OR_APP_MODE, Constants.App)
                                    editorrr.apply()
                                }

                                // use Paper Book to Save Use online CSv or Local CSv
                                if (use_local_schedule) {
                                    // se to use local schedule if true
                                    Paper.book()
                                        .write(Common.set_schedule_key, Common.schedule_offline)
                                } else {
                                    // se to use online  schedule if false
                                    Paper.book()
                                        .write(Common.set_schedule_key, Common.schedule_online)
                                }


                                isTvModeSettingsReady = true

                                if (!isJsonAPICallReady) {
                                    ApiCall(applicationContext, ServerUrl)
                                }
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            // Handle error (e.g., show a toast)
                            Log.e("ApiResponse", "Error: ${response.message()}")
                            isTvModeSettingsReady = false
                            infotext?.text = "Error: Unable to fetch TV or App Mode Settings"
                            Toast.makeText(
                                applicationContext,
                                "Error: Unable to fetch TV or App Mode Settings",
                                Toast.LENGTH_SHORT
                            ).show()
                            manageUIStateOnNetworkIssuesForRetry()
                        }
                    }
                } catch (e: HttpException) {
                    withContext(Dispatchers.Main) {
                        // Handle HTTP exception (e.g., show a toast)
                        Log.e("ApiResponse", "HTTP Exception: ${e.message}")
                        isTvModeSettingsReady = false
                        manageUIStateOnNetworkIssuesForRetry()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        // Handle general exception (e.g., show a toast)
                        Log.e("ApiResponse", "Error: ${e.message}")
                        isTvModeSettingsReady = false
                        infotext?.text = "Error: ${e.message}"
                        manageUIStateOnNetworkIssuesForRetry()

                    }
                }
            }
        } else {
            showToastMessage("Slow internet connection")
        }
    }


    @SuppressLint("SourceLockedOrientationActivity")
    private fun applyOritenation() {

        // make screen to be full screen
        //  window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        // stop all service

        Utility.hideSystemBars(window)


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
                        showToastMessage("You need to Sync Files for Offline Usage")
                        null
                    }
                }

                // Now back on the main thread to update the UI
                if (filePath != null) {

                    Log.d("MAMMA", "No internet casll Screen  File !=null")
                    if (!isCallingStart && isMyActivityRunning) {

                        Log.d("MAMMA", "My activoitu is runing")

                        val sharedBiometric: SharedPreferences = applicationContext.getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)
                        val get_TV_or_App_Mode = sharedBiometric.getString(Constants.MY_TV_OR_APP_MODE, "").toString()
                        val JSON_MAIN_URL = sharedBiometric.getString(Constants.JSON_MAIN_URL, "").toString()

                        Log.d("MAMMA", "App_State  :::$get_TV_or_App_Mode")

                        if (get_TV_or_App_Mode == Constants.TV_Mode) {
                            val editText88 = sharedBiometric.edit()
                            editText88.putString(Constants.get_Launching_State_Of_WebView, Constants.launch_WebView_Offline)
                            editText88.apply()

                            val myActivity = Intent(applicationContext, WebViewPage::class.java)
                            myActivity.putExtra(Constants.USE_TEMP_OFFLINE_WEB_VIEW_PAGE, Constants.USE_TEMP_OFFLINE_WEB_VIEW_PAGE)
                            startActivity(myActivity)
                            finish()
                            Log.d("MAMMA", "TV: Splash Screen")

                        } else {

                            Log.d("MAMMA", "Appp: TV_MODE_")

                            val editText88 = sharedBiometric.edit()
                            editText88.putString(Constants.get_Launching_State_Of_WebView, Constants.launch_Default_WebView_url)
                            editText88.apply()

                            val myActivity = Intent(applicationContext, WebViewPage::class.java)
                            myActivity.putExtra(Constants.USE_TEMP_OFFLINE_WEB_VIEW_PAGE, Constants.USE_TEMP_OFFLINE_WEB_VIEW_PAGE)

                            val urlPath = "${Constants.CUSTOM_CP_SERVER_DOMAIN}/$CLO/$DEMO/App/$fileName"

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
                    showToastMessage("You need to Sync Files for Offline Usage")

                    Log.d("MAMMA", "No files: Splash Screen")
                }


            } catch (e: Exception) {
                showToastMessage("You need to Sync Files for Offline Usage")
            }
        }
    }


    private fun getFilePath(CLO: String, DEMO: String, filename: String): String? {
        val baseDir = getExternalFilesDir(null)  // ✅ App-private scoped external storage
        val relativePath = "Syn2AppLive/$CLO/$DEMO/${Constants.App}"
        val destinationFolder = File(baseDir, relativePath)
        val myFile = File(destinationFolder, filename)

        return if (myFile.exists()) {
            myFile.toURI().toString()  // Use proper file URI (e.g. file:///...)
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