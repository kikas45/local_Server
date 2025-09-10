package local.com.server.additionalSettings

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import local.com.server.additionalSettings.utils.Constants
import local.com.server.databinding.ActivityMaintenaceBinding
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import local.com.server.myService.ServerService

class SettingMaintenaceActivity : AppCompatActivity() {

    private val sharedPrefs: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_BIOMETRIC,
            Context.MODE_PRIVATE
        )
    }

    private lateinit var binding: ActivityMaintenaceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaintenaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔹 Restore saved state for the switch
        val hideBarEnabled = sharedPrefs.getBoolean(Constants.KEY_HIDE_BAR, false)
        binding.imageUseBranding.isChecked = hideBarEnabled
        updateHideBarText(hideBarEnabled)



        binding.closeBs.setOnClickListener {
            navigateBackToSetting()
        }

        // 🔹 Handle switch toggle
        binding.imageUseBranding.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean(Constants.KEY_HIDE_BAR, isChecked).apply()
            updateHideBarText(isChecked)
        }

        // 🔹 Open Device Home
        binding.textDeviceHome.setOnClickListener {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        }

        // 🔹 Open Wi-Fi Settings
        binding.textWIFISettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
        }

        // 🔹 Open Device Settings
        binding.textDeviceSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
        }

        binding.textSeverEgnine.setOnClickListener {
            sharedPrefs.edit().putString(Constants.KEY_NAVIGATOR, Constants.SettingMaintenaceActivity).apply()
            val intent = Intent(this, SeverEngineActivity::class.java)
            startActivity(intent)
            finish()
        }



        binding.textExitApplication.setOnClickListener {
            applicationContext.stopService(Intent(applicationContext, ServerService::class.java))
            Toast.makeText(applicationContext, "Please wait", Toast.LENGTH_SHORT).show()
            lifecycleScope.launch { 
                delay(3000)
                finishAndRemoveTask()
                android.os.Process.killProcess(android.os.Process.myTid())
            }
        }

        binding.textFinishApp.setOnClickListener {
           finish()
        }

    }

    private fun updateHideBarText(hide: Boolean) {
        binding.textHideSattusBar.text = if (hide) {
            "Show Maintenance Bar"
        } else {
            "Hide Maintenance Bar"
        }
    }


    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        navigateBackToSetting()
    }


    private fun navigateBackToSetting() {
        val intent = Intent(this, DemoWebActivity::class.java)
        startActivity(intent)
        finish()

    }

}
