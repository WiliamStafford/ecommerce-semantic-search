package com.ecommerce.order.service;

import com.ecommerce.order.domain.CartItem;
import com.ecommerce.order.dto.request.CartItemRequest;
import com.ecommerce.order.dto.response.CartResponse;

import java.util.List;

public interface CartService {
    CartItem addToCart(Long userId, CartItemRequest request);

    List<CartItem> getCartItems(Long userId);

    void updateQuantity(Long userId, Long sellerProductId, int delta);

    void removeFromCart(Long userId, Long sellerProductId);

    void clearCart(Long userId);

    CartResponse getCartSummary(Long userId);
}