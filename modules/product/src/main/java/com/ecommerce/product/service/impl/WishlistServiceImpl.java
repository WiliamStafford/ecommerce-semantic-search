package com.ecommerce.product.service.impl;

import com.ecommerce.product.domain.Product;
import com.ecommerce.product.domain.SellerProduct;
import com.ecommerce.product.domain.Wishlist;
import com.ecommerce.product.dto.response.ProductHomeResponse; // Dùng chung định dạng trang chủ
import com.ecommerce.product.dto.response.ProductResponseDTO;
import com.ecommerce.product.repository.jpa.ProductRepository; // 🌟 Bổ sung để lấy tên, ảnh trái cây
import com.ecommerce.product.repository.jpa.SellerProductRepository;
import com.ecommerce.product.repository.jpa.WishlistRepository;
import com.ecommerce.product.service.WishlistService;
import com.ecommerce.product.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final SellerProductRepository sellerProductRepository;
    private final ProductRepository productRepository;
    private final ReviewService reviewService;

    @Override
    @Transactional
    public void toggleWishlist(Long userId, Long sellerProductId) {
        if (wishlistRepository.existsByUserIdAndSellerProductId(userId, sellerProductId)) {
            wishlistRepository.deleteByUserIdAndSellerProductId(userId, sellerProductId);
        } else {
            Wishlist wishlist = Wishlist.builder()
                    .userId(userId)
                    .sellerProductId(sellerProductId)
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
    public Set<Long> getFavoriteSellerProductIds(Long userId) {
        if (userId == null) return Collections.emptySet();

        return wishlistRepository.findAllByUserId(userId)
                .stream()
                .map(Wishlist::getSellerProductId)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isFavorite(Long userId, Long sellerProductId) {
        if (userId == null) return false;
        return wishlistRepository.existsByUserIdAndSellerProductId(userId, sellerProductId);
    }

    @Override
    public List<ProductResponseDTO> getProductsWithFavoriteStatus(Long userId) {
        List<SellerProduct> sellerProducts = sellerProductRepository.findAll();
        Set<Long> favoriteIds = getFavoriteSellerProductIds(userId);

        return sellerProducts.stream().map(sp -> ProductResponseDTO.builder()
                .sellerProductId(sp.getId())
                .productName(sp.getName())
                .price(sp.getPrice())
                .avatar(sp.getImageUrl())
                .averageRating(reviewService.calculateAverageRating(sp.getId()))
                .isFavorite(favoriteIds.contains(sp.getId()))
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public List<ProductHomeResponse> getFavoriteProductsForUser(Long userId) {
        if (userId == null) return Collections.emptyList();

        List<Wishlist> wishlistItems = wishlistRepository.findAllByUserId(userId);
        if (wishlistItems.isEmpty()) return Collections.emptyList();

        Set<Long> favoriteSellerProductIds = wishlistItems.stream()
                .map(Wishlist::getSellerProductId)
                .collect(Collectors.toSet());

        List<SellerProduct> sellerProducts = sellerProductRepository.findAllById(favoriteSellerProductIds);
        if (sellerProducts.isEmpty()) return Collections.emptyList();

        Set<Long> productIds = sellerProducts.stream().map(SellerProduct::getProductId).collect(Collectors.toSet());
        Map<Long, Product> productMap = productRepository.findAllById(productIds)
                .stream().collect(Collectors.toMap(Product::getId, p -> p));

        return sellerProducts.stream().map(sp -> {
            Product p = productMap.get(sp.getProductId());
            return new ProductHomeResponse(
                    sp.getId(),
                    p != null ? p.getProductName() : "Không xác định",
                    sp.getPrice(),
                    p != null ? p.getAvatar() : null,
                    p != null ? p.getOrigin() : "N/A",
                    4.8,
                    true
            );
        }).collect(Collectors.toList());
    }

    @Override
    public void deleteBySellerProductId(Long id) {
        wishlistRepository.deleteBySellerProductId(id);

    }
}