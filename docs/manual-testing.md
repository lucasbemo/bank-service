# Manual Testing Guide

This guide walks through the full feature set using raw HTTP calls.

## 1) Start the app (choose one)

#### Option A: Docker
```bash
docker compose up --build
```

#### Option B: Local (DB required)
```bash
docker compose up -d db
mvn spring-boot:run
```

### Integration tests (choose one, app must be running)

#### Option A: k6 via Docker
```bash
docker compose --profile k6 run k6 run /k6/smoke.js
```

#### Option B: k6 via local script
```bash
tests/scripts/run-k6.sh
```
Reports are stored in `tests/reports/`.

Base URL: `http://localhost:8080`

## 2) Create Users

### Create a COMMON user
```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Example User",
    "document": "00000000000",
    "email": "user@example.com",
    "password": "secret",
    "type": "COMMON",
    "initialBalance": 1000.00
  }'
```

### Create another COMMON user
```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Example User 2",
    "document": "00000000001",
    "email": "user2@example.com",
    "password": "secret",
    "type": "COMMON",
    "initialBalance": 500.00
  }'
```

### Create a SHOPKEEPER user
```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Example Shop",
    "document": "00000000002",
    "email": "shop@example.com",
    "password": "secret",
    "type": "SHOPKEEPER",
    "initialBalance": 800.00
  }'
```

## 3) Create a Transfer (Happy Path)
```bash
curl -X POST http://localhost:8080/transfers \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: transfer-1" \
  -d '{
    "value": 10.00,
    "payer": 1,
    "payee": 2
  }'
```

## 4) Idempotency Replay
Send the same request with the same key and payload. The response should return the same transfer id.
```bash
curl -X POST http://localhost:8080/transfers \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: transfer-1" \
  -d '{
    "value": 10.00,
    "payer": 1,
    "payee": 2
  }'
```

## 5) Idempotency Conflict
Same key but different payload should return `409`.
```bash
curl -X POST http://localhost:8080/transfers \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: transfer-1" \
  -d '{
    "value": 12.00,
    "payer": 1,
    "payee": 2
  }'
```

## 6) Shopkeeper Cannot Send
```bash
curl -X POST http://localhost:8080/transfers \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: transfer-2" \
  -d '{
    "value": 5.00,
    "payer": 3,
    "payee": 1
  }'
```

## 7) Insufficient Balance
```bash
curl -X POST http://localhost:8080/transfers \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: transfer-3" \
  -d '{
    "value": 10000.00,
    "payer": 1,
    "payee": 2
  }'
```

## 8) Missing Idempotency Header
```bash
curl -X POST http://localhost:8080/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "value": 10.00,
    "payer": 1,
    "payee": 2
  }'
```

## Expected Response Wrapper
Success:
```json
{
  "success": true,
  "timestamp": "2026-01-31T12:34:56Z",
  "data": { "id": 123, "status": "APPROVED", "createdAt": "2026-01-31T12:34:56Z" },
  "error": null
}
```

Error:
```json
{
  "success": false,
  "timestamp": "2026-01-31T12:34:56Z",
  "data": null,
  "error": { "code": "INSUFFICIENT_BALANCE", "message": "Payer does not have enough balance" }
}
```
