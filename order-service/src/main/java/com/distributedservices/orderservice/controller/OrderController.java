package com.distributedservices.orderservice.controller;

import com.distributedservices.orderservice.dto.OrderRequest;
import com.distributedservices.orderservice.dto.OrderResponse;
import com.distributedservices.orderservice.model.Order;
import com.distributedservices.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        logger.info("POST /api/orders - Creating order for user: {}", orderRequest.getUserId());
        try {
            OrderResponse orderResponse = orderService.createOrder(orderRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
        } catch (IllegalArgumentException e) {
            logger.error("Error creating order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        logger.info("GET /api/orders/{}", id);
        try {
            OrderResponse orderResponse = orderService.getOrderById(id);
            return ResponseEntity.ok(orderResponse);
        } catch (RuntimeException e) {
            logger.error("Error fetching order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/order-number/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByOrderNumber(@PathVariable String orderNumber) {
        logger.info("GET /api/orders/order-number/{}", orderNumber);
        try {
            OrderResponse orderResponse = orderService.getOrderByOrderNumber(orderNumber);
            return ResponseEntity.ok(orderResponse);
        } catch (RuntimeException e) {
            logger.error("Error fetching order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(@PathVariable Long userId) {
        logger.info("GET /api/orders/user/{}", userId);
        List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable Order.OrderStatus status) {
        logger.info("GET /api/orders/status/{}", status);
        List<OrderResponse> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        logger.info("GET /api/orders - Fetching all orders");
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable Long id,
                                                     @Valid @RequestBody OrderRequest orderRequest) {
        logger.info("PUT /api/orders/{} - Updating order", id);
        try {
            OrderResponse orderResponse = orderService.updateOrder(id, orderRequest);
            return ResponseEntity.ok(orderResponse);
        } catch (IllegalArgumentException e) {
            logger.error("Error updating order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            logger.error("Error updating order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long id,
                                                          @RequestBody Order.OrderStatus status) {
        logger.info("PATCH /api/orders/{}/status - Updating order status to {}", id, status);
        try {
            OrderResponse orderResponse = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(orderResponse);
        } catch (RuntimeException e) {
            logger.error("Error updating order status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        logger.info("DELETE /api/orders/{} - Deleting order", id);
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Error deleting order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
