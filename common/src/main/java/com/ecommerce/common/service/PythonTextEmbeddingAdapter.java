package com.ecommerce.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class PythonTextEmbeddingAdapter {

    private final RestTemplate restTemplate;

    @Value("${python.brain.url:http://localhost:8000/api/v1/embed}")
    private String pythonApiUrl;

    @Cacheable(value = "embeddings", key = "#queryText")
    public float[] getEmbedding(String queryText) {
        if (queryText == null || queryText.isBlank()) {
            return new float[384];
        }

        try {
            Map<String, String> body = Map.of("text", queryText.trim());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            var response = restTemplate.exchange(
                    pythonApiUrl,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, List<Double>>>() {}
            );

            List<Double> vector = response.getBody() != null ? response.getBody().get("vector") : null;

            if (vector != null && !vector.isEmpty()) {
                float[] floatArray = new float[vector.size()];
                for (int i = 0; i < vector.size(); i++) {
                    floatArray[i] = vector.get(i).floatValue();
                }
                return floatArray;
            }
        } catch (Exception e) {
            log.error("Lỗi khi kết nối bộ não AI: {}", e.getMessage());
        }
        return new float[384];
    }
}