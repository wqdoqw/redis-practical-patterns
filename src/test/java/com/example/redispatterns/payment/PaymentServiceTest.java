package com.example.redispatterns.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    
    @Mock
    private ValueOperations<String, Object> valueOperations;
    
    private PaymentService paymentService;
    private PaymentRequest testRequest;
    private String idempotencyKey;
    
    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        paymentService = new PaymentService(redisTemplate);
        
        testRequest = new PaymentRequest(
                "customer123",
                new BigDecimal("100.00"),
                "credit_card",
                "Test payment"
        );
        
        idempotencyKey = UUID.randomUUID().toString();
    }
    
    @Test
    void processPayment_FirstRequest_ShouldAcquireLockAndProcess() {
        // Mock Redis operations
        when(valueOperations.get("idem:resp:" + idempotencyKey)).thenReturn(null);
        when(valueOperations.setIfAbsent(
                eq("idem:lock:" + idempotencyKey),
                ArgumentMatchers.any(),
                eq(30L),
                eq(TimeUnit.SECONDS)
        )).thenReturn(true);
        
        // Execute
        PaymentResponse response = paymentService.processPayment(testRequest, idempotencyKey);
        
        // Verify
        assertNotNull(response);
        assertEquals("customer123", response.getCustomerId());
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        assertEquals("credit_card", response.getPaymentMethod());
        assertEquals("COMPLETED", response.getStatus());
        
        // Verify Redis operations
        verify(valueOperations).get("idem:resp:" + idempotencyKey);
        verify(valueOperations).setIfAbsent(
                eq("idem:lock:" + idempotencyKey),
                ArgumentMatchers.any(),
                eq(30L),
                eq(TimeUnit.SECONDS)
        );
        verify(valueOperations).set(
                eq("idem:resp:" + idempotencyKey),
                ArgumentMatchers.any(),
                eq(10L),
                eq(TimeUnit.MINUTES)
        );
        verify(redisTemplate).delete("idem:lock:" + idempotencyKey);
    }
    
    @Test
    void processPayment_CachedResponse_ShouldReturnCachedResponse() {
        // Create a cached response
        PaymentResponse cachedResponse = new PaymentResponse(
                "customer123",
                new BigDecimal("100.00"),
                "credit_card"
        );
        
        // Mock Redis operations
        when(valueOperations.get("idem:resp:" + idempotencyKey)).thenReturn(cachedResponse);
        
        // Execute
        PaymentResponse response = paymentService.processPayment(testRequest, idempotencyKey);
        
        // Verify
        assertNotNull(response);
        assertSame(cachedResponse, response);
        
        // Verify Redis operations
        verify(valueOperations).get("idem:resp:" + idempotencyKey);
        verify(valueOperations, never()).setIfAbsent(
                anyString(),
                any(),
                anyLong(),
                any(TimeUnit.class)
        );
    }
    
    @Test
    void processPayment_LockNotAcquired_ShouldThrowException() {
        // Mock Redis operations
        when(valueOperations.get("idem:resp:" + idempotencyKey)).thenReturn(null);
        when(valueOperations.setIfAbsent(
                eq("idem:lock:" + idempotencyKey),
                ArgumentMatchers.any(),
                eq(30L),
                eq(TimeUnit.SECONDS)
        )).thenReturn(false);
        
        // Execute and verify
        assertThrows(PaymentService.PaymentProcessingException.class, () -> {
            paymentService.processPayment(testRequest, idempotencyKey);
        });
        
        // Verify Redis operations
        verify(valueOperations).get("idem:resp:" + idempotencyKey);
        verify(valueOperations).setIfAbsent(
                eq("idem:lock:" + idempotencyKey),
                ArgumentMatchers.any(),
                eq(30L),
                eq(TimeUnit.SECONDS)
        );
        verify(redisTemplate, never()).delete(anyString());
    }
}