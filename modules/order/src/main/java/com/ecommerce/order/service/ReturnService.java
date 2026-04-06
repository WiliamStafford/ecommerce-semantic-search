package com.ecommerce.order.service;

import com.ecommerce.order.domain.ReturnRequest;
import java.util.List;

public interface ReturnService {
    ReturnRequest createReturnRequest(Long userId, Long orderItemId, String reason, String evidence);

    List<ReturnRequest> getAllRequests();

    void approveAndRefund(Long requestId);

    void rejectRequest(Long requestId, String adminNote);

    List<ReturnRequest> getRequestsByCustomerId(Long userId);
}