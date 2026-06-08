package com.ecommerce.order.service.impl;

import com.ecommerce.order.domain.Cart;
import com.ecommerce.order.domain.CartItem;
import com.ecommerce.order.dto.request.CartItemRequest;
import com.ecommerce.order.dto.response.CartItemResponse;
import com.ecommerce.order.dto.response.CartResponse;
import com.ecommerce.order.repository.CartItemRepository;
import com.ecommerce.order.repository.CartRepository;
import com.ecommerce.order.service.CartService;
import com.ecommerce.product.service.ProductService;
import com.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserService userService;
    private final ProductService productService;

    // Helper: Tìm hoặc tạo giỏ hàng mới cho user
    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder()
                                .userId(userId)
                                .build()
                ));
    }

    @Override
    @Transactional
    public CartItem addToCart(Long userId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);

        // Tìm xem sản phẩm đã có trong giỏ chưa để cộng dồn[cite: 1, 3]
        return cartItemRepository.findByCartIdAndSellerProductId(cart.getId(), request.getSellerProductId())
                .map(item -> {
                    item.setQuantity(item.getQuantity() + request.getQuantity());
                    return cartItemRepository.save(item);
                })
                .orElseGet(() -> {
                    CartItem newItem = CartItem.builder()
                            .cartId(cart.getId())
                            .sellerProductId(request.getSellerProductId())
                            .quantity(request.getQuantity())
                            .build();
                    return cartItemRepository.save(newItem);
                });
    }

    @Override
    public List<CartItem> getCartItems(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại"));
        return cartItemRepository.findByCartId(cart.getId());
    }

    @Override
    @Transactional
    public void updateQuantity(Long userId, Long sellerProductId, int delta) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại"));

        CartItem item = cartItemRepository.findByCartIdAndSellerProductId(cart.getId(), sellerProductId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ"));

        int newQuantity = item.getQuantity() + delta;
        if (newQuantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        }
    }

    @Override
    @Transactional
    public void removeFromCart(Long userId, Long sellerProductId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại"));

        cartItemRepository.findByCartIdAndSellerProductId(cart.getId(), sellerProductId)
                .ifPresent(cartItemRepository::delete);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại"));
        cartItemRepository.deleteAllByCartId(cart.getId());
    }


    @Override
    public CartResponse getCartSummary(Long userId) {
        Cart cart = getOrCreateCart(userId);
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        double totalPrice = 0.0;
        List<CartItemResponse> itemResponses = new ArrayList<>();
        for (CartItem item : items) {
            var product = productService.getSellerProductById(item.getSellerProductId());
            totalPrice += item.getQuantity() * product.getPrice();
            itemResponses.add(CartItemResponse.builder()
                    .id(item.getId())
                    .sellerProductId(item.getSellerProductId())
                    .quantity(item.getQuantity())
                    .productName(product.getName())
                    .price(product.getPrice())
                    .imageUrl(product.getImageUrl())
                    .build());
        }

        // 4. Đóng gói kết quả cuối cùng vào CartResponse
        return CartResponse.builder()
                .cartId(cart.getId())
                .items(itemResponses)
                .totalPrice(totalPrice)
                .build();
    }
}