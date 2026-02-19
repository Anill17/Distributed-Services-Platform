package com.distributedservices.productservice.controller;

import com.distributedservices.productservice.dto.PagedProductResponse;
import com.distributedservices.productservice.dto.ProductRequest;
import com.distributedservices.productservice.dto.ProductResponse;
import com.distributedservices.productservice.service.ProductService;
import com.distributedservices.productservice.webtest.ProductServiceWebTestApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@org.springframework.test.context.ContextConfiguration(classes = ProductServiceWebTestApplication.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("POST /api/products returns 201 and created product")
    void createProduct_returns201() throws Exception {
        ProductRequest request = new ProductRequest(
                "Test Product",
                "Desc",
                new BigDecimal("19.99"),
                5,
                "Electronics",
                "SKU-101"
        );
        ProductResponse response = new ProductResponse(
                1L, "Test Product", "Desc", new BigDecimal("19.99"),
                5, "Electronics", "SKU-101", LocalDateTime.now(), LocalDateTime.now()
        );
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("SKU-101"))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    @DisplayName("GET /api/products/{id} returns 200 and product when found")
    void getProductById_returns200() throws Exception {
        ProductResponse response = new ProductResponse(
                1L, "Test", "Desc", new BigDecimal("10"),
                1, "Cat", "SKU-1", LocalDateTime.now(), LocalDateTime.now()
        );
        when(productService.getProductById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("SKU-1"));
    }

    @Test
    @DisplayName("GET /api/products returns 200 and paged response")
    void getAllProducts_returns200() throws Exception {
        PagedProductResponse paged = PagedProductResponse.builder()
                .products(List.of())
                .totalElements(0)
                .totalPages(0)
                .currentPage(0)
                .pageSize(10)
                .build();
        when(productService.getAllProducts(any())).thenReturn(paged);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));
    }

    @Test
    @DisplayName("DELETE /api/products/{id} returns 204")
    void deleteProduct_returns204() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }
}
