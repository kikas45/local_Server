

package sync2app.com.syncapplive.additionalSettings

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import sync2app.com.syncapplive.AppNetworkModule.RemoteConfigViewModel
import sync2app.com.syncapplive.databinding.ActivityDeMo202100Binding

class DE_MO_202100_2222 : AppCompatActivity() {

    private lateinit var binding: ActivityDeMo202100Binding
    private val viewModel by viewModels<RemoteConfigViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeMo202100Binding.inflate(layoutInflater)
        setContentView(binding.root)
      //  observeViewModel()

        binding.button.setOnClickListener {
         //   makeNetworkRequest()
        }

    }

    private fun makeNetworkRequest() {
        val baseUrl = "https://cp.cloudappserver.co.uk/app_base/public/"
        val endpoint = "CLO/DE_MO_2021000/App/Config/appConfig.json"
       // viewModel.getRemoteConfig(baseUrl, endpoint)
    }

    private fun observeViewModel() {
        viewModel.configLiveData.observe(this) { result ->
            result.onSuccess { response ->
                val config = response.remoteConfig

                binding.textDisplay.text = listOf(
                    config?.DrawerHeaderText,
                    config?.bottom1_img_url,
                    config?.bottom6_img_url
                ).joinToString("\n") { it ?: "N/A" }


            }.onFailure { error ->
                Toast.makeText(this, "Failed: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}




/*



    private fun loadLocalConfigFromAssets(): RemoteConfig? {
        return try {
            val json = assets.open("appConfig.json").bufferedReader().use { it.readText() }
            val remoteConfigJson = JSONObject(json).getJSONObject("remoteConfig")
            val gson = Gson()
            gson.fromJson(remoteConfigJson.toString(), RemoteConfig::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun observeViewModel() {
        viewModel.configLiveData.observe(this) { result ->
            result
                .onSuccess { response ->
                    getRemoteValues(response.remoteConfig)
                    handleSuccessState()
                }
                .onFailure { error ->
                    // Try loading from local file
                    val localConfig = loadLocalConfigFromAssets()
                    if (localConfig != null) {
                        getRemoteValues(localConfig)
                        handleSuccessState()
                    } else {
                        showErrorUI("No internet and failed to load local config.")
                    }
                }
        }
    }












package sync2app.com.syncapplive.additionalSettings

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
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

        makeNetworkRequest()
        binding.button.setOnClickListener {
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

*/
