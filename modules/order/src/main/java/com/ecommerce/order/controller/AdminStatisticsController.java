package com.ecommerce.order.controller;

import com.ecommerce.order.dto.response.StatResponse;
import com.ecommerce.order.dto.response.TopProductResponse;
import com.ecommerce.order.dto.response.TopProductResponseAdmin;
import com.ecommerce.order.service.OrderReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/reports") // Đổi tiền tố tại đây
@RequiredArgsConstructor
public class AdminStatisticsController {

    private final OrderReportService orderReportService;

    // Link: /api/v1/admin/reports/{sellerId}/stats/period
    @GetMapping("/{sellerId}/stats/period")
    public ResponseEntity<List<StatResponse>> getStatsForAdmin(
            @PathVariable Long sellerId,
            @RequestParam String periodType) {

        return ResponseEntity.ok(orderReportService.getStatsByPeriod(sellerId, periodType.toUpperCase()));
    }
    @GetMapping("/revenue")
    public ResponseEntity<List<Map<String, Object>>> getRevenueBySeller() {
        return ResponseEntity.ok(orderReportService.calculateRevenueBySeller());
    }

    // Link: /api/v1/admin/reports/{sellerId}/stats/products/top
    @GetMapping("/{sellerId}/stats/products/top")
    public ResponseEntity<List<TopProductResponseAdmin>> getTopProductsForAdmin(
            @PathVariable Long sellerId) {

        return ResponseEntity.ok(orderReportService.getTopProductsForAdmin(sellerId));
    }
}