package com.ecommerce.product.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductSummaryProjection(
        Long id,
        @JsonProperty("productName")
        String productName,
        Long categoryId,
        String avatar,
        Double price,
        Float score
) {}