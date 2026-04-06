package com.ecommerce.user.service;

import com.ecommerce.user.domain.Message;

public interface ChatService {
    Message sendMessage(Long senderId, Long receiverId, String content);

    Message getMessagesByConversation(Long convId);
}
