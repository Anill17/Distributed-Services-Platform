package com.distributedservices.orderservice.service;

import com.distributedservices.orderservice.dto.OrderLineItemRequest;
import com.distributedservices.orderservice.dto.OrderRequest;
import com.distributedservices.orderservice.dto.OrderResponse;
import com.distributedservices.orderservice.messaging.OrderEvent;
import com.distributedservices.orderservice.model.Order;
import com.distributedservices.orderservice.model.OrderLineItem;
import com.distributedservices.orderservice.repository.OrderElasticsearchRepository;
import com.distributedservices.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderElasticsearchRepository elasticsearchRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Override
    @Transactional
    @CachePut(value = "orders", key = "#result.id")
    @CacheEvict(value = "ordersList", allEntries = true)
    public OrderResponse createOrder(OrderRequest orderRequest) {
        log.info("Creating order for user: {}", orderRequest.getUserId());

        if (orderRepository.findByOrderNumber(orderRequest.getOrderNumber()).isPresent()) {
            throw new IllegalArgumentException("Order number already exists: " + orderRequest.getOrderNumber());
        }

        Order order = orderRequest.toOrder();
        Order saved = orderRepository.save(order);
        log.info("Order created with id: {}", saved.getId());

        try {
            elasticsearchRepository.save(saved);
            log.debug("Order indexed in Elasticsearch: {}", saved.getId());
        } catch (Exception e) {
            log.warn("Failed to index order in Elasticsearch: {}", e.getMessage());
        }

        publishOrderEvent(OrderEvent.TOPIC_ORDER_CREATED, saved);
        return OrderResponse.fromOrder(saved);
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        log.debug("Getting order by id: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return OrderResponse.fromOrder(order);
    }

    @Override
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        log.debug("Getting order by orderNumber: {}", orderNumber);
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with order number: " + orderNumber));
        return OrderResponse.fromOrder(order);
    }

    @Override
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        log.debug("Getting orders by userId: {}", userId);
        try {
            List<Order> orders = elasticsearchRepository.findByUserId(userId);
            return orders.stream().map(OrderResponse::fromOrder).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Elasticsearch failed for getOrdersByUserId, falling back to DB: {}", e.getMessage());
            return orderRepository.findByUserId(userId).stream()
                    .map(OrderResponse::fromOrder)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<OrderResponse> getOrdersByStatus(Order.OrderStatus status) {
        log.debug("Getting orders by status: {}", status);
        try {
            List<Order> orders = elasticsearchRepository.findByStatus(status.name());
            return orders.stream().map(OrderResponse::fromOrder).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Elasticsearch failed for getOrdersByStatus, falling back to DB: {}", e.getMessage());
            return orderRepository.findByStatus(status).stream()
                    .map(OrderResponse::fromOrder)
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Cacheable(value = "ordersList", key = "'all'")
    public List<OrderResponse> getAllOrders() {
        log.debug("Getting all orders");
        try {
            Iterable<Order> iterable = elasticsearchRepository.findAll();
            return StreamSupport.stream(iterable.spliterator(), false)
                    .map(OrderResponse::fromOrder)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Elasticsearch failed for getAllOrders, falling back to DB: {}", e.getMessage());
            return orderRepository.findAll().stream()
                    .map(OrderResponse::fromOrder)
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(Long id, OrderRequest orderRequest) {
        log.info("Updating order: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        if (!order.getOrderNumber().equals(orderRequest.getOrderNumber())
                && orderRepository.findByOrderNumber(orderRequest.getOrderNumber()).isPresent()) {
            throw new IllegalArgumentException("Order number already exists: " + orderRequest.getOrderNumber());
        }

        order.setUserId(orderRequest.getUserId());
        order.setOrderNumber(orderRequest.getOrderNumber());
        order.setTotalAmount(orderRequest.getTotalAmount());
        order.setStatus(orderRequest.getStatus());
        order.setShippingAddress(orderRequest.getShippingAddress());
        order.setUpdatedAt(LocalDateTime.now());

        order.getLineItems().clear();
        if (orderRequest.getLineItems() != null) {
            for (OrderLineItemRequest req : orderRequest.getLineItems()) {
                OrderLineItem item = new OrderLineItem();
                item.setOrder(order);
                item.setProductId(req.getProductId());
                item.setQuantity(req.getQuantity());
                order.getLineItems().add(item);
            }
        }

        Order updated = orderRepository.save(order);
        log.info("Order updated: {}", updated.getId());

        try {
            elasticsearchRepository.save(updated);
            log.debug("Order updated in Elasticsearch: {}", updated.getId());
        } catch (Exception ex) {
            log.warn("Failed to update order in Elasticsearch: {}", ex.getMessage());
        }

        publishOrderEvent(OrderEvent.TOPIC_ORDER_UPDATED, updated);
        return OrderResponse.fromOrder(updated);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long id, Order.OrderStatus status) {
        log.info("Updating order status: {} to {}", id, status);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());

        Order updated = orderRepository.save(order);
        log.info("Order status updated: {}", updated.getId());

        try {
            elasticsearchRepository.save(updated);
            log.debug("Order updated in Elasticsearch: {}", updated.getId());
        } catch (Exception ex) {
            log.warn("Failed to update order in Elasticsearch: {}", ex.getMessage());
        }

        publishOrderEvent(OrderEvent.TOPIC_ORDER_STATUS_UPDATED, updated);
        return OrderResponse.fromOrder(updated);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "orders", key = "#id"),
            @CacheEvict(value = "ordersList", allEntries = true)
    })
    public void deleteOrder(Long id) {
        log.info("Deleting order: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        try {
            elasticsearchRepository.deleteById(id);
            log.debug("Order deleted from Elasticsearch: {}", id);
        } catch (Exception e) {
            log.warn("Failed to delete order from Elasticsearch: {}", e.getMessage());
        }

        orderRepository.delete(order);
        log.info("Order deleted: {}", id);
        publishOrderEvent(OrderEvent.TOPIC_ORDER_DELETED, order);
    }

    private void publishOrderEvent(String topic, Order order) {
        try {
            OrderEvent event = OrderEvent.fromOrder(order);
            kafkaTemplate.send(topic, order.getOrderNumber(), event);
            log.debug("Published {} for order: {}", topic, order.getId());
        } catch (Exception e) {
            log.warn("Failed to publish Kafka event {}: {}", topic, e.getMessage());
        }
    }
}
