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

    private static Retrofit getClient() {
        if (retrofit == null) {
            // ✅ NEW: Add timeouts
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS);

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.API_BASE_URL)  // ✅ FIXED
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