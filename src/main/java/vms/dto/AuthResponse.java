package vms.dto;

public record AuthResponse(
    String message,
    String token,
    String username,
    String email,
    String role
) {}
