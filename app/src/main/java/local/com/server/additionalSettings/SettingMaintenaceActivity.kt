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

    private val PREFS_NAME = "app_prefs"
    private val LAST_URL_KEY = "last_url"



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



        setUpUrlLaunch()

    }


    private fun setUpUrlLaunch() {
        binding.apply {
            // 🔹 Load last saved URL into EditText
            val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            val lastUrl = prefs.getString(LAST_URL_KEY, "")
            if (!lastUrl.isNullOrEmpty()) {
                binding.editTextUserID.setText(lastUrl)
            }


            binding.textLaunch.setOnClickListener {
                val url = binding.editTextUserID.text.toString().trim()
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    // 🔹 Save the URL for next time
                    prefs.edit().putString(LAST_URL_KEY, url).apply()

                    val intent = Intent(applicationContext, DemoWebActivity::class.java)
                    intent.putExtra(Constants.url_launch, url)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(applicationContext, "Enter a valid URL (http/https)", Toast.LENGTH_SHORT).show()
                }
            }

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
