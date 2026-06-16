package com.ecommerce.order.controller;

import com.ecommerce.common.security.CurrentUserProvider;
import com.ecommerce.common.security.UserPrincipal;
import com.ecommerce.order.dto.response.StatResponse;
import com.ecommerce.order.dto.response.TopProductResponse;
import com.ecommerce.order.service.OrderReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seller/stats")
@RequiredArgsConstructor
public class SellerStatisticsController {

    private final OrderReportService orderReportService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/stats/period")
    public ResponseEntity<List<StatResponse>> getStats(@RequestParam String periodType) {
        String type = periodType.toUpperCase();
        if (!type.equals("YEAR") && !type.equals("MONTH") && !type.equals("WEEK")) {
            return ResponseEntity.badRequest().build(); //
        }
        Long sellerId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(orderReportService.getStatsByPeriod(sellerId, type));
    }
    @GetMapping("/revenue/year")
    public ResponseEntity<List<StatResponse>> getRevenue() {
        Long sellerId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(orderReportService.getRevenueByYearForSeller(sellerId));
    }

    @GetMapping("/products/top")
    public ResponseEntity<List<TopProductResponse>> getTopProducts() {
        Long sellerId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(orderReportService.getTopProductsForSeller(sellerId));
    }
}
