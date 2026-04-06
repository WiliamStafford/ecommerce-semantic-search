package com.ecommerce.order.service.impl;

import ch.qos.logback.core.CoreConstants;
import com.ecommerce.order.domain.Cart;
import com.ecommerce.order.domain.CartItem;
import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderItem;
import com.ecommerce.order.dto.request.OrderRequest;
import com.ecommerce.order.enums.OrderStatus;
import com.ecommerce.order.repository.CartItemRepository;
import com.ecommerce.order.repository.CartRepository;
import com.ecommerce.order.repository.OrderItemRepository;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.product.domain.SellerProduct;
import com.ecommerce.product.repository.SellerProductRepository;
import com.ecommerce.product.service.ProductService;
import com.ecommerce.user.domain.User;
import com.ecommerce.user.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ecommerce.order.domain.Order.*;

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

        User user = userService.getProfileById(userId);
        userService.validateUserActive(user.getEmail());

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại"));
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        Order order = builder()
                .userId(userId)
                .shippingAddress(shippingAddress)
                .orderStatus(OrderStatus.PENDING)
                .totalPrice(0.0)
                .build();
        Order savedOrder = orderRepository.save(order);

        double finalTotalPrice = 0.0;
        Long sellerId = null;

        for (CartItem item : cartItems) {
            SellerProduct sp = productService.getSellerProductById(item.getSellerProductId());

            if (savedOrder.getSellerId() == null) {
                savedOrder.setSellerId(sp.getSellerId());
            }

            OrderItem oi = OrderItem.builder()
                    .orderId(savedOrder.getId())
                    .sellerProductId(item.getSellerProductId())
                    .quantity(item.getQuantity())
                    .price(sp.getPrice())
                    .build();
            orderItemRepository.save(oi);

            finalTotalPrice += sp.getPrice() * item.getQuantity();
        }


        savedOrder.setTotalPrice(finalTotalPrice);
        savedOrder.setSellerId(sellerId);
        orderRepository.save(savedOrder);


        cartItemRepository.deleteAllByCartId(cart.getId());

        return savedOrder;
    }

    @Override
    @Transactional
    public Order createOrder(OrderRequest request, String email) {
        userService.validateUserActive(email);
        Long buyerId = userService.findIdByEmail(email);

        if (request.items() == null || request.items().isEmpty()) {
            throw new RuntimeException("Đơn hàng phải có ít nhất một sản phẩm!");
        }

        Order order = builder()
                .sellerId(request.sellerId())
                .userId(buyerId)
                .shippingAddress(request.shippingAddress())
                .orderStatus(OrderStatus.PENDING)
                .totalPrice(0.0)
                .build();

        Order savedOrder = orderRepository.save(order);

        double finalTotalPrice = 0.0;
        Long sellerIdFromProduct = null;

        for (var itemReq : request.items()) {
            SellerProduct sp = productService.getSellerProductById(itemReq.sellerProductId());

            if (sp.getStock() < itemReq.quantity()) {
                throw new RuntimeException("Sản phẩm ID " + sp.getId() + " không đủ hàng!");
            }

            sp.setStock(sp.getStock() - itemReq.quantity());
            sellerProductRepository.saveAndFlush(sp);

            log.info(">>>> Đang trừ kho: Sản phẩm ID {}, Số lượng mua {}", sp.getId(), itemReq.quantity());

            if (sellerIdFromProduct == null) {
                sellerIdFromProduct = sp.getSellerId();
            }

            OrderItem oi = OrderItem.builder()
                    .orderId(savedOrder.getId())
                    .sellerProductId(itemReq.sellerProductId())
                    .quantity(itemReq.quantity())
                    .price(sp.getPrice())
                    .build();

            orderItemRepository.save(oi);

            finalTotalPrice += sp.getPrice() * itemReq.quantity();
        }

        savedOrder.setTotalPrice(finalTotalPrice);
        savedOrder.setSellerId(sellerIdFromProduct);

        return orderRepository.save(savedOrder);
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại!"));
    }


    @Transactional
    public void updateCartItemQuantity(Long userId, Long sellerProductId, int delta) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại"));

        CartItem item = cartItemRepository.findByCartIdAndSellerProductId(cart.getId(), sellerProductId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ"));

        int newQuantity = item.getQuantity() + delta;
        if (newQuantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        }
    }

    @Override
    public void updateStatus(Long id, OrderStatus status) {
        orderRepository.updateStatus(id, status);
    }

    @Override
    public Order findAllByStatus(OrderStatus status) {
      return  orderRepository.findAllByOrderStatus(status);
    }
}