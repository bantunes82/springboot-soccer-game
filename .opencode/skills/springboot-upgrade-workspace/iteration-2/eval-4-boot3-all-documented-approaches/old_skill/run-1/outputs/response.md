# Spring Boot 2.7 → 3.x upgrade: decision trace (simulated)

> **What is real and what is simulated:** I ran nothing. I did not execute Maven, Docker, SDKMAN, or curl, and I did not change any application files. Every outcome below is a simulated observation from `scenario.md`. Commands are listed in the order the skill workflow requires. When the scenario does not give the outcome of a required step, I mark it **NOT EVIDENCED** and do not guess a result.

---

## 1. Inspect the project

| # | Action | Observation (simulated) |
|---|---|---|
| 1.1 | Read every `README.md`, `AGENTS.md`, and `CLAUDE.md` in the repository | I found root `AGENTS.md`, `api/README.md`, and root `CLAUDE.md`. |
| 1.2 | Check the working tree (`git status`) | The tree is clean, so there are no user changes to preserve. |
| 1.3 | Resolve the effective Boot version from the root POM parent | `spring-boot-starter-parent:2.7.18` |
| 1.4 | Check the toolchain | `mvn` (no wrapper), JDK 21 (not GraalVM), Docker running, no SonarQube server running. |
| 1.5 | Check stable-release evidence | Fixture metadata: `2.7.18` is the latest stable 2.x and `3.5.12` is the latest stable 3.x. |
| 1.6 | Confirm the destination | You explicitly confirmed moving to 3.x. We are already on the latest stable 2.x, so the immediate stage is **2.7.18 → latest stable 3.x (3.5.12)** with the pinned 3.5 recipe. No major version is skipped. |

### Documented validation checklist (built before the upgrade)

| ID | Source → heading | Command(s) and working directory | Prerequisites | Success signal |
|---|---|---|---|---|
| V0 | Skill §3 step 2 | `mvn clean install` (reactor root) | None | Exit code 0, tests not skipped |
| V1 | `AGENTS.md` (root) | `mvn clean verify` from `api/` (integration tests are tagged and run in the `integration-test` phase) | Docker for integration tests | Exit code 0, unit and integration tests run |
| V2 | `api/README.md` → Running the application → Approach 1 | `mvn spring-boot:run` from `api/` | Docker and Compose (Postgres) | `curl --fail http://127.0.0.1:8081/api/actuator/health/readiness` succeeds |
| V3 | `CLAUDE.md` (root) | `curl --fail http://127.0.0.1:8081/api/v1/items` after starting the app, applied to **every** run approach (V2, V4, V5, V6) | A running app | HTTP 200 |
| V4 | `api/README.md` → Approach 2 | `mvn spring-boot:test-run` from `api/` | Docker (Testcontainers) | Readiness check plus V3 |
| V5 | `api/README.md` → Approach 3 | `mvn spring-boot:build-image`, then `docker compose -f ../infra/compose.yaml --profile api up` | Docker | Readiness check plus V3 |
| V6 | `api/README.md` → Approach 4 (native) | `sdk install java 21.0.2-graalce`, then `mvn -Pnative spring-boot:build-image` and `docker compose -f ../infra/compose.yaml --profile api up` | GraalVM 21 via SDKMAN, Docker | Readiness check plus V3 |
| V7 | `api/README.md` → Testing | `mvn -PnativeTest clean test` | GraalVM 21 | Exit code 0 |
| V8 | `api/README.md` → Static analysis | `docker compose -f ../infra/sonar.yaml up`, then `mvn clean install -Psonar` | A local SonarQube server (not running) | Analysis submitted to the local SonarQube |

V0 (`mvn clean install`) is required by the skill workflow, not by a project document. `mvn clean verify` (V1) is the documented project command, and it already covers the tagged integration tests, so no extra integration-test profile is needed.

---

## 2. Run the selected stage

```bash
mvn -U org.openrewrite.maven:rewrite-maven-plugin:6.46.1:run --define rewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:6.37.1 --define rewrite.activeRecipes=org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5 --define rewrite.exportDatatables=true
```

- **Simulated:** the recipe succeeded.

## 3. Inspect and validate

### 3.1 Diff and POM inspection
- `git diff` and the `api` effective POM (`mvn help:effective-pom` from `api/`).
- **Simulated:** the parent is now `3.5.12`, and the `api` effective POM confirms it. This matches the verified stable target.

### 3.2 Build and tests
- V0 `mvn clean install`: the scenario does not give an outcome, so this is **NOT EVIDENCED**. It must run before the stage can count as complete.
- V1 `mvn clean verify` from `api/`: **Simulated: PASSED** (unit and integration tests).

### 3.3 Documented run approaches
Before running these, I re-read every `README.md`, `AGENTS.md`, and `CLAUDE.md`. The scenario reports no changes to them, so no checklist items were added. Each approach runs on its own: start it in the background with logs captured, wait for readiness, run V3, then stop it and run Compose `down` before the next approach.

1. **V2, Approach 1** `mvn spring-boot:run`: **Simulated:** started. Readiness passed and `/api/v1/items` returned 200. I stopped it and Boot ran Compose `down`.
2. **V4, Approach 2** `mvn spring-boot:test-run`: **Simulated: FAILED** with `ClassNotFoundException: org.springframework.boot.testcontainers.service.connection.ServiceConnection`.
   - **Fix:** add the test-scoped dependency `org.springframework.boot:spring-boot-testcontainers` to `api/pom.xml`. Its version is managed by the Boot 3.5.12 parent.
   - **Rerun of Approach 2, simulated:** started. Readiness passed and `/v1/items` returned 200. I stopped it.
   - **Rerun of affected earlier items:** this fix changes the test classpath, so V1 `mvn clean verify` (and V0) must run again. The scenario gives **no rerun outcome**, so this is **NOT EVIDENCED**.
3. **V5, Approach 3** `mvn spring-boot:build-image`, then `docker compose -f ../infra/compose.yaml --profile api up`: **Simulated:** the image build and Compose startup passed. Readiness passed and `/v1/items` returned 200. Then `docker compose -f ../infra/compose.yaml --profile api down`.
4. **V6, Approach 4 (native):**
   - Prerequisite set up through the documented SDKMAN step: `sdk install java 21.0.2-graalce`, then use it for this shell. **Simulated:** installed.
   - `mvn -Pnative spring-boot:build-image`: **Simulated: FAILED** because a reflection hint is missing for `ItemDto`.
   - **Fix:** register reflection for `ItemDto` in the application's `RuntimeHintsRegistrar`, using `@ImportRuntimeHints` or `@RegisterReflectionForBinding(ItemDto.class)`.
   - **Rerun, simulated:** the native image builds. The Compose `--profile api up` command started it, readiness passed, and `/v1/items` returned 200. Then Compose `down`.
   - **Rerun of affected earlier items:** this fix changes main sources and AOT processing, so V1 (and V0) plus the JVM image (V5) should run again. The scenario gives **no rerun outcomes**, so these are **NOT EVIDENCED**.
5. **V7** `mvn -PnativeTest clean test` (GraalVM active): **Simulated: PASSED** after the hint fix.
6. **V8, Static analysis:** SonarQube is not running. The documented setup starts a Docker Compose service and `mvn clean install -Psonar` submits analysis to it. You have not yet been asked about this, so I have **not** started it. It is **BLOCKED pending your confirmation** (the question is below).

### 3.4 Cleanup
- I stopped only what this validation started: the app processes and the Compose `api` stacks. I left the SDKMAN GraalVM install in place and restored the default JDK for the shell. The final diff contains the recipe changes, the `spring-boot-testcontainers` test dependency, and the `ItemDto` runtime hint.

---

## 4. Final report

**The 2.7.18 → 3.5.12 stage is not yet ready.** The recipe applied cleanly. After two fixes, every run approach passed in the simulation. Three things remain: the SonarQube analysis needs your go-ahead, and the build/tests and JVM image were not rerun after the fixes.

### Version
- **Before:** Spring Boot 2.7.18, the latest stable 2.x according to the fixture metadata.
- **After:** **3.5.12**, confirmed in the `api` effective POM. The fixture metadata identifies 3.5.12 as the latest stable 3.x.

### Changes
- **Recipe:** pinned `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5` (rewrite-maven-plugin 6.46.1, rewrite-spring 6.37.1).
- **Manual fix 1:** added the test dependency `spring-boot-testcontainers`. Without it, `mvn spring-boot:test-run` failed with a `ClassNotFoundException` for `ServiceConnection`.
- **Manual fix 2:** added a reflection runtime hint for `ItemDto`, which the native image build needed.

### Validation (all results are simulated, nothing was really executed)

| ID | Source → heading | Command(s) | Result | Evidence |
|---|---|---|---|---|
| V0 | Skill workflow | `mvn clean install` | **NOT EVIDENCED** | No outcome in the scenario, before or after the fixes |
| V1 | `AGENTS.md` | `mvn clean verify` (from `api/`) | PASSED before the fixes; **rerun after the fixes NOT EVIDENCED** | Unit and integration tests passed |
| V2 | `api/README.md` → Approach 1 | `mvn spring-boot:run` | PASSED | Readiness OK, `/v1/items` returned 200 |
| V3 | `CLAUDE.md` | `curl --fail …/api/v1/items` | PASSED for V2, V4, V5, V6 | HTTP 200 in each approach |
| V4 | `api/README.md` → Approach 2 | `mvn spring-boot:test-run` | FAILED, then fixed, then PASSED | `ClassNotFoundException` fixed by adding `spring-boot-testcontainers`; checks OK after the fix |
| V5 | `api/README.md` → Approach 3 | `mvn spring-boot:build-image` + `docker compose -f ../infra/compose.yaml --profile api up` | PASSED before the hint fix; **rerun NOT EVIDENCED** | Image built, readiness OK, `/v1/items` returned 200 |
| V6 | `api/README.md` → Approach 4 | `sdk install java 21.0.2-graalce`; `mvn -Pnative spring-boot:build-image` + Compose `up` | FAILED, then fixed, then PASSED | Missing `ItemDto` hint fixed; checks OK after the fix |
| V7 | `api/README.md` → Testing | `mvn -PnativeTest clean test` | PASSED | Passed with GraalVM after the fix |
| V8 | `api/README.md` → Static analysis | `docker compose -f ../infra/sonar.yaml up`; `mvn clean install -Psonar` | **BLOCKED** | SonarQube is not running; needs your confirmation to start it and submit analysis |

### Status
The stage is **incomplete**. These items must pass, or you must explicitly waive them, before the 3.x stage counts as done:
1. V8 SonarQube analysis (blocked, waiting for your confirmation).
2. Reruns after the fixes: `mvn clean install`, `mvn clean verify`, and Approach 3 (JVM image).

### Questions for you
1. **May I start the local SonarQube** (`docker compose -f ../infra/sonar.yaml up` from `api/`) and run `mvn clean install -Psonar`, which submits analysis to it? You can also waive this item for this stage.
2. Once V8 is resolved, I will run the outstanding reruns (V0, V1, V5).

I am **not** proposing the next major stage (3.x → 4.x) yet. After every item above has passed or been waived, I will ask whether you want more validation and whether to go ahead with the Spring Boot 4.x stage.
