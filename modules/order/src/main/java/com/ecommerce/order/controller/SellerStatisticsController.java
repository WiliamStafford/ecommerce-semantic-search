package com.ecommerce.order.controller;

import com.ecommerce.common.security.UserPrincipal;
import com.ecommerce.order.service.OrderReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/v1/seller/stats")
@RequiredArgsConstructor
public class SellerStatisticsController {
    private final OrderReportService reportService;

    @GetMapping("/revenue/year")
    public ResponseEntity<?> getRevenue(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(reportService.getRevenueByYearForSeller(user.getId()));
    }
}
