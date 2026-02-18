# Redis 실전 패턴 모음

Spring Boot 3.2.x와 Java 17을 사용하여 Redis의 실용적인 패턴들을 시연하는 예제 프로젝트입니다.

## 개요

이 프로젝트는 확장성과 안정성이 중요한 서비스에서 자주 쓰이는 3가지 Redis 패턴을 다룹니다.

1. **Cache-Aside 패턴**: 자주 조회되는 데이터를 캐시에 두어 읽기 성능을 향상
2. **Idempotency-Key 패턴**: 중복 요청을 방지하고 안전한 재시도를 보장
3. **Rate Limiting 패턴**: Lua 스크립트를 활용한 원자적 속도 제한으로 API 오남용 방지

## 핵심 설계 결정

### Redis 키 설계

- **캐시 키**: `product:{id}`
- **멱등성 키**:
  - Lock: `idem:lock:{key}`
  - Response: `idem:resp:{key}`
- **속도 제한 키**: `rl:{ip}:{path}`

### TTL 전략

- **캐시 TTL**: 10분 (설정 가능)
- **멱등성 Lock TTL**: 30초
- **멱등성 응답 TTL**: 10분
- **Rate Limit 윈도우**: 10초

## 실행 방법

### 사전 준비물

- Java 17+
- Docker 및 Docker Compose

### 순서

1. Redis 시작:
   ```
   docker-compose up -d
   ```

2. 애플리케이션 실행:
   ```
   ./gradlew bootRun
   ```
   
   - 애플리케이션은 `http://localhost:8090` 포트에서 실행됩니다.

## API 엔드포인트

### Cache-Aside 패턴

```
GET /products/{id}
```

첫 호출은 느립니다(DB 조회 시뮬레이션), 이후 호출은 캐시로 인해 빠릅니다.

예제 호출:
```
curl -i "http://localhost:8090/products/1"
```

- 동일한 엔드포인트를 연속으로 두 번 호출해 보세요. 첫 번째는 느리고, 두 번째는 캐시 적중으로 훨씬 빠릅니다.

### Idempotency-Key 패턴

```
POST /payments HTTP/1.1
Accept: */*
Accept-Encoding: deflate, gzip
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36
Host: localhost
Idempotency-Key: abc123
Content-Type: application/json
Content-Length: 130

{
    "customerId": "customer123",
    "amount": 100.00,
    "paymentMethod": "credit_card",
    "description": "Order payment"
}
```

- 동일한 키 = 동일한 응답(애플리케이션 재시작 이후에도 유지될 수 있음)
- 같은 키로 동시 요청 시 하나만 처리되고 나머지는 409 Conflict 반환

### Rate Limiting 패턴

`/payments` 하위 모든 엔드포인트는 클라이언트 IP 기준 10초당 20건으로 제한됩니다.
제한을 초과하면 429 Too Many Requests가 반환됩니다.
