package com.ecommerce.payment.provider.dto;

import java.util.List;

public record PayPalOrderResponse(
    String id,
    String status,
    List<PayPalLink> links
) {
    public record PayPalLink(
        String href,
        String rel,
        String method
    ) {}
}
