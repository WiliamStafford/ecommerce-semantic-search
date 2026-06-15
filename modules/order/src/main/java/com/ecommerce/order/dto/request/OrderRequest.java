package com.ecommerce.order.dto.request;

import com.ecommerce.order.enums.PaymentMethod;
import java.util.List;

import com.ecommerce.order.enums.PaymentMethod;
import java.util.List;

public record OrderRequest(
        Long userId,
        Long sellerId,
        Long addressId,
        PaymentMethod paymentMethod,
        List<OrderItemRequest> items,
        Double totalPrice,
        AddressRequest newAddress
) {
    public boolean hasNewAddress() {
        return newAddress != null;
    }
}