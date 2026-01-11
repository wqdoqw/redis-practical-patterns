package com.example.redispatterns.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.beans.factory.annotation.Value;
import java.time.Duration;

@Configuration
public class RedisConfig {
    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.timeout:10000}")
    private int redisTimeout;

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // HTTP 메시지 컨버전에서 사용하는 기본 ObjectMapper에는 기본 타입 활성화를 적용하지 않습니다 (명시적 직렬화 유지)
        return mapper;
    }

    @Bean(name = "redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(mapper.getPolymorphicTypeValidator(), DefaultTyping.NON_FINAL);
        return mapper;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        try {
            RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(redisHost, redisPort);

            // Lettuce 클라이언트에 연결 타임아웃을 설정합니다
            LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                    .commandTimeout(Duration.ofMillis(redisTimeout))
                    .shutdownTimeout(Duration.ZERO)  // 즉시 종료
                    .build();

            LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redisConfig, clientConfig);

            // 사용 전에 연결을 검증하도록 설정합니다
            connectionFactory.setValidateConnection(true);

            log.info("Configured Redis connection to {}:{} with timeout {}ms", redisHost, redisPort, redisTimeout);
            return connectionFactory;
        } catch (Exception e) {
            log.warn("Failed to create Redis connection factory: {}. Application will start without Redis support.", e.getMessage());
            // Redis를 사용할 수 없는 상황임을 나타내기 위해 null을 반환
            return null;
        }
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(@Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        try {
            RedisConnectionFactory connectionFactory = redisConnectionFactory();
            if (connectionFactory == null) {
                log.warn("Redis connection factory is null. Creating a dummy RedisTemplate that will fail gracefully.");
                // 실제 Redis가 없을 때도 메서드 호출시 안전하게 실패하도록 동작하는 템플릿을 반환
                return createDummyRedisTemplate(redisObjectMapper);
            }

            template.setConnectionFactory(connectionFactory);

            template.setKeySerializer(new StringRedisSerializer());
            template.setHashKeySerializer(new StringRedisSerializer());

            GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);
            template.setValueSerializer(jsonSerializer);
            template.setHashValueSerializer(jsonSerializer);

            template.afterPropertiesSet();
            log.info("Successfully created RedisTemplate");
            return template;
        } catch (Exception e) {
            log.error("Failed to create RedisTemplate: {}. Creating a dummy RedisTemplate that will fail gracefully.", e.getMessage());
            return createDummyRedisTemplate(redisObjectMapper);
        }
    }


    private RedisTemplate<String, Object> createDummyRedisTemplate(ObjectMapper redisObjectMapper) {
        log.warn("Creating a dummy RedisTemplate. Redis operations will fail gracefully.");

        RedisTemplate<String, Object> template = new RedisTemplate<>();

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        return template;
    }
}
