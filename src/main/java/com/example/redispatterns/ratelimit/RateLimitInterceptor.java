package com.example.redispatterns.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);
    
    private final RateLimitService rateLimitService;
    
    public RateLimitInterceptor(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 클라이언트 IP 주소 확인
        String clientIp = getClientIp(request);
        
        // 요청 경로 확인
        String requestPath = request.getRequestURI();
        
        // IP와 경로를 기반으로 속도 제한 키 생성
        String rateLimitKey = clientIp + ":" + requestPath;
        
        // 요청 허용 여부 확인
        boolean allowed = rateLimitService.isAllowed(rateLimitKey);
        
        if (!allowed) {
            log.warn("Rate limit exceeded for IP: {}, Path: {}", clientIp, requestPath);
            
            // 429(Too Many Requests) 상태 코드 설정
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Rate limit exceeded. Please try again later.\"}");
            
            return false;
        }
        
        return true;
    }
    
    /**
     * 요청으로부터 클라이언트 IP 주소를 추출합니다.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // 프록시가 여러 단계인 경우, 첫 번째 IP가 실제 클라이언트의 IP입니다
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}