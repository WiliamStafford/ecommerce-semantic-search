package com.ecommerce.order.dto.response;

import java.util.List;

public record SellerDashboardSummaryResponse(
        Double totalRevenue,
        Integer totalOrders,
        Integer lowStockProducts,
        List<com.ecommerce.product.domain.SellerProduct> productsList
) {}