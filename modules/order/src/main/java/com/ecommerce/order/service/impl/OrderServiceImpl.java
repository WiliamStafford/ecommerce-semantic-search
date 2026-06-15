package com.ecommerce.order.service.impl;

import com.ecommerce.order.domain.*;
import com.ecommerce.order.dto.request.OrderItemRequest;
import com.ecommerce.order.dto.request.OrderRequest;
import com.ecommerce.order.dto.response.*;
import com.ecommerce.order.enums.OrderStatus;
import com.ecommerce.order.repository.*;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.product.domain.SellerProduct;
import com.ecommerce.product.repository.jpa.SellerProductRepository;
import com.ecommerce.product.service.ProductService;
import com.ecommerce.user.domain.Address;
import com.ecommerce.user.dto.response.SellerRevenueDTO;
import com.ecommerce.user.repository.AddressRepository;
import com.ecommerce.user.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private final AddressRepository addressRepository;


    @Transactional
    @Override
    public Order checkout(Long userId, Long addressId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại"));
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        if (cartItems.isEmpty()) throw new RuntimeException("Giỏ hàng trống");

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));

        SellerProduct firstProduct = productService.getSellerProductById(cartItems.getFirst().getSellerProductId());
        Long sellerId = firstProduct.getSellerId();

        Order order = Order.builder()
                .userId(userId)
                .sellerId(sellerId)
                .address(address)
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
        Address address = addressRepository.findById(request.addressId())
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));

        Order order = Order.builder()
                .userId(userId)
                .sellerId(request.sellerId())
                .address(address) // Dùng address
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
        return orderRepository.findAllByUserId(userId).stream().map(order -> {
            // Xây dựng chuỗi địa chỉ từ object Address
            String fullAddress = "Chưa có địa chỉ";
            if (order.getAddress() != null) {
                Address addr = order.getAddress();
                fullAddress = String.format("%s, %s, %s, %s, %s",
                        addr.getHouseNumber(),
                        addr.getStreet(),
                        addr.getWard(),
                        addr.getDistrict(),
                        addr.getProvince());
            }

            return OrderResponse.builder()
                    .id(order.getId())
                    .userId(order.getUserId())
                    .totalPrice(order.getTotalPrice())
                    .orderStatus(order.getOrderStatus().name())
                    .shippingAddress(fullAddress) // Dùng chuỗi đã nối
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
                    .build();
        }).toList();
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
    @Transactional
    public Order createOrderWithNewAddress(Long userId, OrderRequest request, Address newAddress) {
        // Gán User cho Address
        newAddress.setUser(userService.findById(userId));
        Address savedAddress = addressRepository.save(newAddress);

        Order order = Order.builder()
                .userId(userId)
                .sellerId(request.sellerId())
                .address(savedAddress)
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
    @Transactional
    public Order createOrderWithNewAddress(Long userId, OrderRequest request) {
        if (request.newAddress() == null) {
            throw new RuntimeException("Thiếu thông tin địa chỉ mới");
        }

        Address newAddress = Address.builder()
                .province(request.newAddress().province())
                .district(request.newAddress().district())
                .ward(request.newAddress().ward())
                .street(request.newAddress().street())
                .houseNumber(request.newAddress().houseNumber())
                .build();

        return createOrderWithNewAddress(userId, request, newAddress);
    }

    @Override
    @Transactional
    public List<Order> processOrderRequest(Long userId, OrderRequest request) {
        Map<Long, List<OrderItemRequest>> itemsBySeller = request.items().stream()
                .collect(Collectors.groupingBy(item ->
                        sellerProductRepository.findById(item.sellerProductId())
                                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"))
                                .getSellerId()
                ));

        List<Order> createdOrders = new ArrayList<>();

        itemsBySeller.forEach((sellerId, items) -> {
            Address address = createAddressFromRequest(request);
            address.setUser(userService.findById(userId));
            addressRepository.save(address);

            Order order = Order.builder()
                    .userId(userId)
                    .sellerId(sellerId)
                    .address(address)
                    .totalPrice(items.stream().mapToDouble(i -> i.price() * i.quantity()).sum())
                    .paymentMethod(request.paymentMethod().name())
                    .orderStatus(OrderStatus.PENDING)
                    .build();

            List<OrderItem> orderItems = items.stream().map(itemReq -> {
                var product = sellerProductRepository.findById(itemReq.sellerProductId()).orElseThrow();
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
            createdOrders.add(orderRepository.save(order));
        });
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại"));

        for (OrderItemRequest item : request.items()) {
            cartItemRepository.deleteByCartIdAndSellerProductId(cart.getId(), item.sellerProductId());
        }
        return createdOrders;
    }
    private Address createAddressFromRequest(OrderRequest request) {
        if (request.newAddress() == null) {
            throw new RuntimeException("Thông tin địa chỉ không hợp lệ");
        }
        return Address.builder()
                .province(request.newAddress().province())
                .district(request.newAddress().district())
                .ward(request.newAddress().ward())
                .street(request.newAddress().street())
                .houseNumber(request.newAddress().houseNumber())
                .build();
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