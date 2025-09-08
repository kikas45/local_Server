package smarty.com.excel.additionalSettings.utils;

import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class UrlCheckerJava {

    public static boolean checkUrlExistence(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            Log.d("CheckUrlTask", "Response Code: " + responseCode);
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            Log.e("CheckUrlTask", "Error: " + e.getMessage());
            return false;
        }
    }
}