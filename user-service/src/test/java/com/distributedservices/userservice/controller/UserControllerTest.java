package com.distributedservices.userservice.controller;

import com.distributedservices.userservice.dto.UserRequest;
import com.distributedservices.userservice.dto.UserResponse;
import com.distributedservices.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("POST /api/users returns 201 and created user")
    void createUser_returns201() throws Exception {
        UserRequest request = new UserRequest();
        request.setUsername("jdoe");
        request.setEmail("jdoe@example.com");
        request.setPassword("secret");
        request.setFirstName("John");
        request.setLastName("Doe");

        UserResponse response = new UserResponse(
                1L, "jdoe", "jdoe@example.com", "John", "Doe",
                LocalDateTime.now(), LocalDateTime.now()
        );
        when(userService.createUser(any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("jdoe"));
    }

    @Test
    @DisplayName("GET /api/users/{id} returns 200 when user found")
    void getUserById_returns200() throws Exception {
        UserResponse response = new UserResponse(
                1L, "jdoe", "jdoe@example.com", "John", "Doe",
                LocalDateTime.now(), LocalDateTime.now()
        );
        when(userService.getUserById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("jdoe"));
    }
}
