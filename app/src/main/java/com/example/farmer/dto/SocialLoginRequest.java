package com.example.farmer.dto;

/**
 * Payload sent to {@code /api/users/google}.
 *
 * <p>
 * The {@code idToken} is the Google ID token obtained from Credential Manager;
 * the backend verifies its signature and audience before trusting it. The
 * optional {@code userType} lets the client express the desired role on first
 * registration (ignored for existing accounts).
 * </p>
 */
public class SocialLoginRequest {

    private String idToken;
    private String userType;

    public SocialLoginRequest() {
    }

    public SocialLoginRequest(String idToken, String userType) {
        this.idToken = idToken;
        this.userType = userType;
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
