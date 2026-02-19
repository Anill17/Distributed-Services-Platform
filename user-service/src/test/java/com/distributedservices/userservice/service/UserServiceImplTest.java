package com.distributedservices.userservice.service;

import com.distributedservices.userservice.dto.UserRequest;
import com.distributedservices.userservice.dto.UserResponse;
import com.distributedservices.userservice.model.User;
import com.distributedservices.userservice.repository.UserElasticsearchRepository;
import com.distributedservices.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserElasticsearchRepository elasticsearchRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOps;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRequest userRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        userRequest = new UserRequest();
        userRequest.setUsername("jdoe");
        userRequest.setEmail("jdoe@example.com");
        userRequest.setPassword("secret");
        userRequest.setFirstName("John");
        userRequest.setLastName("Doe");

        savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("jdoe");
        savedUser.setEmail("jdoe@example.com");
        savedUser.setPassword("encoded");
        savedUser.setFirstName("John");
        savedUser.setLastName("Doe");
        savedUser.setCreatedAt(LocalDateTime.now());
        savedUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("createUser saves user and returns response when username and email are unique")
    void createUser_success() {
        when(userRepository.existsByUsername("jdoe")).thenReturn(false);
        when(userRepository.existsByEmail("jdoe@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.createUser(userRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("jdoe");
        assertThat(response.getEmail()).isEqualTo("jdoe@example.com");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("jdoe");
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded");
    }

    @Test
    @DisplayName("createUser throws when username already exists")
    void createUser_duplicateUsername_throws() {
        when(userRepository.existsByUsername("jdoe")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(userRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("getUserById returns response when user exists")
    void getUserById_found() {
        when(valueOps.get(anyString())).thenReturn(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(savedUser));

        UserResponse response = userService.getUserById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("jdoe");
    }

    @Test
    @DisplayName("getUserById throws when user not found")
    void getUserById_notFound_throws() {
        when(valueOps.get(anyString())).thenReturn(null);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }
}
