package sync2app.com.syncapplive.AppNetworkModule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class RemoteConfigViewModel : ViewModel() {
    private val repository = RemoteConfigRepository()

    private val _configLiveData = MutableLiveData<Result<RemoteConfigResponse>>()
    val configLiveData: LiveData<Result<RemoteConfigResponse>> = _configLiveData

    fun getRemoteConfig(baseUrl: String, endpoint: String) {
        viewModelScope.launch {
            val api = repository.createApi(baseUrl)
            val result = repository.fetchRemoteConfig(api, endpoint)
            _configLiveData.value = result
        }
    }
}



/*
package sync2app.com.syncapplive.AppNetworkModule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class RemoteConfigViewModel : ViewModel() {
    private val _configLiveData = MutableLiveData<Result<RemoteConfigResponse>>()
    val configLiveData: LiveData<Result<RemoteConfigResponse>> = _configLiveData

    fun getRemoteConfig(url: String) {
        viewModelScope.launch {
            try {
                val response = AppRetrofitInstance.api.getRemoteConfig(url)
                _configLiveData.value = Result.success(response)
            } catch (e: Exception) {
                _configLiveData.value = Result.failure(e)
            }
        }
    }
}
*/
