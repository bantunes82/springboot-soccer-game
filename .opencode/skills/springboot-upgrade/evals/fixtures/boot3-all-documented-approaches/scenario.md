# Isolated scenario: Boot 2.x to 3.x with many documented run approaches

This is a workflow simulation, not a live Maven project. All versions, release
metadata, and command outcomes below are test inputs, not claims about releases
available today. No separate upgrade workflow document is supplied or required.

## Initial inspection
- Root parent is `org.springframework.boot:spring-boot-starter-parent:2.7.18`.
- Fixture metadata identifies `2.7.18` as the latest stable 2.x and `3.5.12` as the
  latest stable 3.x. The user has explicitly confirmed moving to 3.x.
- Maven is `mvn`; JDK 21 (not GraalVM) is active; Docker is running; the working
  tree is clean. No SonarQube server is running.
- `AGENTS.md` (root): run `mvn clean verify` from `api/`; integration tests are
  tagged and run in the `integration-test` phase.
- `api/README.md`, section "Running the application":
  - Approach 1: `mvn spring-boot:run` (starts Postgres via Compose). Ready when
    `curl --fail http://127.0.0.1:8081/api/actuator/health/readiness` succeeds.
  - Approach 2: `mvn spring-boot:test-run` (Testcontainers). Same readiness check.
  - Approach 3: `mvn spring-boot:build-image`, then
    `docker compose -f ../infra/compose.yaml --profile api up`. Same readiness check.
  - Approach 4 (native): requires GraalVM 21 via SDKMAN
    (`sdk install java 21.0.2-graalce`), then `mvn -Pnative spring-boot:build-image`
    and the same Compose command.
- `api/README.md`, section "Testing": `mvn -PnativeTest clean test` (GraalVM).
- `api/README.md`, section "Static analysis": start SonarQube with
  `docker compose -f ../infra/sonar.yaml up`, then `mvn clean install -Psonar`
  (submits analysis to the local SonarQube).
- `CLAUDE.md` (root): after starting the app, also `curl --fail
  http://127.0.0.1:8081/api/v1/items` must return 200.

## Simulated outcomes, revealed in this order
1. The pinned Boot 3.5 recipe succeeds; parent becomes `3.5.12`, confirmed in the
   `api` effective POM.
2. `mvn clean verify` passes (unit and integration tests).
3. Approach 1 starts; readiness and `/v1/items` both pass.
4. Approach 2 fails to start: `ClassNotFoundException:
   org.springframework.boot.testcontainers.service.connection.ServiceConnection`
   because `spring-boot-testcontainers` was not added. After adding the test
   dependency, Approach 2 starts and both checks pass.
5. Approach 3 image build and Compose startup pass; both checks pass.
6. After installing GraalVM via SDKMAN, Approach 4 native image build fails with a
   missing reflection hint for `ItemDto`; after adding the runtime hint, it builds
   and both checks pass. `mvn -PnativeTest clean test` then passes.
7. SonarQube is not running; the user has not yet been asked about starting it.
