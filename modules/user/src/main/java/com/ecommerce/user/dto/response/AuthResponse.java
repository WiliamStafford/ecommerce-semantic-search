package com.ecommerce.user.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String email,
        String message
) {}