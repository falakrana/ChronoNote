package com.noteapp.noteapp.dto.auth;

public record AuthResponse(
        String token,
        String tenantId,
        String email
) {
}
