package com.example.redispatterns.exception;

import com.example.redispatterns.payment.PaymentService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;
import java.util.Set;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 검증 에러 처리 (400 Bad Request)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationExceptions(MethodArgumentNotValidException ex) {
        ErrorResponse response = new ErrorResponse("VALIDATION_ERROR", "Validation failed");
        
        BindingResult result = ex.getBindingResult();
        for (FieldError fieldError : result.getFieldErrors()) {
            response.addFieldError(fieldError.getField(), fieldError.getDefaultMessage());
        }
        
        return response;
    }
    
    /**
     * 제약 조건 위반(ConstraintViolation) 에러 처리 (400 Bad Request)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException ex) {
        ErrorResponse response = new ErrorResponse("VALIDATION_ERROR", "Validation failed");
        
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            String propertyPath = violation.getPropertyPath().toString();
            String field = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
            response.addFieldError(field, violation.getMessage());
        }
        
        return response;
    }
    
    /**
     * 필수 요청 헤더 누락 처리 (400 Bad Request)
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingHeader(MissingRequestHeaderException ex) {
        return new ErrorResponse("MISSING_HEADER", "Required header is missing: " + ex.getHeaderName());
    }
    
    /**
     * 결제 처리 중 충돌 상황 처리 (409 Conflict)
     */
    @ExceptionHandler(PaymentService.PaymentProcessingException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handlePaymentProcessing(PaymentService.PaymentProcessingException ex) {
        return new ErrorResponse("PAYMENT_IN_PROGRESS", ex.getMessage());
    }
    
    /**
     * 리소스를 찾을 수 없는 경우 처리 (404 Not Found)
     */
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NoSuchElementException ex) {
        return new ErrorResponse("RESOURCE_NOT_FOUND", ex.getMessage());
    }
    
    /**
     * 500 내부 서버 오류 처리 (Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAllOtherExceptions(Exception ex) {
        log.error("Unhandled exception", ex);
        return new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred");
    }
}