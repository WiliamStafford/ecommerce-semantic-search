package com.ecommerce.order.controller;

import com.ecommerce.order.enums.OrderStatus;
import com.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/A_Order") // Admin Order
@RequiredArgsConstructor
public class A_OrderController {

    private final OrderService orderService;

    @GetMapping("/list")
    public ResponseEntity<?> getAllOrders(@RequestParam(required = false) OrderStatus status) {
        return ResponseEntity.ok(orderService.findAllByStatus(status));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        orderService.updateStatus(id, status);
        return ResponseEntity.ok("Cập nhật trạng thái đơn hàng thành công!");
    }
}