package smarty.com.excel.additionalSettings.appAutoBoot

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import smarty.com.excel.additionalSettings.TvActivityOrAppMode
import smarty.com.excel.additionalSettings.utils.Constants

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Check if the received intent is BOOT_COMPLETED
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {

            // Access shared preferences
            val sharedBiometric = context.getSharedPreferences(Constants.SHARED_BIOMETRIC, AppCompatActivity.MODE_PRIVATE)
            val getStateOfBootToggle = sharedBiometric.getString(Constants.imgEnableAutoBoot, "") ?: ""

            // Check if auto boot is enabled
            if (getStateOfBootToggle == Constants.imgEnableAutoBoot) {

                // Check if app is already running
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val isAppRunning = activityManager.runningAppProcesses.any {
                    it.processName == context.packageName
                }

                if (!isAppRunning) {
                    // Start SplashVideoActivity only if app isn't running
                    val activityIntent = Intent(context, TvActivityOrAppMode::class.java)
                    activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(activityIntent)
                }
            }
        }
    }
}







/*

package sync2app.com.syncapplive.additionalSettings.appAutoBoot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import sync2app.com.syncapplive.additionalSettings.SplashVideoActivity
import sync2app.com.syncapplive.additionalSettings.utils.Constants

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val sharedBiometric = context.getSharedPreferences(Constants.SHARED_BIOMETRIC, AppCompatActivity.MODE_PRIVATE)
        val getStateOfBootToggle = sharedBiometric.getString(Constants.imgEnableAutoBoot, "").toString()

        // Only proceed if the OS version is Android 11 (API 30) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            Intent.ACTION_BOOT_COMPLETED == intent.action &&
            getStateOfBootToggle == Constants.imgEnableAutoBoot
        ) {
            val activityIntent = Intent(context, SplashVideoActivity::class.java)
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(activityIntent)
        }
    }
}




*/



/// original code

/*


package sync2app.com.syncapplive.additionalSettings.appAutoBoot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import sync2app.com.syncapplive.additionalSettings.SplashVideoActivity
import sync2app.com.syncapplive.additionalSettings.utils.Constants

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val sharedBiometric = context.getSharedPreferences(Constants.SHARED_BIOMETRIC, AppCompatActivity.MODE_PRIVATE)
        val getStateOfBootToggle = sharedBiometric.getString(Constants.imgEnableAutoBoot, "").toString()

        if (Intent.ACTION_BOOT_COMPLETED == intent.action && getStateOfBootToggle == Constants.imgEnableAutoBoot) {
            val activityIntent = Intent(context, SplashVideoActivity::class.java)
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(activityIntent)
        }
    }
}
*/

