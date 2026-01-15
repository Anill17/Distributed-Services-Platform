package com.distributedservices.productservice.service;

import com.distributedservices.productservice.dto.ProductRequest;
import com.distributedservices.productservice.dto.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest productRequest);

    ProductResponse getProductById(Long id);

    ProductResponse getProductBySku(String sku);

    List<ProductResponse> getAllProducts();

    List<ProductResponse> getProductsByCategory(String category);

    List<ProductResponse> searchProducts(String query);

    List<ProductResponse> getAvailableProducts();

    ProductResponse updateProduct(Long id, ProductRequest productRequest);

    ProductResponse updateProductQuantity(Long id, Integer quantity);

    void deleteProduct(Long id);
}
