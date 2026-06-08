package com.ecommerce.payment.service;

import com.ecommerce.payment.domain.Money;
import com.ecommerce.payment.domain.PaymentSession;
import com.ecommerce.payment.domain.PaymentTransaction;
import com.ecommerce.payment.enums.PaymentProvider;
import com.ecommerce.payment.enums.PaymentSessionStatus;
import com.ecommerce.payment.enums.PaymentTransactionStatus;
import com.ecommerce.payment.enums.PaymentTransactionType;
import com.ecommerce.payment.port.PaymentGatewayStrategy;
import com.ecommerce.payment.repository.PaymentSessionRepository;
import com.ecommerce.payment.dto.response.PaymentHistoryProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentApplicationService {

    private final PaymentSessionRepository sessionRepository;
    private final PaymentGatewayFactory gatewayFactory;

    @Transactional
    public String initializePayment(Long userId, Long orderId, Long amount, PaymentProvider provider) {
        log.info("Khởi tạo thanh toán cho User: {}, Order: {}", userId, orderId);

        PaymentSession session = new PaymentSession(
                orderId,
                new Money(BigDecimal.valueOf(amount), "VND"),
                UUID.randomUUID().toString()
        );

        session.startProcessing();
        sessionRepository.save(session);

        PaymentGatewayStrategy strategy = gatewayFactory.get(provider);
        return strategy.createPaymentUrl(session);
    }

    @Transactional
    public void processPaymentResult(Map<String, String> params, PaymentProvider provider) {
        Long sessionId = extractSessionId(params, provider);
        log.info("Xử lý kết quả thanh toán cho {} - Session ID: {}", provider, sessionId);

        PaymentSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Session: " + sessionId));

        PaymentGatewayStrategy strategy = gatewayFactory.get(provider);
        boolean isValid = strategy.verifyCallback(params);

        if (isValid) {
            String gatewayTxnId = provider == PaymentProvider.VNPAY ? params.get("vnp_TransactionNo") : params.get("token");
            String providerRef = provider == PaymentProvider.VNPAY ? params.get("vnp_BankCode") : params.get("PayerID");

            PaymentTransaction transaction = PaymentTransaction.builder()
                    .session(session)
                    .provider(provider)
                    .txnReference(gatewayTxnId)
                    .providerReference(providerRef)
                    .amount(session.getTotalAmount())
                    .status(PaymentTransactionStatus.SUCCESS)
                    .type(PaymentTransactionType.CHARGE)
                    .createdAt(LocalDateTime.now())
                    .build();

            session.addTransaction(transaction);
            sessionRepository.save(session);

            log.info("Thanh toán thành công. Session {} đã chuyển sang COMPLETED.", sessionId);
        } else {
            session.fail();
            sessionRepository.save(session);
            log.error("Xác thực giao dịch thất bại cho Session: {}", sessionId);
        }
    }

    private Long extractSessionId(Map<String, String> params, PaymentProvider provider) {
        try {
            String rawId = switch (provider) {
                case MOMO -> params.get("orderId");
                case VNPAY -> params.get("vnp_TxnRef");
                case PAYPAL -> params.get("sessionId");
                case ZALOPAY -> {
                    String appTransId = params.get("app_trans_id");
                    yield appTransId.substring(appTransId.indexOf("_") + 1);
                }
                default -> throw new RuntimeException("Provider not supported");
            };

            return Long.valueOf(rawId);
        } catch (Exception e) {
            log.error("Lỗi trích xuất Session ID từ {}: {}", provider, e.getMessage());
            throw new RuntimeException("Lỗi trích xuất Session ID từ " + provider);
        }
    }

    @Transactional(readOnly = true)
    public List<PaymentHistoryProjection> getPaymentsByUserId(Long userId) {
        log.info(">>>> [SERVICE] Đang tải lịch sử giao dịch trực tuyến cho User ID: {}", userId);
        return sessionRepository.findPaymentHistoryByUserId(userId);
    }
}