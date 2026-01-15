package com.distributedservices.userservice.controller;

import com.distributedservices.userservice.dto.UserRequest;
import com.distributedservices.userservice.dto.UserResponse;
import com.distributedservices.userservice.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest) {
        logger.info("POST /api/users - Creating user: {}", userRequest.getUsername());
        try {
            UserResponse userResponse = userService.createUser(userRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
        } catch (IllegalArgumentException e) {
            logger.error("Error creating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        logger.info("GET /api/users/{}", id);
        try {
            UserResponse userResponse = userService.getUserById(id);
            return ResponseEntity.ok(userResponse);
        } catch (RuntimeException e) {
            logger.error("Error fetching user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        logger.info("GET /api/users/username/{}", username);
        try {
            UserResponse userResponse = userService.getUserByUsername(username);
            return ResponseEntity.ok(userResponse);
        } catch (RuntimeException e) {
            logger.error("Error fetching user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        logger.info("GET /api/users/email/{}", email);
        try {
            UserResponse userResponse = userService.getUserByEmail(email);
            return ResponseEntity.ok(userResponse);
        } catch (RuntimeException e) {
            logger.error("Error fetching user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        logger.info("GET /api/users - Fetching all users");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                   @Valid @RequestBody UserRequest userRequest) {
        logger.info("PUT /api/users/{} - Updating user", id);
        try {
            UserResponse userResponse = userService.updateUser(id, userRequest);
            return ResponseEntity.ok(userResponse);
        } catch (IllegalArgumentException e) {
            logger.error("Error updating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            logger.error("Error updating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.info("DELETE /api/users/{} - Deleting user", id);
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Error deleting user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/exists/username/{username}")
    public ResponseEntity<Boolean> checkUsernameExists(@PathVariable String username) {
        logger.info("GET /api/users/exists/username/{}", username);
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> checkEmailExists(@PathVariable String email) {
        logger.info("GET /api/users/exists/email/{}", email);
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }
}
