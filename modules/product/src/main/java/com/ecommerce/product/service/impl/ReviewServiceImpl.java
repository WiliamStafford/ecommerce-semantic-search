package com.ecommerce.product.service.impl;

import com.ecommerce.product.domain.Review;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.repository.ReviewRepository;
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
    private final ProductRepository productRepository;

    @Transactional
    @Override
    public Review createReview(Long userId, Long orderItemId, int rating, String comment, Long productId) {

        int deliveredCount = reviewRepository.countDeliveredOrderItems(orderItemId);
        if (deliveredCount == 0) {
            throw new RuntimeException("Bạn chỉ có thể đánh giá sản phẩm khi đơn hàng đã được giao thành công!");
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
}