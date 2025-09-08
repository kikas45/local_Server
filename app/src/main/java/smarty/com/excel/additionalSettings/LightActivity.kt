package smarty.com.excel.additionalSettings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import smarty.com.excel.R
import smarty.com.excel.additionalSettings.utils.Constants
import smarty.com.excel.additionalSettings.utils.Utility
import smarty.com.excel.myService.ServerService

class LightActivity : AppCompatActivity() {

    private val sharedPrefSever: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SAVE_PORT_VALUES,
            Context.MODE_PRIVATE
        )
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_light)

        // init sever
        setupAutoStartSwitch()

        lifecycleScope.launch {
            delay(10000)
            startActivity(Intent(applicationContext, TvActivityOrAppMode::class.java))
            finish()
        }

    }

    @SuppressLint("ImplicitSamInstance")
    private fun setupAutoStartSwitch() {
        val autoStartEnabled = sharedPrefSever.getBoolean("${Constants.autoStartServer}", false)
        if (autoStartEnabled) {
            lifecycleScope.launch {
                delay(500)
                callServiceStart()
            }
        }else{
            applicationContext.stopService(Intent(applicationContext, ServerService::class.java))
        }
    }

    @SuppressLint("ImplicitSamInstance")
    private fun callServiceStart() {
        val savedPorto = sharedPrefSever.getString(Constants.localUrl, "").orEmpty()

        if (!Utility.foregroundForSeverServiceClass(applicationContext)) {
            // Service not running → start it
            applicationContext.stopService(Intent(applicationContext, ServerService::class.java))
            applicationContext.startService(Intent(applicationContext, ServerService::class.java))
        } else {
            // Service is running → validate config
            if (!isValidLocalAddress(savedPorto)) {
                // Stop service because config is invalid
                applicationContext.stopService(Intent(applicationContext, ServerService::class.java))
                Toast.makeText(
                    this,
                    "Server configuration is invalid. Please reconfigure and restart.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(this, "Server already running", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Simple validation for saved server address (e.g., 192.168.x.x:PORT).
     */
    private fun isValidLocalAddress(url: String): Boolean {
        if (url.isBlank()) return false
        if (url.contains("0.0.0.0")) return false
        if (url.contains("127.0.0.1")) return false // loopback
        if (!url.contains(":")) return false        // no port part
        return true
    }


}