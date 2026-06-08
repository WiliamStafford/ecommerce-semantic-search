package com.ecommerce.order.service.impl;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderItem;
import com.ecommerce.order.dto.response.OrderItemResponse;
import com.ecommerce.order.dto.response.SellerOrderResponse;
import com.ecommerce.order.dto.response.SellerDashboardSummaryResponse; //
import com.ecommerce.order.enums.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.service.SellerOrderService;
import com.ecommerce.product.domain.SellerProduct;
import com.ecommerce.product.enums.SellerProductStatus; //
import com.ecommerce.product.repository.jpa.SellerProductRepository; //
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerOrderServiceImpl implements SellerOrderService {

    private final OrderRepository orderRepository;

    private final SellerProductRepository sellerProductRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SellerOrderResponse> getOrdersBySeller(Long sellerId) {
        List<Order> dbOrders = orderRepository.findBySellerId(sellerId);

        return dbOrders.stream()
                .map(order -> new SellerOrderResponse(
                        order.getId(),
                        "ORD-" + order.getId(),
                        order.getTotalPrice(),
                        order.getShippingAddress(),
                        order.getOrderStatus(),
                        order.getCreatedAt(),
                        order.getOrderItems().stream().map(item ->
                                OrderItemResponse.builder()
                                        .id(item.getId())
                                        .productName(item.getProductName())
                                        .quantity(item.getQuantity())
                                        .price(item.getPrice())
                                        .imageUrl(item.getImageUrl())
                                        .build()
                        ).toList()
                ))
                .toList();
    }

    @Override
    @Transactional
    public boolean updateSellerOrderStatus(Long orderId, OrderStatus status) {
        log.info(">>>> [ORDER-SERVICE] Tiến hành cập nhật trạng thái đơn hàng tổng ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + orderId));

        // 🌟 Logic mới: Trừ kho khi chuyển sang trạng thái DELIVERED
        if (status == OrderStatus.DELIVERED && order.getOrderStatus() != OrderStatus.DELIVERED) {
            log.info(">>>> [ORDER-SERVICE] Đơn hàng {} được giao thành công, tiến hành trừ tồn kho...", orderId);
            for (OrderItem item : order.getOrderItems()) {
                SellerProduct product = sellerProductRepository.findById(item.getSellerProductId())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại trong kho"));

                int newStock = product.getStock() - item.getQuantity();
                product.setStock(newStock);
                sellerProductRepository.save(product);
                log.info(">>>> [ORDER-SERVICE] Cập nhật kho cho SP [{}]: giảm {} đơn vị", product.getProductName(), item.getQuantity());
            }
        }

        log.info(">>>> [ORDER-SERVICE] Đổi trạng thái đơn [{}] từ {} -> Trạng thái mới: {}",
                order.getId(), order.getOrderStatus(), status);

        order.setOrderStatus(status);
        order.setUpdatedAt(java.time.LocalDateTime.now());
        orderRepository.save(order);

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public SellerDashboardSummaryResponse getSellerDashboardSummary(Long sellerId) {
        log.info(">>>> [ORDER-SERVICE] Thực thi tổng hợp số liệu cho Dashboard của sellerId: {}", sellerId);

        List<com.ecommerce.product.domain.SellerProduct> productsList =
                sellerProductRepository.findAllBySellerIdAndStatus(sellerId, SellerProductStatus.ACTIVE);

        int lowStockCount = (int) productsList.stream()
                .filter(p -> p.getStock() != null && p.getStock() <= 5)
                .count();

        List<Order> sellerOrders = orderRepository.findBySellerId(sellerId);
        int totalOrders = sellerOrders.size();

        double totalRevenue = sellerOrders.stream()
                .filter(order -> order.getOrderStatus() != OrderStatus.CANCELLED
                                 && order.getOrderStatus() != OrderStatus.PENDING)
                .mapToDouble(order -> order.getTotalPrice() != null ? order.getTotalPrice() : 0.0)
                .sum();

        log.info(">>>> [ORDER-SERVICE] Tổng hợp thành công! Doanh thu: {}đ, Số đơn: {}, Cảnh báo kho: {}",
                totalRevenue, totalOrders, lowStockCount);

        return new SellerDashboardSummaryResponse(totalRevenue, totalOrders, lowStockCount, productsList);
    }
}