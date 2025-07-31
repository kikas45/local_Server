package sync2app.com.syncapplive.additionalSettings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import sync2app.com.syncapplive.AppNetworkModule.RemoteConfigViewModel
import sync2app.com.syncapplive.databinding.ActivityDeMo202100Binding

class DE_MO_202100 : AppCompatActivity() {

    private lateinit var binding: ActivityDeMo202100Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeMo202100Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            makeNetworkRequest()
        }

    }


    private fun makeNetworkRequest() {
        val viewModel = ViewModelProvider(this)[RemoteConfigViewModel::class.java]

        val baseUrl = "https://cp.cloudappserver.co.uk/app_base/public/"
        val endpoint = "CLO/DE_MO_2021000/App/Config/appConfig.json"

        viewModel.configLiveData.observe(this) { result ->
            result.onSuccess { configResponse ->
                val config = configResponse.remoteConfig
                binding.textDisplay.text =
                    "${config.DrawerHeaderText}\n${config.bottom1_img_url}\n${config.bottom6_img_url}"
            }.onFailure { error ->
                Toast.makeText(this, "Error: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.getRemoteConfig(baseUrl, endpoint)
    }



}
