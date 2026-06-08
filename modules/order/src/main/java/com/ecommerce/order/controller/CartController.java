package com.ecommerce.order.controller;

import com.ecommerce.order.domain.CartItem;
import com.ecommerce.order.dto.request.CartItemRequest;
import com.ecommerce.order.dto.response.CartItemResponse;
import com.ecommerce.order.dto.response.CartResponse;
import com.ecommerce.order.service.CartService;
import com.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final ProductService productService;

    @PostMapping("/add")
    public ResponseEntity<CartItemResponse> addToCart(
            @RequestParam Long userId,
            @RequestBody CartItemRequest request) {
        CartItem item = cartService.addToCart(userId, request);
        return ResponseEntity.ok(mapToResponse(item));
    }

    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getCartItems(@RequestParam Long userId) {
        List<CartItem> items = cartService.getCartItems(userId);
        List<CartItemResponse> response = items.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-quantity")
    public ResponseEntity<String> updateQuantity(
            @RequestParam Long userId,
            @RequestParam Long sellerProductId,
            @RequestParam int delta) {
        cartService.updateQuantity(userId, sellerProductId, delta);
        return ResponseEntity.ok("Cập nhật số lượng thành công");
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<String> removeFromCart(
            @RequestParam Long userId,
            @PathVariable Long productId) {
        cartService.removeFromCart(userId, productId);
        return ResponseEntity.ok("Đã xóa sản phẩm khỏi giỏ hàng");
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(@RequestParam Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok("Giỏ hàng đã được làm trống");
    }

    @GetMapping("/summary")
    public ResponseEntity<CartResponse> getCartSummary(@RequestParam Long userId) {
        CartResponse response = cartService.getCartSummary(userId);
        return ResponseEntity.ok(response);
    }

    private CartItemResponse mapToResponse(CartItem item) {
        var product = productService.getSellerProductById(item.getSellerProductId());

        return CartItemResponse.builder()
                .id(item.getId())
                .sellerProductId(item.getSellerProductId())
                .quantity(item.getQuantity())
                .productName(product.getName())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .build();
    }
}