package com.example.redispatterns.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentResponse {
    private String paymentId;
    private String customerId;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private LocalDateTime timestamp;
    
    // JSON 역직렬화를 위한 기본 생성자
    public PaymentResponse() {
    }
    
    public PaymentResponse(String customerId, BigDecimal amount, String paymentMethod) {
        this.paymentId = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = "COMPLETED";
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
    
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "PaymentResponse{" +
                "paymentId='" + paymentId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", amount=" + amount +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", status='" + status + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}