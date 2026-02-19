package com.distributedservices.productservice.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderLineItemPayload {
    private Long productId;
    private Integer quantity;
}
