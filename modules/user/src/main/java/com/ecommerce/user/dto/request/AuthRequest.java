package com.ecommerce.user.dto.request;

import com.ecommerce.user.enums.AuthType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AuthRequest(
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        String email,
        String password,
        @NotNull(message = "Phương thức đăng nhập không được để trống")
        AuthType authType
) {}