package com.distributedservices.userservice.service;

import com.distributedservices.userservice.dto.UserRequest;
import com.distributedservices.userservice.dto.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserRequest userRequest);

    UserResponse getUserById(Long id);

    UserResponse getUserByUsername(String username);

    UserResponse getUserByEmail(String email);

    List<UserResponse> getAllUsers();

    UserResponse updateUser(Long id, UserRequest userRequest);

    void deleteUser(Long id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
