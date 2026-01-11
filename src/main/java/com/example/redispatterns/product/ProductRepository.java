package com.example.redispatterns.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
public class ProductRepository {
    private static final Logger log = LoggerFactory.getLogger(ProductRepository.class);
    
    // 인메모리 Map (간단한 DB 대체 용도)
    private final Map<Long, Product> productMap = new HashMap<>();
    
    public ProductRepository() {
        productMap.put(1L, new Product(1L, "Laptop", new BigDecimal("1299.99"), "High-performance laptop"));
        productMap.put(2L, new Product(2L, "Smartphone", new BigDecimal("799.99"), "Latest smartphone model"));
        productMap.put(3L, new Product(3L, "Headphones", new BigDecimal("199.99"), "Noise-cancelling headphones"));
        productMap.put(4L, new Product(4L, "Tablet", new BigDecimal("499.99"), "10-inch tablet"));
        productMap.put(5L, new Product(5L, "Smartwatch", new BigDecimal("299.99"), "Fitness tracking smartwatch"));
    }
    
    public Optional<Product> findById(Long id) {
        log.info("Fetching product with ID: {} from database", id);
        
        // 느린 데이터베이스 조회를 시뮬레이션
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread interrupted during simulated database delay", e);
        }
        
        return Optional.ofNullable(productMap.get(id));
    }
    
    public Product save(Product product) {
        log.info("Saving product: {}", product);
        productMap.put(product.getId(), product);
        return product;
    }
}