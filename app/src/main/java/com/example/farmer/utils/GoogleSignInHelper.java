package com.example.farmer.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;

import com.example.farmer.BuildConfig;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Thin wrapper around the AndroidX Credential Manager for "Sign in with
 * Google".
 *
 * <p>
 * It requests a Google ID token (JWT) from the device and hands it back to the
 * caller. The token is then verified by the Spring backend — the client never
 * trusts the profile on its own.
 * </p>
 */
public final class GoogleSignInHelper {

    private static final String TAG = "GoogleSignInHelper";

    public interface Callback {
        /** Called with a Google-issued ID token (JWT) ready to send to the backend. */
        void onGoogleIdToken(String idToken);

        /** Called with a user-facing error message. */
        void onError(String message);
    }

    private GoogleSignInHelper() {
    }

    /**
     * @return {@code true} when a real Web Client ID has been provided in
     *         {@code BuildConfig.GOOGLE_WEB_CLIENT_ID}.
     */
    public static boolean isConfigured() {
        String clientId = BuildConfig.GOOGLE_WEB_CLIENT_ID;
        return clientId != null
                && !clientId.isEmpty()
                && !clientId.startsWith("REPLACE_WITH")
                && clientId.endsWith(".apps.googleusercontent.com");
    }

    /**
     * Launches the Google sign-in bottom sheet and retrieves an ID token.
     *
     * @param context  an Activity context (required for the credential UI)
     * @param callback receives the token or a user-facing error
     */
    public static void signIn(@NonNull Context context, @NonNull Callback callback) {
        if (!isConfigured()) {
            callback.onError("Google sign-in is not configured yet. Add your Web Client ID.");
            return;
        }

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        CredentialManager credentialManager = CredentialManager.create(context);
        Executor executor = Executors.newSingleThreadExecutor();

        credentialManager.getCredentialAsync(
                context,
                request,
                null,
                executor,
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleResult(result, callback);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e(TAG, "Credential retrieval failed", e);
                        callback.onError(describe(e));
                    }
                });
    }

    private static void handleResult(GetCredentialResponse response, Callback callback) {
        Credential credential = response.getCredential();

        if (credential instanceof CustomCredential
                && GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
            try {
                GoogleIdTokenCredential googleIdToken = GoogleIdTokenCredential.createFrom(credential.getData());
                callback.onGoogleIdToken(googleIdToken.getIdToken());
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse Google ID token", e);
                callback.onError("Google sign-in failed. Please try again.");
            }
        } else {
            Log.e(TAG, "Unexpected credential type: " + credential.getType());
            callback.onError("Google sign-in failed. Please try again.");
        }
    }

    private static String describe(GetCredentialException e) {
        if (e instanceof GetCredentialCancellationException) {
            return "Google sign-in cancelled.";
        }
        if (e instanceof NoCredentialException) {
            return "No Google account found on this device.";
        }
        return "Google sign-in failed. Please try again.";
    }
}
