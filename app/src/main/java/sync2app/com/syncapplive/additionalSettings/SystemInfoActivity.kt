package sync2app.com.syncapplive.additionalSettings

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import sync2app.com.syncapplive.R
import sync2app.com.syncapplive.additionalSettings.HardwareModel.RetrofitInstance
import sync2app.com.syncapplive.additionalSettings.autostartAppOncrash.Methods
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.databinding.ActivitySystemInfoPowellBinding
import sync2app.com.syncapplive.databinding.CustomHardwareApprovedBinding
import sync2app.com.syncapplive.databinding.CustomHardwareFailedBinding
import java.io.File

class SystemInfoActivity : AppCompatActivity() {


    private lateinit var binding: ActivitySystemInfoPowellBinding


    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private val hardwareData: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.HARD_WARE_DATA,
            Context.MODE_PRIVATE
        )
    }


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



    private val simpleSavedPassword: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SIMPLE_SAVED_PASSWORD,
            Context.MODE_PRIVATE
        )
    }


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySystemInfoPowellBinding.inflate(layoutInflater)
        setContentView(binding.root)


        applyOritenation()


       // setUpBackground()  ///   for dark theme, enable it latter if needed

        //add exception
        Methods.addExceptionHandler(this)


        val get_imgToggleImageBackground = sharedBiometric.getString(Constants.imgToggleImageBackground, "")
        val get_imageUseBranding = sharedBiometric.getString(Constants.imageUseBranding, "")
        if (get_imgToggleImageBackground.equals(Constants.imgToggleImageBackground) && get_imageUseBranding.equals(Constants.imageUseBranding) ){
            loadBackGroundImage()
        }




        binding.apply {

            closeBs.setOnClickListener {
                onBackPressed()
            }



            binding.textHardWareStatus.setOnClickListener {
                if (isNetworkAvailable()) {
                    fetchData()
                } else {
                    showToastMessage("No Internet Connection")
                    binding.progressBar.visibility = View.GONE
                    binding.viewCover.visibility = View.GONE
                    binding.textErrorText.visibility = View.GONE
                }

            }


            startNetWorkCall()


            textTextInfo.text = "Device name"
            textdeviceDescription.text = Build.DEVICE.toString()

            textModel.text = "Model"
            textModelDescription.text = Build.MODEL.toString()

            textMananufacturer.text = "Manufacturer"
            textManufacturerDescription.text = Build.MANUFACTURER.toString()

            textBrand.setText("Brand")
            textBrandDescription.text = Build.BRAND.toString()

            textAndroidTvOs.text = "OS Version"
            textAndroidTvOsDescription.text = Build.VERSION.RELEASE.toString()

            textAndroidSDK.text = "SDK Version"
            textSDKDescription.text = Build.VERSION.SDK_INT.toString()

            textTvOSBuildVersion.text = "Build Number"
            textTvOSBuildVersionDescription.text = Build.DISPLAY.toString()

        }

    }


    private fun startNetWorkCall() {
        binding.progressBar.visibility = View.VISIBLE
        binding.viewCover.visibility = View.VISIBLE
        binding.textErrorText.visibility = View.VISIBLE

        handler.postDelayed(Runnable {
            if (isNetworkAvailable()) {
                fetchData()
            } else {
                showToastMessage("No Internet Connection")
                binding.progressBar.visibility = View.GONE
                binding.viewCover.visibility = View.GONE
                binding.textErrorText.visibility = View.GONE
            }

        }, 1000)
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun fetchData() {

        binding.progressBar.visibility = View.VISIBLE
        binding.viewCover.visibility = View.VISIBLE
        binding.textErrorText.visibility = View.VISIBLE


        GlobalScope.launch(Dispatchers.IO) {
            try {

                val get_tMaster = simpleSavedPassword.getString(Constants.get_editTextMaster, "").toString()
                val get_UserID = simpleSavedPassword.getString(Constants.get_UserID, "").toString()
                val get_LicenseKey = simpleSavedPassword.getString(Constants.get_LicenseKey, "").toString()


                val baseUrl = "$get_tMaster/$get_UserID/$get_LicenseKey/Hdw/"

                RetrofitInstance.initialize(baseUrl)

                val response = RetrofitInstance.api.getAppConfig()

                val editor = hardwareData.edit()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        var isReady = false

                        binding.progressBar.visibility = View.GONE
                        binding.viewCover.visibility = View.GONE
                        binding.textErrorText.visibility = View.GONE

                        val deviceList = response.body()
                        deviceList?.let {

                            for (device in it.devices) {
                                if (device.deviceName == Build.DEVICE.toString()
                                    && device.manufacturer == Build.MANUFACTURER.toString()
                                    && device.model == Build.MODEL.toString()
                                    && device.brand == Build.BRAND.toString()
                                    && device.osVersion == Build.VERSION.RELEASE.toString()
                                    && device.sdkVersion == Build.VERSION.SDK_INT.toString()
                                    && device.buildNumber == Build.DISPLAY.toString()
                                ) {

                                    binding.textBtnhardWareAprroved.setBackgroundResource(R.drawable.round_edit_text_solid_green_design)
                                    binding.textBtnhardWareAprroved.text = "HARDWARE APPROVED"
                                    binding.imageNotApproved.visibility = View.INVISIBLE
                                    binding.imageApproved.visibility = View.VISIBLE

                                    editor.putString("deviceName", device.deviceName)
                                    editor.putString("manufacturer", device.manufacturer)
                                    editor.putString("model", device.model)
                                    editor.putString("brand", device.brand)
                                    editor.putString("osVersion", device.osVersion)
                                    editor.putString("sdkVersion", device.sdkVersion)
                                    editor.putString("buildNumber", device.buildNumber)
                                    editor.apply()
                                    isReady = true

                                }

                            }

                        }


                        if (isReady == true){

                            var deviceName = hardwareData.getString("deviceName", "").toString()
                            var manufacturer = hardwareData.getString("manufacturer", "").toString()
                            var model = hardwareData.getString("model", "").toString()
                            var brand = hardwareData.getString("brand", "").toString()
                            var osVersion = hardwareData.getString("osVersion", "").toString()
                            var sdkVersion = hardwareData.getString("sdkVersion", "").toString()
                            var buildNumber = hardwareData.getString("buildNumber", "").toString()

                            showPopHardWareApproved(deviceName, model, manufacturer, brand, osVersion, sdkVersion, buildNumber)

                        }else{
                            showPopHardWFailedTotal()
                            isReady = false
                        }


                    } else {
                        showToastMessage("Failed to fetch data")
                        binding.progressBar.visibility = View.GONE
                        binding.viewCover.visibility = View.GONE
                        binding.textErrorText.visibility = View.GONE
                    }
                }

            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    showToastMessage("HTTP Exception: ${e.message()}")
                    binding.progressBar.visibility = View.GONE
                    binding.viewCover.visibility = View.GONE
                    binding.textErrorText.visibility = View.GONE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToastMessage("Error: ${e.message}")
                    // binding.textView13?.text = "Error: ${e.message}"
                    binding.progressBar.visibility = View.GONE
                    binding.viewCover.visibility = View.GONE
                    binding.textErrorText.visibility = View.GONE
                }
            }
        }
    }


    @SuppressLint("MissingInflatedId", "SetTextI18n")
    private fun showPopHardWFailedTotal(
    ) {

        val binding: CustomHardwareFailedBinding =
            CustomHardwareFailedBinding.inflate(layoutInflater)
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(binding.root)

        val alertDialog = alertDialogBuilder.create()
        // alertDialog.setCancelable(false)


        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))








        binding.apply {


            val preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)

            if (preferences.getBoolean("darktheme", false)) {
                consMainAlertSubLayout.setBackgroundResource(R.drawable.card_design_account_number_dark_pop_layout)

                textModel.setTextColor(resources.getColor(R.color.dark_light_gray_pop))
                textModel3.setTextColor(resources.getColor(R.color.dark_light_gray_pop))
                textDeviceName.setTextColor(resources.getColor(R.color.dark_light_gray_pop))
                textDeviceName3.setTextColor(resources.getColor(R.color.dark_light_gray_pop))
                textManufacturer.setTextColor(resources.getColor(R.color.dark_light_gray_pop))
                textManufacturer2.setTextColor(resources.getColor(R.color.dark_light_gray_pop))
                textBrand.setTextColor(resources.getColor(R.color.dark_light_gray_pop))
                textBrand2.setTextColor(resources.getColor(R.color.dark_light_gray_pop))
                textOsVersion.setTextColor(resources.getColor(R.color.dark_light_gray_pop))
                textOsVersion2.setTextColor(resources.getColor(R.color.dark_light_gray_pop))
                textSdkVersion.setTextColor(resources.getColor(R.color.dark_light_gray_pop))
                textSdkVersion2.setTextColor(resources.getColor(R.color.dark_light_gray_pop))
                textBuildNumber.setTextColor(resources.getColor(R.color.dark_light_gray_pop))
                textBuildNumber2.setTextColor(resources.getColor(R.color.dark_light_gray_pop))
                textDisplayResult.setTextColor(resources.getColor(R.color.dark_light_gray_pop))


                textShareApp.setTextColor(resources.getColor(R.color.dark_light_gray_pop))
                textShareApp.setBackgroundResource(R.drawable.card_design_buy_gift_card_extra_dark_black)

                textContinuPassword2.setTextColor(resources.getColor(R.color.dark_light_gray_pop))
                textContinuPassword2.setBackgroundResource(R.drawable.card_design_buy_gift_card_extra_dark_black)



                val drawable_imagSucessful = ContextCompat.getDrawable(applicationContext, R.drawable.ic_failed_circle)
                drawable_imagSucessful?.setColorFilter(ContextCompat.getColor(applicationContext, R.color.red), PorterDuff.Mode.SRC_IN)
                imagSucessful.setImageDrawable(drawable_imagSucessful)


                val drawable_imageCheckDevice = ContextCompat.getDrawable(applicationContext, R.drawable.ic_failed_hardware_circle)
                drawable_imageCheckDevice?.setColorFilter(ContextCompat.getColor(applicationContext, R.color.red), PorterDuff.Mode.SRC_IN)
                imageCheckDevice.setImageDrawable(drawable_imageCheckDevice)
                imageModel.setImageDrawable(drawable_imageCheckDevice)
                imageManufactuer.setImageDrawable(drawable_imageCheckDevice)
                imageBrand.setImageDrawable(drawable_imageCheckDevice)
                imageOsVersion.setImageDrawable(drawable_imageCheckDevice)
                imageSDK.setImageDrawable(drawable_imageCheckDevice)
                imageBuildNumber.setImageDrawable(drawable_imageCheckDevice)




            }



        }



        binding.apply {

            textDeviceName3.text =  Build.DEVICE + ""

            textModel3.text =   Build.MODEL + ""

            textManufacturer2.text =   Build.MANUFACTURER + ""
            textBrand2.text =   Build.BRAND + ""

            textOsVersion2.text =   Build.VERSION.RELEASE + ""

            textSdkVersion2.text =    Build.VERSION.SDK_INT.toString() + ""
            textBuildNumber2.text =   Build.DISPLAY


            textContinuPassword2.setOnClickListener {
                alertDialog.dismiss()
            }

            textShareApp.setOnClickListener {

                val deviceInfoBuilder = StringBuilder()
                deviceInfoBuilder.append("Device name: ").append(Build.DEVICE).append("\n")
                deviceInfoBuilder.append("Model: ").append(Build.MODEL).append("\n")
                deviceInfoBuilder.append("Manufacturer: ").append(Build.MANUFACTURER).append("\n")
                deviceInfoBuilder.append("Brand: ").append(Build.BRAND).append("\n")
                deviceInfoBuilder.append("OS Version: ").append(Build.VERSION.RELEASE).append("\n")
                deviceInfoBuilder.append("SDK Version: ").append(Build.VERSION.SDK_INT).append("\n")
                deviceInfoBuilder.append("Build Number: ").append(Build.DISPLAY).append("\n")

                val deviceInformation = deviceInfoBuilder.toString()

                sendShareData(deviceInformation)
                alertDialog.dismiss()
            }

        }


        alertDialog.show()


    }






    @SuppressLint("MissingInflatedId", "SetTextI18n")
    private fun showPopHardWFailed(
        deviceName: String,
        model: String,
        manufacturer: String,
        brand: String,
        osVersion: String,
        sdkVersion: String,
        buildNumber: String,
    ) {

        val binding: CustomHardwareFailedBinding =
            CustomHardwareFailedBinding.inflate(layoutInflater)
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(binding.root)

        val alertDialog = alertDialogBuilder.create()
        // alertDialog.setCancelable(false)


        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        binding.apply {


            if ( deviceName.equals(Build.DEVICE + "") ) {
                textDeviceName3.text = deviceName

            }else{
                textDeviceName3.text =  Build.DEVICE + ""
            }


            if ( model.equals(Build.MODEL + "") ) {
                textModel3.text = model
            }else{
                textModel3.text =  Build.MODEL + ""
            }


            if (manufacturer.equals(Build.MANUFACTURER + "") ) {
                textManufacturer2.text = manufacturer
            }else{
                textManufacturer2.text =   Build.MANUFACTURER + ""
            }




            if ( brand.equals(Build.BRAND + "") ) {
                textBrand2.text =  Build.BRAND + ""
            }else{
                textBrand2.text = brand
            }



            if (osVersion.equals(Build.VERSION.RELEASE.toString()) ) {
                textOsVersion2.text =  Build.VERSION.RELEASE + ""
            }else{
                textOsVersion2.text = osVersion
            }



            if (sdkVersion.equals(Build.VERSION.SDK_INT.toString()) ) {
                textSdkVersion2.text =  Build.VERSION.SDK_INT.toString() + ""
            }else{
                textSdkVersion2.text =  sdkVersion
            }




            if ( buildNumber.equals(Build.DISPLAY.toString()) ) {
                textBuildNumber2.text =   Build.DISPLAY + ""
            }else{
                textBuildNumber2.text =  buildNumber
            }



            if (deviceName != Build.DEVICE + "") {
                imageCheckDevice.setBackgroundResource(R.drawable.ic_failed_hardware_circle)
            }


            if (model != Build.MODEL + "") {
                imageModel.setBackgroundResource(R.drawable.ic_failed_hardware_circle)
            }


            if (manufacturer != Build.MANUFACTURER + "") {
                imageManufactuer.setBackgroundResource(R.drawable.ic_failed_hardware_circle)
            }

            if (brand != Build.BRAND + "") {
                imageBrand.setBackgroundResource(R.drawable.ic_failed_hardware_circle)
            }

            if (osVersion != Build.VERSION.RELEASE.toString() + "") {
                imageOsVersion.setBackgroundResource(R.drawable.ic_failed_hardware_circle)
            }


            if (sdkVersion != Build.VERSION.SDK_INT.toString() + "" ) {
                imageSDK.setBackgroundResource(R.drawable.ic_failed_hardware_circle)
            }


            if (buildNumber != Build.DISPLAY + "" ) {
                imageBuildNumber.setBackgroundResource(R.drawable.ic_failed_hardware_circle)
            }


            textContinuPassword2.setOnClickListener {
                alertDialog.dismiss()
            }

        }


        alertDialog.show()


    }






    @SuppressLint("MissingInflatedId")
    private fun showPopHardWareApproved(
        deviceName: String,
        model: String,
        manufacturer: String,
        brand: String,
        osVersion: String,
        sdkVersion: String,
        buildNumber: String,
    ) {

        val binding: CustomHardwareApprovedBinding =
            CustomHardwareApprovedBinding.inflate(layoutInflater)
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(binding.root)

        val alertDialog = alertDialogBuilder.create()
        // alertDialog.setCancelable(false)


        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))





        binding.apply {

            textDeviceName3.text = deviceName
            textModel3.text = model
            textManufacturer2.text = manufacturer
            textOsVersion2.text = osVersion
            textSdkVersion2.text = sdkVersion
            textBuildNumber2.text = buildNumber



            textContinuPassword2.setOnClickListener {
                alertDialog.dismiss()
            }




            textShareApp.setOnClickListener {

                val deviceInfoBuilder = StringBuilder()
                deviceInfoBuilder.append("Device name: ").append(deviceName).append("\n")
                deviceInfoBuilder.append("Model: ").append(model).append("\n")
                deviceInfoBuilder.append("Manufacturer: ").append(manufacturer).append("\n")
                deviceInfoBuilder.append("Brand: ").append(brand).append("\n")
                deviceInfoBuilder.append("OS Version: ").append(osVersion).append("\n")
                deviceInfoBuilder.append("SDK Version: ").append(sdkVersion).append("\n")
                deviceInfoBuilder.append("Build Number: ").append(buildNumber).append("\n")

                val deviceInformation = deviceInfoBuilder.toString()

                sendShareData(deviceInformation)
                alertDialog.dismiss()
            }


        }


        alertDialog.show()


    }




    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }


    private fun showToastMessage(messages: String) {

        try {
            Toast.makeText(applicationContext, messages, Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    fun sendShareData(crashMessage: String?) {
        //  val email = "kola@moreadvice.co.uk"
        val email = "support@syn2app.com"
        val subject = "Crash Report"

        val uriText = "mailto:" + Uri.encode(email) +
                "?subject=" + Uri.encode(subject) +
                "&body=" + Uri.encode(crashMessage)

        val uri = Uri.parse(uriText)

        // Intent for sending text
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, crashMessage)
        sendIntent.type = "text/plain"

        // Intent for sending email
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = uri

        // Create a chooser with both intents
        val chooserIntent = Intent.createChooser(sendIntent, "Send message via:")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(emailIntent))

        try {
            startActivity(chooserIntent)
        } catch (e: ActivityNotFoundException) {
            // Handle case where no email app is available
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
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


    @SuppressLint("SourceLockedOrientationActivity")
    private fun applyOritenation() {
        val getState = sharedBiometric.getString(Constants.IMG_TOGGLE_FOR_ORIENTATION, "").toString()

        if (getState == Constants.USE_POTRAIT){
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        }else if (getState == Constants.USE_LANDSCAPE){

            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        }else if (getState == Constants.USE_UNSEPECIFIED){
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        }

    }




}
