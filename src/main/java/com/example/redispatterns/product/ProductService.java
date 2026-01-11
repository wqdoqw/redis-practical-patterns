package com.example.redispatterns.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    
    private final ProductRepository productRepository;
    
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    /**
     * ID로 상품을 조회합니다. 캐시를 활용합니다.
     * 
     * @Cacheable 동작:
     * 1. 캐시에 상품이 있는지 확인
     * 2. 있으면 메서드 호출 없이 바로 반환
     * 3. 없으면 메서드를 실행하고 결과를 캐시에 저장
     */
    @Cacheable(cacheNames = "product", key = "#id")
    public Product getProduct(Long id) {
        log.info("Cache miss for product ID: {}, fetching from repository", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found with ID: " + id));
    }
}