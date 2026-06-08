package com.ecommerce.order.dto.response;

import com.ecommerce.order.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record SellerOrderResponse(
        Long orderId,
        String orderCode,
        Double totalPrice,
        String shippingAddress,
        OrderStatus orderStatus,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {}