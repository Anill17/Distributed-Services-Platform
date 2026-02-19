package com.distributedservices.orderservice.dto;

import com.distributedservices.orderservice.model.Order;
import com.distributedservices.orderservice.model.OrderLineItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

    private List<@Valid OrderLineItemRequest> lineItems = new ArrayList<>();

    public Order toOrder() {
        Order order = new Order();
        order.setUserId(this.userId);
        order.setOrderNumber(this.orderNumber);
        order.setTotalAmount(this.totalAmount);
        order.setStatus(this.status);
        order.setShippingAddress(this.shippingAddress);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        if (lineItems != null) {
            for (OrderLineItemRequest req : lineItems) {
                OrderLineItem item = new OrderLineItem();
                item.setProductId(req.getProductId());
                item.setQuantity(req.getQuantity());
                item.setOrder(order);
                order.getLineItems().add(item);
            }
        }
        return order;
    }
}
