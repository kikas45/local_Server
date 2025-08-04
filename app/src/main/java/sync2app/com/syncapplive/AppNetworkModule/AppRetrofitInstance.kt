package sync2app.com.syncapplive.AppNetworkModule

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AppRetrofitInstance {
    fun createApiService(dummyBaseUrl: String): RemoteConfigApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(dummyBaseUrl) // Required, even though overridden by @Url
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(RemoteConfigApi::class.java)
    }
}



/*
object AppRetrofitInstance {

    fun createApiService(baseUrl: String): RemoteConfigApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(RemoteConfigApi::class.java)
    }
}
*/
