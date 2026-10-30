---
name: springboot-upgrade
description: Upgrade a Maven Spring Boot application incrementally using the self-contained workflow and pinned OpenRewrite recipes in this skill. Use when asked to upgrade or migrate Spring Boot, update an older Boot release, or move between Boot 2.x, 3.x, and 4.x. Includes compilation, test failure repair, mandatory execution of EVERY run/validation approach documented in README.md, AGENTS.md, and CLAUDE.md after each upgrade stage, a mandatory smoke test of every interface the running application exposes (REST API, Swagger UI/OpenAPI, web UI, actuator, auth flows, etc.) after every step and approach, and confirmation between major-version stages.
compatibility: Requires Maven, a compatible JDK, access to the configured Maven repositories, and the application's documented validation dependencies.
---

# Spring Boot upgrade

Carry out one validated upgrade stage at a time. A successful recipe run is only
the start: compilation, unit/integration tests, documented application checks,
and a smoke test of the running application's real interfaces after every step
establish whether the application is ready.

## 1. Inspect the project

- Locate the repository root. Use the workflow and commands defined in this skill
  as the upgrade procedure; no separate workflow document is required.
- MANDATORY: locate and read every `README.md`, `AGENTS.md`, and `CLAUDE.md` file
  in the repository (root and every module; search the tree rather than assuming
  locations). Extract **every** documented step and approach for building,
  running, testing, and validating the application — whatever the heading
  ("Running the application", "Approach 1/2/3…", "Getting started",
  "Verification", "Testing", "Native", "Coverage", "Static analysis", etc.).
  Treat these documents as the authoritative, non-optional validation procedure
  for this project — do not substitute a generic `mvn clean install` or a guessed
  `java -jar` invocation for them.
- Build the **documented validation checklist** now and keep it for the whole
  upgrade (see section 3, step 5). List each approach/step as its own item with:
  source file and heading, exact command(s) and working directory, prerequisites
  (Docker, Compose, GraalVM, SonarQube, tokens…), and the success signal (exit
  code, readiness/health URL, endpoint response, report produced). When the same
  workflow is described in several files, merge into one item but keep every
  distinct command. Alternatives are *not* "pick one": if a README lists
  Approach 1, 1.1, 2, 3, 3.1, and 4, the checklist has all six, plus every
  documented test, integration-test, native-test, coverage, and analysis command.
- Build the **smoke-test plan** now as well (see section 3a). Documentation
  rarely lists every interface, so derive it from the code and configuration
  too: base URL (`server.port`, `server.servlet.context-path`,
  `spring.webflux.base-path`, profile overrides), controllers and their mappings
  (`@RestController`, `@Controller`, router functions), springdoc/springfox
  (`/v3/api-docs`, `/swagger-ui/index.html`), actuator exposure and health
  groups, server-rendered or static web pages (Thymeleaf/Freemarker templates,
  `static/index.html`), GraphQL, WebSocket, or other exposed protocols, and the
  security model (public vs. protected routes, documented token helpers or test
  credentials). Record which interfaces exist; "no web UI" is a finding, not an
  assumption.
- Before running the recipe, if the application can be started with the
  documented default approach, capture a **baseline smoke run** (in particular
  save the OpenAPI document and representative responses). Comparing against it
  after the upgrade exposes silently dropped endpoints, changed JSON shapes, or
  altered status codes that a bare "200 OK" would hide. If a baseline cannot be
  captured, say so and continue.
- Inspect the working tree so unrelated user changes are preserved.
- Inspect the root and module `pom.xml` files. Resolve Spring Boot's effective
  version, including inherited parents, properties, or an imported
  `spring-boot-dependencies` BOM. A plugin version alone does not establish the
  application's dependency version. Use Maven's effective POM if necessary.
- Identify the reactor root, Maven launcher, JDK/toolchain settings, test profiles,
  and any services required for validation. Use `./mvnw` instead of `mvn` when the
  project requires its wrapper, retaining the command's remaining arguments.
- Establish the requested destination, if specified. An unspecified destination
  means first completing the current-major stabilization stage. Explain the
  detected version and immediate stage before executing it.

## 2. Select a single stage

Verify stable releases using official Spring Boot release information or Maven
repository metadata available to the project. Record the source of the selected
version. Do not treat a recipe's name, a successful exit, or a remembered version
as proof that its output is the latest stable release. Exclude snapshots,
milestones, and release candidates when selecting a stable target.

| Starting state | Immediate stage |
| --- | --- |
| Older 1.x | Stabilize on the latest stable 1.x first. This skill supplies no 1.x recipe: establish the required migration steps from official guidance rather than guessing a command. |
| Latest stable 1.x, moving toward newer majors | Upgrade to latest stable 2.x with the 2.7 recipe. |
| Older 2.x | Upgrade to latest stable 2.x with the 2.7 recipe, then validate and pause. |
| Latest stable 2.x, with the next migration confirmed | Upgrade to latest stable 3.x with the 3.5 recipe. |
| Older 3.x | Upgrade to latest stable 3.x with the 3.5 recipe, then validate and pause. |
| Latest stable 3.x, with the next migration confirmed | Upgrade to latest stable 4.x with the 4.1 recipe. |
| Older 4.x | Upgrade to latest stable 4.x with the 4.1 recipe. |
| Already at the requested stable destination | Verify the current state and report that no version change is needed. |

Do not skip a major version. A request for a distant destination does not remove
the confirmation between completed stages. If starting on a latest
stable major and the user explicitly requests the immediate next major, that
request authorizes the first transition; subsequent transitions still require
the confirmation described in section 4.

Use the commands below for each stage. Keep the pinned coordinates and recipe IDs;
do not silently replace an unavailable recipe or upgrade the recipe artifacts.
If a command cannot resolve, or its target line conflicts with the verified
stable destination, explain the evidence and ask how to proceed. Do not force
an unpublished target or call an older result "latest."

### Stage: Spring Boot 2.x

```bash
mvn -U org.openrewrite.maven:rewrite-maven-plugin:6.46.1:run --define rewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:6.37.1 --define rewrite.activeRecipes=org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_7 --define rewrite.exportDatatables=true
```

### Stage: Spring Boot 3.x

```bash
mvn -U org.openrewrite.maven:rewrite-maven-plugin:6.46.1:run --define rewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:6.37.1 --define rewrite.activeRecipes=org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5 --define rewrite.exportDatatables=true
```

### Stage: Spring Boot 4.x

```bash
mvn -U org.openrewrite.maven:rewrite-maven-plugin:6.46.1:run -Drewrite.recipeArtifactCoordinates=io.arconia.migrations:rewrite-spring:1.2.3 -Drewrite.activeRecipes=io.arconia.rewrite.spring.boot4.UpgradeSpringBoot_4_1
```

## 3. Inspect and validate the result

After each recipe, complete all these steps before considering another stage:

1. Inspect the diff and POMs, resolving inherited versions again when needed.
   Confirm the actual resulting Boot version against the selected stable target
   across the reactor. If nothing changed unexpectedly, or versions are
   inconsistent, investigate before declaring success.
   - **Record what the recipe changed before you change anything yourself.**
     The final report must separate recipe changes from agent changes (section
     4), and that becomes guesswork once both are mixed in the working tree.
     Right after the recipe exits, save `git diff --stat`, `git diff`, and
     `git status --porcelain` to the temp directory (new files are untracked and
     do not appear in `git diff`). Also keep the recipe's evidence of which
     sub-recipe touched which file: the `Changes have been made to <file> by:`
     lines in the Maven log, and, when `rewrite.exportDatatables=true` was used,
     the datatables under `target/rewrite/datatables/` (e.g. source-file
     results). If the tree had user changes before the recipe, snapshot those
     first too, so they are attributed to neither.
   - From this point on, keep a short **agent change log**: for every edit you
     make (code, POM, configuration, tests, docs, scripts), note the file, what
     changed, and the failure or reason that required it (e.g. "compile error
     in `X.java`", "smoke test: `/v3/api-docs` 500"). Also log when you revert
     or adjust something the recipe did — that is an agent change too.
2. Run `mvn clean install` from the appropriate reactor root (or its project
   wrapper equivalent). Do not skip tests to manufacture a successful build.
3. Fix migration-related compilation, unit-test, and integration-test failures,
   and rerun the affected validation until it passes. Follow the repository's
   conventions for Java/Jakarta, Spring Security, configuration, and dependency
   migration fixes; use official migration guidance for the destination version.
4. Check whether integration tests require an explicit profile or command beyond
   `clean install`; inspect the build configuration and documented instructions,
   and add any such command to the checklist. Report skipped or unavailable tests
   accurately.
5. MANDATORY — run the **entire documented validation checklist** from section 1
   after every stage, and without exception after every major-version upgrade
   (1.x→2.x, 2.x→3.x, 3.x→4.x). This is a hard requirement of this skill, not a
   suggestion; a stage cannot be completed while any item is unrun.
   - Re-read every `README.md`, `AGENTS.md`, and `CLAUDE.md` first: the upgrade
     may have changed what they reference, and you may add items you missed.
   - Execute **every** item — every run approach and every documented
     test/verification command — not a representative subset. Each approach
     exercises different code paths that a major upgrade can break: Compose
     integration, Testcontainers wiring, the observability agent and profile,
     buildpack image builds, native-image/AOT hints, coverage and analysis
     plugins. One passing approach says nothing about the others.
   - For each run approach: perform its documented setup, start it (background
     long-running processes and capture logs), wait for the documented readiness
     signal, run the **full smoke test from section 3a** against it, then stop it
     and run its documented cleanup (e.g. Compose `down`) before starting the
     next one, so ports and containers do not collide. Run items sequentially.
   - Every other item is also followed by a smoke test (section 3a explains how
     for build-only and test-only items). An item is PASSED only when its own
     command succeeded **and** its smoke test passed.
   - Fix every failure these items surface, then rerun the failing item **and**
     any earlier items the fix could affect, until all pass.
   - A missing prerequisite (e.g. GraalVM for native, a SonarQube server, Docker
     not running) is not a reason to skip silently. First try to satisfy it using
     the repository's documented setup (e.g. SDKMAN). Steps that need `sudo`,
     credentials, are documented for another OS, or publish results to an
     external service: ask the user before running them, offering the documented
     command or the closest local equivalent. If an item still cannot run, mark
     it **BLOCKED** with the reason; the stage is then not ready and you must not
     propose the next major stage until the user resolves it or explicitly
     waives that specific item.
   - Only if none of these files document any run/validation step, say so
     explicitly, then start the application with an appropriate local command
     and still run the full smoke test from section 3a.
6. Clean up only processes/resources started for this validation. Inspect the
   final diff and retain each checklist item's command, result, and evidence
   (exit code, key log lines, HTTP status, smoke-test results) for reporting.

## 3a. Smoke test after every step and approach

Builds and test suites pass while the running application is broken more often
than one would expect after a Boot upgrade: a renamed property silently changes
the context path, Spring Security 6 defaults lock down routes, springdoc stops
serving Swagger UI, Jackson changes a JSON shape, a native image lacks a
reflection hint. Only exercising the real interfaces of the running application
catches these. That is why every checklist item ends with a smoke test — no
exceptions, including after a fix and its rerun.

**When, per item type:**

| Item type | Smoke test |
| --- | --- |
| Run approach (`spring-boot:run`, `test-run`, Compose, `java -jar`, profile variants) | Run it against that instance while it is up, before stopping it. |
| Build-only (`build-image`, native image, `package`/jar) | Start the produced artifact (via the documented follow-up, e.g. Compose profile, or `docker run`/`java -jar`) and smoke test it. An image that was built but never started is not validated. |
| Test-only or analysis (`test`, `verify`, `install`, native tests, coverage, Sonar) | Afterwards, start the application with the primary documented run approach and smoke test it. Consecutive test-only items may share one smoke run only if no code, configuration, or dependency changed between them. |

**What to exercise** — every interface the smoke-test plan (section 1) found,
not only what the documentation mentions:

1. **Health:** liveness/readiness (or `/actuator/health`) returns UP, including
   each component in the documented health group.
2. **API docs and Swagger UI:** `/v3/api-docs` (or the configured path) returns
   valid JSON; diff its paths and operations against the baseline when you have
   one. `/swagger-ui/index.html` (or the configured path) returns 200 HTML that
   references the Swagger UI bundle, and the configuration it loads points at a
   reachable API document.
3. **REST API:** at least one representative request per controller /
   resource. Assert status **and** content (expected fields, non-empty seed
   data, content type), not only that the call returned 2xx.
4. **Security:** a protected route without credentials returns 401/403; when a
   token helper or test credentials are documented, perform an authenticated
   write (e.g. create → read back → delete/clean up) so you leave no test data
   behind.
5. **Web UI:** if server-rendered or static pages exist, request them and assert
   expected markup/text; load key assets (JS/CSS). If a headless browser is
   readily available, use it for the main page; otherwise HTTP checks suffice.
6. **Other interfaces** the application exposes (GraphQL, WebSocket, messaging,
   gRPC, actuator info/metrics): one minimal round trip each.
7. **Logs:** scan the startup and request logs for errors, stack traces, and
   deprecation warnings introduced by the upgrade.

Use plain commands (`curl --fail -sS -w '%{http_code}'`, `jq`) so results are
reproducible, and keep a short evidence line per check (URL, status, the
asserted value). Prefer writing the checks once as a small script in a temp
directory and reusing it for every item and every stage — it keeps runs
consistent and makes comparing approaches meaningful.

A failed smoke check fails the item: fix the cause, rerun the item and its smoke
test. If a check cannot run (e.g. no credentials for an authenticated call), mark
that check BLOCKED with the reason and ask the user — do not quietly drop it.

## 4. Report and pause

Summarize:

- **Version:** initial → actual resulting version, stage, stable-release evidence.
- **Changes:** split into two clearly labeled subsections so the user can tell
  automated migration from hand-made fixes — they carry different risk and
  review needs (recipe edits are mechanical and repeatable; agent edits are
  judgment calls that deserve closer review). Base both on the snapshot and
  change log from section 3, step 1, not on memory.
  - **Changes made by the OpenRewrite recipe:** the exact command and recipe ID
    (with artifact coordinates), then the files it modified/added/deleted,
    grouped by kind of change (e.g. Boot parent/BOM version, dependency
    coordinates and versions, `javax.*` → `jakarta.*` imports, renamed
    properties, Spring Security API migrations, plugin updates), naming the
    responsible sub-recipe where the log or datatables show it. If the recipe
    changed nothing, say so.
  - **Changes made by the AI agent:** one entry per change with file(s), what
    was changed, and why — the failing build/test/approach/smoke check that
    required it, or another reason (e.g. aligning a version the recipe missed,
    updating docs/commands). Include any recipe change you reverted or adjusted,
    and say explicitly if you made no manual changes.

  If a single file was touched by both, list it in both subsections, each
  describing only its own part. Pre-existing user changes belong to neither.
  Example layout:

  ```markdown
  ### Changes made by the OpenRewrite recipe
  Recipe: `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5`
  (`org.openrewrite.recipe:rewrite-spring:6.37.1`) — 7 files
  - `pom.xml`: Boot parent 3.2.5 → 3.5.x; springdoc 2.3.0 → 2.8.x (UpgradeSpringBoot_3_5 → UpgradeDependencyVersion)
  - `src/main/resources/application.yml`: renamed `management.metrics.export.*` properties (ChangeSpringPropertyKey)

  ### Changes made by the AI agent
  | File | Change | Reason |
  | --- | --- | --- |
  | `TestContainersConfiguration.java` | Replaced removed `@ServiceConnection` attribute | `mvn clean verify` compile error |
  | `RestTeamRuntimeHints.java` | Registered reflection hint for `TeamDto` | Native smoke test: `/v1/teams` 500 |
  ```
- **Validation:** the full documented validation checklist as a table, one row
  per item: source (file → heading), command(s), result (PASSED / FAILED /
  BLOCKED / WAIVED by user), smoke-test result, and evidence. Every item found in
  section 1 must appear; do not drop rows that were not run.
- **Smoke tests:** the interfaces exercised (health, API docs, Swagger UI, REST
  endpoints, auth, web UI, others) with URL and result, which items each smoke
  run covered, and any differences from the pre-upgrade baseline.
- **Status:** ready only when every checklist item and its smoke test PASSED (or
  the user explicitly waived it); otherwise describe the incomplete stage
  without implying success.
- **Next:** only after every checklist item passed or was waived, ask whether
  the user wants additional validation. If a major-version migration is still
  pending, ask whether to proceed to that specific next stage. Wait for the
  answer before executing it.

Keep the final report concise and distinguish executed checks from suggested or
blocked ones. For a planning-only request, give a plan and do not claim execution.
