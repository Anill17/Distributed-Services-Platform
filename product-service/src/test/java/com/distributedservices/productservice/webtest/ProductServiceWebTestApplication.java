package com.distributedservices.productservice.webtest;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minimal application for @WebMvcTest controller slice: no JPA, no Elasticsearch.
 */
@SpringBootApplication(scanBasePackages = "com.distributedservices.productservice.controller")
public class ProductServiceWebTestApplication {
}
