package sync2app.com.syncapplive.additionalSettings
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import sync2app.com.syncapplive.R
import sync2app.com.syncapplive.additionalSettings.savedDownloadHistory.scanutil.CustomShortcuts
import sync2app.com.syncapplive.additionalSettings.savedDownloadHistory.scanutil.DefaultCustomShortCut
import sync2app.com.syncapplive.additionalSettings.utils.Constants
import sync2app.com.syncapplive.databinding.FragmentShortCutBottomBinding
import java.io.File

class ShortCutBottomFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentShortCutBottomBinding? = null
    private val binding get() = _binding!!


    private val sharedBiometric: SharedPreferences by lazy {
        requireContext().getSharedPreferences(
            Constants.SHARED_BIOMETRIC, Context.MODE_PRIVATE
        )
    }


    private val myDownloadClass: SharedPreferences by lazy {
        requireContext().getSharedPreferences(
            Constants.MY_DOWNLOADER_CLASS, Context.MODE_PRIVATE
        )
    }



    // setting for shortCut icons
    private val SELECT_PICTURE = 200
    private var isImagePicked = false
    var imagePicked: Uri? = null
    private lateinit var custImageView: ImageView
    private lateinit var customimageRadipoButton: RadioButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentShortCutBottomBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        custImageView = view.findViewById<View>(R.id.custImageView) as ImageView
        customimageRadipoButton = view.findViewById<View>(R.id.customimageRadipoButton) as RadioButton

        show_My_Short_Cut_icons(view)



        val get_imgToggleImageBackground = sharedBiometric.getString(Constants.imgToggleImageBackground, "")
        val get_imageUseBranding = sharedBiometric.getString(Constants.imageUseBranding, "")
        if (get_imgToggleImageBackground.equals(Constants.imgToggleImageBackground) && get_imageUseBranding.equals(Constants.imageUseBranding) ){
            loadBackGroundImage()
        }


    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingInflatedId", "CutPasteId")
    private fun show_My_Short_Cut_icons(view: View) {

        var defaultradio = false
        var customradio = false


        val textLogin = view.findViewById<View>(R.id.textLogin) as TextView
        val editTextText = view.findViewById<View>(R.id.editTextText) as EditText
        val imageCloseBottomSheet = view.findViewById<View>(R.id.textCancelPopUp) as TextView
        custImageView = view.findViewById<View>(R.id.custImageView) as ImageView
        val defaultImageFaltImage =
            view.findViewById<View>(R.id.defaultImageFaltImage) as ImageView
        val defaultImageRadioButton = view.findViewById<View>(R.id.defaultImageRadioButton) as RadioButton
        customimageRadipoButton = view.findViewById<View>(R.id.customimageRadipoButton) as RadioButton


        imageCloseBottomSheet.setOnClickListener {
            dismiss()
        }

        custImageView.setOnClickListener {
                 imageChooser()
                customimageRadipoButton.isChecked = true
                defaultImageRadioButton.isChecked = false
                defaultradio = false
                customradio = true
                binding.editTextText.setText("")

        }



        defaultImageFaltImage.setOnClickListener {
            defaultImageRadioButton.isChecked = true
            customimageRadipoButton.isChecked = false
            binding.editTextText.setText("Sync2App")

        }




        defaultImageRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                defaultImageRadioButton.isChecked = true
                customimageRadipoButton.isChecked = false
                defaultradio = true
                customradio = false
                binding.editTextText.setText("Sync2App")
            }
        }





        textLogin.setOnClickListener {
            hideKeyBoard(editTextText)

            val getEditString = editTextText.text.toString().trim()

            if (defaultradio) {
                if (getEditString.isNotEmpty()) {
                    //  val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imagePicked)
                    if (Build.VERSION.SDK_INT >= 25) {
                        DefaultCustomShortCut.setUp(requireContext(), getEditString)
                    }
                    if (Build.VERSION.SDK_INT >= 28) {
                        shortcutPin(requireContext(), Constants.shortcut_website_id, 0)
                    }
                    dismiss()
                } else {
                    showToastMessage("Add name")
                }
            } else {
                //  showToastMessage("Image and name required")
            }



            if (customradio) {
                if (getEditString.isNotEmpty() && isImagePicked) {
                    if (Build.VERSION.SDK_INT >= 25) {
                        val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imagePicked)
                        CustomShortcuts.setUp(requireContext(), getEditString, bitmap)
                    }
                    if (Build.VERSION.SDK_INT >= 28) {
                        shortcutPin(requireContext(), Constants.shortcut_messages_id, 1)
                    }
                  dismiss()
                } else {
                    showToastMessage("Image and name required")
                }
            } else {
                // showToastMessage("Image and name required")
            }
        }


    }


    private fun imageChooser() {
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                val selectedImageUri: Uri? = data?.data
                if (selectedImageUri != null) {
                    custImageView.setImageURI(selectedImageUri)
                    customimageRadipoButton.isChecked = true
                    isImagePicked = true

                    imagePicked = selectedImageUri

                } else {
                    customimageRadipoButton.isChecked = false
                    isImagePicked = false
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun shortcutPin(context: Context, shortcut_id: String, requestCode: Int) {

        val shortcutManager = requireContext().getSystemService(ShortcutManager::class.java)

        if (shortcutManager!!.isRequestPinShortcutSupported) {
            val pinShortcutInfo =
                ShortcutInfo.Builder(context, shortcut_id).build()

            val pinnedShortcutCallbackIntent =
                shortcutManager.createShortcutResultIntent(pinShortcutInfo)

            val successCallback = PendingIntent.getBroadcast(
                context, /* request code */ requestCode,
                pinnedShortcutCallbackIntent, /* flags */ PendingIntent.FLAG_MUTABLE
            )

            shortcutManager.requestPinShortcut(
                pinShortcutInfo,
                successCallback.intentSender
            )
        }
    }


    private fun showToastMessage(messages: String) {

        try {
            Toast.makeText(requireContext(), messages, Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
        }
    }

    private fun hideKeyBoard(editText: EditText) {
        try {
            editText.clearFocus()
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editText.windowToken, 0)
        } catch (ignored: java.lang.Exception) {
        }
    }

    private fun loadBackGroundImage() {
        val fileTypes = "app_background.png"
        val getFolderClo = myDownloadClass.getString(Constants.getFolderClo, "").toString()
        val getFolderSubpath = myDownloadClass.getString(Constants.getFolderSubpath, "").toString()

        val baseDir = requireContext().getExternalFilesDir(null)
        val relativePath = "${Constants.Syn2AppLive}/$getFolderClo/$getFolderSubpath/${Constants.App}/Config"
        val file = File(File(baseDir, relativePath), fileTypes)

        if (file.exists()) {
            Glide.with(this).load(file).centerCrop().into(binding.backgroundImage)
        }
    }




    override fun onDestroy() {
        super.onDestroy()
        try {

            _binding = null

        } catch (_: Exception) {
        }
    }


}