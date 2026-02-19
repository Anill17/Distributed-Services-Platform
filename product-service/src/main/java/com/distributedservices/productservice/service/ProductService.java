package com.distributedservices.productservice.service;

import com.distributedservices.productservice.dto.PagedProductResponse;
import com.distributedservices.productservice.dto.ProductRequest;
import com.distributedservices.productservice.dto.ProductResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest productRequest);

    ProductResponse getProductById(Long id);

    ProductResponse getProductBySku(String sku);

    PagedProductResponse getAllProducts(Pageable pageable);

    List<ProductResponse> getProductsByCategory(String category);

    List<ProductResponse> searchProducts(String query);

    List<ProductResponse> getAvailableProducts();

    ProductResponse updateProduct(Long id, ProductRequest productRequest);

    ProductResponse updateProductQuantity(Long id, Integer quantity);

    void deleteProduct(Long id);

    /**
     * Adjust product stock by delta (positive = add, negative = deduct).
     * Used by order-event consumers for inventory updates.
     */
    void adjustQuantity(Long productId, int delta);
}
