package com.example.redispatterns.product;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private LocalDateTime createdAt;
    
    // JSON 역직렬화를 위해 필요한 기본 생성자
    public Product() {
    }
    
    public Product(Long id, String name, BigDecimal price, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}