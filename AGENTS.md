# AGENTS.md

Maven multi-module project. The root `pom.xml` (`super-parent-root`) is the parent, and its only module is `rest-team/`, a Spring Boot 4.1.2 / Java 25 CRUD API for soccer teams. Docker infrastructure lives in `infrastructure/`. The commands are documented in `rest-team/README.md`, and the upgrade history is in `SpringBootUpgrade.md`.

## Toolchain (`.sdkmanrc`)
- `java=25.4.4.1+1-graalce`, `maven=3.9.16`. Run `sdk env` (or `sdk env install`) before any `mvn` command. SDKMAN also has Maven 3.6.1 installed, and the Boot 4.1 plugins reject Maven 3.6.x.
- There is no Maven wrapper (`mvnw`), so use plain `mvn`.
- Native builds and tests (`-Pnative`, `-PnativeTest`) need the GraalVM JDK from `.sdkmanrc`.

## Where to run commands
- Run the app, image, and compose commands from `rest-team/`. Paths are relative: `spring.docker.compose.file=../infrastructure/docker-compose-linux.yaml`, and the README uses `docker-compose -f ../infrastructure/...`.
- Run the full build from the repo root. The root `pom.xml` contains the shared surefire, annotation processor, and `sonar` profile config.

## Run approaches (all need Docker)
| # | Command (from `rest-team/`) | Notes |
|---|---|---|
| 1 | `mvn spring-boot:run` | spring-boot-docker-compose starts Postgres and Keycloak and stops them with `down` on exit |
| 1.1 | `mvn spring-boot:run -Pobservability` | Maven profile activates Spring profile `observability`, which adds compose profile `observability` (collector, Prometheus, Loki, Tempo, Grafana) and attaches the OTel agent copied to `target/agent/optel-agent.jar` |
| 2 | `mvn spring-boot:test-run` | Uses Testcontainers through `src/test/.../TestRestTeamApplication` (forces Spring profile `test`, which disables docker compose) |
| 3 | `mvn spring-boot:build-image` then `docker-compose -f ../infrastructure/docker-compose-linux.yaml --profile rest-team up` | Buildpacks, no Dockerfile. Image is `rest-team:1.0` |
| 3.1 | `mvn spring-boot:build-image -Pobservability` then `... --profile rest-team-observability up` | |
| 4 | `mvn -Pnative spring-boot:build-image` then `... --profile rest-team up` | Native image (`BP_JVM_VERSION=25`) |

- The `rest-team*` compose services use `network_mode: host`, and the compose file is Linux-oriented. On macOS, Docker Desktop host networking must be enabled for approaches 3, 3.1, and 4.
- The Keycloak image is legacy `quay.io/keycloak/keycloak:12.0.4`, which is slow, especially on arm64. Its context path is `/auth`, and the realm `team-realm` is imported from JSON. Testcontainers waits up to 180s for it, and compose readiness waits up to 3m.

## Smoke-test endpoints (once running)
- App: port 8081, context path `/rest-team`. Example: `http://127.0.0.1:8081/rest-team/actuator/health`. Readiness includes `db` and a custom `keycloak` health indicator.
- Swagger UI: `http://127.0.0.1:8081/rest-team/swagger-ui/index.html?configUrl=/rest-team/v3/api-docs/swagger-config`. Authorize with Keycloak password flow:
  - `teamuser`/`teamuser` has role `team` and can call secured endpoints.
  - `test`/`test` does not have role `team`, so secured endpoints should return 403.
- Get a token from the CLI with `infrastructure/keycloak/scripts/get-accessToken.sh` (client `team-client`).
- Keycloak admin: `http://127.0.0.1:8080/auth/` (`admin`/`admin`). Grafana: `http://localhost:3000` (`admin`/`admin`). Prometheus: `:9090`.

## Testing
- Surefire is split by JUnit tag in the root `pom.xml`:
  - `mvn test` runs only unit tests (excludes `@Tag("integration")`).
  - Integration tests (`*IntegrationTest`, `@Tag("integration")`) run in the `integration-test` phase, so use `mvn verify`. They need Docker (Testcontainers for Postgres 13.2 and Keycloak 12.0.4, through `TestContainersConfiguration`).
- Single unit test: `mvn test -Dtest=TeamServiceTest`
- Single integration test: `mvn verify -Dtest=TeamResourceIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false`
- Integration tests use loom-unit `@ShouldNotPin`, so avoid code that pins virtual threads, such as `synchronized` around blocking I/O.
- Test seed data is in `src/test/resources/db/migration/V9999__test-data.sql`. The test Keycloak realm is a separate copy in `src/test/resources/keycloak/realms/team-realm.json`, so keep it in sync with the copy in `infrastructure/`.
- Native tests: `mvn -PnativeTest clean test`.
- Coverage and Sonar: start `infrastructure/sonarqube-docker-compose.yaml`, then run `mvn clean install -Psonar`. The pom hardcodes credentials `admin`/`adminadmin`, so change the admin password to `adminadmin` on first login.

## Code and DB gotchas
- Flyway owns the schema (`src/main/resources/db/migration`). Hibernate uses `ddl-auto=validate`, so entity changes need a new `V*.sql` migration.
- Hikari `auto-commit=false` and `open-in-view=false` are intentional.
- Lombok and MapStruct share one annotation-processor path in the root pom (it includes `lombok-mapstruct-binding`), so keep all three entries when touching the compiler config.
- Native reflection hints are in `config/RestTeamRuntimeHints`. In Spring Framework 7 use `MemberCategory.ACCESS_DECLARED_FIELDS` (the OpenRewrite recipe wrongly produced `INVOKE_DECLARED_FIELDS`).
- Messages are i18n (`messages.properties`, `messages_es.properties`), so update both.
