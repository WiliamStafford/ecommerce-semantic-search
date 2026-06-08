package com.ecommerce.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
@Slf4j
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(30))
                .setReadTimeout(Duration.ofSeconds(30))
                .additionalMessageConverters(new StringHttpMessageConverter(StandardCharsets.UTF_8))
                .additionalInterceptors((request, body, execution) -> {
                    log.info("Gọi Python API: {} {}", request.getMethod(), request.getURI());
                    return execution.execute(request, body);
                })
                .build();
    }
}