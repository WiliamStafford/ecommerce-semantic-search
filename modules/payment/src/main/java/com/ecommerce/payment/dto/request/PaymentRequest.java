package com.ecommerce.payment.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentRequest {
    private String orderId;
    private Long amount;
    private String orderInfo;
    private String requestId;
}
