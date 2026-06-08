package com.ecommerce.payment.provider.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record PayPalOrderRequest(
    String intent,
    List<PurchaseUnit> purchase_units
) {
    @Builder
    public record PurchaseUnit(Amount amount) {}

    @Builder
    public record Amount(String currency_code, String value) {}
}
