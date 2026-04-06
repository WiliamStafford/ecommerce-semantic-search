package com.ecommerce.order.service;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.dto.request.OrderRequest;
import com.ecommerce.order.enums.OrderStatus;
import org.springframework.transaction.annotation.Transactional;

public interface OrderService {
    Order createOrder(OrderRequest request, String email);

    @Transactional
    Order checkout(Long userId, String shippingAddress);

    Order getOrderById(Long id);

    void updateCartItemQuantity(Long userId, Long sellerProductId, int delta);

    void updateStatus(Long id, OrderStatus status);

    Order findAllByStatus(OrderStatus status);
}