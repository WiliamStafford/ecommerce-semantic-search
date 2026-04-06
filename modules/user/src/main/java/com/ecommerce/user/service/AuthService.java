package com.ecommerce.user.service;

import com.ecommerce.user.dto.request.AuthRequest;
import com.ecommerce.user.dto.request.RegisterRequest;
import com.ecommerce.user.dto.response.AuthResponse;
import jakarta.transaction.Transactional;

public interface AuthService {
    @Transactional
    AuthResponse register(RegisterRequest request);

    AuthResponse authenticate(AuthRequest request);
}