package com.distributedservices.orderservice.repository;

import com.distributedservices.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    List<Order> findByUserId(Long userId);
    
    List<Order> findByStatus(Order.OrderStatus status);
    
    List<Order> findByUserIdAndStatus(Long userId, Order.OrderStatus status);
}
