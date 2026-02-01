# Architecture Overview

This project follows hexagonal architecture to keep domain logic independent from infrastructure and frameworks.

## High‑Level Diagram

```mermaid
flowchart LR
  subgraph Inbound
    Web[REST Controllers]
  end

  subgraph Application
    UseCase[Use Cases]
  end

  subgraph Domain
    Model[Entities + Value Objects]
    Ports[Ports]
  end

  subgraph Outbound
    DB[Persistence Adapters]
    Auth[Authorization HTTP]
    Notify[Notification HTTP]
    Outbox[Outbox Scheduler]
  end

  Web --> UseCase
  UseCase --> Ports
  Ports --> DB
  Ports --> Auth
  Ports --> Notify
  Outbox --> Notify
  UseCase --> Model
```

## Request Flow (Transfer)
```mermaid
sequenceDiagram
  autonumber
  participant C as Client
  participant API as TransferController
  participant UC as CreateTransferUseCase
  participant AUTH as AuthorizationPort
  participant DB as Wallet/Transfer Repos
  participant OUT as Outbox

  C->>API: POST /transfers (Idempotency-Key)
  API->>UC: validate + command
  UC->>AUTH: authorize()
  AUTH-->>UC: approved/denied
  UC->>DB: lock wallets + check balance
  UC->>DB: debit/credit + save transfer
  UC->>OUT: save outbox event
  UC-->>API: transfer result
  API-->>C: response wrapper
```

## Key Design Decisions
- Domain rules live in application services, independent of web adapters
- Ports isolate all external dependencies (DB, HTTP services)
- Outbox prevents failed notifications from affecting transfer commits
