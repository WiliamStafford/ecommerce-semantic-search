package com.ecommerce.order.service.impl;

import com.ecommerce.order.domain.*;
import com.ecommerce.order.dto.request.OrderRequest;
import com.ecommerce.order.dto.response.*;
import com.ecommerce.order.enums.OrderStatus;
import com.ecommerce.order.repository.*;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.product.domain.SellerProduct;
import com.ecommerce.product.repository.jpa.SellerProductRepository;
import com.ecommerce.product.service.ProductService;
import com.ecommerce.user.dto.response.SellerRevenueDTO;
import com.ecommerce.user.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final UserService userService;
    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final SellerProductRepository sellerProductRepository;
    @Override
    @Transactional
    public Order checkout(Long userId, String shippingAddress) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại"));
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        if (cartItems.isEmpty()) throw new RuntimeException("Giỏ hàng trống");

        SellerProduct firstProduct = productService.getSellerProductById(cartItems.getFirst().getSellerProductId());
        Long sellerId = firstProduct.getSellerId();

        Order order = Order.builder()
                .userId(userId)
                .sellerId(sellerId)
                .shippingAddress(shippingAddress)
                .orderStatus(OrderStatus.PENDING)
                .paymentMethod("COD")
                .build();
        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = cartItems.stream().map(ci -> {
            SellerProduct sp = productService.getSellerProductById(ci.getSellerProductId());
            return OrderItem.builder()
                    .order(savedOrder)
                    .sellerProductId(ci.getSellerProductId())
                    .productName(sp.getProductName())
                    .imageUrl(sp.getAvatar())
                    .quantity(ci.getQuantity())
                    .price(sp.getPrice())
                    .build();
        }).collect(Collectors.toList());

        double total = orderItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        savedOrder.setOrderItems(orderItems);
        savedOrder.setTotalPrice(total);
        orderRepository.save(savedOrder);

        cartItemRepository.deleteAll(cartItems);

        return savedOrder;
    }
    @Override
    @Transactional
    public Order createOrder(Long userId, OrderRequest request) {
        Order order = Order.builder()
                .userId(userId)
                .sellerId(request.sellerId())
                .shippingAddress(request.shippingAddress())
                .totalPrice(request.totalPrice())
                .paymentMethod(request.paymentMethod().name())
                .orderStatus(OrderStatus.PENDING)
                .build();

        List<OrderItem> orderItems = request.items().stream().map(itemReq -> {
            var product = sellerProductRepository.findById(itemReq.sellerProductId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

            return OrderItem.builder()
                    .order(order)
                    .sellerProductId(itemReq.sellerProductId())
                    .productName(product.getProductName())
                    .imageUrl(product.getAvatar())
                    .quantity(itemReq.quantity())
                    .price(itemReq.price())
                    .build();
        }).collect(Collectors.toList());

        order.setOrderItems(orderItems);
        return orderRepository.save(order);
    }
    @Override
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findAllByUserId(userId).stream().map(order ->
                OrderResponse.builder()
                        .id(order.getId())
                        .userId(order.getUserId())
                        .totalPrice(order.getTotalPrice())
                        .orderStatus(order.getOrderStatus().name())
                        .shippingAddress(order.getShippingAddress())
                        .createdAt(order.getCreatedAt())
                        .items(order.getOrderItems().stream().map(item ->
                                OrderItemResponse.builder()
                                        .id(item.getId())
                                        .sellerProductId(item.getSellerProductId())
                                        .quantity(item.getQuantity())
                                        .price(item.getPrice())
                                        .productName(item.getProductName())
                                        .imageUrl(item.getImageUrl())
                                        .build()
                        ).toList())
                        .build()
        ).toList();
    }

    @Override
    public List<SellerRevenueDTO> getSellerRevenueData() {
        List<Object[]> results = orderRepository.calculateRevenueBySeller();

        return results.stream().map(row -> {
            String shopName = (row[0] != null) ? row[0].toString() : "Shop chưa đặt tên";

            Double totalRevenue = 0.0;
            if (row[1] != null) {
                totalRevenue = ((Number) row[1]).doubleValue();
            }

            Long orderCount = 0L;
            if (row[2] != null) {
                orderCount = ((Number) row[2]).longValue();
            }

            return new SellerRevenueDTO(shopName, totalRevenue, orderCount);
        }).collect(Collectors.toList());
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại!"));
    }

    @Transactional
    public void updateCartItemQuantity(Long userId, Long sellerProductId, int delta) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại"));

        CartItem cartItem = cartItemRepository.findByCartIdAndSellerProductId(cart.getId(), sellerProductId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại trong giỏ hàng"));


        int newQuantity = cartItem.getQuantity() + delta;
        if (newQuantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
        }

    }

    @Override
    @Transactional
    public void updateStatus(Long id, OrderStatus status) {
        Order order = getOrderById(id);

        if (status == OrderStatus.CANCELLED && order.getOrderStatus() != OrderStatus.CANCELLED) {
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(id);
            for (OrderItem item : orderItems) {
                SellerProduct sp = sellerProductRepository.findById(item.getSellerProductId())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));
                sp.setStock(sp.getStock() + item.getQuantity());
                sellerProductRepository.save(sp);
            }
            log.info(">>>> Đã hoàn kho cho đơn hàng #{}", id);
        }

        order.setOrderStatus(status);
        orderRepository.save(order);
    }

    @Override
    public Order findAllByStatus(OrderStatus status) {
        return orderRepository.findAllByOrderStatus(status);
    }
}