# Repository guidance

## Toolchain and command location
- The root POM is an aggregator/parent; `rest-team` is the only application module. Run the commands below from `rest-team/` unless stated otherwise: the configured Compose path is relative to that directory.
- At the repository root, use SDKMAN (`sdk env install` if needed, then `sdk env`). `.sdkmanrc` selects OpenJDK `21.0.2-open` and Maven `3.9.16`; verify with `mvn -v`. The file currently has whitespace after `java=` and is gitignored, so check it locally if activation fails. There is no Maven wrapper.
- The root POM currently uses Spring Boot parent `3.2.5`, but separately pins the Boot Maven plugin to `3.5.16` via `springboot-maven-plugin.version`. Check both when changing Boot versions.

## Running the application
The detailed workflows are in `rest-team/README.md`; their commands map to these configurations:

| Workflow | Command from `rest-team/` | Prerequisites/behavior |
| --- | --- | --- |
| Development with Compose | `mvn spring-boot:run` | Docker daemon and Compose; Boot starts PostgreSQL and Keycloak and runs Compose `down` on shutdown. |
| Development with observability | `mvn spring-boot:run -Pobservability` | Also starts the telemetry stack; the Maven profile downloads and attaches the OpenTelemetry agent and activates the Spring profile. |
| Development with Testcontainers | `mvn spring-boot:test-run` | Docker daemon; `TestRestTeamApplication` activates the `test` Spring profile (disabling Compose) and imports `TestContainersConfiguration`. |
| JVM container image | `mvn spring-boot:build-image` | Docker daemon; builds `rest-team:1.0` using buildpacks. |
| Instrumented container image | `mvn spring-boot:build-image -Pobservability` | Builds the same image tag with OpenTelemetry support. |
| Native container image | `mvn -Pnative spring-boot:build-image` | Native buildpack workflow documented in the module README. |

- After a regular/native image build: `docker-compose -f ../infrastructure/docker-compose-linux.yaml --profile rest-team up`. After an observability image build, use `--profile rest-team-observability` instead. Both app services use `network_mode: host` and loopback addresses; this workflow assumes host networking support despite the published port entries.
- Native workflows in the README use GraalVM 21; the SDKMAN-pinned OpenJDK is not GraalVM. Local native tests require GraalVM/native-image.
- Default application base URL: `http://127.0.0.1:8081/rest-team`; Swagger: `/swagger-ui/index.html`; readiness: `/actuator/health/readiness` (checks both PostgreSQL and Keycloak).
- GET `/v1/teams/**` is public; writes require the Keycloak `team` role. Local token helper: `infrastructure/keycloak/scripts/get-accessToken.sh` (paths here are repository-relative). Compose imports `infrastructure/keycloak/realms/team-realm.json`; Testcontainers imports the separate realm under `rest-team/src/test/resources/keycloak/realms/`.

## Verification
- Fast unit suite: `mvn test`. From the repository root, `mvn -pl rest-team test` selects the module.
- Focused unit test: `mvn -Dtest=TeamServiceTest test`; a single method: `mvn '-Dtest=TeamServiceTest#findRandom_ReturnsTeam' test`.
- Full JVM verification: `mvn clean verify`. Integration tests are selected by JUnit `@Tag("integration")`, not filename alone: the root POM excludes the tag during `test`, then runs it through Surefire's `integration-tests` execution in the `integration-test` phase. `mvn test` does not cover them.
- Focused integration test: `mvn test-compile surefire:test@integration-tests -Dtest=HealthIntegrationTest`. Docker must be running; `TestContainersConfiguration` provisions `postgres:13.2` and `quay.io/keycloak/keycloak:12.0.4`, with dynamic connection details. A separately running Compose stack is not required.
- Test Flyway seed data is `rest-team/src/test/resources/db/migration/V9999__test-data.sql`; it is also on the classpath during `spring-boot:test-run`.
- Coverage: `mvn clean verify -Psonar`; reports are under `target/site/jacoco-ut`, `jacoco-it`, and merged `jacoco`. `mvn clean install -Psonar` additionally submits analysis to SonarQube at port 9000 because the scanner is bound to `install`. SonarQube startup is documented in the module README; its `sysctl` commands are Linux-specific.
- Native tests: `mvn -PnativeTest clean test`. Several Mockito-based test classes have `@DisabledInNativeImage`, so this does not provide the same coverage as JVM verification.

## Code and fixture constraints
- Schema changes belong in `rest-team/src/main/resources/db/migration/`: Flyway applies migrations before Hibernate validates the schema (`ddl-auto=validate`).
- MapStruct mappers use Spring components via `SpringMappingConfig`; the root compiler configuration combines MapStruct, Lombok, and `lombok-mapstruct-binding` annotation processors. Mapper implementations are generated during compilation.
- Validation uses `messages.properties` and `messages_es.properties`; integration tests assert Spanish responses too. Native resource registration for these bundles lives in `RestTeamRuntimeHints`.
- `.opencode/skills/springboot-upgrade/evals/README.md` describes synthetic agent-workflow evaluations, not application validation. Its fixture versions and command outcomes are simulated; do not use them as evidence that this application builds or runs.
