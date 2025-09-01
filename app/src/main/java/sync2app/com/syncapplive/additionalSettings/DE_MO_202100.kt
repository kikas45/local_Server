package sync2app.com.syncapplive.additionalSettings

import android.R
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.databinding.ActivityDeMo202100Binding
import android.content.Context
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sync2app.com.syncapplive.additionalSettings.utils.Utility
import sync2app.com.syncapplive.myService.ServerService
import kotlin.toString
import androidx.core.content.edit
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import sync2app.com.syncapplive.additionalSettings.SavedPathIndexList.IndexList
import sync2app.com.syncapplive.additionalSettings.SavedPathIndexList.IndexViewModel
import sync2app.com.syncapplive.additionalSettings.SavedPathIndexList.SavedPathIndexListAdapter
import sync2app.com.syncapplive.additionalSettings.autostartAppOncrash.Methods
import sync2app.com.syncapplive.databinding.CustomPortLayoutBinding
import sync2app.com.syncapplive.databinding.CustomSavedPathListBinding
import java.io.File


import android.Manifest
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.provider.Settings
import android.text.format.Formatter
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.URL






class DE_MO_202100 : AppCompatActivity(), SavedPathIndexListAdapter.OnItemClickListener {

    private lateinit var binding: ActivityDeMo202100Binding
    private val mUserViewModel by viewModels<IndexViewModel>()

    private val sharedPreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SAVE_PORT_VALUES,
            Context.MODE_PRIVATE
        )
    }

    private val sharedBiometric: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_BIOMETRIC, Context.MODE_PRIVATE
        )
    }
    private val sharedTVAPPModePreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_TV_APP_MODE, Context.MODE_PRIVATE
        )
    }

    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    private lateinit var customSavedDownloadDialog: Dialog

    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private val adapter by lazy {
        SavedPathIndexListAdapter(this)
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION: Int = 100
    }


    @SuppressLint("UseKtx", "UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeMo202100Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check location permission (needed for SSID)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            showNetworkInfo()
        }


        val filterPr = IntentFilter().apply { addAction(Constants.SERVER_PROGRESS_RECIEVER) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            registerReceiver(progressSeverService, filterPr, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(progressSeverService, filterPr)
        }

        //add exception
        Methods.addExceptionHandler(this)

        setUpFullScreenWindows()

        setupAutoStartSwitch()

        val get_imgToggleImageBackground =
            sharedBiometric.getString(Constants.imgToggleImageBackground, "")
        val get_imageUseBranding = sharedBiometric.getString(Constants.imageUseBranding, "")
        if (get_imgToggleImageBackground.equals(Constants.imgToggleImageBackground) && get_imageUseBranding.equals(
                Constants.imageUseBranding
            )
        ) {
            loadBackGroundImage()
        }


        // Restore saved port value
        val savedPort = sharedPreferences.getInt("lastPort", 8080)
        binding.portInput.setText(savedPort.toString())

        binding.startServerBtn.setOnClickListener {
            Utility.hideKeyBoard(applicationContext, binding.editTextPath)
            callServiceStart()
        }

        binding.stopServerBtn.setOnClickListener {
            Utility.hideKeyBoard(applicationContext, binding.editTextPath)
            callServiceStop()
        }

        binding.imgCloseDialog.setOnClickListener {
            Utility.hideKeyBoard(applicationContext, binding.editTextPath)
            closeApplication()
        }

        binding.textClickSetPort.setOnClickListener {
            Utility.hideKeyBoard(applicationContext, binding.editTextPath)
            showPopSetPort()
        }

        binding.closeBs.setOnClickListener {
            Utility.hideKeyBoard(applicationContext, binding.editTextPath)
            navigateBack()
        }

        binding.textBrowsePath.setOnClickListener {
            Utility.hideKeyBoard(applicationContext, binding.editTextPath)
            startActivity(Intent(applicationContext, FileExplorerActivity::class.java))
        }


        binding.txtNetworkInfo.setOnClickListener {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
        }



        binding.textSaveLocation.setOnClickListener {
            Utility.hideKeyBoard(applicationContext, binding.editTextPath)
            val getEditText = binding.editTextPath.text.toString().trim()

            // Only proceed if input contains "App/"
            if (getEditText.contains("App/")) {
                val index = getEditText.indexOf("App/")

                // Extract everything after "App/"
                val trimmedPath = getEditText.substring(index + "App/".length)

                val savedPorto = sharedPreferences.getInt("lastPort", 8080)
                val path = "$savedPorto/$trimmedPath"

                val indexList = IndexList(VALUES = path)
                mUserViewModel.addUser(indexList)

                binding.editTextPath.setText(path)
                Toast.makeText(this, "Saved successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Path must contain \"App/\"", Toast.LENGTH_SHORT).show()
            }
        }



        binding.textViewAllSavedList.setOnClickListener {
            Utility.hideKeyBoard(applicationContext, binding.editTextPath)
            showSavedPathList()
        }


        setupCopyAddressButton()

        setupServerActionButtons()



        lifecycleScope.launch {
            delay(300)
            iniUiActions()
        }

    }

    private fun setupAutoStartSwitch() {
        // Restore saved switch state
        val autoStartEnabled = sharedPreferences.getBoolean("autoStartServer", false)
        binding.imagShowOnlineStatus.isChecked = autoStartEnabled

        // Save switch state on toggle
        binding.imagShowOnlineStatus.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit()
                .putBoolean("autoStartServer", isChecked)
                .apply()
        }

        // If enabled → auto start server with coroutine delay
        if (autoStartEnabled) {
            lifecycleScope.launch {
                delay(5000)
                callServiceStart()
            }
        }
    }

    private fun iniUiActions() {
        val getServerState = sharedPreferences.getString(Constants.PR_SEVER_STATE, "").toString()
        if (getServerState == Constants.SL_Running) {
            if (Utility.foregroundForSeverServiceClass(applicationContext)) {
                manageUIOnServerStart()
            }
        } else {
            if (!Utility.foregroundForSeverServiceClass(applicationContext)) {
                manageUIOnServerStops()
            }
        }
    }

    private fun setupCopyAddressButton() {
        Utility.hideKeyBoard(applicationContext, binding.editTextPath)
        binding.textCopyIpAdress.setOnClickListener {
            val address = binding.addressText.text.toString()
            if (address.startsWith("Address: http")) {
                val url = address.substringAfter("Address: ")
                val clipboard =
                    getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Server URL", url))
                Toast.makeText(this, "Server URL copied", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No server running or URL not available", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    private fun callServiceStart() {
        if (!Utility.foregroundForSeverServiceClass(applicationContext)) {
            Utility.hideKeyBoard(applicationContext, binding.portInput)
            val port = binding.portInput.text.toString().toIntOrNull() ?: 8080

            if (port < 1000 || port > 9999) {
                Toast.makeText(this, "Port must be 4 digits (1000-9999)", Toast.LENGTH_SHORT).show()
                return
            }

            sharedPreferences.edit { putInt("lastPort", port) }
            applicationContext.stopService(Intent(applicationContext, ServerService::class.java))
            applicationContext.startService(Intent(applicationContext, ServerService::class.java))

        } else {
            Toast.makeText(this, "Server already running", Toast.LENGTH_SHORT).show()
        }
    }


    private fun callServiceStop() {
        Utility.hideKeyBoard(applicationContext, binding.editTextPath)
        if (Utility.foregroundForSeverServiceClass(applicationContext)) {
            applicationContext.stopService(Intent(applicationContext, ServerService::class.java))
        } else {
            Toast.makeText(this, "Server is currently no Running", Toast.LENGTH_SHORT).show()
        }
    }


    private var isCalled = true
    private val progressSeverService = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Constants.SERVER_PROGRESS_RECIEVER) {
                val status = intent.getStringExtra(Constants.SERVER_RUNNING_STATE)
                if (status == Constants.SERVER_STARTED) {
                    if (isCalled) {
                        isCalled = false
                        lifecycleScope.launch {
                            delay(1000)
                            isCalled = true
                            manageUIOnServerStart()
                        }
                    }
                } else if (status == Constants.SERVER_STOPPED) {
                    if (isCalled) {
                        isCalled = false
                        lifecycleScope.launch {
                            delay(1000)
                            isCalled = true
                            manageUIOnServerStops()
                        }
                    }
                }
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun manageUIOnServerStart() {
        val statusText = sharedPreferences.getString(Constants.SERVER_STATE, "").toString()
        val localUrl = sharedPreferences.getString(Constants.localUrl, "").toString()
        val PR_STROAGE = sharedPreferences.getString(Constants.PR_STROAGE, "").toString()
        // binding.statusText.text = statusText
        binding.addressText.text = "Address: $localUrl"
        binding.textSeverState.text = "Running"
        binding.textDisplayStoragePath.text = PR_STROAGE
        binding.textDisplayStoragePath.visibility = View.VISIBLE
        binding.textCopyIpAdress.visibility = View.VISIBLE
        binding.viewWithCustomChrome.visibility = View.VISIBLE
        binding.viewWithInstalledChrome.visibility = View.VISIBLE
        binding.textViewAllSavedList.visibility = View.VISIBLE
        binding.stopServerBtn.visibility = View.VISIBLE
        binding.txtNetworkInfo.visibility = View.VISIBLE
        binding.startServerBtn.visibility = View.GONE
        binding.textSeverState.setTextColor(
            ContextCompat.getColor(
                this@DE_MO_202100,
                R.color.holo_green_dark
            )
        )
    }


    @SuppressLint("SetTextI18n")
    private fun manageUIOnServerStops() {
        binding.addressText.text = "Address: --"
        binding.textSeverState.text = "Stopped"
        binding.textDisplayStoragePath.visibility = View.INVISIBLE
        binding.textCopyIpAdress.visibility = View.INVISIBLE
        binding.viewWithCustomChrome.visibility = View.INVISIBLE
        binding.viewWithInstalledChrome.visibility = View.INVISIBLE
        binding.textViewAllSavedList.visibility = View.GONE
        binding.stopServerBtn.visibility = View.GONE
        binding.txtNetworkInfo.visibility = View.GONE
        binding.startServerBtn.visibility = View.VISIBLE
        binding.textSeverState.setTextColor(
            ContextCompat.getColor(
                this@DE_MO_202100,
                R.color.holo_red_dark
            )
        )

    }

    override fun onDestroy() {
        super.onDestroy()
        if (progressSeverService != null) {
            unregisterReceiver(progressSeverService)
        }
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        navigateBack()
    }

    private fun navigateBack() {
        val intent = Intent(applicationContext, MaintenanceActivity::class.java)
        startActivity(intent)
        finish()

    }

    private fun closeApplication() {
        val getServerState = sharedPreferences.getString(Constants.PR_SEVER_STATE, "").toString()
        if (getServerState == Constants.SL_Off) {
            if (!Utility.foregroundForSeverServiceClass(applicationContext)) {
                finishAndRemoveTask()
                Process.killProcess(Process.myPid())
            } else {
                showAlertDialog("Please kindly Stop the Sever , Then Exit Application")
            }
        } else {
            showAlertDialog("Please kindly Stop the Sever , Then Exit Application")
        }


    }

    private fun showAlertDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Server Status")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun setupServerActionButtons() {
        // Open in Chrome (already implemented earlier)
        binding.viewWithInstalledChrome.setOnClickListener {
            val getServerState =
                sharedPreferences.getString(Constants.PR_SEVER_STATE, "").toString()
            if (getServerState == Constants.SL_Running) {
                if (Utility.foregroundForSeverServiceClass(applicationContext)) {
                    val localUrl = sharedPreferences.getString(Constants.localUrl, "").toString()
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("$localUrl/index.html"))

                    try {
                        intent.setPackage("com.android.chrome")
                        startActivity(intent)
                    } catch (e: Exception) {
                        intent.setPackage(null)
                        startActivity(intent)
                    }
                }
            } else {
                showAlertDialog("Sever needs to be Running before launching Chrome Browser")
            }

        }

        // Open inside WebView page
        binding.viewWithCustomChrome.setOnClickListener {
            val getServerState =
                sharedPreferences.getString(Constants.PR_SEVER_STATE, "").toString()
            if (getServerState == Constants.SL_Running) {
                val localUrl = sharedPreferences.getString(Constants.localUrl, "").toString()

                try {
                    val customTabsIntent = CustomTabsIntent.Builder()
                        .setShowTitle(true) // Show page title
                        .setUrlBarHidingEnabled(true) // Hide URL bar when scrolling
                        .setShareState(CustomTabsIntent.SHARE_STATE_ON) // Add share option
                        .build()

                    customTabsIntent.launchUrl(this, Uri.parse(localUrl))
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "No browser found to open link", Toast.LENGTH_SHORT).show()
                }
            } else {
                showAlertDialog("Sever needs to be Running before launching Chrome Browser")
            }
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

    @SuppressLint("MissingInflatedId")
    private fun showPopSetPort() {
        val bindingCM: CustomPortLayoutBinding = CustomPortLayoutBinding.inflate(
            layoutInflater
        )
        val builder = AlertDialog.Builder(this)
        builder.setView(bindingCM.getRoot())
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setCancelable(false)
        if (alertDialog.window != null) {
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.window!!.attributes.windowAnimations =
                sync2app.com.syncapplive.R.style.PauseDialogAnimation
        }



        bindingCM.textContinuPasswordDai.setOnClickListener {
            val port = bindingCM.eitTextEnterNewPassword.text.toString().toIntOrNull() ?: 8080
            if (port < 1000 || port > 9999) {
                Toast.makeText(this, "Port must be 4 digits (1000-9999)", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
                return@setOnClickListener
            }
            binding.portInput.setText(port.toString())
            Utility.hideKeyBoard(applicationContext, bindingCM.eitTextEnterNewPassword)
            alertDialog.dismiss()

        }

        bindingCM.imgCloseDialog.setOnClickListener {
            Utility.hideKeyBoard(applicationContext, bindingCM.eitTextEnterNewPassword)
            alertDialog.dismiss()
        }
        alertDialog.show()
    }


    private fun showSavedPathList() {

        customSavedDownloadDialog = Dialog(this)
        val bindingCm = CustomSavedPathListBinding.inflate(LayoutInflater.from(this))
        customSavedDownloadDialog.setContentView(bindingCm.root)
        customSavedDownloadDialog.setCancelable(true)
        customSavedDownloadDialog.setCanceledOnTouchOutside(true)
        customSavedDownloadDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val textErrorText = bindingCm.textErrorText
        val textClearAllData = bindingCm.textClearAllData




        bindingCm.apply {


            closeBs.setOnClickListener {
                Utility.hideKeyBoard(applicationContext, binding.editTextPath)
                customSavedDownloadDialog.dismiss()
            }


            imageCrossClose.setOnClickListener {
                Utility.hideKeyBoard(applicationContext, binding.editTextPath)
                customSavedDownloadDialog.dismiss()
            }

            textClearAllData.setOnClickListener {
                mUserViewModel.deleteAllUsers()
                Utility.hideKeyBoard(applicationContext, binding.editTextPath)
                customSavedDownloadDialog.dismiss()
            }


            handler.postDelayed(Runnable {
                recyclerSavedDownload.adapter = adapter
                recyclerSavedDownload.layoutManager = LinearLayoutManager(applicationContext)

                mUserViewModel.readAllData.observe(this@DE_MO_202100, Observer { user ->
                    adapter.setData(user)
                    if (user.isNotEmpty()) {
                        textErrorText.visibility = View.GONE
                        textClearAllData.visibility = View.VISIBLE
                    } else {
                        textClearAllData.visibility = View.GONE
                        textErrorText.visibility = View.VISIBLE
                    }
                })


            }, 200)
        }



        customSavedDownloadDialog.show()

    }

    override fun onItemClicked(item: IndexList) {

    }

    override fun onItemLongClicked(item: IndexList, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Path")
            .setMessage("Do you want to delete \"${item.VALUES}\"?")
            .setPositiveButton("Yes") { _, _ ->
                // Remove from DB
                mUserViewModel.deleteUser(item)

                Toast.makeText(this, "Deleted: ${item.VALUES}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }


//// the network info ///
//// the network info ///
//// the network info ///

    /*
private fun showNetworkInfo() {
    val info = StringBuilder()

    // Get WiFi SSID and Local IP
    val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager?
    wifiManager?.connectionInfo?.let { wifiInfo: WifiInfo ->
        val ssid = wifiInfo.ssid
        val ipInt = wifiInfo.ipAddress
        val localIp = Formatter.formatIpAddress(ipInt)

        info.append("NETWORK DETAILS").append("").append("\n")
        info.append("Connected to : ").append(ssid).append("\n")
        info.append("Local IP: ").append(localIp).append("\n")
        info.append("").append("").append("\n")
        info.append("==Click here to view Wifi settings==").append("").append("\n")

    }

    // Fetch Public IP in background using Coroutine
    CoroutineScope(Dispatchers.IO).launch {
        val publicIp = getPublicIP()
        withContext(Dispatchers.Main) {
            info.append("Public IP: ").append(publicIp).append("\n")
            binding.txtNetworkInfo.text = info.toString()
        }
    }
}

// Function to get public IP (runs in IO thread)
private fun getPublicIP(): String {
    return try {
        val url = URL("https://api.ipify.org")
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 5000
        connection.readTimeout = 5000

        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val ip = reader.readLine()
        reader.close()
        connection.disconnect()
        ip
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}

// Handle permission result
override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    if (requestCode == REQUEST_LOCATION_PERMISSION) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showNetworkInfo()
        } else {
            Toast.makeText(this, "Permission required to get WiFi SSID", Toast.LENGTH_SHORT).show()
        }
    }
}
}*/


    @SuppressLint("SetTextI18n")
    private fun showNetworkInfo() {
        val info = StringBuilder()
        info.append("NETWORK DETAILS\n\n")

        val cm =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        for (network in cm.allNetworks) {
            val caps = cm.getNetworkCapabilities(network) ?: continue
            val linkProperties = cm.getLinkProperties(network) ?: continue

            var connectionType = "Unknown"
            var networkName = "Unknown"

            when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    connectionType = "Wi-Fi"
                    val wifiManager =
                        applicationContext.getSystemService(WIFI_SERVICE) as WifiManager?
                    networkName =
                        wifiManager?.connectionInfo?.ssid?.replace("\"", "") ?: "Unknown SSID"
                }

                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    connectionType = "Ethernet"
                    networkName = linkProperties.interfaceName ?: "Ethernet"
                }
            }

            for (linkAddress in linkProperties.linkAddresses) {
                val host = linkAddress.address
                if (host is Inet4Address && !host.isLoopbackAddress) {
                    info.append("Connected via: $connectionType\n")
                    info.append("Network Name: $networkName\n")
                    info.append("Local IP: ${host.hostAddress}\n\n")
                }
            }
        }

        // Fetch Public IP in background
        CoroutineScope(Dispatchers.IO).launch {
            val publicIp = Utility.getPublicIP()
            withContext(Dispatchers.Main) {
                info.append("Public IP: $publicIp\n")
                binding.txtNetworkInfo.text = info.toString()
            }
        }
    }
}