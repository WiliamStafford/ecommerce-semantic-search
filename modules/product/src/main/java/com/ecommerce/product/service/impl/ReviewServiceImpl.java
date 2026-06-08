package com.ecommerce.product.service.impl;

import com.ecommerce.product.domain.Review;
import com.ecommerce.product.repository.jpa.ReviewRepository;
import com.ecommerce.product.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    @Transactional
    @Override
    public Review createReview(Long userId, Long orderItemId, int rating, String comment, Long productId) {

        if (reviewRepository.existsByOrderItemId(orderItemId)) {
            throw new RuntimeException("Bạn đã gửi bài đánh giá cho sản phẩm này trước đó rồi!");
        }

        if (reviewRepository.countReturnRequestByOrderItemId(orderItemId) > 0) {
            throw new RuntimeException("Mặt hàng này đang nằm trong diện khiếu nại đổi trả, không thể viết đánh giá!");
        }

        if (reviewRepository.countDeliveredOrderItems(orderItemId) == 0) {
            throw new RuntimeException("Bạn không thể đánh giá sản phẩm thuộc hóa đơn chưa hoàn tất hoặc chưa giao thành công!");
        }

        Review review = Review.builder()
                .userId(userId)
                .orderItemId(orderItemId)
                .productId(productId)
                .rating(rating)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return reviewRepository.save(review);
    }
    @Override
    public List<Review> getReviewsByProduct(Long productId) {
        return reviewRepository.findAllByProductId(productId);
    }

    @Override
    public Double calculateAverageRating(Long productId) {
        List<Review> reviews = reviewRepository.findAllByProductId(productId);
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    @Override
    public List<Review> getUserReviews(Long userId) {
        return reviewRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }
}