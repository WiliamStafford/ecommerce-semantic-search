package com.ecommerce.user.service;

import com.ecommerce.user.dto.request.ResetPasswordRequest;
import jakarta.validation.Valid;

public interface PasswordResetService {
    void sendResetCode(String email);

    void resetPassword(@Valid ResetPasswordRequest request);
}
