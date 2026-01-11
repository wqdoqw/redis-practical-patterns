package com.example.redispatterns.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    
    private final ProductService productService;
    
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        log.info("Received request for product ID: {}", id);
        
        long startTime = System.currentTimeMillis();
        Product product = productService.getProduct(id);
        long endTime = System.currentTimeMillis();
        
        log.info("Request for product ID: {} completed in {} ms", id, (endTime - startTime));
        
        return ResponseEntity.ok(product);
    }
}