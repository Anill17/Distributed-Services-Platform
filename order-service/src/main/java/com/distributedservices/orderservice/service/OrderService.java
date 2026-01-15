package com.distributedservices.orderservice.service;

import com.distributedservices.orderservice.dto.OrderRequest;
import com.distributedservices.orderservice.dto.OrderResponse;
import com.distributedservices.orderservice.model.Order;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(OrderRequest orderRequest);

    OrderResponse getOrderById(Long id);

    OrderResponse getOrderByOrderNumber(String orderNumber);

    List<OrderResponse> getOrdersByUserId(Long userId);

    List<OrderResponse> getOrdersByStatus(Order.OrderStatus status);

    List<OrderResponse> getAllOrders();

    OrderResponse updateOrder(Long id, OrderRequest orderRequest);

    OrderResponse updateOrderStatus(Long id, Order.OrderStatus status);

    void deleteOrder(Long id);
}
