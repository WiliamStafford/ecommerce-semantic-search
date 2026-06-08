package com.ecommerce.product.service;

import com.ecommerce.product.dto.response.ProductSummaryProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RerankAdapter {

    private final RestTemplate restTemplate;

    public List<ProductSummaryProjection> rerank(
            String query,
            List<ProductSummaryProjection> candidates,
            Integer categoryId
    ) {
        try {
            Map<String, Object> body = Map.of(
                    "query", query,
                    "candidates", candidates,
                    "category_id", categoryId != null ? categoryId : 0
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            var response = restTemplate.exchange(
                    "http://127.0.0.1:8000/api/v1/rerank",
                    org.springframework.http.HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<List<ProductSummaryProjection>>() {}
            );

            return response.getBody();

        } catch (Exception e) {
            log.error("Rerank API Error: {}", e.getMessage());
            return candidates;
        }
    }
}