# Spring Boot 2.7 → 3.x stage: decision trace (simulated)

> **Evidence boundary.** Everything below comes only from the isolated fixture
> `scenario.md`. I did **not** run Maven, Docker, curl, or SDKMAN, and I did not
> change any application files. Each "Observed" line is a **simulated outcome**
> from the fixture. Lines marked **No fixture evidence** describe checks the skill
> requires but the fixture does not give a result for. I do not count those as
> passed.

## 1. Inspection (simulated)

| Fact | Source |
| --- | --- |
| Effective Boot version: `spring-boot-starter-parent:2.7.18` | scenario: initial inspection |
| Latest stable 2.x = `2.7.18`; latest stable 3.x = `3.5.12` | fixture release metadata (not real-world release data) |
| User explicitly confirmed moving to 3.x | user request + scenario |
| Launcher `mvn` (no wrapper); JDK 21, not GraalVM; Docker running; clean tree; SonarQube **not** running | scenario |

**Decision:** The service is already on the latest stable 2.x, and you confirmed
the move to the next major. That makes **2.x → 3.x (target 3.5.12)** the only
stage I execute here. I use the pinned 3.5 recipe. I will not go on to 4.x
without asking you first.

### Documented validation checklist (built before running the recipe)

| # | Source → heading | Command(s) / working directory | Prerequisites | Success signal |
| --- | --- | --- | --- | --- |
| C1 | `AGENTS.md` (root) | `mvn clean verify` in `api/` (integration tests are tagged and run in the `integration-test` phase) | Docker for integration tests | exit 0; unit and integration tests pass |
| C2 | Skill §3 step 2 | `mvn clean install` from the reactor root | as C1 | exit 0 |
| A1 | `api/README.md` → Running, Approach 1 | `mvn spring-boot:run` in `api/` | Docker + Compose (Postgres) | readiness 200 |
| A2 | `api/README.md` → Running, Approach 2 | `mvn spring-boot:test-run` in `api/` | Docker (Testcontainers) | readiness 200 |
| A3 | `api/README.md` → Running, Approach 3 | `mvn spring-boot:build-image`, then `docker compose -f ../infra/compose.yaml --profile api up` | Docker | image built; readiness 200 |
| A4 | `api/README.md` → Running, Approach 4 (native) | `sdk install java 21.0.2-graalce`, then `mvn -Pnative spring-boot:build-image`, then the same Compose command | GraalVM 21 via SDKMAN | native image built; readiness 200 |
| T1 | `api/README.md` → Testing | `mvn -PnativeTest clean test` | GraalVM | exit 0 |
| S1 | `api/README.md` → Static analysis | `docker compose -f ../infra/sonar.yaml up`, then `mvn clean install -Psonar` | Local SonarQube; the command submits analysis | exit 0; analysis accepted |
| X | `CLAUDE.md` (root) | After any start: `curl --fail http://127.0.0.1:8081/api/v1/items` | running app | 200 |

### Smoke-test plan (one reusable script, run after every item)

1. Readiness: `curl --fail -sS -w '%{http_code}' http://127.0.0.1:8081/api/actuator/health/readiness`. Expect 200 with status UP, and every component UP.
2. REST: `curl --fail -sS -w '%{http_code}' http://127.0.0.1:8081/api/v1/items`. Expect 200, JSON content type, and a non-empty list (the CLAUDE.md check).
3. API docs and Swagger UI (`/api/v3/api-docs`, `/api/swagger-ui/index.html`), security behavior (unauthenticated write gets 401/403), any web UI, other actuator endpoints, and a log scan.
   - The fixture does not show whether these interfaces exist, so I record them as **"existence not established by fixture"**.
   - On a real run, I would derive them from controllers and configuration and add them here.

**Baseline:** The fixture has no pre-upgrade smoke run or OpenAPI snapshot, so I
could not capture a baseline. Post-upgrade results have nothing to be compared
against.

## 2. Ordered trace

1. **Recipe** (commands not executed):
   ```bash
   mvn -U org.openrewrite.maven:rewrite-maven-plugin:6.46.1:run --define rewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:6.37.1 --define rewrite.activeRecipes=org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5 --define rewrite.exportDatatables=true
   mvn help:effective-pom -pl api
   ```
   - Observed: the recipe succeeds and the parent becomes `3.5.12`.
   - Observed: the `api` effective POM confirms `3.5.12`, which matches the stable target.
   - Next: inspect the diff.

2. **C1 – `mvn clean verify`** (in `api/`)
   - Observed: passes, including unit and integration tests.
   - Test-only item, so it needs a follow-up smoke run with the primary approach. Step 3 covers this.

3. **A1 – `mvn spring-boot:run`**
   - Observed: starts, readiness 200, `/v1/items` 200. This also serves as the smoke run for C1.
   - No fixture evidence: Swagger/OpenAPI, security, and log-scan checks.
   - Then stop the app. Compose `down` runs on shutdown.

4. **A2 – `mvn spring-boot:test-run`**
   - Observed: **fails** with `ClassNotFoundException: org.springframework.boot.testcontainers.service.connection.ServiceConnection`.
   - Fix (described, not applied): add the test-scoped dependency `org.springframework.boot:spring-boot-testcontainers`. Its version is managed by the 3.5.12 parent.
   - Rerun A2. Observed: starts, readiness 200, `/v1/items` 200.
   - Reruns needed because the fix changed test dependencies:
     - **C1** (`mvn clean verify`) must be rerun because test dependencies changed.
     - **A1** must be rerun as its smoke run.
     - **No fixture evidence** for either rerun, so they are pending.

5. **A3 – `mvn spring-boot:build-image`, then `docker compose -f ../infra/compose.yaml --profile api up`**
   - Observed: build and startup pass; readiness 200, `/v1/items` 200. The image was started, not just built.
   - Then `docker compose -f ../infra/compose.yaml --profile api down`.

6. **A4 – native image**
   - `sdk install java 21.0.2-graalce` (the documented SDKMAN setup), then `mvn -Pnative spring-boot:build-image`.
   - Observed: build **fails** with a missing reflection hint for `ItemDto`.
   - Fix (described, not applied): register `ItemDto` for reflection in the app's `RuntimeHintsRegistrar`, or use `@RegisterReflectionForBinding(ItemDto.class)`.
   - Rebuild. Observed: builds; the same Compose command starts it; readiness 200, `/v1/items` 200. Then Compose `down`.
   - Reruns needed because the hint is main code:
     - **C1, A1, A3** must be rerun (the A3 image must be rebuilt).
     - **No fixture evidence** for these reruns, so they are pending.

7. **T1 – `mvn -PnativeTest clean test`**
   - Observed: passes.
   - It is test-only and needs a follow-up smoke run with A1. **No fixture evidence** for that run.

8. **C2 – `mvn clean install`**
   - Required by the skill. **No fixture evidence**, so it is pending.

9. **S1 – SonarQube**
   - Observed: SonarQube is not running, and you have not been asked about it yet.
   - The item means starting an extra service and submitting analysis, so I need your approval first.
   - Status: **BLOCKED pending your decision**. I did not skip it silently.

10. **Cleanup:** stop only the processes and containers started for validation (Compose `down` for each profile). SDKMAN GraalVM stays installed.

## 3. Final report

**Version:** 2.7.18 → **3.5.12**, confirmed in the effective POM.
- Stage: Boot 2.x → 3.x.
- Stable-release evidence: fixture metadata only.

**Changes:**
- Recipe `UpgradeSpringBoot_3_5` (rewrite-spring 6.37.1).
- Manual fix 1: added test dependency `spring-boot-testcontainers`.
- Manual fix 2: added a native runtime reflection hint for `ItemDto`.
- Both fixes are described only; no files were changed.

| Item | Source | Command | Result | Smoke test | Evidence |
| --- | --- | --- | --- | --- | --- |
| C1 | AGENTS.md | `mvn clean verify` | PASSED initially (simulated); **rerun pending** after both fixes | Readiness + items via A1: passed (simulated, before fixes) | fixture outcome 2–3 |
| C2 | Skill §3 | `mvn clean install` | **NOT RUN – no evidence** | pending | — |
| A1 | README Approach 1 | `mvn spring-boot:run` | PASSED (simulated, before fixes); **rerun pending** | readiness 200, `/v1/items` 200 | fixture outcome 3 |
| A2 | README Approach 2 | `mvn spring-boot:test-run` | FAILED → fixed → PASSED (simulated) | readiness 200, `/v1/items` 200 | fixture outcome 4 |
| A3 | README Approach 3 | `build-image` + Compose `--profile api up` | PASSED (simulated, before native fix); **rerun pending** | readiness 200, `/v1/items` 200 | fixture outcome 5 |
| A4 | README Approach 4 | SDKMAN GraalVM + `-Pnative build-image` + Compose | FAILED → fixed → PASSED (simulated) | readiness 200, `/v1/items` 200 | fixture outcome 6 |
| T1 | README Testing | `mvn -PnativeTest clean test` | PASSED (simulated) | follow-up A1 smoke run **pending** | fixture outcome 6 |
| S1 | README Static analysis | `docker compose -f ../infra/sonar.yaml up`; `mvn clean install -Psonar` | **BLOCKED – needs your approval** | pending | fixture outcome 7 |

**Smoke tests:**
- Executed (simulated) on A1–A4:
  - `GET /api/actuator/health/readiness`: 200
  - `GET /api/v1/items`: 200
- Not established by the fixture: OpenAPI/Swagger UI, security (401/403 and authenticated writes), web UI, other actuator endpoints, and log scans.
- No baseline was available for comparison.

**Status:** **Not ready.**
- S1 is blocked.
- C2 has not been run.
- C1, A1, A3, and T1's smoke run still need to be rerun after the two fixes.
- Several smoke-test interfaces are unverified.

I am not proposing the 4.x stage.

**Questions for you:**
1. May I start SonarQube locally (`docker compose -f ../infra/sonar.yaml up`) and run `mvn clean install -Psonar`, which submits analysis to it? Or do you waive that item?
2. Once S1 is resolved, I will:
   - rerun the pending items (C1, C2, A1, A3) and T1's follow-up smoke run;
   - extend the smoke test to every interface the code exposes;
   - report back.

   Only after that will I ask whether you want more validation or to move on to 4.x.
