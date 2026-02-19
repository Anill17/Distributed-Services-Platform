package com.distributedservices.productservice.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Mirror of order-service OrderEvent for Kafka deserialization.
 * Status is String to avoid coupling to order-service model.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventPayload {
    public static final String TOPIC_ORDER_CREATED = "order-created";
    public static final String TOPIC_ORDER_UPDATED = "order-updated";
    public static final String TOPIC_ORDER_STATUS_UPDATED = "order-status-updated";
    public static final String TOPIC_ORDER_DELETED = "order-deleted";

    public static final String STATUS_CANCELLED = "CANCELLED";

    private Long orderId;
    private String orderNumber;
    private Long userId;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime timestamp;
    private List<OrderLineItemPayload> lineItems = Collections.emptyList();
}
