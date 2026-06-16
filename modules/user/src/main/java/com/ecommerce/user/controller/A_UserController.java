package com.ecommerce.user.controller;

import com.ecommerce.user.dto.request.UserUpdateReq;
import com.ecommerce.user.dto.response.UserResponseDTO;
import com.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/A_User")
@RequiredArgsConstructor
public class A_UserController {

    private final UserService userService;

    @PatchMapping("/buyers/{id}/status")
    public ResponseEntity<?> changeBuyerStatus(@PathVariable Long id, @RequestParam boolean enabled) {
        userService.updateUserStatus(id, enabled);
        String msg = enabled ? "Đã mở khóa tài khoản người mua" : "Đã khóa tài khoản người mua";
        return ResponseEntity.ok(msg);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/sellers/{id}/status")
    public ResponseEntity<?> changeSellerStatus(@PathVariable Long id, @RequestParam boolean enabled) {
        userService.updateUserStatus(id, enabled);
        String msg = enabled ? "Đã cho phép Seller hoạt động lại" : "Đã đình chỉ quyền bán hàng của Seller";
        return ResponseEntity.ok(msg);
    }

    //    @GetMapping("/list")
//    public ResponseEntity<?> getAllUsersByRole(@RequestParam String role) {
//        return ResponseEntity.ok(userService.findAllByRole(role));
//    }
    @GetMapping("/list")
    public ResponseEntity<List<UserResponseDTO>> getAllUsersByRole(@RequestParam String role) {
        return ResponseEntity.ok(userService.findAllUsersWithAddressByRole(role));
    }

    @PatchMapping("/sellers/{id}/approve")
    public ResponseEntity<?> approveSeller(@PathVariable Long id) {
        userService.promoteToSeller(id);
        return ResponseEntity.ok("Người dùng đã được phê duyệt làm Seller thành công");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUserByAdmin(@PathVariable Long id, @RequestBody UserUpdateReq request) {
        userService.updateUserByAdmin(id, request);
        return ResponseEntity.ok("Cập nhật thông tin người dùng thành công");
    }
}