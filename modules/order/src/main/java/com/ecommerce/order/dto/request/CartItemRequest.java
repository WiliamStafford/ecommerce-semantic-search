package com.ecommerce.order.dto.request;

import lombok.Data;

@Data
public class CartItemRequest {
    private Long sellerProductId;
    private Integer quantity;
}