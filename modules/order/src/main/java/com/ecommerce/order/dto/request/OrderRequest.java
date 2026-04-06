package com.ecommerce.order.dto.request;

import com.ecommerce.order.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
public record OrderRequest(
        @NotNull(message = "ID người mua không được để trống")
        Long userId,

        @NotNull(message = "ID người bán không được để trống")
        Long sellerId,

        @NotBlank(message = "Địa chỉ giao hàng không được để trống")
        String shippingAddress,

        @NotNull(message = "Phương thức thanh toán không được để trống")
        PaymentMethod paymentMethod,

        @NotEmpty(message = "Đơn hàng phải có ít nhất một sản phẩm")
        List<OrderItemRequest> items,

        Double totalPrice
) {
}