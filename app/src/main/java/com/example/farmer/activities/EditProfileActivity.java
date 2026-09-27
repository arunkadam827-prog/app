package com.example.farmer.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.farmer.R;
import com.example.farmer.models.User;
import com.example.farmer.services.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private EditText addressEditText;
    private EditText cityEditText;

    private Button saveButton;
    private SharedPreferences preferences;
    private Long userId; // ✅ FIXED: Changed from int to Long

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        nameEditText = findViewById(R.id.edit_name);
        emailEditText = findViewById(R.id.edit_email);
        phoneEditText = findViewById(R.id.edit_phone);
        addressEditText = findViewById(R.id.edit_address);
        cityEditText = findViewById(R.id.edit_city);
        saveButton = findViewById(R.id.save_profile_button);

        findViewById(R.id.back_button).setOnClickListener(v -> finish());

        preferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        userId = preferences.getLong("user_id", -1L); // ✅ FIXED: Get as Long

        loadUserDataFromPrefs();

        saveButton.setOnClickListener(v -> saveProfileToServer());
    }

    private void loadUserDataFromPrefs() {
        nameEditText.setText(preferences.getString("user_name", ""));
        emailEditText.setText(preferences.getString("user_email", ""));
        phoneEditText.setText(preferences.getString("user_phone", ""));
        addressEditText.setText(preferences.getString("user_address", ""));
        cityEditText.setText(preferences.getString("user_location", ""));

        // Email remains disabled as it's the unique identifier
        emailEditText.setEnabled(false);
    }

    private void saveProfileToServer() {
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();

        // Validations
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            phoneEditText.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(address)) {
            addressEditText.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(city)) {
            cityEditText.setError("Required");
            return;
        }

        saveButton.setEnabled(false);

        // 1. Create a User object with updated details
        User updatedUser = new User();
        updatedUser.setFullName(name);
        updatedUser.setPhone(phone);
        updatedUser.setAddress(address);
        updatedUser.setCity(city);
        // Add email so the backend knows which user to update if needed
        updatedUser.setEmail(preferences.getString("user_email", ""));

        // 2. Call Retrofit - no casting needed now ✅ FIXED
        RetrofitClient.getApiService().updateProfile(userId, updatedUser)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        saveButton.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            // 3. Update local SharedPreferences to keep UI in sync
                            updateLocalPrefs(name, phone, address, city);

                            Toast.makeText(EditProfileActivity.this,
                                    "Profile updated on server!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(EditProfileActivity.this,
                                    "Failed to update: " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        saveButton.setEnabled(true);
                        Toast.makeText(EditProfileActivity.this,
                                "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updateLocalPrefs(String name, String phone, String address, String city) {
        preferences.edit()
                .putString("user_name", name)
                .putString("user_phone", phone)
                .putString("user_address", address)
                .putString("user_location", city)
                .apply();
    }
}