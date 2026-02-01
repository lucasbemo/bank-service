# Bank Service (Spring Boot 3 / Java 21)

Minimalist, production‑lean backend for the Bank challenge using hexagonal architecture, strict idempotency, and concurrency‑safe transfers.

## Overview
- Small, production‑lean backend for a digital bank with users, wallets, and transfers exposed via a simple HTTP API
- Idempotent transfers: same `Idempotency-Key` and payload return the same result; different payload returns `409`
- Pessimistic row locks on payer and payee wallets, acquired in deterministic order (lower userId first) to reduce deadlocks
- Single transaction for debit, credit, transfer record, and outbox event; failures roll back and balances remain correct
- Outbox notifications decouple external calls so money movement never blocks

### Technologies and patterns
- Java 21 + Spring Boot 3
- Hexagonal architecture (domain first)
- Swagger/OpenAPI docs
- k6 integration tests with concise reports
- Docker + Compose with resource caps


## Features Overview

### Core Features
- **User creation** with type `COMMON` or `SHOPKEEPER`
- **Transfers** between users with strict authorization rules
- **Idempotency** required for every transfer
- **Balance safety** with transactional updates
- **Outbox notifications** (best‑effort, async)
- **Consistent API wrapper** for success and errors

### Rules Enforced
- `SHOPKEEPER` users cannot send transfers
- Payer must have sufficient balance
- Authorization service must approve transfer
- Every transfer requires `Idempotency-Key`

### Observability
- All errors are normalized through a common error wrapper
- Transfer operations are logged via Spring Boot defaults

### Docs and Tools
- Swagger UI at `/swagger-ui.html`
- OpenAPI JSON at `/v3/api-docs`
- k6 test runner at `tests/scripts/run-k6.sh`


## Docs
- Architecture: [docs/architecture.md](docs/architecture.md)
- Concurrency deep dive: [docs/concurrency.md](docs/concurrency.md)
- Manual testing: [docs/manual-testing.md](docs/manual-testing.md)
- Plan: [docs/plan.md](docs/plan.md)


## Quick Start

### Run the app (choose one)

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


## API
### Create User
`POST /users`

Headers:
- `Content-Type: application/json`

Body:
```json
{
  "fullName": "Example User",
  "document": "00000000000",
  "email": "user@example.com",
  "password": "secret",
  "type": "COMMON",
  "initialBalance": 1000.00
}
```

Response wrapper:
```json
{
  "success": true,
  "timestamp": "2026-01-31T12:34:56Z",
  "data": { "id": 1, "fullName": "Example User", "type": "COMMON", "balance": 1000.00 },
  "error": null
}
```

### Create Transfer
`POST /transfers`

Headers:
- `Idempotency-Key: <uuid>` (required)

Body:
```json
{
  "value": 10.00,
  "payer": 1,
  "payee": 2
}
```

Response wrapper:
```json
{
  "success": true,
  "timestamp": "2026-01-31T12:34:56Z",
  "data": { "id": 123, "status": "APPROVED", "createdAt": "2026-01-31T12:34:56Z" },
  "error": null
}
```

## Swagger
- UI: `http://localhost:8080/swagger-ui.html`
- JSON: `http://localhost:8080/v3/api-docs`
