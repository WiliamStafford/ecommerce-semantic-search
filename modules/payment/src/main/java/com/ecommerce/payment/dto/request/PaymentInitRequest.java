package com.ecommerce.payment.dto.request;

import com.ecommerce.payment.enums.PaymentProvider;
import java.util.UUID;

public record PaymentInitRequest(
        Long orderId,
        Long amount,
        PaymentProvider provider
) {}