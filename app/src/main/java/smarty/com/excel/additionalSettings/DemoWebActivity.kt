package smarty.com.excel.additionalSettings

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import smarty.com.excel.R
import smarty.com.excel.additionalSettings.utils.Constants
import smarty.com.excel.additionalSettings.utils.Utility
import smarty.com.excel.additionalSettings.utils.isInternetAvailableOnBing
import smarty.com.excel.databinding.ActivityDemoWebBinding
import smarty.com.excel.databinding.CustomExitOrNotBinding
import smarty.com.excel.databinding.CustomOfflinePopLayoutBinding
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

class DemoWebActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDemoWebBinding
    private val TAG = "DemoWebActivity"

    private var isSystemRunning = true
    private var alertDialog: AlertDialog? = null

    private val sharedPreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SAVE_PORT_VALUES,
            Context.MODE_PRIVATE
        )
    }
    private var connectivityReceiver: ConnectivityReceiver? = null
    private var isCalledInfor = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDemoWebBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        initializeWebSettings()

        binding.textGoWifi.setOnClickListener {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
        }


        binding.textFixServer.setOnClickListener {
            when (binding.textFixServer.tag) {
                "FIX" -> {
                    navigateBackToSetting()
                }

                "WIFI" -> {
                    val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                    startActivity(intent)
                }
            }
        }


    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun loadOnlineLiveUrl(url: String) {
        try {
            // Load the provided URL
            binding.myWebview.loadUrl(url)

            // init chrome client
            setupWebViewClients()

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "loadOnlineLiveUrl: " + e.message.toString())
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeWebSettings() = with(binding.myWebview.settings) {
        javaScriptEnabled = true
        domStorageEnabled = true
        loadsImagesAutomatically = true
        builtInZoomControls = true
        displayZoomControls = false
        databaseEnabled = true
        allowFileAccess = true
        allowContentAccess = true
        setSupportMultipleWindows(true)
        javaScriptCanOpenWindowsAutomatically = true
        cacheMode = WebSettings.LOAD_NO_CACHE
        mediaPlaybackRequiresUserGesture = false
        layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING

        // Accept cookies
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.acceptThirdPartyCookies(binding.myWebview)

        // Debugging
        WebView.setWebContentsDebuggingEnabled(true)

        val url_launch = intent.getStringExtra("url_launch")

        // Init web view load state
        if (Utility.foregroundForSeverServiceClass(applicationContext)) {
            val localUrl = sharedPreferences.getString(Constants.localUrl, "").orEmpty()
            if (isValidLocalAddress(localUrl)) {
                if (Utility.isNetworkAvailable(applicationContext)) {
                    if (url_launch != null) {
                        loadOnlineLiveUrl(url_launch)
                    } else {
                        loadOnlineLiveUrl(localUrl)
                    }
                    binding.textView24.text = localUrl
                } else {
                    show_Pop_Confirm_Exit(
                        "Attention!",
                        "Network not detected. The server requires a connection to your local network (Wi-Fi or Ethernet) to function properly. Please connect and try again."
                    )
                }

            } else {
                show_Pop_Confirm_Exit(
                    "Attention!",
                    "The server needs proper configuration. Ensure you are connected to a local network before continuing."
                )
            }
        } else {
            show_Pop_Confirm_Exit(
                "Heads Up!",
                "The server needs to be started first. Kindly start it before continuing."
            )
        }
    }


    private fun isValidLocalAddress(url: String): Boolean {
        if (url.isBlank()) return false
        if (url.contains("0.0.0.0")) return false
        if (url.contains("127.0.0.1")) return false // loopback
        if (!url.contains(":")) return false        // no port
        return true
    }


    @SuppressLint("MissingInflatedId")
    private fun show_Pop_Confirm_Exit(title: String, body: String) {
        val binding: CustomExitOrNotBinding = CustomExitOrNotBinding.inflate(layoutInflater)
        val alertDialogBuilder = android.app.AlertDialog.Builder(this)
        alertDialogBuilder.setView(binding.root)

        val alertDialog = alertDialogBuilder.create()

        // 🔹 Only lock dialog if it's "Heads Up!"
        if (title == "Heads Up!") {
            alertDialog.setCancelable(false)
            alertDialog.setCanceledOnTouchOutside(false)
        } else {
            alertDialog.setCancelable(true)
            alertDialog.setCanceledOnTouchOutside(true)
        }

        // Transparent background + animation
        alertDialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            attributes.windowAnimations = R.style.PauseDialogAnimation
        }

        binding.textSucessful.text = title
        binding.textBodyMessage.text = body

        binding.textLaunchMyOnline.setOnClickListener {
            alertDialog.dismiss()
        }

        binding.textLaunchMyOffline.setOnClickListener {
            startActivity(Intent(applicationContext, DE_MO_202100::class.java))
            finish()
            alertDialog.dismiss()
        }

        alertDialog.show()
    }


    private fun InitWebvIewloadStates() {
        val filename = "/index.html"
        lifecycleScope.launch {
            loadOffline_Saved_Path_Offline_Webview(Constants.CLO, Constants.USER, filename)
        }

    }


    private fun getFilePath(CLO: String, DEMO: String, filename: String): String? {
        val baseDir = getExternalFilesDir(null)  // ✅ App-private scoped external storage
        val relativePath = "Syn2AppLive/$CLO/$DEMO/${Constants.App}"
        val destinationFolder = File(baseDir, relativePath)
        val myFile = File(destinationFolder, filename)

        return if (myFile.exists()) {
            // myFile.toURI().toString()  // Use proper file URI (e.g. file:///...)
            myFile.toURI().toURL().toString()
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

            if (isActive) {

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
                        binding.myWebview.apply {
                            clearHistory()
                            loadUrl(filePath.toString())
                            setupWebViewClients()
                        }
                    }
                } else {
                    if (isSystemRunning) {
                        showPopForTVConfiguration(Constants.compleConfiguration)
                    }
                }
            }
        }
    }


    @SuppressLint("MissingInflatedId", "UseCompatLoadingForDrawables")
    private fun showPopForTVConfiguration(message: String) {

        val bindingCL: CustomOfflinePopLayoutBinding =
            CustomOfflinePopLayoutBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this@DemoWebActivity)
        builder.setView(bindingCL.getRoot())
        alertDialog = builder.create() // Assign the dialog to the field
        alertDialog!!.setCanceledOnTouchOutside(false)
        alertDialog!!.setCancelable(false)
        if (alertDialog!!.window != null) {
            alertDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog!!.window!!.attributes.windowAnimations =
                R.style.PauseDialogAnimationCloseOnly
        }

        val textDescription: TextView = bindingCL.textDescription
        val imageView24: ImageView = bindingCL.imageView24
        imageView24.background = resources.getDrawable(R.drawable.ic_sync_cm)

        if (!message.isEmpty()) {
            textDescription.text = message
        }

        bindingCL.imgCloseDialog.setOnClickListener {
            alertDialog!!.dismiss()
        }


        bindingCL.textContinue.setOnClickListener {
            alertDialog!!.dismiss()
        }


        bindingCL.textContinuPasswordDai3.setOnClickListener {
            startActivity(Intent(applicationContext, SelectDownloadUrlActivity::class.java))
            finish()
            alertDialog!!.dismiss()
        }



        alertDialog!!.show()


    }


    private var isOfflinePage = false  // 🔹 add this at class level

    private fun setupWebViewClients() {
        binding.myWebview.webViewClient = object : WebViewClient() {

            override fun shouldInterceptRequest(
                view: WebView?,
                url: String
            ): WebResourceResponse? {
                if (url.contains("googleads.g.doubleclick.net")) {
                    val textStream: InputStream = ByteArrayInputStream("".toByteArray())
                    return WebResourceResponse("text/plain", "UTF-8", textStream)
                }
                return super.shouldInterceptRequest(view, url)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url == null) return false
                return if (
                    url.startsWith("http://") ||
                    url.startsWith("https://") ||
                    url.startsWith("file:///")
                ) {
                    false
                } else {
                    try {
                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME).apply {
                            addCategory(Intent.CATEGORY_BROWSABLE)
                            component = null
                            selector = null
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                    } catch (e: Exception) {
                        Log.e(TAG, "shouldOverrideUrlLoading Exception: ${e.message}")
                    }
                    true
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                if (isOfflinePage) {
                    binding.SimpleProgressBar.visibility = View.GONE
                } else {
                    binding.SimpleProgressBar.visibility = View.VISIBLE
                }
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                // Always hide for offline page
                if (isOfflinePage) {
                    binding.SimpleProgressBar.visibility = View.GONE
                    isOfflinePage = false  // reset for next navigation
                } else {
                    binding.SimpleProgressBar.visibility = View.GONE
                }
                super.onPageFinished(view, url)
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                showOfflinePage(view, failingUrl)
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: android.webkit.WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                if (request?.isForMainFrame == true) {
                    showOfflinePage(view, request.url.toString())
                }
            }

            private fun showOfflinePage(view: WebView?, failingUrl: String?) {
                try {
                    isOfflinePage = true  // 🔹 mark that we're loading the offline page
                    binding.SimpleProgressBar.visibility = View.GONE

                    val inputStream = assets.open("offline.html")
                    val htmlTemplate = inputStream.bufferedReader().use { it.readText() }

                    val html = htmlTemplate.replace("{{URL}}", failingUrl ?: "Unknown URL")

                    view?.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        binding.myWebview.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback
            ) {
                callback.invoke(origin, true, false)
                Log.d(TAG, "Geolocation permission requested for $origin")
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        isSystemRunning = true


        // Init web view load state
        if (Utility.foregroundForSeverServiceClass(applicationContext)) {
            if (Utility.isNetworkAvailable(applicationContext)) {
                if (isSystemRunning) {
                    showNetworkInfoLoadWeb()
                }
            } else {
                binding.topServerLayout.visibility = View.VISIBLE
                binding.textDescribeSituation.text =
                    "Attention !!, Sever needs to be connected to a Network Area"
                binding.textFixServer.text = "Wi-Fi Settings"
                binding.textFixServer.tag = "WIFI"

            }



            if (isSystemRunning) {
                connectivityReceiver = ConnectivityReceiver()
                val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    registerReceiver(
                        connectivityReceiver,
                        intentFilter,
                        Context.RECEIVER_NOT_EXPORTED
                    )
                } else {
                    registerReceiver(connectivityReceiver, intentFilter)
                }

            }

        }
    }


    override fun onStop() {
        super.onStop()

        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        isSystemRunning = false

        unregisterReceiver(connectivityReceiver)

    }



    inner class ConnectivityReceiver : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            try {
                val connectivityManager =
                    context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {

                    binding.topServerLayout.visibility = View.GONE
                    binding.textFixServer.text = "Fix Now"
                    binding.textFixServer.tag = "Fix Now"


                } else {
                    // No internet Connection
                    binding.topServerLayout.visibility = View.VISIBLE
                    binding.textDescribeSituation.text = "Attention !!, Sever needs to be connected to a Network Area"
                    binding.textFixServer.text = "Wi-Fi Settings"
                    binding.textFixServer.tag = "WIFI"

                }
                // No internet Connection
            } catch (ignored: java.lang.Exception) {
            }
        }
    }


    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        navigateBackToSetting()
    }


    private fun navigateBackToSetting() {
        binding.myWebview.stopLoading()
        binding.myWebview.destroy()
        showToastMessage("Returning to Settings...")
        lifecycleScope.launch {
            delay(500) // shorter, smoother
            val intent = Intent(this@DemoWebActivity, DE_MO_202100::class.java)
            startActivity(intent)
            finish()
        }

    }


    private fun showToastMessage(message: String) {
        runOnUiThread {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }


    @SuppressLint("SetTextI18n")
    private fun showNetworkInfoLoadWeb() {
        val details = Utility.getLocalNetworkDetails(applicationContext)
        val info = details?.infoString ?: "No active network detected."
        if (isSystemRunning) {

            if (isCalledInfor) {
                isCalledInfor = false
                lifecycleScope.launch {
                    delay(1000) // shorter delay for responsiveness
                    if (isSystemRunning) {
                        if (Utility.foregroundForSeverServiceClass(applicationContext)) {
                            val savedNetworkType =
                                sharedPreferences.getString("serverNetworkType", null)

                            if (savedNetworkType != info) {
                                showNetworkChangedDialog()
                            }
                        }
                    }
                }
            }
        }
    }


    //// == To Check if network changed

    @SuppressLint("SetTextI18n")
    private fun showNetworkChangedDialog() {
        binding.topServerLayout.visibility = View.VISIBLE
        binding.textDescribeSituation.text =
            "Attention !!, Network Change Detected, both Sever and Network needs to be on same Network Area"
        binding.textFixServer.text = "Fix Now"
        binding.textFixServer.tag = "FIX"

    }


}
