package com.ecommerce.product.dto.response;

import lombok.Builder;
import lombok.Data;

public record ProductHomeResponse(
        Long sellerProductId,
        String productName,
        Double price,
        String avatar,
        String origin,
        Double averageRating,
        boolean isFavorite
) {}