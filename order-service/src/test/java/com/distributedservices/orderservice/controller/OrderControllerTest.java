package com.distributedservices.orderservice.controller;

import com.distributedservices.orderservice.dto.OrderRequest;
import com.distributedservices.orderservice.dto.OrderResponse;
import com.distributedservices.orderservice.model.Order;
import com.distributedservices.orderservice.service.OrderService;
import com.distributedservices.orderservice.webtest.OrderServiceWebTestApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@org.springframework.test.context.ContextConfiguration(classes = OrderServiceWebTestApplication.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    @DisplayName("POST /api/orders returns 201 and created order")
    void createOrder_returns201() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setUserId(1L);
        request.setOrderNumber("ORD-001");
        request.setTotalAmount(new BigDecimal("99.99"));
        request.setStatus(Order.OrderStatus.PENDING);
        request.setShippingAddress("123 Main St");
        request.setLineItems(List.of());

        OrderResponse response = new OrderResponse(
                1L, 1L, "ORD-001", new BigDecimal("99.99"),
                Order.OrderStatus.PENDING, "123 Main St", LocalDateTime.now(), LocalDateTime.now()
        );
        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderNumber").value("ORD-001"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @DisplayName("GET /api/orders/{id} returns 200 and order when found")
    void getOrderById_returns200() throws Exception {
        OrderResponse response = new OrderResponse(
                1L, 1L, "ORD-001", new BigDecimal("99.99"),
                Order.OrderStatus.PENDING, "Address", LocalDateTime.now(), LocalDateTime.now()
        );
        when(orderService.getOrderById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderNumber").value("ORD-001"));
    }

    @Test
    @DisplayName("GET /api/orders/user/{userId} returns 200 and list")
    void getOrdersByUserId_returns200() throws Exception {
        when(orderService.getOrdersByUserId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/orders/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("DELETE /api/orders/{id} returns 204")
    void deleteOrder_returns204() throws Exception {
        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isNoContent());
    }
}
