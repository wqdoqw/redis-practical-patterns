package com.example.redispatterns.exception;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ErrorResponse {
    private String code;
    private String message;
    private String traceId;
    private LocalDateTime timestamp;
    private List<FieldError> fieldErrors;
    
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
        this.traceId = UUID.randomUUID().toString();
        this.fieldErrors = new ArrayList<>();
    }
    
    public ErrorResponse(String code, String message) {
        this();
        this.code = code;
        this.message = message;
    }
    
    // 게터/세터 (Getters and Setters)
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getTraceId() {
        return traceId;
    }
    
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }
    
    public void setFieldErrors(List<FieldError> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
    
    public void addFieldError(String field, String message) {
        this.fieldErrors.add(new FieldError(field, message));
    }
    
    /**
     * 특정 필드에 대한 검증 오류를 표현합니다.
     */
    public static class FieldError {
        private String field;
        private String message;
        
        public FieldError() {
        }
        
        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }
        
        public String getField() {
            return field;
        }
        
        public void setField(String field) {
            this.field = field;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}