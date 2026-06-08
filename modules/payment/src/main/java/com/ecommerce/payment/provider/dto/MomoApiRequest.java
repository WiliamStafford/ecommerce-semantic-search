package com.ecommerce.payment.provider.dto;

import lombok.Builder;

@Builder
public record MomoApiRequest(
        String partnerCode,
        String requestId,
        Long amount,
        String orderId,
        String orderInfo,
        String redirectUrl,
        String ipnUrl,
        String extraData,
        String requestType,
        String signature,
        String lang
) {}