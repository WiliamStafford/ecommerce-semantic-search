package com.ecommerce.order.service;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.dto.request.OrderRequest;
import com.ecommerce.order.enums.OrderStatus;
import com.ecommerce.user.dto.response.SellerRevenueDTO;
import org.springframework.transaction.annotation.Transactional;
import com.ecommerce.order.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {
    Order createOrder(Long userId, OrderRequest request);

    @Transactional
    Order checkout(Long userId, String shippingAddress);

    Order getOrderById(Long id);

    void updateCartItemQuantity(Long userId, Long sellerProductId, int delta);

    void updateStatus(Long id, OrderStatus status);

    Order findAllByStatus(OrderStatus status);

    List<OrderResponse>  getOrdersByUserId(Long userId);

    List<SellerRevenueDTO> getSellerRevenueData();

}