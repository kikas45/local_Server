package sync2app.com.syncapplive.additionalSettings

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import sync2app.com.syncapplive.additionalSettings.utils.NetworkMonitor
import sync2app.com.syncapplive.databinding.ActivityDeMo202100Binding

class DE_MO_202100 : AppCompatActivity() {

    private lateinit var binding: ActivityDeMo202100Binding
    private lateinit var networkMonitor: NetworkMonitor
    private  var isDownloadStartOnNetworkCall = true


    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeMo202100Binding.inflate(layoutInflater)
        setContentView(binding.root)

        findInternet()

        binding.button.setOnClickListener {
            val intent = Intent(applicationContext, MaintenanceActivity::class.java)
            startActivity(intent)
            finish()
        }


    }

    private fun findInternet(){
        networkMonitor = NetworkMonitor(applicationContext) { isConnected ->
            if (isConnected){


                handler.postDelayed(Runnable {
                    if (isDownloadStartOnNetworkCall) {
                        isDownloadStartOnNetworkCall = false
                        binding.textDisplay.visibility = View.GONE
                        Toast.makeText(applicationContext, "Internet Connection", Toast.LENGTH_SHORT).show()
                    }
                }, 1000)


            }else{
                binding.textDisplay.visibility = View.VISIBLE

                Toast.makeText(applicationContext, "No Internet Connection", Toast.LENGTH_SHORT).show()
            }
        }
        networkMonitor.register()
    }



    override fun onResume() {
        super.onResume()
        networkMonitor.checkNow()
    }

    override fun onDestroy() {
        super.onDestroy()
        networkMonitor.unregister()
    }
}
