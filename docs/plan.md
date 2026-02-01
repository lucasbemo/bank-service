# Bank Service - Execution Plan

## Objectives
- Implement a minimalist, production‑lean Spring Boot 3 + Java 21 solution
- Use hexagonal architecture
- Enforce idempotency via `Idempotency-Key` header
- Avoid race conditions with pessimistic locking + transactional flow
- Provide consistent API wrapper and exception handling
- Provide k6 integration tests with concise reports
- Include Docker + Compose with resource caps

## Architecture

```
src/main/java/com/bank
  /domain
    /model
    /exception
    /port
  /application
    /usecase
    /service
  /adapter
    /in/web
    /out/persistence
    /out/http
    /out/scheduler
```

## Core Rules
- Shopkeeper cannot send
- Payer must have sufficient balance
- Authorization service must approve
- Notification is best‑effort (outbox)
- `Idempotency-Key` header required

## Concurrency Strategy
- Pessimistic row locking on wallet
- Unique constraint on idempotency key
- Single DB transaction for debit/credit + transfer save + outbox write
- Outbox dispatch in scheduler

## API Contracts
- `POST /users`
- `POST /transfers` (requires `Idempotency-Key` header)

Common response wrapper:
```
{
  "success": true,
  "timestamp": "...",
  "data": { ... },
  "error": null
}
```

## Exception Handling
- Central `@ControllerAdvice`
- 400: missing idempotency, validation
- 403: unauthorized payer, auth denied
- 404: user not found
- 409: idempotency conflict
- 422: insufficient balance

## Swagger/OpenAPI
- Springdoc
- Document `Idempotency-Key` header

## k6 Integration Tests

Structure:
```
tests/k6/
  smoke.js
  load.js
  spike.js
  idempotency.js
  data/
tests/reports/
tests/scripts/run-k6.sh
```

Thresholds:
- smoke: failed <1%, p95 <400ms
- load: failed <1%, p95 <500ms, p99 <900ms
- spike: failed <2%, p95 <800ms
- idempotency: failed <0.5%, p95 <300ms, checks >99%

Reports:
- JSON summary per test in `tests/reports/`
- History preserved by timestamped filenames

## Docker
- Multi‑stage Dockerfile
- docker-compose for app + Postgres (+ optional k6)
- Resource caps via `.env`
  - `APP_MEM_LIMIT=512m`
  - `APP_CPU_LIMIT=0.50`
  - `DB_MEM_LIMIT=512m`
  - `DB_CPU_LIMIT=0.50`
