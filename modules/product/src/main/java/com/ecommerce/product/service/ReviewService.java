package com.ecommerce.product.service;

import com.ecommerce.product.domain.Review;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ReviewService {
    @Transactional
    Review createReview(Long userId, Long orderItemId, int rating, String comment, Long productId);


    List<Review> getReviewsByProduct(Long productId);

    Double calculateAverageRating(Long productId);
}