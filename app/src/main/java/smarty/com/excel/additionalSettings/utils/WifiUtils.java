package smarty.com.excel.additionalSettings.utils;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiUtils {

    public static void connectToWifi(Context context, String ssid, String password, String securityType) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration wifiConfig = new WifiConfiguration();

        wifiConfig.SSID = "\"" + ssid + "\""; // Surround SSID with double quotes

        switch (securityType.toUpperCase()) {
            case "WPA":
            case "WPA2":
                wifiConfig.preSharedKey = "\"" + password + "\""; // Surround password with double quotes
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                break;
            case "WEP":
                wifiConfig.wepKeys[0] = "\"" + password + "\""; // Surround password with double quotes
                wifiConfig.wepTxKeyIndex = 0;
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                break;
            case "OPEN":
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                break;
            default:
                Log.e("WifiUtils", "Unsupported security type: " + securityType);
                return;
        }

        int netId = wifiManager.addNetwork(wifiConfig);
        if (netId == -1) {
            Log.e("WifiUtils", "Failed to add network configuration");
            return;
        }

        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
    }






}
