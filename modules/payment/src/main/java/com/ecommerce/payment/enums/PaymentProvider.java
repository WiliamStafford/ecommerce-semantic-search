package com.ecommerce.payment.enums;

import lombok.Getter;

@Getter
public enum PaymentProvider {

    MOMO("Momo", false, false, "VND"),
    VNPAY("VNPay", false, false, "VND"),
    ZALOPAY("ZaloPay", false, false, "VND"),
    PAYPAL("PayPal", true, true, "USD");

    private final String displayName;
    private final boolean supportsAuthorization;
    private final boolean international;
    private final String defaultCurrency;

    PaymentProvider(String displayName, boolean supportsAuthorization, boolean international, String defaultCurrency) {
        this.displayName = displayName;
        this.supportsAuthorization = supportsAuthorization;
        this.international = international;
        this.defaultCurrency = defaultCurrency;
    }

    public boolean isDomestic() {
        return !this.international;
    }

    /**
     * Kiểm tra xem Provider có hỗ trợ luồng "Giữ tiền" (Authorize) rồi mới "Thu tiền" (Capture) không.
     * Thường dùng cho các cổng quốc tế như PayPal/Stripe.
     */
    public boolean canSupportAuthFlow() {
        return this.supportsAuthorization;
    }
}
