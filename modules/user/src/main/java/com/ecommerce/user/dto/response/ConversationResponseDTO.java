package com.ecommerce.user.dto.response;
import lombok.Builder;

@Builder
public record ConversationResponseDTO(
        Long id,
        String sellerName,
        String lastMessage,
        String productImageUrl,
        String productName,
        Long senderId,
        Long receiverId,
        Long targetUserId,
        Long productSellerId,
        Double price,
        String description,
        String targetEmail
) {}