package com.ecommerce.user.service.impl;

import com.ecommerce.user.domain.Conversation;
import com.ecommerce.user.domain.Message;
import com.ecommerce.user.repository.ConversationRepository;
import com.ecommerce.user.repository.MessageRepository;
import com.ecommerce.user.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final ConversationRepository convRepo;
    private final MessageRepository msgRepo;

    @Transactional
    public Message sendMessage(Long senderId, Long receiverId, String content) {
        Conversation conv = convRepo.findByBuyerIdAndSellerId(senderId, receiverId)
                .or(() -> convRepo.findByBuyerIdAndSellerId(receiverId, senderId))
                .orElseGet(() -> convRepo.save(Conversation.builder()
                        .buyerId(senderId).sellerId(receiverId).build()));

        Message msg = Message.builder()
                .conversationId(conv.getId())
                .senderId(senderId)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        conv.setLastMessageAt(LocalDateTime.now());
        convRepo.save(conv);
        return msgRepo.save(msg);
    }

    @Override
    public Message getMessagesByConversation(Long convId) {
        return msgRepo.findByConversationIdOrderByCreatedAtAsc(convId);
    }
}
