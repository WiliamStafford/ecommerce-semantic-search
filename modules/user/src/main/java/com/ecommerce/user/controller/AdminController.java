package com.ecommerce.user.controller;

import com.ecommerce.user.domain.User;
import com.ecommerce.user.dto.request.UserUpdateReq;
import com.ecommerce.user.dto.response.UserResponseDTO;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final UserRepository userRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/sellers/pending")
    public ResponseEntity<?> getPendingSellers() {
        return ResponseEntity.ok(userService.findAllPending());
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/sellers/{id}/approve")
    public ResponseEntity<?> approveSeller(@PathVariable Long id) {
        userService.promoteToSeller(id);
        return ResponseEntity.ok("Duyệt đăng ký thành công");
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/sellers/{id}/reject")
    public ResponseEntity<?> rejectSeller(@PathVariable Long id) {
        userService.rejectRegistration(id);
        return ResponseEntity.ok("Đã từ chối đơn đăng ký");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAllUsers());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{id}/block")
    public ResponseEntity<?> blockUser(@PathVariable Long id) {
        userService.blockUser(id);
        return ResponseEntity.ok("Đã khóa tài khoản");
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{id}")
    public ResponseEntity<Void> updateUserByAdmin(@PathVariable Long id, @RequestBody UserUpdateReq request) {
        userService.updateUserByAdmin(id, request);
        return ResponseEntity.ok().build();
    }

    // --- QUẢN LÝ SHOP ---
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/sellers")
    public ResponseEntity<?> getAllShops() {
        return ResponseEntity.ok(userService.findAllShops());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/sellers/{id}/close")
    public ResponseEntity<?> closeShop(@PathVariable Long id) {
        userService.closeShop(id);
        return ResponseEntity.ok("Đã đóng shop");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users-with-addresses")
    public ResponseEntity<List<UserResponseDTO>> getAllUsersWithAddresses() {
        return ResponseEntity.ok(userService.getAllUsersWithAddress());
    }

}