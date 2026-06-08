package com.ecommerce.product.service;

import com.ecommerce.product.dto.request.SearchProductsBySemanticQuery;
import com.ecommerce.product.dto.response.SearchProductsProjection;

public interface ProductSemanticSearchUseCase {
    SearchProductsProjection execute(SearchProductsBySemanticQuery query);
}