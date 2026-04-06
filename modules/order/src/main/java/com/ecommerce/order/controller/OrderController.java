package com.ecommerce.order.controller;

import com.ecommerce.order.dto.request.OrderRequest;
import com.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/buy-now")
    public ResponseEntity<?> buyNow(@RequestBody OrderRequest request, Principal principal) {
        return ResponseEntity.ok(orderService.createOrder(request, principal.getName()));
    }
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestParam Long userId, @RequestParam String address) {
        return ResponseEntity.ok(orderService.checkout(userId, address));
    }

    @PatchMapping("/cart/update")
    public ResponseEntity<?> updateCartQuantity(
            @RequestParam Long userId,
            @RequestParam Long sellerProductId,
            @RequestParam int delta) {
        orderService.updateCartItemQuantity(userId, sellerProductId, delta);
        return ResponseEntity.ok("Cập nhật giỏ hàng thành công");
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
}