# Spring Boot 2.7 → 3.x: decision trace for this stage (simulated)

> **What counts as evidence:** Every outcome below comes from the simulated fixture
> `evals/fixtures/boot3-all-documented-approaches/scenario.md`. **I ran no Maven, Docker, curl or SDKMAN
> commands, and I changed no application files.** "Simulated: PASSED" means the scenario says the step
> passed. "Not in evidence" means the skill requires the step but the scenario gives no result for it,
> so I don't claim it passed.

## 1. Inspection (simulated observations)

| # | Decision / observation | Source |
| --- | --- | --- |
| 1 | Effective Boot version is `spring-boot-starter-parent:2.7.18`. | scenario: Initial inspection |
| 2 | Fixture metadata says `2.7.18` is the latest stable 2.x and `3.5.12` is the latest stable 3.x. That means the 2.x stabilization stage is already done. You explicitly confirmed moving to 3.x, so the immediate stage is **2.7.18 → latest stable 3.x (3.5.12) using the 3.5 recipe**. There is no major skip and no 4.x step. | scenario + skill §2 table |
| 3 | Toolchain: `mvn` (no wrapper), JDK 21 (not GraalVM), Docker running, clean working tree (so no user changes need to be preserved), no SonarQube running. | scenario |
| 4 | I read every README/AGENTS/CLAUDE file the scenario lists and built the documented validation checklist below. All alternatives are included; none of them is optional. | skill §1 |
| 5 | Smoke-test plan. The documented interfaces are readiness `http://127.0.0.1:8081/api/actuator/health/readiness` and `GET http://127.0.0.1:8081/api/v1/items` (CLAUDE.md). The skill also requires checking API docs/Swagger, security, web UI, other interfaces and logs, all derived from the code. The scenario gives no code or config for these, so they are **not in evidence** and must be derived from the source in a real run. | skill §1, §3a |
| 6 | Baseline smoke run before the recipe: **not in evidence**. In a real run I would capture `/v3/api-docs` and sample responses from Approach 1 on 2.7.18. Without it, there is no before/after comparison. | skill §1 |

### Documented validation checklist

| ID | Source → heading | Command(s) (working dir) | Prerequisites | Success signal |
| --- | --- | --- | --- | --- |
| C0 | Skill §3 step 2 | `mvn clean install` (reactor root) | — | exit 0, tests not skipped |
| C1 | `AGENTS.md` (root) | `mvn clean verify` (`api/`). Integration tests are tagged and run in the `integration-test` phase. | Docker | exit 0, unit and integration tests run |
| C2 | `api/README.md` → Running → Approach 1 | `mvn spring-boot:run` (`api/`) | Docker/Compose (Postgres) | readiness returns 200; `/api/v1/items` returns 200 |
| C3 | `api/README.md` → Running → Approach 2 | `mvn spring-boot:test-run` (`api/`) | Docker (Testcontainers) | same as C2 |
| C4 | `api/README.md` → Running → Approach 3 | `mvn spring-boot:build-image`, then `docker compose -f ../infra/compose.yaml --profile api up` (`api/`) | Docker | same as C2 |
| C5 | `api/README.md` → Running → Approach 4 (native) | `sdk install java 21.0.2-graalce`, `mvn -Pnative spring-boot:build-image`, then the same Compose command | GraalVM 21 | same as C2 |
| C6 | `api/README.md` → Testing | `mvn -PnativeTest clean test` (`api/`) | GraalVM 21 | exit 0 |
| C7 | `api/README.md` → Static analysis | `docker compose -f ../infra/sonar.yaml up`, then `mvn clean install -Psonar` (`api/`) | SonarQube server; publishes analysis | exit 0, analysis submitted |
| SM | `CLAUDE.md` (root) + skill §3a | Smoke test after every item: readiness, `curl --fail http://127.0.0.1:8081/api/v1/items`, plus code-derived interfaces | the running app | all checks pass |

## 2. Ordered execution trace

| Step | Action / exact command | Outcome | Basis |
| --- | --- | --- | --- |
| 1 | Run the pinned recipe from the reactor root: `mvn -U org.openrewrite.maven:rewrite-maven-plugin:6.46.1:run --define rewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:6.37.1 --define rewrite.activeRecipes=org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5 --define rewrite.exportDatatables=true` | Succeeded; parent is now `3.5.12` | Simulated |
| 2 | Before any manual edit, save `git diff --stat`, `git diff`, `git status --porcelain`, the `Changes have been made to <file> by:` log lines and `target/rewrite/datatables/` to the temp directory. Start the agent change log. | Snapshot contents (the per-file recipe change list) are **not in evidence** | Required by skill; not simulated |
| 3 | Re-resolve the effective POM for `api` | `3.5.12` confirmed, matching the stable target | Simulated |
| 4 | C0 `mvn clean install` (root) | **Not in evidence** | Required by skill §3 step 2 |
| 5 | C1 `mvn clean verify` (`api/`) | PASSED: unit and integration tests | Simulated |
| 6 | Re-read the README/AGENTS/CLAUDE files after the upgrade | No new items reported | Required; the scenario reports no doc changes |
| 7 | C2 Approach 1 `mvn spring-boot:run` (in the background, logs captured), wait for readiness, smoke test | readiness 200 and `/v1/items` 200. This run also serves as the smoke test for C1. Then stop the app; Boot runs Compose `down`. | Simulated |
| 8 | C3 Approach 2 `mvn spring-boot:test-run` | **FAILED**: `ClassNotFoundException: org.springframework.boot.testcontainers.service.connection.ServiceConnection` | Simulated |
| 9 | **Fix A1**: add the test-scoped dependency `org.springframework.boot:spring-boot-testcontainers` to the `api` POM. Log it as an agent change. | — | Fix described in scenario (file not edited here) |
| 10 | Rerun C3 and smoke test it | starts; readiness and `/v1/items` both pass; then stop | Simulated |
| 11 | Fix A1 changed test dependencies, so the earlier items it could affect must be rerun: C1 `mvn clean verify` (and C0), each followed by a smoke run | **Not in evidence** | Required by skill §3 step 5 |
| 12 | C4 Approach 3 `mvn spring-boot:build-image`, then `docker compose -f ../infra/compose.yaml --profile api up`, smoke test, then `docker compose -f ../infra/compose.yaml --profile api down` | build and startup pass; readiness and `/v1/items` both pass | Simulated |
| 13 | Satisfy the C5 prerequisite using the documented SDKMAN setup: `sdk install java 21.0.2-graalce` | GraalVM installed | Simulated |
| 14 | C5 `mvn -Pnative spring-boot:build-image` | **FAILED**: missing reflection hint for `ItemDto` | Simulated |
| 15 | **Fix A2**: register a runtime reflection hint for `ItemDto` in the application's `RuntimeHintsRegistrar` (or create and import one). Log it as an agent change. | — | Fix described in scenario (file not edited here) |
| 16 | Rerun C5: native build, Compose `--profile api up`, smoke test, then `down` | build passes; readiness and `/v1/items` both pass | Simulated |
| 17 | C6 `mvn -PnativeTest clean test` | PASSED | Simulated |
| 18 | Smoke run after C6 (test-only item; code changed since the last test-only smoke run): Approach 1 plus the smoke test | **Not in evidence** | Required by skill §3a |
| 19 | Rerun earlier items affected by Fix A2 (main-code change): C1/C0, then C2–C4 with their smoke tests | **Not in evidence** | Required by skill §3 step 5 |
| 20 | C7 Sonar: SonarQube is not running. Its startup (`docker compose -f ../infra/sonar.yaml up`) and `mvn clean install -Psonar`, which publishes analysis, need your approval. You haven't been asked yet, so I did not run it. | **BLOCKED**: approval needed | Simulated + skill §3 step 5 |
| 21 | Clean up only what this validation started (app processes, Compose stacks). Keep the GraalVM install, which is the documented setup. | — | Required by skill |

---

## Final report

**What I did:** I didn't actually run anything or change any files. This report is built only from the simulated evidence in `scenario.md`.

### Version
- `2.7.18` → `3.5.12` (stage: latest stable 2.x → latest stable 3.x). The fixture metadata lists 3.5.12 as the latest stable 3.x, and the simulated effective POM for `api` confirms it.

### Changes made by the OpenRewrite recipe
Recipe: `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5` (`org.openrewrite.recipe:rewrite-spring:6.37.1`, plugin `rewrite-maven-plugin:6.46.1`):
- Root `pom.xml`: `spring-boot-starter-parent` 2.7.18 → 3.5.12.
- Anything else the recipe changed (for example `javax.*` → `jakarta.*`, renamed properties, Security changes) is **not in the simulated evidence**. In a real run, this list comes from the saved diff, the `Changes have been made to … by:` log lines and the datatables.

### Changes made by the AI agent
| File | Change | Reason |
| --- | --- | --- |
| `api/pom.xml` | Added the test-scoped dependency `org.springframework.boot:spring-boot-testcontainers` | Approach 2 (`spring-boot:test-run`) failed with `ClassNotFoundException …ServiceConnection` |
| Application `RuntimeHintsRegistrar` (exact file not given in the scenario) | Registered a reflection hint for `ItemDto` | Approach 4 native build failed: missing reflection hint for `ItemDto` |

### Validation
| ID | Source | Command | Result | Smoke test | Evidence |
| --- | --- | --- | --- | --- | --- |
| C0 | Skill §3 | `mvn clean install` | **Not run** (no evidence) | — | — |
| C1 | `AGENTS.md` | `mvn clean verify` | Simulated: PASSED before the fixes; **rerun after A1/A2 not in evidence** | Simulated: via Approach 1 run (pre-fix) | unit and integration tests passed |
| C2 | README Approach 1 | `mvn spring-boot:run` | Simulated: PASSED (pre-fix); rerun after A2 not in evidence | readiness 200, `/v1/items` 200 | simulated |
| C3 | README Approach 2 | `mvn spring-boot:test-run` | Simulated: FAILED → fixed (A1) → PASSED | readiness, `/v1/items` pass | simulated |
| C4 | README Approach 3 | `build-image` + Compose `--profile api up` | Simulated: PASSED (before A2); rerun after A2 not in evidence | readiness, `/v1/items` pass | simulated |
| C5 | README Approach 4 | `sdk install java 21.0.2-graalce`; `mvn -Pnative spring-boot:build-image` + Compose | Simulated: FAILED → fixed (A2) → PASSED | readiness, `/v1/items` pass | simulated |
| C6 | README Testing | `mvn -PnativeTest clean test` | Simulated: PASSED | **Follow-up smoke run not in evidence** | simulated |
| C7 | README Static analysis | `docker compose -f ../infra/sonar.yaml up`; `mvn clean install -Psonar` | **BLOCKED**: SonarQube isn't running and starting it and publishing analysis needs your approval | — | — |

### Smoke tests
- Exercised (simulated): `http://127.0.0.1:8081/api/actuator/health/readiness` and `http://127.0.0.1:8081/api/v1/items` (status 200) on Approaches 1–4.
- **Not in evidence:** OpenAPI/Swagger UI, the protected-route 401/403 check and an authenticated write, any web UI or other interfaces, log scans for errors and deprecations, content assertions beyond status 200, and a baseline diff (no baseline was captured).

### Status
**The stage is not ready.** The upgrade to 3.5.12 and the documented approaches pass in the simulation. Still open:
- C7 (Sonar) is BLOCKED.
- `mvn clean install` (C0) has no evidence.
- The reruns after the two fixes have no evidence.
- The smoke test after C6 has no evidence.
- Several smoke-test interfaces have no evidence.

I won't propose the 3.x → 4.x stage until these are resolved or you waive them.

### Next: your decision
1. **SonarQube (C7):** may I start it locally with `docker compose -f ../infra/sonar.yaml up` and run `mvn clean install -Psonar`? This submits the analysis to that local SonarQube. Or do you want to waive this item?
2. Once you answer, I'll finish the remaining validation:
   - `mvn clean install`
   - the reruns of `mvn clean verify` and Approaches 1–3 after the fixes, each with a smoke test
   - the smoke run after C6
   - the full smoke test of every interface
