package com.distributedservices.productservice;

import com.distributedservices.productservice.repository.ProductElasticsearchRepository;
import com.distributedservices.productservice.repository.ProductRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableCaching
@EnableJpaRepositories(
        basePackages = "com.distributedservices.productservice.repository",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = ProductElasticsearchRepository.class
        )
)
@EnableElasticsearchRepositories(
        basePackages = "com.distributedservices.productservice.repository",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = ProductRepository.class
        )
)
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
