package com.distributedservices.productservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Document(indexName = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Field(type = FieldType.Text)
    private String name;

    @Column(columnDefinition = "TEXT")
    @Field(type = FieldType.Text)
    private String description;

    @Column(nullable = false)
    @Field(type = FieldType.Double)
    private BigDecimal price;

    @Column(nullable = false)
    @Field(type = FieldType.Integer)
    private Integer quantity;

    @Column(name = "category")
    @Field(type = FieldType.Keyword)
    private String category;

    @Column(name = "sku", unique = true, nullable = false)
    @Field(type = FieldType.Keyword)
    private String sku;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
