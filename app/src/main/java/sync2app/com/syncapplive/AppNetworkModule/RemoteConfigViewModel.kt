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

    fun getRemoteConfig(fullUrl: String) {
        viewModelScope.launch {
            // Extract a valid base URL from the full URL
            val dummyBaseUrl = fullUrl.substringBeforeLast("/") + "/"

            val api = repository.createApi(dummyBaseUrl)
            val result = repository.fetchRemoteConfig(api, fullUrl)
            _configLiveData.value = result
        }
    }
}




/*
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
*/
