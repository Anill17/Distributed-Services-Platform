package com.distributedservices.userservice.repository;

import com.distributedservices.userservice.model.User;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserElasticsearchRepository extends ElasticsearchRepository<User, Long> {
    
    List<User> findByUsernameContaining(String username);
    
    List<User> findByEmailContaining(String email);
    
    List<User> findByFirstNameContainingOrLastNameContaining(String firstName, String lastName);
}
