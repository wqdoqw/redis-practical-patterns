package com.example.redispatterns.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RateLimitServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    
    private RateLimitService rateLimitService;
    
    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService(redisTemplate);
    }
    
    @Test
    void isAllowed_UnderLimit_ShouldReturnTrue() {
        // Mock Redis script execution to return a count under the limit
        when(redisTemplate.execute(
                ArgumentMatchers.<RedisScript<Long>>any(),
                ArgumentMatchers.<List<String>>any(),
                any(),
                any()
        )).thenReturn(5L);
        
        // Execute
        boolean allowed = rateLimitService.isAllowed("test:key", 10, 60);
        
        // Verify
        assertTrue(allowed);
        
        // Verify Redis operations
        verify(redisTemplate).execute(
                ArgumentMatchers.<RedisScript<Long>>any(),
                eq(Collections.singletonList("rl:test:key")),
                eq(10),
                eq(60)
        );
    }
    
    @Test
    void isAllowed_AtLimit_ShouldReturnTrue() {
        // Mock Redis script execution to return a count at the limit
        when(redisTemplate.execute(
                ArgumentMatchers.<RedisScript<Long>>any(),
                ArgumentMatchers.<List<String>>any(),
                any(),
                any()
        )).thenReturn(10L);
        
        // Execute
        boolean allowed = rateLimitService.isAllowed("test:key", 10, 60);
        
        // Verify
        assertTrue(allowed);
    }
    
    @Test
    void isAllowed_OverLimit_ShouldReturnFalse() {
        // Mock Redis script execution to return a count over the limit
        when(redisTemplate.execute(
                ArgumentMatchers.<RedisScript<Long>>any(),
                ArgumentMatchers.<List<String>>any(),
                any(),
                any()
        )).thenReturn(11L);
        
        // Execute
        boolean allowed = rateLimitService.isAllowed("test:key", 10, 60);
        
        // Verify
        assertFalse(allowed);
    }
    
    @Test
    void isAllowed_RedisError_ShouldReturnTrue() {
        // Mock Redis script execution to throw an exception
        when(redisTemplate.execute(
                ArgumentMatchers.<RedisScript<Long>>any(),
                ArgumentMatchers.<List<String>>any(),
                any(),
                any()
        )).thenThrow(new RuntimeException("Redis error"));
        
        // Execute
        boolean allowed = rateLimitService.isAllowed("test:key", 10, 60);
        
        // Verify - should allow the request in case of error
        assertTrue(allowed);
    }
    
    @Test
    void isAllowed_DefaultLimits_ShouldUseDefaultValues() {
        // Execute with default limits
        rateLimitService.isAllowed("test:key");
        
        // Verify Redis operations with default values (20 requests per 10 seconds)
        verify(redisTemplate).execute(
                ArgumentMatchers.<RedisScript<Long>>any(),
                eq(Collections.singletonList("rl:test:key")),
                eq(20),
                eq(10)
        );
    }
}