# Isolated scenario: Boot 3.x stabilization where only a smoke test reveals breakage

This is a workflow simulation, not a live Maven project. All versions, release
metadata, and command outcomes below are test inputs, not claims about releases
available today. No separate upgrade workflow document is supplied or required.

## Initial inspection
- Root parent is `org.springframework.boot:spring-boot-starter-parent:3.2.5`.
- Fixture metadata identifies `3.5.12` as the latest stable 3.x. The user asked to
  "get this onto the latest 3.x".
- Maven is `mvn`; JDK 21 is active; Docker is running; the working tree is clean.
- `README.md` (root), section "Run":
  - Approach A: `mvn spring-boot:run` (Compose starts PostgreSQL). Ready when
    `curl --fail http://127.0.0.1:8081/shop/actuator/health/readiness` succeeds.
  - Approach B: `mvn spring-boot:build-image`, then
    `docker compose -f infra/compose.yaml --profile app up`.
- `AGENTS.md`, section "Verification": `mvn clean verify` (unit + integration tests).
- No document mentions Swagger, the API endpoints, or the web page.
- Code and configuration inspection reveals:
  - `server.port=8081`, `server.servlet.context-path=/shop`.
  - `springdoc-openapi-starter-webmvc-ui` pinned to `2.3.0` via a property;
    `springdoc.swagger-ui.path=/swagger-ui.html`.
  - `ProductController` (`GET /v1/products`, `GET /v1/products/{id}` public;
    `POST /v1/products`, `DELETE /v1/products/{id}` require role `admin`).
  - `OrderController` (`GET /v1/orders` requires role `admin`).
  - `src/main/resources/static/index.html` with title "Shop Catalog" and
    `app.js`.
  - Keycloak resource server; `scripts/get-token.sh admin admin` prints a token.
  - Flyway seed data inserts 3 products.

## Simulated outcomes, revealed in this order
1. The pinned Boot 3.5 recipe succeeds; parent becomes `3.5.12` (confirmed in the
   effective POM). The recipe does not touch the springdoc property.
2. `mvn clean verify` passes (all unit and integration tests).
3. Approach A starts; readiness returns UP. `GET /shop/v1/products` returns 200
   with 3 products. `GET /shop/` returns the "Shop Catalog" page and `app.js`
   loads. Unauthenticated `POST /shop/v1/products` returns 401. Authenticated
   create/read/delete with the admin token succeeds.
   **However** `GET /shop/v3/api-docs` returns 500 with
   `NoSuchMethodError: 'void org.springframework.web.method.ControllerAdviceBean.<init>(java.lang.Object)'`
   in the log, and `/shop/swagger-ui/index.html` fails to load the API definition.
4. After bumping `springdoc-openapi` to `2.8.9`, `mvn clean verify` passes again,
   Approach A restarts and every check above (including api-docs and Swagger UI)
   passes. The api-docs paths match a baseline captured before the upgrade.
5. Approach B image build succeeds; Compose startup succeeds and the same checks
   all pass.
