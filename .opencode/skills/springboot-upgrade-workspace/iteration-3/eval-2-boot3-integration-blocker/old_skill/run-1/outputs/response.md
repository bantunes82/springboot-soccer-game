# Spring Boot upgrade — decision trace (scenario: Boot 3.x with integration blocker)

> **Execution disclaimer:** Nothing below was actually executed. All versions, release metadata, and command outcomes come from the **simulated evidence** in `scenario.md`. No Maven command was run, no application files were changed, and no database, integration test, or runtime check happened. Items marked "Simulated" describe outcomes the scenario supplied. Items marked "Not run" or "BLOCKED" were not performed in this simulation either.

## 1. Inspection (simulated observations)

1. **Workflow source:** The skill's own workflow is the upgrade procedure. No separate workflow document is required.
2. **Documentation read** (from the scenario's summaries of README and AGENTS):
   - README: `mvn clean install` → `mvn -Pintegration verify` → `java -jar service/target/service.jar --spring.profiles.active=local` → `curl --fail http://localhost:8080/actuator/health`.
   - AGENTS: integration checks must pass before the stage counts as ready. The `integration` profile needs a local PostgreSQL instance. `clean install` runs unit tests only.
3. **Effective Boot version:** The root POM uses a company parent and imports `org.springframework.boot:spring-boot-dependencies:${spring-boot.version}`, with `spring-boot.version=3.2.0`. All modules inherit that BOM. The Boot Maven plugin has its own version, which does **not** set the dependency version, so it is ignored for version detection. **Detected: 3.2.0.**
4. **Toolchain and working tree:** `mvn` (no wrapper). A compatible JDK is installed. The working tree is clean, so there are no unrelated user changes to protect.
5. **Destination:** The user eventually wants 4.x. Under the skill's rules, an older 3.x release must first be stabilized on the latest stable 3.x. Majors cannot be skipped, and the 3.x → 4.x move needs confirmation once this stage is validated.
6. **Stable-release evidence:** The fixture metadata (simulated) names **3.5.12** as the latest stable 3.x. That is the target, and no snapshots, milestones, or release candidates are considered.
7. **Immediate stage:** 3.2.0 → 3.5.12 using the pinned 3.5 recipe. Validate, then pause.

### Documented validation checklist (built up front)

| # | Source | Command (working dir: repository root) | Prerequisites | Success signal |
| --- | --- | --- | --- | --- |
| 1 | README | `mvn clean install` | Maven, JDK | Exit 0. Unit tests pass. |
| 2 | README + AGENTS | `mvn -Pintegration verify` | Local PostgreSQL | Exit 0. Integration tests run and pass. |
| 3 | README | `java -jar service/target/service.jar --spring.profiles.active=local` | Built jar, PostgreSQL (runtime needs the DB) | Application starts. |
| 4 | README | `curl --fail http://localhost:8080/actuator/health` | Item 3 running | HTTP 200, status `UP` |

### Smoke-test plan

- Base URL: `http://localhost:8080`, profile `local`.
- Health: `/actuator/health` must return 200 and `UP`, including the DB component.
- Other interfaces (controllers, OpenAPI/Swagger, security, web UI): the scenario provides no code-level evidence. On a real project I would derive them from controllers and config. Here they are recorded as **unknown from the provided evidence**, not as absent.
- **Baseline smoke run:** It could not be captured. The pre-upgrade runtime also needs PostgreSQL, which is unavailable (scenario item 4). This is recorded, and the upgrade continues.

## 2. Stage command

```bash
mvn -U org.openrewrite.maven:rewrite-maven-plugin:6.46.1:run --define rewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:6.37.1 --define rewrite.activeRecipes=org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5 --define rewrite.exportDatatables=true
```

**Simulated outcome:** The recipe succeeds.

## 3. Post-recipe validation (ordered)

1. **Diff and POM inspection.** Simulated: the BOM property changed `3.2.0` → `3.5.12`, and the effective POMs show 3.5.12 for every module. This matches the verified stable target and is consistent across the reactor.
2. **`mvn clean install`.** Simulated: succeeds with unit tests only. No integration tests ran, as AGENTS predicts. This does **not** cover the integration requirement.
3. **Check for extra integration commands.** Integration tests need `-Pintegration` (README/AGENTS). That command is already checklist item 2.
4. **`mvn -Pintegration verify`.** Simulated: **fails** with PostgreSQL connection refused. This is a missing prerequisite, not a code failure caused by the migration, so there is no migration fix to apply.
5. **Try to satisfy the prerequisite.** Simulated: database provisioning is unavailable in this environment, and no alternate test database is configured. I would not invent a database setup, change the test config, or skip tests to get a green build. Provisioning a database usually needs infrastructure access or credentials, so it is a question for the user. **Item 2 → BLOCKED.**
6. **Runtime run approach** (`java -jar … --spring.profiles.active=local` plus health curl). The runtime also needs PostgreSQL, so it cannot start and become healthy. **Items 3–4 → BLOCKED.** The test-only items' smoke tests depend on this run approach, so they are BLOCKED too.
7. **Cleanup.** No processes or containers were started, so there is nothing to clean up. The retained diff is only the BOM property change made by the recipe (simulated).

## 4. Final status message

**Version:** 3.2.0 → 3.5.12 (simulated). Stage: latest stable 3.x stabilization. Evidence: the supplied fixture metadata (simulated) lists 3.5.12 as the latest stable 3.x. Effective POMs confirm 3.5.12 in every module (simulated).

**Changes:** Pinned recipe `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5` (rewrite-maven-plugin 6.46.1, rewrite-spring 6.37.1). No manual fixes.

**Validation:**

| # | Source | Command | Result | Smoke test | Evidence |
| --- | --- | --- | --- | --- | --- |
| 1 | README | `mvn clean install` | PASSED (simulated; unit tests only) | BLOCKED: app cannot start without PostgreSQL | Simulated: build succeeds, no integration tests run |
| 2 | README + AGENTS | `mvn -Pintegration verify` | **BLOCKED** | BLOCKED | Simulated: PostgreSQL connection refused, and no DB can be provisioned here |
| 3 | README | `java -jar service/target/service.jar --spring.profiles.active=local` | **BLOCKED** (not run) | BLOCKED | Runtime requires PostgreSQL |
| 4 | README | `curl --fail http://localhost:8080/actuator/health` | **BLOCKED** (not run) | n/a | Depends on item 3 |

**Smoke tests:** None were run. The health endpoint and any other interfaces are BLOCKED because PostgreSQL is missing. There was no pre-upgrade baseline for the same reason.

**Status:** **NOT READY.** The upgrade to 3.5.12 is applied and compiles with passing unit tests (simulated). AGENTS requires the integration checks before the stage is ready, and those checks and the runtime validation are BLOCKED on PostgreSQL. I am **not** proposing the 3.x → 4.x stage. You have not given permission for it, and this stage is incomplete.

**Needed from you:** Please pick one of these:
1. Provide a local PostgreSQL instance, or connection details for one, that the `integration` profile and the `local` runtime profile can use. I will then rerun `mvn -Pintegration verify`, start the jar, and run the health and smoke checks.
2. Tell me another approved way to provision the database (for example, a Docker or Compose setup you approve).
3. Explicitly waive the integration and runtime items, knowing that the stage would then be reported as unvalidated against PostgreSQL.

Once every item has passed or you have waived it, I'll ask whether you want to go on to the Spring Boot 4.x stage. I won't start that stage before then.
