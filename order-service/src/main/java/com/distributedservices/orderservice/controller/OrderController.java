package com.distributedservices.orderservice.controller;

import com.distributedservices.orderservice.dto.OrderRequest;
import com.distributedservices.orderservice.dto.OrderResponse;
import com.distributedservices.orderservice.model.Order;
import com.distributedservices.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        log.info("POST /api/orders - Creating order for user: {}", orderRequest.getUserId());
        try {
            OrderResponse orderResponse = orderService.createOrder(orderRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
        } catch (IllegalArgumentException e) {
            log.warn("Validation error creating order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        log.info("GET /api/orders/{}", id);
        try {
            OrderResponse orderResponse = orderService.getOrderById(id);
            return ResponseEntity.ok(orderResponse);
        } catch (RuntimeException e) {
            log.debug("Order not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/order-number/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByOrderNumber(@PathVariable String orderNumber) {
        log.info("GET /api/orders/order-number/{}", orderNumber);
        try {
            OrderResponse orderResponse = orderService.getOrderByOrderNumber(orderNumber);
            return ResponseEntity.ok(orderResponse);
        } catch (RuntimeException e) {
            log.debug("Order not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(@PathVariable Long userId) {
        log.info("GET /api/orders/user/{}", userId);
        List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable Order.OrderStatus status) {
        log.info("GET /api/orders/status/{}", status);
        List<OrderResponse> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        log.info("GET /api/orders - Fetching all orders");
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable Long id,
                                                     @Valid @RequestBody OrderRequest orderRequest) {
        log.info("PUT /api/orders/{} - Updating order", id);
        try {
            OrderResponse orderResponse = orderService.updateOrder(id, orderRequest);
            return ResponseEntity.ok(orderResponse);
        } catch (IllegalArgumentException e) {
            log.warn("Validation error updating order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.debug("Order not found for update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long id,
                                                          @RequestBody Order.OrderStatus status) {
        log.info("PATCH /api/orders/{}/status - Updating order status to {}", id, status);
        try {
            OrderResponse orderResponse = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(orderResponse);
        } catch (RuntimeException e) {
            log.debug("Order not found for status update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        log.info("DELETE /api/orders/{} - Deleting order", id);
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.debug("Order not found for deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
