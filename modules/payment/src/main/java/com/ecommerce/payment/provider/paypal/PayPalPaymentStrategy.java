package com.ecommerce.payment.provider.paypal;


import com.ecommerce.payment.config.PaymentProperties;
import com.ecommerce.payment.domain.PaymentSession;
import com.ecommerce.payment.dto.request.PaymentRequest;
import com.ecommerce.payment.dto.request.PaymentResponse;
import com.ecommerce.payment.enums.PaymentProvider;
import com.ecommerce.payment.port.PaymentGatewayStrategy;
import com.ecommerce.payment.provider.dto.PayPalCaptureResponse;
import com.ecommerce.payment.provider.dto.PayPalOrderResponse;
import com.ecommerce.payment.provider.dto.PayPalTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
@Component
@RequiredArgsConstructor
@Slf4j
public class PayPalPaymentStrategy implements PaymentGatewayStrategy {

    private final PaymentProperties paymentProperties;
    private final RestTemplate restTemplate;

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.PAYPAL;
    }

    @Override
    public String createPaymentUrl(PaymentSession session) {
        BigDecimal rate = paymentProperties.getExchangeRate().getVndToUsd();
        BigDecimal usdAmount = session.getTotalAmount().getAmount().divide(rate, 2, RoundingMode.HALF_UP);

        String accessToken = getAccessToken();
        var conf = paymentProperties.getPaypal();

        Map<String, Object> orderBody = getStringObjectMap(session, conf, usdAmount);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(orderBody, headers);

        try {
            ResponseEntity<PayPalOrderResponse> response = restTemplate.postForEntity(
                conf.getBaseUrl() + "/v2/checkout/orders",
                entity,
                PayPalOrderResponse.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
                return response.getBody().links().stream()
                    .filter(link -> "approve".equals(link.rel()))
                    .map(PayPalOrderResponse.PayPalLink::href)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("PayPal approve link not found"));
            }
        } catch (Exception e) {
            log.error("PayPal Order Creation Failed: ", e);
        }
        throw new RuntimeException("Lỗi khởi tạo PayPal");
    }

    private static Map<String, Object> getStringObjectMap(PaymentSession session, PaymentProperties.PayPalConfig conf, BigDecimal usdAmount) {
        String returnUrlWithSession = conf.getReturnUrl() + "?sessionId=" + session.getId();

        Map<String, Object> orderBody = Map.of(
            "intent", "CAPTURE",
            "purchase_units", List.of(Map.of(
                "reference_id", session.getId().toString(),
                "amount", Map.of(
                    "currency_code", "USD",
                    "value", usdAmount.toString()
                )
            )),
            "application_context", Map.of(
                "return_url", returnUrlWithSession,
                "cancel_url", conf.getCancelUrl(),
                "brand_name", "Shoppe clone",
                "user_action", "PAY_NOW"
            )
        );
        return orderBody;
    }

    @Override
    public boolean verifyCallback(Map<String, String> queryParams) {
        String paypalOrderId = queryParams.get("token");
        if (paypalOrderId == null) {
            log.warn("PayPal callback missing token");
            return false;
        }

        try {
            return captureOrder(paypalOrderId);
        } catch (Exception e) {
            log.error("PayPal Capture Error for Order {}: ", paypalOrderId, e);
            return false;
        }
    }

    private boolean captureOrder(String paypalOrderId) {
        String accessToken = getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>("{}", headers);

        try {
            ResponseEntity<PayPalCaptureResponse> response = restTemplate.postForEntity(
                paymentProperties.getPaypal().getBaseUrl() + "/v2/checkout/orders/" + paypalOrderId + "/capture",
                entity,
                PayPalCaptureResponse.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                String status = Objects.requireNonNull(response.getBody()).status();
                log.info("PayPal Capture Success. Status: {}", status);
                return "COMPLETED".equals(status);
            }
        } catch (Exception e) {
            log.error("PayPal Capture failed. Request: {}/v2/checkout/orders/{}/capture",
                paymentProperties.getPaypal().getBaseUrl(), paypalOrderId, e);
        }
        return false;
    }

    private String getAccessToken() {
        var conf = paymentProperties.getPaypal();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(conf.getClientId(), conf.getClientSecret());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<PayPalTokenResponse> response = restTemplate.postForEntity(
            conf.getBaseUrl() + "/v1/oauth2/token",
            request,
            PayPalTokenResponse.class
        );

        return Objects.requireNonNull(response.getBody()).access_token();
    }

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        return PaymentResponse.builder().message("Use createPaymentUrl").build();
    }

    @Override
    public boolean verifySignature(Map<String, String> fields) {

        return true;
    }
}
