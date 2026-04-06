package com.ecommerce.product.controller;

import com.ecommerce.product.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/{productId}/toggle")
    public ResponseEntity<?> toggleWishlist(@RequestParam Long userId, @PathVariable Long productId) {
        wishlistService.toggleWishlist(userId, productId);
        return ResponseEntity.ok("Đã cập nhật danh sách yêu thích");
    }

    @GetMapping("/my-list")
    public ResponseEntity<?> getMyWishlist(@RequestParam Long userId) {
        return ResponseEntity.ok(wishlistService.getMyWishlist(userId));
    }

    @GetMapping("/{productId}/check")
    public ResponseEntity<Boolean> checkFavorite(@RequestParam Long userId, @PathVariable Long productId) {
        return ResponseEntity.ok(wishlistService.isFavorite(userId, productId));
    }
}