package com.ecommerce.product.dto.request;

public record SearchProductsBySemanticQuery(
        String q,
        Integer categoryId,
        int page,
        int size
) {
    public SearchProductsBySemanticQuery(String query) {
        this(query, null,0, 10);
    }
}