package com.distributedservices.orderservice.webtest;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minimal application for @WebMvcTest controller slice: no JPA, no Elasticsearch.
 */
@SpringBootApplication(scanBasePackages = "com.distributedservices.orderservice.controller")
public class OrderServiceWebTestApplication {
}
