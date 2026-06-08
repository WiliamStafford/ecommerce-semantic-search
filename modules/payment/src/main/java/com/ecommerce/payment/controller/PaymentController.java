package com.ecommerce.payment.controller;


import com.ecommerce.common.security.CurrentUserProvider;
import com.ecommerce.payment.dto.command.AddPaymentMethodCommand;
import com.ecommerce.payment.dto.request.PaymentInitRequest;
import com.ecommerce.payment.dto.response.PaymentHistoryProjection;
import com.ecommerce.payment.enums.PaymentProvider;
import com.ecommerce.payment.service.PaymentApplicationService;
import com.ecommerce.payment.service.PaymentMethodManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentMethodManagementService paymentMethodManagementService;
    private final PaymentApplicationService paymentService;
    private final CurrentUserProvider currentUserProvider;
    private final PaymentApplicationService paymentApplicationService;

    @PostMapping("/methods")
    public ResponseEntity<Void> addPaymentMethod(@RequestBody @Valid AddPaymentMethodCommand command) {
        Long userId = currentUserProvider.getCurrentUserId();
        paymentMethodManagementService.addStoredPaymentMethod(userId, command);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/methods/{paymentMethodId}")
    public ResponseEntity<Void> deletePaymentMethod(@PathVariable Long paymentMethodId) {
        Long userId = currentUserProvider.getCurrentUserId();
        paymentMethodManagementService.deleteStoredPaymentMethod(userId, paymentMethodId);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/init")
    public ResponseEntity<Map<String, String>> initializePayment(@RequestBody @Valid PaymentInitRequest request) {

        Long userId = currentUserProvider.getCurrentUserId();

        log.info("Khởi tạo thanh toán cho User: {} - Order: {}", userId, request.orderId());

        String paymentUrl = paymentService.initializePayment(
            userId,
            request.orderId(),
            request.amount(),
            request.provider()
        );

        return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));
    }

    @PostMapping("/webhook/momo")
    public ResponseEntity<Void> handleMomoWebhook(@RequestBody Map<String, String> payload) {
        log.info("Momo Webhook IPN received: {}", payload);
        paymentService.processPaymentResult(payload, PaymentProvider.MOMO);
        return ResponseEntity.noContent().build();
    }


    @GetMapping(value = "/callback/paypal", produces = org.springframework.http.MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> handlePayPalReturn(@RequestParam Map<String, String> allParams) {
        log.info("PayPal Return received with params: {}", allParams);

        try {
            paymentService.processPaymentResult(allParams, PaymentProvider.PAYPAL);
            return ResponseEntity.ok(getSuccessHtml("PayPal"));

        } catch (Exception e) {
            log.error("PayPal processing error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("<html><body><h1>Lỗi xử lý giao dịch PayPal!</h1></body></html>");
        }
    }

    @GetMapping(value = "/callback/vnpay", produces = org.springframework.http.MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> handleVNPayReturn(@RequestParam Map<String, String> allParams) {
        log.info("VNPAY Return received with params: {}", allParams);

        try {
            paymentService.processPaymentResult(allParams, PaymentProvider.VNPAY);

            return ResponseEntity.ok(getSuccessHtml("VNPAY"));

        } catch (Exception e) {
            log.error("VNPAY processing error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("<html><body><h1>Lỗi xử lý giao dịch VNPAY!</h1></body></html>");
        }
    }


    @GetMapping("/webhook/vnpay")
    public ResponseEntity<Map<String, String>> handleVNPayIPN(@RequestParam Map<String, String> allParams) {
        log.info("VNPAY IPN received: {}", allParams);
        try {
            paymentService.processPaymentResult(allParams, PaymentProvider.VNPAY);
            return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Confirm Success"));
        } catch (Exception e) {
            log.error("VNPAY IPN error: ", e);
            return ResponseEntity.ok(Map.of("RspCode", "99", "Message", "Unknow error"));
        }
    }

    private String getSuccessHtml(String provider) {
        return "<html><body style='text-align:center; font-family:sans-serif; padding-top: 50px;'>" +
               "<h1 style='color: #28a745;'>Thanh toán " + provider + " thành công!</h1>" +
               "<p>Đơn hàng của bạn đang được xử lý. Bạn có thể đóng cửa sổ này.</p>" +
               "</body></html>";
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentHistoryProjection>> getPaymentHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentApplicationService.getPaymentsByUserId(userId));
    }
}
