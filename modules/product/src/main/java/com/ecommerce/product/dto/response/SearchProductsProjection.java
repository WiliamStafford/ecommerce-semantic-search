package com.ecommerce.product.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchProductsProjection {

    private List<ProductSummaryProjection> products;

    private int total;

    private int page;

    private int pageSize;

    public int getSize() {
        return products.size();
    }
}