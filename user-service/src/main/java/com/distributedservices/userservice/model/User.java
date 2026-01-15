package com.distributedservices.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Document(indexName = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Field(type = FieldType.Text)
    private String username;

    @Column(nullable = false, unique = true)
    @Field(type = FieldType.Text)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name")
    @Field(type = FieldType.Text)
    private String firstName;

    @Column(name = "last_name")
    @Field(type = FieldType.Text)
    private String lastName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
