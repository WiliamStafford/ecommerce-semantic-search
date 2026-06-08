package com.ecommerce.order.domain;

import com.ecommerce.order.enums.ReturnStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "resolve_returns")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết với yêu cầu gốc
    private Long returnRequestId;

    // Thông tin Seller
    private Long sellerId;
    private String sellerName;

    // Thông tin Khách hàng
    private Long customerId;
    private String customerName;

    // Ảnh minh chứng (Kết hợp cả 2 nguồn)
    private String customerEvidenceUrl; // Ảnh khách gửi lúc khiếu nại
    private String sellerRefundProofUrl; // Ảnh thanh toán hoàn tiền của Seller

    // Thông tin đơn hàng
    private Long orderId;
    private Long orderItemId;
    private Double refundAmount;

    // Ghi chú và Trạng thái
    @Column(columnDefinition = "TEXT")
    private String resolutionNote; // Ghi chú của Seller khi duyệt

    private LocalDateTime resolvedAt;

    @Enumerated(EnumType.STRING)
    private ReturnStatus finalStatus;
}