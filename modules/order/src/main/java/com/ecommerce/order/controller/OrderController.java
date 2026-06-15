package com.ecommerce.order.controller;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.dto.request.OrderRequest;
import com.ecommerce.order.enums.OrderStatus;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.user.domain.Address;
import com.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final UserService userService;
    private final OrderService orderService;

    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(Principal principal) {
        Long userId = userService.findIdByEmail(principal.getName());
        log.info(">>>> User {} yêu cầu xem danh sách đơn hàng", principal.getName());
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id, Principal principal) {
        log.info(">>>> User {} yêu cầu hủy đơn hàng #{}", principal.getName(), id);
        orderService.updateStatus(id, OrderStatus.CANCELLED);
        return ResponseEntity.ok("Hủy đơn hàng thành công!");
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        orderService.updateStatus(id, status);
        return ResponseEntity.ok("Cập nhật trạng thái thành công!");
    }


    @PostMapping("/buy_right_now")
    public ResponseEntity<?> buyRightNow(@RequestBody OrderRequest request, Principal principal) {
        log.info(">>>> User {} thực hiện đặt hàng nhanh (Buy Right Now)", principal.getName());
        Long userId = userService.findIdByEmail(principal.getName());
        return ResponseEntity.ok(orderService.createOrder(userId, request));
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestParam Long addressId, Principal principal) {
        Long userId = userService.findIdByEmail(principal.getName());
        return ResponseEntity.ok(orderService.checkout(userId, addressId));
    }

    @PostMapping("/buy-now")
    public ResponseEntity<?> buyNow(@RequestBody OrderRequest request, Principal principal) {
        Long userId = userService.findIdByEmail(principal.getName());
        List<Order> orders = orderService.processOrderRequest(userId, request);

        return ResponseEntity.ok(orders);
    }
}