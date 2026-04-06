package com.ecommerce.user.controller;

import com.ecommerce.user.dto.request.*;
import com.ecommerce.user.dto.response.AuthResponse;
import com.ecommerce.user.service.AuthService;
import com.ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UserUpdateReq request, Principal principal) {
        String email = principal.getName();
        log.info("Cập nhật thông tin profile cho: {}", email);
        return ResponseEntity.ok(userService.updateProfile(email, request));
    }


}