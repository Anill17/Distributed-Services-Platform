package com.distributedservices.productservice.repository;

import com.distributedservices.productservice.model.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductElasticsearchRepository extends ElasticsearchRepository<Product, Long> {
    
    List<Product> findByNameContaining(String name);
    
    List<Product> findByDescriptionContaining(String description);
    
    List<Product> findByCategory(String category);
    
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);
}
