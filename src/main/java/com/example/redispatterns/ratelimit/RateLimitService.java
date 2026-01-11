package com.example.redispatterns.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {
    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);
    
    // 기본 속도 제한: 10초에 20건
    private static final int DEFAULT_LIMIT = 20;
    private static final int DEFAULT_WINDOW_SECONDS = 10;
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisScript<Long> rateLimitScript;
    
    public RateLimitService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.rateLimitScript = createRateLimitScript();
    }
    
    /**
     * 요청이 속도 제한 기준을 만족하는지 확인합니다.
     * 
     * @param key 속도 제한 키 (예: "ip:path")
     * @param limit 시간 창에서 허용되는 최대 요청 수
     * @param windowSeconds 시간 창(초)
     * @return 허용되면 true, 제한을 초과하면 false
     */
    public boolean isAllowed(String key, int limit, int windowSeconds) {
        try {
            Long count = redisTemplate.execute(
                    rateLimitScript,
                    Collections.singletonList("rl:" + key),
                    limit,
                    windowSeconds
            );
            
            boolean allowed = count != null && count <= limit;
            if (!allowed) {
                log.warn("Rate limit exceeded for key: {}, count: {}, limit: {}", key, count, limit);
            }
            
            return allowed;
        } catch (Exception e) {
            log.error("Error executing rate limit script", e);
            // 오류 발생 시, 요청을 차단하지 않고 지나가도록 허용
            return true;
        }
    }
    
    /**
     * 기본 설정으로 요청 허용 여부를 확인합니다.
     * 
     * @param key 속도 제한 키
     * @return 허용되면 true, 제한을 초과하면 false
     */
    public boolean isAllowed(String key) {
        return isAllowed(key, DEFAULT_LIMIT, DEFAULT_WINDOW_SECONDS);
    }
    
    /**
     * 속도 제한을 위한 Redis Lua 스크립트를 생성합니다.
     */
    private RedisScript<Long> createRateLimitScript() {
        // 속도 제한을 위한 인라인 Lua 스크립트
        String luaScript = 
                "local key = KEYS[1] " +
                "local limit = tonumber(ARGV[1]) " +
                "local windowSeconds = tonumber(ARGV[2]) " +
                
                "local current = tonumber(redis.call('get', key) or '0') " +
                "current = current + 1 " +
                "if current == 1 then " +
                "  redis.call('setex', key, windowSeconds, current) " +
                "else " +
                "  redis.call('incrby', key, 1) " +
                "end " +
                "return current";
        
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(Long.class);
        
        return script;
    }
}