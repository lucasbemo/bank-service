# Concurrency Safeguards (Deep Dive)

Transfers are the critical path. This design protects correctness under heavy load using a combination of:
- Required idempotency keys
- Pessimistic row locking on wallets
- Single ACID transaction for debit/credit + transfer + outbox
- Deterministic lock ordering to reduce deadlock risk

## Strategy Summary
1) **Idempotency required**: every transfer request carries `Idempotency-Key`.
2) **Pessimistic lock**: lock payer and payee wallet rows with `PESSIMISTIC_WRITE`.
3) **Single transaction**: all balance updates and transfer persistence happen atomically.
4) **Outbox**: notification is async and never blocks the money movement.

## Locking Diagram
```mermaid
sequenceDiagram
  autonumber
  participant UC as CreateTransferService
  participant U as UserRepository
  participant W as WalletRepository
  participant A as AuthorizationService
  participant T as TransferRepository
  participant O as Outbox

  UC->>UC: begin transaction
  UC->>UC: validate idempotency
  UC->>T: find by idempotency key
  alt key exists + same payload
    UC-->>UC: return stored transfer
  else key exists + different payload
    UC-->>UC: throw conflict
  else key not found
  UC->>U: load payer/payee users
  UC->>A: authorize transfer
  UC->>W: lock lower userId
  UC->>W: lock higher userId
  UC->>W: load payer/payee wallets
  UC->>UC: check balance
  alt insufficient balance
    UC-->>UC: throw insufficient balance
  else sufficient balance
    UC->>W: update balances
    UC->>T: save transfer
    UC->>O: save outbox event
    UC-->>UC: commit transaction
  end
  end
```

## Why Pessimistic Locking
- Prevents **double-spend** when many transfers hit the same payer simultaneously.
- Ensures balance is checked against the most current committed state.
- Keeps logic simple and deterministic under stress.

## Idempotency Behavior
- If key already exists with **same payload**, return existing transfer result.
- If key exists with **different payload**, respond `409` conflict.

```mermaid
flowchart TD
  A[Request with Idempotency-Key] --> B{Key exists?}
  B -->|No| C[Process transfer]
  B -->|Yes + same payload| D[Return stored result]
  B -->|Yes + different payload| E[409 Conflict]
```

## Deadlock Avoidance
- Locks are acquired in **deterministic order** (lower userId first).
- This reduces lock inversion between concurrent transfers.

## Transaction Boundary
All of these steps are inside **one transaction**:
- Lock wallets
- Check payer balance
- Debit/credit
- Save transfer
- Save outbox event

If any step fails, the transaction rolls back and balances remain unchanged.

## Failure Scenarios
- **Authorization denied**: transaction starts but exits before locks; no balance changes
- **Insufficient balance**: transaction rolls back
- **Notification failure**: does not affect transfer; outbox can retry
