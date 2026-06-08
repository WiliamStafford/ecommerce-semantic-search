package com.ecommerce.payment.provider.dto;

import java.util.List;

public record PayPalCaptureResponse(
    String id,
    String status,
    List<PurchaseUnit> purchase_units
) {
    public record PurchaseUnit(
        String reference_id,
        Payments payments
    ) {}

    public record Payments(
        List<Capture> captures
    ) {}

    public record Capture(
        String id,
        String status
    ) {}
}
