package com.ecommerce.user.service;

import com.ecommerce.user.domain.Conversation;
import com.ecommerce.user.domain.Message;
import com.ecommerce.user.dto.response.ConversationResponseDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
public interface ChatService {
    Message sendMessage(Long senderId, Long receiverId, String content, Long productSellerId);

    List<Message> getMessagesByConversation(Long convId);



    Long getOrCreateConversation(Long buyerId, Long sellerId, Long productSellerId);

    void deleteConversation(Long convId, Long userId);

    List<ConversationResponseDTO> getConversations(Long userId, String mode);
}