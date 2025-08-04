package sync2app.com.syncapplive.AppNetworkModule

class RemoteConfigRepository {
    fun createApi(dummyBaseUrl: String): RemoteConfigApi {
        return AppRetrofitInstance.createApiService(dummyBaseUrl)
    }

    suspend fun fetchRemoteConfig(api: RemoteConfigApi, fullUrl: String): Result<RemoteConfigResponse> {
        return try {
            val response = api.getRemoteConfig(fullUrl)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}



/*
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
*/
