### Project Guidelines

These guidelines define architectural and tooling constraints for this repository. They are binding for new code and refactors. If a deviation is needed, document and discuss it before implementation.

---

### Testing constraints
- No Testcontainers policy [CURRENT].
- Use Docker Compose for local dev/test infrastructure; see docker-compose.yml.
- Dev Services are disabled: set in application.properties
  - %dev.quarkus.devservices.enabled=false
  - %test.quarkus.devservices.enabled=false

---

### Quarkus Version
- Require Quarkus 3.28 or higher.
- Maven: set `quarkus.platform.version` to `3.28.x` or newer.

---

### REST Layer (Reactive)
- Use RESTEasy Reactive (`quarkus-rest`) for all HTTP APIs.
- Prefer non-blocking I/O on the event loop; explicitly mark/block where needed (see Persistence section).
- Follow the RESTEasy Reactive guide and Quarkus best practices.
- URL design:
    - Versioned, resource-oriented URLs: `/api/v{version}/resources` (e.g., `/api/v1/orders`).
    - Keep conventions uniform, e.g., `/posts` and `/posts/{slug}/comments`.
- Responses:
    - Always send explicit HTTP status codes (e.g., 200 OK, 201 Created, 404 Not Found).
    - For collections, use pagination when results can be unbounded.
    - Top-level JSON must be an object to allow future extension.
    - Use either snake_case or camelCase consistently across APIs.
- Error model:
    - Standardize on RFC 7807 (`application/problem+json`) with fields: `type`, `title`, `status`, `detail`, `traceId`.
    - Implement `ExceptionMapper`s for domain errors and validation errors to return 4xx with RFC 7807 bodies and include a `traceId`.

---

### Web vs Persistence Layer Separation
- Do not expose JPA entities directly from controllers/resources.
- Define explicit request/response DTO records for all external payloads.
- Apply Bean Validation annotations on request DTOs; validate at the boundary.

---

### Dependency Injection
- Prefer constructor injection over field/setter injection.
- Declare mandatory dependencies as `final` and inject via constructors.

---

### Code Quality
- Keep methods short: prefer fewer than 20 lines of executable code.
  - Exceptions allowed for simple DTOs/records, trivial getters/setters, or clearly linear glue code; otherwise split into smaller, named methods. Aim for one purpose per method.
- Limit complexity:
  - Cyclomatic complexity: target < 10 per method; hard cap at 15 with justification in code review.
  - Cognitive complexity: target < 15 per method (use Sonar-style metric as reference).
  - Avoid deep nesting (> 3 levels). Use guard clauses, early returns, or strategy/polymorphism to flatten logic.
- Clear naming conventions (Java):
  - Packages: lower.case.segments; Classes/Interfaces: PascalCase; Methods/fields: lowerCamelCase; Constants: UPPER_SNAKE_CASE.
  - REST DTOs match API naming policy (Jackson naming strategy already configured). Do not leak entity-specific jargon into public DTOs.
  - Names should reveal intent: prefer domain terms over abbreviations; avoid misleading or overly generic names (e.g., doProcess, handleStuff).
- Consistent abstraction levels:
  - Within a method, operate at a single level of abstraction (no mixing of high-level orchestration with low-level details). Extract lower-level details to private helpers/services.
  - Within a class, group responsibilities cohesively. If a class mixes concerns (transport, domain, persistence), split it.
- Parameters and returns:
  - Prefer fewer than 5 parameters; consider parameter objects/builders when exceeding 4.
  - Favor immutable DTOs/records at boundaries; avoid returning null—use Optional for absent single values; use empty collections for lists.
- Error handling:
  - Fail fast with clear messages; do not swallow exceptions. Map domain/validation errors via the RFC 7807 mappers defined in these guidelines.
- Tests and coverage expectations:
  - Unit-test complex branches and domain logic; new features should include tests. Avoid flakiness (no sleeps; use deterministic clocks or fakes where needed).
- Static analysis and reviews:
  - Keep code free of warnings from the chosen static analysis (SpotBugs/Checkstyle/PMD or Sonar). Configure rules to enforce the complexity and naming guidelines.
  - Code Review checklist should include: method length, complexity, naming clarity, abstraction consistency, and adherence to API/persistence boundaries.

---

### Frontend Technology
- Use Qute for server-side templating and frontend rendering (`quarkus-qute`).
- Organize templates under `src/main/resources/templates`.

---

### Internationalization (i18n)
- Externalize all user-facing text via message bundles rather than hardcoding.
- Use Qute message bundles under src/main/resources/messages*.properties. Place templates in src/main/resources/templates.
- Document bundle naming, supported locales, and fallback strategy (default locale).

---

### Validation
- Include Hibernate Validator (`quarkus-hibernate-validator`).
- Map `ConstraintViolationException` to `400 Bad Request` with a structured error body containing field paths and messages.

---

### Persistence and Reactive Strategy
- We use RESTEasy Reactive at the web layer. For persistence, we choose the blocking Hibernate ORM strategy
- Blocking Hibernate ORM (current default)
    - Use quarkus-hibernate-orm with JDBC and Panache (quarkus-hibernate-orm-panache).
    - Offload DB I/O from the event loop:
      - For blocking work, use @Blocking or @RunOnVirtualThread (Java 21+). Keep transactions short and avoid blocking the event loop.
    - Use @Transactional at the service layer; keep transactions short-lived and avoid long transactions in endpoints.
    - Use DTOs for API boundaries.
    - Implement pagination, filtering, and sorting in repositories/services; enforce limit of 100 results per page.

---

### Database and Migrations
- Use PostgreSQL as the primary database.
- Use a migrations tool:
    - Flyway (`quarkus-flyway`)
- Panache usage policy: use Repository pattern 
- Use soft-delete and auditing policies (createdAt/updatedAt/createdBy/updatedBy); centralize via MappedSuperclass and/or entity listeners.
- Pagination defaults: page=0, size=20, max size=100. Return metadata in the top-level object.

---

### Security
- Protect REST APIs and management endpoints. Apply least privilege; annotate with `@RolesAllowed` or equivalent.
- Authentication strategy 
  - JPA-backed security (`quarkus-security-jpa`) — when users/roles are JPA-managed by the application. [CURRENT]
- Store secrets outside VCS (env vars, Vault/KMS). Do not log credentials or tokens.
- Use password hashing (bcrypt/argon2) and secure token handling.

---

### Management Endpoints and Observability
- Health and Metrics:
    - Use SmallRye Health (`/q/health`).
    - Use Micrometer with Prometheus (`/q/metrics`). Add `quarkus-micrometer-registry-prometheus`. Expose Prometheus metrics at `/q/metrics`. Do not include sensitive labels. Consider adding custom meters via Micrometer’s `MeterRegistry`.
    - Enable separate management interface in production: `quarkus.management.enabled=true` and bind to restricted port/interface.
    - Do not expose sensitive details in readiness/liveness.
- Tracing:
    - Use OpenTelemetry (`quarkus-opentelemetry`) for distributed tracing.
    - Propagate `traceId` into logs (MDC) and consider returning it in a response header (e.g., `X-Trace-Id`).

---

### Logging
- Use Quarkus logging (JBoss Logging API). Use `quarkus-logging-json` for structured logs in production.
- Use parameterized or formatted logging (`log.debugf`) to avoid string concatenation overhead.
- Guard expensive debug/trace log construction: check log level before computing.
- Protect sensitive data: mask or omit PII, tokens, passwords.
- Use MDC to include `requestId` and `traceId` in logs.
- MDC population strategy:
  - Add a request filter that generates or extracts `requestId` and puts it into MDC at request start; clear it at request end.
  - Propagate OpenTelemetry `traceId` from the active span into MDC so logs correlate with distributed traces.

---

### API Documentation
- Use SmallRye OpenAPI (`quarkus-smallrye-openapi`).
- Serve OpenAPI at `/q/openapi` and enable Swagger UI in dev/test only.
- Keep API contracts versioned; consider contract tests to verify compatibility.
- Swagger UI profile gating (mirror application.properties):
  - `%dev.quarkus.swagger-ui.always-include=true`
  - `%test.quarkus.swagger-ui.always-include=true`
  - `%prod.quarkus.swagger-ui.always-include=false`

---

### Testing Strategy
- Use JUnit 5 with `@QuarkusTest`.
- HTTP tests should use RestAssured.
- Contract tests: consider Pact or OpenAPI schema validation.
- Infrastructure for tests (No Testcontainers policy):
    - Use Docker Compose services (see docker-compose.yml).
    - Document required services and how to start them for dev/CI.
    - Disable Dev Services in application.properties: %dev.quarkus.devservices.enabled=false and %test.quarkus.devservices.enabled=false.
    - Example: Start Postgres locally: `docker compose up -d db`; stop with `docker compose down`.
    - Configure %dev and %test datasource URLs to match Compose:
      - %dev.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/appdb
      - %dev.quarkus.datasource.username=appuser
      - %dev.quarkus.datasource.password=apppass
      - %test.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/appdb
      - %test.quarkus.datasource.username=appuser
      - %test.quarkus.datasource.password=apppass

---

### JSON and Serialization
- Maintain consistent property naming (choose snake_case or camelCase) across all services.
- If using Jackson, configure naming strategy accordingly (e.g., `quarkus.jackson.property-naming-strategy`).
- Avoid exposing JPA entities in JSON; use DTOs and custom serializers as needed.

---

### Caching and Conditional Requests
- For cacheable GETs, consider supporting ETags (`If-None-Match`) or `If-Modified-Since`.
- Define cache headers appropriate to data volatility and client needs.

---

### Configuration and Environments
- Use Quarkus profiles: `%dev`, `%test`, `%prod` in `application.properties`.
- Externalize secrets (env vars, Vault/KMS); avoid committing secrets.
- Document ports, base paths, and management interface settings.

---

### Pagination Conventions
- Request parameters: `page` (0-based) and `size` with sensible defaults and max caps.
- Response envelope for collections (top-level object):
  - `{ "data": [ ... ], "page": 0, "size": 20, "totalElements": 123, "totalPages": 7 }`
- Enforce a maximum page size of 100 across repositories/services.

---

### Notes
- These rules are binding for new code and refactors.
- If a deviation is needed, document and discuss it before implementation.
