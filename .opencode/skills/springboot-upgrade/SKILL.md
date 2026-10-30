---
name: springboot-upgrade
description: Upgrade a Maven Spring Boot application incrementally using the self-contained workflow and pinned OpenRewrite recipes in this skill. Use when asked to upgrade or migrate Spring Boot, update an older Boot release, or move between Boot 2.x, 3.x, and 4.x. Includes compilation, test failure repair, mandatory execution of EVERY run/validation approach documented in README.md, AGENTS.md, and CLAUDE.md after each upgrade stage, and confirmation between major-version stages.
compatibility: Requires Maven, a compatible JDK, access to the configured Maven repositories, and the application's documented validation dependencies.
---

# Spring Boot upgrade

Carry out one validated upgrade stage at a time. A successful recipe run is only
the start: compilation, unit/integration tests, and documented application checks
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
     signal, run the documented functional/smoke checks (health/readiness,
     Swagger/API docs, public endpoints, authenticated calls when a token helper
     is documented), then stop it and run its documented cleanup (e.g. Compose
     `down`) before starting the next one, so ports and containers do not
     collide. Run items sequentially.
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
     explicitly, then use an appropriate local startup and smoke check and explain
     what was verified.
6. Clean up only processes/resources started for this validation. Inspect the
   final diff and retain each checklist item's command, result, and evidence
   (exit code, key log lines, HTTP status) for reporting.

## 4. Report and pause

Summarize:

- **Version:** initial → actual resulting version, stage, stable-release evidence.
- **Changes:** recipe used and material manual fixes.
- **Validation:** the full documented validation checklist as a table, one row
  per item: source (file → heading), command(s), result (PASSED / FAILED /
  BLOCKED / WAIVED by user), and evidence. Every item found in section 1 must
  appear; do not drop rows that were not run.
- **Status:** ready only when every checklist item PASSED (or the user explicitly
  waived it); otherwise describe the incomplete stage without implying success.
- **Next:** only after every checklist item passed or was waived, ask whether
  the user wants additional validation. If a major-version migration is still
  pending, ask whether to proceed to that specific next stage. Wait for the
  answer before executing it.

Keep the final report concise and distinguish executed checks from suggested or
blocked ones. For a planning-only request, give a plan and do not claim execution.
