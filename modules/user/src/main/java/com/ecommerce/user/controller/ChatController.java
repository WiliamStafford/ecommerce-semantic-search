package com.ecommerce.user.controller;

import com.ecommerce.user.service.ChatService;
import com.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final UserService userService;
    @PostMapping("/send")
    public ResponseEntity<?> send(Principal principal,
                                  @RequestParam Long receiverId,
                                  @RequestBody String content
                                ) {
        Long senderId = userService.findIdByEmail(principal.getName());
        return ResponseEntity.ok(chatService.sendMessage(senderId, receiverId, content));
    }

    @GetMapping("/history/{convId}")
    public ResponseEntity<?> getHistory(@PathVariable Long convId) {
        return ResponseEntity.ok(chatService.getMessagesByConversation(convId));
    }
}
