package com.example.farmer.services;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;
import com.example.farmer.BuildConfig;

public class RetrofitClient {

    // ✅ FIXED: Use BuildConfig for dynamic URL
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    public static String getBaseUrl() {
        boolean isEmulator = android.os.Build.HARDWARE.contains("goldfish")
                || android.os.Build.HARDWARE.contains("ranchu")
                || android.os.Build.MODEL.contains("sdk")
                || android.os.Build.PRODUCT.contains("sdk");
        if (isEmulator) {
            return "http://10.0.2.2:8080/";
        }
        return BuildConfig.API_BASE_URL;
    }

    private static Retrofit getClient() {
        if (retrofit == null) {
            // ✅ NEW: Add timeouts
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS);

            retrofit = new Retrofit.Builder()
                    .baseUrl(getBaseUrl())
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getClient().create(ApiService.class);
        }
        return apiService;
    }
}