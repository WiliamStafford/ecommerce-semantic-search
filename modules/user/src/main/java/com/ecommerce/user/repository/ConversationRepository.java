package com.ecommerce.user.repository;

import com.ecommerce.user.domain.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository  extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByBuyerIdAndSellerId(Long buyerId, Long sellerId);

    List<Conversation> findAllByBuyerIdOrSellerIdOrderByLastMessageAtDesc(Long buyerId, Long sellerId);
}
