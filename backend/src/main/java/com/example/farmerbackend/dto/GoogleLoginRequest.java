package com.example.farmerbackend.dto;

/**
 * Request body for {@code POST /api/users/google}.
 *
 * <p>{@code idToken} is the Google-issued ID token (JWT) obtained on the
 * Android client. It is verified server-side before any account is created or
 * logged in.</p>
 */
public class GoogleLoginRequest {

    private String idToken;

    /** Optional role requested on first-time registration (FARMER or BUYER). */
    private String userType;

    public GoogleLoginRequest() {
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
