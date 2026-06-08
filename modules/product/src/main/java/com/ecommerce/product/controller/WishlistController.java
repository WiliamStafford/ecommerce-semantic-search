package com.ecommerce.product.controller;

import com.ecommerce.common.security.JwtCurrentUserProvider; // 🌟 Inject provider stateless
import com.ecommerce.product.dto.response.ProductResponseDTO;
import com.ecommerce.product.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;
    private final JwtCurrentUserProvider currentUserProvider;

    @PostMapping("/{sellerProductId}/toggle")
    public ResponseEntity<?> toggleWishlist(@PathVariable Long sellerProductId) {
        Long userId = currentUserProvider.getCurrentUserId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập!");

        wishlistService.toggleWishlist(userId, sellerProductId);
        return ResponseEntity.ok("Đã cập nhật danh sách yêu thích");
    }

    @GetMapping("/my-list")
    public ResponseEntity<?> getMyWishlist() {
        Long userId = currentUserProvider.getCurrentUserId(); //
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập!");
        return ResponseEntity.ok(wishlistService.getFavoriteProductsForUser(userId));
    }
}