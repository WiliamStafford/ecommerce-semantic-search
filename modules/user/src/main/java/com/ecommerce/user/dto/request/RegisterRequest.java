package com.ecommerce.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.lang.Nullable;

public record RegisterRequest(
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
        String password,

        @Nullable
        String fullName,

        @Nullable
        String avatar,

        @Nullable
        Integer age,

        @Nullable
        @Pattern(regexp = "^\\d{10,11}$", message = "Số điện thoại phải có từ 10-11 chữ số")
        String phone
) {

}