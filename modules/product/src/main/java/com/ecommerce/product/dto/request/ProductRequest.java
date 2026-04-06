package com.ecommerce.product.dto.request;

import java.util.List;

public record ProductRequest(
        String productName,
        Long categoryId,
        String origin,
        Integer stock,
        String size,
        String description,
        List<Double> vector
) {}