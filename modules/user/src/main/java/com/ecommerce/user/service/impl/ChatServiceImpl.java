package com.ecommerce.user.service.impl;

import com.ecommerce.product.dto.response.ProductResponseDTO;
import com.ecommerce.product.service.ProductService;
import com.ecommerce.user.domain.Conversation;
import com.ecommerce.user.domain.Message;
import com.ecommerce.user.dto.response.ConversationResponseDTO;
import com.ecommerce.user.repository.ConversationRepository;
import com.ecommerce.user.repository.MessageRepository;
import com.ecommerce.user.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final ConversationRepository convRepo;
    private final MessageRepository msgRepo;
    private final ProductService productService;
    private final com.ecommerce.product.service.UserLookupService userLookupService;

    @Transactional
    @Override
    public Message sendMessage(Long senderId, Long receiverId, String content, Long productSellerId) {
        Long convId = getOrCreateConversation(senderId, receiverId, productSellerId);
        if (productSellerId == null) {
            throw new IllegalArgumentException("Không thể gửi tin nhắn mà không có thông tin sản phẩm!");
        }
        Message msg = msgRepo.save(Message.builder()
                .conversationId(convId)
                .senderId(senderId)
                .content(content)
                .productSellerId(productSellerId)
                .createdAt(LocalDateTime.now())
                .build());

        convRepo.findById(convId).ifPresent(conv ->
                conv.setLastMessageAt(LocalDateTime.now()));

        return msg;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getMessagesByConversation(Long convId) {
        return msgRepo.findAllByConversationIdOrderByCreatedAtAsc(convId);
    }



    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponseDTO> getConversations(Long userId, String mode) {
        List<Conversation> convs = "SELLER".equals(mode)
                ? convRepo.findBySellerIdOrderByLastMessageAtDesc(userId)
                : convRepo.findByBuyerIdOrderByLastMessageAtDesc(userId);

        if (convs.isEmpty()) return List.of();

        return convs.stream().map(conv -> {
            ProductResponseDTO product = (conv.getProductSellerId() != null)
                    ? productService.getProductById(conv.getProductSellerId())
                    : null;

            Long targetUserId = conv.getBuyerId().equals(userId) ? conv.getSellerId() : conv.getBuyerId();
            String targetName = userLookupService.getSellerName(targetUserId);
            String targetEmail = userLookupService.getSellerEmail(targetUserId);

            String finalTargetName = (targetName == null || targetName.isBlank()) ? "Người dùng ẩn danh" : targetName;
            String finalTargetEmail = (targetEmail == null || targetEmail.isBlank()) ? "Không có email" : targetEmail;
            Message lastMsg = msgRepo.findTopByConversationIdOrderByCreatedAtDesc(conv.getId());

            return new ConversationResponseDTO(
                    conv.getId(),
                    finalTargetName,
                    lastMsg != null ? lastMsg.getContent() : "Chưa có tin nhắn",
                    product != null ? product.getAvatar() : null,
                    product != null ? product.getProductName() : "Sản phẩm",
                    conv.getBuyerId(),
                    conv.getSellerId(),
                    targetUserId,
                    conv.getProductSellerId(),
                    product != null ? product.getPrice() : 0.0,
                    product != null ? product.getDescription() : "Chưa có mô tả",
                    finalTargetEmail
            );
        }).toList();
    }
    @Override
    @Transactional
    public synchronized Long getOrCreateConversation(Long buyerId, Long sellerId, Long productSellerId) {
        if (productSellerId == null) {
            return null;
        }
        return convRepo.findByBuyerIdAndSellerIdAndProductSellerId(buyerId, sellerId, productSellerId)
                .or(() -> convRepo.findByBuyerIdAndSellerIdAndProductSellerId(sellerId, buyerId, productSellerId))
                .map(Conversation::getId)
                .orElseGet(() -> {
                    Conversation newConv = Conversation.builder()
                            .buyerId(buyerId)
                            .sellerId(sellerId)
                            .productSellerId(productSellerId)
                            .lastMessageAt(LocalDateTime.now())
                            .build();
                    return convRepo.save(newConv).getId();
                });
    }

    @Override
    @Transactional
    public void deleteConversation(Long convId, Long userId) {
        Conversation conversation = convRepo.findById(convId)
                .orElseThrow(() -> new RuntimeException("Hội thoại không tồn tại"));

        if (!conversation.getBuyerId().equals(userId) && !conversation.getSellerId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xóa hội thoại này");
        }
        msgRepo.deleteByConversationId(convId);
        convRepo.delete(conversation);
    }
}