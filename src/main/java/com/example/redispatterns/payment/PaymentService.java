package com.example.redispatterns.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    
    // Redis 키 prefix들
    private static final String IDEMPOTENCY_LOCK_PREFIX = "idem:lock:";
    private static final String IDEMPOTENCY_RESP_PREFIX = "idem:resp:";
    
    // TTL 값 설정
    private static final long LOCK_TTL_SECONDS = 30;
    private static final long RESPONSE_TTL_MINUTES = 10;
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public PaymentService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 멱등성 키를 활용하여 결제를 처리합니다.
     * 
     * @param request 결제 요청 객체
     * @param idempotencyKey 클라이언트가 제공한 멱등성 키
     * @return 결제 응답 객체
     * @throws PaymentProcessingException 동일한 키로 다른 요청이 처리 중인 경우 발생
     */
    public PaymentResponse processPayment(PaymentRequest request, String idempotencyKey) {
        // 해당 멱등성 키에 대한 캐시된 응답이 이미 있는지 확인
        String respKey = IDEMPOTENCY_RESP_PREFIX + idempotencyKey;
        PaymentResponse cachedResponse = (PaymentResponse) redisTemplate.opsForValue().get(respKey);
        
        if (cachedResponse != null) {
            log.info("Found cached response for idempotency key: {}", idempotencyKey);
            return cachedResponse;
        }
        
        // 해당 멱등성 키에 대한 락을 획득 시도
        String lockKey = IDEMPOTENCY_LOCK_PREFIX + idempotencyKey;
        String lockToken = UUID.randomUUID().toString();
        
        Boolean lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockToken, LOCK_TTL_SECONDS, TimeUnit.SECONDS);
        
        if (lockAcquired == null || !lockAcquired) {
            log.warn("Another request with the same idempotency key is being processed: {}", idempotencyKey);
            throw new PaymentProcessingException("A payment with this idempotency key is already being processed");
        }
        
        try {
            // 결제 처리 로직을 시뮬레이션
            log.info("Processing payment for customer: {}, amount: {}", request.getCustomerId(), request.getAmount());
            Thread.sleep(1000); // 처리 시간 시뮬레이션
            
            // 결제 응답 생성
            PaymentResponse response = new PaymentResponse(
                    request.getCustomerId(),
                    request.getAmount(),
                    request.getPaymentMethod()
            );
            
            // 멱등성 키와 함께 응답을 캐시에 저장
            redisTemplate.opsForValue().set(respKey, response, RESPONSE_TTL_MINUTES, TimeUnit.MINUTES);
            
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentProcessingException("Payment processing was interrupted");
        } finally {
            // Release the lock
            redisTemplate.delete(lockKey);
        }
    }
    
    public static class PaymentProcessingException extends RuntimeException {
        public PaymentProcessingException(String message) {
            super(message);
        }
    }
}