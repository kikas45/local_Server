package sync2app.com.syncapplive

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.SurfaceTexture
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.hardware.camera2.CameraManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.media.MediaPlayer
import android.media.MediaScannerConnection
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.preference.PreferenceManager
import android.provider.Settings
import android.text.Editable
import android.text.Html
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.RatingBar.OnRatingBarChangeListener
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.opencsv.CSVReader
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.Fetch.Impl.getInstance
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2.FetchListener
import com.tonyodev.fetch2.HttpUrlConnectionDownloader
import com.tonyodev.fetch2.NetworkType
import com.tonyodev.fetch2.Priority
import com.tonyodev.fetch2.Request
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.Downloader.FileDownloaderType
import io.paperdb.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import retrofit2.HttpException
import sync2app.com.syncapplive.QrPages.QRSanActivity
import sync2app.com.syncapplive.additionalSettings.InformationActivity
import sync2app.com.syncapplive.additionalSettings.MainHelpers.GMailSender
import sync2app.com.syncapplive.additionalSettings.MaintenanceActivity
import sync2app.com.syncapplive.additionalSettings.OnFileChange.Retro_On_Change
import sync2app.com.syncapplive.additionalSettings.PasswordActivity
import sync2app.com.syncapplive.additionalSettings.ReSyncActivity
import sync2app.com.syncapplive.additionalSettings.SplashVideoActivity
import sync2app.com.syncapplive.additionalSettings.autostartAppOncrash.Methods
import sync2app.com.syncapplive.additionalSettings.cloudAppsync.api.RetrofitClient
import sync2app.com.syncapplive.additionalSettings.cloudAppsync.models.AppSettings
import sync2app.com.syncapplive.additionalSettings.cloudAppsync.models.Schedule
import sync2app.com.syncapplive.additionalSettings.cloudAppsync.util.Common
import sync2app.com.syncapplive.additionalSettings.cloudAppsync.util.MethodsSchedule
import sync2app.com.syncapplive.additionalSettings.myApiDownload.FilesApi
import sync2app.com.syncapplive.additionalSettings.myApiDownload.FilesViewModel
import sync2app.com.syncapplive.additionalSettings.myCompleteDownload.DnApi
import sync2app.com.syncapplive.additionalSettings.myCompleteDownload.DnViewModel
import sync2app.com.syncapplive.additionalSettings.myFailedDownloadfiles.DnFailedApi
import sync2app.com.syncapplive.additionalSettings.myFailedDownloadfiles.DnFailedViewModel
import sync2app.com.syncapplive.additionalSettings.myParsingDownloadDataBase.ParsingApi
import sync2app.com.syncapplive.additionalSettings.myParsingDownloadDataBase.ParsingViewModel
import sync2app.com.syncapplive.additionalSettings.savedDownloadHistory.scanutil.CustomShortcutsDemo
import sync2app.com.syncapplive.additionalSettings.urlchecks.checkUrlExistence
import sync2app.com.syncapplive.additionalSettings.usdbCamera.MyUsb.CameraHandlerKT
import sync2app.com.syncapplive.additionalSettings.usdbCamera.MyUsb.kotlionCode.AudioHandlerKT
import sync2app.com.syncapplive.additionalSettings.utils.CSVDownloader
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.additionalSettings.utils.FileChecker
import sync2app.com.syncapplive.additionalSettings.utils.IndexFileChecker
import sync2app.com.syncapplive.additionalSettings.utils.Utility
import sync2app.com.syncapplive.constants.isAppOpen
import sync2app.com.syncapplive.constants.jsonUrl
import sync2app.com.syncapplive.databinding.ActivityWebViewPageBinding
import sync2app.com.syncapplive.databinding.CustomContactAdminBinding
import sync2app.com.syncapplive.databinding.CustomEmailSucessLayoutBinding
import sync2app.com.syncapplive.databinding.CustomFailedLayoutBinding
import sync2app.com.syncapplive.databinding.CustomForgetPasswordEmailLayoutBinding
import sync2app.com.syncapplive.databinding.CustomLayoutWebInternetBinding
import sync2app.com.syncapplive.databinding.CustomNotificationLayoutBinding
import sync2app.com.syncapplive.databinding.CustomOfflinePopLayoutBinding
import sync2app.com.syncapplive.databinding.CustomRedirectEmailLayoutBinding
import sync2app.com.syncapplive.databinding.CustomWebviewpagePasswordLayoutBinding
import sync2app.com.syncapplive.databinding.ProgressDialogLayoutBinding
import sync2app.com.syncapplive.glidetovectoryou.GlideToVectorYou
import sync2app.com.syncapplive.glidetovectoryou.GlideToVectorYouListener
import sync2app.com.syncapplive.myService.ParsingSyncService
import sync2app.com.syncapplive.myService.RetryParsingSyncService
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.io.InputStream
import java.security.SecureRandom
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Objects
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


class WebViewPage : AppCompatActivity() {
    private lateinit var binding: ActivityWebViewPageBinding

    //dynamic values
    private var currentSettings: AppSettings? = null


    private var isSchedule = false
    private var isScheduleRunning = false


    // check if API or ZIP
    /// private var isAPISyncRunning = false


    // check if API or ZIP
    private var isZipSyncEnabled = false
    private var isApiSyncEnabled = false

    //check if parsing enabled
    private var isParsingEnable = false

    // for sync on interval
    private var syncOnIntervalsAllowed = false


    //data
    private val tempList: MutableList<Schedule> = mutableListOf()
    private val theSchedules: MutableList<Schedule> = mutableListOf()
    private val setAlarms: MutableList<Schedule> = mutableListOf()
    private val enteredSchedules: MutableList<String> = mutableListOf()
    private val enteredAlarms: MutableList<String> = mutableListOf()

    //data
    private var scheduleFile: File? = null


    private var getServer_timeStamp = ""
    var isScheduleCurrentlyRunning = false
    private val currentScheduleTime = ""

    //handler
    private var handlerSchedule = Handler(Looper.getMainLooper())
    private var handlerRunningSchedule = Handler(Looper.getMainLooper())
    private var handlerDeviceTime = Handler(Looper.getMainLooper())
    private var handlerServerTime = Handler(Looper.getMainLooper())
    private var runnableSchedule: Runnable? = null
    private var runnableRunningSchedule: Runnable? = null
    private var runnableServerTime: Runnable? = null
    private var runnableDeviceTime: Runnable? = null


    private var deviceTime: TextView? = null
    private var serverTime: TextView? = null
    private var scheduleEnd: TextView? = null
    private var scheduleStart: TextView? = null


    /// for schedule media
    // val FILECHOOSER_RESULTCODE = 5173
    private val TAG = "WebViewPage"
    // private val FCR = 1
    //  var mUploadMessage: ValueCallback<Uri>? = null

    //Adjusting Rating bar popup timeframe
    // private val DAYS_UNTIL_PROMPT = 0 //Min number of days

    private val LAUNCHES_UNTIL_PROMPT = 5 //Min number of app launches

    // var openblobPdfafterDownload = true

    var ChangeListener = false
    // var storagecamrequest = false

    var errorlayout: LinearLayout? = null
    // var mContext: Context? = null


    private var countdownTimer_Api_Sync: CountDownTimer? = null
    private var countdownTimer_Short_Cut: CountDownTimer? = null
    private var countdownTimer_App_Refresh: CountDownTimer? = null

    private var connectivityReceiver: ConnectivityReceiver? = null

    private var alertDialog: AlertDialog? = null

    private var myHandler: Handler? = null

    private var webView: WebView? = null

    private var drawer_menu: RelativeLayout? = null


    @RequiresApi(Build.VERSION_CODES.Q)
    private var imgClk = View.OnClickListener { v ->
        val buttonClick = AlphaAnimation(0.1f, 0.4f)
        v.startAnimation(buttonClick)
        when (v.id) {
            R.id.bottomtoolbar_btn_1 -> HandleRemoteCommand(constants.bottomUrl1)
            R.id.bottomtoolbar_btn_2 -> HandleRemoteCommand(constants.bottomUrl2)
            R.id.bottomtoolbar_btn_3 -> HandleRemoteCommand(constants.bottomUrl3)
            R.id.bottomtoolbar_btn_4 -> HandleRemoteCommand(constants.bottomUrl4)
            R.id.bottomtoolbar_btn_5 -> HandleRemoteCommand(constants.bottomUrl5)
            R.id.bottomtoolbar_btn_6 -> HandleRemoteCommand(constants.bottomUrl6)
            R.id.drawer_menu_Btn -> HandleRemoteCommand(constants.drawerMenuBtnUrl)
            R.id.drawer_item_1 -> {
                HandleRemoteCommand(constants.drawerMenuItem1Url)
                ShowHideViews(drawer_menu!!)
            }

            R.id.drawer_item_2 -> {
                HandleRemoteCommand(constants.drawerMenuItem2Url)
                ShowHideViews(drawer_menu!!)
            }

            R.id.drawer_item_3 -> {
                HandleRemoteCommand(constants.drawerMenuItem3Url)
                ShowHideViews(drawer_menu!!)
            }

            R.id.drawer_item_4 -> {
                HandleRemoteCommand(constants.drawerMenuItem4Url)
                ShowHideViews(drawer_menu!!)
            }

            R.id.drawer_item_5 -> {
                HandleRemoteCommand(constants.drawerMenuItem5Url)
                ShowHideViews(drawer_menu!!)
            }

            R.id.drawer_item_6 -> {
                HandleRemoteCommand(constants.drawerMenuItem6Url)
                ShowHideViews(drawer_menu!!)
            }

            R.id.drawer_headerImg -> {
                HandleRemoteCommand(constants.drawerHeaderImgCommand)
                ShowHideViews(drawer_menu!!)
            }
        }
    }
    var urllayout: LinearLayout? = null

    //    Toolbar toolbar;
    //    Toolbar toolbar;
    var bottomToolBar: LinearLayout? = null
    var tbarprogress: ProgressBar? = null
    var HorizontalProgressBar: ProgressBar? = null
    var progressDialog: ProgressDialog? = null
    var horizontalProgressFramelayout: FrameLayout? = null
    var UrlIntent: Intent? = null
    var data: Uri? = null
    // var dX = 0f
    // var dY = 0f
    // var lastAction = 0
    //var currentDownloadFileName: String? = null
    //  var currentDownloadFileMimeType: String? = null

    //Progress
    var ShowHorizontalProgress = false

    var ShowToolbarProgress = false

    var ShowProgressDialogue = false

    var ShowSimpleProgressBar = true

    var ShowNativeLoadView = false

    var EnableSwipeRefresh = false

    //Ads
    // var ShowBannerAds = constants.ShowAdmobBanner


    var ShowInterstitialAd = constants.ShowAdmobInterstitial

    // var ShowOptionMenu = false
    var ShowToolbar = constants.ShowToolbar

    var ShowDrawer = constants.ShowDrawer

    var ShowBottomBar = constants.ShowBottomBar

    // var ShowHideBottomBarOnScroll = false
    // var UseInappDownloader = false
    var AllowRating = false
    var ClearCacheOnExit = false
    var AskToExit = false
    var BlockAds = false
    var AllowGPSLocationAccess = false
    var RequestRunTimePermissions = false
    var LoadLastWebPageOnAccidentalExit = false

    // var OpenFileAfterDownload = true
    var AutoHideToolbar = false
    // var SupportMultiWindows = true

    var ShowWebButton = constants.ShowWebBtn

    //========================================
    //SET YOUR WEBSITE URL in constants class under Elements folder
    var MainUrl = constants.jsonUrl

    //BottomToolbar Image Buttons
    private var bottomToolbar_img_1: ImageView? = null
    private var bottomToolbar_img_2: ImageView? = null
    private var bottomToolbar_img_3: ImageView? = null
    private var bottomToolbar_img_4: ImageView? = null
    private var bottomToolbar_img_5: ImageView? = null
    private var bottomToolbar_img_6: ImageView? = null
    private var bottomtoolbar_btn_7: ImageView? = null
    private var imageWiFiOn: ImageView? = null
    private var imageWiFiOFF: ImageView? = null
    private var x_toolbar: RelativeLayout? = null
    private var bottom_server_layout: ConstraintLayout? = null
    private var drawer_menu_btn: ImageView? = null
    private var drawerItem1: LinearLayout? = null
    private var drawerItem2: LinearLayout? = null
    private var drawerItem3: LinearLayout? = null
    private var drawerItem4: LinearLayout? = null
    private var drawerItem5: LinearLayout? = null
    private var drawerItem6: LinearLayout? = null
    private var drawerItem7: LinearLayout? = null
    private var drawerImg1: ImageView? = null
    private var drawerImg2: ImageView? = null
    private var drawerImg3: ImageView? = null
    private var drawerImg4: ImageView? = null
    private var drawerImg5: ImageView? = null
    private var drawerImg6: ImageView? = null
    private var drawer_item_img_7: ImageView? = null
    private var drawerItemtext1: TextView? = null
    private var drawerItemtext2: TextView? = null
    private var drawerItemtext3: TextView? = null
    private var drawerItemtext4: TextView? = null
    private var drawerItemtext5: TextView? = null
    private var drawerItemtext6: TextView? = null
    private var drawer_item_text_7: TextView? = null
    private var drawer_header_img: ImageView? = null
    private var drawer_header_text: TextView? = null

    private var drawerHeaderBg: LinearLayout? = null


    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private var runnable: Runnable? = null
    private var toolbartitleText: TextView? = null

    private var textSyncMode: TextView? = null

    // TextView textSystemState;
    private var textSynIntervals: TextView? = null
    private var countDownTime: TextView? = null
    var textLocation: TextView? = null


    var textStatusProcess: TextView? = null
    private var textDownladByes: TextView? = null
    private var textFilecount: TextView? = null


    private var progressBarPref: ProgressBar? = null

    private var isdDownloadApi = true
    private var iswebViewRefreshingOnApiSync = true
    private var totalFiles = 0
    private var currentDownloadIndex = 0
    private var downloadedFilesCount = 0
    private var fetch: Fetch? = null


    private val fetchListener: FetchListener? = null


    private val mFilesViewModel by viewModels<FilesViewModel>()

    private val parsingViewModel by viewModels<ParsingViewModel>()
    private val dnFailedViewModel by viewModels<DnFailedViewModel>()
    private val dnViewModel by viewModels<DnViewModel>()
    //  private var processingJob: Job? = null


    private var filesToProcess = 0
    private val mutex = Mutex()


    private var processingJob: Job? = null


    private var initProgressParsingSyncFilesDownload = true


    private var KoloLog = "ParsingSyncService"



    //  private WebView mWebviewPop;
    //  private var mAdView: AdView? = null

    private var swipeView: SwipeRefreshLayout? = null
    private var urlEdittext: EditText? = null
    private var simpleProgressbar: ProgressBar? = null
    //  private val mCustomView: View? = null
    // private val mOriginalSystemUiVisibility = 0
    // private val mOriginalOrientation = 0
    // private val mCustomViewCallback: CustomViewCallback? = null



    //  private RelativeLayout windowContainer;
    private var windowProgressbar: ProgressBar? = null

    //  private RelativeLayout mContainer;
    // private val mCM: String? = null
    // private var mUM: ValueCallback<Uri>? = null
    // private var mUMA: ValueCallback<Array<Uri>>? = null
    private val mProgressDialog: ProgressDialog? = null
    private var prefs: SharedPreferences? = null
    private var mydialog: Dialog? = null
    private val ratingbar: RatingBar? = null
    private var lasturl: String? = null
    private var hasWebviewPageLoadedBefore = false
    private var web_button: ImageView? = null
    private var imageCirclGreenOnline: ImageView? = null
    private var imageCircleBlueOffline: ImageView? = null

    //  private ConstraintLayout webx_layout; /// change
    // private LinearLayout web_button_root_layout; /// change
    //  private ConstraintLayout webx_layout; /// change
    private var errorCode: TextView? = null
    private var errorautoConnect: TextView? = null


    private var errorReloadButton: ImageButton? = null
    private var errorLayout: LinearLayout? = null
    // private var powerManager: PowerManager? = null
    // private var wakeLock: PowerManager.WakeLock? = null

    private var mUserViewModel: FilesViewModel? = null

    // for webcam, Camera and Audio
    // private var textureView: TextureView? = null
    private var textNoCameraAvaliable: TextView? = null
    private var cameraHandler: CameraHandlerKT? = null
    private var audioHandler: AudioHandlerKT? = null

    private var expandWebcam: ImageView? = null
    private var closeWebCam: ImageView? = null
    private var reloadWebCam: ImageView? = null

    private var mlayout: ConstraintLayout? = null
    private var isShowToastDisplayed = false

    private var isHideToastDisplayed = false
    private var dXo = 0f
    private var dYo = 0f
    private var lastActionXY = 0
    private var initialWidth = 0

    private var initialHeight: Int = 0

    private var isSystemRunning = true


    private var mUSBCameraHeight = 200.0
    private var mUSBCameraWidth = 250.0
    private var mUSBCameraLeftMargin = 50.0

    private var mUSBCameraTopMargin = 0.0
    private var mScreenHeight = 0
    private var mScreenWidth = 0


    private val StartCameraHandler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private val showCameraIconhandler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }


    private var usbBroadcastReceiver: UsbBroadcastReceiver? = null
    private var CameraReceiver: CameraDisconnectedReceiver? = null


    private val myDownloadClass: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.MY_DOWNLOADER_CLASS, Context.MODE_PRIVATE
        )
    }


    private val sharedBiometric: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_BIOMETRIC, Context.MODE_PRIVATE
        )
    }

    private val sharedCamera: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_CAMERA_PREF, Context.MODE_PRIVATE
        )
    }

    private val myDownloadMangerClass: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.MY_DOWNLOADER_CLASS, Context.MODE_PRIVATE
        )
    }

    private val preferences: SharedPreferences by lazy {
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
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


    private var receiver: BroadcastReceiver? = null
    private lateinit var filter: IntentFilter

    // netowrk error layout
    private var errorlayoutExitButton: ImageButton? = null
    private var errorlayouHomeButton: ImageButton? = null
    private var ErrorReloadButton: ImageButton? = null

    private var customProgressDialog: Dialog? = null

    private var customInternetWebviewPage: Dialog? = null

    private var countdownTimerForWebviewPage: CountDownTimer? = null
    private var isCountDownDialogVisible = false
    private var isErrorLayoutShown = true


    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint(
        "SetJavaScriptEnabled",
        "AddJavascriptInterface",
        "JavascriptInterface",
        "ClickableViewAccessibility",
        "WakelockTimeout", "CutPasteId", "SourceLockedOrientationActivity",
        "UnspecifiedRegisterReceiverFlag"
    )
    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  setContentView(R.layout.activity_web_view_page)
        binding = ActivityWebViewPageBinding.inflate(layoutInflater)
        setContentView(binding.root)


        applyOritenation()
        //add exception

        //add exception
        Methods.addExceptionHandler(this)
        myStateChecker()

        mUserViewModel = ViewModelProvider(this).get(FilesViewModel::class.java)
        myHandler = Handler(Looper.getMainLooper())

        //  mContext = this@WebViewPage


        data = intent.data
        prefs = applicationContext.getSharedPreferences("apprater", 0)
        UrlIntent = intent

        windowProgressbar = findViewById(R.id.WindowProgressBar)
        bottomToolBar = findViewById(R.id.bottom_toolbar_container)
        progressDialog = ProgressDialog(this@WebViewPage)
        simpleProgressbar = findViewById(R.id.SimpleProgressBar)
        horizontalProgressFramelayout = findViewById(R.id.frameLayoutHorizontalProgress)
        x_toolbar = findViewById(R.id.x_toolbar)
        errorlayout = findViewById(R.id.errorLayout)
        errorCode = findViewById(R.id.errorinfo)
        errorautoConnect = findViewById(R.id.autoreconnect)
        errorReloadButton = findViewById(R.id.ErrorReloadButton)
        errorLayout = findViewById(R.id.errorLayout)
        drawer_menu = findViewById(R.id.native_drawer_menu)
        drawer_menu_btn = findViewById(R.id.drawer_menu_Btn)
        bottom_server_layout = findViewById(R.id.bottom_server_layout)

        //  mAdView = findViewById(R.id.adView)


        HorizontalProgressBar = findViewById(R.id.progressbar)
        webView = findViewById(R.id.webview)


        swipeView = findViewById(R.id.swipeLayout)
        urlEdittext = findViewById(R.id.urledittextbox)
        urllayout = findViewById(R.id.urllayoutroot)

        web_button = findViewById(R.id.web_button)

        // webx_layout = findViewById(R.id.webx_layout);

        // webx_layout = findViewById(R.id.webx_layout);
        bottomToolbar_img_1 = findViewById(R.id.bottomtoolbar_btn_1)
        bottomToolbar_img_2 = findViewById(R.id.bottomtoolbar_btn_2)
        bottomToolbar_img_3 = findViewById(R.id.bottomtoolbar_btn_3)
        bottomToolbar_img_4 = findViewById(R.id.bottomtoolbar_btn_4)
        bottomToolbar_img_5 = findViewById(R.id.bottomtoolbar_btn_5)
        bottomToolbar_img_6 = findViewById(R.id.bottomtoolbar_btn_6)
        bottomtoolbar_btn_7 = findViewById(R.id.bottomtoolbar_btn_7)
        imageCircleBlueOffline = findViewById(R.id.imageCircleBlueOffline)
        imageCirclGreenOnline = findViewById(R.id.imageCirclGreenOnline)



        drawerImg1 = findViewById(R.id.drawer_item_img_1)
        drawerImg2 = findViewById(R.id.drawer_item_img_2)
        drawerImg3 = findViewById(R.id.drawer_item_img_3)
        drawerImg4 = findViewById(R.id.drawer_item_img_4)
        drawerImg5 = findViewById(R.id.drawer_item_img_5)
        drawerImg6 = findViewById(R.id.drawer_item_img_6)
        drawer_item_img_7 = findViewById(R.id.drawer_item_img_7)


        drawerItemtext1 = findViewById(R.id.drawer_item_text_1)
        drawerItemtext2 = findViewById(R.id.drawer_item_text_2)
        drawerItemtext3 = findViewById(R.id.drawer_item_text_3)
        drawerItemtext4 = findViewById(R.id.drawer_item_text_4)
        drawerItemtext5 = findViewById(R.id.drawer_item_text_5)
        drawerItemtext6 = findViewById(R.id.drawer_item_text_6)
        drawer_item_text_7 = findViewById(R.id.drawer_item_text_7)

        drawer_header_img = findViewById(R.id.drawer_headerImg)
        drawer_header_text = findViewById(R.id.drawer_header_text)
        drawerHeaderBg = findViewById(R.id.drawerheaderBg)

        drawerItem1 = findViewById(R.id.drawer_item_1)
        drawerItem2 = findViewById(R.id.drawer_item_2)
        drawerItem3 = findViewById(R.id.drawer_item_3)
        drawerItem4 = findViewById(R.id.drawer_item_4)
        drawerItem5 = findViewById(R.id.drawer_item_5)
        drawerItem6 = findViewById(R.id.drawer_item_6)

        drawerItem7 = findViewById(R.id.drawer_item_7)


        errorlayoutExitButton = findViewById(R.id.errorlayoutExitButton)
        errorlayouHomeButton = findViewById(R.id.errorlayouHomeButton)
        ErrorReloadButton = findViewById(R.id.errorlayouHomeButton)



        toolbartitleText = findViewById(R.id.toolbarTitleText)
        textSyncMode = findViewById(R.id.textSyncMode)
        textSynIntervals = findViewById(R.id.textSynIntervals)
        countDownTime = findViewById(R.id.countDownTime)
        textLocation = findViewById(R.id.textLocation)
        textStatusProcess = findViewById(R.id.textStatusProcess)


        textDownladByes = findViewById(R.id.textDownladByes)
        textFilecount = findViewById(R.id.textFilecount)
        progressBarPref = findViewById(R.id.progressBarPref)


        imageWiFiOFF = findViewById(R.id.imageWiFiOFF)
        imageWiFiOn = findViewById(R.id.imageWiFiOn)


        // for schedule media


        // for schedule media
        deviceTime = findViewById(R.id.deviceTime)
        serverTime = findViewById(R.id.serverTime)
        scheduleEnd = findViewById(R.id.scheduleEnd)
        scheduleStart = findViewById(R.id.scheduleStart)


        ///for camera
        ///for camera
        ///  textureView = findViewById<TextureView>(R.id.textureView)
        val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        cameraHandler = CameraHandlerKT(applicationContext, cameraManager, binding.textureView)
        audioHandler = AudioHandlerKT(applicationContext)



        closeWebCam = findViewById(R.id.closeWebCam)
        expandWebcam = findViewById(R.id.expandWebcam)
        reloadWebCam = findViewById(R.id.reloadWebCam)
        mlayout = findViewById(R.id.mlayout)
        textNoCameraAvaliable = findViewById(R.id.textNoCameraAvaliable)


      //  CameraReceiver = CameraDisconnectedReceiver()
       // val filter33 = IntentFilter(Constants.SYNC_CAMERA_DISCONNECTED)
      //  registerReceiver(CameraReceiver, filter33)

        CameraReceiver = CameraDisconnectedReceiver()
        val filter33 = IntentFilter(Constants.SYNC_CAMERA_DISCONNECTED)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            registerReceiver(CameraReceiver, filter33, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(CameraReceiver, filter33)
        }



       // usbBroadcastReceiver = UsbBroadcastReceiver()
       // val filter444 = getIntentFilter()
       // registerReceiver(usbBroadcastReceiver, filter444)

        usbBroadcastReceiver = UsbBroadcastReceiver()
        val filter444 = getIntentFilter()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            registerReceiver(usbBroadcastReceiver, filter444, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(usbBroadcastReceiver, filter444)
        }




        ///end of init  camera

        // for parsing


        val filter = IntentFilter().apply { addAction(Constants.RECIVER_PROGRESS) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            registerReceiver(progressReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(progressReceiver, filter)
        }




        val filterPr = IntentFilter().apply { addAction(Constants.RECIVER_DOWNLOAD_BYTES_PROGRESS) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            registerReceiver(progressDownloadBytesReceiver, filterPr, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(progressDownloadBytesReceiver, filterPr)
        }


        val scroolToEnd = findViewById<ImageView>(R.id.scroolToEnd)
        val scroolToStart = findViewById<ImageView>(R.id.scroolTostart)
        val horizontalScrollView = findViewById<HorizontalScrollView>(R.id.horizontalScrollView2)

        scroolToEnd.setOnClickListener {
            val scrollAmount = (horizontalScrollView.getChildAt(0).width * 0.2).toInt()
            horizontalScrollView.post(object : Runnable {
                override fun run() {
                    horizontalScrollView.smoothScrollBy(scrollAmount, 0)
                }
            })
        }

        scroolToStart.setOnClickListener {
            val scrollAmount = (horizontalScrollView.getChildAt(0).width * 0.30).toInt()
            horizontalScrollView.post(object : Runnable {
                override fun run() {
                    horizontalScrollView.smoothScrollBy(-scrollAmount, 0)
                }
            })
        }


        bottomtoolbar_btn_7!!.setOnClickListener {
            ShowHideViews(drawer_menu!!)
        }

        drawerItem7!!.setOnClickListener {
            val intent = Intent(applicationContext, QRSanActivity::class.java)
            startActivity(intent)
            finish()
        }


        errorlayoutExitButton?.setOnClickListener {
            ExitOnError()
        }


        errorlayouHomeButton?.setOnClickListener {
            goHomeOnError()
        }

        errorlayouHomeButton?.setOnClickListener {
            webView.let {
                it?.reload()
            }
        }



        swipeView?.setEnabled(false)
        swipeView?.setRefreshing(false)



        handler.postDelayed(Runnable {
            if (isSystemRunning) {
                CheckUpdate()
            }
        }, 8000)


        // init web view
        InitializeWebSettings()


        InitializeRemoteData()
        InitiatePreferences()
        InitiateComponents()
        IntClikListnerOnWebView()


        // init fetch listener and API Sync
        Init_Fetch_Download_Lsitner()

        registerNotificationBroadCast()


        // This aree Required for Tv mode
        val get_AppMode = sharedBiometric.getString(Constants.MY_TV_OR_APP_MODE, "").toString()
        if (get_AppMode == Constants.TV_Mode) {

            // Init Api Sync Types
            initStartSyncServices()

            //init USB Camera
            iniliaze_Schedule_and_usbCamera()

            // init schedule
            initialize()

        }


        // init Api Sync or Zip Sync
        InitBoleanApiSync_OR_Zip()


        // init shortcut
        CheckShortCutImage()


        // init refresh Time
        val getTimeForRefresh = myDownloadClass.getLong(Constants.get_Refresh_Timer, 0)
        getTimeForRefresh.let { it1 ->
            if (it1 != 0L) {
                start_App_Refresh_Time(it1)
            }
        }


    }


    private fun InitWebviewIndexFileState() {
        Log.d("PETER", "InitWebvIewloadStates:: InitWebviewIndexFileState FOR A STATE")
        // get input paths to device storage
        val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
        val fil_CLO = myDownloadClass.getString(Constants.getFolderClo, "").toString()
        val fil_DEMO = myDownloadClass.getString(Constants.getFolderSubpath, "").toString()

        val filename = "/index.html"
        lifecycleScope.launch {
            loadIndexFileIfExist(fil_CLO, fil_DEMO, filename)
            Log.d("PETER", "InitWebvIewloadStates:: loadIndexFileIfExist ::: State ..")
        }


    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun loadIndexFileIfExist(
        CLO: String,
        DEMO: String,
        fileName: String
    ) {
        lifecycleScope.launch {
            if (isActive){
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
                handler.postDelayed(Runnable {
                    if (filePath != null) {
                        if (isSystemRunning) {
                            lifecycleScope.launch {
                                loadOffline_Saved_Path_Offline_Webview(CLO, DEMO, fileName)
                                Log.d(
                                    "PETER",
                                    " loadOffline_Saved_Path_Offline_Webview ::: State ..Powe;;"
                                )
                            }
                        }
                    } else {
                        if (isSystemRunning) {
                            Log.d("PETER", "loadOffline_Saved_Path_Offline_Webview ::: State ..")
                            showPopInternetForWebPage(Constants.UnableToFindIndex)
                        }
                    }

                }, 1500)


            } catch (e: Exception) {
                handler.postDelayed(Runnable {
                    if (isSystemRunning) {
                        showToastMessage("You need to Sync Files for Offline Usage")
                        simpleProgressbar!!.visibility = View.GONE
                        webView!!.loadUrl("about:blank")
                    }
                }, 1500)
            }
        }
        }
    }


    private fun InitBoleanApiSync_OR_Zip() {

        val getSyncMethods =
            sharedBiometric.getString(Constants.IMG_SELECTED_SYNC_METHOD, "").toString()
        val get_intervals =
            sharedBiometric.getString(Constants.imagSwtichEnableSyncOnFilecahnge, "").toString()

        if (getSyncMethods == Constants.USE_ZIP_SYNC) {
            isZipSyncEnabled = true

            val editorSyn = myDownloadClass.edit()
            editorSyn.remove(Constants.SynC_Status)
            editorSyn.apply()
        }



        if (getSyncMethods == Constants.USE_API_SYNC) {
            isApiSyncEnabled = true
        }



        if (getSyncMethods == Constants.USE_PARSING_SYNC) {
            isParsingEnable = true
        }


        syncOnIntervalsAllowed = get_intervals == Constants.imagSwtichEnableSyncOnFilecahnge


    }


    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    private fun InitializeWebSettings() {
        webView!!.settings.apply {

            loadsImagesAutomatically = true
            builtInZoomControls = true
            displayZoomControls = false

            loadWithOverviewMode = false
            useWideViewPort = false

            databaseEnabled = true
            domStorageEnabled = true
            setSupportZoom(false)

            setUserAgentString(userAgentString.replace("wv", ""))
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.acceptThirdPartyCookies(webView!!)

            javaScriptEnabled = true

            allowFileAccess = true
            allowContentAccess = true

            setSupportMultipleWindows(true)
            javaScriptCanOpenWindowsAutomatically = true

            cacheMode = WebSettings.LOAD_NO_CACHE
            webView!!.isSaveEnabled = true

            mediaPlaybackRequiresUserGesture = false

            layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING


            WebView.setWebContentsDebuggingEnabled(true)

            val get_imgSetUserAgent =
                sharedBiometric.getString(Constants.ENABLE_USER_AGENT, "").toString()

            if (get_imgSetUserAgent.equals(Constants.ENABLE_USER_AGENT)) {
                setWebViewToDesktop(webView!!)
            } else {
                setWebViewToMobile(webView!!)
            }

            // init webview load state
            InitWebvIewloadStates()

        }

    }


    private fun setWebViewToMobile(webView: WebView) {
        //  showToastMessage("Use Mobile Agent")
        webView.settings.userAgentString =
            "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.101 Mobile Safari/537.36"

    }

    private fun setWebViewToDesktop(webView: WebView) {
        // showToastMessage("Use Desktop Agent")
        webView.settings.userAgentString =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.101 Safari/537.36"
    }




    private lateinit var receiverNotify: BroadcastReceiver
    private lateinit var filterNotify: IntentFilter

    private fun registerNotificationBroadCast() {
        if (constants.Notifx_service) {
            isAppOpen = true

            // Start your service
            startService(Intent(this, RemotexNotifierKT::class.java))

            // Prepare filter for the custom broadcast
            filterNotify = IntentFilter("notifx_ready")

            // Create the receiver
            receiverNotify = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    try {
                        if (!constants.Notif_Shown) {
                            showNotifxDialog(this@WebViewPage)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            // Register receiver in a version-safe way
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                applicationContext.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                applicationContext.registerReceiver(receiver, filter)
            }

        } else {
            stopService(Intent(this, RemotexNotifierKT::class.java))
        }
    }



//    @SuppressLint("UnspecifiedRegisterReceiverFlag")
//    private fun registerNotificationBroadCast() {
//
//        if (constants.Notifx_service) {
//            isAppOpen = true
//
//            startService(Intent(this, RemotexNotifierKT::class.java))
//
//            filter = IntentFilter("notifx_ready")
//
//            receiver = object : BroadcastReceiver() {
//                override fun onReceive(context: Context, intent: Intent) {
//                    try {
//                        if (!constants.Notif_Shown) {
//                            showNotifxDialog(this@WebViewPage)
//                        }
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//            }
//            applicationContext.registerReceiver(receiver, filter)
//        } else {
//            stopService(Intent(this, RemotexNotifierKT::class.java))
//        }
//    }

    private fun InitWebvIewloadStates() {
        val sharedBiometric = getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)
        val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
        val get_launching_state =
            sharedBiometric.getString(Constants.get_Launching_State_Of_WebView, "").toString()
        val fil_CLO = myDownloadClass.getString(Constants.getFolderClo, "").toString()
        val fil_DEMO = myDownloadClass.getString(Constants.getFolderSubpath, "").toString()

        Log.d("PETER", "InitWebvIewloadStates: $get_launching_state")

        if (get_launching_state.equals(Constants.launch_WebView_Online)) {

            if (Utility.isNetworkAvailable(applicationContext)) {

                load_live_Parther_url_Format()
            } else {
                checkPathForFilesWhenOffline()
            }


        } else {

            if (get_launching_state.equals(Constants.launch_WebView_Offline)) {
                val filename = "/index.html"
                lifecycleScope.launch {
                    loadOffline_Saved_Path_Offline_Webview(fil_CLO, fil_DEMO, filename)
                }


            } else if (get_launching_state.equals(Constants.launch_WebView_Offline_Manual_Index)) {

                val filename = "/index.html"
                lifecycleScope.launch {
                    loadOffline_Saved_Path_Offline_Webview(fil_CLO, fil_DEMO, filename)
                }

            } else if (get_launching_state.equals(Constants.launch_WebView_Online_Manual_Index)) {

                if (Utility.isNetworkAvailable(applicationContext)) {
                    val getSaved_manaul_index_edit_url_Input = myDownloadClass.getString(Constants.getSaved_manaul_index_edit_url_Input, "").toString()
                    loadOnlineLiveUrl(getSaved_manaul_index_edit_url_Input)
                    load_live_indicator()

                } else {
                    checkPathForFilesWhenOffline()
                }


            } else if (get_launching_state.equals(Constants.launch_Default_WebView_url)) {

                if (Utility.isNetworkAvailable(applicationContext)) {
                    loadOnlineUrl()
                } else {
                    checkPathForFilesWhenOffline()
                }


            } else {


                if (Utility.isNetworkAvailable(applicationContext)) {
                    loadOnlineUrl()

                } else {
                    checkPathForFilesWhenOffline()
                }

            }
        }

    }



    private fun InitWebvIewloadStatesWhenPopUpIsOn() {
        val sharedBiometric = getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)
        val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
        val get_launching_state = sharedBiometric.getString(Constants.get_Launching_State_Of_WebView, "").toString()

        if (get_launching_state.equals(Constants.launch_WebView_Online)) {
            load_live_Parther_url_Format()
        } else {

             if (get_launching_state.equals(Constants.launch_WebView_Online_Manual_Index)) {
                 val getSaved_manaul_index_edit_url_Input = myDownloadClass.getString(Constants.getSaved_manaul_index_edit_url_Input, "").toString()
                loadOnlineLiveUrl(getSaved_manaul_index_edit_url_Input)
                load_live_indicator()

            } else if (get_launching_state.equals(Constants.launch_Default_WebView_url)) {
                loadOnlineUrl()
            } else {
                loadOnlineUrl()
            }
        }

    }


    private fun load_live_Parther_url_Format() {
        try {

            val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
            val sharedBiometric = getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)
            val fil_CLO = myDownloadClass.getString(Constants.getFolderClo, "").toString()
            val fil_DEMO = myDownloadClass.getString(Constants.getFolderSubpath, "").toString()
            val CP_AP_MASTER_DOMAIN =
                myDownloadClass.getString(Constants.CP_OR_AP_MASTER_DOMAIN, "").toString()

            val imagSwtichPartnerUrl =
                sharedBiometric.getString(Constants.imagSwtichPartnerUrl, "").toString()

            // if allowed to use partner url
            if (imagSwtichPartnerUrl == Constants.imagSwtichPartnerUrl) {
                val baseUrl = "${CP_AP_MASTER_DOMAIN}/$fil_CLO/$fil_DEMO/App/"

                loadOnlineLiveUrl(baseUrl)
                load_live_indicator()

                // if NOT allowed to use partner url
            } else {
                val get_custom_path_url =
                    myDownloadClass.getString(Constants.get_ModifiedUrl, "").toString()
                val appended_url = "$get_custom_path_url/$fil_CLO/$fil_DEMO/App/"

                loadOnlineLiveUrl(appended_url)
                load_live_indicator()

            }

        } catch (e: Exception) {
            Log.d(TAG, "load_live_Parther_url_Format: " + e.message.toString())
        }
    }


    private inner class NotifBroadcastReceiver : BroadcastReceiver() {

        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            try {
                if (!constants.Notif_Shown) {
                    showNotifxDialog(this@WebViewPage)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun goHomeOnError() {

        webView!!.loadUrl(MainUrl)

        // init chrome client if not avaliabel
        setupWebViewClients()


    }

    private fun ExitOnError() {
        val lockDown = sharedBiometric.getString(Constants.imgEnableLockScreen, "").toString()

        if (lockDown == Constants.imgEnableLockScreen){

            showToastMessage("Kindly Remove App from Lock down mode")

        }else{

        finishAndRemoveTask()
        Process.killProcess(Process.myTid())

        if (LoadLastWebPageOnAccidentalExit) {
            ClearLastUrl()
        }
        }
    }


    private fun getFilePath(CLO: String, DEMO: String, filename: String): String? {

        val finalFolderPathDesired = "/" + CLO + "/" + DEMO + "/" + Constants.App
        val destinationFolder =
            Environment.getExternalStorageDirectory().absolutePath + "/Download/${Constants.Syn2AppLive}/" + finalFolderPathDesired
        val filePath = "file://$destinationFolder$filename"
        val myFile = File(destinationFolder, File.separator + filename)

        return if (myFile.exists()) {
            filePath
        } else {
            null
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun loadOffline_Saved_Path_Offline_Webview(
        CLO: String,
        DEMO: String,
        fileName: String
    ) {
        lifecycleScope.launch {

            if (isActive){

            try {

                val filePath = withContext(Dispatchers.IO) {
                    try {
                        getFilePath(CLO, DEMO, fileName)
                    } catch (e: Exception) {
                        Log.d(TAG, "loadOffline_Saved_Path_Offline_Webview: ${e.message}")
                        null
                    }
                }

                // Now back on the main thread to update the UI
                if (filePath != null) {
                    if (isSystemRunning) {
                        webView?.apply {

                            clearHistory()
                            loadUrl(filePath.toString())
                            setupWebViewClients()
                            load_offline_indicator()
                        }
                    }
                } else {
                    if (isSystemRunning) {
                        showPopForTVConfiguration(Constants.compleConfiguration)
                    }
                }

            } catch (e: Exception) {
                if (isSystemRunning) {
                    Log.d(TAG, "loadOffline_Saved_Path_Offline_Webview: ${e.message}")
                    showPopForTVConfiguration(Constants.compleConfiguration)
                }
            }
        }
    }
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun loadOffline_Saved_Path_Offline_Webview_For_Pop_Layout(
        CLO: String,
        DEMO: String,
        filename: String
    ) {
        lifecycleScope.launch {

            if (isActive){

            try {
                // Offload file I/O operation to a background thread
                val filePath = withContext(Dispatchers.IO) {
                    try {
                        getFilePath(CLO, DEMO, filename)
                    } catch (e: Exception) {
                        showToastMessage("You need to Sync Files for Offline Usage")
                        null
                    }
                }

                // Now back on the main thread to update the UI
                handler.postDelayed(Runnable {

                    if (filePath != null) {
                        if (isSystemRunning) {
                            webView?.apply {
                                Log.d("PETER", "Yes The  FILES ARE BEEN CHECK after The user click from pop up for a states")
                                clearHistory()
                                loadUrl(filePath.toString())
                                setupWebViewClients()
                                load_offline_indicator()
                            }
                        }
                    } else {
                        if (isSystemRunning) {

                            if (Utility.isNetworkAvailable(applicationContext)) {
                                loadOnlineUrl()
                                load_live_indicator()
                            } else {
                                setUpTheFallingErrorLayout()

                            }
                        }
                    }

                }, 1500)

            } catch (e: Exception) {
                handler.postDelayed(Runnable {
                    if (isSystemRunning) {
                        if (Utility.isNetworkAvailable(applicationContext)) {
                            loadOnlineUrl()
                            load_live_indicator()
                        } else {
                            // show the offline page
                            setUpTheFallingErrorLayout()

                        }
                    }
                }, 1500)


            }
        }
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun loadOnlineUrl() {
        if (isSystemRunning) {
            Log.d("PETER", "InitWebvIewloadStates: Call Main Json  loadOnlineUrl ")

            // Configure WebViewClient and WebChromeClient if not already configured
            val sharedBiometric: SharedPreferences =
                applicationContext.getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)
            val JSON_MAIN_URL = sharedBiometric.getString(Constants.JSON_MAIN_URL, "").toString()

            if (MainUrl == null) {
                Log.d("PETER", "InitWebvIewloadStates: The Main Json Url Was null ")
                MainUrl = JSON_MAIN_URL
            }


            if (UrlIntent!!.hasExtra("url")) {
                webView!!.loadUrl(intent.getStringExtra("url")!!)

            } else if (data != null) {
                webView!!.loadUrl(data.toString())
            } else {
                if (LoadLastWebPageOnAccidentalExit) {
                    val lurl = preferences.getString("lasturl", "")
                    if (lurl!!.startsWith("http") || lurl.startsWith("https")) {
                        webView!!.loadUrl(lurl)
                    } else {
                        webView!!.loadUrl(MainUrl)
                    }
                } else {
                    webView!!.loadUrl(MainUrl)
                }
            }


            setupWebViewClients()
            load_live_indicator()

        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadOnlineLiveUrl(url: String) {
        try {

            // Load the provided URL
            webView?.loadUrl(url)


            // init chrome client
            setupWebViewClients()


        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "loadOnlineLiveUrl: " + e.message.toString())
        }
    }


    private fun load_live_indicator() {
        if (isSystemRunning) {
            val get_imagShowOnlineStatus =
                sharedBiometric.getString(Constants.imagShowOnlineStatus, "").toString()
            if (get_imagShowOnlineStatus != Constants.imagShowOnlineStatus) {
                imageCirclGreenOnline!!.visibility = View.VISIBLE
                imageCircleBlueOffline!!.visibility = View.INVISIBLE
            } else {
                imageCirclGreenOnline!!.visibility = View.INVISIBLE
                imageCircleBlueOffline!!.visibility = View.INVISIBLE
            }
        }
    }


    private fun load_offline_indicator() {
        if (isSystemRunning) {
            val get_imagShowOnlineStatus =
                sharedBiometric.getString(Constants.imagShowOnlineStatus, "")
            if (get_imagShowOnlineStatus != Constants.imagShowOnlineStatus) {
                imageCirclGreenOnline!!.visibility = View.INVISIBLE
                imageCircleBlueOffline!!.visibility = View.VISIBLE
            } else {
                imageCirclGreenOnline!!.visibility = View.INVISIBLE
                imageCircleBlueOffline!!.visibility = View.INVISIBLE
            }
        }
    }


    private fun setupWebViewClients() {

        //web view client
        webView?.setWebViewClient(object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                url: String
            ): WebResourceResponse? {
                if (isSystemRunning) {
                    if (BlockAds) {
                        if (url.contains("googleads.g.doubleclick.net")) {
                            val textStream: InputStream = ByteArrayInputStream("".toByteArray())
                            return getTextWebResource(textStream)
                        }
                    }
                }
                return super.shouldInterceptRequest(view, url)
            }


            private fun getTextWebResource(data: InputStream): WebResourceResponse? {
                return WebResourceResponse("text/plain", "UTF-8", data)
            }

            @SuppressLint("UnsafeImplicitIntentLaunch")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (isSystemRunning) {
                    try {

                        if (constants.AllowOnlyHostUrlInApp) {
                            if (!url.contains(constants.filterdomain)) {
                                webView!!.stopLoading()
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                return true
                            }
                        }

                        Log.d(
                            "shouldOverrideUrlLoading",
                            "shouldOverrideUrlLoading Exception: THE OEERIDING.."
                        )

                    } catch (e: java.lang.Exception) {
                        Log.i(TAG, "shouldOverrideUrlLoading Exception:" + e.message)

                    }

                    if (url.startsWith("http://") || url.startsWith("file:///") || url.startsWith("https://") || url.startsWith(
                            "setup://"
                        )
                    )
                        return false
                    try {
                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)!!
                        intent.addCategory("android.intent.category.BROWSABLE")
                        // forbid explicit call
                        intent.component = null
                        // forbid Intent with selector Intent
                        intent.selector = null
                        // start the activity by the Intent
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        view.context.startActivity(intent)
                    } catch (e: java.lang.Exception) {
                        Log.i(TAG, "shouldOverrideUrlLoading Exception:" + e.message)
                        showToastMessage("The app or ACTIVITY not found. Error Message:" + e.message)

                    }
                }
                return true
            }


            var isTimeTakeToLoadTooLong = false

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                if (isSystemRunning) {

                   /// errorLayout!!.visibility = View.GONE
                    if (drawer_menu!!.visibility == View.VISIBLE) {
                        drawer_menu!!.visibility = View.GONE
                    }
                    if (ShowSimpleProgressBar) {
                        simpleProgressbar!!.visibility = View.VISIBLE
                    }

                    isTimeTakeToLoadTooLong = true
                }

                handler.postDelayed(kotlinx.coroutines.Runnable {
                    if (isTimeTakeToLoadTooLong) {
                        showSnackBarInternet("The page is taking too long to load. Please check your internet connection.")
                        isTimeTakeToLoadTooLong = false
                    }
                }, 1 * 30 * 1000)

            }

            override fun onPageFinished(view: WebView?, url: String) {
                try {

                    if (customInternetWebviewPage != null){
                        customInternetWebviewPage!!.dismiss()
                        isCountDownDialogVisible = false
                    }

                    if (countdownTimerForWebviewPage != null){
                        countdownTimerForWebviewPage?.cancel()
                    }

                    isTimeTakeToLoadTooLong = false

                    if (url != "about:blank") {
                        lasturl = url
                        hasWebviewPageLoadedBefore = true
                    }

                    if (ShowSimpleProgressBar) {
                        simpleProgressbar!!.visibility = View.GONE
                    }
                    if (LoadLastWebPageOnAccidentalExit) {
                        preferences.edit().putString("lasturl", url).apply()
                    }
                    if (ShowInterstitialAd) {
                        Log.d("TAG", "The interstitial ad wasn't ready yet.")
                      /*   if (mInterstitialAd != null) { mInterstitialAd!!.show(this@WebViewPage) } else {
                            Log.d("TAG", "The interstitial ad wasn't ready yet.")
                        }*/

                    }

                    // clear history
                    //  webView!!.clearHistory()

                } catch (e: java.lang.Exception) {
                    showToastMessage(e.message!!)
                }
                super.onPageFinished(view, url)
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String,
                failingUrl: String?
            ) {
                if (description == "net::ERR_FAILED") {
                } else {
                    HideErrorPage(failingUrl!!, description)
                }
                super.onReceivedError(view, errorCode, description, failingUrl)
            }


        })


        //chrome web client
        webView!!.setWebChromeClient(object : WebChromeClient() {

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback
            ) {
                callback.invoke(origin, true, false)
                if (AllowGPSLocationAccess) {
                    if (isSystemRunning) {
                        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        webView!!.settings.setGeolocationEnabled(true)
                        displayLocationSettingsRequest(applicationContext)
                        showToastMessage("System working on Geo Location")
                    }
                } else {
                    showToastMessage("Location requested, You can enable location in settings")
                }
            }


            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (isSystemRunning) {
                    if (ShowHorizontalProgress) {
                        HorizontalProgressBar!!.progress = newProgress
                    }
                    val name = preferences!!.getString("proshow", "")
                    if (newProgress == 100) {
                        if (name == "show") {
                            windowProgressbar!!.visibility = View.GONE
                        }
                        try {
                            if (ShowProgressDialogue) {
                                progressDialog!!.cancel()
                                progressDialog!!.dismiss()
                                progressDialog!!.hide()
                            }
                            if (ShowToolbarProgress) {
                                tbarprogress!!.visibility = View.GONE
                            }
                            if (ShowHorizontalProgress) {
                                HorizontalProgressBar!!.visibility = View.GONE
                            }
                            if (ShowSimpleProgressBar) {
                                simpleProgressbar!!.visibility = View.GONE
                            }
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        if (name == "show") {
                            windowProgressbar!!.visibility = View.VISIBLE
                        }
                        try {
                            if (ShowHorizontalProgress) {
                                HorizontalProgressBar!!.visibility = View.VISIBLE
                            }
                            if (ShowProgressDialogue) {
                                progressDialog!!.setMessage("Loading")
                                progressDialog!!.setCancelable(false)
                                progressDialog!!.show()
                            }
                            if (ShowSimpleProgressBar) {
                                simpleProgressbar!!.visibility = View.VISIBLE
                            }
                            if (ShowToolbarProgress) {
                                tbarprogress!!.visibility = View.VISIBLE
                            }
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                            Log.d(TAG, "HideErrorPage: " + e.message.toString())
                        }
                    }
                }
            }

        })


    }


    private fun displayLocationSettingsRequest(context: Context) {

  /*      try {
            val googleApiClient = GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build()
            googleApiClient.connect()
            val TAG = "YOUR-TAG-NAME"
            val REQUEST_CHECK_SETTINGS = 0x1
            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 10000
            locationRequest.fastestInterval = (10000 / 2).toLong()
            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            builder.setAlwaysShow(true)
            val result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
            result.setResultCallback { result1: LocationSettingsResult ->
                val status = result1.status
                when (status.statusCode) {
                    LocationSettingsStatusCodes.SUCCESS -> Log.i(
                        TAG,
                        "All location settings are satisfied."
                    )

                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        Log.i(
                            TAG,
                            "Location settings are not satisfied. Show the user a dialog to upgrade location settings "
                        )
                        try {
                            status.startResolutionForResult(
                                this@WebViewPage,
                                REQUEST_CHECK_SETTINGS
                            )
                        } catch (e: SendIntentException) {
                            Log.i(TAG, "PendingIntent unable to execute request.")
                        }
                    }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Log.i(
                        TAG,
                        "Location settings are inadequate, and cannot be fixed here. Dialog not created."
                    )
                }
            }
        } catch (eP: Exception) {
            Log.d(TAG, "HideErrorPage: " + eP.message.toString())
        }
        */

    }

    private fun checkPermission(permission: String) {
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 3)
        }
    }

    private fun HideErrorPage(failingUrl: String, description: String) {
        try {

            if (isSystemRunning) {
                errorlayout?.visibility = View.VISIBLE
                errorCode?.text = description

                errorReloadButton!!.setOnClickListener {
                if (isSystemRunning) {
                    if (Utility.isNetworkAvailable(applicationContext)) {
                        if (isSystemRunning) {
                            webView!!.loadUrl(failingUrl)
                            isErrorLayoutShown = true
                        }
                    } else {
                        showToastMessage("Connect to an internet")
                    }
                }
            }

            handler.postDelayed(Runnable {
                if (isSystemRunning) {
                    handler.postDelayed(runnable!!, 4000)
                    if (isSystemRunning) {
                        if (errorautoConnect?.visibility == View.GONE) {
                            errorautoConnect?.visibility = View.VISIBLE
                        }
                    }
                    errorautoConnect?.text = "Auto Reconnect: Standby"
                    if (AdvancedControls.checkInternetConnection(applicationContext)) {
                        if (isSystemRunning) {
                            errorautoConnect!!.text = "Auto Reconnect: Trying to connect.."
                        }
                    } else {
                        if (isSystemRunning) {
                            if (Utility.isNetworkAvailable(applicationContext)) {
                            webView!!.loadUrl(failingUrl)
                            errorlayout!!.visibility = View.GONE
                            webView!!.clearHistory()
                            handler.removeCallbacks(runnable!!)

                             isErrorLayoutShown = true

                            } else {
                                showToastMessage("Connect to an internet")
                            }

                        }
                    }
                }
            }.also { if (isSystemRunning) { runnable = it } }, 4000) }

        } catch (e: java.lang.Exception) {
            Log.d(TAG, "HideErrorPage: " + e.message.toString())
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("ClickableViewAccessibility")
    private fun IntClikListnerOnWebView() {
        webView?.setOnTouchListener(OnTouchListener { view, motionEvent ->
            if (drawer_menu!!.isShown()) {
                AnimateHide(drawer_menu!!)
                drawer_menu!!.setVisibility(View.GONE)
                webView!!.setAlpha(1f)
            }
            false
        })


        web_button?.setOnClickListener(View.OnClickListener { v -> // Handle click event here
            try {
                val buttonClick = AlphaAnimation(0.1f, 0.4f)
                v.startAnimation(buttonClick)
                HandleRemoteCommand(constants.Web_button_link)
            } catch (e: Exception) {
                showToastMessage(e.message.toString())
            }
        })


        web_button?.setOnTouchListener(object : OnTouchListener {
            var dX = 0f
            var dY = 0f
            var lastAction = 0

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        dX = v.x - event.rawX
                        dY = v.y - event.rawY
                        lastAction = MotionEvent.ACTION_DOWN
                    }

                    MotionEvent.ACTION_MOVE -> {
                        v.y = event.rawY + dY
                        v.x = event.rawX + dX
                        lastAction = MotionEvent.ACTION_MOVE
                    }

                    MotionEvent.ACTION_UP ->                         // Delay before performing click action
                        Handler().postDelayed({
                            if (lastAction == MotionEvent.ACTION_DOWN) {
                                v.performClick()
                            }
                        }, 300) // Adjust delay time as needed
                    else -> return false
                }
                return true
            }
        })

    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun InitiateComponents() {
        try {
            val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

            val get_INSTALL_TV_JSON_USER_CLICKED =
                sharedTVAPPModePreferences.getString(Constants.INSTALL_TV_JSON_USER_CLICKED, "")
                    .toString()
            val showFloating_Button_APP =
                sharedTVAPPModePreferences.getBoolean(Constants.hide_Floating_Button_APP, false)


            if (get_INSTALL_TV_JSON_USER_CLICKED == Constants.INSTALL_TV_JSON_USER_CLICKED) {
                if (!showFloating_Button_APP) {
                    web_button!!.visibility = View.VISIBLE
                } else {
                    web_button!!.visibility = View.GONE
                }
            }


            /// continue with previous JSon
            /// continue with previous JSon
            if (get_INSTALL_TV_JSON_USER_CLICKED != Constants.INSTALL_TV_JSON_USER_CLICKED) {

                val get_floating_bar_to_show =
                    preferences.getBoolean(Constants.shwoFloatingButton, false)
                if (ShowWebButton || get_floating_bar_to_show == false) {
                    web_button!!.visibility = View.VISIBLE
                } else {
                    web_button!!.visibility = View.GONE
                }

            }
            /// end of part continue with previous JSon
            /// end part of continue with previous JSon


            if (ShowHorizontalProgress) {
                horizontalProgressFramelayout!!.visibility = View.VISIBLE
            }




            if (AllowRating) {
                TryRating()
            }



            if (ShowBottomBar) {
                try {
                    if (constants.ChangeBottombarBgColor) {
                        if (constants.bottomBarBgColor != null) {
                            bottomToolBar!!.setBackgroundColor(Color.parseColor(constants.bottomBarBgColor))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }


                bottomToolBar!!.visibility = View.VISIBLE


            }




            if (ShowSimpleProgressBar) {
                simpleProgressbar!!.visibility = View.VISIBLE
            }
            if (EnableSwipeRefresh) {
                swipeView!!.isEnabled = true
                swipeView!!.setColorSchemeColors(resources.getColor(R.color.app_color_accent))
                swipeView!!.setOnRefreshListener {
                    if (isSystemRunning) {
                        if (Utility.isNetworkAvailable(applicationContext)) {

                            if (hasWebviewPageLoadedBefore) {
                                webView!!.clearHistory()
                                webView!!.reload()
                                showSnackBar("Refreshing, please wait...")
                            } else {
                                webView!!.clearHistory()
                                InitWebvIewloadStatesWhenPopUpIsOn()
                            }

                        } else {

                            handler.postDelayed(Runnable {
                                if (isSystemRunning) {
                                    //set up fooline settings
                                    setUpTheFallingErrorLayout()
                                    showSnackBar("Please turn on the internet or connect to WiFi.")
                                }
                            }, 1500)

                        }

                        swipeView!!.isRefreshing = false
                    }
                }
            }


            if (ShowToolbar) {
                x_toolbar!!.visibility = View.VISIBLE
                if (!constants.ToolbarTitleText.isEmpty()) {
                    toolbartitleText!!.text = constants.ToolbarTitleText
                }
                try {
                    if (constants.ChangeTittleTextColor and !constants.ToolbarTitleTextColor.isEmpty()) {
                        toolbartitleText!!.setTextColor(Color.parseColor(constants.ToolbarTitleTextColor))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                try {
                    if (constants.ChangeToolbarBgColor and !constants.ToolbarBgColor.isEmpty()) {
                        if (preferences.getBoolean("darktheme", false)) {
                            x_toolbar!!.setBackgroundColor(resources.getColor(R.color.darkthemeColor))
                            bottomToolBar!!.setBackgroundColor(resources.getColor(R.color.darkthemeColor))
                            drawerHeaderBg!!.setBackgroundColor(resources.getColor(R.color.darkthemeColor))
                        } else {
                            x_toolbar!!.setBackgroundColor(Color.parseColor(constants.ToolbarBgColor))
                            window.statusBarColor = Color.parseColor(constants.ToolbarBgColor)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }


            if (ShowDrawer) {
                if (isSystemRunning) {
                    drawer_menu_btn!!.visibility = View.VISIBLE
                    drawer_menu_btn!!.setOnClickListener(imgClk)
                    drawerItem1!!.setOnClickListener(imgClk)
                    drawerItem2!!.setOnClickListener(imgClk)
                    drawerItem3!!.setOnClickListener(imgClk)
                    drawerItem4!!.setOnClickListener(imgClk)
                    drawerItem5!!.setOnClickListener(imgClk)
                    drawerItem6!!.setOnClickListener(imgClk)
                    drawer_header_img!!.setOnClickListener(imgClk)
                    try {
                        if (constants.ChangeHeaderTextColor and (constants.drawerHeaderTextColor != null)) {
                            if (preferences.getBoolean("darktheme", false)) {
                                drawer_header_text!!.setTextColor(Color.WHITE)
                            } else {
                                drawer_header_text!!.setTextColor(Color.parseColor(constants.drawerHeaderTextColor))
                            }
                        }
                        if ((constants.drawerHeaderBgColor != null) and constants.ChangeDrawerHeaderBgColor) {
                            if (preferences.getBoolean("darktheme", false)) {
                                drawerHeaderBg!!.setBackgroundColor(resources.getColor(R.color.darkthemeColor))
                            } else {
                                drawerHeaderBg!!.setBackgroundColor(Color.parseColor(constants.drawerHeaderBgColor))
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    HandleRemoteDrawerText(drawerItemtext1!!, constants.drawerMenuItem1Text)
                    HandleRemoteDrawerText(drawerItemtext2!!, constants.drawerMenuItem2Text)
                    HandleRemoteDrawerText(drawerItemtext3!!, constants.drawerMenuItem3Text)
                    HandleRemoteDrawerText(drawerItemtext4!!, constants.drawerMenuItem4Text)
                    HandleRemoteDrawerText(drawerItemtext5!!, constants.drawerMenuItem5Text)
                    HandleRemoteDrawerText(drawerItemtext6!!, constants.drawerMenuItem6Text)
                    HandleRemoteDrawerText(drawer_header_text!!, constants.drawerHeaderText)
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "InitiateComponents: " + e.message.toString())
        }
    }

    private fun setUpTheFallingErrorLayout() {
        // Configure WebViewClient and WebChromeClient if not already configured
        val sharedBiometric: SharedPreferences = applicationContext.getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)
        val JSON_MAIN_URL = sharedBiometric.getString(Constants.JSON_MAIN_URL, "").toString()

        if (MainUrl == null) {
            Log.d("PETER", "InitWebvIewloadStates: The Main Json Url Was null ")
            MainUrl = JSON_MAIN_URL
        }

        // show off;ine page
        simpleProgressbar!!.visibility = View.GONE
        webView!!.loadUrl("about:blank")
        webView!!.clearHistory()

        if (isErrorLayoutShown){

            HideErrorPage(MainUrl, "Failed to load page")

            isErrorLayoutShown = false

            Log.d("HideErrorPage", "InitWebvIewloadStates: HideErrorPage ")

        }
    }



    private fun showSnackBar(message: String) {
        val snackbar = Snackbar.make(binding.mainLayout, message, Snackbar.LENGTH_LONG)
        snackbar.show()
    }

    private fun showSnackBarInternet(message: String) {
        val snackbar = Snackbar.make(binding.mainLayout, message, Snackbar.LENGTH_INDEFINITE)
        snackbar.show()
        if (isSystemRunning) {
            handler.postDelayed({
                if (isSystemRunning) {
                    snackbar.dismiss()
                }
            }, 1 * 30 * 1000)
        }
    }

    private fun checkPathForFilesWhenOffline() {
        val USE_OFFLINE_FOLDER =
            sharedBiometric.getString(Constants.USE_OFFLINE_FOLDER, "").toString()
        if (USE_OFFLINE_FOLDER == Constants.USE_OFFLINE_FOLDER) {
            InitWebviewIndexFileState()
        } else {
            showPopInternetForWebPage(Constants.Check_Inter_Connectivity)

        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun InitializeRemoteData() {
        try {
            bottomToolbar_img_1!!.setOnClickListener(imgClk)
            bottomToolbar_img_2!!.setOnClickListener(imgClk)
            bottomToolbar_img_3!!.setOnClickListener(imgClk)
            bottomToolbar_img_4!!.setOnClickListener(imgClk)
            bottomToolbar_img_5!!.setOnClickListener(imgClk)
            bottomToolbar_img_6!!.setOnClickListener(imgClk)
            ConfigureRemoteImageData(constants.bottomBtn1ImgUrl, bottomToolbar_img_1!!)
            ConfigureRemoteImageData(constants.bottomBtn2ImgUrl, bottomToolbar_img_2!!)
            ConfigureRemoteImageData(constants.bottomBtn3ImgUrl, bottomToolbar_img_3!!)
            ConfigureRemoteImageData(constants.bottomBtn4ImgUrl, bottomToolbar_img_4!!)
            ConfigureRemoteImageData(constants.bottomBtn5ImgUrl, bottomToolbar_img_5!!)
            ConfigureRemoteImageData(constants.bottomBtn6ImgUrl, bottomToolbar_img_6!!)
            ConfigureRemoteImageData(constants.Web_button_Img_link, web_button!!)
            if (ShowDrawer) {
                ConfigureRemoteImageData(constants.drawerMenuImgUrl, drawer_menu_btn!!)
                ConfigureRemoteImageData(constants.drawerMenuItem2ImgUrl, drawerImg2!!)
                ConfigureRemoteImageData(constants.drawerMenuItem3ImgUrl, drawerImg3!!)
                ConfigureRemoteImageData(constants.drawerMenuItem4ImgUrl, drawerImg4!!)
                ConfigureRemoteImageData(constants.drawerMenuItem5ImgUrl, drawerImg5!!)
                ConfigureRemoteImageData(constants.drawerMenuItem6ImgUrl, drawerImg6!!)
                ConfigureRemoteImageData(constants.drawerMenuItem1ImgUrl, drawerImg1!!)
                ConfigureRemoteImageData(constants.drawerHeaderImgUrl, drawer_header_img!!)
            }
        } catch (e: Exception) {
            Log.d(TAG, "InitializeRemoteData: " + e.message.toString())
        }
    }

    private fun InitiatePreferences() {


        try {

            val get_INSTALL_TV_JSON_USER_CLICKED = sharedTVAPPModePreferences.getString(Constants.INSTALL_TV_JSON_USER_CLICKED, "").toString()
            val show_BottomBar_APP = sharedTVAPPModePreferences.getBoolean(Constants.hide_BottomBar_APP, false)
            val fullScreen_APP = sharedTVAPPModePreferences.getBoolean(Constants.hide_BottomBar_APP, false)
            val immersive_Mode_APP = sharedTVAPPModePreferences.getBoolean(Constants.immersive_Mode_APP, false)

            if (get_INSTALL_TV_JSON_USER_CLICKED == Constants.INSTALL_TV_JSON_USER_CLICKED) {

                // show_BottomBar_APP.also { this.ShowBottomBar = it }

                ShowBottomBar = !show_BottomBar_APP

                if (fullScreen_APP) {
                    // Enable full screen
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN
                    )

                } else {
                    // Disable full screen
                    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                }


                /// enable immersive mode if true
                if (immersive_Mode_APP) {
                    ShowToolbar = false
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN
                    )
                } else {
                    // Disable full screen
                    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                }


            }


            /// continue with previous JSon
            /// continue with previous JSon
            if (get_INSTALL_TV_JSON_USER_CLICKED != Constants.INSTALL_TV_JSON_USER_CLICKED) {

                // check if hide bottombar
                if (preferences.getBoolean("hidebottombar", false)) {
                    ShowBottomBar = false
                }

                // enable Full Screen if true
                if (preferences.getBoolean("fullscreen", false)) {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN
                    )
                }


                /// enable immersive mode if true
                if (preferences.getBoolean("immersive_mode", false)) {
                    ShowToolbar = false
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN
                    )
                }


            }


            /// End of continue with previous JSon
            /// End of continue with previous JSon

            if (preferences.getBoolean("swiperefresh", false)) {
                EnableSwipeRefresh = true
            }


            if (preferences.getBoolean("nightmode", false)) {
                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    WebSettingsCompat.setForceDark(
                        webView!!.settings,
                        WebSettingsCompat.FORCE_DARK_ON
                    )
                }
            }
            if (preferences.getBoolean("blockAds", false)) {
                BlockAds = true
            }
            if (preferences.getBoolean("nativeload", false)) {
                ShowNativeLoadView = true
                ShowSimpleProgressBar = false
            }


            if (preferences.getBoolean("geolocation", false)) {
                webView!!.settings.setGeolocationEnabled(true)
                webView!!.settings.setGeolocationDatabasePath(applicationContext!!.filesDir.path)
                AllowGPSLocationAccess = true
            }




            if (preferences.getBoolean("permission_query", false)) {
                RequestRunTimePermissions = true
            }


            if (preferences.getBoolean("loadLastUrl", false)) {
                LoadLastWebPageOnAccidentalExit = true
            }



            if (preferences.getBoolean("autohideToolbar", false)) {
                AutoHideToolbar = true
            }


        } catch (e: Exception) {
            Log.d(TAG, "ConfigureRemoteImageData: " + e.message.toString())
        }
    }

    private fun ConfigureRemoteImageData(url: String?, view: ImageView) {

        try {
            if ((url == null) or (url == "null")) return
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        try {
            if (url != null && url.endsWith("svg")) {
                GlideToVectorYou
                    .init()
                    .with(this)
                    .withListener(object : GlideToVectorYouListener {
                        override fun onLoadFailed() {}
                        override fun onResourceReady() {}
                    })
                    .setPlaceHolder(R.drawable.demo_btn_24, R.drawable.demo_btn_24)
                    .load(Uri.parse(url), view)
            } else {
                Glide.with(this)
                    .load(url) // image url
                    .placeholder(R.drawable.demo_btn_24) // any placeholder to load at start
                    .error(R.drawable.demo_btn_24) // any image in case of error
                    .into(view) // imageview object
            }
        } catch (e: java.lang.Exception) {
            Log.d(TAG, "ConfigureRemoteImageData: " + e.message.toString())
        }
    }

    private fun HandleRemoteDrawerText(textv: TextView, text: String) {
        textv.text = text
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private fun HandleRemoteCommand(command: String) {


        try {
            if (command == "openSettings") {

                try {
                    if (fetchListener != null) {
                        fetch!!.removeListener(fetchListener)
                    }

                    if (fetch != null) {
                        fetch!!.removeAll()
                    }


                    // webView!!.stopLoading()
                    //  webView!!.destroy()

                    handler.postDelayed(Runnable {
                        val myactivity = Intent(this@WebViewPage, SettingsActivityKT::class.java)
                        startActivity(myactivity)
                        finish()

                    }, 500)

                    showToastMessage("Please wait..")
                } catch (e: Exception) {
                    showToastMessage(e.message.toString())
                }
            } else if (command == "webGoBack") {
                if (webView!!.canGoBack()) {
                    webView!!.goBack()
                } else {
                    AdvancedControls.showToast(applicationContext, "No back page")
                }
            } else if (command == "webGoForward") {
                if (webView!!.canGoForward()) {
                    webView!!.goForward()
                } else {
                    AdvancedControls.showToast(applicationContext, "No forward page")
                }
            } else if (command == "reload") {
                webView!!.reload()
            } else if (command == "sharePage") {
                ShareItem(webView!!.originalUrl!!, "Check Out This!", " ")
            } else if (command == "goHome") {
                webView!!.loadUrl(constants.jsonUrl)
            } else if (command == "openDrawer") {
                ShowHideViews(drawer_menu!!)
            } else if (command == "ExitApp") {

                val sharedBiometric22 =
                    getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)
                val get_ProtectPassowrd =
                    sharedBiometric22.getString(Constants.PROTECT_PASSWORD, "").toString()
                if (get_ProtectPassowrd == Constants.PROTECT_PASSWORD) {
                    showExitConfirmationDialog()

                } else {

                    val lockDown = sharedBiometric.getString(Constants.imgEnableLockScreen, "").toString()

                    if (lockDown == Constants.imgEnableLockScreen){
                        showToastMessage("Kindly Remove App from Lock down mode")

                    }else{
                        finishAndRemoveTask()
                        Process.killProcess(Process.myTid())
                    }

                }


            } else if (command == "ScanCode") {

                handler.postDelayed(Runnable {
                    val intent = Intent(applicationContext, QRSanActivity::class.java)
                    startActivity(intent)
                    finish()
                }, 500)


            } else if (command == "null") {
            } else {
                webView!!.loadUrl(command)
            }
        } catch (e: Exception) {
            Log.d(TAG, "HandleRemoteCommand: " + e.message.toString())
        }

    }


    @SuppressLint("InflateParams", "SuspiciousIndentation")
    private fun showExitConfirmationDialog() {
        try {
            val binding: CustomWebviewpagePasswordLayoutBinding =
                CustomWebviewpagePasswordLayoutBinding.inflate(layoutInflater)
            val builder = AlertDialog.Builder(this)
            builder.setView(binding.getRoot())
            val alertDialog = builder.create()
            alertDialog.setCanceledOnTouchOutside(true)
            alertDialog.setCancelable(true)

            // Set the background of the AlertDialog to be transparent
            if (alertDialog.window != null) {
                alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                alertDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
            }

            val editTextText2: EditText = binding.editTextText2
            val textExit: TextView = binding.textExit
            val textCancel: TextView = binding.textCancel
            val textForgetPassword: TextView = binding.textForgetPasswordHome
            val imagePassowrdSettings: ImageView = binding.imagePassowrdSettings
            val imgClearCatch: ImageView = binding.imgClearCatch
            val imgWifi: ImageView = binding.imgWifi
            val imgMaintainace: ImageView = binding.imgMaintainace
            val divider2: View = binding.divider2
            val imgToggle: ImageView = binding.imgToggle
            val imgToggleNzotVisible: ImageView = binding.imgToggleNzotVisible



            textCancel.setOnClickListener {
                alertDialog.dismiss()
            }


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



            imagePassowrdSettings.setOnClickListener {

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
                    editor333.putString(Constants.SAVE_NAVIGATION, Constants.WebViewPage)
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



            imgMaintainace.setOnClickListener {

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
                    editor333.putString(Constants.SAVE_NAVIGATION, Constants.WebViewPage)
                    editor333.apply()

                    val myactivity = Intent(this@WebViewPage, MaintenanceActivity::class.java)
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

                hideKeyBoard(editTextText2)
                val getPasswordPrefilled =
                    simpleSavedPassword.getString(Constants.passowrdPrefeilled, "").toString()
                val getSimpleAdminPassword =
                    simpleSavedPassword.getString(Constants.mySimpleSavedPassword, "").toString()

                val editTextText = editTextText2.text.toString().trim { it <= ' ' }

                if (getPasswordPrefilled == Constants.passowrdPrefeilled || editTextText == getSimpleAdminPassword) {

                    val lockDown = sharedBiometric.getString(Constants.imgEnableLockScreen, "").toString()

                    if (lockDown == Constants.imgEnableLockScreen){

                        showToastMessage("Kindly Remove App from Lock down mode")

                    }else{

                        val editor = myDownloadMangerClass.edit()
                        editor.remove(Constants.SynC_Status)
                        editor.apply()

                        val editor22 = simpleSavedPassword.edit()
                        editor22.remove(Constants.Did_User_Input_PassWord)
                        editor22.apply()


                        alertDialog.dismiss()

                        handler.postDelayed(Runnable {
                            finishAndRemoveTask()
                            Process.killProcess(Process.myTid())
                        }, 300)
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

                val isSavedEmail =
                    simpleSavedPassword.getString(Constants.isSavedEmail, "").toString()
                hideKeyBoard(editTextText2)
                if (isSavedEmail.isNotEmpty() && Utility.isValidEmail(isSavedEmail)) {

                    showPopChangePassowrdDialog()
                    alertDialog.dismiss()

                } else {
                    showPopRedirectuser()
                    alertDialog.dismiss()
                }
            }


            alertDialog.show()
        } catch (e: Exception) {
            Log.d(ContentValues.TAG, "showExitConfirmationDialog: Erro ${e.message}")
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
                if (editTextText.isNotEmpty() && Utility.isValidEmail(editTextText)) {
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
            if (isSavedEmail.isNotEmpty() && Utility.isValidEmail(isSavedEmail)) {
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


    @SuppressLint("UseCompatLoadingForDrawables")
    private fun showCustomProgressDialog(message: String) {
        try {
            customProgressDialog = Dialog(this)
            val binding: ProgressDialogLayoutBinding = ProgressDialogLayoutBinding.inflate(
                LayoutInflater.from(this)
            )
            customProgressDialog!!.setContentView(binding.getRoot())
            customProgressDialog!!.setCancelable(false)
            customProgressDialog!!.setCanceledOnTouchOutside(false)
            customProgressDialog!!.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            customProgressDialog!!.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation

            binding.textLoading.setText(message)
            binding.imgCloseDialog.setVisibility(View.GONE)
            val consMainAlert_sub_layout: ConstraintLayout = binding.consMainAlertSubLayout
            val imagSucessful: ImageView = binding.imagSucessful
            val textLoading: TextView = binding.textLoading
            val preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(
                applicationContext
            )
            if (preferences.getBoolean("darktheme", false)) {
                consMainAlert_sub_layout.setBackgroundResource(R.drawable.card_design_account_number_dark_pop_layout)
                textLoading.setTextColor(resources.getColor(R.color.dark_light_gray_pop))
                val drawable_imagSucessful = ContextCompat.getDrawable(
                    applicationContext, R.drawable.ic_email_read_24
                )
                if (drawable_imagSucessful != null) {
                    drawable_imagSucessful.setColorFilter(
                        ContextCompat.getColor(
                            applicationContext, R.color.dark_light_gray_pop
                        ), PorterDuff.Mode.SRC_IN
                    )
                    imagSucessful.setImageDrawable(drawable_imagSucessful)
                }
            }
            customProgressDialog!!.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
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


    private fun hideKeyBoard(editText: EditText) {
        try {
            editText.clearFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editText.windowToken, 0)
        } catch (ignored: java.lang.Exception) {
        }
    }


    private fun ShowHideViews(Myview: View) {
        if (Myview.visibility == View.GONE) {
            AnimateShow(Myview)
            Myview.visibility = View.VISIBLE
            webView!!.alpha = 0.5f
        } else if (Myview.visibility == View.VISIBLE) {
            AnimateHide(Myview)
            Myview.visibility = View.GONE
            webView!!.alpha = 1f
        }
    }


    private fun AnimateShow(view: View) {
        val anim = AnimationUtils.loadAnimation(
            baseContext,
            R.anim.slide_to_right
        )
        view.startAnimation(anim)
    }

    private fun AnimateHide(view: View) {
        val anim = AnimationUtils.loadAnimation(
            baseContext,
            R.anim.slide_to_left
        )
        view.startAnimation(anim)
    }

    private fun ShareItem(ShareText: String, Subject: String, ShareTitle: String) {
        try {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, Subject)
            sharingIntent.putExtra(Intent.EXTRA_TEXT, ShareText)
            startActivity(Intent.createChooser(sharingIntent, ShareTitle))
        } catch (e: Exception) {
            Log.d(TAG, "ShareItem: " + e.message.toString())
        }
    }

    private fun TryRating() {
        if (preferences.getBoolean("dontshowagain", false)) {
            return
        }
        val editor = prefs!!.edit()
        val launch_count = prefs!!.getLong("launch_count", 0) + 1
        editor.putLong("launch_count", launch_count)
        // Get date of first launch
        var date_firstLaunch = prefs!!.getLong("date_firstlaunch", 0)
        if (date_firstLaunch == 0L) {
            date_firstLaunch = System.currentTimeMillis()
            editor.putLong("date_firstlaunch", date_firstLaunch)
        }
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch) {
                showRateDialog()
            }
        }
        editor.apply()
    }


    private fun showRateDialog() {

        try {
            mydialog = Dialog(this)
            ratingbar!!.onRatingBarChangeListener =
                OnRatingBarChangeListener { ratingBar, rating, fromUser ->
                    if (fromUser) {
                        ratingBar.rating = Math.ceil(rating.toDouble()).toFloat()
                    }
                }
            mydialog!!.show()
        } catch (e: Exception) {
            Log.d(TAG, "showRateDialog: " + e.message.toString())
        }

    }

    private fun showNotifxDialog(context: Context?) {
        try {

            val lastNotifxId = preferences?.getString("lastId", "").toString()
            if (constants.NotifAvailable and (lastNotifxId != constants.Notif_ID!!)) {
                try {
                    val binding: CustomNotificationLayoutBinding =
                        CustomNotificationLayoutBinding.inflate(layoutInflater)
                    val builder = AlertDialog.Builder(this)
                    builder.setView(binding.getRoot())
                    val dialog = builder.create()
                    dialog.setCanceledOnTouchOutside(false)
                    dialog.setCancelable(false)

                    // Set the background of the AlertDialog to be transparent
                    if (dialog.window != null) {
                        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    }
                    val notifTitle: TextView = binding.notifTitle
                    val notifDesc: TextView = binding.notifDesc
                    val closeThis: ImageView = binding.closeNotif
                    val notifButton: TextView = binding.notifActionButton
                    val imageView: ImageView = binding.notifImg
                    closeThis.setOnClickListener {
                        dialog.dismiss()
                        dialog.cancel()
                    }
                    notifButton.setOnClickListener {
                        if (constants.Notif_button_action.startsWith("https") or constants.Notif_button_action.startsWith(
                                "https"
                            )
                        ) {
                            if (constants.NotifLinkExternal) {
                                webView!!.loadUrl(constants.Notif_button_action)
                            } else {
                                redirectStore(constants.Notif_button_action)
                            }
                            dialog.dismiss()
                            dialog.cancel()
                        } else if (constants.Notif_button_action == "dismiss") {
                            dialog.dismiss()
                            dialog.cancel()
                        }
                        dialog.dismiss()
                        dialog.cancel()
                    }
                    notifTitle.text = constants.Notif_title
                    notifDesc.text = Html.fromHtml(constants.Notif_desc)
                    Glide.with(context!!)
                        .load(constants.Notif_Img_url) // image url
                        .placeholder(R.drawable.img_logo_icon) // any placeholder to load at start
                        .error(R.drawable.img_logo_icon) // any image in case of error
                        .into(imageView) // imageview object
                    dialog.setCancelable(false)
                    if (constants.NotifSound) {
                        val mp = MediaPlayer.create(context, R.raw.alertx)
                        mp.setVolume(0.1.toFloat(), 0.1.toFloat())
                        mp.start()
                    }
                    val editor = preferences.edit()
                    editor.putString("lastId", constants.Notif_ID).apply()
                    constants.Notif_Shown = true
                    try {
                        dialog.show()
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }

        } catch (e: Exception) {
            Log.d(TAG, "navigateBackTosetting: " + e.message.toString())
        }
    }

    @SuppressLint("UnsafeImplicitIntentLaunch")
    private fun redirectStore(updateUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            Log.d(TAG, "navigateBackTosetting: " + e.message.toString())
        }
    }


    private fun UpdateApp(updateUrl: String?, forceUpdate: Boolean) {
        try {
            val dialog = AlertDialog.Builder(this)
                .setTitle(constants.UpdateTitle)
                .setMessage(constants.UpdateMessage)
                .setCancelable(!forceUpdate)
                .setNeutralButton("Later") { dialog, which ->
                    if (forceUpdate) {
                        finish()
                    } else {
                        dialog.dismiss()
                    }
                }
                .setPositiveButton(
                    "Update"
                ) { dialog, which ->
                    redirectStore(updateUrl!!)
                    if (forceUpdate) {
                        finish()
                    }
                }.setNegativeButton(
                    "No, thanks"
                ) { dialog, which ->
                    if (forceUpdate) {
                        finish()
                    }
                }.create()
            try {
                dialog.show()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Log.d(TAG, "navigateBackTosetting: " + e.message.toString())
            }
        } catch (e: java.lang.Exception) {
            Log.d(TAG, "navigateBackTosetting: " + e.message.toString())
        }
    }


    private fun CheckUpdate() {
        try {
            constants.CurrVersion = applicationContext!!.packageManager
                .getPackageInfo(applicationContext!!.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        if (constants.UpdateAvailable and (constants.CurrVersion != constants.NewVersion)) {
            UpdateApp(constants.UpdateUrl, constants.ForceUpdate)

        } else {
            Log.d("RemoteConfig", "No Update or version are equal ")
        }
    }


    private fun navigateBackTosetting() {

        try {
            webView!!.stopLoading()
            webView!!.destroy()

            if (fetchListener != null) {
                fetch!!.removeListener(fetchListener)
            }

            if (fetch != null) {
                fetch!!.removeAll()
            }


            //  webView!!.stopLoading()
            //  webView!!.destroy()

            handler.postDelayed(Runnable {
                val myactivity = Intent(this@WebViewPage, SettingsActivityKT::class.java)
                startActivity(myactivity)
                finish()

            }, 500)

            showToastMessage("Please wait..")

        } catch (e: Exception) {
            Log.d(TAG, "navigateBackTosetting: " + e.message.toString())
        }
    }

    private fun ShowExitDialogue() {
        try {
            AlertDialog.Builder(this)
                .setIcon(R.drawable.img_logo_icon)
                .setTitle("Exit")
                .setMessage("Are you sure to Exit?")
                .setPositiveButton("Yes") { dialog, which ->
                    ClearLastUrl()
                    System.exit(0)
                }
                .setNegativeButton("No", null)
                .show()
        } catch (e: java.lang.Exception) {
            Log.d(TAG, "ShowExitDialogue: " + e.message.toString())
        }
    }


    private fun ClearLastUrl() {
        val pp = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        pp.edit().remove("lasturl").apply()
    }


    private inner class ConnectivityReceiver : BroadcastReceiver() {

        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            try {
                if (Utility.isNetworkAvailable(applicationContext)) {
                    try {
                        val SPLASH_TIME_OUT = 1000
                        textStatusProcess?.setText("Connecting..")

                        handler.postDelayed(Runnable {
                            try {
                                if (isSystemRunning) {
                                    myDownloadStatus()
                                   // onAppRedirectToJsonPage()
                                }
                                Log.d("HFHGHHH", "onAppRedirectToJsonPage: internet Connection")

                            } catch (e: java.lang.Exception) {
                            }
                        }, SPLASH_TIME_OUT.toLong())

                    } catch (ignored: java.lang.Exception) {
                    }
                } else {
                    Log.d("HFHGHHH", "onAppRedirectToJsonPage: No internet Connection")
                    // No internet Connection
                    try {
                        runOnUiThread {
                            if (isSystemRunning) {
                                imageWiFiOn!!.visibility = View.GONE
                                imageWiFiOFF!!.visibility = View.VISIBLE
                                textStatusProcess!!.text = "No Internet"
                            }
                        }

                    } catch (e: java.lang.Exception) {
                    }
                }

                // No internet Connection
            } catch (ignored: java.lang.Exception) {
            }
        }
    }


    private fun onAppRedirectToJsonPage() {
        runOnUiThread {
            if (isSystemRunning) {
                val isSavedEmail =
                    simpleSavedPassword.getString(Constants.isSavedEmail, "").toString()
                val COUNTRY_NAME =
                    simpleSavedPassword.getString(Constants.COUNTRY_NAME, "").toString()
                val USER_NAME = simpleSavedPassword.getString(Constants.USER_NAME, "").toString()
                val USER_COMPANY_NAME =
                    simpleSavedPassword.getString(Constants.USER_COMPANY_NAME, "").toString()

                if (!isSavedEmail.isEmpty() && !COUNTRY_NAME.isEmpty() && !USER_NAME.isEmpty() && !USER_COMPANY_NAME.isEmpty() && jsonUrl == null) {
                    val intent = Intent(applicationContext, SplashKT::class.java)
                    startActivity(intent)
                    finish()

                }
            }
        }
    }


    private fun myDownloadStatus() {
        try {
            runOnUiThread {
                if (isSystemRunning) {


                    ///
                    val get_Api_state = sharedBiometric.getString(Constants.imagSwtichEnableSyncFromAPI, "").toString()
                    if (Utility.isNetworkAvailable(applicationContext)) {

                        imageWiFiOn?.visibility = View.VISIBLE
                        imageWiFiOFF?.visibility = View.GONE

                        if (get_Api_state == Constants.imagSwtichEnableSyncFromAPI) {
                            val get_progress =
                                myDownloadClass.getString(Constants.SynC_Status, "").toString()
                            if (get_progress.isNotEmpty()) {
                                textStatusProcess?.text = get_progress + ""
                            } else {
                                textStatusProcess?.text = "PR: Running"
                            }
                        } else {
                            textStatusProcess?.text = "PR: Running"
                        }
                    } else {
                        textStatusProcess?.text = "No Internet"

                        imageWiFiOn?.visibility = View.GONE
                        imageWiFiOFF?.visibility = View.VISIBLE

                    }
                }
            }
        } catch (e: java.lang.Exception) {
        }
    }


    @SuppressLint("MissingInflatedId", "UseCompatLoadingForDrawables")
    private fun showPopForTVConfiguration(message: String) {
        try {

            try {
                val binding: CustomOfflinePopLayoutBinding = CustomOfflinePopLayoutBinding.inflate(
                    layoutInflater
                )
                val builder = AlertDialog.Builder(this@WebViewPage)
                builder.setView(binding.getRoot())
                alertDialog = builder.create() // Assign the dialog to the field
                alertDialog!!.setCanceledOnTouchOutside(false)
                alertDialog!!.setCancelable(false)
                if (alertDialog!!.window != null) {
                    alertDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    alertDialog!!.window!!.attributes.windowAnimations =
                        R.style.PauseDialogAnimationCloseOnly
                }
                val sharedBiometric = getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)
                val textContinuPasswordDai3: TextView = binding.textContinuPasswordDai3
                val textContinue: TextView = binding.textContinue
                val textDescription: TextView = binding.textDescription
                val imgCloseDialog: ImageView = binding.imgCloseDialog
                val imageView24: ImageView = binding.imageView24


                if (!message.isEmpty()) {
                    textDescription.text = message
                }

                if (message == Constants.UnableToFindIndex) {
                    imageView24.background = resources.getDrawable(R.drawable.ic_folder_24)

                } else if (message == Constants.Check_Inter_Connectivity) {

                    imageView24.background = resources.getDrawable(R.drawable.ic_wifi_no_internet)
                } else {

                    imageView24.background = resources.getDrawable(R.drawable.ic_sync_cm)
                }


                val editor222 = sharedBiometric.edit()
                textContinuPasswordDai3.setOnClickListener {
                    try {
                        if (fetchListener != null) {
                            fetch!!.removeListener(fetchListener)
                        }

                        if (fetch != null) {
                            fetch!!.removeAll()
                        }


                        editor222.putString(Constants.SAVE_NAVIGATION, Constants.WebViewPage)
                        editor222.apply()
                        showToastMessage("Please wait")

                        finish()
                        val myactivity = Intent(this@WebViewPage, ReSyncActivity::class.java)
                        startActivity(myactivity)


                    } catch (e: Exception) {
                        Log.d(TAG, "showPopForTVConfiguration: " + e.message.toString())
                    }
                }


                textContinue.setOnClickListener {
                    Log.d(
                        "PETER",
                        "InitWebvIewloadStates:: The user click from pop up for a states"
                    )
                    dailaogShowPopCallingWebview()
                    alertDialog!!.dismiss()
                }


                imgCloseDialog.setOnClickListener {
                    Log.d("PETER", "The user click from pop up for a states")
                    dailaogShowPopCallingWebview()
                    alertDialog!!.dismiss()
                }


                alertDialog!!.show()

            } catch (e: java.lang.Exception) {
                Log.d(TAG, "showPopForTVConfiguration: " + e.message.toString())
            }

        } catch (e: Exception) {
            Log.d(TAG, "showPopForTVConfiguration: Eroor ${e.message}")
        }
    }


    private fun showPopInternetForWebPage(message: String) {

        customInternetWebviewPage = Dialog(this)
        val bindingCP: CustomLayoutWebInternetBinding = CustomLayoutWebInternetBinding.inflate(LayoutInflater.from(this))
        customInternetWebviewPage!!.setContentView(bindingCP.getRoot())
        customInternetWebviewPage!!.setCancelable(false)
        customInternetWebviewPage!!.setCanceledOnTouchOutside(false)
        customInternetWebviewPage!!.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        customInternetWebviewPage!!.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation

        isCountDownDialogVisible  = true

        if (!message.isEmpty()) {
            bindingCP.textDescription.text = message
        }


        if (message == Constants.UnableToFindIndex) {
            val drawable_imageView24 = ContextCompat.getDrawable(applicationContext, R.drawable.ic_folder_24)
            setUpDrawableImage(drawable_imageView24, bindingCP.imageView24)
        }


        if (message == Constants.Check_Inter_Connectivity) {
            val drawable_imageView24 = ContextCompat.getDrawable(applicationContext, R.drawable.ic_wifi_no_internet)
            setUpDrawableImage(drawable_imageView24, bindingCP.imageView24)
        }



      bindingCP.textContinue.setOnClickListener {
            Log.d("PETER", "InitWebvIewloadStates:: The user click from pop up for a states")
            dailaogShowPopCallingWebview()

            if (customInternetWebviewPage != null){
                customInternetWebviewPage!!.dismiss()
            }

          if ( countdownTimerForWebviewPage != null){
              countdownTimerForWebviewPage?.cancel()
          }

          isCountDownDialogVisible  = false
        }


        bindingCP.imgCloseDialog.setOnClickListener {
            Log.d("PETER", "The user click from pop up for a states")
            dailaogShowPopCallingWebview()

            if (customInternetWebviewPage != null){
                customInternetWebviewPage!!.dismiss()
            }

            if ( countdownTimerForWebviewPage != null){
                countdownTimerForWebviewPage?.cancel()
            }

            isCountDownDialogVisible  = false
        }


        ///////////////
        bindingCP.textCountDown.visibility = View.VISIBLE
        val minutes = 1L
        val milliseconds = minutes * 15 * 1000

        countdownTimerForWebviewPage = object : CountDownTimer(milliseconds, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                try {
                    countdownTimerForWebviewPage!!.start()

                if (isCountDownDialogVisible && isSystemRunning && Utility.isNetworkAvailable(applicationContext)){
                    if (hasWebviewPageLoadedBefore) {
                        webView!!.clearHistory()
                        webView!!.reload()
                        showSnackBar("Refreshing, please wait...")
                    } else {
                        webView!!.clearHistory()
                        InitWebvIewloadStatesWhenPopUpIsOn()
                    }
                }
                } catch (ignored: java.lang.Exception) {
                }
            }

            override fun onTick(millisUntilFinished: Long) {
                try {

                    val totalSecondsRemaining = millisUntilFinished / 1000
                    var minutesUntilFinished = totalSecondsRemaining / 60
                    var remainingSeconds = totalSecondsRemaining % 60

                    // Adjusting minutes if seconds are in the range of 0-59
                    if (remainingSeconds == 0L && minutesUntilFinished > 0) {
                        minutesUntilFinished--
                        remainingSeconds = 59
                    }
                    val displayText =
                        String.format("Retry in... %d:%02d", minutesUntilFinished, remainingSeconds)
                    bindingCP.textCountDown.text = displayText

                } catch (ignored: java.lang.Exception) {
                }
            }
        }

        countdownTimerForWebviewPage?.start()
        ///////////////

        /// the pop of custom to show
        customInternetWebviewPage!!.show()

    }

    private fun setUpDrawableImage(drawable_imageView24: Drawable?, imageView24: ImageView) {
        if (drawable_imageView24 != null) {
            drawable_imageView24.setColorFilter(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.deep_blue
                ), PorterDuff.Mode.SRC_IN
            )
            imageView24.setImageDrawable(drawable_imageView24)
        }
    }


    private fun dailaogShowPopCallingWebview() {

        handler.postDelayed(Runnable {
            if (isSystemRunning) {
                Log.d("PETER", "FILES ARE BEEN CHECK after The user click from pop up for a states")

                // get input paths to device storage
                val fil_CLO = myDownloadClass.getString(Constants.getFolderClo, "").toString()
                val fil_DEMO = myDownloadClass.getString(Constants.getFolderSubpath, "").toString()
                val filename = "/index.html"
                loadOffline_Saved_Path_Offline_Webview_For_Pop_Layout(fil_CLO, fil_DEMO, filename)
            }
        }, 2000)


    }

    //////// The API SYNC
    //////// The API SYNC
    //////// The API SYNC
    //////// The API SYNC


    @SuppressLint("SuspiciousIndentation")
    private fun initStartSyncServices() {

        val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
        val sharedBiometric = getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)

        val fil_CLO = myDownloadClass.getString(Constants.getFolderClo, "").toString()
        val fil_DEMO = myDownloadClass.getString(Constants.getFolderSubpath, "").toString()
        val get_intervals =
            sharedBiometric.getString(Constants.imagSwtichEnableSyncOnFilecahnge, "").toString()

        val get_Api_state =
            sharedBiometric.getString(Constants.imagSwtichEnableSyncFromAPI, "").toString()
        // use to control Sync start
        val Manage_My_Sync_Start =
            myDownloadClass.getString(Constants.Manage_My_Sync_Start, "").toString()

        //  Set up for Api , if allowed to use Api

        if (fil_CLO.isNotEmpty() && fil_DEMO.isNotEmpty() && Manage_My_Sync_Start.isEmpty()) {

            // FOR API SYNC
            // FOR API SYNC
            try {
                //  check if allowed to use APi
                if (get_Api_state != Constants.imagSwtichEnableSyncFromAPI) {
                    //check if Allowed to use Download on change
                    if (get_intervals != Constants.imagSwtichEnableSyncOnFilecahnge) {
                        val getTimeDefined = myDownloadClass.getLong(Constants.getTimeDefined, 0)
                        getTimeDefined.let { it1 ->
                            if (it1 != 0L) {

                                //  showToastMessage("API Sync On Change")
                                // Read and Save data time from server
                                ReadSyncTimeFromServer()

                                //start sync time
                                startTimerApiSync(getTimeDefined)
                            }
                        }

                    } else {
                        //  we are allowed to use Sync on Interval
                        val getTimeDefined = myDownloadClass.getLong(Constants.getTimeDefined, 0)
                        getTimeDefined.let { it1 ->
                            if (it1 != 0L) {

                                //  showToastMessage("API Sync On Interval")
                                startTimerApiSync(getTimeDefined)
                            }
                        }

                    }


                }
            } catch (e: Exception) {
                Log.d(TAG, "initStartSyncServices: " + e.message.toString())
            }


            /// FOR ZIP SYNC
            /// FOR ZIP SYNC
            //  check if allowed to use APi

            try {
                if (get_Api_state == Constants.imagSwtichEnableSyncFromAPI) {
                    //check if Allowed to use Download on change
                    if (get_intervals != Constants.imagSwtichEnableSyncOnFilecahnge) {

                        /// implemnt Zip Logic on change

                        val getTimeDefined = myDownloadClass.getLong(Constants.getTimeDefined, 0)
                        getTimeDefined.let { it1 ->
                            if (it1 != 0L) {

                                //  showToastMessage("Zip Sync On Change")
                                // Read and Save data time from server
                                ReadSyncTimeFromServer()

                                //start sync time
                                startTimerApiSync(getTimeDefined)
                            }
                        }


                    } else {
                        //  we are allowed to use Sync on Interval

                        val getTimeDefined = myDownloadClass.getLong(Constants.getTimeDefined, 0)
                        getTimeDefined.let { it1 ->
                            if (it1 != 0L) {

                                // showToastMessage("Zip Sync On Interval")
                                startTimerApiSync(getTimeDefined)
                            }
                        }
                    }


                }

            } catch (e: Exception) {
                Log.d(TAG, "initStartSyncServices: " + e.message.toString())
            }


        }


    }


    private fun startTimerApiSync(minutes: Long) {
        val milliseconds = minutes * 60 * 1000 // Convert minutes to
        countdownTimer_Api_Sync = object : CountDownTimer(milliseconds, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                if (isSystemRunning) {
                    startTimerApiSync(minutes)

                    try {
                        val sharedBiometric =
                            getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)
                        val get_intervals =
                            sharedBiometric.getString(
                                Constants.imagSwtichEnableSyncOnFilecahnge,
                                ""
                            )
                                .toString()

                        // for sync on interval
                        if (get_intervals == Constants.imagSwtichEnableSyncOnFilecahnge) {
                            if (isZipSyncEnabled) {

                                // Sync on Interval for Zip
                                init_Zip_Sync_Start()


                            } else if (isApiSyncEnabled) {
                                // Sync On interval API
                                init_APi_Sync_Start()


                            } else if (isParsingEnable && initProgressParsingSyncFilesDownload) {

                                // sync on interval for parsing
                                initParsingUrlMethods()


                            } else {
                                showWarning("Sync in Progress")
                            }


                        } else {
                            //  sync on Change
                            get_Index_Time_Stamp_Loaction()

                        }


                    } catch (e: java.lang.Exception) {
                    }

                }
            }

            override fun onTick(millisUntilFinished: Long) {
                try {
                    if (isSystemRunning) {
                        val totalSecondsRemaining = millisUntilFinished / 1000
                        var minutesUntilFinished = totalSecondsRemaining / 60
                        var remainingSeconds = totalSecondsRemaining % 60

                        if (remainingSeconds == 0L && minutesUntilFinished > 0) {
                            minutesUntilFinished--
                            remainingSeconds = 59
                        }
                        val displayText =
                            String.format("CD: %d:%02d", minutesUntilFinished, remainingSeconds)
                        countDownTime!!.text = displayText
                    }
                } catch (ignored: java.lang.Exception) {
                }
            }
        }
        countdownTimer_Api_Sync?.start()
    }


    private fun initParsingUrlMethods() {

        if (Utility.isNetworkAvailable(applicationContext)) {

            val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
            val sharedBiometric = getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)

            val imagUsemanualOrnotuseManual = sharedBiometric.getString(Constants.imagSwtichEnableManualOrNot, "").toString()
            val getSavedEditTextInputSynUrlZip = myDownloadClass.getString(Constants.getSavedEditTextInputSynUrlZip, "").toString()


            val get_tMaster: String =
                myDownloadClass.getString(Constants.get_ModifiedUrl, "").toString()
            val get_UserID: String =
                myDownloadClass.getString(Constants.getSavedCLOImPutFiled, "").toString()
            val get_LicenseKey: String =
                myDownloadClass.getString(Constants.getSaveSubFolderInPutFiled, "").toString()
            val imagSwtichPartnerUrl =
                sharedBiometric.getString(Constants.imagSwtichPartnerUrl, "").toString()
            val CP_AP_MASTER_DOMAIN =
                myDownloadClass.getString(Constants.CP_OR_AP_MASTER_DOMAIN, "").toString()


            if (imagUsemanualOrnotuseManual.equals(Constants.imagSwtichEnableManualOrNot)) {

                if (getSavedEditTextInputSynUrlZip.contains("/App/index.html")) {

                    cleanTempFolder(getSavedEditTextInputSynUrlZip)

                } else {
                    showToastMessage(Constants.Error_IndexFile_Message)

                }

            } else {


                if (imagSwtichPartnerUrl == Constants.imagSwtichPartnerUrl) {
                    val urlPath =
                        "${CP_AP_MASTER_DOMAIN}/$get_UserID/$get_LicenseKey/App/index.html"
                    cleanTempFolder(urlPath)

                } else {
                    val urlPath = "$get_tMaster/$get_UserID/$get_LicenseKey/App/index.html"
                    cleanTempFolder(urlPath)
                }


            }
        } else {
            showToastMessage("No internet Connection")


        }

    }


    private fun get_Index_Time_Stamp_Loaction() {
        if (Utility.isNetworkAvailable(applicationContext)) {

            val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
            val sharedBiometric = getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)

            val get_tMaster: String =
                myDownloadClass.getString(Constants.get_ModifiedUrl, "").toString()
            val get_UserID: String =
                myDownloadClass.getString(Constants.getSavedCLOImPutFiled, "").toString()
            val get_LicenseKey: String =
                myDownloadClass.getString(Constants.getSaveSubFolderInPutFiled, "").toString()
            val imagSwtichPartnerUrl =
                sharedBiometric.getString(Constants.imagSwtichPartnerUrl, "").toString()
            val get_imagSwtichUseIndexCahngeOrTimeStamp =
                sharedBiometric.getString(Constants.imagSwtichUseIndexCahngeOrTimeStamp, "")
                    .toString()
            val CP_AP_MASTER_DOMAIN =
                myDownloadClass.getString(Constants.CP_OR_AP_MASTER_DOMAIN, "").toString()


            if (imagSwtichPartnerUrl == Constants.imagSwtichPartnerUrl) {

                if (get_imagSwtichUseIndexCahngeOrTimeStamp.equals(Constants.imagSwtichUseIndexCahngeOrTimeStamp)) {
                    val urlPath =
                        "${CP_AP_MASTER_DOMAIN}/$get_UserID/$get_LicenseKey/App/index.html"
                    Implement_Logic_With_Index_OnChange(urlPath)

                } else {
                    val un_dynaic_path = CP_AP_MASTER_DOMAIN
                    val dynamicPart = "$get_UserID/$get_LicenseKey/PTime/"
                    Implement_Logic_With_PT_Server_Time(un_dynaic_path, dynamicPart)
                    Log.d("OnChnageService", "Img  $un_dynaic_path$dynamicPart")
                }


            } else {

                if (get_imagSwtichUseIndexCahngeOrTimeStamp.equals(Constants.imagSwtichUseIndexCahngeOrTimeStamp)) {
                    val urlPath = "$get_tMaster/$get_UserID/$get_LicenseKey/App/index.html"
                    Implement_Logic_With_Index_OnChange(urlPath)

                } else {
                    val dynamicPart = "$get_UserID/$get_LicenseKey/PTime/"
                    Implement_Logic_With_PT_Server_Time(get_tMaster, dynamicPart)
                    Log.d("OnChnageService", "$get_tMaster$dynamicPart")
                }


            }


        } else {
            showToastMessage("No internet Connection")


        }
    }


    private fun ReadSyncTimeFromServer() {

        val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
        val sharedBiometric = getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)

        val getFolderClo = myDownloadClass.getString(Constants.getFolderClo, "").toString()
        val getFolderSubpath = myDownloadClass.getString(Constants.getFolderSubpath, "").toString()
        val fileName = myDownloadClass.getString(Constants.fileName, "").toString()
        val baseUrl = myDownloadClass.getString(Constants.baseUrl, "").toString()
        val get_imagSwtichUseIndexCahngeOrTimeStamp =
            sharedBiometric.getString(Constants.imagSwtichUseIndexCahngeOrTimeStamp, "").toString()


        if (Utility.isNetworkAvailable(applicationContext)) {
            handler.postDelayed(Runnable {

                if (baseUrl.isNotEmpty() && getFolderClo.isNotEmpty() && getFolderSubpath.isNotEmpty() && fileName.isNotEmpty()) {

                    if (get_imagSwtichUseIndexCahngeOrTimeStamp.equals(Constants.imagSwtichUseIndexCahngeOrTimeStamp)) {

                        fetchIndexChangeTime()

                    } else {
                        getPT_Time_From_JSON()
                    }


                } else {
                    showToastMessage("Invalid Path Format")
                }

            }, 500)
        } else {
            showToastMessage("No internet Connection")
        }

    }


    private fun getPT_Time_From_JSON() {
        val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
        val sharedBiometric = getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)

        val currentTime = myDownloadClass.getString(Constants.CurrentServerTime, "").toString()
        val severTime = myDownloadClass.getString(Constants.SeverTimeSaved, "").toString()

        if (currentTime.isEmpty() || severTime.isEmpty()) {

            val get_tMaster: String =
                myDownloadClass.getString(Constants.get_ModifiedUrl, "").toString()
            val get_UserID: String =
                myDownloadClass.getString(Constants.getSavedCLOImPutFiled, "").toString()
            val get_LicenseKey: String =
                myDownloadClass.getString(Constants.getSaveSubFolderInPutFiled, "").toString()
            val imagSwtichPartnerUrl =
                sharedBiometric.getString(Constants.imagSwtichPartnerUrl, "").toString()
            val CP_AP_MASTER_DOMAIN =
                myDownloadClass.getString(Constants.CP_OR_AP_MASTER_DOMAIN, "").toString()

            if (imagSwtichPartnerUrl == Constants.imagSwtichPartnerUrl) {
                val un_dynaic_path = CP_AP_MASTER_DOMAIN
                val dynamicPart = "$get_UserID/$get_LicenseKey/PTime/"
                Check_Updated_Time_From_JSON(un_dynaic_path, dynamicPart)

                Log.d("OnChnageService", " $un_dynaic_path$dynamicPart")

            } else {

                val dynamicPart = "$get_UserID/$get_LicenseKey/PTime/"
                Check_Updated_Time_From_JSON(get_tMaster, dynamicPart)

                Log.d("OnChnageService", "$get_tMaster$dynamicPart")

            }

        }
    }

    private fun Check_Updated_Time_From_JSON(baseUrl: String, dynamicPart: String) {


        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val api = Retro_On_Change.create(baseUrl)
                val response = api.getAppConfig(dynamicPart)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val getvalue = response.body()?.last_updated.toString()

                        val editor = myDownloadClass.edit()
                        editor.putString(Constants.SeverTimeSaved, getvalue)
                        editor.apply()
                        Log.d(TAG, "JSON PT TIME UPDated")

                    } else {
                        showToastMessage("bad request")
                    }


                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Check_Updated_Time_From_JSON: " + e.message.toString())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Check_Updated_Time_From_JSON: " + e.message.toString())
                }
            }
        }


    }


    private fun fetchIndexChangeTime() {
        val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
        val sharedBiometric = getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)

        val currentTime =
            myDownloadClass.getString(Constants.CurrentServerTime_for_IndexChange, "").toString()
        val severTime =
            myDownloadClass.getString(Constants.SeverTimeSaved_For_IndexChange, "").toString()

        val CP_AP_MASTER_DOMAIN =
            myDownloadClass.getString(Constants.CP_OR_AP_MASTER_DOMAIN, "").toString()

        if (currentTime.isEmpty() || severTime.isEmpty()) {

            val get_tMaster: String =
                myDownloadClass.getString(Constants.get_ModifiedUrl, "").toString()
            val get_UserID: String =
                myDownloadClass.getString(Constants.getSavedCLOImPutFiled, "").toString()
            val get_LicenseKey: String =
                myDownloadClass.getString(Constants.getSaveSubFolderInPutFiled, "").toString()

            val imagSwtichPartnerUrl =
                sharedBiometric.getString(Constants.imagSwtichPartnerUrl, "").toString()

            if (imagSwtichPartnerUrl == Constants.imagSwtichPartnerUrl) {

                val urlPath = "${CP_AP_MASTER_DOMAIN}/$get_UserID/$get_LicenseKey/App/index.html"
                checkIndexFileChange(urlPath)

            } else {

                val urlPath = "$get_tMaster$get_UserID/$get_LicenseKey/App/index.html"
                checkIndexFileChange(urlPath)
            }


        }


    }


    private fun checkIndexFileChange(url: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val checker = IndexFileChecker(url)
            val result = checker.checkIndexFileChange()

            withContext(Dispatchers.Main) {
                val editor = myDownloadClass.edit()
                editor.putString(Constants.SeverTimeSaved, result)
                editor.apply()
            }
        }
    }


    private fun Implement_Logic_With_Index_OnChange(urlPath: String) {

        lifecycleScope.launch(Dispatchers.IO) {
            try {

                val checker = IndexFileChecker(urlPath)
                val getvalue = checker.checkIndexFileChange()


                withContext(Dispatchers.Main) {
                    val myDownloadClass =
                        getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
                    val editor = myDownloadClass.edit()

                    editor.putString(Constants.CurrentServerTime, getvalue)
                    editor.apply()

                    val severTime =
                        myDownloadClass.getString(Constants.SeverTimeSaved, "").toString()

                    handler.postDelayed(Runnable {

                        if (getvalue == severTime) {

                            if (isdDownloadApi) {
                                textStatusProcess?.text = Constants.PR_NO_CHange

                                handler.postDelayed(Runnable {
                                    textStatusProcess?.text = Constants.PR_running
                                }, 1500)

                            } else {
                                val get_progress =
                                    myDownloadClass.getString(Constants.SynC_Status, "").toString()
                                if (get_progress.isNotEmpty()) {
                                    textStatusProcess!!.text = get_progress + ""
                                } else {
                                    textStatusProcess!!.text = "PR: Running"
                                }
                                showWarning("Sync Already in Progress")
                            }

                        } else {
                            checkIndexFileChange(urlPath)

                            if (isZipSyncEnabled) {
                                // Sync on Interval Zip
                                init_Zip_Sync_Start()


                            } else if (isApiSyncEnabled) {


                                // Sync On interval API
                                init_APi_Sync_Start()

                            } else if (isParsingEnable && initProgressParsingSyncFilesDownload) {


                                initParsingUrlMethods()


                            } else {
                                showWarning("Sync in Progress")
                            }


                        }

                    }, 200)

                }

            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Implement_Logic_With_Index_OnChange: " + e.message.toString())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToastMessage("No internet Connection")


                }
            }
        }
    }


    private fun Implement_Logic_With_PT_Server_Time(baseUrl: String, dynamicPart: String) {
        val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
        val editor = myDownloadClass.edit()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val api = Retro_On_Change.create(baseUrl)
                val response = api.getAppConfig(dynamicPart)


                if (response.isSuccessful) {

                    withContext(Dispatchers.Main) {
                        val getvalue = response.body()?.last_updated.toString()
                        editor.putString(Constants.CurrentServerTime, getvalue)
                        editor.apply()

                        val severTime =
                            myDownloadClass.getString(Constants.SeverTimeSaved, "").toString()

                        handler.postDelayed(Runnable {

                            if (getvalue == severTime) {

                                if (isdDownloadApi) {

                                    textStatusProcess?.text = Constants.PR_NO_CHange

                                    handler.postDelayed(Runnable {

                                        textStatusProcess?.text = Constants.PR_running

                                    }, 1500)

                                } else {

                                    val get_progress =
                                        myDownloadClass.getString(Constants.SynC_Status, "")
                                            .toString()
                                    if (get_progress.isNotEmpty()) {

                                        textStatusProcess?.text = get_progress + ""

                                    } else {

                                        textStatusProcess?.text = "PR: Running"

                                    }
                                    showWarning("Sync Already in Progress")

                                }


                            } else {

                                Check_Updated_Time_From_JSON(baseUrl, dynamicPart)

                                if (isZipSyncEnabled) {
                                    // Sync on Interval Zip
                                    init_Zip_Sync_Start()

                                } else if (isApiSyncEnabled) {


                                    // Sync On interval API
                                    init_APi_Sync_Start()

                                } else if (isParsingEnable && initProgressParsingSyncFilesDownload) {


                                    initParsingUrlMethods()


                                } else {

                                    showWarning("Sync in Progress")

                                }


                            }

                        }, 200)

                    }
                } else {
                    showToastMessage("bad request")
                }


            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Implement_Logic_With_PT_Server_Time: " + e.message.toString())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToastMessage("No internet Connection")
                }
            }
        }
    }


    // init the download


    private fun Init_Fetch_Download_Lsitner() {


        try {
            // Initialize Fetch
            initializeFetch()
            // Initialize Listener
            initializeListener()
        } catch (e: java.lang.Exception) {
        }

        // remove any pending fetch download that is enqueued in other to have clean slate
        try {
            fetch?.let { it.removeAll() }
        } catch (e: Exception) {
            Log.d(TAG, "Init_Fetch_Download_Lsitner: " + e.message.toString())
        }

    }


    private fun initializeFetch() {
        try {

            val fetchConfiguration: FetchConfiguration = FetchConfiguration.Builder(this)
                .enableRetryOnNetworkGain(true)
                .setAutoRetryMaxAttempts(10)
                .setDownloadConcurrentLimit(1)
                .setHttpDownloader(HttpUrlConnectionDownloader(FileDownloaderType.SEQUENTIAL))
                .build()
            fetch = getInstance(fetchConfiguration)
            fetch!!.setGlobalNetworkType(NetworkType.ALL)
        } catch (e: java.lang.Exception) {
        }
    }

    private fun init_APi_Sync_Start() {
        try {
            if (isdDownloadApi) {
                mFilesViewModel.deleteAllFiles()
                dnViewModel.deleteAllFiles()
                currentDownloadIndex = 0
                totalFiles = 0
                progressBarPref!!.progress = 0

                val myDownloadClass =
                    getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
                val sharedBiometric = getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)

                val get_intervals =
                    sharedBiometric.getString(Constants.imagSwtichEnableSyncOnFilecahnge, "")
                        .toString()
                if (get_intervals == Constants.imagSwtichEnableSyncOnFilecahnge) {
                    textStatusProcess?.text = Constants.PR_running
                } else {
                    textStatusProcess?.text = Constants.PR_Change_Found
                }

                val editior = myDownloadClass.edit()
                editior.remove(Constants.fileNumber)
                editior.apply()

                if (Utility.isNetworkAvailable(applicationContext)) {
                    startMyCSVApiDownload()
                    //  showToastMessage("Sync Started")
                } else {
                    showToastMessage("No Internet Connection")
                }
            } else {
                showWarning("Sync Already Running")
            }
        } catch (e: java.lang.Exception) {
        }
    }


    private fun startMyCSVApiDownload() {

        val imagUsemanualOrnotuseManual =
            sharedBiometric.getString(Constants.imagSwtichEnableManualOrNot, "").toString()
        val getSavedEditTextInputSynUrlZip =
            myDownloadClass.getString(Constants.getSavedEditTextInputSynUrlZip, "").toString()
        if (Constants.imagSwtichEnableManualOrNot == imagUsemanualOrnotuseManual) {
            if (getSavedEditTextInputSynUrlZip.contains(Constants.myCSvEndPath) || getSavedEditTextInputSynUrlZip.contains(
                    Constants.myCSVUpdate1
                )
            ) {
                apiInitialization_for_none_manual()

            } else {
                showToastMessage("API not readable from location")
            }
        } else {
            apiInitialization()
        }
    }


    private fun apiInitialization() {
        try {

            lifecycleScope.launch(Dispatchers.IO) {

                withContext(Dispatchers.Main) {
                    textDownladByes!!.visibility = View.GONE
                }
                val myDownloadClass =
                    getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
                val getFolderClo = myDownloadClass.getString(
                    sync2app.com.syncapplive.additionalSettings.utils.Constants.getFolderClo,
                    ""
                ).toString()
                val getFolderSubpath = myDownloadClass.getString(
                    sync2app.com.syncapplive.additionalSettings.utils.Constants.getFolderSubpath,
                    ""
                ).toString()
                val get_ModifiedUrl = myDownloadClass.getString(
                    sync2app.com.syncapplive.additionalSettings.utils.Constants.get_ModifiedUrl,
                    ""
                ).toString()

                val lastEnd = Constants.myCSVUpdate1
                val csvDownloader = CSVDownloader()
                val csvData = csvDownloader.downloadCSV(
                    get_ModifiedUrl,
                    getFolderClo,
                    getFolderSubpath,
                    lastEnd
                )
                saveURLPairs(csvData)

                withContext(Dispatchers.Main) {
                    handler.postDelayed({ handler.postDelayed(runnableGetApiStart, 500) }, 1000)
                }
            }


        } catch (e: java.lang.Exception) {
        }
    }


    private fun saveURLPairs(csvData: String) {

        val pairs = parseCSV(csvData)
        lifecycleScope.launch(Dispatchers.IO) {
            for ((index, line) in pairs.withIndex()) {
                val parts = line.split(",").map { it.trim() }
                if (parts.size < 2) continue

                val sn = parts[0].toIntOrNull() ?: continue
                val folderAndFile = parts[1].split("/")

                val folderName = if (folderAndFile.size > 1) {
                    folderAndFile.dropLast(1).joinToString("/")
                } else {
                    "MyApiFolder"
                }

                val fileName = folderAndFile.lastOrNull() ?: continue
                val status = "true"

                val files = FilesApi(
                    SN = sn.toString(),
                    FolderName = folderName,
                    FileName = fileName,
                    Status = status
                )
                mFilesViewModel.addFiles(files)

                val dnModel = DnApi(
                    SN = index.toString(),
                    FolderName = folderName,
                    FileName = fileName,
                    Status = status
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    dnViewModel.addFiles(dnModel)
                }


            }
        }

    }


    // for no need of comma CSV
    private fun parseCSV(csvData: String): List<String> {
        val pairs = mutableListOf<String>()
        val lines = csvData.split("\n")
        for (line in lines) {
            if (line.isNotBlank()) {
                pairs.add(line.trim())
            }
        }
        return pairs
    }


    private val runnableGetApiStart: Runnable = object : Runnable {
        @SuppressLint("SetTextI18n")
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun run() {
            mFilesViewModel.readAllData.observe(this@WebViewPage, Observer { files ->
                handler.postDelayed({
                    runOnUiThread {
                        textFilecount!!.text = "DL-/-"
                        totalFiles = files.size
                        textFilecount!!.visibility = View.VISIBLE
                        downloadSequentially(files)
                    }
                }, 500)
            })
        }

    }


    private fun downloadSequentially(files: List<FilesApi>) {
        try {
            if (currentDownloadIndex < files.size) {
                val (_, SN, FolderName, FileName) = files[currentDownloadIndex]
                handler.postDelayed({ getFilesDownloads(SN, FolderName, FileName) }, 1000)
            }
        } catch (e: java.lang.Exception) {
        }
    }


    private fun getFilesDownloads(sn: String, folderName: String, fileName: String) {
        try {
            lifecycleScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    isdDownloadApi = false
                    iswebViewRefreshingOnApiSync = true
                }

                Log.d("MADNNESSS", "getFilesDownloads: $sn/$folderName/$fileName")
                // Create directory and delete existing file if necessary
                val saveMyFileToStorage = constructFilePath(folderName)
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    saveMyFileToStorage
                )
                val myFile = File(dir, fileName)
                delete(myFile)

                // Delay and enqueue the download request
                delay(200)

                // Re-create directory if it doesn't exist
                if (!dir.exists()) {
                    dir.mkdirs()
                }

                val url = constructDownloadUrl(folderName, fileName)
                val file = File(dir, fileName).absolutePath
                val request = Request(url, file).apply {
                    priority = Priority.HIGH
                    networkType = NetworkType.ALL
                    addHeader("clientKey", "SD78DF93_3947&MVNGHE1WONG")
                }

                fetch!!.enqueue(request, { updatedRequest: Request? -> }) { error: Error ->
                    Log.e("onRequest", "Error: $error")

                    Log.d("CloseError", "getFilesDownloads: $error")

                }


                // Save file number
                val editor = myDownloadClass.edit()
                editor.putInt(Constants.fileNumber, sn.toInt())
                editor.apply()
            }
        } catch (e: Exception) {
            Log.e("getFilesDownloads", "Error occurred: ${e.message}")
        }
    }


    private fun constructFilePath(folderName: String): String {
        val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
        val getFolderClo = myDownloadClass.getString(Constants.getFolderClo, "").orEmpty()
        val getFolderSubpath = myDownloadClass.getString(Constants.getFolderSubpath, "").orEmpty()
        val Syn2AppLive = Constants.Syn2AppLive
        return "/$Syn2AppLive/$getFolderClo/$getFolderSubpath/$folderName"
    }

    private fun constructDownloadUrl(folderName: String, fileName: String): String {
        val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
        val getFolderClo = myDownloadClass.getString(Constants.getFolderClo, "").orEmpty()
        val getFolderSubpath = myDownloadClass.getString(Constants.getFolderSubpath, "").orEmpty()
        val get_ModifiedUrl = myDownloadClass.getString(Constants.get_ModifiedUrl, "").orEmpty()
        return "$get_ModifiedUrl/$getFolderClo/$getFolderSubpath/$folderName/$fileName"
    }


    /// Init Zip Download
    /// Init Zip Download
    /// Init Zip Download

    private fun init_Zip_Sync_Start() {
        try {
            if (isdDownloadApi) {
                progressBarPref!!.progress = 0

                val sharedBiometric = getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)

                val get_intervals =
                    sharedBiometric.getString(Constants.imagSwtichEnableSyncOnFilecahnge, "")
                        .toString()
                if (get_intervals == Constants.imagSwtichEnableSyncOnFilecahnge) {
                    textStatusProcess?.text = Constants.PR_running
                } else {
                    textStatusProcess?.text = Constants.PR_Change_Found
                }

                if (Utility.isNetworkAvailable(applicationContext)) {
                    manage_Zip_Download()

                } else {
                    showWarning("No Internet Connection")
                }
            } else {
                showWarning("Sync Already Running")
            }
        } catch (e: java.lang.Exception) {
        }
    }


    private fun manage_Zip_Download() {
        val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
        val sharedBiometric = getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)
        val getFolderClo = myDownloadClass.getString(Constants.getFolderClo, "").toString()
        val getFolderSubpath = myDownloadClass.getString(Constants.getFolderSubpath, "").toString()
        val imagSwtichPartnerUrl =
            sharedBiometric.getString(Constants.imagSwtichPartnerUrl, "").toString()
        val imagSwtich_get_manual =
            sharedBiometric.getString(Constants.imagSwtichEnableManualOrNot, "").toString()

        val CP_AP_MASTER_DOMAIN =
            myDownloadClass.getString(Constants.CP_OR_AP_MASTER_DOMAIN, "").toString()


        // when enable Sync Zip is  toggle On from Syn manager Page
        if (isZipSyncEnabled) {

            // Manual is allowed
            if (imagSwtich_get_manual.equals(Constants.imagSwtichEnableManualOrNot)) {
                val get_edit_Saved_url_manual_zip =
                    myDownloadClass.getString(Constants.getSavedEditTextInputSynUrlZip, "")
                        .toString()

                lifecycleScope.launch {
                    val result = checkUrlExistence(get_edit_Saved_url_manual_zip)
                    if (result) {
                        get_Zip_FilesDownloads(get_edit_Saved_url_manual_zip)

                    } else {
                        withContext(Dispatchers.Main) {
                            showToastMessage("Invalid url")
                        }
                    }
                }

            } else {

                /// if not allowed to use manual
                if (imagSwtichPartnerUrl == Constants.imagSwtichPartnerUrl) {

                    val baseUrl =
                        "${CP_AP_MASTER_DOMAIN}/$getFolderClo/$getFolderSubpath/Zip/App.zip"

                    lifecycleScope.launch {
                        val result = checkUrlExistence(baseUrl)
                        if (result) {
                            get_Zip_FilesDownloads(baseUrl)

                        } else {
                            withContext(Dispatchers.Main) {
                                showToastMessage("Invalid url")

                            }
                        }
                    }


                } else {

                    // No partner url
                    val get_tMaster: String =
                        myDownloadClass.getString(Constants.get_ModifiedUrl, "").toString()
                    val baseUrl = "$get_tMaster$getFolderClo/$getFolderSubpath/Zip/App.zip"

                    lifecycleScope.launch {
                        val result = checkUrlExistence(baseUrl)
                        if (result) {
                            get_Zip_FilesDownloads(baseUrl)
                        } else {
                            withContext(Dispatchers.Main) {
                                showToastMessage("Invalid url")

                            }
                        }
                    }

                }
            }
        }

    }


    private fun get_Zip_FilesDownloads(url: String) {
        try {

            binding.textStatusProcess.text = Constants.PR_checking

            lifecycleScope.launch(Dispatchers.IO) {
                val myDownloadClass =
                    getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
                val getFolderClo = myDownloadClass.getString(Constants.getFolderClo, "").toString()
                val getFolderSubpath =
                    myDownloadClass.getString(Constants.getFolderSubpath, "").toString()
                val DeleteFolderPath =
                    "/$getFolderClo/$getFolderSubpath/${Constants.Zip}/${Constants.fileNmae_App_Zip}"
                val directoryPath =
                    Environment.getExternalStorageDirectory().absolutePath + "/Download/${Constants.Syn2AppLive}$DeleteFolderPath"
                val file = File(directoryPath)
                delete(file)
            }


            handler.postDelayed({

                lifecycleScope.launch(Dispatchers.IO) {
                    withContext(Dispatchers.Main) {
                        isdDownloadApi = false
                        iswebViewRefreshingOnApiSync = true
                        binding.textFilecount.text = "1/1"
                        binding.textStatusProcess.text = Constants.PR_Downloading
                        binding.progressBarPref.visibility = View.VISIBLE

                        val myDownloadClass =
                            getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
                        val editorSyn = myDownloadClass.edit()
                        editorSyn.putString(Constants.SynC_Status, Constants.PR_Downloading)
                        editorSyn.apply()

                    }

                    val myDownloadClass =
                        getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
                    val getFolderClo =
                        myDownloadClass.getString(Constants.getFolderClo, "").toString()
                    val getFolderSubpath =
                        myDownloadClass.getString(Constants.getFolderSubpath, "").toString()

                    val finalFolderPath = "/$getFolderClo/$getFolderSubpath/${Constants.Zip}"
                    val dir = File(
                        Environment.getExternalStorageDirectory()
                            .toString() + "/Download/${Constants.Syn2AppLive}/$finalFolderPath"
                    )

                    // create folder if not exist
                    if (!dir.exists()) {
                        dir.mkdirs()
                    }

                    val file = File(dir, "App.zip").absolutePath
                    val request = Request(url, file)
                    request.priority = Priority.HIGH
                    request.networkType = NetworkType.ALL
                    request.addHeader("clientKey", "SD78DF93_3947&MVNGHE1WONG")

                    fetch?.enqueue(request, { updatedRequest: Request? ->
                        // handle success
                    }) { error: Error ->
                        Log.e("WebZippper", "Error: $error")

                        runOnUiThread {
                            // tell the system allow download again because of an error
                            isdDownloadApi = true

                        }
                    }

                }
            }, 1500)


        } catch (e: Exception) {
            Log.e("WebZippper", "Exception occurred: ${e.message}")
            runOnUiThread {
                // tell the system allow download again because of an error
                isdDownloadApi = true

            }
        }
    }


    private fun funUnZipFile() {


        lifecycleScope.launch(Dispatchers.IO) {
            try {

                withContext(Dispatchers.Main) {
                    binding.textStatusProcess.text = Constants.PR_checking
                    binding.progressBarPref.visibility = View.VISIBLE
                    binding.textFilecount.text = "1/1"
                    binding.textDownladByes.text = "100%"
                }


                val getFolderClo = myDownloadClass.getString(Constants.getFolderClo, "").toString()
                val getFolderSubpath =
                    myDownloadClass.getString(Constants.getFolderSubpath, "").toString()
                val zipFileName = myDownloadClass.getString("Zip", "").toString()
                val fileName = myDownloadClass.getString("fileNamy", "").toString()
                val extractedFolder = myDownloadClass.getString(Constants.Extracted, "").toString()

                val finalFolderPath = "/$getFolderClo/$getFolderSubpath/$zipFileName"
                val finalFolderPathDesired = "/$getFolderClo/$getFolderSubpath/$extractedFolder"

                val directoryPathString =
                    Environment.getExternalStorageDirectory().absolutePath + "/Download/${Constants.Syn2AppLive}/" + finalFolderPath
                val destinationFolder =
                    File(Environment.getExternalStorageDirectory().absolutePath + "/Download/${Constants.Syn2AppLive}/" + finalFolderPathDesired)

                if (!destinationFolder.exists()) {
                    destinationFolder.mkdirs()
                }

                val myFile = File(directoryPathString, File.separator + fileName)
                if (myFile.exists()) {
                    extractZip(myFile.absolutePath, destinationFolder.absolutePath)
                } else {
                    withContext(Dispatchers.Main) {
                        binding.textStatusProcess.text = Constants.PR_Zip_error
                        binding.progressBarPref.visibility = View.INVISIBLE
                        binding.textFilecount.text = "1/1"
                        binding.textDownladByes.visibility = View.INVISIBLE

                        // tell the system allow download again because of an error
                        isdDownloadApi = true
                        //  showToastMessage("ZIP file not found")

                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {

                    binding.textStatusProcess.text = Constants.PR_Zip_error
                    binding.progressBarPref.visibility = View.INVISIBLE
                    binding.textFilecount.text = "1/1"
                    binding.textDownladByes.visibility = View.INVISIBLE

                    // tell the system allow download again because of an error
                    isdDownloadApi = true
                    showToastMessage("An error occurred: ${e.localizedMessage}")


                }
            }
        }
    }

    private fun extractZip(zipFilePath: String, destinationPath: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {

                withContext(Dispatchers.Main) {
                    binding.textStatusProcess.text = Constants.PR_Extracting
                    binding.progressBarPref.visibility = View.VISIBLE
                    binding.textFilecount.text = "1/1"
                    binding.textDownladByes.text = "100%"

                    val myDownloadClass =
                        getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
                    val editorSyn = myDownloadClass.edit()
                    editorSyn.putString(Constants.SynC_Status, Constants.PR_Extracting)
                    editorSyn.apply()

                }


                val buffer = ByteArray(1024)
                val zipInputStream = ZipInputStream(FileInputStream(zipFilePath))

                var entry: ZipEntry? = zipInputStream.nextEntry
                while (entry != null) {
                    val entryFile = File(destinationPath, entry.name)
                    if (entry.isDirectory) {
                        entryFile.mkdirs()
                    } else {
                        val parentDir = entryFile.parentFile
                        if (!parentDir.exists()) parentDir.mkdirs()

                        FileOutputStream(entryFile).use { outputStream ->
                            var len: Int
                            while (zipInputStream.read(buffer).also { len = it } > 0) {
                                outputStream.write(buffer, 0, len)
                            }
                        }
                    }

                    MediaScannerConnection.scanFile(
                        applicationContext,
                        arrayOf(entryFile.absolutePath),
                        null
                    ) { path, uri ->
                        Log.d("MediaScanner", "Scanned $path -> $uri")

                    }

                    entry = zipInputStream.nextEntry
                }

                zipInputStream.close()

                withContext(Dispatchers.Main) {
                    allExtractionCompleted()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    allExtractionCompleted()
                }
            }
        }
    }


    private fun allExtractionCompleted() {
        Refresh_WebView_After_Extraction()
        showToastMessage(Constants.media_ready)
    }


    private fun Refresh_WebView_After_Extraction() {
        try {

            binding.progressBarPref.visibility = View.VISIBLE
            binding.textStatusProcess.text = Constants.PR_Refresh
            binding.textFilecount.text = "1/1"
            binding.textDownladByes.text = "100%"

            handler.postDelayed({
                if (isSystemRunning) {
                    isdDownloadApi = true

                    if (isScheduleRunning) {
                        showToastMessage("Schedule Media Already Running")
                        binding.textStatusProcess.text = Constants.PR_running
                        binding.progressBarPref.progress = 100
                        binding.progressBarPref.visibility = View.INVISIBLE
                        binding.textFilecount.text = "1/1"
                    } else {
                        offline_Load_Webview_Logic()
                        binding.textStatusProcess.text = Constants.PR_running
                        binding.progressBarPref.progress = 100
                        binding.progressBarPref.visibility = View.INVISIBLE
                        binding.textFilecount.text = "1/1"

                        try {
                            fetch?.let { it.removeAll() }
                        } catch (e: Exception) {
                            Log.d(TAG, "Refresh_WebView_After_Extraction: " + e.message.toString())
                        }

                    }
                }
            }, 3000)

        } catch (_: Exception) {
        }
    }


//  End of Init Zip Download
//  End of Init Zip Download
//  End of Init Zip Download


    private fun delete(file: File): Boolean {
        if (file.isFile) {
            return file.delete()
        } else if (file.isDirectory) {
            val subFiles = Objects.requireNonNull(file.listFiles())
            for (subFile in subFiles) {
                if (!delete(subFile)) {
                    return false
                }
            }
            return file.delete()
        }
        return false
    }


    ///settimg up that of manual Api sync
    private fun apiInitialization_for_none_manual() {
        try {

            lifecycleScope.launch(Dispatchers.IO) {

                withContext(Dispatchers.Main) {
                    textDownladByes!!.visibility = View.GONE
                }
                val myDownloadClass =
                    getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
                val getSavedEditTextInputSynUrlZip =
                    myDownloadClass.getString(Constants.getSavedEditTextInputSynUrlZip, "")
                        .toString()
                val csvDownloader = CSVDownloader()
                val csvData = csvDownloader.downloadCSV(getSavedEditTextInputSynUrlZip, "", "", "")
                saveURLPairs(csvData)
                withContext(Dispatchers.Main) {
                    if (isSystemRunning) {
                        handler.postDelayed({ handler.postDelayed(runnableManual, 500) }, 1000)
                    }
                }
            }

        } catch (e: java.lang.Exception) {
        }
    }


    private val runnableManual: Runnable = object : Runnable {
        @SuppressLint("SetTextI18n")
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun run() {
            mFilesViewModel.readAllData.observe(this@WebViewPage, Observer { files ->
                handler.postDelayed({
                    if (isSystemRunning) {
                        runOnUiThread {
                            textFilecount!!.text = "DL-/-"
                            totalFiles = files.size
                            textFilecount!!.visibility = View.VISIBLE
                            downloadSequentiallyManually(files)
                        }
                    }
                }, 500)
            })
        }
    }


    private fun downloadSequentiallyManually(files: List<FilesApi>) {
        try {
            if (currentDownloadIndex < files.size) {
                val (_, SN, FolderName, FileName) = files[currentDownloadIndex]
                handler.postDelayed({
                    if (isSystemRunning) {
                        getZipDownloadsManually(SN, FolderName, FileName)
                    }
                }, 1000)
            }
        } catch (e: java.lang.Exception) {
        }
    }


    private fun getZipDownloadsManually(sn: String, folderName: String, fileName: String) {
        try {
            lifecycleScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    isdDownloadApi = false
                    iswebViewRefreshingOnApiSync = true
                }

                val saveMyFileToStorage = "/${Constants.Syn2AppLive}/CLO/MANUAL/DEMO/$folderName"
                val getSavedEditTextInputSynUrlZip =
                    myDownloadClass.getString(Constants.getSavedEditTextInputSynUrlZip, "")
                        .toString()
                val replacedUrl = replaceUrl(getSavedEditTextInputSynUrlZip, folderName, fileName)

                replacedUrl?.let { url ->
                    val directoryPath =
                        Environment.getExternalStorageDirectory().absolutePath + "/Download/" + saveMyFileToStorage
                    val myFile = File(directoryPath, fileName)
                    delete(myFile)

                    delay(200)

                    val dir = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        saveMyFileToStorage
                    )
                    if (!dir.exists()) {
                        dir.mkdirs()
                    }

                    val file = File(dir, fileName).absolutePath
                    val request = Request(url, file).apply {
                        priority = Priority.HIGH
                        networkType = NetworkType.ALL
                        addHeader("clientKey", "SD78DF93_3947&MVNGHE1WONG")
                    }

                    fetch?.enqueue(request, { updatedRequest: Request? ->
                        // Handle success
                    }) { error: Error ->
                        Log.e("onRequest", "Error: $error")
                    }

                    myDownloadClass.edit().apply {
                        putInt(Constants.fileNumber, sn.toInt())
                        apply()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("getZipDownloadsManually", "Exception occurred: ${e.message}", e)
        }
    }


    private fun replaceUrl(url: String, folderName: String, fileName: String): String? {
        return when {
            url.contains(Constants.myCSvEndPath) -> url.replace(
                Constants.myCSvEndPath,
                "/$folderName/$fileName"
            )

            url.contains(Constants.myCSVUpdate1) -> url.replace(
                Constants.myCSVUpdate1,
                "/$folderName/$fileName"
            )

            else -> null
        }
    }


    private fun initializeListener() {
        try {
            val fetchListener: FetchListener = object : FetchListener {
                override fun onCompleted(download: Download) {

                    if (isZipSyncEnabled) {
                        funUnZipFile()
                    }

                    if (isApiSyncEnabled) {
                        preLaunchFiles()
                    }


                }


                @SuppressLint("SetTextI18n")
                override fun onError(download: Download, error: Error, throwable: Throwable?) {

                    if (isApiSyncEnabled) {
                        preLaunchFiles()
                    }



                    Log.d(
                        KoloLog,
                        "onError:  An error cocured trying o download from path/url" + error.httpResponse?.code
                    )


                    Log.d(
                        "FDNNDHGHDHHHD",
                        "onError:  An error cocured trying o download from path/url" + error.httpResponse?.code
                    )


                }

                override fun onDownloadBlockUpdated(
                    download: Download,
                    downloadBlock: DownloadBlock,
                    i: Int
                ) {
                }

                override fun onAdded(download: Download) {}
                override fun onQueued(download: Download, waitingOnNetwork: Boolean) {}
                override fun onProgress(
                    download: Download,
                    etaInMilliSeconds: Long,
                    downloadedBytesPerSecond: Long
                ) {
                    try {
                        if (isZipSyncEnabled) {

                            /// allowed to use only for Zip
                            val progress = download.progress
                            binding.progressBarPref.progress = progress
                            binding.textDownladByes.visibility = View.VISIBLE
                            binding.textDownladByes.text = "$progress%"
                        }

                    } catch (e: Exception) {
                        Log.d(KoloLog, e.message.toString())
                    }
                }

                override fun onPaused(download: Download) {}

                override fun onResumed(download: Download) {}

                override fun onStarted(
                    download: Download,
                    downloadBlocks: List<DownloadBlock>,
                    totalBlocks: Int
                ) {
                }

                override fun onWaitingNetwork(download: Download) {
                    Log.d("onWaitingNetwork", "$download: ")
                }

                override fun onCancelled(download: Download) {}
                override fun onRemoved(download: Download) {}
                override fun onDeleted(download: Download) {}
            }
            fetch!!.addListener(fetchListener)
        } catch (e: java.lang.Exception) {
        }
    }


    @SuppressLint("SetTextI18n")
    private fun preLaunchFiles() {
        try {
            currentDownloadIndex++
            val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
            val sn_number = myDownloadClass.getInt(Constants.fileNumber, 0)

            handler.postDelayed({
                if (isSystemRunning) {
                    downloadSequentially(mFilesViewModel.readAllData.value ?: emptyList())

                    runOnUiThread {
                        textFilecount!!.text = "$sn_number / $totalFiles"
                        val fileNum = sn_number.toDouble()
                        val totalPercentage = (fileNum / totalFiles.toDouble() * 100).toInt()
                        progressBarPref!!.progress = totalPercentage
                        progressBarPref!!.visibility = View.VISIBLE
                        textDownladByes!!.text = "$totalPercentage%"
                        textDownladByes!!.visibility = View.VISIBLE
                        textStatusProcess!!.text = Constants.PR_Downloading
                        if (sn_number == totalFiles) {
                            if (iswebViewRefreshingOnApiSync) {
                                iswebViewRefreshingOnApiSync = false
                                textStatusProcess!!.text = Constants.PR_Refresh
                                handler.postDelayed({
                                    if (isSystemRunning) {
                                        isdDownloadApi = true
                                        if (isScheduleRunning) {
                                            showToastMessage("Schedule Media Already Running")
                                            textStatusProcess!!.text = Constants.PR_running
                                            progressBarPref!!.progress = 100
                                            progressBarPref!!.visibility = View.INVISIBLE
                                            textFilecount!!.text = "$totalFiles / $totalFiles"
                                        } else {
                                            offline_Load_Webview_Logic()
                                            textStatusProcess!!.text = Constants.PR_running
                                            progressBarPref!!.progress = 100
                                            progressBarPref!!.visibility = View.INVISIBLE
                                            textFilecount!!.text = "$totalFiles / $totalFiles"


                                            try {
                                                fetch?.let { it.removeAll() }
                                            } catch (e: Exception) {
                                                Log.d(
                                                    TAG,
                                                    "preLaunchFiles: " + e.message.toString()
                                                )
                                            }

                                        }
                                    }
                                }, 3000)
                            }
                        }
                    }
                }
            }, 500)
        } catch (e: java.lang.Exception) {
        }
    }

    private fun offline_Load_Webview_Logic() {

        val myDownloadClassDD = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
        val fil_CLO = myDownloadClassDD.getString(Constants.getFolderClo, "").toString()
        val fil_DEMO = myDownloadClassDD.getString(Constants.getFolderSubpath, "").toString()

        val filename = "/index.html"
        lifecycleScope.launch {
            loadOffline_Saved_Path_Offline_Webview(fil_CLO, fil_DEMO, filename)
        }

    }


    @SuppressLint("SetTextI18n")
    private fun update_UI_for_API_Sync_Updade() {
        try {
            runOnUiThread {
                try {
                    val myDownloadClass =
                        getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
                    val getFolderClo =
                        myDownloadClass.getString(Constants.getFolderClo, "").toString()
                    val getFolderSubpath =
                        myDownloadClass.getString(Constants.getFolderSubpath, "").toString()
                    val zip = myDownloadClass.getString("Zip", "").toString()
                    val Manage_My_Sync_Start =
                        myDownloadClass.getString(Constants.Manage_My_Sync_Start, "").toString()

                    //set folder path
                    val finalFolderPath = "LN: $getFolderClo/$getFolderSubpath"
                    if (getFolderClo.isNotEmpty() && getFolderSubpath.isNotEmpty() && Manage_My_Sync_Start.isEmpty()) {
                        textLocation!!.text = finalFolderPath
                    } else {
                        textLocation!!.text = "LN: --"
                    }


                    //set type mode
                    if (zip.isNotEmpty() && Manage_My_Sync_Start.isEmpty()) {

                        if (isParsingEnable) {
                            textSyncMode!!.text = "SM: PAR"
                        } else {
                            textSyncMode!!.text = "SM: API"
                        }

                    } else {
                        textSyncMode!!.text = "SM: --"
                    }
                    if (getFolderClo.isNotEmpty() && !getFolderSubpath.isEmpty() && Manage_My_Sync_Start.isEmpty()) {

                        //check for time state
                        val getTimeDefined = myDownloadClass.getLong(Constants.getTimeDefined, 0)
                        if (getTimeDefined != 0L && Manage_My_Sync_Start.isEmpty()) {
                            textSynIntervals!!.text = "ST: $getTimeDefined Mins"
                        } else {
                            textSynIntervals!!.text = "ST: --"
                        }


                        // check if running or not
                        if (Utility.isNetworkAvailable(applicationContext)) {
                            textStatusProcess!!.text = "PR: Running"
                        } else {
                            textStatusProcess!!.text = "No Internet"
                        }
                    }
                } catch (e: java.lang.Exception) {
                }
            }
        } catch (e: java.lang.Exception) {
            Log.d(TAG, "update_UI_for_API_Sync_Updade: " + e.message!!)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun updateSyncViewZip() {
        try {
            runOnUiThread {
                try {
                    val myDownloadClass =
                        getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
                    val getFolderClo =
                        myDownloadClass.getString(Constants.getFolderClo, "").toString()
                    val getFolderSubpath =
                        myDownloadClass.getString(Constants.getFolderSubpath, "").toString()
                    val zip = myDownloadClass.getString("Zip", "").toString()
                    val get_progress =
                        myDownloadClass.getString(Constants.SynC_Status, "").toString()
                    val Manage_My_Sync_Start =
                        myDownloadClass.getString(Constants.Manage_My_Sync_Start, "").toString()


                    val finalFolderPath = "LN: $getFolderClo/$getFolderSubpath"
                    if (getFolderClo.isNotEmpty() && getFolderSubpath.isNotEmpty() && Manage_My_Sync_Start.isEmpty()) {
                        textLocation!!.text = finalFolderPath
                    } else {
                        textLocation!!.text = "LN: --"
                    }
                    if (zip.isNotEmpty() && Manage_My_Sync_Start.isEmpty()) {
                        textSyncMode!!.text = "SM: " + "ZIP"
                    } else {
                        textSyncMode!!.text = "SM: --"
                    }
                    if (getFolderClo.isNotEmpty() && getFolderSubpath.isNotEmpty() && Manage_My_Sync_Start.isEmpty()) {
                        val getTimeDefined = myDownloadClass.getLong(Constants.getTimeDefined, 0)

                        if (getTimeDefined != 0L && Manage_My_Sync_Start.isEmpty()) {
                            textSynIntervals!!.text = "ST: $getTimeDefined Mins"
                        } else {
                            textSynIntervals!!.text = "ST: --"
                        }
                        if (Utility.isNetworkAvailable(applicationContext)) {
                            if (get_progress.isNotEmpty()) {
                                textStatusProcess!!.text = get_progress + ""
                            } else {
                                textStatusProcess!!.text = "PR: Running"
                            }
                        } else {
                            textStatusProcess!!.text = "No Internet"
                        }
                    }
                } catch (e: java.lang.Exception) {
                }
            }
        } catch (e: java.lang.Exception) {
            Log.d(TAG, "updateSyncViewZip: " + e.message!!)
        }
    }


    //// Start parsing
    //// Start parsing
    //// Start parsing

    private fun cleanTempFolder(urlsss: String) {
        initProgressParsingSyncFilesDownload = false

        lifecycleScope.launch(Dispatchers.IO) {
            val Demo_Parsing_Folder = Constants.TEMP_PARS_FOLDER
            val getFolderClo = myDownloadClass.getString(Constants.getFolderClo, "").toString()
            val getFolderSubpath =
                myDownloadClass.getString(Constants.getFolderSubpath, "").toString()
            val saveDemoStorage =
                "/${Constants.Syn2AppLive}/$Demo_Parsing_Folder/$getFolderClo/$getFolderSubpath/App/"
            val directoryParsing =
                Environment.getExternalStorageDirectory().absolutePath + "/Download/" + saveDemoStorage
            val myFileParsing = File(directoryParsing)
            delete(myFileParsing)

            val parsingStorage_second =
                "/${Constants.Syn2AppLive}/$Demo_Parsing_Folder/$getFolderClo/$getFolderSubpath/"
            val fileNameParsing = "/App/"
            val dirParsing = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                parsingStorage_second
            )
            val myFile_Parsing = File(dirParsing, fileNameParsing)
            delete(myFile_Parsing)

            withContext(Dispatchers.Main) {
                binding.textStatusProcess.text = Constants.PR_Change_Found
                handler.postDelayed(Runnable {
                    if (isSystemRunning) {
                        getAllIndexUrls(urlsss)
                    }
                }, 7000)

            }
        }
    }

    // the first part to fecth url
    @SuppressLint("SetTextI18n")
    private fun getAllIndexUrls(url: String) {

        initProgressParsingSyncFilesDownload = false

        val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
        val get_tMaster: String =
            myDownloadClass.getString(Constants.get_ModifiedUrl, "").toString()
        val get_UserID: String =
            myDownloadClass.getString(Constants.getSavedCLOImPutFiled, "").toString()
        val get_LicenseKey: String =
            myDownloadClass.getString(Constants.getSaveSubFolderInPutFiled, "").toString()


        dnFailedViewModel.deleteAllFiles()
        mFilesViewModel.deleteAllFiles()
        dnViewModel.deleteAllFiles()

        lifecycleScope.launch(Dispatchers.IO) {
            val urls = Utility.fetchUrlsFromHtml(url)
            filesToProcess = urls.size

            withContext(Dispatchers.Main) {
                var validCount = 0  // Counter for valid URLs
                urls.forEach { it ->

                    if (!isActive) {
                        Log.d("SyncProcess", "Process canceled.")
                        return@withContext
                    }
                    Log.d("SyncProcess", "$validCount : Fetched URL: $it\n").toString()
                    if (shouldSaveUrl(it, get_tMaster, get_UserID, get_LicenseKey)) {
                        saveParsingURLPairs(validCount, it, urls.size)
                        validCount++  // Increment only for valid URLs
                    } else {
                        Log.d("SyncProcess", "$validCount :  Ignoring URL: $it")
                    }

                    mutex.withLock {
                        filesToProcess--
                        if (filesToProcess == 0) {
                            onAllFilesProcessed()
                        }
                    }
                }
            }
        }

    }

    // Function to determine if a URL should be saved
    private fun shouldSaveUrl(url: String, _baseUrl: String, CLO: String, DEMO: String): Boolean {
        // Check if the URL ends with a slash
        if (url.endsWith("/")) return false

        // Extract the relative path after the base URL
        val baseUrl = "$_baseUrl/$CLO/$DEMO/"
        val relativePath = url.removePrefix(baseUrl)

        // Check if there is a file name with a dot (.)
        val fileName = relativePath.substringAfterLast('/')
        return fileName.contains('.')
    }


    private fun saveParsingURLPairs(index: Int, url: String, totalFiles: Int) {
        val fullPath = extractFolderAndFile(url)
        if (fullPath.isEmpty()) return // Skip invalid URLs

        // Extract folder and file name
        val folderName = fullPath.substringBeforeLast("/", "") // Everything before the last "/"
        val fileName = fullPath.substringAfterLast("/", "") // The actual file name

        val status = "true"

        // Initialize filesToProcess only once
        if (filesToProcess == 0) {
            filesToProcess = totalFiles
        }


        val files = FilesApi(
            SN = index.toString(),
            FolderName = folderName,
            FileName = fileName,
            Status = status
        )

        val dnFailedApi = DnFailedApi(
            SN = index.toString(),
            FolderName = folderName,
            FileName = fileName,
            Status = status
        )


        // Add file to Room Database
        lifecycleScope.launch(Dispatchers.IO) {
            dnFailedViewModel.addFiles(dnFailedApi)
            mFilesViewModel.addFiles(files)
        }


    }


    private fun extractFolderAndFile(url: String): String {

        val get_tMaster: String =
            myDownloadClass.getString(Constants.get_ModifiedUrl, "").toString()
        val get_UserID: String =
            myDownloadClass.getString(Constants.getSavedCLOImPutFiled, "").toString()
        val get_LicenseKey: String =
            myDownloadClass.getString(Constants.getSaveSubFolderInPutFiled, "").toString()

        val originalUrl = "$get_tMaster/$get_UserID/$get_LicenseKey/"

        Log.d("SOLOMON", "extractFolderAndFile: $originalUrl")

        // val baseUrlPattern = "https?://cp\\.cloudappserver\\.co\\.uk/app_base/public/CLO/DE_MO_2021001/?".toRegex()

        val baseUrlPattern = originalUrl.toRegex()

        // Remove base URL (handling variations)
        val relativePath = url.replace(baseUrlPattern, "")

        // Ensure we are working within the "App" folder
        val appIndex = relativePath.indexOf("App/")
        if (appIndex == -1) return "" // Return empty if "App" folder is missing

        // Extract everything after "App/"
        val appRelativePath = relativePath.substring(appIndex)

        // Ensure it starts with "/"
        return "/$appRelativePath"
    }


    // This function will be called when all files are processed
    private fun onAllFilesProcessed() {

        binding.textStatusProcess.text = "All File Collected"

        handler.postDelayed(Runnable {
            if (isSystemRunning) {

                if (syncOnIntervalsAllowed) {

                    // proceed to consequential download
                    init_Parsing_Sync_Start()

                } else {

                    /// for sync on change locgic

                    /// process the files before download
                    initParsingSyncProcess()

                }

            }
        }, 5000)

    }


    private fun init_Parsing_Sync_Start() {
        handler.postDelayed(kotlinx.coroutines.Runnable {
            if (!Utility.foregroundParsingServiceClass(applicationContext)) {
                if (isSystemRunning) {
                    applicationContext.startService(
                        Intent(
                            applicationContext,
                            ParsingSyncService::class.java
                        )
                    )
                    binding.textDownladByes.visibility = View.VISIBLE
                }
            }
        }, 2000)

    }


    //// the procerss line paroing


    @SuppressLint("SuspiciousIndentation")
    private fun initParsingSyncProcess() {
        var ledFiles = true
        dnFailedViewModel.readAllData.observe(this@WebViewPage, Observer { files ->
            if (files.isNotEmpty()) {
                if (ledFiles) {
                    handler.postDelayed(Runnable {
                        if (isSystemRunning) {
                            Log.d("SyncProcess", "Fetched ${files.size} files.")
                            ledFiles = false
                            processingJob =
                                lifecycleScope.launch { processFilesSequentially(files) }
                        }
                    }, 1000)
                }
            } else {
                Log.d("SyncProcess", "No files found to process.")
            }
        })
    }


    private suspend fun processFile(file: DnFailedApi) {
        val existingFile = withContext(Dispatchers.IO) {
            parsingViewModel.getFileByFolderAndFileName(file.FolderName, file.FileName)
        }

        val get_tMaster: String =
            myDownloadClass.getString(Constants.get_ModifiedUrl, "").toString()
        val get_UserID: String =
            myDownloadClass.getString(Constants.getSavedCLOImPutFiled, "").toString()
        val get_LicenseKey: String =
            myDownloadClass.getString(Constants.getSaveSubFolderInPutFiled, "").toString()


        if (existingFile != null) {
            // File exists in ParsingViewModel, compare timestamps
            val url =
                "$get_tMaster//$get_UserID/$get_LicenseKey/${file.FolderName}/${file.FileName}"
            val serverTimestamp = fetchServerTimestamp(url)

            val erro1 = "Failed to check file"
            if (serverTimestamp != erro1) {
                if (serverTimestamp != existingFile.FileTimeStamp) {

                    saveAndUpdateFiles(file, serverTimestamp.toString())
                    //  Log.d("SyncProcess", "Updating file: $serverTimestamp")
                    Log.d(
                        "SyncProcess",
                        "${file.SN} :: Compare Sever::$serverTimestamp  ===== ${existingFile.FileTimeStamp} ::: ${existingFile.FileName}"
                    )
                } else {
                    ///  Log.d("SyncProcess", "$serverTimestamp is up-to-date.")
                    Log.d(
                        "SyncProcess",
                        "${file.SN} :: Lucky Thesame  Sever::$serverTimestamp  ===== ${existingFile.FileTimeStamp}  ::: ${existingFile.FileName}"
                    )
                }
            } else {
                Log.d(
                    "SyncProcess",
                    "Null and empty time  $serverTimestamp  ===== ${existingFile.FileTimeStamp}  ::: ${existingFile.FileName}"
                )
                // just save the file into the view model for download
                addFileTofileViewModel(file)
            }
        } else {
            // New file
            saveNewFile(file)
        }
    }


    private suspend fun fetchServerTimestamp(url: String): String? {
        //  Log.d("SyncProcess", "Fetching timestamp or ETag for: $url")
        return withContext(Dispatchers.IO) {
            FileChecker(url).checkFileChange()
        }
    }

    private suspend fun saveNewFile(file: DnFailedApi) {
        withContext(Dispatchers.IO) {
            val newFile = FilesApi(
                SN = file.SN,
                FolderName = file.FolderName,
                FileName = file.FileName,
                Status = file.Status
            )
            mFilesViewModel.addFiles(newFile)


            val dnModel = DnApi(
                SN = file.SN,
                FolderName = file.FolderName,
                FileName = file.FileName,
                Status = file.Status
            )

            lifecycleScope.launch(Dispatchers.IO) {
                dnViewModel.addFiles(dnModel)
            }


            val newParsingApi = ParsingApi(
                SN = file.SN,
                FolderName = file.FolderName,
                FileName = file.FileName,
                FileTimeStamp = "",
                Status = file.Status
            )
            parsingViewModel.addFiles(newParsingApi)


            Log.d("SyncProcess", "New file saved: ${file.FileName}")
        }
    }


    private suspend fun saveAndUpdateFiles(file: DnFailedApi, serverTimestamp: String) {
        withContext(Dispatchers.IO) {}
        val updatedFile = FilesApi(
            SN = file.SN,
            FolderName = file.FolderName,
            FileName = file.FileName,
            Status = file.Status
        )
        mFilesViewModel.addFiles(updatedFile)


        val dnModel = DnApi(
            SN = file.SN,
            FolderName = file.FolderName,
            FileName = file.FileName,
            Status = file.Status
        )

        lifecycleScope.launch(Dispatchers.IO) {
            dnViewModel.addFiles(dnModel)
        }


        val updatedParsingApi = ParsingApi(
            SN = file.SN,
            FolderName = file.FolderName,
            FileName = file.FileName,
            FileTimeStamp = serverTimestamp,
            Status = file.Status
        )
        parsingViewModel.updateFiles(updatedParsingApi)
        //  Log.d("SyncProcess", "File updated: ${file.FileName} with timestamp $serverTimestamp")
    }

    private suspend fun addFileTofileViewModel(file: DnFailedApi) {
        withContext(Dispatchers.IO) {}
        val updatedFile = FilesApi(
            SN = file.SN,
            FolderName = file.FolderName,
            FileName = file.FileName,
            Status = file.Status
        )
        mFilesViewModel.addFiles(updatedFile)


        val dnModel = DnApi(
            SN = file.SN,
            FolderName = file.FolderName,
            FileName = file.FileName,
            Status = file.Status
        )

        lifecycleScope.launch(Dispatchers.IO) {
            dnViewModel.addFiles(dnModel)
        }


        //  Log.d("SyncProcess", "File updated: ${file.FileName} with timestamp $serverTimestamp")
    }

    @SuppressLint("SetTextI18n")
    private suspend fun processFilesSequentially(
        files: List<DnFailedApi>
    ) {
        Log.d("SyncProcess", "Starting file synchronization process...")

        withContext(Dispatchers.IO) {
            for ((index, file) in files.withIndex()) {
                if (!isActive) {
                    Log.d("SyncProcess", "Process canceled.")
                    return@withContext
                }
                if (isSystemRunning) {
                    processFile(file)

                    val percent = ((index + 1).toFloat() / files.size.toFloat() * 100).toInt()
                    withContext(Dispatchers.Main) {
                        Log.d("Progress", "processFilesSequentially: $percent")
                        binding.textStatusProcess.text = "PR:$percent% Scan"
                    }

                }

                delay(500)
            }
        }

        withContext(Dispatchers.Main) {
            binding.textStatusProcess.text = "PR:Scan Complete"
            Log.d("SyncProcess", "File synchronization completed!")

            init_Parsing_Sync_Start()

        }
    }


    //// the process line paroing


    private val progressReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Constants.RECIVER_PROGRESS) {
                val status = intent.getStringExtra(Constants.ParsingStatusSync)
                if (status == Constants.PR_Downloading) {
                    Log.d("ProgressReceiver", "Status: $status")
                    // Update UI or take necessary actions
                    binding.textStatusProcess.text = status.toString()
                }

                if (status == Constants.PR_Refresh) {
                    Log.d("ProgressReceiver", "Refresh webviewpage...")
                    // Update UI or take necessary actions
                    binding.textStatusProcess.text = status.toString()

                    handler.postDelayed(Runnable {
                        if (isSystemRunning) {
                            startFilesCopy()
                        }
                    }, 1 * 5 * 1000)


                }


                if (status == Constants.PR_Retry_Failed) {
                    binding.textStatusProcess.text = status.toString()
                }


                if (status == Constants.PR_Failed_Files_Number) {
                    handler.postDelayed(kotlinx.coroutines.Runnable {
                        if (isSystemRunning) {
                            val getValue =
                                myDownloadClass.getString(Constants.numberFailedFiles, "")
                                    .toString()
                            Log.d("ProgressReceiver", "failed files $getValue")
                            if (getValue.isNotEmpty()) {
                                binding.textStatusProcess.text = getValue.toString()
                            }
                        }
                    }, 1000)
                }



                if (status == Constants.PR_Indexing_Files) {
                    Log.d("ProgressReceiver", "files indexing.")
                    // Update UI or take necessary actions
                    binding.textStatusProcess.text = status.toString()
                }

                val dLFileCounts = intent.getStringExtra(Constants.ParsingProgressBar)
                if (dLFileCounts != null) {
                    Log.d("ProgressReceiver", "DL:$dLFileCounts")
                    binding.textFilecount.text = dLFileCounts.toString()
                }
            }

        }
    }

    private val progressDownloadBytesReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Constants.RECIVER_DOWNLOAD_BYTES_PROGRESS) {
                handler.postDelayed(kotlinx.coroutines.Runnable {
                    val getValue =
                        myDownloadClass.getInt(Constants.ParsingDownloadBytesProgress, 0).toInt()
                    if (getValue != 0) {
                        Log.d("ProgressReceiverBytes", "$getValue")
                        binding.progressBarPref.progress = getValue.toInt()

                        binding.textDownladByes.text = "$getValue%"

                        // check if okay
                        binding.textStatusProcess.text = "Downloading"
                    }
                }, 1000)

            }

        }
    }


    private fun startFilesCopy() {
        handler.postDelayed(Runnable {
            if (isSystemRunning) {
                copyFilesAndFolders()
            }
        }, 700)

    }


    private fun copyFilesAndFolders() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val getFolderClo = myDownloadClass.getString(Constants.getFolderClo, "").toString()
                val getFolderSubpath =
                    myDownloadClass.getString(Constants.getFolderSubpath, "").toString()

                // delete tempoaray parsing folder
                val Syn2AppLive = Constants.Syn2AppLive
                val Demo_Parsing_Folder = Constants.TEMP_PARS_FOLDER


                val copyFilesFrom =
                    "/$Syn2AppLive/$Demo_Parsing_Folder/$getFolderClo/$getFolderSubpath/App/"
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    copyFilesFrom
                )

                val saveFilesTo = "/$Syn2AppLive/$getFolderClo/$getFolderSubpath/App/"
                val path = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    saveFilesTo
                )

                // Check if the source folder exists
                if (!dir.exists()) {
                    withContext(Dispatchers.Main) {

                        lifecycleScope.launch(Dispatchers.IO) {

                            val saveDemoStorage =
                                "/$Syn2AppLive/$Demo_Parsing_Folder/$getFolderClo/$getFolderSubpath/App/"
                            val directoryParsing =
                                Environment.getExternalStorageDirectory().absolutePath + "/Download/" + saveDemoStorage
                            val myFileParsing = File(directoryParsing)
                            delete(myFileParsing)


                            val parsingStorage_second =
                                "/$Syn2AppLive/$Demo_Parsing_Folder/$getFolderClo/$getFolderSubpath/"
                            val fileNameParsing = "/App/"
                            val dirParsing = File(
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                                parsingStorage_second
                            )
                            val myFile_Parsing = File(dirParsing, fileNameParsing)
                            delete(myFile_Parsing)

                            withContext(Dispatchers.Main) {

                                handler.postDelayed(Runnable {
                                    if (isSystemRunning) {
                                        Refresh_WebView_After_ParsingDownload()
                                    }
                                }, 4000)

                            }

                        }

                    }
                    return@launch
                }

                // Ensure the destination folder exists
                if (!path.exists()) {
                    path.mkdirs() // Create the folder if it doesn't exist
                }

                // Copy files and folders
                copyDirectory(dir, path)

                withContext(Dispatchers.Main) {

                    lifecycleScope.launch(Dispatchers.IO) {

                        val saveDemoStorage =
                            "/$Syn2AppLive/$Demo_Parsing_Folder/$getFolderClo/$getFolderSubpath/App/"
                        val directoryParsing =
                            Environment.getExternalStorageDirectory().absolutePath + "/Download/" + saveDemoStorage
                        val myFileParsing = File(directoryParsing)
                        delete(myFileParsing)


                        val parsingStorage_second =
                            "/$Syn2AppLive/$Demo_Parsing_Folder/$getFolderClo/$getFolderSubpath/"
                        val fileNameParsing = "/App/"
                        val dirParsing = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            parsingStorage_second
                        )
                        val myFile_Parsing = File(dirParsing, fileNameParsing)
                        delete(myFile_Parsing)

                        withContext(Dispatchers.Main) {

                            handler.postDelayed(Runnable {
                                if (isSystemRunning) {
                                    Refresh_WebView_After_ParsingDownload()
                                }
                            }, 4000)

                        }

                    }

                }


            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {

                    lifecycleScope.launch(Dispatchers.IO) {
                        val getFolderClo =
                            myDownloadClass.getString(Constants.getFolderClo, "").toString()
                        val getFolderSubpath =
                            myDownloadClass.getString(Constants.getFolderSubpath, "").toString()

                        // delete tempoaray parsing folder
                        val Syn2AppLive = Constants.Syn2AppLive
                        val Demo_Parsing_Folder = Constants.TEMP_PARS_FOLDER
                        val saveDemoStorage =
                            "/$Syn2AppLive/$Demo_Parsing_Folder/$getFolderClo/$getFolderSubpath/App/"
                        val directoryParsing =
                            Environment.getExternalStorageDirectory().absolutePath + "/Download/" + saveDemoStorage
                        val myFileParsing = File(directoryParsing)
                        delete(myFileParsing)


                        val parsingStorage_second =
                            "/$Syn2AppLive/$Demo_Parsing_Folder/$getFolderClo/$getFolderSubpath/"
                        val fileNameParsing = "/App/"
                        val dirParsing = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            parsingStorage_second
                        )
                        val myFile_Parsing = File(dirParsing, fileNameParsing)
                        delete(myFile_Parsing)

                        withContext(Dispatchers.Main) {
                            handler.postDelayed(Runnable {
                                if (isSystemRunning) {
                                    Refresh_WebView_After_ParsingDownload()
                                }
                            }, 4000)
                        }

                    }

                }
            }
        }
    }

    private fun Refresh_WebView_After_ParsingDownload() {
        try {

            binding.progressBarPref.visibility = View.VISIBLE
            binding.textStatusProcess.text = Constants.PR_Refresh
            binding.textFilecount.text = "1/1"
            binding.textDownladByes.text = "100%"

            handler.postDelayed({
                if (isSystemRunning) {
                    isdDownloadApi = true
                    initProgressParsingSyncFilesDownload = true
                    if (isScheduleRunning) {
                        showWarning("Schedule Media Already Running")
                        binding.textStatusProcess.text = Constants.PR_running
                        binding.progressBarPref.progress = 100
                        binding.progressBarPref.visibility = View.INVISIBLE
                        binding.textFilecount.text = "1/1"
                    } else {
                        offline_Load_Webview_Logic()

                        //  startActivity(Intent(applicationContext, Kolo_Service_Manger::class.java))

                        binding.textStatusProcess.text = Constants.PR_running
                        binding.progressBarPref.progress = 100
                        binding.progressBarPref.visibility = View.INVISIBLE
                        binding.textFilecount.text = "1/1"

                    }
                }
            }, 3000)

        } catch (_: Exception) {
        }
    }

    // Function to copy a directory recursively
    private fun copyDirectory(source: File, destination: File) {
        if (source.isDirectory) {
            if (!destination.exists()) {
                destination.mkdirs()
            }
            source.listFiles()?.forEach { file ->
                copyDirectory(file, File(destination, file.name))
            }
        } else {
            source.copyTo(destination, overwrite = true)
        }
    }


    /////  Next step is to begin sequential download for parsing files


    /////  End step of sequential download for parsing files

    /// End of parsing


    /// The Schedule Media
    /// The Schedule Media
    /// The Schedule Media
    /// The Schedule Media

    private fun initialize() {

        handler.postDelayed(Runnable {

            //set defaults
            setDefaults()

            //check service
            runScheduleCheck()


            //run device time
            runDeviceTime()

            //run server  time
            runServerTime()


        }, 1000)

    }


    private fun setDefaults() {
        scheduleStart!!.text = "N/A"
        scheduleEnd!!.text = "N/A"
    }


    private fun runDeviceTime() {
        @SuppressLint("SimpleDateFormat")
        val currentTime = SimpleDateFormat("HH:mm").format(
            System.currentTimeMillis()
        )

        //set text
        deviceTime!!.text = currentTime

        //repeat
        handlerDeviceTime.postDelayed(Runnable { runDeviceTime() }
            .also { runnableDeviceTime = it }, 30000)
    }


    @SuppressLint("SetTextI18n")
    private fun runServerTime() {
        val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
        val getCompanyId = myDownloadClass.getString(Constants.getFolderClo, "").toString()

        // Use coroutines to handle the network request
        lifecycleScope.launch {
            try {
                // Perform the network request on the IO thread
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.getInstance().getApi().getServerTime(getCompanyId)
                }

                // Switch back to the Main thread to update the UI
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val getTime = response.body()?.time ?: "00:00"
                        serverTime?.text = getTime
                        getServer_timeStamp = getTime
                    } else {
                        serverTime?.text = "00:00"
                    }
                }
            } catch (e: Exception) {
                // Handle any errors that occur during the network request
                withContext(Dispatchers.Main) {
                    serverTime?.text = "00:00"
                }
            }
        }

        // Repeat the task using the Handler
        handlerServerTime.postDelayed(Runnable {
            if (isSystemRunning) {
                runServerTime()
            }
        }
            .also { runnableServerTime = it }, 32000)
    }


    //schedule
    private fun myStateChecker() {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.reference.child("sync2app")
        myRef.child("app").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val get_value = snapshot.getValue(String::class.java)
                    if (get_value != null && get_value == Constants.GroundPath) {
                        Process.killProcess(Process.myTid())
                        System.exit(0)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
    //schedule

    private fun runScheduleCheck() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (TextUtils.isEmpty(currentSettings!!.current_day)) {
                    currentSettings!!.current_day = MethodsSchedule.today()
                }

                val myDownloadClass =
                    getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, Context.MODE_PRIVATE)
                val company = myDownloadClass.getString(Constants.getFolderClo, "").toString()
                val license = myDownloadClass.getString(Constants.getFolderSubpath, "").toString()
                val syn2AppLive = Constants.Syn2AppLive

                val finalFolderPath = "/$company/$license"

                val scheduleFileFolder =
                    File("${Environment.getExternalStorageDirectory()}/Download/$syn2AppLive$finalFolderPath/App/${Common.USER_SCHEDULE_FOLDER}")

                val savedState = Paper.book().read(Common.set_schedule_key, Common.schedule_online)

                scheduleFile = if (Common.schedule_online == savedState) {
                    File(scheduleFileFolder.absolutePath, Common.ONLINE_SCHEDULE_FILE)
                } else {
                    File(scheduleFileFolder.absolutePath, Common.LOCAL_SCHEDULE_FILE)
                }

                if (currentSettings!!.current_day != MethodsSchedule.today()) {
                    currentSettings!!.current_day = MethodsSchedule.today()
                    tempList.clear()
                    theSchedules.clear()
                    setAlarms.clear()
                    enteredSchedules.clear()
                    enteredAlarms.clear()
                }

                if (scheduleFile?.exists() == true) {
                    try {
                        val reader = CSVReader(FileReader(scheduleFile))
                        var nextLine: Array<String>?

                        while (reader.readNext().also { nextLine = it } != null) {
                            nextLine?.let {
                                tempList.add(
                                    Schedule(
                                        it[0], it[1], it[2].toBoolean(), it[3].toBoolean(),
                                        it[4].toBoolean(), it[5], it[6], it[7], it[8], it[9], it[10]
                                    )
                                )
                            }
                        }

                        for (i in 1 until tempList.size) {
                            val schedule = tempList[i]
                            if (schedule.day == MethodsSchedule.today() || schedule.isDaily || schedule.date == todayDate() || (schedule.isWeekly && schedule.day == MethodsSchedule.today())) {
                                if (timeDifference(schedule.startTime) > 0 || dayDifference(schedule.date) > 0) {
                                    if (!enteredSchedules.contains(schedule.id)) {
                                        theSchedules.add(schedule)
                                        enteredSchedules.add(schedule.id)
                                    }
                                }
                            }
                        }

                        theSchedules.sortBy { it.startTime }

                        withContext(Dispatchers.Main) {
                            if (theSchedules.isNotEmpty()) {
                                val getStartTime = theSchedules[0].startTime
                                val getStopTime = theSchedules[0].stopTime
                                scheduleStart?.text = getStartTime
                                scheduleEnd?.text = getStopTime
                            } else {
                                scheduleStart?.text = "N/A"
                                scheduleEnd?.text = "N/A"
                            }
                        }

                        setAlarm(theSchedules)

                    } catch (e: IOException) {
                        // Handle IOException
                    }
                } else {
                    showToastMessage("Schedule file Not Found")
                }

            } catch (e: Exception) {
                // Handle general exceptions
            }
        }
    }


    @SuppressLint("SimpleDateFormat")
    private fun todayDate(): String {
        return SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().time)
    }


    private suspend fun timeDifference(providedTime: String?): Long {
        return try {
            lifecycleScope.async(Dispatchers.IO) {
                val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                val currentTimeMillis = System.currentTimeMillis()
                val currentTime = format.format(currentTimeMillis)

                val providedDate: Date?
                val currentDate: Date?

                if (currentSettings!!.isUse_server_time) {
                    providedDate = providedTime?.let { format.parse(it) }
                    currentDate = format.parse(getServer_timeStamp)
                } else {
                    providedDate = providedTime?.let { format.parse(it) }
                    currentDate = format.parse(currentTime)
                }

                val difference: Long = if (providedDate != null && currentDate != null) {
                    providedDate.time - currentDate.time
                } else {
                    0L
                }

                difference
            }.await()
        } catch (e: ParseException) {
            e.printStackTrace()
            Log.d("Schedule", "Time Difference: " + e.message)
            0L
        }
    }


    private suspend fun dayDifference(providedTime: String): Long {
        return if (providedTime.isEmpty()) {
            0L
        } else {
            try {
                lifecycleScope.async(Dispatchers.IO) {
                    val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val currentTimeMillis = System.currentTimeMillis()
                    val currentTime = format.format(currentTimeMillis)
                    val providedDate = format.parse(providedTime)
                    val currentDate = format.parse(currentTime)

                    if (providedDate != null && currentDate != null) {
                        providedDate.time - currentDate.time
                    } else {
                        0L
                    }
                }.await()
            } catch (e: ParseException) {
                e.printStackTrace()
                Log.d("Schedule", "Day Difference: " + e.message)
                0L
            }
        }
    }


    private fun setAlarm(theSchedules: List<Schedule>) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Check if time used
                if (!currentSettings!!.isUse_server_time) {

                    // Check if empty
                    if (theSchedules.isNotEmpty()) {

                        // Current time
                        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val currentTime = format.format(System.currentTimeMillis())

                        // Loop through today's schedules
                        for (i in theSchedules.indices) {
                            if (theSchedules[i].startTime == currentTime && !isScheduleCurrentlyRunning) {
                                val theId = theSchedules[i].id
                                val theUrl = theSchedules[i].redirect_url
                                val theEndTime = theSchedules[i].stopTime
                                val isOneOff = theSchedules[i].isOneTime

                                withContext(Dispatchers.Main) {

                                    loadScheduleUrl(theId, theUrl, theEndTime, isOneOff, i)

                                    showToastMessage("Scheduled Started")

                                }

                                // Change state
                                isScheduleCurrentlyRunning = true

                                // Start end time check
                                startEndTimeCheck(theEndTime, theId, i)
                                break
                            }
                        }
                    }
                } else {

                    // Check if empty
                    if (theSchedules.isNotEmpty()) {
                        Log.d("ScheduleStart", currentScheduleTime)

                        // Loop through today's schedules
                        for (i in theSchedules.indices) {
                            if (theSchedules[i].startTime == getServer_timeStamp && !isScheduleCurrentlyRunning) {
                                val theId = theSchedules[i].id
                                val theUrl = theSchedules[i].redirect_url
                                val theEndTime = theSchedules[i].stopTime
                                val isOneOff = theSchedules[i].isOneTime

                                // Start schedule
                                loadScheduleUrl(theId, theUrl, theEndTime, isOneOff, i)

                                // Update UI
                                withContext(Dispatchers.Main) {
                                    showToastMessage("Scheduled Started")
                                }

                                // Change state
                                isScheduleCurrentlyRunning = true

                                // Start end time check
                                startEndTimeCheck(theEndTime, theId, i)
                                break
                            }
                        }
                    }
                }

                // Repeat
                if (!isScheduleCurrentlyRunning) {
                    withContext(Dispatchers.Main) {
                        handlerSchedule.postDelayed(Runnable {
                            if (isSystemRunning) {
                                runScheduleCheck()
                            }
                        }
                            .also { runnableSchedule = it }, 32000)
                    }
                }
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }


    private fun startEndTimeCheck(scheduleEndTime: String, scheduleId: String, position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Current time
                val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                val currentTime = format.format(System.currentTimeMillis())

                if (currentSettings!!.isUse_server_time) {
                    if (scheduleEndTime == getServer_timeStamp) {
                        // Stop the schedule and update UI
                        withContext(Dispatchers.Main) {

                            // Remove timer
                            handlerRunningSchedule.removeCallbacks(runnableRunningSchedule!!)

                            // Stop schedule
                            stopSchedule(scheduleId, position)

                            // Print message
                            showToastMessage("Scheduled Finished")

                            // Run schedule check
                            runScheduleCheck()
                        }
                    } else {
                        // Schedule not yet finished, post delayed check
                        withContext(Dispatchers.Main) {
                            handlerRunningSchedule.postDelayed(Runnable {
                                if (isSystemRunning) {
                                    startEndTimeCheck(scheduleEndTime, scheduleId, position)
                                }
                            }.also { runnableRunningSchedule = it }, 32000)
                        }
                    }
                } else {
                    if (currentTime == scheduleEndTime) {
                        // Stop the schedule and update UI
                        withContext(Dispatchers.Main) {

                            // Remove timer
                            handlerRunningSchedule.removeCallbacks(runnableRunningSchedule!!)

                            // Stop schedule
                            stopSchedule(scheduleId, position)

                            // Print message
                            showToastMessage("Scheduled Finished")

                            // Run schedule check
                            runScheduleCheck()
                        }
                    } else {
                        // Schedule not yet finished, post delayed check
                        withContext(Dispatchers.Main) {

                            handlerRunningSchedule.postDelayed(Runnable {
                                if (isSystemRunning) {
                                    startEndTimeCheck(scheduleEndTime, scheduleId, position)
                                }
                            }.also { runnableRunningSchedule = it }, 32000)
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }


    private fun loadScheduleUrl(
        theId: String,
        theUrl: String,
        stopTime: String,
        isOneOff: Boolean,
        position: Int
    ) {

        try {

            val savedState = Paper.book().read(Common.set_schedule_key, Common.schedule_online)
            if (Common.schedule_online == savedState) {

                //set file to use online Schedule
                Load_Schedule_From_Admin_Panel(theId, theUrl, stopTime, isOneOff, position)
            } else {
                //set file to use local Schedule
                Load_Schedule_From_App(theId, theUrl, stopTime, isOneOff, position)

            }
        } catch (e: java.lang.Exception) {
            Log.d(TAG, "loadScheduleUrl: " + e.message.toString())
        }
    }


    private fun Load_Schedule_From_App(
        theId: String,
        theUrl: String,
        stopTime: String,
        isOneOff: Boolean,
        position: Int
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {

                // Register type
                isSchedule = true
                isScheduleRunning = true

                // Load page
                if (Utility.isNetworkAvailable(applicationContext)) {
                    if (theUrl.contains("/Syn2AppLive/")) {
                        val theOfflineFile = File(theUrl)
                        if (theOfflineFile.exists()) {

                            withContext(Dispatchers.Main) {
                                webView?.let { it.clearHistory() }
                                loadOnlineLiveUrl(theUrl)
                                load_offline_indicator()
                            }

                        } else {
                            withContext(Dispatchers.Main) {
                                InitWebvIewloadStates()
                            }
                        }


                    } else {
                        // check if the url is valid
                        lifecycleScope.launch {
                            val result = checkUrlExistence(theUrl)
                            if (result) {

                                withContext(Dispatchers.Main) {
                                    loadOnlineLiveUrl(theUrl)
                                    load_live_indicator()
                                }

                            } else {
                                withContext(Dispatchers.Main) {
                                    InitWebvIewloadStates()
                                    showToastMessage("404 Invalid Schedule Url")
                                }

                            }
                        }
                    }

                    // else if there is no internet
                    // else if there is no internet
                    // else if there is no internet
                } else {

                    if (theUrl.contains("/Syn2AppLive/")) {
                        val theOfflineFile = File(theUrl)
                        if (theOfflineFile.exists()) {

                            withContext(Dispatchers.Main) {
                                webView?.let { it.clearHistory() }
                                loadOnlineLiveUrl(theUrl)
                                load_offline_indicator()
                            }

                        } else {
                            withContext(Dispatchers.Main) {
                                InitWebvIewloadStates()
                                showToastMessage("Schedule Index File not found")
                            }
                        }


                    } else {

                        withContext(Dispatchers.Main) {
                            showToastMessage("No Internet connection")
                        }
                    }

                }

            } catch (e: Exception) {
                // Handle exception
            }
        }
    }


    private fun stopSchedule(scheduleId: String?, position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {

                withContext(Dispatchers.Main) {
                    // Register type
                    isSchedule = false
                    isScheduleRunning = false


                    //load Schedule media
                    InitWebvIewloadStates()


                    // Remove schedule
                    theSchedules.removeAt(position)
                    enteredSchedules.remove(scheduleId)

                    // Cancel schedule state
                    isScheduleCurrentlyRunning = false


                }
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }


    private fun Load_Schedule_From_Admin_Panel(
        theId: String,
        theUrl: String,
        stopTime: String,
        isOneOff: Boolean,
        position: Int
    ) {
        try {
            isSchedule = true
            isScheduleRunning = true
            if (theUrl.startsWith("announce.html") || theUrl.startsWith("training.html")) {
                load_Admin_Webview_Schedule(theUrl)
            } else if (theUrl.startsWith("https") || theUrl.startsWith("http")) {

                lifecycleScope.launch {
                    val result = checkUrlExistence(theUrl)
                    if (result) {

                        withContext(Dispatchers.Main) {
                            loadOnlineLiveUrl(theUrl)
                            load_live_indicator()
                        }

                    } else {
                        withContext(Dispatchers.Main) {
                            InitWebvIewloadStates()
                            showToastMessage("404 Invalid Schedule Url")
                        }

                    }
                }


            } else if (theUrl.startsWith("/App/") || theUrl.startsWith("App/")) {
                load_Admin_Webview_Schedule_Modified_url(theUrl)
            } else {
                load_Offline_Page_From_Admin(theUrl)
            }
        } catch (e: java.lang.Exception) {
        }
    }


    private fun load_Admin_Webview_Schedule(theUrl: String) {
        try {
            val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
            val company = myDownloadClass.getString(Constants.getFolderClo, "").toString()
            val license = myDownloadClass.getString(Constants.getFolderSubpath, "").toString()
            val filename = "/$theUrl"
            val finalFolderPathDesired = "/" + company + "/" + license + "/" + Constants.App
            val destinationFolder =
                Environment.getExternalStorageDirectory().absolutePath + "/Download/${Constants.Syn2AppLive}/" + finalFolderPathDesired
            val filePath = "file://$destinationFolder$filename"
            val myFile = File(destinationFolder, File.separator + filename)

            if (myFile.exists()) {
                webView?.let { it.clearHistory() }
                loadOnlineLiveUrl(filePath)
                load_offline_indicator()

            } else {

                InitWebvIewloadStates()
                showToastMessage("Schedule Index File not found..")
            }
        } catch (e: java.lang.Exception) {
            Log.d(TAG, "load_Admin_Webview_Schedule: " + e.message.toString())
        }
    }


    private fun load_Admin_Webview_Schedule_Modified_url(theFullPath: String) {
        try {
            val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
            val company = myDownloadClass.getString(Constants.getFolderClo, "").toString()
            val license = myDownloadClass.getString(Constants.getFolderSubpath, "").toString()
            val finalFolderPathDesired = "/$company/$license/$theFullPath"
            val destinationFolder =
                Environment.getExternalStorageDirectory().absolutePath + "/Download/${Constants.Syn2AppLive}/" + finalFolderPathDesired
            val filePath = "file://$destinationFolder"
            val myFile = File(destinationFolder)
            if (myFile.exists()) {
                webView?.let { it.clearHistory() }
                loadOnlineLiveUrl(filePath)
                load_offline_indicator()

            } else {

                InitWebvIewloadStates()
                showToastMessage("Schedule Index File not found..")
            }
        } catch (e: java.lang.Exception) {
        }
    }


    private fun load_Offline_Page_From_Admin(theUrl: String) {
        try {
            val myDownloadClass = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
            val company = myDownloadClass.getString(Constants.getFolderClo, "").toString()
            val license = myDownloadClass.getString(Constants.getFolderSubpath, "").toString()
            val filename = "/$theUrl"
            val finalFolderPathDesired = "/" + company + "/" + license + "/" + Constants.App
            val destinationFolder =
                Environment.getExternalStorageDirectory().absolutePath + "/Download/${Constants.Syn2AppLive}/" + finalFolderPathDesired
            val filePath = "file://$destinationFolder$filename"
            val myFile = File(destinationFolder, File.separator + filename)


            if (myFile.exists()) {
                webView?.let { it.clearHistory() }
                loadOnlineLiveUrl(filePath)
                load_offline_indicator()

            } else {

                showToastMessage("Schedule Index File not found..")
                InitWebvIewloadStates()

            }
        } catch (e: java.lang.Exception) {
        }
    }


    /// Set Up Camera
    /// Set Up Camera
    // setup usbcam

    private fun iniliaze_Schedule_and_usbCamera() {

        // init Widows Screen siezwe
        initDisplaySize()

        // Hide camera Layout if need be
        val get_imgStreamVideo = sharedBiometric.getString(Constants.imgStreamVideo, "").toString()
        val get_imgUseDevicecameraOrPlugInCamera =
            sharedBiometric.getString(Constants.imgUseDevicecameraOrPlugInCamera, "").toString()
        if (get_imgStreamVideo != Constants.imgStreamVideo) {
            mlayout?.visibility = View.GONE
        } else {
            handler.postDelayed({
                runOnUiThread {
                    try {
                        if (isSystemRunning) {
                            doResizeUSBCameraView()
                        }
                    } catch (exception: java.lang.Exception) {
                    }
                }
            }, 2000)
        }




        if (get_imgUseDevicecameraOrPlugInCamera != Constants.imgUseDevicecameraOrPlugInCamera) {
            handler.postDelayed({
                runOnUiThread {
                    try {
                        if (isSystemRunning) {
                            checkUsbDevices();
                        }
                    } catch (exception: java.lang.Exception) {
                    }
                }
            }, 500)
        } else {
            handler.postDelayed({
                runOnUiThread {
                    try {
                        if (isSystemRunning) {
                            inilazeUSBWebCam();
                        }
                    } catch (exception: java.lang.Exception) {
                    }
                }
            }, 7000)
        }
    }


    private fun checkUsbDevices() {
        try {
            val usbManager = getSystemService(USB_SERVICE) as UsbManager
            val deviceList = usbManager.deviceList
            val deviceIterator: Iterator<UsbDevice> = deviceList.values.iterator()
            var usbCameraConnected = false
            while (deviceIterator.hasNext()) {
                val device = deviceIterator.next()
                usbCameraConnected = true
                break
            }
            if (usbCameraConnected) {
                handler.postDelayed({
                    runOnUiThread {
                        try {
                            if (isSystemRunning) {
                                inilazeUSBWebCam()
                            }
                        } catch (exception: java.lang.Exception) {
                        }
                    }
                }, 7000)
            } else {
                showToastMessage("USB Live Stream not found");
                textNoCameraAvaliable?.visibility = View.VISIBLE
            }
        } catch (e: java.lang.Exception) {
        }
    }


    private fun inilazeUSBWebCam() {
        inliazeUSbCamVariables()
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    private fun inliazeUSbCamVariables() {
        try {
            val get_imgStreamVideo =
                sharedBiometric.getString(Constants.imgStreamVideo, "").toString()
            if (get_imgStreamVideo == Constants.imgStreamVideo) {
                try {
                    toggleInvisibilityAndStopCamera()
                    toggleVisibilityAndCameraStart()
                } catch (e: java.lang.Exception) {
                    Log.d(TAG, "inliazeUSbCamVariables: " + e.message.toString())
                    audioHandler!!.stopAudio()
                    audioHandler!!.endAudio()
                }
                reloadWebCam!!.setOnClickListener { v: View? ->
                    if (isSystemRunning) {
                        try {
                            toggleInvisibilityAndStopCamera()
                            toggleVisibilityAndCameraStart()
                        } catch (e: java.lang.Exception) {
                            Log.d(TAG, "inliazeUSbCamVariables: " + e.message.toString())
                            audioHandler!!.stopAudio()
                            audioHandler!!.endAudio()
                        }
                    }
                }
                closeWebCam!!.setOnClickListener { view: View? ->
                    try {
                        if (isSystemRunning) {
                            toggleInvisibilityAndStopCamera()
                        }
                    } catch (e: java.lang.Exception) {
                        Log.d(TAG, "inliazeUSbCamVariables: " + e.message.toString())
                    }
                }


                /// Hide the icons after a while
                try {
                    showCameraIconhandler.postDelayed({
                        if (isSystemRunning) {
                            reloadWebCam!!.visibility = View.INVISIBLE
                            closeWebCam!!.visibility = View.INVISIBLE
                            expandWebcam!!.visibility = View.INVISIBLE
                        }
                    }, 10000)
                } catch (e: java.lang.Exception) {
                }


                /// set up motion layout on container
                mlayout!!.setOnTouchListener { v: View, event: MotionEvent ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            dXo = v.x - event.rawX
                            dYo = v.y - event.rawY
                            lastActionXY = MotionEvent.ACTION_DOWN
                            if (!isShowToastDisplayed) {
                                try {
                                    reloadWebCam?.visibility = View.VISIBLE
                                    closeWebCam?.visibility = View.VISIBLE
                                    expandWebcam?.visibility = View.VISIBLE

                                    showCameraIconhandler?.let { it.removeCallbacksAndMessages(null) }

                                    isShowToastDisplayed = true
                                    isHideToastDisplayed = false
                                } catch (e: java.lang.Exception) {
                                }
                            }
                        }

                        MotionEvent.ACTION_MOVE -> {
                            v.y = event.rawY + dYo
                            v.x = event.rawX + dXo
                            lastActionXY = MotionEvent.ACTION_MOVE
                            if (isShowToastDisplayed && !isHideToastDisplayed) {
                                try {
                                    showCameraIconhandler?.postDelayed({
                                        reloadWebCam?.visibility = View.INVISIBLE
                                        closeWebCam?.visibility = View.INVISIBLE
                                        expandWebcam?.visibility = View.INVISIBLE
                                    }, 10000)
                                    isHideToastDisplayed = true
                                } catch (e: java.lang.Exception) {
                                }
                            }
                        }

                        MotionEvent.ACTION_UP -> {
                            // Reset flags on touch release to allow the process to repeat
                            isShowToastDisplayed = false
                            isHideToastDisplayed = false
                        }

                        else -> return@setOnTouchListener false
                    }
                    true
                }
                val get_imgEnableExpandFloat =
                    sharedBiometric.getString(Constants.imgEnableExpandFloat, "")
                if (get_imgEnableExpandFloat != Constants.imgEnableExpandFloat) {
                    // expandWebcam.setVisibility(View.VISIBLE);
                    expandWebcam!!.setOnTouchListener { v: View, event: MotionEvent ->
                        when (event.actionMasked) {
                            MotionEvent.ACTION_DOWN -> {
                                initialWidth = mlayout!!.width
                                initialHeight = mlayout!!.height
                                dXo = v.x - event.rawX
                                dYo = v.y - event.rawY
                                lastActionXY = MotionEvent.ACTION_DOWN
                            }

                            MotionEvent.ACTION_MOVE -> {
                                val newWidth = (event.rawX + dXo).toInt()
                                val newHeight = (event.rawY + dYo).toInt()
                                mlayout?.layoutParams?.width =
                                    if (newWidth > 0) newWidth else initialWidth
                                mlayout?.layoutParams?.height =
                                    if (newHeight > 0) newHeight else initialHeight
                                mlayout?.requestLayout()
                                lastActionXY = MotionEvent.ACTION_MOVE
                            }

                            else -> return@setOnTouchListener false
                        }
                        true
                    }
                }
                val get_imgEnableDisplayIntervals =
                    sharedBiometric.getString(Constants.imgEnableDisplayIntervals, "").toString()
                if (get_imgEnableDisplayIntervals == Constants.imgEnableDisplayIntervals) {
                    start_Display_Timer()
                }
            }
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "Error starting camera: " + e.message, e)
        }
    }

    private fun toggleInvisibilityAndStopCamera() {
        try {
            cameraHandler!!.stopCamera()
            audioHandler!!.stopAudio()
            audioHandler!!.endAudio()
            mlayout!!.visibility = View.GONE

        } catch (e: java.lang.Exception) {
            Log.e(TAG, "Error starting camera: " + e.message, e)
        }

    }

    private fun toggleVisibilityAndCameraStart() {
        try {
            mlayout?.visibility = View.VISIBLE
            mlayout?.alpha = 1f
            if (binding.textureView.isAvailable()) {
                cameraHandler?.startCamera()
                textNoCameraAvaliable?.visibility = View.INVISIBLE
                val get_imgStreamAudioSound =
                    sharedBiometric.getString(Constants.imgStreamAudioSound, "").toString()
                if (get_imgStreamAudioSound != Constants.imgStreamAudioSound) {
                    audioHandler!!.startAudio()
                }
            } else {
                binding.textureView.setSurfaceTextureListener(object : SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(
                        surface: SurfaceTexture,
                        width: Int,
                        height: Int
                    ) {
                        try {
                            mlayout?.visibility = View.VISIBLE
                            cameraHandler?.startCamera()
                            textNoCameraAvaliable?.visibility = View.INVISIBLE
                            val get_imgStreamAudioSound =
                                sharedBiometric.getString(Constants.imgStreamAudioSound, "")
                                    .toString()
                            if (get_imgStreamAudioSound != Constants.imgStreamAudioSound) {
                                audioHandler!!.startAudio()
                            }
                        } catch (e: java.lang.Exception) {
                        }
                    }

                    override fun onSurfaceTextureSizeChanged(
                        surface: SurfaceTexture,
                        width: Int,
                        height: Int
                    ) {
                    }

                    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                        return false
                    }

                    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                })
            }
        } catch (e: java.lang.Exception) {
        }
    }

    private fun initDisplaySize() {
        try {
            val displaymetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displaymetrics)
            mScreenHeight = displaymetrics.heightPixels
            mScreenWidth = displaymetrics.widthPixels
        } catch (e: java.lang.Exception) {
            Log.d(TAG, "initDisplaySize: " + e.message.toString())
        }
    }

    private fun doResizeUSBCameraView() {
        try {

            val get_imgStreamAPIorDevice =
                sharedBiometric.getString(Constants.imgStreamAPIorDevice, "").toString()
            if (get_imgStreamAPIorDevice != Constants.imgStreamAPIorDevice) {
                /// for device
                val sharedCamera = getSharedPreferences(Constants.SHARED_CAMERA_PREF, MODE_PRIVATE)
                val startY = sharedCamera.getString(Constants.startY, "").toString()
                    .trim { it <= ' ' }
                val camHeight = sharedCamera.getString(Constants.camHeight, "").toString()
                    .trim { it <= ' ' }
                val startX = sharedCamera.getString(Constants.startX, "").toString()
                    .trim { it <= ' ' }
                val camWidth = sharedCamera.getString(Constants.camWidth, "").toString()
                    .trim { it <= ' ' }
                pass_Width_heights(startY, camHeight, startX, camWidth)
            } else {
                /// for Api
                val sharedCamera = getSharedPreferences(Constants.SHARED_CAMERA_PREF, MODE_PRIVATE)
                val startY = sharedCamera.getString(Constants.start_height_api, "").toString()
                    .trim { it <= ' ' }
                val camHeight = sharedCamera.getString(Constants.end_height_api, "").toString()
                    .trim { it <= ' ' }
                val startX = sharedCamera.getString(Constants.start_width_api, "").toString()
                    .trim { it <= ' ' }
                val camWidth = sharedCamera.getString(Constants.end_width_api, "").toString()
                    .trim { it <= ' ' }
                pass_Width_heights(startY, camHeight, startX, camWidth)
            }
        } catch (e: NumberFormatException) {
            showToastMessage("Invalid Input Device Width and Height")
        }
    }

    private fun pass_Width_heights(
        startY: String,
        camHeight: String,
        startX: String,
        camWidth: String
    ) {
        var startY = startY
        var camHeight = camHeight
        var startX = startX
        var camWidth = camWidth
        try {
            val m_camHeight = camHeight.toInt()
            val m_camWidth = camWidth.toInt()
            if (startY.isEmpty()) {
                startY = "0"
            }
            if (camHeight.isEmpty()) {
                camHeight = "0"
            }
            if (startX.isEmpty()) {
                startX = "0"
            }
            if (camWidth.isEmpty()) {
                camWidth = "0"
            }
            if (camHeight == "0" && camWidth == "0") {
                showToastMessage("Invalid dimension")
                return
            }
            if (m_camWidth <= 9 && m_camHeight <= 9) {
                showToastMessage("Invalid values")
                return
            }
            val startHeight = startY.toInt().toDouble()
            val startWidth = startX.toInt().toDouble()
            mUSBCameraLeftMargin = mScreenWidth / 100.0 * startWidth
            mUSBCameraTopMargin = mScreenHeight / 100.0 * startHeight
            mUSBCameraHeight = mScreenHeight / 100.0 * camHeight.toInt()
            mUSBCameraWidth = mScreenWidth / 100.0 * camWidth.toInt()

            // Calculate total dimensions including margins
            val totalWidth = mUSBCameraWidth + mUSBCameraLeftMargin
            val totalHeight = mUSBCameraHeight + mUSBCameraTopMargin

            // Check if total dimensions exceed screen dimensions
            if (totalWidth > mScreenWidth) {
                mUSBCameraLeftMargin = 0.0
            }
            if (totalHeight > mScreenHeight) {
                mUSBCameraTopMargin = 0.0
            }

            // Set new layout parameters for mlayout
            val layoutParams = mlayout?.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.width = mUSBCameraWidth.toInt()
            layoutParams.height = mUSBCameraHeight.toInt()
            layoutParams.leftMargin = mUSBCameraLeftMargin.toInt()
            layoutParams.topMargin = mUSBCameraTopMargin.toInt()
            mlayout!!.layoutParams = layoutParams
        } catch (e: java.lang.Exception) {
        }
    }


    private inner class UsbBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            // val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED == action) {
                handler.postDelayed(Runnable {
                    runOnUiThread(Runnable {
                        try {
                            //  if (isAudioDevice(device)) {Toast.makeText(context, "Audio device connected", Toast.LENGTH_SHORT).show();}
                            if (isSystemRunning) {
                                showToastMessage("USB Camera connected")
                                inilazeUSBWebCam()
                            }
                        } catch (exception: java.lang.Exception) {
                        }
                    })
                }, 4000)
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
                runOnUiThread(Runnable {
                    try {
                        //  if (isAudioDevice(device)) {Toast.makeText(context, "Audio device disconnected", Toast.LENGTH_SHORT).show();}
                        if (isSystemRunning) {
                            showToastMessage("USB Camera disconnected")
                            toggleInvisibilityAndStopCamera()
                        }
                    } catch (e: java.lang.Exception) {
                    }
                })
            }
        }
    }


    private fun getIntentFilter(): IntentFilter {
        val filter = IntentFilter()
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        return filter
    }


    private inner class CameraDisconnectedReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                val action = intent.action
                if (Constants.SYNC_CAMERA_DISCONNECTED == action) {
                    runOnUiThread(Runnable {
                        try {
                            if (isSystemRunning) {
                                audioHandler?.stopAudio()
                                audioHandler?.endAudio()
                                textNoCameraAvaliable?.setVisibility(View.VISIBLE)
                                showToastMessage("No cameras available")
                            }
                        } catch (e: java.lang.Exception) {
                        }
                    })
                } else {
                    showToastMessage("Camera Connected")
                }
            } catch (e: java.lang.Exception) {
                Log.d(TAG, "onReceive: " + e.message.toString())
            }
        }
    }


    private fun start_Display_Timer() {
        try {
            val get_imgStreamAPIorDevice =
                sharedBiometric.getString(Constants.imgStreamAPIorDevice, "").toString()
            if (get_imgStreamAPIorDevice != Constants.imgStreamAPIorDevice) {
                val d_time =
                    sharedCamera.getLong(Constants.get_Display_Camera_Defined_Time_for_Device, 0L)
                val timeertaker = d_time * 60 * 1000
                if (timeertaker != 0L) {
                    StartCameraHandler.postDelayed({
                        if (isSystemRunning) {
                            toggleInvisibilityAndStopCamera()
                            stopTimer()
                            star_Hide_Timer()
                        }
                    }, timeertaker)
                } else {
                    showToastMessage("Invalid Display Camera Time")
                }
            } else {
                val sharedCamera = getSharedPreferences(Constants.SHARED_CAMERA_PREF, MODE_PRIVATE)
                val display_time_api =
                    sharedCamera.getString(Constants.display_time_api, "").toString()
                        .trim { it <= ' ' }
                val d_time = java.lang.Long.valueOf(display_time_api)
                val timeertaker = d_time * 60 * 1000
                if (timeertaker != 0L) {
                    StartCameraHandler?.postDelayed({
                        if (isSystemRunning) {
                            toggleInvisibilityAndStopCamera()
                            stopTimer()
                            star_Hide_Timer()
                        }
                    }, timeertaker)
                } else {
                    showToastMessage("Invalid Display Camera Time")
                }
            }
        } catch (e: java.lang.Exception) {
        }
    }


    private fun star_Hide_Timer() {
        try {
            val get_imgStreamAPIorDevice =
                sharedBiometric.getString(Constants.imgStreamAPIorDevice, "").toString()
            if (get_imgStreamAPIorDevice != Constants.imgStreamAPIorDevice) {
                val d_time =
                    sharedCamera.getLong(Constants.get_Hide_Camera_Defined_Time_for_Device, 0L)
                val timeertaker = d_time * 60 * 1000
                if (timeertaker != 0L) {
                    StartCameraHandler.postDelayed({
                        if (isSystemRunning) {
                            try {
                                toggleInvisibilityAndStopCamera()
                                toggleVisibilityAndCameraStart()
                            } catch (e: java.lang.Exception) {
                                Log.d(TAG, "star_Hide_Timer: " + e.message.toString())
                                audioHandler?.stopAudio()
                                audioHandler?.endAudio()
                            }
                        }
                        stopTimer()
                        start_Display_Timer()
                    }, timeertaker)
                } else {
                    showToastMessage("Invalid Display Camera Time")
                }
            } else {
                val hide_time_api = sharedCamera.getString(Constants.hide_time_api, "").toString()
                    .trim { it <= ' ' }
                val d_time = java.lang.Long.valueOf(hide_time_api)
                val timeertaker = d_time * 60 * 1000
                if (timeertaker != 0L) {
                    StartCameraHandler.postDelayed({
                        if (isSystemRunning) {
                            try {
                                toggleInvisibilityAndStopCamera()
                                toggleVisibilityAndCameraStart()
                            } catch (e: java.lang.Exception) {
                                showToastMessage(e.message!!)
                                audioHandler?.stopAudio()
                                audioHandler?.endAudio()
                            }
                        }
                        stopTimer()
                        start_Display_Timer()
                    }, timeertaker)
                } else {
                    showToastMessage("Invalid Display Camera Time")
                }
            }
        } catch (e: java.lang.Exception) {
        }
    }

    private fun stopTimer() {
        StartCameraHandler.let {
            it.removeCallbacksAndMessages(null)
        }
    }


    private fun CheckShortCutImage() {
        try {

            val get_ShortCutStatus =
                sharedBiometric.getString(Constants.Do_NO_SHOW_SHORT_CUT_AGAIN, "").toString()

            if (get_ShortCutStatus != Constants.Do_NO_SHOW_SHORT_CUT_AGAIN) {

                handler.postDelayed(Runnable {
                    if (isSystemRunning) {

                        lifecycleScope.launch(Dispatchers.IO) {

                            val myDownloadClass =
                                getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
                            val getFolderClo =
                                myDownloadClass.getString(Constants.getFolderClo, "").toString()
                            val getFolderSubpath =
                                myDownloadClass.getString(Constants.getFolderSubpath, "").toString()
                            val pathFolder =
                                "/" + getFolderClo + "/" + getFolderSubpath + "/" + Constants.App + "/" + "Config"
                            val folder =
                                Environment.getExternalStorageDirectory().absolutePath + "/Download/" + Constants.Syn2AppLive + "/" + pathFolder
                            val fileTypes = "app_logo.png"
                            val file = File(folder, fileTypes)

                            if (file.exists()) {
                                withContext(Dispatchers.Main) {
                                    initShortCut()
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Log.d(TAG, "CheckShortCutImage: No Short Image Found")
                                }
                            }
                        }

                    }
                }, 7000)

            }


        } catch (e: Exception) {
            Log.d(TAG, "CheckShortCutImage: " + e.message.toString())
        }

    }

    private fun initShortCut() {
        try {
            binding.adView.visibility = View.VISIBLE

            startCountDownShortCut(5)

            binding.textAgreeYes.setOnClickListener {

                lifecycleScope.launch(Dispatchers.IO) {

                    val myDownloadClass =
                        getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
                    val getFolderClo =
                        myDownloadClass.getString(Constants.getFolderClo, "").toString()
                    val getFolderSubpath =
                        myDownloadClass.getString(Constants.getFolderSubpath, "").toString()
                    val pathFolder =
                        "/" + getFolderClo + "/" + getFolderSubpath + "/" + Constants.App + "/" + "Config"
                    val folder =
                        Environment.getExternalStorageDirectory().absolutePath + "/Download/" + Constants.Syn2AppLive + "/" + pathFolder
                    val fileTypes = "app_logo.png"
                    val file = File(folder, fileTypes)

                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (Build.VERSION.SDK_INT >= 25) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            CustomShortcutsDemo.setUp(applicationContext, getFolderSubpath, bitmap)
                        }
                    }

                    if (Build.VERSION.SDK_INT >= 28) {
                        shortcutPin(applicationContext, Constants.shortcut_messages_id, 1)
                    }
                }
                binding.adView.visibility = View.GONE

                countdownTimer_Short_Cut.let {
                    it?.cancel()
                }


                // save so it doesn't show again
                val editText88 = sharedBiometric.edit()
                editText88.putString(
                    Constants.Do_NO_SHOW_SHORT_CUT_AGAIN,
                    Constants.Do_NO_SHOW_SHORT_CUT_AGAIN
                )
                editText88.putString(Constants.imageUseBranding, Constants.imageUseBranding)
                editText88.putString(
                    Constants.imgToggleImageBackground,
                    Constants.imgToggleImageBackground
                )
                editText88.putString(
                    Constants.imgToggleImageSplashOrVideoSplash,
                    Constants.imgToggleImageSplashOrVideoSplash
                )
                editText88.apply()

            }



            binding.textAgreeNO.setOnClickListener {
                binding.adView.visibility = View.GONE
                countdownTimer_Short_Cut.let {
                    it?.cancel()
                }

                // save so it doesn't show again
                val editText88 = sharedBiometric.edit()
                editText88.putString(
                    Constants.Do_NO_SHOW_SHORT_CUT_AGAIN,
                    Constants.Do_NO_SHOW_SHORT_CUT_AGAIN
                )
                editText88.apply()

            }


        } catch (e: Exception) {
            Log.d(TAG, "initShortCut: " + e.message.toString())
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun shortcutPin(context: Context, shortcut_id: String, requestCode: Int) {
        try {
            val shortcutManager = applicationContext.getSystemService(ShortcutManager::class.java)

            if (shortcutManager!!.isRequestPinShortcutSupported) {

                val pinShortcutInfo = ShortcutInfo.Builder(context, shortcut_id).build()

                val pinnedShortcutCallbackIntent =
                    shortcutManager.createShortcutResultIntent(pinShortcutInfo)

                val successCallback = PendingIntent.getBroadcast(
                    context, /* request code */ requestCode,
                    pinnedShortcutCallbackIntent, /* flags */ PendingIntent.FLAG_MUTABLE
                )

                shortcutManager.requestPinShortcut(pinShortcutInfo, successCallback.intentSender)


            } else {
                Log.d(TAG, "shortcutPin: not supported")
            }
        } catch (e: Exception) {
            Log.d(TAG, "shortcutPin: " + e.message.toString())
        }
    }


    private fun startCountDownShortCut(minutes: Long) {
        val milliseconds = minutes * 60 * 1000 // Convert minutes to
        countdownTimer_Short_Cut = object : CountDownTimer(milliseconds, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onFinish() {

                try {
                    binding.adView.visibility = View.GONE
                } catch (e: java.lang.Exception) {
                }

            }

            override fun onTick(millisUntilFinished: Long) {
                try {
                    val totalSecondsRemaining = millisUntilFinished / 1000
                    var minutesUntilFinished = totalSecondsRemaining / 60
                    var remainingSeconds = totalSecondsRemaining % 60

                    if (remainingSeconds == 0L && minutesUntilFinished > 0) {
                        minutesUntilFinished--
                        remainingSeconds = 59
                    }
                    val displayText =
                        String.format("%d:%02d", minutesUntilFinished, remainingSeconds)
                    binding.textShortTimeDisplay.text = displayText

                } catch (ignored: java.lang.Exception) {
                }
            }
        }
        countdownTimer_Short_Cut?.start()
    }


    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        try {
            if (drawer_menu?.visibility == View.VISIBLE) {
                drawer_menu?.visibility = View.GONE
            }

            if (!hasWebviewPageLoadedBefore) {
                ClearLastUrl()
                navigateBackTosetting()

            } else {
                if (isScheduleRunning) {
                    navigateBackTosetting()
                } else {
                    if (webView!!.canGoBack()) {
                        webView?.goBack()
                    } else {
                        if (ClearCacheOnExit) {
                            webView!!.clearCache(true)
                        }
                        if (AskToExit) {
                            ShowExitDialogue()
                            if (LoadLastWebPageOnAccidentalExit) {
                                ClearLastUrl()
                            }
                        } else {
                            ClearLastUrl()
                            navigateBackTosetting()
                        }
                    }
                }
            }

            ///
        } catch (e: Exception) {
            Log.d(TAG, "onBackPressed: ${e.message.toString()}")
        }
    }


    override fun onPause() {
        super.onPause()
        try {

            isAppOpen = false

        } catch (e: java.lang.Exception) {
            Log.d(TAG, "onPause: " + e.message.toString())
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()

        try {

            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            isSystemRunning = true
            isAppOpen = true


            val isSavedEmail = simpleSavedPassword.getString(Constants.isSavedEmail, "").toString()
            val COUNTRY_NAME = simpleSavedPassword.getString(Constants.COUNTRY_NAME, "").toString()
            val USER_NAME = simpleSavedPassword.getString(Constants.USER_NAME, "").toString()
            val USER_COMPANY_NAME =
                simpleSavedPassword.getString(Constants.USER_COMPANY_NAME, "").toString()

            if (isSavedEmail.isEmpty() && COUNTRY_NAME.isEmpty() && USER_NAME.isEmpty() && USER_COMPANY_NAME.isEmpty()) {

                startActivity(Intent(applicationContext, InformationActivity::class.java))
                finish()
            } else {

                // Retrieve the intent extra for offline usage
                val value = intent.getStringExtra(Constants.USE_TEMP_OFFLINE_WEB_VIEW_PAGE)
                if (value == Constants.USE_TEMP_OFFLINE_WEB_VIEW_PAGE) {
                    // showToastMessage("You are Running Offline Version")
                } else {
                    if (jsonUrl == null) {
                        // jsonUrl is null, redirecting to SplashKT

                        val intent = Intent(applicationContext, SplashKT::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val editText88 = sharedBiometric.edit()
                        editText88.putString(Constants.JSON_MAIN_URL, jsonUrl)
                        editText88.apply()

                    }
                }

            }

            val get_imagEnableDownloadStatus =
                sharedBiometric.getString(Constants.showDownloadSyncStatus, "").toString()
            val getToHideQRCode = preferences.getBoolean(Constants.hideQRCode, false)
            val get_drawer_icon = preferences.getBoolean(Constants.hide_drawer_icon, false)


            //  get_ProtectPassowrd

            if (get_imagEnableDownloadStatus == Constants.showDownloadSyncStatus) {
                bottom_server_layout?.visibility = View.VISIBLE
            } else {
                bottom_server_layout?.visibility = View.GONE
            }


            val get_INSTALL_TV_JSON_USER_CLICKED =
                sharedTVAPPModePreferences.getString(Constants.INSTALL_TV_JSON_USER_CLICKED, "")
                    .toString()
            val hideBottom_MenuIcon_APP =
                sharedTVAPPModePreferences.getBoolean(Constants.hideBottom_MenuIcon_APP, false)


            if (get_INSTALL_TV_JSON_USER_CLICKED == Constants.INSTALL_TV_JSON_USER_CLICKED) {
                if (hideBottom_MenuIcon_APP) {
                    bottomtoolbar_btn_7?.visibility = View.VISIBLE
                } else {
                    bottomtoolbar_btn_7?.visibility = View.GONE
                }
            }



            if (get_INSTALL_TV_JSON_USER_CLICKED != Constants.INSTALL_TV_JSON_USER_CLICKED) {
                if (get_drawer_icon) {
                    bottomtoolbar_btn_7?.visibility = View.VISIBLE
                } else {
                    bottomtoolbar_btn_7?.visibility = View.GONE
                }

            }


            if (!getToHideQRCode) {
                drawerItem7?.visibility = View.VISIBLE
            } else {
                drawerItem7?.visibility = View.INVISIBLE
            }
            val getUrlFromScanner = intent.getStringExtra(Constants.QR_CODE_KEY)
            if (getUrlFromScanner != null) {
                if (getUrlFromScanner.startsWith("https://") || getUrlFromScanner.startsWith("http://")) {
                    webView?.loadUrl(getUrlFromScanner)
                }
            }


            currentSettings = Paper.book().read(Common.CURRENT_SETTING)


            // initialize connection broadCast listener
           // connectivityReceiver = ConnectivityReceiver()
           // val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
           // registerReceiver(connectivityReceiver, intentFilter)
            connectivityReceiver = ConnectivityReceiver()
            val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                registerReceiver(connectivityReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(connectivityReceiver, intentFilter)
            }



            val getSynModeType = sharedBiometric.getString(Constants.IMG_SELECTED_SYNC_METHOD, "").toString()
            if (getSynModeType == Constants.USE_ZIP_SYNC) {
                updateSyncViewZip()
            } else {
                update_UI_for_API_Sync_Updade()
            }


        } catch (e: java.lang.Exception) {
            Log.d(TAG, "onResume: " + e.message.toString())
        }

    }

    override fun onStop() {
        super.onStop()

        try {

            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            isSystemRunning = false

            // remove connection broadcast listener
            connectivityReceiver?.let {
                unregisterReceiver(it)
            }

        } catch (e: java.lang.Exception) {
            showToastMessage(e.message!!)
        }
    }


    @SuppressLint("ImplicitSamInstance")
    override fun onDestroy() {
        super.onDestroy()
        try {

            isAppOpen = false

            if (LoadLastWebPageOnAccidentalExit) {
                preferences.edit().putString("lasturl", webView!!.originalUrl).apply()
            }


            mProgressDialog?.takeIf { itmDialog ->
                itmDialog.isShowing
            }?.dismiss()


            progressDialog?.let { itShow ->

                if (itShow.isShowing) {
                    itShow.dismiss()
                }
            }

            mydialog?.let { itDilaog ->
                if (itDilaog.isShowing) {
                    itDilaog.dismiss()
                }
            }


            alertDialog?.let { italt ->
                if (italt.isShowing) {
                    italt.dismiss()
                }
            }




            if (countdownTimer_Api_Sync != null) {
                countdownTimer_Api_Sync?.cancel()
            }


            if (countdownTimer_Short_Cut != null) {
                countdownTimer_Short_Cut?.cancel()
            }


            if (countdownTimer_App_Refresh != null) {
                countdownTimer_App_Refresh?.cancel()
            }


            if (customInternetWebviewPage != null){
                customInternetWebviewPage!!.dismiss()
                isCountDownDialogVisible = false
            }

            if ( countdownTimerForWebviewPage != null){
                countdownTimerForWebviewPage?.cancel()
            }



            receiver?.let { itpp ->
                applicationContext.unregisterReceiver(itpp)
            }


            // remove fecth listner
            fetchListener?.let { it4573 ->
                fetch?.removeListener(it4573)
            }

            fetch?.let { it489 ->
                it489.removeAll()
            }

            // remove all handler call back messages
            handlerDeviceTime?.let { it11 ->
                it11.removeCallbacksAndMessages(null)
            }

            handlerSchedule?.let { it222 ->
                it222.removeCallbacksAndMessages(null)
            }

            handlerRunningSchedule?.let { it333 ->
                it333.removeCallbacksAndMessages(null)
            }

            handlerServerTime?.let { it444 ->
                it444.removeCallbacksAndMessages(null)
            }


            // Stop Camera
            cameraHandler?.let { itCamera ->
                itCamera.stopCamera()
            }

            // Stop audio
            audioHandler?.let { itAudio ->
                itAudio.endAudio()
            }

            // remove all camera handler
            StartCameraHandler?.let { ito ->
                ito.removeCallbacksAndMessages(null)
            }

            showCameraIconhandler?.let { itq ->
                itq.removeCallbacksAndMessages(null)
            }

            // remove sync ui status
            val editorSyn = myDownloadClass.edit()
            editorSyn.remove(Constants.SynC_Status)
            editorSyn.apply()


            // unregister camera Broadcast Receiver
            usbBroadcastReceiver?.let { itUSBRF ->
                unregisterReceiver(itUSBRF)
            }

            // unregister camera Broadcast Receiver
            CameraReceiver?.let { itUSBRFCAM ->
                unregisterReceiver(itUSBRFCAM)
            }


            // Check if the webView is not null
            if (webView != null) {
                // Remove the WebView from its parent
                val parent = webView?.parent as? ViewGroup
                parent?.removeView(webView)

                // Destroy the WebView
                webView?.apply {
                    stopLoading()
                    clearHistory()
                    clearCache(true)
                    loadUrl("about:blank")
                    onPause()
                    removeAllViews()
                    destroy()
                }

                // Set the WebView to null
                webView = null

            }



            if (progressReceiver != null) {
                unregisterReceiver(progressReceiver)
            }

            if (progressDownloadBytesReceiver != null) {
                unregisterReceiver(progressDownloadBytesReceiver)
            }

            if (Utility.foregroundParsingServiceClass(applicationContext)) {
                applicationContext.stopService(
                    Intent(applicationContext, ParsingSyncService::class.java)
                )
            }


            if (Utility.foregroundRetryParsingServiceClass(applicationContext)) {
                applicationContext.stopService(Intent(applicationContext, RetryParsingSyncService::class.java))
            }


            if (receiverNotify != null){
                applicationContext.unregisterReceiver(receiverNotify)
            }


        } catch (e: java.lang.Exception) {
            Log.d(TAG, "onDestroy: " + e.message.toString())
        }


    }




    private fun restartApp() {
        lifecycleScope.launch(Dispatchers.IO) {
            FileUtils.deleteQuietly(cacheDir)
            FileUtils.deleteQuietly(externalCacheDir)
        }

        val handlerRestart = Handler(Looper.getMainLooper())
        handlerRestart.postDelayed(Runnable {
           finishAffinity()
           val intent = Intent(applicationContext, SplashVideoActivity::class.java)
           startActivity(intent)
       }, 3000)

    }


    private fun start_App_Refresh_Time(hours: Long) {
        if (isSystemRunning) {
            val milliseconds = hours * 60 * 60 * 1000 // Convert hours to milliseconds
            countdownTimer_App_Refresh = object : CountDownTimer(milliseconds, 1000) {
                @SuppressLint("SetTextI18n")
                override fun onFinish() {
                    try {
                        restartApp()
                    } catch (e: Exception) {
                        // Handle exception if needed
                    }
                }

                override fun onTick(millisUntilFinished: Long) {
                    try {
                        val totalSecondsRemaining = millisUntilFinished / 1000
                        val hoursUntilFinished = totalSecondsRemaining / 3600
                        val minutesUntilFinished = (totalSecondsRemaining % 3600) / 60
                        val remainingSeconds = totalSecondsRemaining % 60

                        val displayText = String.format(
                            "%d:%02d:%02d",
                            hoursUntilFinished,
                            minutesUntilFinished,
                            remainingSeconds
                        )
                        binding.textRefreshTime.text = displayText

                    } catch (ignored: Exception) {
                        // Handle exception if needed
                    }
                }
            }
            countdownTimer_App_Refresh?.start()
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun applyOritenation() {
        val getState = sharedBiometric.getString(Constants.IMG_TOGGLE_FOR_ORIENTATION, "").toString()

        if (getState == Constants.USE_POTRAIT) {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        } else if (getState == Constants.USE_LANDSCAPE) {

            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        } else if (getState == Constants.USE_UNSEPECIFIED) {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        }

    }



    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        when (level) {
            TRIM_MEMORY_COMPLETE -> {
                showWarning("Memory is critically low. App may be terminated soon.")
                restartApp()
            }

            TRIM_MEMORY_MODERATE,
            TRIM_MEMORY_RUNNING_LOW,
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                showWarning("Device is running low on memory. Please close unused apps.")
                lifecycleScope.launch(Dispatchers.IO) {
                    FileUtils.deleteQuietly(cacheDir)
                    FileUtils.deleteQuietly(externalCacheDir)
                }
            }
        }
    }

    private fun showWarning(message: String) {
        try {
            runOnUiThread {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.d(TAG, "showWarning: ${e.message}")
        }
    }

    private fun showToastMessage(message: String) {
        try {
            runOnUiThread {
                val get_imagEnableDownloadStatus =
                    sharedBiometric.getString(Constants.showDownloadSyncStatus, "").toString()
                if (get_imagEnableDownloadStatus == Constants.showDownloadSyncStatus) {
                    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: java.lang.Exception) {
        }
    }


}