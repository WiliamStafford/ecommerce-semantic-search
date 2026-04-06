package com.ecommerce.order.controller;

import com.ecommerce.order.domain.ReturnRequest;
import com.ecommerce.order.dto.request.ReturnRequestDTO;
import com.ecommerce.order.service.ReturnService;
import com.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
@RestController
@RequestMapping("/api/v1/returns")
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnService returnService;
    private final UserService userService;

    @PostMapping("/request")
    public ResponseEntity<?> createReturnRequest(
            @RequestBody ReturnRequestDTO dto, // Đổi sang RequestBody
            java.security.Principal principal) {

        Long userIdFromToken = userService.findIdByEmail(principal.getName());

        var request = returnService.createReturnRequest(
                userIdFromToken,
                dto.orderItemId(),
                dto.reason(),
                dto.evidence()
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
}