package com.ecommerce.product.service;

import com.ecommerce.product.domain.Wishlist;
import java.util.List;

public interface WishlistService {

    void toggleWishlist(Long userId, Long productId);

    List<Wishlist> getMyWishlist(Long userId);


    boolean isFavorite(Long userId, Long productId);
}