---
name: springboot-upgrade
description: Upgrade a Maven Spring Boot application incrementally using this repository's springboot-upgrade-workflow.md and its pinned OpenRewrite recipes. Use when asked to upgrade or migrate Spring Boot, update an older Boot release, or move between Boot 2.x, 3.x, and 4.x. Includes compilation, test failure repair, documented runtime validation, and confirmation between major-version stages.
compatibility: Requires Maven, a compatible JDK, access to the configured Maven repositories, and the application's documented validation dependencies.
---

# Spring Boot upgrade

Carry out one validated upgrade stage at a time. A successful recipe run is only
the start: compilation, unit/integration tests, and documented application checks
establish whether the application is ready.

## 1. Inspect the project and workflow

- Locate the repository root and read its `springboot-upgrade-workflow.md`
  (`../../../springboot-upgrade-workflow.md` relative to this skill). This is the
  user-maintained workflow; read it on each invocation so subsequent edits apply.
  If it is missing, ask for it rather than silently inventing a replacement.
- Read applicable `AGENTS.md` instructions and the root/module README files.
  Inspect the working tree so unrelated user changes are preserved.
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
| Older 1.x | Stabilize on the latest stable 1.x first. The workflow supplies no 1.x recipe: establish the required migration steps from official guidance rather than guessing a command. |
| Latest stable 1.x, moving toward newer majors | Upgrade to latest stable 2.x with the 2.7 recipe. |
| Older 2.x | Upgrade to latest stable 2.x with the 2.7 recipe, then validate and pause. |
| Latest stable 2.x, with the next migration confirmed | Upgrade to latest stable 3.x with the 3.5 recipe. |
| Older 3.x | Upgrade to latest stable 3.x with the 3.5 recipe, then validate and pause. |
| Latest stable 3.x, with the next migration confirmed | Upgrade to latest stable 4.x with the 4.1 recipe. |
| Older 4.x | Upgrade to latest stable 4.x with the 4.1 recipe. |
| Already at the requested stable destination | Verify the current state and report that no version change is needed. |

Do not skip a major version. A request for a distant destination does not remove
the workflow's confirmation between completed stages. If starting on a latest
stable major and the user explicitly requests the immediate next major, that
request authorizes the first transition; subsequent transitions still require
the step-7 confirmation.

The commands below reproduce the workflow at skill creation. If the workflow is
edited, follow its current commands. Keep the pinned coordinates and recipe IDs;
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
   `clean install`; inspect the build configuration and documented instructions.
   Report skipped or unavailable tests accurately. If required dependencies or
   services are unavailable, report the blocker and pause this stage.
5. Revisit README and applicable AGENTS validation/startup instructions. Follow
   the documented application launch and functional/smoke checks, fixing failures
   and rerunning checks. Build success alone does not prove runtime readiness.
   If no validation process is documented, use an appropriate local startup or
   smoke check where feasible and explain what was verified or remains blocked.
6. Clean up only processes/resources started for this validation. Inspect the
   final diff and retain relevant command results and test outcomes for reporting.

## 4. Report and pause

Summarize:

- **Version:** initial → actual resulting version, stage, stable-release evidence.
- **Changes:** recipe used and material manual fixes.
- **Validation:** commands, build/test results, runtime checks, and any blockers.
- **Status:** ready only when the required checks actually passed; otherwise
  describe the incomplete stage without implying success.
- **Next:** after successful validation, ask whether the user wants additional
  validation. If a major-version migration is still pending, ask whether to
  proceed to that specific next stage. Wait for the answer before executing it.

Keep the final report concise and distinguish executed checks from suggested or
blocked ones. For a planning-only request, give a plan and do not claim execution.
