# Spring Boot 2.7 → 3.x upgrade: decision trace (simulated)

> **Execution disclaimer:** I did not run any of these commands. No Maven, Docker, SDKMAN, or curl command was run, and no application file was changed. Every "result" below comes from the simulated outcomes in `scenario.md`. A result marked *simulated* is a scenario input, not something I observed. Where the scenario gives no outcome, I mark the item **NOT EVIDENCED** or **BLOCKED** instead of assuming it passed.

## 1. Inspection

| # | Decision / observation | Source |
| --- | --- | --- |
| 1.1 | Effective Boot version is `2.7.18`, set by root parent `spring-boot-starter-parent:2.7.18`. | scenario (simulated) |
| 1.2 | `2.7.18` is the latest stable 2.x and `3.5.12` is the latest stable 3.x. The source is the fixture's release metadata; I did not check live data. | scenario (simulated) |
| 1.3 | Launcher is `mvn` (no wrapper). The active JDK is 21 (not GraalVM). Docker is running. The working tree is clean, so there are no user changes to preserve. SonarQube is not running. | scenario (simulated) |
| 1.4 | The user explicitly confirmed moving to 3.x. The project is on the latest stable 2.x, so the immediate stage is **2.7.18 → latest stable 3.x (3.5.12)** using the 3.5 recipe. This request authorizes only this stage, not 4.x. | user request + skill §2 |
| 1.5 | Read every README.md, AGENTS.md, and CLAUDE.md the scenario lists: root `AGENTS.md`, `api/README.md`, and root `CLAUDE.md`. From them I built the documented validation checklist (below). | scenario |
| 1.6 | Smoke-test plan. Base URL is `http://127.0.0.1:8081/api`. Planned checks: readiness `/actuator/health/readiness`, REST `/v1/items` (from CLAUDE.md), `/v3/api-docs`, `/swagger-ui/index.html`, a security check, and a log scan. The scenario only gives evidence for readiness and `/v1/items`. It has no evidence on whether the app exposes Swagger/OpenAPI, a security model, a web UI, or other interfaces. Those checks are listed as **NOT EVIDENCED** rather than claimed. | skill §1/§3a |
| 1.7 | The scenario provides no pre-upgrade baseline smoke run, such as a saved OpenAPI document or sample responses. I state this and continue, so no baseline diff is possible. | skill §1 |

### Documented validation checklist

| ID | Source (file → heading) | Command(s), working directory | Prerequisites | Success signal |
| --- | --- | --- | --- | --- |
| C1 | `AGENTS.md` (root) | `mvn clean verify` in `api/` (integration tests are tagged and run in the `integration-test` phase) | Docker for integration tests | exit 0, unit and integration tests pass |
| C2 | skill §3 step 2 | `mvn clean install` from the reactor root | — | exit 0 |
| A1 | `api/README.md` → Running the application → Approach 1 | `mvn spring-boot:run` in `api/` | Docker/Compose (Postgres) | `curl --fail http://127.0.0.1:8081/api/actuator/health/readiness` |
| A2 | `api/README.md` → Approach 2 | `mvn spring-boot:test-run` in `api/` | Docker (Testcontainers) | same readiness check |
| A3 | `api/README.md` → Approach 3 | `mvn spring-boot:build-image`, then `docker compose -f ../infra/compose.yaml --profile api up` | Docker | same readiness check |
| A4 | `api/README.md` → Approach 4 (native) | `sdk install java 21.0.2-graalce` (and use it), `mvn -Pnative spring-boot:build-image`, then `docker compose -f ../infra/compose.yaml --profile api up` | GraalVM 21, Docker | same readiness check |
| T1 | `api/README.md` → Testing | `mvn -PnativeTest clean test` | GraalVM 21 | exit 0 |
| S1 | `api/README.md` → Static analysis | `docker compose -f ../infra/sonar.yaml up`, then `mvn clean install -Psonar` | local SonarQube; publishes analysis | exit 0, analysis submitted |
| X1 | `CLAUDE.md` (root) | after any start: `curl --fail http://127.0.0.1:8081/api/v1/items` | running app | HTTP 200 (and non-empty item content per skill §3a) |

## 2. Recipe (stage: Spring Boot 3.x)

```bash
mvn -U org.openrewrite.maven:rewrite-maven-plugin:6.46.1:run --define rewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:6.37.1 --define rewrite.activeRecipes=org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5 --define rewrite.exportDatatables=true
```

- *Simulated:* the recipe succeeds. The parent becomes `3.5.12`, and the `api` effective POM confirms this. The result matches the verified stable target, so there is no version inconsistency.
- I would inspect the diff after the recipe. The scenario does not supply its contents.

## 3. Ordered validation trace

Every run uses this smoke script, defined once:

```bash
BASE=http://127.0.0.1:8081/api
curl --fail -sS -w '%{http_code}\n' $BASE/actuator/health/readiness
curl --fail -sS -w '%{http_code}\n' $BASE/v1/items
curl --fail -sS -w '%{http_code}\n' $BASE/v3/api-docs        # if springdoc is present
curl --fail -sS -w '%{http_code}\n' $BASE/swagger-ui/index.html  # if springdoc is present
# protected route without credentials -> expect 401/403 (if security exists); scan logs for errors/deprecations
```

| Step | Action / command | Simulated result | Decision |
| --- | --- | --- | --- |
| 1 | C1: `mvn clean verify` (`api/`) | PASS (unit and integration tests) | Next, run a smoke test with A1, the primary approach. |
| 2 | C2: `mvn clean install` | **NOT EVIDENCED.** The scenario gives no outcome. | Must be run. I can't claim a pass. |
| 3 | A1: `mvn spring-boot:run`, wait for readiness, smoke test, stop (Compose `down` on shutdown) | readiness PASS, `/v1/items` 200 PASS | This smoke run also covers C1. Swagger, security, and log checks are NOT EVIDENCED. |
| 4 | A2: `mvn spring-boot:test-run` | **FAIL:** `ClassNotFoundException: org.springframework.boot.testcontainers.service.connection.ServiceConnection` | Cause: Boot 3.1+ `@ServiceConnection` needs `spring-boot-testcontainers`, and the recipe didn't add it. |
| 4a | **Fix:** add `org.springframework.boot:spring-boot-testcontainers` with `<scope>test</scope>` to `api/pom.xml` (version managed by the Boot parent) | — | The test classpath changed. |
| 4b | Rerun A2 and smoke test | readiness PASS, `/v1/items` PASS | A2 passes. Fix 4a changed the test classpath, so C1 (integration tests) must be rerun. The scenario gives no rerun outcome: **NOT EVIDENCED.** |
| 5 | A3: `mvn spring-boot:build-image`, then `docker compose -f ../infra/compose.yaml --profile api up`, smoke test, `docker compose ... down` | build and startup PASS, readiness PASS, `/v1/items` PASS | The image was actually started and smoke-tested. |
| 6 | A4 prereq: `sdk install java 21.0.2-graalce`, `sdk use java 21.0.2-graalce` | GraalVM installed | Uses the documented SDKMAN setup. No sudo needed. |
| 6a | A4: `mvn -Pnative spring-boot:build-image` | **FAIL:** missing reflection hint for `ItemDto` | AOT/native needs an explicit runtime hint. |
| 6b | **Fix:** register `ItemDto` for reflection, e.g. `hints.reflection().registerType(ItemDto.class, MemberCategory.values())` in a `RuntimeHintsRegistrar` imported via `@ImportRuntimeHints` (or `@RegisterReflectionForBinding(ItemDto.class)`) | — | This changes main code, which affects every earlier item. |
| 6c | Rerun the A4 build, Compose `--profile api up`, smoke test, `down` | build PASS, readiness PASS, `/v1/items` PASS | A4 passes. |
| 7 | T1: `mvn -PnativeTest clean test` (GraalVM) | PASS | No code changed after 6b, so this can share the A4 smoke evidence, but a JVM primary-approach smoke run after the fix is still needed (see step 8). |
| 8 | Reruns required by fix 6b (main code change): C1, C2, A1, A2, A3, each with a smoke test | **NOT EVIDENCED.** The scenario provides no rerun outcomes. | Must be run before the stage counts as complete. |
| 9 | S1: `docker compose -f ../infra/sonar.yaml up`, then `mvn clean install -Psonar` | **BLOCKED.** SonarQube is not running, and the command publishes results to a SonarQube service. | Following the skill, I must **ask you** before starting SonarQube and submitting analysis. I have not asked yet. |
| 10 | Cleanup: stop app processes and Compose stacks I started (`docker compose ... down`) | — | The installed GraalVM SDK stays. Only validation resources get removed. |

## 4. Final report

**Version:** Spring Boot `2.7.18` → `3.5.12` (stage: 2.x → 3.x). Fixture metadata identifies 3.5.12 as the latest stable 3.x, and the `api` effective POM confirms it *(simulated)*.

**Changes:**
- OpenRewrite `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5` (rewrite-maven-plugin 6.46.1, rewrite-spring 6.37.1).
- Manual fix 1: added the test-scoped `spring-boot-testcontainers` dependency to `api/pom.xml`.
- Manual fix 2: added a reflection runtime hint for `ItemDto`.
- *(These are the fixes I would make. No file was changed in this evaluation.)*

**Validation checklist:**

| Item | Source | Command | Result | Smoke test | Evidence (simulated) |
| --- | --- | --- | --- | --- | --- |
| C1 | AGENTS.md | `mvn clean verify` | PASSED initially; **rerun pending** after fixes 1 and 2 | via A1 (before fixes) | pass reported in scenario; no rerun data |
| C2 | skill §3 | `mvn clean install` | **NOT RUN / NOT EVIDENCED** | — | none |
| A1 | README → Approach 1 | `mvn spring-boot:run` | PASSED before fixes; **rerun pending** after fix 2 | readiness ✓, `/v1/items` ✓ | scenario step 3 |
| A2 | README → Approach 2 | `mvn spring-boot:test-run` | FAILED → fixed → PASSED; **rerun pending** after fix 2 | readiness ✓, `/v1/items` ✓ | scenario step 4 |
| A3 | README → Approach 3 | `build-image` + Compose `--profile api` | PASSED; **rerun pending** after fix 2 | readiness ✓, `/v1/items` ✓ | scenario step 5 |
| A4 | README → Approach 4 | SDKMAN GraalVM + `-Pnative build-image` + Compose | FAILED → fixed → PASSED | readiness ✓, `/v1/items` ✓ | scenario step 6 |
| T1 | README → Testing | `mvn -PnativeTest clean test` | PASSED | covered by A4 run (no changes in between) | scenario step 6 |
| S1 | README → Static analysis | Sonar Compose + `mvn clean install -Psonar` | **BLOCKED.** SonarQube is down, and the command publishes externally, so it needs your approval. | — | scenario step 7 |
| X1 | CLAUDE.md | `curl --fail …/api/v1/items` | included in every smoke run above | — | — |

**Smoke tests:**
- Evidenced in every simulated run (A1, A2, A3, A4): readiness and `GET /api/v1/items` returned 200.
- **NOT EVIDENCED:** `/v3/api-docs`, Swagger UI, the unauthenticated-access check on protected routes, the authenticated write round trip, content assertions on `/v1/items` (beyond the 200 status), and log scans for errors and deprecations. The scenario doesn't say whether these interfaces exist.
- There was no pre-upgrade baseline, so I couldn't compare API shapes against one.

**Status: NOT READY.** The upgrade compiles, and every run approach worked in simulation. But the stage isn't complete because:
1. `mvn clean install` (C2) has no result.
2. After the `ItemDto` hint changed main code, C1, A1, A2, and A3 have to be rerun, with smoke tests. After the Testcontainers dependency fix, C1 has to be rerun.
3. The full smoke plan (API docs, Swagger, security, logs) has no evidence.
4. S1 (SonarQube) is BLOCKED.

**Questions for you:**
- **SonarQube:** may I start it locally (`docker compose -f ../infra/sonar.yaml up`) and run `mvn clean install -Psonar`, which submits analysis to that local SonarQube? Or do you want to waive this item?
- Once the pending reruns pass and S1 has passed or you've waived it, I'll report back. I won't propose or start the Spring Boot 4.x stage until then, and only once you confirm it.
