package local.com.server.additionalSettings

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import local.com.server.R
import local.com.server.additionalSettings.urlchecks.checkStoragePermission
import local.com.server.additionalSettings.urlchecks.requestStoragePermission
import local.com.server.additionalSettings.utils.Constants
import local.com.server.additionalSettings.utils.Utility
import local.com.server.databinding.ActivityTvOrAppModePageBinding
import local.com.server.databinding.CustomExitAppOrNotBinding
import local.com.server.databinding.CustomGrantAccessPageBinding
import local.com.server.databinding.CustomPopDisplayOverAppsBinding
import local.com.server.databinding.CustomeAllowAppWriteSystemBinding
import local.com.server.databinding.ProgressValidateUserDialogLayoutBinding

class TvActivityOrAppMode : AppCompatActivity() {
    private lateinit var binding: ActivityTvOrAppModePageBinding

    private val handler: Handler by lazy { Handler(Looper.getMainLooper()) }
    private var isDialogPermissionShown = false
    private var btnisClicked = false
    private lateinit var customProgressDialog: Dialog

    private val sharedBiometric: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_BIOMETRIC, Context.MODE_PRIVATE
        )
    }


    private val multiplePermissionId = 14
    private val multiplePermissionNameList = if (Build.VERSION.SDK_INT >= 33) {
        arrayListOf(
            android.Manifest.permission.READ_MEDIA_AUDIO,
            android.Manifest.permission.READ_MEDIA_VIDEO,
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.POST_NOTIFICATIONS,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
            android.Manifest.permission.RECORD_AUDIO
        )
    } else {
        arrayListOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
            android.Manifest.permission.RECORD_AUDIO
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val alreadyGranted = sharedBiometric.getBoolean("permissions_granted", false)
        if (alreadyGranted) {
            // Skip this activity directly
            startActivity(Intent(this, LightActivity::class.java))
            finish()
            return
        }

        binding = ActivityTvOrAppModePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textInstallApp.setOnClickListener {
            btnisClicked = true
            startPermissionProcess()
        }
    }

    override fun onResume() {
        super.onResume()
        if (btnisClicked) {
            // Always re-check permissions when returning to this activity
            startPermissionProcess()
        }
    }


    private fun startPermissionProcess() {


        if (Build.VERSION.SDK_INT >= 30) {

            when {
                Build.VERSION.SDK_INT >= 30 && !isIgnoringBatteryOptimizations(this, packageName) -> {
                    requestIgnoreBatteryOptimizations()
                }
                Build.VERSION.SDK_INT >= 23 && !Settings.System.canWrite(applicationContext) -> {
                    showPopAllowAppToWriteSystem()
                }
                Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this) -> {
                    showPop_For_Allow_Display_Over_Apps()
                }
                !checkStoragePermission(this) -> {
                    showPop_For_Grant_Permsiion()
                }
                else -> {
                    // Runtime dangerous permissions
                    lifecycleScope.launch {
                        delay(800)
                        checkMultiplePermissions()
                    }
                }
            }



        } else {

            when {
                !checkStoragePermission(this) -> {
                    showPop_For_Grant_Permsiion()
                }
                else -> {
                    // Runtime dangerous permissions
                    lifecycleScope.launch {
                        delay(800)
                        checkMultiplePermissions()
                    }
                }
            }


        }

    }



    /** Battery Optimization */
    @SuppressLint("BatteryLife")
    private fun requestIgnoreBatteryOptimizations() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }

    private fun isIgnoringBatteryOptimizations(context: Context, packageName: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.isIgnoringBatteryOptimizations(packageName)
        } else false
    }

    /** Write Settings */
    private fun requestWriteSettingsPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }

    /** Overlay */
    private fun checkOverlayBackground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
        }
    }

    /** Runtime permissions */
    private fun checkMultiplePermissions() {
        val listPermissionNeeded = arrayListOf<String>()
        for (permission in multiplePermissionNameList) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                listPermissionNeeded.add(permission)
            }
        }
        if (listPermissionNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionNeeded.toTypedArray(), multiplePermissionId)
        } else {
            onAllPermissionsGranted()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == multiplePermissionId) {
            if (grantResults.isNotEmpty()) {
                var isGranted = true
                for (result in grantResults) {
                    if (result == PackageManager.PERMISSION_DENIED) {
                        isGranted = false
                        break
                    }
                }
                if (isGranted) {
                    onAllPermissionsGranted()
                } else {
                    if (!isDialogPermissionShown) {
                        isDialogPermissionShown = true
                        showPermissionDeniedDialog()
                    }
                }
            }
        }
    }

    /** Final Step */
    private fun onAllPermissionsGranted() {
        btnisClicked = false
        showCustomProgressDialog()

        // Save flag in sharedBiometric
        sharedBiometric.edit()
            .putBoolean("permissions_granted", true)
            .apply()

        handler.postDelayed({ isReadToMove_All_Permission() }, 1000)
    }

    /** Custom dialogs */
    private fun showCustomProgressDialog() {
        try {
            customProgressDialog = Dialog(this)
            val binding = ProgressValidateUserDialogLayoutBinding.inflate(LayoutInflater.from(this))
            customProgressDialog.setContentView(binding.root)
            customProgressDialog.setCancelable(true)
            customProgressDialog.setCanceledOnTouchOutside(false)
            customProgressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            customProgressDialog.show()
        } catch (_: Exception) {}
    }

    private fun isReadToMove_All_Permission() {
        if (::customProgressDialog.isInitialized) {
            customProgressDialog.dismiss()
        }
        startActivity(Intent(applicationContext, SeverEngineActivity::class.java))
        finish()
    }

    @SuppressLint("MissingInflatedId")
    private fun showPopAllowAppToWriteSystem() {
        val bindingCM = CustomeAllowAppWriteSystemBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this).setView(bindingCM.root)
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.setCancelable(true)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window?.attributes?.windowAnimations = R.style.PauseDialogAnimation

        Utility.startPulseAnimationForText(bindingCM.imagSucessful)

        bindingCM.textContinuPassword2.setOnClickListener {
            requestWriteSettingsPermission()
            alertDialog.dismiss()
        }
        bindingCM.imgCloseDialog.setOnClickListener { alertDialog.dismiss() }

        alertDialog.show()

    }

    @SuppressLint("MissingInflatedId")
    private fun showPop_For_Grant_Permsiion() {
        val bindingCM = CustomGrantAccessPageBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this).setView(bindingCM.root)
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.setCancelable(true)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window?.attributes?.windowAnimations = R.style.PauseDialogAnimation

        Utility.startPulseAnimationForText(bindingCM.imagSucessful)

        bindingCM.textContinuPassword2.setOnClickListener {
            requestStoragePermission(this@TvActivityOrAppMode)
            alertDialog.dismiss()
        }
        bindingCM.imgCloseDialog.setOnClickListener { alertDialog.dismiss() }

        alertDialog.show()
    }

    @SuppressLint("MissingInflatedId")
    private fun showPop_For_Allow_Display_Over_Apps() {
        val bindingCM = CustomPopDisplayOverAppsBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this).setView(bindingCM.root)
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.setCancelable(true)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window?.attributes?.windowAnimations = R.style.PauseDialogAnimation

        Utility.startPulseAnimationForText(bindingCM.imagSucessful)

        bindingCM.textContinuPassword2.setOnClickListener {
            checkOverlayBackground()
            alertDialog.dismiss()
        }
        bindingCM.imgCloseDialog.setOnClickListener { alertDialog.dismiss() }

        alertDialog.show()
    }

    private fun showPermissionDeniedDialog() {
        val builder = AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle("Permission Required")
            .setMessage("Please grant the required permissions in the app settings to proceed.")
        isDialogPermissionShown = true

        builder.setPositiveButton("Go to Settings") { dialog: DialogInterface?, _ ->
            openAppSettings()
            dialog?.dismiss()
            isDialogPermissionShown = false
        }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface?, _ ->
            showToastMessage("Permission Denied!")
            isDialogPermissionShown = false
        }
        builder.show()
    }

    private fun showToastMessage(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", this.packageName, null)
        intent.data = uri
        startActivity(intent)
    }




    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        show_Pop_Confirm_Exit()
    }


    @SuppressLint("MissingInflatedId")
    private fun show_Pop_Confirm_Exit() {
        val binding: CustomExitAppOrNotBinding = CustomExitAppOrNotBinding.inflate(layoutInflater)
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(binding.getRoot())
        alertDialogBuilder.setCancelable(false)
        val alertDialog = alertDialogBuilder.create()

        // Set the background of the AlertDialog to be transparent
        if (alertDialog.window != null) {
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
        }


        binding.textLaunchMyOnline.setOnClickListener { view ->
            alertDialog.dismiss()
        }


        binding.textLaunchMyOffline.setOnClickListener { view ->
            finishAndRemoveTask()
            android.os.Process.killProcess(android.os.Process.myTid())
            alertDialog.dismiss()
        }


        // Show the AlertDialog
        alertDialog.show()
    }

}
