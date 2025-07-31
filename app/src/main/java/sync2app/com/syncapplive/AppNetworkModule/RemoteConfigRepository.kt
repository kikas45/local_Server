package sync2app.com.syncapplive.AppNetworkModule

class RemoteConfigRepository {

    fun createApi(baseUrl: String): RemoteConfigApi {
        return AppRetrofitInstance.createApiService(baseUrl)
    }

    suspend fun fetchRemoteConfig(api: RemoteConfigApi, endpoint: String): Result<RemoteConfigResponse> {
        return try {
            val response = api.getRemoteConfig(endpoint)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}




/*
package sync2app.com.syncapplive.AppNetworkModule

class RemoteConfigRepository {
    suspend fun fetchRemoteConfig(url: String): Result<RemoteConfigResponse> {
        return try {
            val response = AppRetrofitInstance.api.getRemoteConfig(url)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
*/
