package com.distributedservices.orderservice.repository;

import com.distributedservices.orderservice.model.Order;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderElasticsearchRepository extends ElasticsearchRepository<Order, Long> {
    
    List<Order> findByUserId(Long userId);
    
    List<Order> findByStatus(String status);
    
    List<Order> findByOrderNumberContaining(String orderNumber);
}
