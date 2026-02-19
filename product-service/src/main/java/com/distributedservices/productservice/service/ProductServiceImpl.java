package com.distributedservices.productservice.service;

import com.distributedservices.productservice.dto.PagedProductResponse;
import com.distributedservices.productservice.dto.ProductRequest;
import com.distributedservices.productservice.dto.ProductResponse;
import com.distributedservices.productservice.model.Product;
import com.distributedservices.productservice.repository.ProductElasticsearchRepository;
import com.distributedservices.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    public ProductResponse getProductById(Long id) {
        log.debug("Getting product by id: {}", id);
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return ProductResponse.fromProduct(product);
    }

    @Override
    @Cacheable(value = "products", key = "'sku:' + #sku")
    public ProductResponse getProductBySku(String sku) {
        log.info("Getting product by SKU: {}", sku);
        Product product = productRepository.findBySku(sku).orElseThrow(() -> new RuntimeException("Product not found with SKU: " + sku));
        return ProductResponse.fromProduct(product);
    }

    @Override
    public PagedProductResponse getAllProducts(Pageable pageable) {
        log.debug("Getting all products with pagination - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        // Ensure page size is 10
        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), 10, pageable.getSort());

        Page<Product> productPage = productRepository.findAll(pageRequest);

        List<ProductResponse> productResponses = productPage.getContent().stream().map(ProductResponse::fromProduct).collect(Collectors.toList());

        return PagedProductResponse.builder().products(productResponses).totalElements(productPage.getTotalElements()).totalPages(productPage.getTotalPages()).currentPage(productPage.getNumber()).pageSize(productPage.getSize()).build();
    }

    @Override
    @Cacheable(value = "productsList", key = "'category:' + #category")
    public List<ProductResponse> getProductsByCategory(String category) {
        log.info("Getting products by category: {}", category);
        List<Product> products = productRepository.findByCategory(category);
        return products.stream().map(ProductResponse::fromProduct).collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> searchProducts(String query) {
        log.debug("Searching products with query: {}", query);
        try {
            // Search across multiple fields in Elasticsearch
            List<Product> products = elasticsearchRepository.findByNameContaining(query);
            products.addAll(elasticsearchRepository.findByDescriptionContaining(query));

            // Remove duplicates and convert to DTOs
            List<Product> uniqueProducts = products.stream().distinct().collect(Collectors.toList());

            return uniqueProducts.stream().map(ProductResponse::fromProduct).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Elasticsearch search failed, falling back to database search: {}", e.getMessage());
            // Fallback to database search
            List<Product> products = productRepository.findByNameContaining(query);
            products.addAll(productRepository.findByDescriptionContaining(query));

            // Remove duplicates and convert to DTOs
            List<Product> uniqueProducts = products.stream().distinct().collect(Collectors.toList());

            return uniqueProducts.stream().map(ProductResponse::fromProduct).collect(Collectors.toList());
        }
    }

    @Override
    @Cacheable(value = "productsList", key = "'available'")
    public List<ProductResponse> getAvailableProducts() {
        log.debug("Getting available products (quantity > 0)");
        List<Product> products = productRepository.findByQuantityGreaterThan(0);
        return products.stream().map(ProductResponse::fromProduct).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest productRequest) {
        log.info("Updating product by id: {}", id);

        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Check if new SKU conflicts with existing products (excluding current product)
        if (!product.getSku().equals(productRequest.getSku()) && productRepository.existsBySku(productRequest.getSku())) {
            throw new IllegalArgumentException("Product with SKU already exists: " + productRequest.getSku());


        }

        // Update product fields
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setQuantity(productRequest.getQuantity());
        product.setCategory(productRequest.getCategory());
        product.setSku(productRequest.getSku());
        product.setUpdatedAt(LocalDateTime.now());

        // Save to database
        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully with id: {}", updatedProduct.getId());

        // Update Elasticsearch index
        try {
            elasticsearchRepository.save(updatedProduct);
            log.debug("Product updated in Elasticsearch: {}", updatedProduct.getId());
        } catch (Exception e) {
            log.warn("Failed to update product in Elasticsearch: {}", e.getMessage());
        }

        return ProductResponse.fromProduct(updatedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProductQuantity(Long id, Integer quantity) {
        log.info("Updating product quantity by id: {} to {}", id, quantity);

        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be 0 or greater");
        }

        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setQuantity(quantity);
        product.setUpdatedAt(LocalDateTime.now());

        // Save to database
        Product updatedProduct = productRepository.save(product);
        log.info("Product quantity updated successfully with id: {}", updatedProduct.getId());

        // Update Elasticsearch index
        try {
            elasticsearchRepository.save(updatedProduct);
            log.debug("Product updated in Elasticsearch: {}", updatedProduct.getId());
        } catch (Exception e) {
            log.warn("Failed to update product in Elasticsearch: {}", e.getMessage());
        }

        return ProductResponse.fromProduct(updatedProduct);
    }

    @Override
    @Transactional
    @Caching(evict = {@CacheEvict(value = "products", key = "#id"), @CacheEvict(value = "productsList", allEntries = true)})
    public void deleteProduct(Long id) {
        log.info("Deleting product by id: {}", id);

        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Delete from Elasticsearch
        try {
            elasticsearchRepository.deleteById(id);
            log.debug("Product deleted from Elasticsearch: {}", id);
        } catch (Exception e) {
            log.warn("Failed to delete product from Elasticsearch: {}", e.getMessage());
        }

        // Delete from database
        productRepository.delete(product);
        log.info("Product deleted successfully with id: {}", id);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#productId"),
            @CacheEvict(value = "productsList", allEntries = true)
    })
    public void adjustQuantity(Long productId, int delta) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        int newQuantity = product.getQuantity() + delta;
        if (newQuantity < 0) {
            log.warn("Product {} quantity would go negative (current: {}, delta: {}); clamping to 0",
                    productId, product.getQuantity(), delta);
            newQuantity = 0;
        }
        product.setQuantity(newQuantity);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
        try {
            elasticsearchRepository.save(product);
        } catch (Exception e) {
            log.warn("Failed to update product in Elasticsearch: {}", e.getMessage());
        }
        log.debug("Adjusted product {} quantity by {} (new quantity: {})", productId, delta, newQuantity);
    }
}
