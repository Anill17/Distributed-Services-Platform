package com.distributedservices.productservice.service;

import com.distributedservices.productservice.repository.ProductRepository;
import com.distributedservices.productservice.repository.ProductElasticsearchRepository;
import com.distributedservices.productservice.dto.ProductRequest;
import com.distributedservices.productservice.dto.ProductResponse;
import com.distributedservices.productservice.model.Product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductElasticsearchRepository elasticsearchRepository;
    
    @Override
    @Transactional
    @CachePut(value = "products", key = "#result.id")
    @CacheEvict(value = "productsList", allEntries = true)
    public ProductResponse createProduct(ProductRequest productRequest) {
        log.info("Creating product: {}", productRequest.getName());
        
        // Check if SKU already exists
        if (productRepository.existsBySku(productRequest.getSku())) {
            throw new IllegalArgumentException("Product with SKU already exists: " + productRequest.getSku());
        }

        // Create and save product to database
        Product product = productRequest.toProduct();
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with id: {}", savedProduct.getId());
        
        // Index in Elasticsearch (non-blocking, log error if fails)
        try {
            elasticsearchRepository.save(savedProduct);
            log.debug("Product indexed in Elasticsearch: {}", savedProduct.getId());
        } catch (Exception e) {
            log.warn("Failed to index product in Elasticsearch: {}", e.getMessage());
        }
        
        return ProductResponse.fromProduct(savedProduct);
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(Long id) {
        log.debug("Getting product by id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return ProductResponse.fromProduct(product);
    }

    @Override
    @Cacheable(value = "products", key = "'sku:' + #sku")
    public ProductResponse getProductBySku(String sku) {
        log.info("Getting product by SKU: {}", sku);
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Product not found with SKU: " + sku));
        return ProductResponse.fromProduct(product);
    }

    @Override
    @Cacheable(value = "productsList", key = "'all'")
    public List<ProductResponse> getAllProducts() {
        log.debug("Getting all products");
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(ProductResponse::fromProduct)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsByCategory(String category) {
        // TODO: Implement get products by category logic
        return null;
    }

    @Override
    public List<ProductResponse> searchProducts(String query) {
        // TODO: Implement search products logic
        return null;
    }

    @Override
    public List<ProductResponse> getAvailableProducts() {
        // TODO: Implement get available products logic
        return null;
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest productRequest) {
        // TODO: Implement update product logic
        return null;
    }

    @Override
    @Transactional
    public ProductResponse updateProductQuantity(Long id, Integer quantity) {
        // TODO: Implement update product quantity logic
        return null;
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    @CacheEvict(value = "productsList", allEntries = true)
    public void deleteProduct(Long id) {
        // TODO: Implement delete product logic
    }
}
