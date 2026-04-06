package com.ecommerce.order.controller;

import com.ecommerce.order.service.ReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/returns")
@RequiredArgsConstructor
public class A_ReturnController {

    private final ReturnService returnService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllReturnRequests() {
        return ResponseEntity.ok(returnService.getAllRequests());
    }


    @PatchMapping("/{requestId}/approve")
    public ResponseEntity<String> approveReturn(@PathVariable Long requestId) {
        returnService.approveAndRefund(requestId);
        return ResponseEntity.ok("Đã duyệt khiếu nại và hoàn tiền");
    }


    @PatchMapping("/{requestId}/reject")
    public ResponseEntity<String> rejectReturn(
            @PathVariable Long requestId,
            @RequestParam String note) {
        returnService.rejectRequest(requestId, note);
        return ResponseEntity.ok("Đã từ chối yêu cầu khiếu nại.");
    }
}