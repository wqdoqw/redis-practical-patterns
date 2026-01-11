package com.example.redispatterns.payment;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    
    private final PaymentService paymentService;
    
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    /**
     * 멱등성 키(Idempotency-Key)를 활용하여 결제를 처리합니다.
     * 
     * @param request 결제 요청 본문
     * @param idempotencyKey 클라이언트로부터 전달받은 멱등성 키(필수 헤더)
     * @return 결제 응답
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader(name = "Idempotency-Key", required = true) String idempotencyKey) {
        
        log.info("Received payment request with idempotency key: {}", idempotencyKey);
        
        try {
            PaymentResponse response = paymentService.processPayment(request, idempotencyKey);
            return ResponseEntity.ok(response);
        } catch (PaymentService.PaymentProcessingException e) {
            // Return 409 Conflict if another request with the same key is being processed
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(null);
        }
    }
}