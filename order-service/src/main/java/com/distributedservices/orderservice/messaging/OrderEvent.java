package com.distributedservices.orderservice.messaging;

import com.distributedservices.orderservice.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    public static final String TOPIC_ORDER_CREATED = "order-created";
    public static final String TOPIC_ORDER_UPDATED = "order-updated";
    public static final String TOPIC_ORDER_STATUS_UPDATED = "order-status-updated";
    public static final String TOPIC_ORDER_DELETED = "order-deleted";

    private Long orderId;
    private String orderNumber;
    private Long userId;
    private Order.OrderStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime timestamp;
    private List<OrderLineItemEvent> lineItems;

    public static OrderEvent fromOrder(Order order) {
        List<OrderLineItemEvent> items = (order.getLineItems() != null)
                ? order.getLineItems().stream().map(OrderLineItemEvent::from).collect(Collectors.toList())
                : Collections.emptyList();
        return OrderEvent.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .timestamp(LocalDateTime.now())
                .lineItems(items)
                .build();
    }
}
