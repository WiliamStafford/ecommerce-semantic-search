package com.ecommerce.order.controller;

import com.ecommerce.common.security.CurrentUserProvider;
import com.ecommerce.order.dto.response.SellerOrderResponse;
import com.ecommerce.order.enums.OrderStatus;
import com.ecommerce.order.service.SellerOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/seller/orders")
@RequiredArgsConstructor
public class SellerOrderController {

    private final SellerOrderService sellerOrderService;
    private final CurrentUserProvider currentUserProvider; //


    @GetMapping
    public ResponseEntity<List<SellerOrderResponse>> getSellerOrders() {
        log.info(">>>> [ORDER-CONTROLLER] Seller đang gọi API tải danh sách đơn hàng");

        Long currentSellerId = currentUserProvider.getCurrentUserId();
        if (currentSellerId == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(sellerOrderService.getOrdersBySeller(currentSellerId));
    }

    @PutMapping("/{orderItemId}/status")
    public ResponseEntity<Boolean> updateSellerOrderStatus(
            @PathVariable Long orderItemId,
            @RequestParam OrderStatus status
    ) {
        log.info(">>>> [ORDER-CONTROLLER] Cập nhật trạng thái mục đơn {} sang Enum: {}", orderItemId, status);

        boolean isUpdated = sellerOrderService.updateSellerOrderStatus(orderItemId, status);
        return ResponseEntity.ok(isUpdated);
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<com.ecommerce.order.dto.response.SellerDashboardSummaryResponse> getSellerDashboardSummary() {
        log.info(">>>> [ORDER-CONTROLLER] Nhận yêu cầu bốc dữ liệu Dashboard Summary");

        Long currentSellerId = currentUserProvider.getCurrentUserId();
        if (currentSellerId == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(sellerOrderService.getSellerDashboardSummary(currentSellerId));
    }
}