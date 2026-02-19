package com.distributedservices.userservice.webtest;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minimal application for @WebMvcTest controller slice: no JPA, no Elasticsearch.
 */
@SpringBootApplication(scanBasePackages = "com.distributedservices.userservice.controller")
public class UserServiceWebTestApplication {
}
