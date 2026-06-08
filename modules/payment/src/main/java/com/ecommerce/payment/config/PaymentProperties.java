package com.ecommerce.payment.config;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "payment")
@Getter
@Setter
public class PaymentProperties {

    private ExchangeRate exchangeRate;
    private MomoConfig momo;
    private VnPayConfig vnpay;
    private ZaloPayConfig zalopay;
    private PayPalConfig paypal;

    @Getter @Setter
    public static class ExchangeRate {
        private BigDecimal vndToUsd;
    }

    @Getter @Setter
    public static class MomoConfig {
        private String partnerCode;
        private String accessKey;
        private String secretKey;
        private String endpoint;
        private String callbackUrl;
        private String returnUrl;
    }

    @Getter @Setter
    public static class VnPayConfig {
        private String tmnCode;
        private String hashSecret;
        private String url;
        private String ipnUrl;
        private String returnUrl;
    }

    @Getter @Setter
    public static class ZaloPayConfig {
        private String appId;
        private String key1;
        private String key2;
        private String endpoint;
        private String callbackUrl;
        private String redirectUrl;
    }

    @Getter @Setter
    public static class PayPalConfig {
        private String clientId;
        private String clientSecret;
        private String mode;
        private String baseUrl;
        private String returnUrl;
        private String cancelUrl;
    }
}
