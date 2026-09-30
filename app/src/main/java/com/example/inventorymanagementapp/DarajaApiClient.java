package com.example.inventorymanagementapp;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DarajaApiClient {


    private static final String CONSUMER_KEY = "l4amFX1kAbuyWIqTZqKDMfy63WjNA1AfXjqIolGjOGB8ytQO";
    private static final String CONSUMER_SECRET = "t5VvZAA7BTAzMo4rLAJglPKsyWM9cXO0avPAzY4EnoO4mce2KQkVErNFSVofxGox";

    private static final String BUSINESS_SHORT_CODE = "174379";
    private static final String PASSKEY = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919";
    private static final String TOKEN_URL = "https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials";
    private static final String STK_URL = "https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest";
    private static final String TAG = "DarajaAPI";

    public interface MpesaListener {
        void onSuccess();
        void onError(String error);
    }

    public void triggerStkPush(String phoneNumber, String amount, MpesaListener listener) {
        new Thread(() -> {
            try {
                // --- 1. GET AUTH TOKEN ---
                String keys = CONSUMER_KEY + ":" + CONSUMER_SECRET;
                String authHeader = "Basic " + Base64.encodeToString(keys.getBytes(), Base64.NO_WRAP);

                HttpURLConnection conn = (HttpURLConnection) new URL(TOKEN_URL).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", authHeader);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                if (conn.getResponseCode() != 200) {
                    String errorBody = readStream(conn.getErrorStream());
                    Log.e(TAG, "Token Failed: " + errorBody);
                    new Handler(Looper.getMainLooper()).post(() -> listener.onError("Auth Failed. Check Keys."));
                    return;
                }

                String response = readStream(conn.getInputStream());
                String accessToken = new JSONObject(response).getString("access_token");

                // --- 2. FORMAT PHONE NUMBER ---
                String sanitizedPhone = phoneNumber.trim();
                if (sanitizedPhone.startsWith("0")) sanitizedPhone = "254" + sanitizedPhone.substring(1);
                if (sanitizedPhone.startsWith("+")) sanitizedPhone = sanitizedPhone.substring(1);

                // --- 3. SEND STK PUSH ---
                String timestamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
                String password = Base64.encodeToString((BUSINESS_SHORT_CODE + PASSKEY + timestamp).getBytes(), Base64.NO_WRAP);

                JSONObject json = new JSONObject();
                json.put("BusinessShortCode", BUSINESS_SHORT_CODE);
                json.put("Password", password);
                json.put("Timestamp", timestamp);
                json.put("TransactionType", "CustomerPayBillOnline");

                // USE THE REAL AMOUNT (Passed from CartActivity)
                json.put("Amount", amount);

                json.put("PartyA", sanitizedPhone);
                json.put("PartyB", BUSINESS_SHORT_CODE);
                json.put("PhoneNumber", sanitizedPhone);
                json.put("CallBackURL", "https://mydomain.com/path");


                json.put("AccountReference", "Tausi Books");
                json.put("TransactionDesc", "Purchase of Stationery");

                HttpURLConnection stkConn = (HttpURLConnection) new URL(STK_URL).openConnection();
                stkConn.setRequestMethod("POST");
                stkConn.setRequestProperty("Authorization", "Bearer " + accessToken);
                stkConn.setRequestProperty("Content-Type", "application/json");
                stkConn.setDoOutput(true);

                OutputStream os = stkConn.getOutputStream();
                os.write(json.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                // --- 4. HANDLE RESPONSE ---
                if (stkConn.getResponseCode() == 200) {
                    new Handler(Looper.getMainLooper()).post(() -> listener.onSuccess());
                } else {
                    String errorBody = readStream(stkConn.getErrorStream());
                    Log.e(TAG, "STK Failed: " + errorBody);
                    new Handler(Looper.getMainLooper()).post(() -> listener.onError("STK Failed: " + errorBody));
                }

            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
                new Handler(Looper.getMainLooper()).post(() -> listener.onError("Error: " + e.getMessage()));
            }
        }).start();
    }

    // HELPER TO READ STREAMS SAFELY
    private String readStream(InputStream inputStream) {
        if (inputStream == null) return "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}