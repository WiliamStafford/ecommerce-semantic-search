package com.ecommerce.product.service.impl;

import com.ecommerce.product.domain.Wishlist;
import com.ecommerce.product.repository.WishlistRepository;
import com.ecommerce.product.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;

    @Override
    @Transactional
    public void toggleWishlist(Long userId, Long productId) {
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            wishlistRepository.deleteByUserIdAndProductId(userId, productId);
        } else {
            Wishlist wishlist = Wishlist.builder()
                    .userId(userId)
                    .productId(productId)
                    .createdAt(LocalDateTime.now())
                    .build();
            wishlistRepository.save(wishlist);
        }
    }

    @Override
    public List<Wishlist> getMyWishlist(Long userId) {
        return wishlistRepository.findAllByUserId(userId);
    }

    @Override
    public boolean isFavorite(Long userId, Long productId) {
        return wishlistRepository.existsByUserIdAndProductId(userId, productId);
    }
}