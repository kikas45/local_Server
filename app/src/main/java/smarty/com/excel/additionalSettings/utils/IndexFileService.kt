package smarty.com.excel.additionalSettings.utils

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface IndexFileService {
    @GET
    fun getIndexFile(@Url url: String): Call<String>
}