package com.ecommerce.user.controller;


import com.ecommerce.user.dto.request.AuthRequest;
import com.ecommerce.user.dto.request.ForgotPasswordRequest;
import com.ecommerce.user.dto.request.RegisterRequest;
import com.ecommerce.user.dto.request.ResetPasswordRequest;
import com.ecommerce.user.dto.response.AuthResponse;
import com.ecommerce.user.service.AuthService;
import com.ecommerce.user.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class PasswordController {

    private final PasswordResetService passwordResetService;
    private final AuthService authService;

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.sendResetCode(request.email());
        return ResponseEntity.ok("Mã xác nhận đã được gửi về email của bạn.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Yêu cầu đổi mật khẩu mới cho email: {}", request.email());
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok("Mật khẩu của bạn đã được thay đổi thành công!");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        log.info("Yêu cầu đăng nhập cho email: {}", request.email());
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Yêu cầu đăng ký tài khoản mới: {}", request.email());
        return ResponseEntity.ok(authService.register(request));
    }

}