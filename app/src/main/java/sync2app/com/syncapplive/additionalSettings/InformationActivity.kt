package sync2app.com.syncapplive.additionalSettings
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.hbb20.CountryCodePicker
import sync2app.com.syncapplive.R
import sync2app.com.syncapplive.WebViewPage
import sync2app.com.syncapplive.WelcomeSliderKT
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.additionalSettings.utils.Utility
import sync2app.com.syncapplive.constants
import sync2app.com.syncapplive.databinding.ActivityInformationBinding
import sync2app.com.syncapplive.databinding.CustomExitOrNotBinding
import sync2app.com.syncapplive.databinding.CustomPopInformationPageBinding
import sync2app.com.syncapplive.databinding.ProgressValidateUserDialogLayoutBinding
import java.util.regex.Pattern


class InformationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInformationBinding

    private val simpleSavedPassword: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SIMPLE_SAVED_PASSWORD,
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

    private val sharedTVAPPModePreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_TV_APP_MODE, Context.MODE_PRIVATE
        )
    }



    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private lateinit var customProgressDialog: Dialog

    private var isUserNameValid = false
    private var isPasswordValid = false
    private var isMyEmailValid = false
    private var isPhoneValid = false
    private var isComapnyValid = false

    private var getCountryCode = "+234 "
    private var getCountryName = "Nigeria"



    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }


    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyOritenation()

        setupListeners()

        setUpPasswordToggle()

        performButtonClick()

        setUpFullScreenWindows()



        binding.closeBs.setOnClickListener {

            show_Pop_Confirm_Exit("Attention!", "Are you sure you want to exit?")

        }

        Log.d("InitWebvIewloadStates", "InformationActivity: Page ")
    }



    private fun setUpFullScreenWindows() {
        val get_INSTALL_TV_JSON_USER_CLICKED = sharedTVAPPModePreferences.getString(Constants.INSTALL_TV_JSON_USER_CLICKED, "").toString()
        if (get_INSTALL_TV_JSON_USER_CLICKED != Constants.INSTALL_TV_JSON_USER_CLICKED) {
            val img_imgImmesriveModeToggle = preferences.getBoolean(Constants.immersive_mode, false)
            if (img_imgImmesriveModeToggle){
                Utility.hideSystemBars(window)
            }else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }


        }else{

            val immersive_Mode_APP = sharedTVAPPModePreferences.getBoolean(Constants.immersive_Mode_APP, false)
            if (immersive_Mode_APP) {
                Utility.hideSystemBars(window)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }

        }
    }




    private fun setUpPasswordToggle() {
        binding.apply {

            val text = "Important : Please ensure to fill correct details for account recovery and verification."
            val spannableString = SpannableString(text)
            val colorRed = resources.getColor(R.color.red)
            spannableString.setSpan(
                ForegroundColorSpan(colorRed),
                0,
                "Important :".length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            textView22.text = spannableString




            imgToggle.setOnClickListener {
                imgToggle.visibility = View.INVISIBLE
                imgToggleNzotVisible.visibility = View.VISIBLE
                eitTextEnterPasswordDia.transformationMethod = null
                eitTextEnterPasswordDia.setSelection(eitTextEnterPasswordDia.length())

            }


            imgToggleNzotVisible.setOnClickListener {
                imgToggle.visibility = View.VISIBLE
                imgToggleNzotVisible.visibility = View.INVISIBLE
                eitTextEnterPasswordDia.transformationMethod = PasswordTransformationMethod()
                eitTextEnterPasswordDia.setSelection(eitTextEnterPasswordDia.length())


            }





            imgToggle22.setOnClickListener {
                imgToggle22.visibility = View.INVISIBLE
                imgToggleNzotVisible22.visibility = View.VISIBLE
                eitTextEnterConfirmPassword.transformationMethod = null
                eitTextEnterConfirmPassword.setSelection(eitTextEnterConfirmPassword.length())

            }


            imgToggleNzotVisible22.setOnClickListener {
                imgToggle22.visibility = View.VISIBLE
                imgToggleNzotVisible22.visibility = View.INVISIBLE
                eitTextEnterConfirmPassword.transformationMethod = PasswordTransformationMethod()
                eitTextEnterConfirmPassword.setSelection(eitTextEnterConfirmPassword.length())

            }

        }
    }

    private fun setupListeners() {
        binding.textSaveButton.setOnClickListener {
            if (isUserNameValid  && isComapnyValid && isPasswordValid && isMyEmailValid && isPhoneValid ) {
                hideKeyBoard(binding.editTextName)

                getCountryCode = binding.countryCode.getSelectedCountryCode()!!;

                // getting the country name
                getCountryName = binding.countryCode.getSelectedCountryName()!!;


                val confirmPassword = binding.eitTextEnterConfirmPassword.text.toString().trim()
                val name = binding.editTextName.text.toString().trim()
                val phone = binding.editTextPhoneNumber.text.toString().trim()
                val email = binding.editTextEmail.text.toString().trim()
                val companyName = binding.editTextCompanyName.text.toString().trim()

                val editor = simpleSavedPassword.edit()
                editor.putString(Constants.mySimpleSavedPassword, confirmPassword)
                editor.putString(Constants.isSavedEmail, email)
                editor.putString(Constants.COUNTRY_NAME, getCountryName)
                editor.putString(Constants.COUNTRY_CODE, getCountryCode)
                editor.putString(Constants.USER_PHONE, phone)
                editor.putString(Constants.USER_NAME, name)
                editor.putString(Constants.USER_COMPANY_NAME, companyName)
                editor.apply()



                showCustomProgressDialog("Saving...")

                handler.postDelayed(Runnable {
                    hideKeyBoard(binding.editTextName)

                    val editor222 = sharedBiometric.edit()
                    editor222.putString(Constants.FIRST_INFORMATION_PAGE_COMPLETED, Constants.FIRST_INFORMATION_PAGE_COMPLETED)
                    editor222.apply()

                    if (constants.EnableWelcomeSlider) {
                        startActivity(Intent(applicationContext, WelcomeSliderKT::class.java))
                        finish()
                        customProgressDialog.dismiss()

                    }else{
                        gotoRequiredPages()
                    }

                }, 4000)

            }else{
                validateForm()
            }
        }


    }


    private  fun gotoRequiredPages(){


            val sharedBiometric = applicationContext.getSharedPreferences(Constants.SHARED_BIOMETRIC, MODE_PRIVATE)
            val getTvMode = sharedBiometric.getString(Constants.CALL_RE_SYNC_MANGER, "").toString()


            val get_INSTALL_TV_JSON_USER_CLICKED = sharedTVAPPModePreferences.getString(Constants.INSTALL_TV_JSON_USER_CLICKED, "").toString()
            val get_installTVMode = sharedTVAPPModePreferences.getBoolean(Constants.installTVMode, false)
            val getFirstMode = sharedTVAPPModePreferences.getString(Constants.installTVModeForFirstTime, "").toString()

            if (get_INSTALL_TV_JSON_USER_CLICKED == Constants.INSTALL_TV_JSON_USER_CLICKED ){

                if (get_installTVMode && !getFirstMode.equals(Constants.installTVModeForFirstTime)){
                    // saving launch state
                       val editText88 = sharedBiometric.edit()
                      editText88.putString(Constants.get_Launching_State_Of_WebView, Constants.launch_WebView_Offline)
                       editText88.apply()

                    Log.d("InitWebvIewloadStates", "InformationActivity: Yes cliked ")

                    val myactivity = Intent(this@InformationActivity, ReSyncActivity::class.java)
                    myactivity.putExtra("url", constants.jsonUrl)
                    startActivity(myactivity)
                    finish()

                }else{

                    // saving launch state
                       val editText88 = sharedBiometric.edit()
                      editText88.putString(Constants.get_Launching_State_Of_WebView, Constants.launch_Default_WebView_url)
                       editText88.apply()

                    Log.d("InitWebvIewloadStates", "InformationActivity: No clicked")

                    val myactivity =  Intent(this@InformationActivity, WebViewPage::class.java)
                    myactivity. putExtra("url", constants.jsonUrl)
                    startActivity(myactivity)
                    finish()

                }


            }else{
                Log.d("InitWebvIewloadStates", "InformationActivity: Yes cliked ")
                if (getTvMode == Constants.CALL_RE_SYNC_MANGER) {
                    val myactivity = Intent(this@InformationActivity, ReSyncActivity::class.java)
                    myactivity.putExtra("url", constants.jsonUrl)
                    startActivity(myactivity)
                    finish()


                } else {
                    val myactivity =  Intent(this@InformationActivity, WebViewPage::class.java)
                    myactivity. putExtra("url", constants.jsonUrl)
                    startActivity(myactivity)
                    finish()

                }


            }


    }




    private fun validateForm(){
        val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)

        val password = binding.eitTextEnterPasswordDia.text.toString().trim()
        val confirmPassword = binding.eitTextEnterConfirmPassword.text.toString().trim()
        val name = binding.editTextName.text.toString().trim()
        val companyName = binding.editTextCompanyName.text.toString().trim()
        val phone = binding.editTextPhoneNumber.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()


        // Check if passwords are not empty
        if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
            show_Pop_Form_Completed("Attention!", "Passwords do not match")
            binding.eitTextEnterConfirmPassword.error = "Passwords do not match"
            binding.eitTextEnterConfirmPassword.setBackgroundResource(R.drawable.btn_boarder_red)
            binding.eitTextEnterConfirmPassword.startAnimation(shakeAnimation)
            hideKeyBoard(binding.eitTextEnterConfirmPassword)

        } else {

            if (password.isEmpty()) {
                binding.eitTextEnterPasswordDia.error = "Password fields cannot be empty"
                binding.eitTextEnterPasswordDia.setBackgroundResource(R.drawable.btn_boarder_red)
                binding.eitTextEnterPasswordDia.startAnimation(shakeAnimation)
                hideKeyBoard(binding.editTextPhoneNumber)
            }

            if (!isPasswordValid) {
                show_Pop_Form_Completed(
                    "Attention!",
                    "Password length too small\nat least 6 characters"
                )
                binding.eitTextEnterConfirmPassword.error = "At least 6 characters"
                binding.eitTextEnterConfirmPassword.setBackgroundResource(R.drawable.btn_boarder_red)
                binding.eitTextEnterConfirmPassword.startAnimation(shakeAnimation)
                hideKeyBoard(binding.editTextPhoneNumber)
            }


            if (confirmPassword.isEmpty()) {
                show_Pop_Form_Completed("Attention!", "Passwords do not match")
                binding.eitTextEnterConfirmPassword.error = "Password fields cannot be empty"
                binding.eitTextEnterConfirmPassword.setBackgroundResource(R.drawable.btn_boarder_red)
                binding.eitTextEnterConfirmPassword.startAnimation(shakeAnimation)
                hideKeyBoard(binding.editTextPhoneNumber)
            }


        }


        // Check if email is valid
        if (!isValidEmail(email)) {
            // show_Pop_Form_Completed("Attention!", "Invalid Email format")
            binding.editTextEmail.error = "Invalid email format"
            binding.editTextEmail.startAnimation(shakeAnimation)
            binding.editTextEmail.setBackgroundResource(R.drawable.btn_boarder_red)
            hideKeyBoard(binding.editTextEmail)
        }


        // Check if phone is valid
        if (!isValidPhoneNumber(phone)) {
            // show_Pop_Form_Completed("Attention!", "Invalid Phone format")
            binding.editTextPhoneNumber.error = "Invalid Phone format"
            binding.constraintLayout6.startAnimation(shakeAnimation)
            binding.constraintLayout6.setBackgroundResource(R.drawable.btn_boarder_red)
            hideKeyBoard(binding.editTextPhoneNumber)

        }


        // Check if either name or company name is provided
        if (name.isEmpty() && companyName.isEmpty()) {
            show_Pop_Form_Completed("Attention!", "Please enter either Name or Company Name")
            binding.editTextName.error = "Please enter name or company name"
            binding.editTextName.startAnimation(shakeAnimation)
            binding.editTextName.setBackgroundResource(R.drawable.btn_boarder_red)

            binding.editTextCompanyName.error = "Please enter name or company name"
            binding.editTextCompanyName.startAnimation(shakeAnimation)
            binding.editTextCompanyName.setBackgroundResource(R.drawable.btn_boarder_red)
            hideKeyBoard(binding.editTextName)
            hideKeyBoard(binding.editTextCompanyName)
        }


    }


    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(\\.[a-zA-Z]{2,})?"
        val pattern = Pattern.compile(emailPattern)
        return pattern.matcher(email).matches()
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        // Simple phone number validation (basic example)
        val phonePattern = "\\+?[0-9]{10,13}" // Modify pattern as needed
        val pattern = Pattern.compile(phonePattern)
        return pattern.matcher(phone).matches()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    private fun performButtonClick() {

        binding.apply {


            editTextName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val name = binding.editTextName.text.toString().trim()
                    if (name.isNotEmpty() && name.length>1) {
                        editTextName.setBackgroundResource(R.drawable.round_corner_boader)
                        isUserNameValid = true
                    } else {
                        editTextName.setBackgroundResource(R.drawable.btn_boarder_red)
                        isUserNameValid = false
                    }
                }
            })


            editTextCompanyName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val name = binding.editTextCompanyName.text.toString().trim()
                    if (name.isNotEmpty() && name.length>1) {
                        editTextCompanyName.setBackgroundResource(R.drawable.round_corner_boader)
                        isComapnyValid = true
                    } else {
                        editTextCompanyName.setBackgroundResource(R.drawable.btn_boarder_red)
                        isComapnyValid = false
                    }

                }
            })




            editTextEmail.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (isValidEmail(s.toString())) {
                        editTextEmail.setBackgroundResource(R.drawable.round_corner_boader)
                        isMyEmailValid = true
                    } else {
                        editTextEmail.setBackgroundResource(R.drawable.btn_boarder_red)
                        isMyEmailValid = false
                    }
                }
            })




            eitTextEnterPasswordDia.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val password = binding.eitTextEnterPasswordDia.text.toString().trim()

                    if (password.isNotEmpty() && password.length > 5) {
                        eitTextEnterPasswordDia.setBackgroundResource(R.drawable.round_corner_boader)
                    } else {
                        eitTextEnterPasswordDia.setBackgroundResource(R.drawable.btn_boarder_red)

                    }

                }
            })





            eitTextEnterConfirmPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val password = binding.eitTextEnterPasswordDia.text.toString().trim()
                    val confirmPassword = binding.eitTextEnterConfirmPassword.text.toString().trim()

                    if (password.isNotEmpty() && password.length > 5 && password == confirmPassword) {
                        eitTextEnterConfirmPassword.setBackgroundResource(R.drawable.round_corner_boader)
                        isPasswordValid = true
                    } else {
                        eitTextEnterConfirmPassword.setBackgroundResource(R.drawable.btn_boarder_red)
                        isPasswordValid = false
                    }

                }
            })


            editTextPhoneNumber.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (isValidPhoneNumber(s.toString())) {
                        constraintLayout6.setBackgroundResource(R.drawable.round_corner_boader)
                        isPhoneValid = true
                    } else {
                        constraintLayout6.setBackgroundResource(R.drawable.btn_boarder_red)
                        isPhoneValid = false
                    }

                }
            })


        }

    }

    @SuppressLint("MissingInflatedId")
    private fun show_Pop_Form_Completed(title: String, body: String) {
        val binding: CustomPopInformationPageBinding =
            CustomPopInformationPageBinding.inflate(layoutInflater)
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(binding.getRoot())
        alertDialogBuilder.setCancelable(false)
        val alertDialog = alertDialogBuilder.create()

        // Set the background of the AlertDialog to be transparent
        if (alertDialog.window != null) {
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
        }


        binding.textEmailSendOkayBtn.setOnClickListener { view ->
            alertDialog.dismiss()
        }

        binding.textSucessful.text = title
        binding.textBodyMessage.text = body

        // Show the AlertDialog
        alertDialog.show()
    }


    @SuppressLint("MissingInflatedId")
    private fun show_Pop_Confirm_Exit(title: String, body: String) {
        val binding: CustomExitOrNotBinding = CustomExitOrNotBinding.inflate(layoutInflater)
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(binding.getRoot())
        alertDialogBuilder.setCancelable(false)
        val alertDialog = alertDialogBuilder.create()

        // Set the background of the AlertDialog to be transparent
        if (alertDialog.window != null) {
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
        }



        binding.textSucessful.text = title
        binding.textBodyMessage.text = body

        binding.textLaunchMyOnline.setOnClickListener { view ->
            alertDialog.dismiss()
        }


        binding.textLaunchMyOffline.setOnClickListener { view ->
            finishAndRemoveTask()
            Process.killProcess(Process.myTid())
            alertDialog.dismiss()
        }


        // Show the AlertDialog
        alertDialog.show()
    }


    private fun showCustomProgressDialog(message: String) {
        try {
            customProgressDialog = Dialog(this)
            val binding = ProgressValidateUserDialogLayoutBinding.inflate(LayoutInflater.from(this))
            customProgressDialog.setContentView(binding.root)
            customProgressDialog.setCancelable(true)
            customProgressDialog.setCanceledOnTouchOutside(false)
            customProgressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            binding.textLoading.text = message

            customProgressDialog.show()
        } catch (_: Exception) {
        }
    }


    private fun hideKeyBoard(editText: EditText) {
        try {
            editText.clearFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editText.windowToken, 0)
        } catch (ignored: java.lang.Exception) {
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




    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        show_Pop_Confirm_Exit("Attention!", "Are you sure you want to exit?")
    }

}
