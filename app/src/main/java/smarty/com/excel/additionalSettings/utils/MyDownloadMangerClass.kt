
package smarty.com.excel.additionalSettings.utils

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MyDownloadMangerClass {

    @SuppressLint("Range", "SetTextI18n")
    fun getDownloadStatus(progressBar: ProgressBar, textDownladByes: TextView, textViewFilesCount: TextView, context: Context) {
        try {
            val sharedP = context.getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, Context.MODE_PRIVATE)
            val download_ref = sharedP.getLong(Constants.downloadKey, -15)
            val query = DownloadManager.Query()
            query.setFilterById(download_ref)
            val downloadManager = context.getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
            val c = downloadManager.query(query)

            if (c != null && c.moveToFirst()) {
                val status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))

                // Check if download is running or paused
                if (status == DownloadManager.STATUS_RUNNING || status == DownloadManager.STATUS_PAUSED) {
                    progressBar.visibility = View.VISIBLE
                    textDownladByes.visibility = View.VISIBLE

                    textViewFilesCount.visibility = View.VISIBLE
                    textViewFilesCount.text = "DL: 1/1"

                    val bytes_downloaded = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)).toLong()
                    val bytes_total = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)).toLong()

                    // Calculate the percentage of completion
                    val progressPercentage = (bytes_downloaded.toFloat() / bytes_total.toFloat() * 100).toInt()
                    //set text bytes
                    textDownladByes.text = "$progressPercentage%"
                    //set progress for progressbar
                    progressBar.progress = progressPercentage

                } else {
                    progressBar.visibility = View.INVISIBLE

                    textViewFilesCount.text ="DL:1/1"
                    textDownladByes.text ="100%"
                }
            }
        } catch (ignored: Exception) {
            // Handle the exception or remove the catch block if not needed
        }
    }
}


