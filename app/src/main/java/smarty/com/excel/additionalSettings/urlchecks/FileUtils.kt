package smarty.com.excel.additionalSettings.urlchecks


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import smarty.com.excel.additionalSettings.utils.BuildConfig
import smarty.com.excel.R
import java.io.File



private const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.provider"

fun getMimeType(url: String): String {
    val ext = MimeTypeMap.getFileExtensionFromUrl(url)
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "text/plain"
}

//fun getFilesList(selectedItem: File): List<File> {
//    val rawFilesList = selectedItem.listFiles()?.filter { !it.isHidden }
//
//    return if (selectedItem == Environment.getExternalStorageDirectory()) {
//        rawFilesList?.toList() ?: listOf()
//    } else {
//        listOf(selectedItem.parentFile) + (rawFilesList?.toList() ?: listOf())
//    }
//}


fun getFilesList(selectedItem: File): List<File> {
    val rawFilesList = selectedItem.listFiles()?.filter { !it.isHidden }

    val directoryPath =
        Environment.getExternalStorageDirectory().absolutePath + "/Download/Syn2AppLive/"
    val file = File(directoryPath)

    return if (selectedItem == file) {
        rawFilesList?.toList() ?: listOf()
    } else {
        listOf(selectedItem.parentFile) + (rawFilesList?.toList() ?: listOf())
    }
}


fun renderParentLink(activity: AppCompatActivity): String {
    return activity.getString(R.string.go_parent_label)
}

fun renderItem(activity: AppCompatActivity, file: File): String {
    return if (file.isDirectory) {
        activity.getString(R.string.folder_item, file.name)
    } else {
        activity.getString(R.string.file_item, file.name)
    }
}


@SuppressLint("UnsafeImplicitIntentLaunch")
fun openFile(activity: AppCompatActivity, selectedItem: File) {
    // Get URI and MIME type of file
    val uri = FileProvider.getUriForFile(activity.applicationContext, AUTHORITY, selectedItem)
    val mime: String = getMimeType(uri.toString())

    // Open file with user selected app
    val intent = Intent(Intent.ACTION_VIEW)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intent.setDataAndType(uri, mime)
    return activity.startActivity(intent)
}
