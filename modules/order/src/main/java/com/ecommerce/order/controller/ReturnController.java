package com.ecommerce.order.controller;

import com.ecommerce.common.security.JwtCurrentUserProvider;
import com.ecommerce.order.domain.ReturnRequest;
import com.ecommerce.order.dto.request.ReturnRequestDTO;
import com.ecommerce.order.dto.response.ReturnCombinedDTO;
import com.ecommerce.order.enums.ReturnStatus;
import com.ecommerce.order.service.ReturnService;
import com.ecommerce.user.service.UserService;
import com.ecommerce.common.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/returns")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ReturnController {

    private final ReturnService returnService;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;
    private final JwtCurrentUserProvider currentUserProvider;
    private final com.ecommerce.order.repository.ResolveReturnRepository resolveRepository;

    @PostMapping("/upload-evidence")
    public ResponseEntity<?> uploadEvidence(@RequestParam("files") List<MultipartFile> files) {
        try {
            if (files == null || files.isEmpty()) {
                return ResponseEntity.badRequest().body("Vui lòng gửi kèm hình ảnh minh chứng!");
            }
            if (files.size() > 8) {
                return ResponseEntity.badRequest().body("Bạn chỉ được phép tải lên tối đa 8 ảnh minh chứng!");
            }

            List<String> uploadedUrls = new ArrayList<>();

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String imageUrl = cloudinaryService.uploadImage(file, "fruit_fresh/returns");
                    uploadedUrls.add(imageUrl);
                }
            }

            return ResponseEntity.ok(Map.of("urls", uploadedUrls));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi trong quá trình upload hàng loạt: " + e.getMessage());
        }
    }

    @PostMapping("/request")
    public ResponseEntity<?> createReturnRequest(
            @RequestBody ReturnRequestDTO dto,
            java.security.Principal principal) {

        Long userIdFromToken = userService.findIdByEmail(principal.getName());
        var request = returnService.createReturnRequest(
                userIdFromToken,
                dto.orderItemId(),
                dto.reason(),
                dto.evidence(),
                dto.refundMethod(),
                dto.bankName(),
                dto.bankAccountNumber(),
                dto.bankAccountName(),
                dto.paypalEmail()
        );

        return ResponseEntity.ok(request);
    }

    @GetMapping("/my-requests/{userId}")
    public ResponseEntity<?> getMyRequests(@PathVariable Long userId) {
        List<ReturnRequest> myRequests = returnService.getRequestsByCustomerId(userId);
        if (myRequests.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(myRequests);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllReturnRequests() {
        return ResponseEntity.ok(returnService.getAllRequests());
    }

    @PatchMapping("/{requestId}/approve")
    public ResponseEntity<String> approveReturn(@PathVariable Long requestId) {
        returnService.approveAndRefund(requestId);
        return ResponseEntity.ok("Đã duyệt khiếu nại và hoàn tiền thành công");
    }

    @PatchMapping("/{requestId}/reject")
    public ResponseEntity<String> rejectReturn(
            @PathVariable Long requestId,
            @RequestParam String note) {
        returnService.rejectRequest(requestId, note);
        return ResponseEntity.ok("Đã từ chối yêu cầu khiếu nại. Lý do: " + note);
    }

    @GetMapping
    public ResponseEntity<List<ReturnRequest>> getMyStoreReturns() {
        Long sellerId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(returnService.getRequestsBySellerId(sellerId));
    }

    @GetMapping("/with-proof")
    public ResponseEntity<List<ReturnCombinedDTO>> getMyStoreReturnsWithProof() {
        Long sellerId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(returnService.getRequestsWithProofBySellerId(sellerId));
    }


    @PutMapping("/{requestId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long requestId,
            @RequestBody Map<String, Object> payload) {
        try {
            log.info(">>>> [DEBUG] Nhận request PUT cho ID: {}, Payload: {}", requestId, payload);

            Long sellerId = currentUserProvider.getCurrentUserId();
            if (sellerId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token không hợp lệ!");

            String statusStr = payload.get("status").toString().toUpperCase();
            ReturnStatus status = ReturnStatus.valueOf(statusStr);
            String note = payload.getOrDefault("note", "").toString();

            log.info(">>>> [DEBUG] Đang gọi Service với: SellerID={}, Status={}, Note={}", sellerId, status, note);

            returnService.updateRequestStatus(requestId, sellerId, status, note);

            return ResponseEntity.ok(Map.of("message", "Cập nhật thành công!"));
        } catch (Exception e) {
            log.error(">>>> [LỖI 500 CHI TIẾT] Đã xảy ra lỗi tại Controller: ", e);
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @GetMapping("/resolve-history/{requestId}")
    public ResponseEntity<?> getResolveDetail(@PathVariable Long requestId) {
        return resolveRepository.findByReturnRequestId(requestId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{requestId}/upload-refund-proof")
    public ResponseEntity<?> uploadRefundProof(
            @PathVariable Long requestId,
            @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = cloudinaryService.uploadImage(file, "fruit_fresh/refund_proofs");
            returnService.updateRefundProofUrl(requestId, imageUrl);

            return ResponseEntity.ok(Map.of("url", imageUrl));
        } catch (Exception e) {
            log.error(">>>> [LỖI UPLOAD ẢNH]: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi upload: " + e.getMessage());
        }
    }


}