package com.distributedservices.productservice.messaging;

import com.distributedservices.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final CacheManager cacheManager;
    private final ProductService productService;

    @KafkaListener(
            topics = OrderEventPayload.TOPIC_ORDER_CREATED,
            containerFactory = "orderEventListenerContainerFactory"
    )
    public void onOrderCreated(OrderEventPayload event) {
        log.info("Order created event received - orderId: {}, orderNumber: {}, userId: {}",
                event.getOrderId(), event.getOrderNumber(), event.getUserId());
        deductStock(event.getLineItems());
        evictProductListCache();
    }

    @KafkaListener(
            topics = OrderEventPayload.TOPIC_ORDER_UPDATED,
            containerFactory = "orderEventListenerContainerFactory"
    )
    public void onOrderUpdated(OrderEventPayload event) {
        log.info("Order updated event received - orderId: {}, orderNumber: {}, status: {}",
                event.getOrderId(), event.getOrderNumber(), event.getStatus());
        evictProductListCache();
    }

    @KafkaListener(
            topics = OrderEventPayload.TOPIC_ORDER_STATUS_UPDATED,
            containerFactory = "orderEventListenerContainerFactory"
    )
    public void onOrderStatusUpdated(OrderEventPayload event) {
        log.info("Order status updated event received - orderId: {}, orderNumber: {}, status: {}",
                event.getOrderId(), event.getOrderNumber(), event.getStatus());
        if (OrderEventPayload.STATUS_CANCELLED.equalsIgnoreCase(event.getStatus())) {
            restoreStock(event.getLineItems());
        }
        evictProductListCache();
    }

    @KafkaListener(
            topics = OrderEventPayload.TOPIC_ORDER_DELETED,
            containerFactory = "orderEventListenerContainerFactory"
    )
    public void onOrderDeleted(OrderEventPayload event) {
        log.info("Order deleted event received - orderId: {}, orderNumber: {}",
                event.getOrderId(), event.getOrderNumber());
        restoreStock(event.getLineItems());
        evictProductListCache();
    }

    private void deductStock(List<OrderLineItemPayload> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) return;
        for (OrderLineItemPayload item : lineItems) {
            if (item.getProductId() == null || item.getQuantity() == null || item.getQuantity() <= 0) continue;
            try {
                productService.adjustQuantity(item.getProductId(), -item.getQuantity());
                log.debug("Deducted stock: productId={}, quantity={}", item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                log.warn("Failed to deduct stock for product {}: {}", item.getProductId(), e.getMessage());
            }
        }
    }

    private void restoreStock(List<OrderLineItemPayload> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) return;
        for (OrderLineItemPayload item : lineItems) {
            if (item.getProductId() == null || item.getQuantity() == null || item.getQuantity() <= 0) continue;
            try {
                productService.adjustQuantity(item.getProductId(), item.getQuantity());
                log.debug("Restored stock: productId={}, quantity={}", item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                log.warn("Failed to restore stock for product {}: {}", item.getProductId(), e.getMessage());
            }
        }
    }

    private void evictProductListCache() {
        try {
            var cache = cacheManager.getCache("productsList");
            if (cache != null) {
                cache.clear();
                log.debug("Evicted productsList cache after order event");
            }
        } catch (Exception e) {
            log.warn("Failed to evict productsList cache: {}", e.getMessage());
        }
    }
}
