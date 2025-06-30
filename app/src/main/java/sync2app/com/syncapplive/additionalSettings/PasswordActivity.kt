package sync2app.com.syncapplive.additionalSettings

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import sync2app.com.syncapplive.R
import sync2app.com.syncapplive.SettingsActivityKT
import sync2app.com.syncapplive.WebViewPage
import sync2app.com.syncapplive.additionalSettings.autostartAppOncrash.Methods
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.additionalSettings.utils.Utility
import sync2app.com.syncapplive.databinding.ActivityPasswordBinding
import sync2app.com.syncapplive.databinding.CustomDefaultEmailSavedLayoutBinding
import sync2app.com.syncapplive.databinding.CustomDefineRefreshTimeBinding
import sync2app.com.syncapplive.databinding.CustomFailedLayoutBinding
import sync2app.com.syncapplive.databinding.CustomPasswordTimeBinding
import sync2app.com.syncapplive.databinding.CustomSchedulePassowrdBinding
import sync2app.com.syncapplive.databinding.CustomSucessLayoutBinding
import sync2app.com.syncapplive.databinding.CustomValidDisplayTimePasswordBinding
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.regex.Pattern

class PasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPasswordBinding


    private val sharedBiometric: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_BIOMETRIC,
            Context.MODE_PRIVATE
        )
    }

    private val simpleSavedPassword: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SIMPLE_SAVED_PASSWORD,
            Context.MODE_PRIVATE
        )
    }

    private val myDownloadClass: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.MY_DOWNLOADER_CLASS,
            Context.MODE_PRIVATE
        )
    }


    private var olDPassworEnabled = false;
    private var newPassworEnabled = false;
    private var isEmailReady = false;

    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    private val sharedTVAPPModePreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(
            Constants.SHARED_TV_APP_MODE, Context.MODE_PRIVATE
        )
    }



    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)


        applyOritenation()
        // Set Dark Theme


        setUpFullScreenWindows()

        //add exception
        Methods.addExceptionHandler(this)


        val get_imgToggleImageBackground = sharedBiometric.getString(Constants.imgToggleImageBackground, "").toString()
        val get_imageUseBranding = sharedBiometric.getString(Constants.imageUseBranding, "").toString()
        if (get_imgToggleImageBackground.equals(Constants.imgToggleImageBackground) && get_imageUseBranding.equals(
                Constants.imageUseBranding
            )
        ) {
            loadBackGroundImage()
        }



        binding.apply {
            closeBs.setOnClickListener {
                navigateBackScreen()
            }


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
                eitTextEnterNewPassword.transformationMethod = null
                eitTextEnterNewPassword.setSelection(eitTextEnterNewPassword.length())

            }


            imgToggleNzotVisible22.setOnClickListener {
                imgToggle22.visibility = View.VISIBLE
                imgToggleNzotVisible22.visibility = View.INVISIBLE
                eitTextEnterNewPassword.transformationMethod = PasswordTransformationMethod()
                eitTextEnterNewPassword.setSelection(eitTextEnterNewPassword.length())

            }


            val imgPassword = simpleSavedPassword.getString(Constants.passowrdPrefeilled, "").toString()
            imgToggleOffPassword.isChecked = imgPassword == Constants.passowrdPrefeilled


            if (imgPassword == Constants.passowrdPrefeilled) {
                textDisableOrEnablePassowrd.text = "Enable Password Prefilled"
            } else {
                textDisableOrEnablePassowrd.text = "Enter Password Always"
            }


            // init password time
            textRefreshTimer.setOnClickListener {
                definedTimeIntervalsForPassword()
            }

            /// check if time has exceed or not

            get_Current_Time_State_for_Password()


            imgToggleOffPassword.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->

                if (isChecked){

                    val editor = simpleSavedPassword.edit()
                    editor.putString(Constants.passowrdPrefeilled, "passowrdPrefeilled")
                    editor.apply()
                    textDisableOrEnablePassowrd.text = "Enable Password Prefilled"

                    val futureTimeString = simpleSavedPassword.getString(Constants.KEY_FUTURE_TIME_FIRST, null)
                    if (!futureTimeString.isNullOrEmpty()){

                        showPop_Password_For_Logout()

                    }else{

                        val futureTime = getFutureTimeInMinutes(Constants.INTERVAL_5)
                        saveFutureTime(futureTime)

                        binding.textRefreshTimer.text = "Valid For  ${Constants.INTERVAL_5} Minutes"

                        binding.textRefreshTimer.visibility = View.VISIBLE
                        binding.imageView57.visibility = View.VISIBLE
                        binding.imageView4.visibility = View.VISIBLE

                        val editor222 = simpleSavedPassword.edit()
                        editor222.putInt(Constants.REFRESH_PASSWORD, Constants.INTERVAL_5)
                        editor222.apply()

                        showPopDisplayTime("Prefilled Password is Valid\nFor ${Constants.INTERVAL_5} Minutes")

                    }

                }else{

                    val editor = simpleSavedPassword.edit()
                    editor.remove(Constants.passowrdPrefeilled)
                    editor.remove(Constants.KEY_FUTURE_TIME)
                    editor.apply()
                    binding.textDisableOrEnablePassowrd.text = "Enter Password Always"

                    binding.textRefreshTimer.visibility = View.GONE
                    binding.imageView57.visibility = View.GONE
                    binding.imageView4.visibility = View.GONE


                }


            })



            /// is email visibility allowed

            val imgEmailVisibility = simpleSavedPassword.getString(Constants.imagEnableEmailVisisbility, "").toString()
            imagEnableEmailVisisbility.isChecked = imgEmailVisibility.equals(Constants.imagEnableEmailVisisbility)


            if (imgEmailVisibility == Constants.imagEnableEmailVisisbility) {
                textEmailVisbility.text = "Enable email visibility"
            } else {

                textEmailVisbility.text = "Disable email visibility"
            }


            imagEnableEmailVisisbility.setOnCheckedChangeListener { compoundButton, isValued ->
                val editor = simpleSavedPassword.edit()
                if (compoundButton.isChecked) {
                    editor.putString(Constants.imagEnableEmailVisisbility, "imagEnableEmailVisisbility")
                    editor.apply()
                    textEmailVisbility.text = "Enable email visibility"

                } else {
                    editor.remove("imagEnableEmailVisisbility")
                    editor.apply()
                    textEmailVisbility.text = "Disable email visibility"

                }
            }





            old_and_new_PasswordTextChanger()


            val getSavedEmail = simpleSavedPassword.getString(Constants.isSavedEmail, "").toString()

            if (getSavedEmail.isNotEmpty()) {
                binding.editTextEmail.setText(getSavedEmail)

            }


            binding.textContinuPasswordDai2.setOnClickListener {

                val savedSimplePassword = simpleSavedPassword.getString(Constants.mySimpleSavedPassword, "").toString()

                val getOldEditText = binding.eitTextEnterPasswordDia.text.toString()
                val newPasswordText = binding.eitTextEnterNewPassword.text.toString()
                val editTextEmail = binding.editTextEmail.text.toString()

                val editor = simpleSavedPassword.edit()

                if (savedSimplePassword == getOldEditText  && newPasswordText.isNotEmpty()) {
                    editor.putString(Constants.mySimpleSavedPassword, newPasswordText)
                    if (isEmailReady) {
                        editor.putString(Constants.isSavedEmail, editTextEmail)
                    } else {
                        showToastMessage("Invalid email format")
                    }
                    editor.apply()
                    showPopForOkayPassword();
                } else {

                    if (getOldEditText.isEmpty() && newPasswordText.isEmpty()) {

                        if (editTextEmail.isNotEmpty() || getSavedEmail.isNotEmpty()) {

                            if (isEmailReady) {
                                showPopYourDefaultEmailSaved()
                                editor.putString(Constants.isSavedEmail, editTextEmail)
                                editor.apply()
                            } else {
                                binding.editTextEmail.error = "Invalid email format"
                                showPop_For_wrong_Password("Invalid email format\nTry Again")
                            }
                        } else {

                            showToastMessage("Settings saved")
                            onBackPressed()
                        }

                    } else {

                        if (savedSimplePassword != getOldEditText){
                            eitTextEnterPasswordDia.error = "Wrong Password"
                            showPop_For_wrong_Password( "Old Password is Wrong\nTry Again")
                        }

                        if (savedSimplePassword == getOldEditText && newPasswordText.isEmpty()){
                            eitTextEnterNewPassword.error = "Invalid Format"
                            showPop_For_wrong_Password("New Password Filed can not be Empty")
                        }



                    }

                }



                saveTooggleButton()


            }
        }
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



    private fun get_Current_Time_State_for_Password() {

        val getPrefilledPassword = simpleSavedPassword.getString(Constants.passowrdPrefeilled, "").toString()

        if (getPrefilledPassword == Constants.passowrdPrefeilled) {
            val futureTime = getSavedFutureTime()
            if (futureTime != null) {
                val currentTime = Calendar.getInstance().time
                if (currentTime.after(futureTime)) {

                    val editor = simpleSavedPassword.edit()
                    editor.remove(Constants.passowrdPrefeilled)
                    editor.remove(Constants.Did_User_Input_PassWord)
                    editor.apply()

                    binding.textRefreshTimer.visibility = View.GONE
                    binding.imageView57.visibility = View.GONE
                    binding.imageView4.visibility = View.GONE
                }

            }

        }

    }


    @SuppressLint("MissingInflatedId")
    private  fun showPop_Password_For_Logout() {
        val bindingCm: CustomSchedulePassowrdBinding = CustomSchedulePassowrdBinding.inflate(layoutInflater)
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setView(bindingCm.getRoot())
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setCancelable(false)
        // Set the background of the AlertDialog to be transparent
        if (alertDialog.window != null) {
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
        }

        val editTextInputUrl: EditText = bindingCm.eitTextEnterPassword
        val textContinuPassword: TextView = bindingCm.textContinuPassword
        val imgCloseDialog2: ImageView = bindingCm.imgCloseDialogForegetPassword
        val imgToggle: ImageView = bindingCm.imgToggle
        val imgToggleNzotVisible: ImageView = bindingCm.imgToggleNzotVisible
        val textForGetPassword: TextView = bindingCm.textForGetPassword
        val divider2: View = bindingCm.divider2
        val consMainAlert_sub_layout: ConstraintLayout = bindingCm.consMainAlertSubLayout


        val preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if (preferences.getBoolean("darktheme", false)) {
            consMainAlert_sub_layout.setBackgroundResource(R.drawable.card_design_account_number_dark_pop_layout)
            textForGetPassword.setTextColor(resources.getColor(R.color.dark_light_gray_pop))
            textContinuPassword.setTextColor(resources.getColor(R.color.dark_light_gray_pop))
            editTextInputUrl.setTextColor(resources.getColor(R.color.white))


            //  textLogoutButton.setBackgroundResource(R.drawable.card_design_darktheme_outline_pop_layout);
            textContinuPassword.setBackgroundResource(R.drawable.card_design_buy_gift_card_extra_dark_black)
            setDrawableColor(imgCloseDialog2, R.drawable.ic_close_24, R.color.dark_light_gray_pop)
            divider2.setBackgroundColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.dark_light_gray_pop
                )
            )
        }
        val simpleSavedPassword = getSharedPreferences(Constants.SIMPLE_SAVED_PASSWORD, MODE_PRIVATE)
        val simpleAdminPassword = simpleSavedPassword.getString(Constants.mySimpleSavedPassword, "").toString()




        imgToggle.setOnClickListener {
            imgToggle.visibility = View.INVISIBLE
            imgToggleNzotVisible.visibility = View.VISIBLE
            editTextInputUrl.transformationMethod = null
            editTextInputUrl.setSelection(editTextInputUrl.length())

        }


        imgToggleNzotVisible.setOnClickListener {
            imgToggle.visibility = View.VISIBLE
            imgToggleNzotVisible.visibility = View.INVISIBLE
            editTextInputUrl.transformationMethod = PasswordTransformationMethod()
            editTextInputUrl.setSelection(editTextInputUrl.length())


        }




        // Load the shake animation
        val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)

        val smPassowrd = simpleAdminPassword

        textDetctor(smPassowrd, editTextInputUrl, divider2)




        textContinuPassword.setOnClickListener {
            val editTextText = editTextInputUrl.text.toString().trim { it <= ' ' }
            if (editTextText == simpleAdminPassword) {

                hideKeyBoard(editTextInputUrl)

                binding.textDisableOrEnablePassowrd.text = "Enable Password Prefilled"


                //// save the time
                val futureTime = getFutureTimeInMinutes(Constants.INTERVAL_5)
                saveFutureTime(futureTime)
                binding.textRefreshTimer.text = "Valid For  ${Constants.INTERVAL_5} Minutes"

                binding.textRefreshTimer.visibility = View.VISIBLE
                binding.imageView57.visibility = View.VISIBLE
                binding.imageView4.visibility = View.VISIBLE


                val editor = simpleSavedPassword.edit()
                editor.putInt(Constants.REFRESH_PASSWORD, Constants.INTERVAL_5)
                editor.apply()

                showPopDisplayTime("Prefilled Password is Valid\nFor ${Constants.INTERVAL_5} Minutes")


                alertDialog.dismiss()

            } else {

                binding.textDisableOrEnablePassowrd.text = "Enter Password Always"

                binding.textRefreshTimer.visibility = View.GONE
                binding.imageView57.visibility = View.GONE
                binding.imageView4.visibility = View.GONE

                hideKeyBoard(editTextInputUrl)
                showToastMessage("Wrong password")
                editTextInputUrl.error = "Wrong password"
                editTextInputUrl.setTextColor(resources.getColor(R.color.red))
                editTextInputUrl.setHintTextColor(resources.getColor(R.color.red))
                editTextInputUrl.startAnimation(shakeAnimation)
                divider2.startAnimation(shakeAnimation)
                divider2.setBackgroundColor(resources.getColor(R.color.red))



            }
        }


        imgCloseDialog2.setOnClickListener {
            val editor = simpleSavedPassword.edit()
            editor.remove(Constants.passowrdPrefeilled)
            editor.apply()

            binding.imgToggleOffPassword.isChecked = false
            binding.textDisableOrEnablePassowrd.text = "Enter Password Always"

            hideKeyBoard(editTextInputUrl)


            binding.textRefreshTimer.visibility = View.GONE
            binding.imageView57.visibility = View.GONE
            binding.imageView4.visibility = View.GONE

            val editor222 = simpleSavedPassword.edit()
            editor222.remove(Constants.KEY_FUTURE_TIME)
            editor222.apply()


            alertDialog.dismiss()


        }


        alertDialog.show()
    }



    private fun hideKeyBoard(editText: EditText) {
        try {
            editText.clearFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editText.windowToken, 0)
        } catch (ignored: java.lang.Exception) {
        }
    }


    private fun setDrawableColor(imageView: ImageView, drawableId: Int, colorId: Int) {
        val drawable = ContextCompat.getDrawable(applicationContext, drawableId)
        if (drawable != null) {
            drawable.setColorFilter(
                ContextCompat.getColor(applicationContext, colorId),
                PorterDuff.Mode.SRC_IN
            )
            imageView.setImageDrawable(drawable)
        }
    }


    private fun navigateBackScreen() {

        val getStateNaviagtion = sharedBiometric.getString(Constants.SAVE_NAVIGATION, "").toString()
        if (getStateNaviagtion.equals(Constants.SettingsPage)) {

            val intent = Intent(applicationContext, SettingsActivityKT::class.java)
            startActivity(intent)
            finish()

        } else if (getStateNaviagtion.equals(Constants.AdditionNalPage)){
            val intent = Intent(applicationContext, AdditionalSettingsActivity::class.java)
            startActivity(intent)
            finish()
        }

        else if (getStateNaviagtion.equals(Constants.WebViewPage)){
            val intent = Intent(applicationContext, WebViewPage::class.java)
            startActivity(intent)
            finish()
        }

    }


    override fun onBackPressed() {
        navigateBackScreen()
    }


    private fun saveTooggleButton() {
        binding.apply {

            // for password
            val editor = simpleSavedPassword.edit()
            if (imgToggleOffPassword.isChecked) {
                editor.putString(Constants.passowrdPrefeilled, "passowrdPrefeilled")
                editor.apply()

                textDisableOrEnablePassowrd.text = "Enable Password Prefilled"

            } else {

                textDisableOrEnablePassowrd.text = "Enter Password Always"
                editor.remove(Constants.passowrdPrefeilled)
                editor.remove(Constants.Did_User_Input_PassWord)
                editor.apply()

            }


            //for email visibility
            if (imagEnableEmailVisisbility.isChecked) {
                editor.putString(Constants.imagEnableEmailVisisbility, "imagEnableEmailVisisbility")
                editor.apply()
                textEmailVisbility.text = "Enable email visibility"

            } else {

                textEmailVisbility.text = "Disable email visibility"
                editor.remove(Constants.imagEnableEmailVisisbility)
                editor.apply()

            }


        }
    }


    private fun old_and_new_PasswordTextChanger() {
        binding.apply {

            eitTextEnterPasswordDia.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                    if (s.isNotEmpty()) {

                        //  divider2.setBackgroundResource(R.color.deep_blue)
                        //  eitTextEnterPasswordDia.error = "Invalid Password format"
                        olDPassworEnabled = true
                    } else {

                        //  divider2.setBackgroundResource(R.color.red)
                        eitTextEnterPasswordDia.error = "Invalid Password format"
                        olDPassworEnabled = false
                    }

                }

                override fun afterTextChanged(s: Editable) {
                    try {


                    } catch (_: Exception) {
                    }
                }
            })



            eitTextEnterNewPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    try {
                        if (s.isNotEmpty()) {
                            //    divider3.setBackgroundResource(R.color.deep_blue)
                            newPassworEnabled = true
                        } else {

                            //  divider3.setBackgroundResource(R.color.red)
                            eitTextEnterNewPassword.error = "Invalid Password format"
                            newPassworEnabled = false
                        }


                    } catch (_: Exception) {
                    }
                }

                override fun afterTextChanged(s: Editable) {

                }
            })


            editTextEmail.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    try {


                        isEmailReady = isValidEmail(s.toString())


                    } catch (_: Exception) {
                    }
                }

                override fun afterTextChanged(s: Editable) {

                }
            })


        }

    }


    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(\\.[a-zA-Z]{2,})?"
        val pattern = Pattern.compile(emailPattern)
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }

    private fun showToastMessage(messages: String) {

        try {
            Toast.makeText(applicationContext, messages, Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
        }
    }


    @SuppressLint("MissingInflatedId")
    private fun showPopForOkayPassword() {

        val binding: CustomSucessLayoutBinding = CustomSucessLayoutBinding.inflate(layoutInflater)
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(binding.root)

        val alertDialog = alertDialogBuilder.create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation



        binding.apply {

            textContinuPassword2.setOnClickListener {
                onBackPressed()
                alertDialog.dismiss()
            }

        }


        alertDialog.show()


    }


    @SuppressLint("MissingInflatedId")
    private fun showPopYourDefaultEmailSaved() {

        val binding: CustomDefaultEmailSavedLayoutBinding = CustomDefaultEmailSavedLayoutBinding.inflate(layoutInflater)
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(binding.root)

        val alertDialog = alertDialogBuilder.create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation



        binding.apply {

            textContinuPassword2.setOnClickListener {
                onBackPressed()
                alertDialog.dismiss()
            }

        }


        alertDialog.show()


    }


    @SuppressLint("MissingInflatedId")
    private fun showPop_For_wrong_Password(message:String) {

        val bindingCm: CustomFailedLayoutBinding = CustomFailedLayoutBinding.inflate(layoutInflater)
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(bindingCm.root)

        val alertDialog = alertDialogBuilder.create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation



            bindingCm.textView9.text = message

             bindingCm.textContinuPassword2.setOnClickListener {

                alertDialog.dismiss()
            }




        alertDialog.show()


    }


    @SuppressLint("MissingInflatedId")
    private fun showPopDisplayTime(message:String) {

        val bindingCm: CustomValidDisplayTimePasswordBinding = CustomValidDisplayTimePasswordBinding.inflate(layoutInflater)
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(bindingCm.root)

        val alertDialog = alertDialogBuilder.create()

        //alertDialog.setCanceledOnTouchOutside(false)
       // alertDialog.setCancelable(false)

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


            bindingCm.textView9.text = message

             bindingCm.textContinuPassword2.setOnClickListener {

                alertDialog.dismiss()
            }




        alertDialog.show()


    }


    private fun getFutureTimeInMinutes(minutesToAdd: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, minutesToAdd)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(calendar.time)
    }

    private fun saveFutureTime(futureTime: String) {
        val editor = simpleSavedPassword.edit()
        editor.putString(Constants.KEY_FUTURE_TIME, futureTime)
        editor.putString(Constants.KEY_FUTURE_TIME_FIRST, Constants.KEY_FUTURE_TIME_FIRST)
        editor.apply()
    }

    private fun getSavedFutureTime(): Date? {
        val futureTimeString = simpleSavedPassword.getString(Constants.KEY_FUTURE_TIME, null)
        return if (futureTimeString != null) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            try {
                sdf.parse(futureTimeString)
            } catch (e: ParseException) {
                null
            }
        } else {
            null
        }
    }

    @SuppressLint("InflateParams", "SuspiciousIndentation", "SetTextI18n")
    private fun definedTimeIntervalsForPassword() {
        val bindingCm: CustomPasswordTimeBinding = CustomPasswordTimeBinding.inflate(layoutInflater)
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setView(bindingCm.getRoot())
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.setCancelable(true)

        // Set the background of the AlertDialog to be transparent
        // Set the background of the AlertDialog to be transparent
        if (alertDialog.window != null) {
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
        }







        bindingCm.apply {

            imageCrossClose.setOnClickListener {
                alertDialog.dismiss()
            }



            closeBs.setOnClickListener {
                alertDialog.dismiss()
            }



            textFiveMinutes.setOnClickListener {

                val futureTime = getFutureTimeInMinutes(Constants.INTERVAL_5)
                saveFutureTime(futureTime)

                binding.textRefreshTimer.text = "Valid For  ${Constants.INTERVAL_5} Minutes"

                binding.textRefreshTimer.visibility = View.VISIBLE
                binding.imageView57.visibility = View.VISIBLE
                binding.imageView4.visibility = View.VISIBLE

                val editor = simpleSavedPassword.edit()
                editor.putInt(Constants.REFRESH_PASSWORD, Constants.INTERVAL_5)
                editor.apply()

                showPopDisplayTime("Prefilled Password is Valid\nFor ${Constants.INTERVAL_5} Minutes")


                alertDialog.dismiss()

            }


            text10Minutes.setOnClickListener {

                val futureTime = getFutureTimeInMinutes(Constants.INTERVAL_10)
                saveFutureTime(futureTime)

                binding.textRefreshTimer.text = "Valid For  ${Constants.INTERVAL_10} Minutes"

                binding.textRefreshTimer.visibility = View.VISIBLE
                binding.imageView57.visibility = View.VISIBLE
                binding.imageView4.visibility = View.VISIBLE

                val editor = simpleSavedPassword.edit()
                editor.putInt(Constants.REFRESH_PASSWORD, Constants.INTERVAL_10)
                editor.apply()

                showPopDisplayTime("Prefilled Password is Valid\nFor ${Constants.INTERVAL_10} Minutes")

                alertDialog.dismiss()
            }



            text15minutes.setOnClickListener {

                val futureTime = getFutureTimeInMinutes(Constants.INTERVAL_15)
                saveFutureTime(futureTime)

                binding.textRefreshTimer.text = "Valid For  ${Constants.INTERVAL_15} Minutes"

                binding.textRefreshTimer.visibility = View.VISIBLE
                binding.imageView57.visibility = View.VISIBLE
                binding.imageView4.visibility = View.VISIBLE

                val editor = simpleSavedPassword.edit()
                editor.putInt(Constants.REFRESH_PASSWORD, Constants.INTERVAL_15)
                editor.apply()

                showPopDisplayTime("Prefilled Password is Valid\nFor ${Constants.INTERVAL_15} Minutes")

                alertDialog.dismiss()
            }


            text30minutes.setOnClickListener {

                val futureTime = getFutureTimeInMinutes(Constants.INTERVAL_30)
                saveFutureTime(futureTime)

                binding.textRefreshTimer.text = "Valid For  ${Constants.INTERVAL_30} Minutes"

                binding.textRefreshTimer.visibility = View.VISIBLE
                binding.imageView57.visibility = View.VISIBLE
                binding.imageView4.visibility = View.VISIBLE

                val editor = simpleSavedPassword.edit()
                editor.putInt(Constants.REFRESH_PASSWORD, Constants.INTERVAL_30)
                editor.apply()

                showPopDisplayTime("Prefilled Password is Valid\nFor ${Constants.INTERVAL_30} Minutes")

                alertDialog.dismiss()
            }



            text45minutes.setOnClickListener {
                val futureTime = getFutureTimeInMinutes(Constants.INTERVAL_45)
                saveFutureTime(futureTime)

                binding.textRefreshTimer.text = "Valid For  ${Constants.INTERVAL_45} Minutes"

                binding.textRefreshTimer.visibility = View.VISIBLE
                binding.imageView57.visibility = View.VISIBLE
                binding.imageView4.visibility = View.VISIBLE

                val editor = simpleSavedPassword.edit()
                editor.putInt(Constants.REFRESH_PASSWORD, Constants.INTERVAL_45)
                editor.apply()

                showPopDisplayTime("Prefilled Password is Valid\nFor ${Constants.INTERVAL_45} Minutes")

                alertDialog.dismiss()
            }



            text600minutes.setOnClickListener {

                val futureTime = getFutureTimeInMinutes(Constants.INTERVAL_60)
                saveFutureTime(futureTime)

                binding.textRefreshTimer.text = "Valid For  ${Constants.INTERVAL_60} Minutes"

                binding.textRefreshTimer.visibility = View.VISIBLE
                binding.imageView57.visibility = View.VISIBLE
                binding.imageView4.visibility = View.VISIBLE

                val editor = simpleSavedPassword.edit()
                editor.putInt(Constants.REFRESH_PASSWORD, Constants.INTERVAL_60)
                editor.apply()

                showPopDisplayTime("Prefilled Password is Valid\nFor ${Constants.INTERVAL_60} Minutes")

                alertDialog.dismiss()

            }



        }


        alertDialog.show()
    }


    private fun textDetctor(smPassowrd: String, editTextText2: EditText, divider2: View) {
        try {


            editTextText2.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    try {

                        val passowrd = editTextText2.text.toString().trim()

                        if (smPassowrd ==passowrd) {
                            editTextText2.setBackgroundColor(resources.getColor(R.color.zxing_transparent))
                            editTextText2.setTextColor(resources.getColor(R.color.deep_green))
                            divider2.setBackgroundColor(resources.getColor(R.color.deep_green))
                        } else {
                            editTextText2.setBackgroundColor(resources.getColor(R.color.zxing_transparent))
                            editTextText2.setTextColor(resources.getColor(R.color.red))
                            divider2.setBackgroundColor(resources.getColor(R.color.red))

                        }


                    } catch (_: Exception) {
                    }
                }
            })



        }catch (e:Exception){}
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