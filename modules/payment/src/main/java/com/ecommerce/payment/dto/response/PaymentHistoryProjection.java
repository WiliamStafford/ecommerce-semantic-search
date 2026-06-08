package com.ecommerce.payment.dto.response;

import java.time.LocalDateTime;

public interface PaymentHistoryProjection {
    Long getSessionId();
    Long getOrderId();
    Double getAmount();
    String getProvider();
    String getStatus();
    LocalDateTime getCreatedAt();
    String getTransactionId();
    LocalDateTime getPaymentDate();
}