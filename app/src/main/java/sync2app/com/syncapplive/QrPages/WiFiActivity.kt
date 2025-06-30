package sync2app.com.syncapplive.QrPages

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import sync2app.com.syncapplive.WebViewPage
import sync2app.com.syncapplive.R
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.databinding.ActivityWiFiBinding
import java.io.File


class WiFiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWiFiBinding

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



    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWiFiBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val get_imgToggleImageBackground = sharedBiometric.getString(Constants.imgToggleImageBackground, "")
        val get_imageUseBranding = sharedBiometric.getString(Constants.imageUseBranding, "")
        if (get_imgToggleImageBackground.equals(Constants.imgToggleImageBackground) && get_imageUseBranding.equals(Constants.imageUseBranding) ){
            loadBackGroundImage()
        }




        val getWifiName: String = sharedBiometric.getString("getWifiName", "").toString()
        val get_WAp: String = sharedBiometric.getString("get_WAp", "").toString()
        val get_Password: String = sharedBiometric.getString("get_Password", "").toString()
        val get_bolan: String = sharedBiometric.getString("get_bolan", "").toString()


        binding.apply {
            resultFieldWifi.setText(getString(R.string.ssid_value, getWifiName));
            resultFieldWifiEncryption.setText(getString(R.string.encryption_value, get_WAp));
            resultFieldWifiPw.setText(getString(R.string.password_value, get_Password));

        }



        binding.textAddNetwork.setOnClickListener {
            myWiFIConnect(getWifiName,get_WAp , get_Password)

        }


        binding.textCopypassword.setOnClickListener {
            copyWifiPassword(get_Password)
        }


        binding.textCopyRowData.setOnClickListener {
            val messageState = "SSID: $getWifiName\nEncryption: $get_WAp\nPassword: $get_Password\nH: $get_bolan"
            copyWifiPasswordRawData(messageState)
        }


        binding.textDoNothing.setOnClickListener {
          endMyActivity()
        }


        binding.closeBs.setOnClickListener {
            endMyActivity()
        }


    }

    private fun loadBackGroundImage() {
        val sharedP = getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, MODE_PRIVATE)
        val getFolderClo = sharedP.getString(Constants.getFolderClo, "").toString()
        val getFolderSubpath = sharedP.getString(Constants.getFolderSubpath, "").toString()

        val baseDir = getExternalFilesDir(null) // App-private external storage
        val relativePath = "${Constants.Syn2AppLive}/$getFolderClo/$getFolderSubpath/${Constants.App}/Config"
        val folder = File(baseDir, relativePath)
        val fileTypes = "app_background.png"
        val file = File(folder, fileTypes)

        if (file.exists()) {
            Glide.with(this).load(file).centerCrop().into(binding.backgroundImage)
        }

    }


    @SuppressLint("StringFormatInvalid")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun myWiFIConnect(ssid: String, get_WAp: String, pw: String) {
        val encryptionType = get_WAp.trim().uppercase()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            showAlertDialog(R.string.android_version_does_not_support_feature)
            return
        }

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val currentNetworkInfo = wifiManager.connectionInfo
        val currentSSID = currentNetworkInfo.ssid.replace("\"", "")

        if (currentSSID == ssid) {
            showToast("Already connected to Network $ssid")
            return
        }


        when {
            encryptionType.isEmpty() -> connectToWifi(ssid)
            encryptionType == "NOPASS" && pw == null -> { showAlertDialog(sync2app.com.syncapplive.R.string.cannot_connect_to_encrypted_wifi_without_password)
            }
            else -> {
                val suggestion = when (encryptionType) {
                    "NOPASS" -> null
                    "WPA", "WPA2" -> WifiNetworkSuggestion.Builder().setSsid(ssid).setWpa2Passphrase(pw).build()
                    "WPA3" -> WifiNetworkSuggestion.Builder().setSsid(ssid).setWpa3Passphrase(pw).build()
                    else -> {
                        showAlertDialog(R.string.unsupported_wifi_encryption, encryptionType)
                        return
                    }
                }
                openWifiIntent(suggestion!!)
            }
        }
    }

    private fun showAlertDialog(@StringRes messageId: Int, vararg formatArgs: Any) {
        AlertDialog.Builder(this@WiFiActivity)
            .setTitle(android.R.string.dialog_alert_title)
            .setMessage(getString(messageId, *formatArgs))
            .setNegativeButton(android.R.string.ok, null)
            .show()
    }



    @RequiresApi(Build.VERSION_CODES.R)
    private fun connectToWifi(ssid: String) {
        val suggestion = WifiNetworkSuggestion.Builder().setSsid(ssid).build()
        openWifiIntent(suggestion)
    }



    @RequiresApi(Build.VERSION_CODES.R)
    private fun openWifiIntent(suggestion: WifiNetworkSuggestion) {
        val intent = Intent(Settings.ACTION_WIFI_ADD_NETWORKS)
        val networks = ArrayList<WifiNetworkSuggestion>()
        networks.add(suggestion)
        intent.putExtra(Settings.EXTRA_WIFI_NETWORK_LIST, networks)
        startActivity(intent)
    }


    private fun copyWifiPasswordRawData(pw: String) {
        val clipboard = applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.text = pw
    }


    private fun copyWifiPassword(pw: String) {
        val clipboard = applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.text = pw
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        endMyActivity()
    }

    private fun showToast(messages: String) {

        try {
            Toast.makeText(applicationContext, messages, Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
        }
    }


    private fun endMyActivity() {

        val editor = sharedBiometric.edit()
        editor.remove("getWifiName")
        editor.remove("get_WAp")
        editor.remove("get_Password")
        editor.remove("get_bolan")
        editor.apply()
        startActivity(Intent(applicationContext, WebViewPage::class.java))
        finish()
    }

}