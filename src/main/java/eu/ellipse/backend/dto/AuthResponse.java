package eu.ellipse.backend.dto;

import java.util.UUID;

public class AuthResponse {
    private String token;
    private UUID userId;

    public AuthResponse(String token, UUID userId) {
        this.token = token;
        this.userId = userId;
    }

    public String getToken() { return token; }
    public UUID getUserId() { return userId; }
}