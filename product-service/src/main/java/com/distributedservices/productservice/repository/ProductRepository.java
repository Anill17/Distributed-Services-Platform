package com.distributedservices.productservice.repository;

import com.distributedservices.productservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findBySku(String sku);
    
    List<Product> findByCategory(String category);
    
    List<Product> findByNameContaining(String name);
    
    List<Product> findByQuantityGreaterThan(Integer quantity);
    
    boolean existsBySku(String sku);
}
