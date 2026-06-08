package com.ecommerce.payment.port;

// ĐỔI TỪ: com.shopee.user.domain.entities.payment.PaymentSession
// THÀNH:
import com.ecommerce.payment.domain.PaymentSession;

public interface PaymentGatewayPort {
    boolean supports(com.ecommerce.payment.enums.PaymentProvider provider);

    String createPaymentUrl(PaymentSession session);

    boolean verifyNotification(java.util.Map<String, String> params);
}