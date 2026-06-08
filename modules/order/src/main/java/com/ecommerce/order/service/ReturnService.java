package com.ecommerce.order.service;

import com.ecommerce.order.domain.ReturnRequest;
import com.ecommerce.order.dto.response.ReturnCombinedDTO;
import com.ecommerce.order.enums.ReturnStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ReturnService {

    @Transactional(readOnly = true)
    List<ReturnCombinedDTO> getAllRequestsWithProof();

    @Transactional(readOnly = true)
    List<ReturnCombinedDTO> getRequestsWithProofByCustomerId(Long userId);

    List<ReturnRequest> getAllRequests();

    @Transactional(readOnly = true)
    List<ReturnCombinedDTO> getRequestsWithProofBySellerId(Long sellerId);


    @Transactional
    ReturnRequest createReturnRequest(Long userId, Long orderItemId, String reason, String evidence,
                                      String refundMethod, String bankName, String bankAccountNumber,
                                      String bankAccountName, String paypalEmail);

    void approveAndRefund(Long requestId);

    void rejectRequest(Long requestId, String adminNote);

    void updateRequestStatus(Long requestId, Long sellerId, ReturnStatus newStatus, String note);

    void updateRefundProofUrl(Long requestId, String imageUrl);


    List<ReturnRequest> getRequestsByCustomerId(Long userId);

    List<ReturnRequest> getRequestsBySellerId(Long sellerId);
}