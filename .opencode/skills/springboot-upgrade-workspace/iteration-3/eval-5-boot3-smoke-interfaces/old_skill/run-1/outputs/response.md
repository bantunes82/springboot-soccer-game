# Spring Boot 3.x stabilization — decision trace

> **Evidence note:** Nothing here was actually run. Every command outcome below comes from the simulated evidence in `scenario.md` and is marked **[SIM]**. Anything with no simulated outcome is marked **[NO EVIDENCE]** and is reported as not done. I did not run Maven or Docker, did not send any HTTP requests, and did not change any files.

## 1. Inspection

| # | Step | Observation |
|---|---|---|
| 1.1 | Find the repository root and working tree state (`git status`) | [SIM] Working tree is clean, so there are no user changes to preserve. |
| 1.2 | Find and read every `README.md` / `AGENTS.md` / `CLAUDE.md` | [SIM] Root `README.md` → "Run" (Approach A, Approach B). `AGENTS.md` → "Verification" (`mvn clean verify`). No `CLAUDE.md` was reported. |
| 1.3 | Work out the effective Boot version | [SIM] The parent is `spring-boot-starter-parent:3.2.5`. Starting state is **older 3.x**. |
| 1.4 | Confirm toolchain and prerequisites | [SIM] Uses `mvn` (no wrapper). JDK 21 is active and Docker is running. |
| 1.5 | Confirm the destination and the latest stable release | The user asked for "latest 3.x". [SIM] Fixture release metadata gives **3.5.12** as the latest stable 3.x (snapshots, milestones and RCs excluded). Stage: **3.2.5 → 3.5.12** using the pinned 3.5 recipe, then validate and pause. This is not a major-version jump, so no confirmation is needed before it. |
| 1.6 | Inspect code and config for the smoke-test plan (the docs don't list these interfaces) | [SIM] `server.port=8081` and `context-path=/shop`. springdoc `2.3.0` is pinned by a property, with `springdoc.swagger-ui.path=/swagger-ui.html`. `ProductController` and `OrderController` exist. There is a static `index.html` titled "Shop Catalog" plus `app.js`. Security is a Keycloak resource server; `scripts/get-token.sh admin admin` gets a token. Flyway seeds 3 products. No GraphQL or WebSocket was found. |
| 1.7 | Capture a baseline smoke run before the upgrade (Approach A) | [SIM] Baseline `/shop/v3/api-docs` was saved. Outcome 4 confirms a pre-upgrade baseline exists for the comparison. |

### Documented validation checklist (fixed at the start)

| ID | Source | Command(s) / working directory | Prerequisites | Success signal |
|---|---|---|---|---|
| V1 | `AGENTS.md` → Verification | `mvn clean verify` (repo root) | JDK 21, Docker for integration tests | Exit code 0; unit and integration tests pass |
| A | `README.md` → Run → Approach A | `mvn spring-boot:run` (in the background, with logs captured) | Docker/Compose (PostgreSQL) | `curl --fail http://127.0.0.1:8081/shop/actuator/health/readiness` |
| B | `README.md` → Run → Approach B | `mvn spring-boot:build-image`, then `docker compose -f infra/compose.yaml --profile app up`, then `docker compose ... down` | Docker | Image builds; app becomes ready on the same readiness URL |
| S | Skill §3 step 2 | `mvn clean install` | Same as V1 | Exit code 0 |

### Smoke-test plan (one script, reused for every item)

Base URL: `http://127.0.0.1:8081/shop`

1. **Health:** `curl --fail -sS -w '%{http_code}' $BASE/actuator/health/readiness`. Expect 200 and `status=UP`, including the DB component.
2. **API docs:** `curl --fail -sS $BASE/v3/api-docs | jq '.paths|keys'`. Expect 200 with valid JSON, then diff the paths against the baseline.
3. **Swagger UI:** `$BASE/swagger-ui.html` (the configured path) should redirect to `$BASE/swagger-ui/index.html`, which should return 200 HTML referencing the Swagger UI bundle. The config it loads (`$BASE/v3/api-docs/swagger-config`) should point to a reachable `/shop/v3/api-docs`.
4. **REST (public):** `GET $BASE/v1/products` should return 200, JSON, and a 3-item array. `GET $BASE/v1/products/{id}` should return 200 with the expected fields.
5. **Security:**
   - Without a token: `POST $BASE/v1/products` and `GET $BASE/v1/orders` should both return 401.
   - With `TOKEN=$(scripts/get-token.sh admin admin)`:
     - `POST` a product, `GET` it back, then `DELETE` it, so no test data is left behind.
     - `GET $BASE/v1/orders` should return 200.
6. **Web UI:** `GET $BASE/` should return 200 HTML containing `<title>Shop Catalog`. `GET $BASE/app.js` should return 200 JavaScript.
7. **Logs:** scan the startup and request logs for `ERROR`, stack traces and new deprecation warnings.

## 2. Execute the stage

```bash
mvn -U org.openrewrite.maven:rewrite-maven-plugin:6.46.1:run --define rewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:6.37.1 --define rewrite.activeRecipes=org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5 --define rewrite.exportDatatables=true
```

[SIM] The recipe succeeded.

## 3. Inspect and validate

| Step | Command / check | Result |
|---|---|---|
| 3.1 | `git diff` on the POMs; `mvn help:effective-pom` | [SIM] The parent is now `3.5.12`, confirmed in the effective POM and matching the target. **The springdoc property is still `2.3.0`**, which I flagged as a compatibility risk. |
| 3.2 | `mvn clean verify` (V1) | [SIM] PASSED. All unit and integration tests pass. |
| 3.3 | Smoke run after V1, using Approach A (`mvn spring-boot:run`) | [SIM] Readiness UP. Products returned 200 with 3 items. `/shop/` showed "Shop Catalog" and `app.js` loaded. Unauthenticated POST returned 401. Admin create, read and delete succeeded. **FAILED:** `GET /shop/v3/api-docs` returned **500**, with the log showing `NoSuchMethodError: 'void org.springframework.web.method.ControllerAdviceBean.<init>(java.lang.Object)'`. `/shop/swagger-ui/index.html` could not load the API definition. |
| 3.4 | Diagnosis | springdoc 2.3.0 was built against an older Spring Framework 6.x. The `ControllerAdviceBean(Object)` constructor no longer exists in the Framework 6.2 line that Boot 3.5 uses. This is caused by the migration and is not an application code bug. Tests missed it because nothing exercises `/v3/api-docs`. |
| 3.5 | **Fix** | Raise the springdoc version property `2.3.0 → 2.8.9`, a springdoc 2.8.x release compatible with Boot 3.5. No other changes. |
| 3.6 | Rerun V1: `mvn clean verify` | [SIM] PASSED. |
| 3.7 | Rerun A plus the full smoke test, then stop it (Compose `down` runs on shutdown) | [SIM] Readiness UP. api-docs returned 200, and its **paths match the pre-upgrade baseline**. Swagger UI loads the definition. Products, the web UI and app.js, 401 without a token, and admin create/read/delete all PASSED. |
| 3.8 | B: `mvn spring-boot:build-image`, then `docker compose -f infra/compose.yaml --profile app up`, then the smoke test, then `docker compose -f infra/compose.yaml --profile app down` | [SIM] The image built, Compose started the app, and every smoke check passed. Cleanup (`down`) is required. Its result is not stated in the evidence. |
| 3.9 | S: `mvn clean install` | [NO EVIDENCE] The scenario gives no outcome. Not counted as passed. |
| 3.10 | Smoke checks for `GET /v1/orders` (401 without a token, 200 with the admin token) and the `/swagger-ui.html` redirect | [NO EVIDENCE] The scenario doesn't report these specific checks. Not counted as passed. |
| 3.11 | Log scan | [SIM] Only the springdoc `NoSuchMethodError` was reported, and it was fixed. No other findings were given. |

---

## Final report

**Version:** I upgraded Spring Boot 3.2.5 → **3.5.12**, the latest stable 3.x according to the fixture's release metadata. The effective POM confirms 3.5.12 [SIM].

**Changes:**
- Ran the pinned OpenRewrite recipe `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5` (rewrite-spring 6.37.1).
- Manual fix: raised the springdoc-openapi version property from 2.3.0 to **2.8.9**. The recipe left it alone, and 2.3.0 breaks `/v3/api-docs` on Boot 3.5.

**Validation** (all outcomes are simulated; nothing was actually run):

| Item | Source | Command | Result | Smoke test | Evidence |
|---|---|---|---|---|---|
| V1 | AGENTS.md → Verification | `mvn clean verify` | PASSED (after the fix as well) [SIM] | First run: FAILED (api-docs returned 500). After the fix: PASSED (shared smoke run via A) [SIM] | All tests green; after the fix, api-docs returned 200 |
| A | README.md → Run → Approach A | `mvn spring-boot:run` | PASSED [SIM] | PASSED after the fix [SIM] | Readiness UP; api-docs paths match the baseline |
| B | README.md → Run → Approach B | `mvn spring-boot:build-image`, then `docker compose -f infra/compose.yaml --profile app up` / `down` | PASSED [SIM] | PASSED [SIM] | Image built; Compose app passed all checks |
| S | Skill-mandated build | `mvn clean install` | **NOT RUN / no evidence** | — | — |

**Smoke tests** (Approach A after the fix, and Approach B):

| Interface | URL | Result |
|---|---|---|
| Health | `/shop/actuator/health/readiness` | UP [SIM] |
| API docs | `/shop/v3/api-docs` | Before the fix: **500**. After: 200, with paths matching the baseline [SIM] |
| Swagger UI | `/shop/swagger-ui/index.html` | Before the fix: the definition failed to load. After: it loads [SIM] |
| Swagger UI redirect | `/shop/swagger-ui.html` | Not reported |
| Products (public) | `GET /shop/v1/products` | 200, 3 items [SIM] |
| Auth: unauthenticated write | `POST /shop/v1/products` without a token | 401 [SIM] |
| Auth: admin write | create → read → delete with the admin token | Passed, no data left behind [SIM] |
| Orders (protected) | `GET /shop/v1/orders` | **Not reported**. Needs a 401 check without a token and a 200 check with the admin token |
| Web UI | `/shop/` and `/shop/app.js` | "Shop Catalog" page and `app.js` load [SIM] |

**Status: not yet fully ready.**
- All documented items (V1, A and B) and their smoke tests passed in the simulation, and the springdoc regression is fixed.
- Before I'd call this stage ready, these still need to run:
  - `mvn clean install`
  - The `/v1/orders` checks
  - The `/swagger-ui.html` redirect check
- I also need confirmation that Approach B's Compose stack was taken down.

**Next:** Should I run those remaining checks? Once they pass, this stage is complete, and you're on the latest 3.x as you asked. Moving to Spring Boot 4.x would be a separate major-version stage, and I won't start it unless you ask.
