# Testing Plan (Unit Tests First)

## Scope (Current Phase)
- Unit tests only (no Spring context, no database).
- JUnit 5 + Mockito with Arrange-Act-Assert structure.
- Focus on application services and core domain behavior.

## Why This Order
- Unit tests provide fast feedback for business rules.
- Integration tests will be added later with Testcontainers.

## Test Strategy
### Application Services
- CreateUserService
  - Saves user and wallet with initial balance.
  - Accepts lowercase type inputs ("common").
- CreateTransferService
  - Idempotency required (blank key rejected).
  - Same key + same payload returns existing transfer.
  - Same key + different payload returns conflict.
  - Payer and payee must exist.
  - Shopkeeper cannot pay.
  - Authorization must pass.
  - Insufficient balance rejected.
  - Deterministic locking order (lower userId first).
  - Happy path saves updated wallets, transfer, and outbox event.

### Domain Model
- Money
  - Two-decimal scaling.
  - Add/Subtract behavior.
  - Comparison helpers.

## Test Conventions
- No Spring context for unit tests.
- Mockito @Mock + @InjectMocks for ports.
- Reusable fixtures in testsupport package.
- Small, focused tests with clear names.

## Next Phase (Later)
- Integration tests with Testcontainers + PostgreSQL.
- Web layer tests and repository tests when needed.

## Coverage (JaCoCo)
- Run tests: `mvn test`
- Enforce coverage + generate report: `mvn verify`
- Report output: `target/site/jacoco/index.html`
- Thresholds: 80% line, 70% branch
- Exclusions: `config`, `dto`, `adapter.in.web`, `Application`
