package com.example.farmerbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Verifies Google ID tokens by asking Google's tokeninfo endpoint.
 *
 * <p>This keeps the backend dependency-free (no Google SDK required) while still
 * validating that the token was issued by Google and that its audience matches
 * the configured OAuth Web Client ID. For high-traffic production use, Google's
 * official {@code google-api-client} library is recommended.</p>
 */
@Service
public class GoogleTokenVerifier {

    private static final String TOKEN_INFO_URL =
            "https://oauth2.googleapis.com/tokeninfo?id_token=";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${google.client-id:}")
    private String expectedClientId;

    /**
     * A verified Google user profile.
     */
    public record GoogleUser(String email, String name, String picture, boolean emailVerified) {
    }

    /**
     * Validates the supplied ID token.
     *
     * @throws RuntimeException when the token is invalid, expired or was issued
     *                          for a different client.
     */
    public GoogleUser verify(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new RuntimeException("Missing Google ID token");
        }

        JsonNode payload;
        try {
            ResponseEntity<JsonNode> response =
                    restTemplate.getForEntity(TOKEN_INFO_URL + idToken, JsonNode.class);
            payload = response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Unable to verify Google token");
        }

        if (payload == null || payload.has("error_description") || payload.has("error")) {
            throw new RuntimeException("Invalid Google token");
        }

        // The audience must match our Web Client ID when one is configured.
        if (expectedClientId != null && !expectedClientId.isBlank()) {
            String aud = text(payload, "aud");
            if (!expectedClientId.equals(aud)) {
                throw new RuntimeException("Google token audience mismatch");
            }
        }

        String email = text(payload, "email");
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Google account has no email");
        }

        boolean emailVerified = "true".equalsIgnoreCase(text(payload, "email_verified"));
        String name = text(payload, "name");
        String picture = text(payload, "picture");

        return new GoogleUser(email, name, picture, emailVerified);
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }
}
