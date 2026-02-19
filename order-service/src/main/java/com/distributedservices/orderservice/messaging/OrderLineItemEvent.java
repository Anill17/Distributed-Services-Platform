package com.distributedservices.orderservice.messaging;

import com.distributedservices.orderservice.model.OrderLineItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderLineItemEvent {
    private Long productId;
    private Integer quantity;

    public static OrderLineItemEvent from(OrderLineItem item) {
        return new OrderLineItemEvent(item.getProductId(), item.getQuantity());
    }
}
