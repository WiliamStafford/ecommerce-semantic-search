package com.ecommerce.payment.port;

import com.ecommerce.payment.domain.PaymentSession;
import com.ecommerce.payment.dto.request.PaymentRequest;
import com.ecommerce.payment.dto.request.PaymentResponse;
import com.ecommerce.payment.enums.PaymentProvider;
import java.util.Map;

public interface PaymentGatewayStrategy {

    PaymentProvider getProvider();

    String createPaymentUrl(PaymentSession session);

    boolean verifySignature(Map<String, String> fields);

    PaymentResponse createPayment(PaymentRequest request);

    boolean verifyCallback(Map<String, String> queryParams);
}