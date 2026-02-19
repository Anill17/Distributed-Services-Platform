package com.distributedservices.orderservice.service;

import com.distributedservices.orderservice.dto.OrderLineItemRequest;
import com.distributedservices.orderservice.dto.OrderRequest;
import com.distributedservices.orderservice.dto.OrderResponse;
import com.distributedservices.orderservice.messaging.OrderEvent;
import com.distributedservices.orderservice.model.Order;
import com.distributedservices.orderservice.model.OrderLineItem;
import com.distributedservices.orderservice.repository.OrderElasticsearchRepository;
import com.distributedservices.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderElasticsearchRepository elasticsearchRepository;

    @Mock
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @InjectMocks
    private OrderServiceImpl orderService;

    private OrderRequest orderRequest;
    private Order savedOrder;

    @BeforeEach
    void setUp() {
        orderRequest = new OrderRequest();
        orderRequest.setUserId(1L);
        orderRequest.setOrderNumber("ORD-001");
        orderRequest.setTotalAmount(new BigDecimal("99.99"));
        orderRequest.setStatus(Order.OrderStatus.PENDING);
        orderRequest.setShippingAddress("123 Main St");
        orderRequest.setLineItems(List.of(
                new OrderLineItemRequest(1L, 2),
                new OrderLineItemRequest(2L, 1)
        ));

        savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setUserId(1L);
        savedOrder.setOrderNumber("ORD-001");
        savedOrder.setTotalAmount(new BigDecimal("99.99"));
        savedOrder.setStatus(Order.OrderStatus.PENDING);
        savedOrder.setShippingAddress("123 Main St");
        savedOrder.setCreatedAt(LocalDateTime.now());
        savedOrder.setUpdatedAt(LocalDateTime.now());
        savedOrder.setLineItems(new ArrayList<>());
    }

    @Test
    @DisplayName("createOrder saves order and publishes event when order number is unique")
    void createOrder_success() {
        when(orderRepository.findByOrderNumber("ORD-001")).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponse response = orderService.createOrder(orderRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getOrderNumber()).isEqualTo("ORD-001");
        assertThat(response.getUserId()).isEqualTo(1L);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order captured = orderCaptor.getValue();
        assertThat(captured.getOrderNumber()).isEqualTo("ORD-001");
        assertThat(captured.getLineItems()).hasSize(2);

        verify(elasticsearchRepository).save(savedOrder);
        verify(kafkaTemplate).send(eq(OrderEvent.TOPIC_ORDER_CREATED), eq("ORD-001"), any(OrderEvent.class));
    }

    @Test
    @DisplayName("createOrder throws when order number already exists")
    void createOrder_duplicateOrderNumber_throws() {
        when(orderRepository.findByOrderNumber("ORD-001")).thenReturn(Optional.of(savedOrder));

        assertThatThrownBy(() -> orderService.createOrder(orderRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order number already exists");

        verify(orderRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    @DisplayName("getOrderById returns response when order exists")
    void getOrderById_found() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(savedOrder));

        OrderResponse response = orderService.getOrderById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getOrderNumber()).isEqualTo("ORD-001");
    }

    @Test
    @DisplayName("getOrderById throws when order not found")
    void getOrderById_notFound_throws() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("getOrderByOrderNumber returns response when order exists")
    void getOrderByOrderNumber_found() {
        when(orderRepository.findByOrderNumber("ORD-001")).thenReturn(Optional.of(savedOrder));

        OrderResponse response = orderService.getOrderByOrderNumber("ORD-001");

        assertThat(response).isNotNull();
        assertThat(response.getOrderNumber()).isEqualTo("ORD-001");
    }
}
