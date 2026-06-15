package com.ecommerce.order.controller;

import com.ecommerce.order.dto.response.StatResponse;
import com.ecommerce.order.dto.response.TopProductResponse;
import com.ecommerce.order.service.OrderReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/stats")
@RequiredArgsConstructor
public class AdminStatisticsController {
    private final OrderReportService reportService;


}