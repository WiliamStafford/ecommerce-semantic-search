package com.ecommerce.payment.provider.momo;

import com.ecommerce.payment.config.PaymentProperties;
import com.ecommerce.payment.domain.PaymentSession;
import com.ecommerce.payment.dto.request.PaymentRequest;
import com.ecommerce.payment.dto.request.PaymentResponse;
import com.ecommerce.payment.enums.PaymentProvider;
import com.ecommerce.payment.port.PaymentGatewayStrategy;
import com.ecommerce.payment.provider.dto.MomoApiRequest;
import com.ecommerce.payment.util.SignatureUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MomoPaymentStrategy implements PaymentGatewayStrategy {

    private final PaymentProperties paymentProperties;
    private final RestTemplate restTemplate;

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.MOMO;
    }

    @Override
    public String createPaymentUrl(PaymentSession session) {
        long amountValue = session.getAmount();
        String sessionIdStr = String.valueOf(session.getId());

        PaymentRequest request = PaymentRequest.builder()
                .orderId(sessionIdStr + "_" + System.currentTimeMillis())
                .amount(amountValue)
                .orderInfo("ThanhToanShopee_Session_" + sessionIdStr)
                .requestId(sessionIdStr)
                .build();

        PaymentResponse response = this.createPayment(request);
        return response.getPaymentUrl();
    }
    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        var momoConf = paymentProperties.getMomo();
        String partnerCode = momoConf.getPartnerCode();
        String accessKey = momoConf.getAccessKey();
        String secretKey = momoConf.getSecretKey();

        String requestId = request.getRequestId();
        String orderId = request.getOrderId();
        String amount = String.valueOf(request.getAmount());
        String orderInfo = request.getOrderInfo();
        String callbackUrl = momoConf.getCallbackUrl().trim();
        String returnUrl = momoConf.getReturnUrl().trim();
        String extraData = ""; // Phải là chuỗi rỗng, không được null
        String requestType = "captureWallet";

        // Tạo chuỗi Raw Signature (giữ nguyên thứ tự Alphabet này)
        String rawSignature = "accessKey=" + accessKey +
                              "&amount=" + amount +
                              "&extraData=" + extraData +
                              "&ipnUrl=" + callbackUrl +
                              "&orderId=" + orderId +
                              "&orderInfo=" + orderInfo +
                              "&partnerCode=" + partnerCode +
                              "&redirectUrl=" + returnUrl +
                              "&requestId=" + requestId +
                              "&requestType=" + requestType;

        log.info("--- CHUỖI DỮ LIỆU GỐC ĐỂ BĂM: {} ---", rawSignature);

        try {
            String signature = SignatureUtils.hmacSha256(rawSignature, secretKey).toLowerCase();
            MomoApiRequest apiRequest = MomoApiRequest.builder()
                    .partnerCode(partnerCode)
                    .requestId(requestId)
                    .amount(request.getAmount())
                    .orderId(orderId)
                    .orderInfo(orderInfo)
                    .redirectUrl(returnUrl)
                    .ipnUrl(callbackUrl)
                    .requestType(requestType)
                    .extraData(extraData)
                    .signature(signature)
                    .lang("vi")
                    .build();

            log.info("Sending to MoMo with Signature: {}", signature);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    momoConf.getEndpoint(),
                    HttpMethod.POST,
                    new HttpEntity<>(apiRequest),
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> body = response.getBody();
            if (body != null && "0".equals(String.valueOf(body.get("resultCode")))) {
                return PaymentResponse.builder()
                        .paymentUrl(body.get("payUrl").toString())
                        .message("Thành công")
                        .build();
            }

            log.error("MoMo API Error Response: {}", body);
            return PaymentResponse.builder()
                    .message(body != null ? body.get("message").toString() : "Lỗi MoMo")
                    .build();

        } catch (Exception e) {
            log.error("Momo Strategy Exception: ", e);
            throw new RuntimeException("Lỗi khởi tạo MoMo: " + e.getMessage());
        }
    }

    @Override
    public boolean verifyCallback(Map<String, String> queryParams) {
        return true;
    }

    @Override
    public boolean verifySignature(Map<String, String> fields) {
        return true;
    }
}