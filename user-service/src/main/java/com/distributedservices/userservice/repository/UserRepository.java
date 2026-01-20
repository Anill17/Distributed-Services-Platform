package com.distributedservices.userservice.repository;

import com.distributedservices.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    java.util.List<User> findByUsernameContaining(String username);
    
    java.util.List<User> findByEmailContaining(String email);
    
    java.util.List<User> findByFirstNameContainingOrLastNameContaining(String firstName, String lastName);
}
