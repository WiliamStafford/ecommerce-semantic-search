package com.ecommerce.order.service;

import com.ecommerce.order.dto.response.SellerDashboardSummaryResponse;
import com.ecommerce.order.dto.response.SellerOrderResponse;
import com.ecommerce.order.enums.OrderStatus;

import java.util.List;

public interface SellerOrderService {
    List<SellerOrderResponse> getOrdersBySeller(Long currentSellerId);

    boolean updateSellerOrderStatus(Long orderItemId, OrderStatus status);

    SellerDashboardSummaryResponse getSellerDashboardSummary(Long currentSellerId);
}
