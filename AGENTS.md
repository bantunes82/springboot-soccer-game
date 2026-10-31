# AGENTS.md

## Toolchain
- `.sdkmanrc`: Java `21.0.2-open`, Maven `3.9.16`. Run `sdk env` before building. Spring Boot 3.2.5. No Maven wrapper; use `mvn`.
- Native builds and `nativeTest` need GraalVM CE 21 (e.g. 21.0.2+13.1) installed via SDKMAN, not the `.sdkmanrc` JDK.
- Docker must be running for everything except plain unit tests.

## Layout
- Root `pom.xml` is an aggregator (`super-parent-root`) with one module, `rest-team` (CRUD API for soccer teams). Its README is the main runbook.
- `infrastructure/`: docker-compose files, Keycloak scripts, and observability configs (Prometheus, Tempo, Loki, Grafana, OTel collector). There is no Dockerfile. Images are built with buildpacks.
- Annotation processing uses Lombok together with MapStruct (see the root `pom.xml` for `lombok-mapstruct-binding`). Generated mappers only exist after compiling.

## Run (from `rest-team/`)
- `mvn spring-boot:run` uses Spring Boot Docker Compose support and starts Postgres and Keycloak.
- `mvn spring-boot:run -Pobservability` also starts the OTel collector, Prometheus, Loki, Tempo and Grafana.
- `mvn spring-boot:test-run` uses Testcontainers and needs only Docker.
- Container image: `mvn spring-boot:build-image` (add `-Pobservability` for the observability variant, or use `mvn -Pnative spring-boot:build-image` for a native image).
- Run the image with `docker-compose -f ../infrastructure/docker-compose-linux.yaml --profile rest-team up`. The profile is `rest-team-observability` for the observability image. The native image uses the plain `rest-team` profile.
- The compose file is named `-linux`. It was written for Linux and may need adjustments on macOS.
- Swagger UI: http://127.0.0.1:8081/rest-team/swagger-ui/index.html. Keycloak console: http://127.0.0.1:8080/auth/ (admin/admin). Grafana: http://localhost:3000 (admin/admin).
- Swagger OAuth2 password flow users: `teamuser` / `teamuser` (in the `team` role) and `test` / `test` (not in the role, useful for 403 checks).

## Test and verify
- `mvn test` runs unit tests only. Surefire excludes the JUnit tag `integration` by default.
- Integration tests (tag `integration`) run in the `integration-test` phase, so use `mvn verify`. `mvn test` will not exercise them.
- Integration test classes start their Testcontainers once per class, before the test methods run, and tear them down afterwards.
- Single test: `mvn test -Dtest=ClassName#method`. A tagged integration test is excluded by `excludedGroups`, so run it through `mvn verify` and the `integration-tests` execution.
- Native tests: `mvn -PnativeTest clean test` (needs GraalVM).
- Sonar and Jacoco: `mvn clean install -Psonar` requires SonarQube running at 127.0.0.1:9000. Start it with `docker-compose -f ../infrastructure/sonarqube-docker-compose.yaml up`. The `sonar.login` and `sonar.password` credentials are in the root `pom.xml`. The `sonar` profile merges the unit and integration Jacoco reports.
- There are no lint, format or CI configs in the repo.
