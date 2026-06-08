package com.ecommerce.product.dto.request;

import java.util.List;

public record BatchEmbedRequest(List<ProductData> products) {}
