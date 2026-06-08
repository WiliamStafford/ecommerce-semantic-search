package com.ecommerce.order.dto.request;

public record ReturnRequestDTO(
        Long orderItemId,
        String reason,
        String description,
        String evidence,
        String refundMethod,
        String bankName,
        String bankAccountNumber,
        String bankAccountName,
        String paypalEmail,
        String sellerRefundProofUrl
) {
}