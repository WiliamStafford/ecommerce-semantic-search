package com.ecommerce.order.dto.response;
public record ReturnCombinedDTO(
        Long id,
        Long customerId,
        String returnReason,
        String evidenceImageUrls,
        String status,
        String note,
        String sellerRefundProofUrl
) {}
