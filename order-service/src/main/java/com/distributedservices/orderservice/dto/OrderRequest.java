package com.distributedservices.orderservice.dto;

import com.distributedservices.orderservice.model.Order;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class OrderRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Order number is required")
    private String orderNumber;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than 0")
    private BigDecimal totalAmount;

    @NotNull(message = "Status is required")
    private Order.OrderStatus status;

    private String shippingAddress;

    // Constructors
    public OrderRequest() {
    }

    public OrderRequest(Long userId, String orderNumber, BigDecimal totalAmount, Order.OrderStatus status, String shippingAddress) {
        this.userId = userId;
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
        this.status = status;
        this.shippingAddress = shippingAddress;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Order.OrderStatus getStatus() {
        return status;
    }

    public void setStatus(Order.OrderStatus status) {
        this.status = status;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}
