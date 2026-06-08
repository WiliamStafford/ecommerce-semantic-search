package com.ecommerce.config;

import com.ecommerce.product.repository.es.ProductSemanticSearchRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.ecommerce", // Quét toàn bộ để tìm JPA Repositories
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = ProductSemanticSearchRepository.class // CHẶN JPA quét thằng này!
        )
)
@EnableElasticsearchRepositories(
        basePackages = "com.ecommerce.product.repository.es"
)
public class DataConfig {
}