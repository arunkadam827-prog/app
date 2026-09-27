package com.example.farmer.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.farmer.R;

public class SettingsActivity extends AppCompatActivity {

        private Switch notificationSwitch;
        private Switch darkModeSwitch;
        private Button privacyButton;
        private Button languageButton;
        private Button logoutButton;

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

                // Logout
                logoutButton.setOnClickListener(v -> logout());
        }

        private void logout() {
                new com.example.farmer.utils.SessionManager(this).logout();

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
