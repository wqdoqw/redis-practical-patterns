package com.example.redispatterns.config;

import com.example.redispatterns.ratelimit.RateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private final RateLimitInterceptor rateLimitInterceptor;
    
    public WebConfig(RateLimitInterceptor rateLimitInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 특정 경로에만 속도 제한(rate limiting) 적용
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/payments/**");  // 결제 관련 엔드포인트에 적용
                
        // 모든 엔드포인트에 적용
        // registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/**");
    }
}