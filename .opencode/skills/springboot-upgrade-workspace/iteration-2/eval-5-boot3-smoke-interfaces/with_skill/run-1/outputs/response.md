# Spring Boot 3.x stabilization: decision trace (simulated)

> **Execution status:** I did not execute anything. No Maven, Docker, curl, or
> other command was run, and no application file was changed. Every outcome below
> is **[SIM]**: it was taken from the scenario's simulated evidence
> (`scenario.md`). Steps marked **[PLAN]** are things the skill requires but the
> scenario gives no outcome for. They have no result, and I do not claim they
> passed. The versions in this trace (3.2.5, 3.5.12, springdoc 2.3.0/2.8.9) are
> fixture inputs, not verified real-world release facts.

---

## 1. Inspect the project

| # | Action / command | Observation |
|---|---|---|
| 1.1 | `git status` | [SIM] Working tree is clean, so there are no user changes to preserve. |
| 1.2 | Find the documents: `find . -name README.md -o -name AGENTS.md -o -name CLAUDE.md` | [SIM] Found root `README.md` (section "Run") and `AGENTS.md` (section "Verification"). There is no `CLAUDE.md`. |
| 1.3 | Resolve the Boot version: `mvn help:effective-pom` (check the parent and BOM) | [SIM] Parent is `spring-boot-starter-parent:3.2.5`, so the project is on an **older 3.x**. |
| 1.4 | Toolchain | [SIM] Launcher is `mvn` (there is no wrapper), JDK 21 is active, and Docker is running. |
| 1.5 | Find the stable target | [SIM] Fixture release metadata says the latest stable 3.x is **3.5.12**. This metadata is the recorded source; I did not query real repositories. |
| 1.6 | Destination | The user asked for "latest 3.x". From older 3.x, the single stage is the **Boot 3.5 recipe**, followed by validation and a pause. No major-version change is involved. |

### Documented validation checklist (built now and kept for the whole stage)

| ID | Source | Command(s), working directory = repository root | Prerequisites | Success signal |
|---|---|---|---|---|
| V1 | AGENTS.md → Verification | `mvn clean verify` | JDK 21, Docker (for integration tests) | Exit 0; unit and integration tests pass |
| V2 | Skill §3 step 2 (mandatory build) | `mvn clean install` | Same as V1 | Exit 0, tests not skipped |
| A | README.md → Run, Approach A | `mvn spring-boot:run` (Compose starts PostgreSQL) | Docker and Compose | `curl --fail http://127.0.0.1:8081/shop/actuator/health/readiness` returns UP |
| B | README.md → Run, Approach B | `mvn spring-boot:build-image`, then `docker compose -f infra/compose.yaml --profile app up`, then `... --profile app down` | Docker and Compose | Image built, container ready, smoke test passes |

### Smoke-test plan (taken from code and config, because the docs do not mention Swagger, endpoints, or the web page)

The base URL is `http://127.0.0.1:8081/shop`, from `server.port=8081` and `server.servlet.context-path=/shop`.

| Check | Request | Assertion |
|---|---|---|
| S1 Health | `GET /shop/actuator/health/readiness` | 200, `status=UP`, every component UP |
| S2 API docs | `GET /shop/v3/api-docs` | 200 JSON; the `paths` match the baseline (`/v1/products`, `/v1/products/{id}`, `/v1/orders`) |
| S3 Swagger UI | `GET /shop/swagger-ui.html` (configured path, redirects) and `GET /shop/swagger-ui/index.html`, plus `GET /shop/v3/api-docs/swagger-config` | 200 HTML referencing the swagger-ui bundle; the config `url` points to a reachable api-docs |
| S4 Products (public) | `GET /shop/v1/products`, `GET /shop/v1/products/{id}` | 200 JSON, 3 seeded products, expected fields |
| S5 Security, no credentials | `POST /shop/v1/products`, `GET /shop/v1/orders` without a token | 401 or 403 |
| S6 Authenticated write | `TOKEN=$(scripts/get-token.sh admin admin)`, then POST create, GET it back, DELETE | 201/200, read-back matches, delete succeeds, no data left behind |
| S7 Orders (admin) | `GET /shop/v1/orders` with the admin token | 200 JSON |
| S8 Web UI | `GET /shop/` and `GET /shop/app.js` | 200 HTML containing "Shop Catalog"; `app.js` returns 200 |
| S9 Logs | Scan the startup and request logs | No errors, stack traces, or new deprecation warnings |

These checks are written once as `$TMPDIR/smoke.sh` (using `curl --fail -sS -w '%{http_code}'` and `jq`) and reused for every item.

### Baseline (before the upgrade)

`mvn spring-boot:run`, then run `smoke.sh` and save `/shop/v3/api-docs` to `$TMPDIR/baseline-api-docs.json` along with representative responses. Then stop the app; Compose runs `down`.
[SIM] A baseline api-docs file existed. Outcome 4 compares against it.

## 2. Run the single stage (3.2.5 → latest 3.x)

```bash
mvn -U org.openrewrite.maven:rewrite-maven-plugin:6.46.1:run --define rewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:6.37.1 --define rewrite.activeRecipes=org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5 --define rewrite.exportDatatables=true
```

[SIM] The recipe succeeds.

## 3. Inspect and validate

| # | Step | Observation |
|---|---|---|
| 3.1 | `git diff`, `mvn help:effective-pom` | [SIM] Parent is now **3.5.12**, which matches the stable target (confirmed in the effective POM). The recipe **did not touch** the `springdoc-openapi` property, which is still `2.3.0`. I noted this as a risk, because springdoc 2.3.x was built against Spring Framework 6.1. |
| 3.2 | V1 `mvn clean verify` | [SIM] PASSED (unit and integration tests). |
| 3.3 | V2 `mvn clean install` | [PLAN] The scenario gives no outcome, so the result is **NOT EVIDENCED**. |
| 3.4 | Approach A: `mvn spring-boot:run` (background, logs captured), wait for readiness, run `smoke.sh` | [SIM] S1 UP. S4 200 with 3 products. S8 page and `app.js` OK. S5 POST without credentials returns 401. S6 create/read/delete OK. **S2 FAILED:** `GET /shop/v3/api-docs` returns **500** and the log shows `NoSuchMethodError: 'void org.springframework.web.method.ControllerAdviceBean.<init>(java.lang.Object)'`. **S3 FAILED:** Swagger UI cannot load the API definition. S7 and the orders part of S5 have no evidence. |
| 3.5 | Diagnosis | The `ControllerAdviceBean(Object)` constructor no longer exists in the Spring Framework 6.2 that Boot 3.5 brings in. springdoc 2.3.0 calls it, so the error comes from a binary incompatibility caused by the stale springdoc pin. The build and tests did not catch it because no test calls `/v3/api-docs`. Only the smoke test exposed it. |
| 3.6 | Fix | Change the springdoc property in the POM from `2.3.0` to **`2.8.9`** (the springdoc 2.8.x line targets Boot 3.5 / Framework 6.2; the version comes from the scenario). Then stop Approach A and run Compose `down`. [SIM] I did not edit any real file. |
| 3.7 | Rerun V1 `mvn clean verify` | [SIM] PASSED. |
| 3.8 | Rerun V2 `mvn clean install` | [PLAN] **NOT EVIDENCED.** |
| 3.9 | Rerun Approach A and the full `smoke.sh` | [SIM] All of S1–S4, S5 (products POST), S6, and S8 PASSED. api-docs returns 200 and its paths **match the baseline**. Swagger UI loads the definition. S7, the orders part of S5, and S9 are **not evidenced** (the scenario's "every check above" only lists the outcome-3 checks). After the run: stop the app and run Compose `down`. |
| 3.10 | Approach B: `mvn spring-boot:build-image`, `docker compose -f infra/compose.yaml --profile app up`, run `smoke.sh`, then `docker compose -f infra/compose.yaml --profile app down` | [SIM] Image built, Compose started, and the same checks all PASSED. |
| 3.11 | Cleanup and final diff | [PLAN] Stop only the processes and containers I started. The expected final diff is the Boot parent change to 3.5.12, the other recipe edits, and the springdoc property change to 2.8.9. |

---

## 4. Report

The scenario's evidence shows the upgrade working, including one fix that only a smoke test could catch. The stage is still **not ready**: three required checks have no simulated result. I did not run any command or change any file.

**Version:** 3.2.5 → 3.5.12 (older 3.x moved to the latest stable 3.x). The source for 3.5.12 is the fixture's release metadata, and the effective POM confirms it. There is no major-version change.

**Changes:**
- The pinned recipe `UpgradeSpringBoot_3_5` (rewrite-spring 6.37.1) upgraded Boot.
- Fix: the recipe left `springdoc-openapi` at 2.3.0, which is not compatible with Spring Framework 6.2. Calling `/v3/api-docs` caused a 500 with `NoSuchMethodError: ControllerAdviceBean.<init>(Object)`. I bumped the pin to 2.8.9.

**Validation checklist:**

| Item | Source | Command(s) | Result | Smoke test | Evidence |
|---|---|---|---|---|---|
| V1 | AGENTS.md → Verification | `mvn clean verify` | PASSED [SIM], before and after the fix | Covered by the Approach A smoke run after the fix (same code) | All unit and integration tests pass |
| V2 | Skill §3 step 2 | `mvn clean install` | **NOT EVIDENCED** (no simulated outcome) | — | Must be run before the stage counts as ready |
| A | README.md → Run, Approach A | `mvn spring-boot:run` | Before the fix: **FAILED** (api-docs 500). After the fix: PASSED [SIM] | Passed, apart from the gaps listed below | Readiness UP; 3 products; api-docs paths match the baseline |
| B | README.md → Run, Approach B | `mvn spring-boot:build-image` + `docker compose -f infra/compose.yaml --profile app up` | PASSED [SIM] | Same checks passed | Image built; container ready |

**Smoke tests (after the fix, Approach A and Approach B):**

| Check | URL | Result |
|---|---|---|
| Health | `/shop/actuator/health/readiness` | UP [SIM] |
| API docs | `/shop/v3/api-docs` | 200; paths match the pre-upgrade baseline [SIM] (it was 500 before the fix) |
| Swagger UI | `/shop/swagger-ui/index.html` (configured as `/swagger-ui.html`) | Loads the definition [SIM] (it failed before the fix) |
| Products, public | `GET /shop/v1/products` | 200, 3 products [SIM] |
| Security, no credentials | `POST /shop/v1/products` | 401 [SIM] |
| Authenticated write | create → read → delete with the admin token | OK, nothing left behind [SIM] |
| Web UI | `/shop/` and `app.js` | "Shop Catalog" page; `app.js` loads [SIM] |
| Orders | `GET /shop/v1/orders`, without a token (401/403) and with the admin token (200) | **NOT EVIDENCED** |
| Logs | Scan the startup and request logs | **NOT EVIDENCED** after the fix |

**Status:** The stage is **not yet ready**. The upgrade, the springdoc fix, `mvn clean verify`, and both run approaches passed in the simulated evidence. Three required checks have no result in the scenario:
1. `mvn clean install`
2. the `OrderController` security checks (without a token and with the admin token)
3. the post-fix log scan

I am not claiming they passed.

**Next:** Should I run these three remaining checks, or will you waive them? I'll take that decision first. After they pass or you waive them, I'll ask whether you want any more validation. No major-version stage is pending: 3.5.12 is the requested destination, so I will not propose Boot 4.x unless you ask for it.
