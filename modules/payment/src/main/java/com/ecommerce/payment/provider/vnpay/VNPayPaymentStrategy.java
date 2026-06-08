package com.ecommerce.payment.provider.vnpay;

import com.ecommerce.payment.config.PaymentProperties;
import com.ecommerce.payment.domain.PaymentSession;
import com.ecommerce.payment.dto.request.PaymentRequest;
import com.ecommerce.payment.dto.request.PaymentResponse;
import com.ecommerce.payment.enums.PaymentProvider;
import com.ecommerce.payment.port.PaymentGatewayStrategy;
import com.ecommerce.payment.util.SignatureUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class VNPayPaymentStrategy implements PaymentGatewayStrategy {
    private final PaymentProperties paymentProperties;

    @Override
    public PaymentProvider getProvider() { return PaymentProvider.VNPAY; }

    @Override
    public String createPaymentUrl(PaymentSession session) {
        PaymentRequest request = PaymentRequest.builder()
                .orderId(session.getId().toString())
                .amount(session.getAmount())
                .orderInfo("Thanh toan don hang " + session.getOrderId())
                .build();
        return createPayment(request).getPaymentUrl();
    }

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        var vnpConf = paymentProperties.getVnpay();

        Map<String, String> vnp_Params = new TreeMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnpConf.getTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf((long) (request.getAmount() * 100)));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", request.getOrderId());
        vnp_Params.put("vnp_OrderInfo", request.getOrderInfo());
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnpConf.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");
        vnp_Params.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        vnp_Params.forEach((key, value) -> {
            if (value != null && !value.isEmpty()) {
                try {
                    String fieldName = URLEncoder.encode(key, StandardCharsets.UTF_8.toString());
                    String fieldValue = URLEncoder.encode(value, StandardCharsets.UTF_8.toString());

                    hashData.append(fieldName).append('=').append(fieldValue).append('&');
                    query.append(fieldName).append('=').append(fieldValue).append('&');
                } catch (Exception e) {
                    log.error("VNPAY Encoding error", e);
                }
            }
        });

        hashData.deleteCharAt(hashData.length() - 1);
        query.deleteCharAt(query.length() - 1);

        String vnp_SecureHash = null;
        try {
            vnp_SecureHash = SignatureUtils.hmacSha512(hashData.toString(), vnpConf.getHashSecret());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String paymentUrl = vnpConf.getUrl() + "?" + query.toString() + "&vnp_SecureHash=" + vnp_SecureHash;

        return PaymentResponse.builder()
                .paymentUrl(paymentUrl)
                .message("Khởi tạo thanh toán thành công")
                .build();
    }

    @Override
    public boolean verifyCallback(Map<String, String> queryParams) {
        return verifySignature(queryParams);
    }

    @Override
    public boolean verifySignature(Map<String, String> fields) {
        String vnp_SecureHash = fields.get("vnp_SecureHash");
        if (vnp_SecureHash == null || vnp_SecureHash.isEmpty()) return false;

        // Sắp xếp tham số để tính toán lại chữ ký
        Map<String, String> signFields = new TreeMap<>(fields);
        signFields.remove("vnp_SecureHash");
        signFields.remove("vnp_SecureHashType");

        StringBuilder hashData = new StringBuilder();
        signFields.forEach((key, value) -> {
            if (value != null && !value.isEmpty()) {
                try {
                    // QUAN TRỌNG: Phải encode lại vì Spring đã tự động decode Map queryParams rồi
                    String fieldName = URLEncoder.encode(key, StandardCharsets.UTF_8.toString());
                    String fieldValue = URLEncoder.encode(value, StandardCharsets.UTF_8.toString());

                    hashData.append(fieldName).append('=').append(fieldValue).append('&');
                } catch (Exception e) {
                    log.error("VNPAY Verify encoding error", e);
                }
            }
        });

        if (hashData.length() > 0) {
            hashData.deleteCharAt(hashData.length() - 1);
        }

        String checkHash = null;
        try {
            checkHash = SignatureUtils.hmacSha512(hashData.toString(), paymentProperties.getVnpay().getHashSecret());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info("VNPAY Calculated Hash: [{}]", checkHash);
        log.info("VNPAY Callback Hash: [{}]", vnp_SecureHash);

        return checkHash.equalsIgnoreCase(vnp_SecureHash);
    }
}