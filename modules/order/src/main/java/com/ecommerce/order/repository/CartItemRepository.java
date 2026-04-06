package com.ecommerce.order.repository;

import com.ecommerce.order.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartId(Long cartId);
    void deleteAllByCartId(Long cartId);

    Optional<CartItem> findByCartIdAndSellerProductId(Long cartId, Long sellerProductId);

}