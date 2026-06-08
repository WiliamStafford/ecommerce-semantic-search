package com.ecommerce.product.service;

import com.ecommerce.product.domain.Wishlist;
import com.ecommerce.product.dto.response.ProductResponseDTO;

import java.util.List;
import java.util.Set;

public interface WishlistService {

    void toggleWishlist(Long userId, Long productId);

    List<Wishlist> getMyWishlist(Long userId);


    Set<Long> getFavoriteSellerProductIds(Long userId);
    boolean isFavorite(Long userId, Long sellerProductId);

    List<ProductResponseDTO> getProductsWithFavoriteStatus(Long userId);

//    List<ProductResponseDTO> getProductsWithFavoriteStatus(Long userId);
// Thêm dòng này vào cuối file WishlistService.java
List<com.ecommerce.product.dto.response.ProductHomeResponse> getFavoriteProductsForUser(Long userId);

    void deleteBySellerProductId(Long id);
}