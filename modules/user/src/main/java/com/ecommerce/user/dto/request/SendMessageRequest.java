package com.ecommerce.user.dto.request;
public record SendMessageRequest(Long receiverId, String content, Long productSellerId) {}