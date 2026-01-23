package com.distributedservices.productservice.controller;

import com.distributedservices.productservice.dto.ProductRequest;
import com.distributedservices.productservice.dto.ProductResponse;
import com.distributedservices.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        log.info("POST /api/products - Creating product: {}", productRequest.getName());
        ProductResponse productResponse = productService.createProduct(productRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(productResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        log.info("GET /api/products/{}", id);
        ProductResponse productResponse = productService.getProductById(id);
        return ResponseEntity.ok(productResponse);
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponse> getProductBySku(@PathVariable String sku) {
        log.info("GET /api/products/sku/{}", sku);
        ProductResponse productResponse = productService.getProductBySku(sku);
        return ResponseEntity.ok(productResponse);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        log.info("GET /api/products - Fetching all products");
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable String category) {
        log.info("GET /api/products/category/{}", category);
        List<ProductResponse> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String q) {
        log.info("GET /api/products/search?q={}", q);
        List<ProductResponse> products = productService.searchProducts(q);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/available")
    public ResponseEntity<List<ProductResponse>> getAvailableProducts() {
        log.info("GET /api/products/available - Fetching available products");
        List<ProductResponse> products = productService.getAvailableProducts();
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                         @Valid @RequestBody ProductRequest productRequest) {
        log.info("PUT /api/products/{} - Updating product", id);
        ProductResponse productResponse = productService.updateProduct(id, productRequest);
        return ResponseEntity.ok(productResponse);
    }

    @PatchMapping("/{id}/quantity")
    public ResponseEntity<ProductResponse> updateProductQuantity(@PathVariable Long id,
                                                                @RequestParam Integer quantity) {
        log.info("PATCH /api/products/{}/quantity - Updating quantity to {}", id, quantity);
        ProductResponse productResponse = productService.updateProductQuantity(id, quantity);
        return ResponseEntity.ok(productResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("DELETE /api/products/{} - Deleting product", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
