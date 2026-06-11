package com.ecommerce.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerRevenueDTO {
    private String sellerName;
    private Double totalRevenue;
    private Long totalOrders;
}