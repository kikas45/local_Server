package sync2app.com.syncapplive.additionalSettings.cloudAppsync.api;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import sync2app.com.syncapplive.additionalSettings.utils.Constants;

public class RetrofitClientJava {

    private static RetrofitClientJava mInstance;
    private Retrofit retrofit;

    final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .build();



    private RetrofitClientJava(Context context) {

        SharedPreferences myDownloadClass = context.getSharedPreferences(Constants.MY_DOWNLOADER_CLASS, Context.MODE_PRIVATE);
        String CP_AP_MASTER_DOMAIN = myDownloadClass.getString(Constants.CP_OR_AP_MASTER_DOMAIN, "");

        retrofit = new Retrofit.Builder()
                .baseUrl(CP_AP_MASTER_DOMAIN)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }



    public static synchronized RetrofitClientJava getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RetrofitClientJava(context.getApplicationContext());
        }
        return mInstance;
    }





    public ApiJava getApi(){

        return retrofit.create(ApiJava.class);

    }
}
