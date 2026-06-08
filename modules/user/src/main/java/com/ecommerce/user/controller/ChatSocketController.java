package com.ecommerce.user.controller;

import com.ecommerce.user.domain.Message;
import com.ecommerce.user.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/{convId}")
    public void processMessage(@DestinationVariable Long convId, Map<String, String> payload) {
        Long senderId = Long.parseLong(payload.get("senderId"));
        Long receiverId = Long.parseLong(payload.get("receiverId"));
        String content = payload.get("content");

        String sellerIdStr = payload.get("productSellerId");
        Long productSellerId = (sellerIdStr != null) ? Long.parseLong(sellerIdStr) : null;
        Message msg = chatService.sendMessage(senderId, receiverId, content, productSellerId);
        messagingTemplate.convertAndSend("/topic/messages/" + convId, msg);
    }
}