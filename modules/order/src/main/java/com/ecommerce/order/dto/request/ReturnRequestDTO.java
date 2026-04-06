package com.ecommerce.order.dto.request;

public record ReturnRequestDTO(
        Long orderItemId,
        String reason,
        String description,
        String evidence
) {}