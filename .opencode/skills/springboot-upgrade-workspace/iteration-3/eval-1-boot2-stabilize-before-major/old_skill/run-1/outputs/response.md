# Decision trace: Boot 2.3.12 → 2.7.18 (stabilization stage before 3.x/4.x)

> **Evidence note:** Everything below comes from the isolated scenario in
> `evals/fixtures/boot2-stabilize-before-major/scenario.md`. Versions, release metadata, and command
> outcomes are **simulated**. I did not run Maven, start any process, or change any application
> file. When this trace says a step "passed", it means the scenario says it passed, not that I ran it.

## 1. Inspect the project (simulated observations)

| # | Observation | Source |
| --- | --- | --- |
| 1.1 | Root `pom.xml` parent is `spring-boot-starter-parent:2.3.12.RELEASE`, so the effective Boot version is **2.3.12.RELEASE** | scenario: Initial inspection |
| 1.2 | Reactor: root aggregator with modules `domain` and `api`. `api` inherits the root Boot version | scenario |
| 1.3 | Launcher is `mvn` (no wrapper). Working tree is clean, so there are no user changes to preserve | scenario |
| 1.4 | Latest stable 2.x is **2.7.18**, according to the fixture's repository metadata (previews excluded) | scenario (simulated metadata, not checked against live sources) |
| 1.5 | Integration tests are bound to `verify` in the default lifecycle, so `mvn clean install` runs them and no extra profile or command is needed | scenario |
| 1.6 | The README is the only documented validation source given. It says to run `mvn clean install`, then `java -jar api/target/api.jar --spring.profiles.active=smoke`, then `curl --fail http://localhost:8080/health`, then stop the process | scenario |

**Documented validation checklist** (built once, kept for every stage):

| ID | Source | Command(s), working dir = repo root | Prerequisites | Success signal |
| --- | --- | --- | --- | --- |
| V1 | README → build | `mvn clean install` | JDK + Maven | Exit 0. Compile, unit tests, and ITs (bound to `verify`) all pass |
| V2 | README → run + health | `java -jar api/target/api.jar --spring.profiles.active=smoke` (background, logs captured). Then `curl --fail -sS -w '%{http_code}' http://localhost:8080/health`. Then stop that PID | V1 artifact | Health returns 2xx/UP. Process stopped afterwards |

The scenario names no other `README.md`, `AGENTS.md`, or `CLAUDE.md` files. In a real run I would search the whole tree for them.

**Smoke-test plan:** The only interface the scenario shows is `GET http://localhost:8080/health` under the `smoke` profile. The scenario gives no information about controllers, OpenAPI/Swagger, actuator exposure, security, or web UI. In a real run I would derive those from the code and configuration and add them to the plan. Here I can only report them as **not determinable from the provided evidence**. I would not assume they are absent.

**Baseline smoke run:** The scenario provides none. In a real run I would capture one on 2.3.12 first, using V2 plus any derived endpoints. Here I record it as **not captured (no evidence available)**.

**Destination and immediate stage:** The user wants 4.x. The detected version is 2.3.12, which is an older 2.x, so according to the skill's stage table the immediate stage is **upgrade to the latest stable 2.x (2.7.18) with the 2.7 recipe, then validate and pause**. Major versions cannot be skipped. 3.x and 4.x come later, each after its own confirmation.

## 2. Execute the single stage

**Step 2.1: Run the pinned Boot 2.7 recipe from the root:**

```bash
mvn -U org.openrewrite.maven:rewrite-maven-plugin:6.46.1:run --define rewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:6.37.1 --define rewrite.activeRecipes=org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_7 --define rewrite.exportDatatables=true
```

*Simulated outcome 1:* The recipe succeeds and the root parent becomes `2.7.18`.

## 3. Inspect and validate

**Step 3.1: Inspect the diff and POMs.** I would run `git diff` and resolve the effective Boot version per module, for example with `mvn help:effective-pom`.
*Simulated:* Both `domain` and `api` resolve **2.7.18**, which matches the selected stable target. The versions are consistent.

**Step 3.2: Run V1, attempt 1:** `mvn clean install`
*Simulated outcome 2:* **FAILED.** Compilation of `api` fails because a custom subclass of `WebMvcConfigurerAdapter` no longer compiles. That class was deprecated in Spring 5 and is gone from the 2.7 dependency line.

**Step 3.3: Repair (migration-related).** I would change the custom class from `extends WebMvcConfigurerAdapter` to `implements WebMvcConfigurer`, keep its overridden methods (the scenario says they are compatible with the interface's default methods), and update the import to `org.springframework.web.servlet.config.annotation.WebMvcConfigurer`. I would make no other changes and would not skip any tests.
*(I describe this edit here only. I did not apply it, per the task constraints.)*

**Step 3.4: Rerun V1, attempt 2:** `mvn clean install`
*Simulated outcome 3:* **PASSED.** Compilation, unit tests, and integration tests (via `verify`) all pass.

**Step 3.5: Rerun the whole checklist after the fix.**
- V1: covered by attempt 2 above (PASSED, simulated).
- V2:
  ```bash
  java -jar api/target/api.jar --spring.profiles.active=smoke > "$TMPDIR/api-smoke.log" 2>&1 &
  APP_PID=$!
  # wait for startup, then:
  curl --fail -sS -w '%{http_code}' http://localhost:8080/health
  kill "$APP_PID"
  ```
  *Simulated outcome 4:* Startup and health check **PASSED**. The process is stopped afterwards.

V1 is a test-only item, so its smoke test is the V2 run against the artifact it built. No code or dependencies changed between V1 attempt 2 and V2.

**Step 3.6: Smoke test (section 3a)**

| Check | URL / action | Result |
| --- | --- | --- |
| Health | `GET http://localhost:8080/health` | PASSED (simulated) |
| API docs / Swagger UI | not determinable from the scenario | NOT EVIDENCED: needs code inspection in a real run |
| REST endpoints per controller | not determinable from the scenario | NOT EVIDENCED |
| Security (unauthenticated 401/403, authenticated write) | not determinable from the scenario | NOT EVIDENCED |
| Web UI / other interfaces | not determinable from the scenario | NOT EVIDENCED |
| Log scan (errors, deprecations) | startup log | no simulated evidence provided |
| Baseline diff | — | no baseline was captured |

**Step 3.7: Cleanup.** I would stop only the `java -jar` process started in step 3.5. The final diff would be the POM parent bump plus the `WebMvcConfigurer` change.

## 4. Final message to the user

> **Spring Boot 2.x stabilization stage (simulated evidence; nothing was actually executed)**
>
> **Version:** 2.3.12.RELEASE → **2.7.18** for the root parent, `domain`, and `api`. 2.7.18 is the latest stable 2.x according to the fixture's repository metadata (previews excluded). I did not check this against live release data.
>
> **Changes:**
> - The OpenRewrite recipe `org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_7` (`rewrite-maven-plugin:6.46.1`, `rewrite-spring:6.37.1`) moved the parent to 2.7.18.
> - Manual fix: the custom `WebMvcConfigurerAdapter` subclass in `api` now implements `WebMvcConfigurer`. `WebMvcConfigurerAdapter` was removed, and the class's overridden methods carry over unchanged.
>
> **Validation:**
>
> | Item | Source | Command | Result | Smoke | Evidence |
> | --- | --- | --- | --- | --- | --- |
> | V1 | README | `mvn clean install` (root) | PASSED on the 2nd attempt (1st failed compiling `api`, fixed) | Covered by V2 run | Simulated: compile, unit tests, and ITs (bound to `verify`) pass |
> | V2 | README | `java -jar api/target/api.jar --spring.profiles.active=smoke` + `curl --fail http://localhost:8080/health`, then stop | PASSED | Health PASSED | Simulated: health check succeeded, process stopped |
>
> **Smoke tests:** Only `/health` is evidenced, and it passed. The scenario doesn't show any other interfaces (REST controllers, OpenAPI/Swagger, secured routes, UI), and no pre-upgrade baseline was captured. In a real run I would derive and exercise those interfaces too.
>
> **Status:** Every documented checklist item passed in the simulated evidence, so the 2.x stage is complete. One caveat: I couldn't run a broader interface smoke test because the scenario doesn't show those interfaces.
>
> **Next:** Your goal is 4.x, and the next stage is **2.7.18 → latest stable 3.x** with the Boot 3.5 recipe. That includes the `javax` → `jakarta` move, Spring Security 6, and a Java 17+ baseline. Would you like any additional validation on 2.7.18 first? And should I go ahead with the 2.x → 3.x stage? I'll wait for your answer before starting it. 3.x → 4.x would need its own confirmation after that.
