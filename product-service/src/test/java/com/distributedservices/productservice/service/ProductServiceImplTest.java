package com.distributedservices.productservice.service;

import com.distributedservices.productservice.dto.ProductRequest;
import com.distributedservices.productservice.dto.ProductResponse;
import com.distributedservices.productservice.model.Product;
import com.distributedservices.productservice.repository.ProductElasticsearchRepository;
import com.distributedservices.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductElasticsearchRepository elasticsearchRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductRequest productRequest;
    private Product savedProduct;

    @BeforeEach
    void setUp() {
        productRequest = new ProductRequest(
                "Test Product",
                "Description",
                new BigDecimal("29.99"),
                10,
                "Electronics",
                "SKU-001"
        );
        savedProduct = new Product();
        savedProduct.setId(1L);
        savedProduct.setName(productRequest.getName());
        savedProduct.setDescription(productRequest.getDescription());
        savedProduct.setPrice(productRequest.getPrice());
        savedProduct.setQuantity(productRequest.getQuantity());
        savedProduct.setCategory(productRequest.getCategory());
        savedProduct.setSku(productRequest.getSku());
        savedProduct.setCreatedAt(LocalDateTime.now());
        savedProduct.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("createProduct saves product and returns response when SKU is unique")
    void createProduct_success() {
        when(productRepository.existsBySku("SKU-001")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductResponse response = productService.createProduct(productRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getSku()).isEqualTo("SKU-001");
        assertThat(response.getName()).isEqualTo("Test Product");

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getSku()).isEqualTo("SKU-001");
        verify(elasticsearchRepository).save(savedProduct);
    }

    @Test
    @DisplayName("createProduct throws when SKU already exists")
    void createProduct_duplicateSku_throws() {
        when(productRepository.existsBySku("SKU-001")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(productRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SKU already exists");

        verify(productRepository, never()).save(any());
        verify(elasticsearchRepository, never()).save(any());
    }

    @Test
    @DisplayName("getProductById returns response when product exists")
    void getProductById_found() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(savedProduct));

        ProductResponse response = productService.getProductById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getSku()).isEqualTo("SKU-001");
    }

    @Test
    @DisplayName("getProductById throws when product not found")
    void getProductById_notFound_throws() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("getProductBySku returns response when product exists")
    void getProductBySku_found() {
        when(productRepository.findBySku("SKU-001")).thenReturn(Optional.of(savedProduct));

        ProductResponse response = productService.getProductBySku("SKU-001");

        assertThat(response).isNotNull();
        assertThat(response.getSku()).isEqualTo("SKU-001");
    }

    @Test
    @DisplayName("adjustQuantity decreases stock correctly")
    void adjustQuantity_deduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(savedProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        productService.adjustQuantity(1L, -3);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("adjustQuantity restores stock correctly")
    void adjustQuantity_restore() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(savedProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        productService.adjustQuantity(1L, 5);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualTo(15);
    }

    @Test
    @DisplayName("adjustQuantity clamps to zero when delta would make quantity negative")
    void adjustQuantity_clampsToZero() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(savedProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        productService.adjustQuantity(1L, -100);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualTo(0);
    }
}
