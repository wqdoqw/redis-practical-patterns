package com.example.redispatterns.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class PaymentRequest {
    @NotBlank(message = "Customer ID is required")
    private String customerId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
    
    private String description;
    
    // JSON 역직렬화를 위한 기본 생성자
    public PaymentRequest() {
    }
    
    public PaymentRequest(String customerId, BigDecimal amount, String paymentMethod, String description) {
        this.customerId = customerId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.description = description;
    }
    
    // 게터/세터 (Getters and Setters)
    public String getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return "PaymentRequest{" +
                "customerId='" + customerId + '\'' +
                ", amount=" + amount +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}