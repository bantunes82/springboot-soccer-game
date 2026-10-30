# Decision trace: Spring Boot 2.3.12 → 2.7.18 (stage 1 of the path to 4.x)

> **Execution status:** Nothing here was actually run. I did not execute Maven, start the application, or change any files. Every "outcome" below comes from the **simulated evidence** in `scenario.md`. Commands are shown exactly as I would run them. Results are marked **[SIMULATED]**.

## 1. Inspect the project

| # | Step | Evidence / decision |
|---|------|---------------------|
| 1.1 | Find every `README.md`, `AGENTS.md`, and `CLAUDE.md` | **[SIMULATED]** The fixture provides a single README procedure. No AGENTS.md or CLAUDE.md content is supplied. |
| 1.2 | Build the documented validation checklist | **V1:** `mvn clean install` (repo root). Integration tests are bound to `verify`, so this runs unit and integration tests. **V2:** `java -jar api/target/api.jar --spring.profiles.active=smoke`, then `curl --fail http://localhost:8080/health`, then stop the process. The README lists no other approaches (Docker, native, coverage). |
| 1.3 | Build the smoke-test plan | Base URL: `http://localhost:8080`. Health: `/health`. The fixture gives no evidence for controllers, OpenAPI/Swagger, security, or a web UI. In a real run I would derive these from code and config (`@RestController`, springdoc/springfox, actuator exposure, `static/`, security config). Here, those interfaces are **unverified** and are not assumed to be absent. |
| 1.4 | Baseline smoke run before the upgrade | Not in the evidence. In a real run I would start the app with V2 on 2.3.12 and save `/health` plus any API docs and representative responses. **Baseline: not captured (no evidence supplied).** |
| 1.5 | Working tree | **[SIMULATED]** Clean. No user changes to preserve or attribute. |
| 1.6 | Effective Boot version | **[SIMULATED]** The root parent is `spring-boot-starter-parent:2.3.12.RELEASE`. Modules `domain` and `api` inherit it. Launcher: `mvn` (no wrapper). |
| 1.7 | Destination and immediate stage | You asked for **4.x**. The current version is older 2.x, so per the stage table the immediate stage is **upgrade to the latest stable 2.x with the 2.7 recipe, then validate and pause**. Majors cannot be skipped. 2.x→3.x and 3.x→4.x each need your confirmation after the previous stage passes. |

## 2. Select the stage

- Target: **2.7.18**, the latest stable 2.x. Source: **[SIMULATED]** the fixture's repository metadata, which excludes previews/RC/snapshots. I am not relying on the recipe's name or on memory.
- Pinned command, run from the repository root:

```bash
mvn -U org.openrewrite.maven:rewrite-maven-plugin:6.46.1:run --define rewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:6.37.1 --define rewrite.activeRecipes=org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_7 --define rewrite.exportDatatables=true
```

## 3. Ordered execution trace

1. **Run the recipe** (command above). **[SIMULATED]** It succeeds. The root parent becomes `2.7.18`, and both `domain` and `api` resolve 2.7.18. That matches the verified target.
2. **Snapshot the recipe's changes before any manual edit.** Save these to `$TMPDIR/upgrade-2.7/`:
   - `git diff --stat > recipe.diffstat`
   - `git diff > recipe.diff`
   - `git status --porcelain > recipe.status`
   - the `Changes have been made to <file> by:` lines from the Maven log
   - `target/rewrite/datatables/`

   Then inspect the POMs. If needed, confirm the effective version with `mvn help:effective-pom`. **[SIMULATED]** The version is consistent across the reactor. The fixture does not detail the recipe's file-level changes, so the per-file list stays unspecified rather than invented.
3. **Validation V1, attempt 1:** `mvn clean install` (root, tests not skipped). **[SIMULATED]** FAILED: compilation error in `api`. A custom `WebMvcConfigurerAdapter` subclass no longer compiles because the class was removed in Spring 5 / Boot 2.x.
4. **Repair (agent change):** change the class from `extends WebMvcConfigurerAdapter` to `implements WebMvcConfigurer`. Keep the overridden methods unchanged, since **[SIMULATED]** their signatures are compatible with `WebMvcConfigurer`'s default methods. Remove the now-unused import. Record it in the agent change log.
5. **Validation V1, attempt 2:** `mvn clean install`. **[SIMULATED]** PASSED: compilation, unit tests, and integration tests (via `verify`).
6. **Smoke test after V1** (test-only item, so start the app with the primary run approach). This uses the V2 startup:
   ```bash
   java -jar api/target/api.jar --spring.profiles.active=smoke > $TMPDIR/upgrade-2.7/app.log 2>&1 &
   APP_PID=$!
   # wait for readiness, then:
   curl --fail -sS -w '%{http_code}' http://localhost:8080/health
   ```
   Also run the other smoke checks from the plan (API docs/Swagger, one request per controller, unauthenticated write → 401/403, web UI, log scan) **if** the code shows those interfaces exist.
7. **Validation V2** (the README run approach): the same command and `curl`. **[SIMULATED]** PASSED: startup OK, `/health` OK. No code or config changed between steps 6 and 7, so one smoke run covers both. Then `kill $APP_PID` and confirm the port is free. Only the process started for this check is stopped.
8. **Re-read the README/AGENTS/CLAUDE files** for anything the upgrade changed. **[SIMULATED]** Nothing new.
9. **Final diff review:** recipe snapshot vs. working tree. The only extra delta should be the `WebMvcConfigurer` change.
10. **Stop.** Do not run the 3.5 recipe. Report and ask for confirmation.

## 4. Final message I would send you

> **Spring Boot upgrade: stage 1 (2.x stabilization) — simulated evidence only**
>
> **Version:** 2.3.12.RELEASE → **2.7.18** (root parent; `domain` and `api` both resolve 2.7.18). 2.7.18 is the latest stable 2.x according to the supplied repository metadata (previews excluded). Your goal is 4.x, but majors can't be skipped, so this stage only stabilizes on the latest 2.x.
>
> ### Changes made by the OpenRewrite recipe
> Recipe: `org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_7` (`org.openrewrite.recipe:rewrite-spring:6.37.1`, plugin `rewrite-maven-plugin:6.46.1`)
> - `pom.xml`: Boot parent `2.3.12.RELEASE` → `2.7.18`.
> - Other file-level changes: listed in the saved diff, log, and datatables. The simulated evidence does not detail them.
>
> ### Changes made by the AI agent
> | File | Change | Reason |
> | --- | --- | --- |
> | `api` custom MVC config class (subclass of `WebMvcConfigurerAdapter`) | `extends WebMvcConfigurerAdapter` → `implements WebMvcConfigurer`; existing overrides kept; unused import removed | First `mvn clean install` failed to compile `api` because `WebMvcConfigurerAdapter` was removed |
>
> ### Validation
> | Item | Source | Command | Result | Smoke test | Evidence |
> | --- | --- | --- | --- | --- | --- |
> | V1 Build + unit + integration tests | README | `mvn clean install` (root) | FAILED → fixed → **PASSED** | PASSED (via V2 run) | Simulated: 2nd run passed compilation, unit tests, and ITs (bound to `verify`) |
> | V2 Run + health check | README | `java -jar api/target/api.jar --spring.profiles.active=smoke`; `curl --fail http://localhost:8080/health`; stop process | **PASSED** | PASSED | Simulated: startup OK, `/health` OK; process stopped |
>
> ### Smoke tests
> - `GET http://localhost:8080/health`: OK (simulated).
> - API docs/Swagger, REST endpoints, auth, web UI: the supplied evidence doesn't show whether these interfaces exist, so they weren't exercised. On the real repository I would derive and run them before calling the stage ready.
> - No pre-upgrade baseline was captured, so there is no baseline diff.
>
> **Status:** According to the simulated evidence, every documented checklist item passed, so the 2.x stabilization stage is complete. This is a simulated trace: no Maven command, application run, or file change was actually performed.
>
> **Next:** Do you want any additional validation on 2.7.18? Should I proceed to the next stage, **Spring Boot 2.7.18 → latest stable 3.x** using the `UpgradeSpringBoot_3_5` recipe? This includes the `javax` → `jakarta` migration and requires Java 17+. I'll wait for your answer before starting it. The 3.x → 4.x stage will need a separate confirmation after that.
