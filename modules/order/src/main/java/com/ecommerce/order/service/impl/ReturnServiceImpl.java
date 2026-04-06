package com.ecommerce.order.service.impl;

import com.ecommerce.order.domain.ReturnRequest;
import com.ecommerce.order.domain.ReturnRefund; // Thêm import
import com.ecommerce.order.domain.OrderItem;
import com.ecommerce.order.enums.ReturnStatus;
import com.ecommerce.order.enums.RefundStatus; // Thêm import
import com.ecommerce.order.repository.OrderItemRepository;
import com.ecommerce.order.repository.ReturnRequestRepository;
import com.ecommerce.order.repository.ReturnRefundRepository; // Thêm import
import com.ecommerce.order.service.ReturnService;
import com.ecommerce.user.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReturnServiceImpl implements ReturnService {

    private final ReturnRequestRepository returnRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReturnRefundRepository refundRepository;
    private final WalletService walletService;

    @Override
    @Transactional
    public ReturnRequest createReturnRequest(Long userId, Long orderItemId, String reason, String evidence) {
        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món hàng trong đơn hàng"));

        if (returnRepository.existsByOrderItemId(orderItemId)) {
            throw new RuntimeException("Sản phẩm này đã được gửi yêu cầu khiếu nại trước đó!");
        }

        ReturnRequest request = ReturnRequest.builder()
                .orderItemId(orderItemId)
                .customerId(userId)
                .sellerId(item.getSellerId())
                .returnReason(reason)
                .evidenceImageUrls(evidence)
                .status(ReturnStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return returnRepository.save(request);
    }

    @Override
    @Transactional
    public void approveAndRefund(Long requestId) {
        ReturnRequest request = returnRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Yêu cầu khiếu nại không tồn tại"));

        if (request.getStatus() != ReturnStatus.PENDING) {
            throw new RuntimeException("Yêu cầu này đã được xử lý rồi!");
        }

        OrderItem item = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new RuntimeException("Dữ liệu món hàng không khớp"));

        double refundAmount = item.getPrice() * item.getQuantity();

        walletService.changeBalance(item.getSellerId(), -refundAmount);
        walletService.changeBalance(request.getCustomerId(), refundAmount);

        ReturnRefund refund = ReturnRefund.builder()
                .returnRequestId(requestId)
                .refundAmount(refundAmount)
                .status(RefundStatus.COMPLETED)
                .requestDate(LocalDateTime.now())
                .build();
        refundRepository.save(refund);

        request.setStatus(ReturnStatus.APPROVED);
        request.setUpdatedAt(LocalDateTime.now());
        returnRepository.save(request);
    }

    @Override
    @Transactional
    public void rejectRequest(Long requestId, String adminNote) {
        ReturnRequest request = returnRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Yêu cầu không tồn tại"));

        request.setStatus(ReturnStatus.REJECTED);
        request.setNote(adminNote);
        request.setUpdatedAt(LocalDateTime.now());
        returnRepository.save(request);
    }

    @Override
    public List<ReturnRequest> getAllRequests() {
        return returnRepository.findAll();
    }

    @Override
    public List<ReturnRequest> getRequestsByCustomerId(Long userId) {
        return returnRepository.findAllByCustomerId(userId);
    }
}