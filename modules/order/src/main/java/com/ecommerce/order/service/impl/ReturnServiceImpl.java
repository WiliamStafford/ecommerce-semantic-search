package com.ecommerce.order.service.impl;

import com.ecommerce.order.domain.*;
import com.ecommerce.order.dto.response.ReturnCombinedDTO;
import com.ecommerce.order.enums.ReturnStatus;
import com.ecommerce.order.enums.RefundStatus;
import com.ecommerce.order.repository.*;
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
    private final OrderRepository orderRepository;
    private final ResolveReturnRepository resolveRepository;
    private final WalletService walletService;

    @Transactional
    @Override
    public ReturnRequest createReturnRequest(Long userId, Long orderItemId, String reason, String evidence,
                                             String refundMethod, String bankName, String bankAccountNumber,
                                             String bankAccountName, String paypalEmail) {

        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món hàng"));

        if (returnRepository.existsByOrderItemId(orderItemId)) {
            throw new RuntimeException("Sản phẩm đã được yêu cầu khiếu nại!");
        }

        Order order = orderRepository.findById(item.getOrderId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        ReturnRequest request = ReturnRequest.builder()
                .orderItemId(orderItemId)
                .customerId(userId)
                .sellerId(order.getSellerId())
                .returnReason(reason)
                .evidenceImageUrls(evidence)
                .status(ReturnStatus.PENDING)
                .refundMethod(refundMethod)
                .bankName(bankName)
                .bankAccountNumber(bankAccountNumber)
                .bankAccountName(bankAccountName)
                .paypalEmail(paypalEmail)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return returnRepository.save(request);
    }

    @Override
    @Transactional
    public void approveAndRefund(Long requestId) {
        ReturnRequest request = returnRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Khiếu nại không tồn tại"));

        OrderItem item = orderItemRepository.findById(request.getOrderItemId()).orElseThrow();
        Order order = orderRepository.findById(item.getOrderId()).orElseThrow();

        double refundAmount = item.getPrice() * item.getQuantity();

        ReturnRefund refund = ReturnRefund.builder()
                .returnRequestId(requestId)
                .refundAmount(refundAmount)
                .status(RefundStatus.PROCESSING)
                .build();

        refundRepository.save(refund);
        request.setStatus(ReturnStatus.APPROVED);
        returnRepository.save(request);
    }

    @Override
    @Transactional
    public void updateRefundProofUrl(Long requestId, String imageUrl) {
        ReturnRefund refund = refundRepository.findByReturnRequestId(requestId)
                .orElseGet(() -> {
                    ReturnRefund newRefund = ReturnRefund.builder()
                            .returnRequestId(requestId)
                            .refundAmount(0.0)
                            .status(RefundStatus.COMPLETED)
                            .build();
                    return refundRepository.save(newRefund);
                });

        refund.setRefundProofUrl(imageUrl);
        refund.setStatus(RefundStatus.COMPLETED);
        refundRepository.save(refund);

        resolveRepository.findByReturnRequestId(requestId).ifPresent(resolve -> {
            resolve.setSellerRefundProofUrl(imageUrl);
            resolveRepository.save(resolve);
        });
    }

    @Transactional
    @Override
    public void updateRequestStatus(Long requestId, Long sellerId, ReturnStatus newStatus, String note) {
        ReturnRequest request = returnRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Khiếu nại không tồn tại"));

        if (!request.getSellerId().equals(sellerId)) {
            throw new RuntimeException("Bạn không có quyền xử lý đơn này!");
        }

        request.setStatus(newStatus);
        request.setNote(note);
        request.setUpdatedAt(LocalDateTime.now());
        returnRepository.save(request);

        if (newStatus == ReturnStatus.APPROVED && !refundRepository.existsByReturnRequestId(requestId)) {
            approveAndRefund(requestId);
        }

        ResolveReturn record = resolveRepository.findByReturnRequestId(requestId)
                .orElse(ResolveReturn.builder().returnRequestId(requestId).build());

        record.setSellerId(sellerId);
        record.setCustomerId(request.getCustomerId());
        record.setCustomerEvidenceUrl(request.getEvidenceImageUrls());
        record.setResolutionNote(note);
        record.setResolvedAt(LocalDateTime.now());
        record.setFinalStatus(newStatus);
        resolveRepository.save(record);
    }

    @Override
    @Transactional
    public void rejectRequest(Long requestId, String note) {
        ReturnRequest request = returnRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Yêu cầu không tồn tại"));

        request.setStatus(ReturnStatus.REJECTED);
        request.setNote(note);
        request.setUpdatedAt(LocalDateTime.now());
        returnRepository.save(request);

        ResolveReturn record = resolveRepository.findByReturnRequestId(requestId)
                .orElse(ResolveReturn.builder().returnRequestId(requestId).build());
        record.setResolutionNote(note);
        record.setResolvedAt(LocalDateTime.now());
        record.setFinalStatus(ReturnStatus.REJECTED);
        resolveRepository.save(record);
    }

    // --- Các phương thức lấy danh sách đã đồng bộ ---

    @Override
    @Transactional(readOnly = true)
    public List<ReturnCombinedDTO> getAllRequestsWithProof() {
        return returnRepository.findAll().stream().map(this::mapToCombinedDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnCombinedDTO> getRequestsWithProofBySellerId(Long sellerId) {
        return returnRepository.findAllBySellerId(sellerId).stream().map(this::mapToCombinedDTO).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ReturnCombinedDTO> getRequestsWithProofByCustomerId(Long userId) {
        return returnRepository.findAllByCustomerId(userId).stream().map(this::mapToCombinedDTO).toList();
    }

    // Hàm chung để lấy ảnh hoàn tiền từ bảng refundRepository
    private ReturnCombinedDTO mapToCombinedDTO(ReturnRequest req) {
        String proofUrl = refundRepository.findByReturnRequestId(req.getId())
                .map(ReturnRefund::getRefundProofUrl)
                .orElse(null);

        return new ReturnCombinedDTO(
                req.getId(),
                req.getCustomerId(),
                req.getReturnReason(),
                req.getEvidenceImageUrls(),
                req.getStatus().name(),
                req.getNote(),
                proofUrl
        );
    }

    @Override
    public List<ReturnRequest> getAllRequests() { return returnRepository.findAll(); }

    @Override
    public List<ReturnRequest> getRequestsBySellerId(Long sellerId) {
        return returnRepository.findAllBySellerId(sellerId);
    }

    @Override
    public List<ReturnRequest> getRequestsByCustomerId(Long userId) {
        return returnRepository.findAllByCustomerId(userId);
    }
}