package com.ecommerce.product.controller;

import com.ecommerce.common.security.JwtCurrentUserProvider;
import com.ecommerce.product.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@Slf4j
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReviewController {

    private final ReviewService reviewService;
    private final JwtCurrentUserProvider currentUserProvider;

    @PostMapping("/add")
    public ResponseEntity<?>
    addReview(@RequestBody Map<String, Object> payload) {
        Long userId = currentUserProvider.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập hệ thống!");
        }

        try {
            Long orderItemId = ((Number) payload.get("orderItemId")).longValue();
            Long productId = ((Number) payload.get("productId")).longValue();
            Integer rating = ((Number) payload.get("rating")).intValue();
            String comment = payload.get("comment") != null ? payload.get("comment").toString() : "";

            var review = reviewService.createReview(userId, orderItemId, rating, comment, productId);
            return ResponseEntity.ok(Map.of("message", "Gửi đánh giá thành công!", "data", review));
        } catch (Exception e) {
            log.error("Lỗi đánh giá: ", e);
            return ResponseEntity.badRequest().body(Map.of("message", "Dữ liệu không hợp lệ: " + e.getMessage()));
        }
    }

    @GetMapping("/my-reviews")
    public ResponseEntity<?> getMyReviews() {
        Long userId = currentUserProvider.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập!");
        }

        return ResponseEntity.ok(reviewService.getUserReviews(userId));
    }
}