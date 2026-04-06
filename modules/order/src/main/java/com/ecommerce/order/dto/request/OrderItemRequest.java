package com.ecommerce.order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull(message = "ID sản phẩm của người bán không được để trống")
        Long sellerProductId,

        @Min(value = 1, message = "Số lượng phải ít nhất là 1")
        Integer quantity,

        @NotNull(message = "Giá tại thời điểm mua không được để trống")
        Double price
) {
}