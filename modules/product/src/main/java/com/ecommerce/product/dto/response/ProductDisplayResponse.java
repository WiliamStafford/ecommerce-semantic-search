package com.ecommerce.product.dto.response;

public record ProductDisplayResponse(
        Long sellerProductId,
        String productName,
        Double price,
        String sellerName,
        String imageUrl
) {}