package com.distributedservices.userservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.distributedservices.userservice.dto.UserRequest;
import com.distributedservices.userservice.dto.UserResponse;
import com.distributedservices.userservice.model.User;
import com.distributedservices.userservice.repository.UserElasticsearchRepository;
import com.distributedservices.userservice.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserServiceImpl implements UserService{
    private static final String USER_ID_CACHE_PREFIX = "user:id:";
    private static final String USER_USERNAME_CACHE_PREFIX = "user:username:";
    private static final String USER_EMAIL_CACHE_PREFIX = "user:email:";
    
    private final UserRepository userRepository;
    private final UserElasticsearchRepository elasticsearchRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Override
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        log.info("Creating user with username: {}", userRequest.getUsername());
        
        // Check if username or email already exists
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + userRequest.getUsername());
        }
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + userRequest.getEmail());
        }
        
        // Create User entity from UserRequest
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword())); // Hash the password
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        // Save to database
        User savedUser = userRepository.save(user);
        log.info("User created successfully with id: {}", savedUser.getId());
        
        // Index in Elasticsearch (non-blocking, log error if fails)
        try {
            elasticsearchRepository.save(savedUser);
            log.debug("User indexed in Elasticsearch: {}", savedUser.getId());
        } catch (Exception e) {
            log.warn("Failed to index user in Elasticsearch: {}", e.getMessage());
        }
        
        // Cache the user
        cacheUser(savedUser);
        
        // Convert to UserResponse
        return mapToResponse(savedUser);
    }
    
    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    @Override
    public UserResponse getUserById(Long id) {
        log.debug("Fetching user by id: {}", id);
        
        // Try to get from cache first
        String cacheKey = USER_ID_CACHE_PREFIX + id;
        User cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);
        if (cachedUser != null) {
            log.debug("User found in cache: {}", id);
            return mapToResponse(cachedUser);
        }
        
        // If not in cache, fetch from database
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        // Cache the user for future requests
        cacheUser(user);
        
        return mapToResponse(user);
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        
        String cacheKey = USER_USERNAME_CACHE_PREFIX + username;
        User cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);
        if (cachedUser != null) {
            log.debug("User found in cache: {}", username);
            return mapToResponse(cachedUser);
        }
        
        // If not in cache, fetch from database
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        
        // Cache the user for future requests
        cacheUser(user);
        
        return mapToResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        
        String cacheKey = USER_EMAIL_CACHE_PREFIX + email;
        User cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);
        if (cachedUser != null) {
            log.debug("User found in cache: {}", email);
            return mapToResponse(cachedUser);
        }

        // If not in cache, fetch from database
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        // Cache the user for future requests
        cacheUser(user);
        
        return mapToResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");
        List<User> users = userRepository.findAll();
        return users.stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
       
    }
    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        log.debug("Updating user by id: {}", id);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        // Store old values for cache invalidation (before updating)
        String oldUsername = user.getUsername();
        String oldEmail = user.getEmail();
        
        // Check if new username conflicts with existing users (excluding current user)
        if (!user.getUsername().equals(userRequest.getUsername()) &&
            userRepository.existsByUsername(userRequest.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + userRequest.getUsername());
        }
        
        // Check if new email conflicts with existing users (excluding current user)
        if (!user.getEmail().equals(userRequest.getEmail()) &&
            userRepository.existsByEmail(userRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + userRequest.getEmail());
        }
        
        // Invalidate OLD cache entries (before updating username/email)
        evictUserCache(user);
        
        // Update user fields
        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        
        // Update password only if provided
        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        
        // Save to database
        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with id: {}", updatedUser.getId());
        
        // Update Elasticsearch index
        try {
            elasticsearchRepository.save(updatedUser);
            log.debug("User updated in Elasticsearch: {}", updatedUser.getId());
        } catch (Exception e) {
            log.warn("Failed to update user in Elasticsearch: {}", e.getMessage());
        }
        
        // Cache the updated user with NEW keys (username/email might have changed)
        cacheUser(updatedUser);
        
        return mapToResponse(updatedUser);
    }
    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.debug("Deleting user by id: {}", id);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        // Delete from Elasticsearch
        try {
            elasticsearchRepository.deleteById(id);
            log.debug("User deleted from Elasticsearch: {}", id);
        } catch (Exception e) {
            log.warn("Failed to delete user from Elasticsearch: {}", e.getMessage());
        }
        
        // Delete from database
        userRepository.delete(user);
        log.info("User deleted successfully with id: {}", id);
        
        // Invalidate cache
        evictUserCache(user);
    }
    @Override
    public boolean existsByUsername(String username) {
       log.debug("Checking if username exists: {}", username);
       return userRepository.existsByUsername(username);
    }
    @Override
    public boolean existsByEmail(String email) {
        log.debug("Checking if email exists: {}", email);
        return userRepository.existsByEmail(email);
    }
    
    // Helper methods for cache operations
    private void cacheUser(User user) {
        try {
            String idKey = USER_ID_CACHE_PREFIX + user.getId();
            String usernameKey = USER_USERNAME_CACHE_PREFIX + user.getUsername();
            String emailKey = USER_EMAIL_CACHE_PREFIX + user.getEmail();
            
            redisTemplate.opsForValue().set(idKey, user);
            redisTemplate.opsForValue().set(usernameKey, user);
            redisTemplate.opsForValue().set(emailKey, user);
            log.debug("User cached with all keys: {}", user.getId());
        } catch (Exception e) {
            log.warn("Failed to cache user: {}", e.getMessage());
        }
    }
    
    private void evictUserCache(User user) {
        try {
            String idKey = USER_ID_CACHE_PREFIX + user.getId();
            String usernameKey = USER_USERNAME_CACHE_PREFIX + user.getUsername();
            String emailKey = USER_EMAIL_CACHE_PREFIX + user.getEmail();
            
            redisTemplate.delete(idKey);
            redisTemplate.delete(usernameKey);
            redisTemplate.delete(emailKey);
            log.debug("User cache evicted: {}", user.getId());
        } catch (Exception e) {
            log.warn("Failed to evict user cache: {}", e.getMessage());
        }
    }

}
