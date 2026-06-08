package com.ecommerce.user.controller;

import com.ecommerce.common.security.JwtCurrentUserProvider;
import com.ecommerce.user.domain.Conversation;
import com.ecommerce.user.domain.Message;
import com.ecommerce.user.dto.request.SendMessageRequest;
import com.ecommerce.user.dto.request.StartChatRequest;
import com.ecommerce.user.dto.response.ConversationResponseDTO;
import com.ecommerce.user.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;
    private final JwtCurrentUserProvider currentUserProvider;

    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestBody SendMessageRequest request) {
        Long senderId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(chatService.sendMessage(
                senderId,
                request.receiverId(),
                request.content(),
                request.productSellerId()
        ));
    }

    @GetMapping("/history/{convId}")
    public ResponseEntity<List<Message>> getMessages(@PathVariable Long convId) {
        return ResponseEntity.ok(chatService.getMessagesByConversation(convId));
    }


    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponseDTO>> getConversations(
            @RequestParam(required = false, defaultValue = "CUSTOMER") String mode) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(chatService.getConversations(userId, mode));
    }
    @PostMapping("/start")
    public ResponseEntity<Long> startConversation(@RequestBody StartChatRequest request) {
        Long buyerId = currentUserProvider.getCurrentUserId();

        if (request.productSellerId() == null) {
            throw new IllegalArgumentException("ProductSellerId không được để trống");
        }

        return ResponseEntity.ok(chatService.getOrCreateConversation(
                buyerId,
                request.sellerId(),
                request.productSellerId()
        ));
    }
    @DeleteMapping("/conversation/{convId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long convId) {
        Long userId = currentUserProvider.getCurrentUserId();
        chatService.deleteConversation(convId, userId);
        return ResponseEntity.noContent().build();
    }
}