package com.distributedservices.productservice.controller;

import com.distributedservices.productservice.dto.ProductRequest;
import com.distributedservices.productservice.dto.ProductResponse;
import com.distributedservices.productservice.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        logger.info("POST /api/products - Creating product: {}", productRequest.getName());
        try {
            ProductResponse productResponse = productService.createProduct(productRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(productResponse);
        } catch (IllegalArgumentException e) {
            logger.error("Error creating product: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        logger.info("GET /api/products/{}", id);
        try {
            ProductResponse productResponse = productService.getProductById(id);
            return ResponseEntity.ok(productResponse);
        } catch (RuntimeException e) {
            logger.error("Error fetching product: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponse> getProductBySku(@PathVariable String sku) {
        logger.info("GET /api/products/sku/{}", sku);
        try {
            ProductResponse productResponse = productService.getProductBySku(sku);
            return ResponseEntity.ok(productResponse);
        } catch (RuntimeException e) {
            logger.error("Error fetching product: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        logger.info("GET /api/products - Fetching all products");
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable String category) {
        logger.info("GET /api/products/category/{}", category);
        List<ProductResponse> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String q) {
        logger.info("GET /api/products/search?q={}", q);
        List<ProductResponse> products = productService.searchProducts(q);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/available")
    public ResponseEntity<List<ProductResponse>> getAvailableProducts() {
        logger.info("GET /api/products/available - Fetching available products");
        List<ProductResponse> products = productService.getAvailableProducts();
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                         @Valid @RequestBody ProductRequest productRequest) {
        logger.info("PUT /api/products/{} - Updating product", id);
        try {
            ProductResponse productResponse = productService.updateProduct(id, productRequest);
            return ResponseEntity.ok(productResponse);
        } catch (IllegalArgumentException e) {
            logger.error("Error updating product: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            logger.error("Error updating product: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PatchMapping("/{id}/quantity")
    public ResponseEntity<ProductResponse> updateProductQuantity(@PathVariable Long id,
                                                                @RequestParam Integer quantity) {
        logger.info("PATCH /api/products/{}/quantity - Updating quantity to {}", id, quantity);
        try {
            ProductResponse productResponse = productService.updateProductQuantity(id, quantity);
            return ResponseEntity.ok(productResponse);
        } catch (RuntimeException e) {
            logger.error("Error updating product quantity: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        logger.info("DELETE /api/products/{} - Deleting product", id);
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Error deleting product: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
