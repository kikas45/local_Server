package sync2app.com.syncapplive.AppNetworkModule

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AppRetrofitInstance {

    fun createApiService(baseUrl: String): RemoteConfigApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(RemoteConfigApi::class.java)
    }
}





/*
package sync2app.com.syncapplive.AppNetworkModule

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AppRetrofitInstance {
    private const val BASE_URL ="https://cp.cloudappserver.co.uk/app_base/public/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: RemoteConfigApi by lazy {
        retrofit.create(RemoteConfigApi::class.java)
    }
}
*/
