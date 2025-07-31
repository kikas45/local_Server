package sync2app.com.syncapplive.AppNetworkModule

import retrofit2.http.GET
import retrofit2.http.Url

interface RemoteConfigApi {
    @GET
    suspend fun getRemoteConfig(@Url url: String): RemoteConfigResponse
}
