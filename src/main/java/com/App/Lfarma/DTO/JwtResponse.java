// Andorid

package com.App.Lfarma.DTO;

public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String username;
    private String role;
    private String message;

    public JwtResponse(String token, String username, String role, String message) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.message = message;
    }

    // Getters
    public String getToken() { return token; }
    public String getType() { return type; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getMessage() { return message; }
}