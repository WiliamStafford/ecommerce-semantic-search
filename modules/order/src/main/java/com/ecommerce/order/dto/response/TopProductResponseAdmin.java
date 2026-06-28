package com.ecommerce.order.dto.response;

public record TopProductResponseAdmin(
        Long productId,
        String productName,
        Integer totalSold,
        Double totalRevenue
) {}