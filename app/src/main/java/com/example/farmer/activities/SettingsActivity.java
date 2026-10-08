package com.example.farmer.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.farmer.R;
import com.example.farmer.services.ApiService;
import com.example.farmer.services.RetrofitClient;
import com.example.farmer.utils.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SettingsActivity extends AppCompatActivity {

        private Switch notificationSwitch;
        private Switch darkModeSwitch;
        private Button privacyButton;
        private Button languageButton;
        private Button logoutButton;

        private TextView serverUrlCurrent;
        private View serverUrlEdit;
        private Button serverUrlReset;
        private Button serverUrlTest;

        private SharedPreferences preferences;

        @SuppressLint("MissingInflatedId")
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_settings);

                notificationSwitch = findViewById(R.id.notification_switch);
                darkModeSwitch = findViewById(R.id.dark_mode_switch);
                privacyButton = findViewById(R.id.privacy);
                languageButton = findViewById(R.id.language_button);
                logoutButton = findViewById(R.id.settings_logout_button);

                serverUrlCurrent = findViewById(R.id.server_url_current);
                serverUrlEdit = findViewById(R.id.server_url_edit);
                serverUrlReset = findViewById(R.id.server_url_reset);
                serverUrlTest = findViewById(R.id.server_url_test);

                findViewById(R.id.back_button).setOnClickListener(v -> finish());

                preferences = getSharedPreferences("app_settings", MODE_PRIVATE);

                // Load saved settings
                notificationSwitch.setChecked(
                                preferences.getBoolean("notifications", true));

                darkModeSwitch.setChecked(
                                preferences.getBoolean("dark_mode", false));

                // Notification setting
                notificationSwitch.setOnCheckedChangeListener(
                                (buttonView, isChecked) -> {
                                        preferences.edit()
                                                        .putBoolean("notifications", isChecked)
                                                        .apply();

                                        Toast.makeText(
                                                        this,
                                                        isChecked
                                                                        ? "Notifications enabled"
                                                                        : "Notifications disabled",
                                                        Toast.LENGTH_SHORT).show();
                                });

                // Dark mode setting
                darkModeSwitch.setOnCheckedChangeListener(
                                (buttonView, isChecked) -> {
                                        preferences.edit()
                                                        .putBoolean("dark_mode", isChecked)
                                                        .apply();

                                        Toast.makeText(
                                                        this,
                                                        isChecked
                                                                        ? "Dark mode enabled. Restart app to apply."
                                                                        : "Dark mode disabled. Restart app to apply.",
                                                        Toast.LENGTH_SHORT).show();
                                });

                // Privacy
                privacyButton.setOnClickListener(v -> Toast.makeText(
                                this,
                                "Privacy settings coming soon",
                                Toast.LENGTH_SHORT).show());

                // Language
                languageButton.setOnClickListener(v -> Toast.makeText(
                                this,
                                "Language settings coming soon",
                                Toast.LENGTH_SHORT).show());

                // Server URL
                refreshServerUrlLabel();
                serverUrlEdit.setOnClickListener(v -> showServerUrlDialog());
                serverUrlReset.setOnClickListener(v -> {
                        RetrofitClient.setServerUrl(this, null);
                        refreshServerUrlLabel();
                        Toast.makeText(this, R.string.server_url_reset, Toast.LENGTH_SHORT).show();
                });
                serverUrlTest.setOnClickListener(v -> testServerConnection());

                // Logout
                logoutButton.setOnClickListener(v -> logout());
        }

        private void refreshServerUrlLabel() {
                String url = RetrofitClient.getBaseUrl(this);
                serverUrlCurrent.setText(url);
        }

        private void showServerUrlDialog() {
                LinearLayout container = new LinearLayout(this);
                container.setOrientation(LinearLayout.VERTICAL);
                int pad = (int) (20 * getResources().getDisplayMetrics().density);
                container.setPadding(pad, pad / 2, pad, 0);

                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
                input.setHint(R.string.server_url_hint);
                input.setSingleLine(true);
                String current = RetrofitClient.getServerUrl(this);
                if (current != null) {
                        input.setText(current);
                }
                container.addView(input);

                new AlertDialog.Builder(this)
                                .setTitle(R.string.server_url_dialog_title)
                                .setMessage(R.string.server_url_dialog_message)
                                .setView(container)
                                .setPositiveButton(R.string.save_changes, (dialog, which) -> {
                                        String url = input.getText().toString().trim();
                                        if (url.isEmpty()) {
                                                RetrofitClient.setServerUrl(this, null);
                                                refreshServerUrlLabel();
                                                Toast.makeText(this, R.string.server_url_reset,
                                                                Toast.LENGTH_SHORT).show();
                                                return;
                                        }
                                        if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                                Toast.makeText(this, R.string.server_url_invalid,
                                                                Toast.LENGTH_LONG).show();
                                                return;
                                        }
                                        RetrofitClient.setServerUrl(this, url);
                                        refreshServerUrlLabel();
                                        Toast.makeText(this, R.string.server_url_saved,
                                                        Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton(android.R.string.cancel, null)
                                .show();
        }

        /**
         * Pings the configured server on a background thread and reports the result.
         */
        private void testServerConnection() {
                final String url = RetrofitClient.getBaseUrl(this);
                serverUrlTest.setEnabled(false);
                serverUrlTest.setText(R.string.server_url_testing);

                new Thread(() -> {
                        boolean ok = false;
                        try {
                                OkHttpClient client = new OkHttpClient.Builder()
                                                .connectTimeout(8, TimeUnit.SECONDS)
                                                .readTimeout(8, TimeUnit.SECONDS)
                                                .build();
                                Request request = new Request.Builder()
                                                .url(url)
                                                .head()
                                                .build();
                                Response response = client.newCall(request).execute();
                                ok = response.isSuccessful() || response.code() < 500;
                                response.close();
                        } catch (Exception ignored) {
                                ok = false;
                        }
                        final boolean success = ok;
                        runOnUiThread(() -> {
                                serverUrlTest.setEnabled(true);
                                serverUrlTest.setText(R.string.server_url_test);
                                Toast.makeText(this,
                                                success ? R.string.server_url_ok
                                                                : R.string.server_url_fail,
                                                Toast.LENGTH_LONG).show();
                        });
                }).start();
        }

        private void logout() {
                new SessionManager(this).logout();

                Intent intent = new Intent(
                                SettingsActivity.this,
                                LoginActivity.class);

                intent.setFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
                finish();
        }
}
