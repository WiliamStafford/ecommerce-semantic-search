package com.ecommerce.product.controller;

import com.ecommerce.product.dto.request.SearchProductsBySemanticQuery;
import com.ecommerce.product.dto.response.SearchProductsProjection;
import com.ecommerce.product.service.ProductSemanticSearchUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductSemanticSearchController {

    private final ProductSemanticSearchUseCase semanticSearchUseCase;


    @GetMapping("/semantic-search")
    public ResponseEntity<SearchProductsProjection> search(
            @RequestParam("q") String query,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        SearchProductsBySemanticQuery searchQuery = new SearchProductsBySemanticQuery(
                query,
                categoryId,
                page,
                size
        );

        return ResponseEntity.ok(semanticSearchUseCase.execute(searchQuery));
    }
}