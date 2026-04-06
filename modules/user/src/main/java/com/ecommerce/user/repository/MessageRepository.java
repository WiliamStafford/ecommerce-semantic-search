package com.ecommerce.user.repository;

import com.ecommerce.user.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllByConversationIdOrderByCreatedAtAsc(Long conversationId);

    Message findFirstByConversationIdOrderByCreatedAtDesc(Long conversationId);

    Message findByConversationIdOrderByCreatedAtAsc(Long convId);
}
