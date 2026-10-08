package com.example.farmer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.farmer.R;
import com.example.farmer.dto.LoginResponse;
import com.example.farmer.dto.SocialLoginRequest;
import com.example.farmer.models.User;
import com.example.farmer.services.ApiService;
import com.example.farmer.services.RetrofitClient;
import com.example.farmer.utils.GoogleSignInHelper;
import com.example.farmer.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * New-generation registration screen.
 *
 * Keeps the same registration algorithm: build a {@link User}, POST it to the
 * backend, persist the returned session and route to the role dashboard.
 */
public class RegistrationActivity extends AppCompatActivity {

    private static final String TAG = "RegistrationActivity";

    private TextInputLayout nameLayout, emailLayout, passwordLayout, phoneLayout;
    private TextInputEditText etName, etEmail, etPassword, etPhone;
    private RadioGroup roleGroup;
    private MaterialButton btnRegister;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        sessionManager = new SessionManager(this);

        nameLayout = findViewById(R.id.name_layout);
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        phoneLayout = findViewById(R.id.phone_layout);

        etName = findViewById(R.id.name);
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        etPhone = findViewById(R.id.phone);

        roleGroup = findViewById(R.id.role_radio_group);
        btnRegister = findViewById(R.id.register_btn);
        progressBar = findViewById(R.id.registration_progress);

        setupTextWatchers();

        btnRegister.setOnClickListener(v -> handleRegistration());

        View loginTextView = findViewById(R.id.login_text_view);
        if (loginTextView != null) {
            loginTextView.setOnClickListener(v -> finish());
        }

        findViewById(R.id.continue_google_button)
                .setOnClickListener(v -> handleGoogleSignIn());
    }

    // ================= Google Sign-Up =================

    private void handleGoogleSignIn() {
        setLoading(true);
        GoogleSignInHelper.signIn(this, new GoogleSignInHelper.Callback() {
            @Override
            public void onGoogleIdToken(String idToken) {
                performGoogleSignUp(idToken);
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(RegistrationActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void performGoogleSignUp(String idToken) {
        String role = currentRole();

        RetrofitClient.getApiService()
                .googleSignIn(new SocialLoginRequest(idToken, role))
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LoginResponse> call,
                            @NonNull Response<LoginResponse> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess() && response.body().getUser() != null) {
                            User user = response.body().getUser();
                            if (user.getUserType() == null) {
                                user.setUserType(role);
                            }
                            sessionManager.createLoginSession(user);
                            Toast.makeText(RegistrationActivity.this,
                                    "Welcome to Farmer+!", Toast.LENGTH_SHORT).show();
                            navigateToDashboard(user.getUserType());
                        } else {
                            String message = response.body() != null ? response.body().getMessage() : null;
                            Toast.makeText(RegistrationActivity.this,
                                    message != null ? message : "Google sign-in failed. Please try again.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                        setLoading(false);
                        Log.e(TAG, "Google sign-up network failure", t);
                        showNetworkError();
                    }
                });
    }

    private String currentRole() {
        return (roleGroup != null && roleGroup.getCheckedRadioButtonId() == R.id.radio_farmer)
                ? "FARMER"
                : "BUYER";
    }

    private void setupTextWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (nameLayout != null)
                    nameLayout.setError(null);
                if (emailLayout != null)
                    emailLayout.setError(null);
                if (passwordLayout != null)
                    passwordLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        if (etName != null)
            etName.addTextChangedListener(watcher);
        if (etEmail != null)
            etEmail.addTextChangedListener(watcher);
        if (etPassword != null)
            etPassword.addTextChangedListener(watcher);
    }

    private void handleRegistration() {
        String name = text(etName);
        String email = text(etEmail);
        String password = text(etPassword);
        String phone = text(etPhone);

        if (!validateFields(name, email, password)) {
            return;
        }

        setLoading(true);

        // Fixed: Assigned in a single declaration so that it remains effectively final
        final String role = (roleGroup != null && roleGroup.getCheckedRadioButtonId() == R.id.radio_farmer)
                ? "FARMER"
                : "BUYER";

        User newUser = new User(name, email, password, role);
        if (!TextUtils.isEmpty(phone)) {
            newUser.setPhone(phone);
        }

        ApiService apiService = RetrofitClient.getApiService();
        apiService.registerUser(newUser).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    User registeredUser = response.body();

                    // Some backends echo the role; fall back to the selected role.
                    if (registeredUser.getUserType() == null) {
                        registeredUser.setUserType(role);
                    }

                    sessionManager.createLoginSession(registeredUser);

                    Toast.makeText(RegistrationActivity.this,
                            "Welcome to Farmer+!", Toast.LENGTH_SHORT).show();

                    navigateToDashboard(registeredUser.getUserType());
                } else {
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                setLoading(false);
                Log.e(TAG, "Network failure", t);
                showNetworkError();
            }
        });
    }

    private void navigateToDashboard(String userType) {
        Intent intent;
        if ("FARMER".equalsIgnoreCase(userType)) {
            intent = new Intent(this, FarmerDashboardActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean validateFields(String name, String email, String password) {
        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            nameLayout.setError("Name is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email address");
            isValid = false;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
            isValid = false;
        }
        return isValid;
    }

    private void setLoading(boolean isLoading) {
        if (btnRegister != null)
            btnRegister.setEnabled(!isLoading);
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Friendly, actionable message when the backend cannot be reached.
     * The most common cause on a real phone is a wrong/unreachable server URL.
     */
    private void showNetworkError() {
        Toast.makeText(this,
                "Cannot reach the server.\n\nFix: Settings → Server URL → enter the "
                        + "backend address (e.g. http://"
                        + "192.168.1.5:8080). Phone and PC must be on the same Wi-Fi.",
                Toast.LENGTH_LONG).show();
    }

    private void handleErrorResponse(Response<User> response) {
        if (response.code() == 409) {
            emailLayout.setError("This email is already registered");
        } else {
            Toast.makeText(this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
        }
    }

    private String text(TextInputEditText field) {
        return field == null || field.getText() == null ? "" : field.getText().toString().trim();
    }
}