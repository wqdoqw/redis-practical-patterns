package com.example.redispatterns.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ContextConfiguration(classes = ProductServiceTest.TestCacheConfig.class)
public class ProductServiceTest {

    @Configuration
    @EnableCaching
    static class TestCacheConfig {
        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("product");
        }
    }

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product(1L, "Test Product", new BigDecimal("99.99"), "Test Description");
        
        // Configure the mock repository to return the test product
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
    }

    @Test
    void getProduct_ShouldCacheProduct() {
        // First call should hit the repository
        Product result1 = productService.getProduct(1L);
        
        // Second call should use the cached value
        Product result2 = productService.getProduct(1L);
        
        // Verify results
        assertNotNull(result1);
        assertEquals(testProduct.getId(), result1.getId());
        assertEquals(testProduct.getName(), result1.getName());
        
        // Verify repository was called only once (for the first call)
        verify(productRepository, times(1)).findById(1L);
    }
}