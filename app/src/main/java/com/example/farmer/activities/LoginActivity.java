package com.example.farmer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.farmer.R;
import com.example.farmer.dto.LoginResponse;
import com.example.farmer.dto.SocialLoginRequest;
import com.example.farmer.models.User;
import com.example.farmer.services.RetrofitClient;
import com.example.farmer.utils.GoogleSignInHelper;
import com.example.farmer.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * New-generation login screen.
 *
 * The authentication algorithm is unchanged: it validates the input locally,
 * posts the credentials to the Spring backend, persists the verified session
 * via {@link SessionManager} and routes the user to the correct dashboard
 * based on the role returned by the server.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText emailField;
    private TextInputEditText passwordField;
    private RadioButton farmerRadio;
    private MaterialButton loginBtn;
    private ProgressBar progressBar;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Restore an active session without flashing the login UI.
        if (sessionManager.isLoggedIn()) {
            String savedRole = sessionManager.getUserType();
            Log.d(TAG, "Restoring active session for role: " + savedRole);
            navigateToDashboard(savedRole);
            return;
        }

        setContentView(R.layout.activity_login);
        initViews();
    }

    private void initViews() {
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        emailField = findViewById(R.id.email_edit_text);
        passwordField = findViewById(R.id.password_edit_text);
        farmerRadio = findViewById(R.id.farmer_radio_button);
        loginBtn = findViewById(R.id.login_button);
        progressBar = findViewById(R.id.login_progress);

        loginBtn.setOnClickListener(v -> attemptLogin());

        findViewById(R.id.signup_text_view)
                .setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegistrationActivity.class)));

        findViewById(R.id.continue_google_button)
                .setOnClickListener(v -> handleGoogleSignIn());
    }

    // ================= Google Sign-In =================

    private void handleGoogleSignIn() {
        setLoading(true);
        GoogleSignInHelper.signIn(this, new GoogleSignInHelper.Callback() {
            @Override
            public void onGoogleIdToken(String idToken) {
                performGoogleSignIn(idToken);
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                showError(message);
            }
        });
    }

    private void performGoogleSignIn(String idToken) {
        // Role is only used if this is a first-time Google account creation.
        String role = farmerRadio != null && farmerRadio.isChecked() ? "FARMER" : "BUYER";

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
                                showError("Server configuration error: account type missing");
                                return;
                            }
                            sessionManager.createLoginSession(user);
                            navigateToDashboard(user.getUserType());
                        } else {
                            String message = response.body() != null ? response.body().getMessage() : null;
                            showError(
                                    !TextUtils.isEmpty(message) ? message : "Google sign-in failed. Please try again.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                        setLoading(false);
                        Log.e(TAG, "Google sign-in network failure", t);
                        showError("Cannot reach the server. Check your connection.");
                    }
                });
    }

    private void attemptLogin() {
        clearErrors();

        String email = text(emailField);
        String password = text(passwordField);

        if (!validateEmail(email))
            return;
        if (!validatePassword(password))
            return;

        // Role is selected purely for UX; the backend remains source of truth.
        String role = farmerRadio != null && farmerRadio.isChecked() ? "FARMER" : "BUYER";
        performNetworkLogin(email, password, role);
    }

    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Enter your email");
            emailField.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email address");
            emailField.requestFocus();
            return false;
        }
        return true;
    }

    private boolean validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Enter your password");
            passwordField.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
            passwordField.requestFocus();
            return false;
        }
        return true;
    }

    private void performNetworkLogin(String email, String password, String role) {
        setLoading(true);

        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", email);
        credentials.put("password", password);
        credentials.put("userType", role);

        Log.d(TAG, "Dispatching login for " + email + " as " + role);

        RetrofitClient.getApiService().login(credentials).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                setLoading(false);
                Log.d(TAG, "HTTP response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    if (loginResponse.isSuccess() && loginResponse.getUser() != null) {
                        User user = loginResponse.getUser();

                        if (user.getUserType() == null) {
                            showError("Server configuration error: account type missing");
                            return;
                        }

                        sessionManager.createLoginSession(user);
                        navigateToDashboard(user.getUserType());
                    } else {
                        String message = loginResponse.getMessage();
                        showError(!TextUtils.isEmpty(message) ? message : "Invalid email or password");
                    }
                } else {
                    showError("Server error (" + response.code() + "). Please try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                setLoading(false);
                Log.e(TAG, "Network failure", t);
                showError("Cannot reach the server. Check your connection.");
            }
        });
    }

    private void navigateToDashboard(String userType) {
        Intent intent;
        if ("FARMER".equalsIgnoreCase(userType)) {
            intent = new Intent(LoginActivity.this, FarmerDashboardActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, MainActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (loginBtn != null) {
            loginBtn.setEnabled(!isLoading);
        }
    }

    private void clearErrors() {
        if (emailLayout != null)
            emailLayout.setError(null);
        if (passwordLayout != null)
            passwordLayout.setError(null);
    }

    private String text(TextInputEditText field) {
        return field == null || field.getText() == null ? "" : field.getText().toString().trim();
    }

    private void showError(String message) {
        if (message == null || message.trim().isEmpty()) {
            message = "Login failed";
        }
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
