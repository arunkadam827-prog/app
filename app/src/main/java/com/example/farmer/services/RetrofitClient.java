package com.example.farmer.services;

import android.content.Context;
import android.content.SharedPreferences;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;
import com.example.farmer.BuildConfig;

/**
 * Central HTTP client factory.
 *
 * The base URL is resolved in this order:
 * 1. A user-configured server URL (saved in SharedPreferences via
 * {@link #setServerUrl(Context, String)} — set from Settings → Server URL).
 * 2. The emulator loopback (http://10.0.2.2:8080/) when running on an emulator.
 * 3. The compile-time {@link BuildConfig#API_BASE_URL} fallback.
 *
 * This lets an APK installed on ANY phone reach the backend by simply entering
 * the PC's IP in Settings, without rebuilding the app.
 */
public class RetrofitClient {

    private static final String PREF_NAME = "app_settings";
    private static final String KEY_SERVER_URL = "server_url";

    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    /** Returns the effective base URL (always ends with '/'). */
    public static String getBaseUrl() {
        return getBaseUrl(null);
    }

    /** Same as {@link #getBaseUrl()} but allows passing a Context explicitly. */
    public static String getBaseUrl(Context context) {
        // 1. User-configured override (highest priority)
        if (context != null) {
            String custom = getServerUrl(context);
            if (custom != null && !custom.trim().isEmpty()) {
                return normalize(custom);
            }
        }

        // 2. Emulator → host machine loopback
        if (isEmulator()) {
            return "http://10.0.2.2:8080/";
        }

        // 3. Compile-time default
        return normalize(BuildConfig.API_BASE_URL);
    }

    /**
     * Saves a custom server URL (e.g. "http://192.168.1.5:8080") and resets the
     * cached Retrofit instance so the next request uses the new URL.
     * Pass an empty/null string to fall back to the default URL.
     */
    public static void setServerUrl(Context context, String url) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (url == null || url.trim().isEmpty()) {
            prefs.edit().remove(KEY_SERVER_URL).apply();
        } else {
            prefs.edit().putString(KEY_SERVER_URL, url.trim()).apply();
        }
        // Force rebuild with the new URL
        reset();
    }

    /** Returns the user-configured server URL, or null if none was set. */
    public static String getServerUrl(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String url = prefs.getString(KEY_SERVER_URL, null);
        return (url == null || url.trim().isEmpty()) ? null : url.trim();
    }

    /** True when the app is running on an Android emulator. */
    public static boolean isEmulator() {
        return android.os.Build.HARDWARE.contains("goldfish")
                || android.os.Build.HARDWARE.contains("ranchu")
                || android.os.Build.FINGERPRINT.startsWith("generic")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("sdk")
                || android.os.Build.PRODUCT.contains("sdk")
                || android.os.Build.PRODUCT.contains("emulator");
    }

    /** Ensures the URL ends with a trailing slash as Retrofit requires. */
    private static String normalize(String url) {
        if (url == null || url.trim().isEmpty()) {
            return "http://10.0.2.2:8080/";
        }
        url = url.trim();
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        return url;
    }

    /** Discards the cached Retrofit/ApiService so the next call rebuilds them. */
    public static synchronized void reset() {
        retrofit = null;
        apiService = null;
    }

    private static Retrofit getClient() {
        if (retrofit == null) {
            // Long timeouts: Render's free tier sleeps after 15 min idle and the
            // first request can take ~50s while the service wakes up.
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(90, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS);

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
