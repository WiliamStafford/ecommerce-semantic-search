package com.ecommerce.user.repository;

import com.ecommerce.user.domain.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByBuyerIdAndSellerIdAndProductSellerId(Long buyerId, Long sellerId, Long productSellerId);

    @Query("SELECT c FROM Conversation c WHERE c.buyerId = :userId OR c.sellerId = :userId ORDER BY c.lastMessageAt DESC")
    List<Conversation> findAllByUserId(@Param("userId") Long userId);
    List<Conversation> findAllByBuyerIdOrSellerIdOrderByLastMessageAtDesc(Long buyerId, Long sellerId);

    List<Conversation> findByBuyerIdOrderByLastMessageAtDesc(Long buyerId);

    // Lọc hội thoại theo người bán
    List<Conversation> findBySellerIdOrderByLastMessageAtDesc(Long sellerId);
}
