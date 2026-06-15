package com.ecommerce.order.dto.response;

public record TopProductResponse(String productName, Long totalSold, Double totalRevenue) {}