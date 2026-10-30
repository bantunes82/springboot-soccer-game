# Spring Boot 3.x stabilization — decision trace (simulated)

> **Execution disclaimer:** Nothing below was actually executed. No Maven, Docker, curl, or git
> command was run and no application file was changed. Every "result" is a **simulated
> observation** taken from `evals/fixtures/boot3-smoke-interfaces/scenario.md`. Commands are the
> exact commands I would run. Where the scenario gives no outcome for a step the skill requires,
> I mark it **NOT EVIDENCED** and do not assume it passed.

---

## 1. Inspect the project

| # | Action / command | Simulated observation |
| --- | --- | --- |
| 1.1 | Find docs: `find . -name README.md -o -name AGENTS.md -o -name CLAUDE.md` (excluding `target/`, `node_modules/`) | Root `README.md` ("Run": Approach A, Approach B) and `AGENTS.md` ("Verification"). No `CLAUDE.md`. |
| 1.2 | `git status --porcelain` | Clean working tree, so there are no user changes to keep separate. |
| 1.3 | Resolve the Boot version: inspect `pom.xml`, then `mvn help:effective-pom -Doutput=$TMP/effective-pom-before.xml` | Parent `spring-boot-starter-parent:3.2.5`, so this is **older 3.x**. |
| 1.4 | Toolchain: `mvn -v`, `docker info` | `mvn` (no wrapper), JDK 21, Docker running. |
| 1.5 | Target: the user asked for "latest 3.x". Check stable-release metadata (Maven metadata for `spring-boot-starter-parent`, without snapshots, milestones or RCs). | Fixture metadata gives **3.5.12** as the latest stable 3.x. Stage: **3.2.5 → 3.5.12, using the pinned 3.5 recipe**. 4.x is out of scope. |

### Documented validation checklist (fixed for the whole stage)

| ID | Source | Command(s) (working dir: repo root) | Prerequisites | Success signal |
| --- | --- | --- | --- | --- |
| V1 | Skill §3 step 2 | `mvn clean install` | JDK 21, Maven, Docker (ITs) | exit 0, tests not skipped |
| V2 | `AGENTS.md` → Verification | `mvn clean verify` | Docker for ITs | exit 0; unit and IT counts > 0, 0 failures |
| V3 | `README.md` → Run → Approach A | `mvn spring-boot:run` (background, log to `$TMP/approachA.log`) | Docker/Compose (PostgreSQL) | `curl --fail http://127.0.0.1:8081/shop/actuator/health/readiness`, then the full smoke test |
| V4 | `README.md` → Run → Approach B | `mvn spring-boot:build-image`, then `docker compose -f infra/compose.yaml --profile app up -d`, cleanup with `docker compose -f infra/compose.yaml --profile app down` | Docker, Compose | image built, container ready, full smoke test |

Every test-only item (V1, V2) is followed by an Approach A smoke run.

### Smoke-test plan (from code and config, because the docs mention none of it)

Base URL `http://127.0.0.1:8081/shop` (`server.port=8081`, `server.servlet.context-path=/shop`).
I put these in `$TMP/smoke.sh` and reuse the script for every item:

| ID | Interface | Check |
| --- | --- | --- |
| S1 | Health | `GET /shop/actuator/health/readiness` → 200, `.status=="UP"` with every component UP (db) |
| S2 | OpenAPI | `GET /shop/v3/api-docs` → 200 JSON. `jq '.paths\|keys'` diffed against the baseline. |
| S3 | Swagger UI | `GET /shop/swagger-ui.html` (configured path) → redirect to `/shop/swagger-ui/index.html` → 200 HTML referencing `swagger-ui-bundle.js`. `GET /shop/v3/api-docs/swagger-config` → `url` points to a reachable api-docs. |
| S4 | ProductController (public) | `GET /shop/v1/products` → 200, `application/json`, length 3 (seed). `GET /shop/v1/products/{id}` for a seeded id → 200 with the expected fields. |
| S5 | Security, no token | `POST /shop/v1/products` → 401. `GET /shop/v1/orders` → 401. |
| S6 | Security, authenticated | `TOKEN=$(scripts/get-token.sh admin admin)`, then `POST /v1/products` → 201, `GET` it back → 200, `DELETE` → 2xx, `GET` → 404 (no test data left behind). `GET /shop/v1/orders` with the token → 200 JSON. |
| S7 | Web UI | `GET /shop/` → 200 HTML containing `<title>Shop Catalog</title>`. `GET /shop/app.js` → 200. |
| S8 | Logs | `grep -E 'ERROR\|Exception\|deprecat' $TMP/<item>.log` |

### Baseline (before the recipe)

`mvn spring-boot:run`, wait for readiness, run `smoke.sh`, then save `curl -sS .../shop/v3/api-docs > $TMP/api-docs-baseline.json` and the representative responses. Stop the app, which runs Compose `down`.
*Simulated:* a baseline was captured (scenario step 4 refers to it).

---

## 2. Run the stage recipe

```bash
mvn -U org.openrewrite.maven:rewrite-maven-plugin:6.46.1:run --define rewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:6.37.1 --define rewrite.activeRecipes=org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5 --define rewrite.exportDatatables=true
```

*Simulated:* success.

## 3. Inspect and validate

**3.1 Snapshot recipe changes before touching anything:**
`git diff --stat > $TMP/recipe-diffstat.txt`, `git diff > $TMP/recipe.diff`, `git status --porcelain > $TMP/recipe-status.txt`. I also keep the `Changes have been made to … by:` log lines and `target/rewrite/datatables/`.
Then `mvn help:effective-pom`.
*Simulated:* the parent is now **3.5.12**, which matches the verified target, and the effective POM confirms it. The springdoc version property is **unchanged (2.3.0)**. I note this as a risk: springdoc 2.3.x targets Spring Framework 6.1, while Boot 3.5 ships 6.2.

**3.2 V1 `mvn clean install`.** **NOT EVIDENCED:** the scenario gives no outcome. This required step must still run, and the result has to be recorded.

**3.3 V2 `mvn clean verify`.** *Simulated:* PASSED (all unit and integration tests). The ITs run inside `verify`, so no extra profile is needed.

**3.4 V3 Approach A plus the smoke test** (this also serves as the smoke run for V2):
`mvn spring-boot:run > $TMP/approachA.log 2>&1 &`, poll readiness, then `$TMP/smoke.sh`.

| Check | Simulated result |
| --- | --- |
| S1 readiness | UP ✅ |
| S4 `GET /shop/v1/products` | 200, 3 products ✅ |
| S7 `GET /shop/`, `app.js` | "Shop Catalog" page, `app.js` loads ✅ |
| S5 unauthenticated `POST /v1/products` | 401 ✅ |
| S6 admin create/read/delete | succeeds ✅ |
| **S2 `GET /shop/v3/api-docs`** | **500 ❌**. Log: `NoSuchMethodError: 'void org.springframework.web.method.ControllerAdviceBean.<init>(java.lang.Object)'` |
| **S3 Swagger UI** | **fails to load the API definition ❌** |

→ **V3 FAILED.** The build and tests pass, but the running app's API docs are broken. Diagnosis: springdoc 2.3.0 calls a `ControllerAdviceBean` constructor that Spring Framework 6.2 (Boot 3.5) removed. The recipe left the pinned springdoc property alone.
Stop the app. The Compose `down` runs on shutdown, and I confirm it with `docker ps`.

**3.5 Fix (agent change).** In the POM property, change `springdoc-openapi-starter-webmvc-ui` from 2.3.0 to **2.8.9** (the springdoc line that supports Boot 3.5). I add this to the agent change log.

**3.6 Rerun the affected items.** The fix changes dependencies, so every item reruns.
- V1 `mvn clean install`: needs a rerun. **NOT EVIDENCED** in the scenario.
- V2 `mvn clean verify`: *simulated* PASSED.
- V3 Approach A plus the full smoke test: *simulated* readiness UP. Products, web UI, 401 and the admin create/read/delete pass. `/v3/api-docs` returns 200, and its **paths match the pre-upgrade baseline**. Swagger UI loads the definition. ✅ This run also covers V2's smoke requirement because nothing changed in between. Stop, then Compose `down`.
- V4 Approach B: `mvn spring-boot:build-image`, then `docker compose -f infra/compose.yaml --profile app up -d`, readiness, `smoke.sh`, then `docker compose -f infra/compose.yaml --profile app down`. *Simulated:* the build succeeds, startup succeeds, and "the same checks all pass" ✅.

**3.7 Cleanup:** I stop only the processes and containers I started (`docker ps` shows none left), then do a final `git diff` review.

---

## 4. Final report

### Version
**3.2.5 → 3.5.12** (stage: 3.x stabilization). The latest stable 3.x comes from the fixture's release metadata, with snapshots, milestones and RCs excluded. The effective POM confirms 3.5.12.

### Changes made by the OpenRewrite recipe
Recipe: `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5` (`org.openrewrite.recipe:rewrite-spring:6.37.1`, plugin `rewrite-maven-plugin:6.46.1`)
- `pom.xml`: `spring-boot-starter-parent` 3.2.5 → 3.5.12.
- The scenario only shows this change. Any further file-level detail would come from `$TMP/recipe.diff`, the Maven log, and the datatables. The recipe did **not** change the springdoc version property.

### Changes made by the AI agent
| File | Change | Reason |
| --- | --- | --- |
| `pom.xml` (springdoc version property) | `springdoc-openapi-starter-webmvc-ui` 2.3.0 → 2.8.9 | Approach A smoke test: `GET /shop/v3/api-docs` returned 500 with `NoSuchMethodError … ControllerAdviceBean.<init>(Object)`, and Swagger UI could not load the definition. springdoc 2.3.0 is incompatible with Spring Framework 6.2. |

No other manual changes. (In this eval I did not actually edit any file.)

### Validation (all simulated)
| Item | Source | Command | Result | Smoke test | Evidence |
| --- | --- | --- | --- | --- | --- |
| V1 | Skill §3.2 | `mvn clean install` | **NOT EVIDENCED** | n/a | The scenario gives no outcome. It must be run before calling the stage ready. |
| V2 | `AGENTS.md` → Verification | `mvn clean verify` | PASSED (before and after the fix) | PASSED (via V3 rerun) | all unit and IT tests passed |
| V3 | `README.md` → Run → Approach A | `mvn spring-boot:run` | FAILED → fixed → **PASSED** | PASSED after the fix | api-docs 500 before the fix; all checks pass after it |
| V4 | `README.md` → Run → Approach B | `mvn spring-boot:build-image`, then `docker compose -f infra/compose.yaml --profile app up` | **PASSED** | PASSED | image built; the same checks passed |

### Smoke tests (base `http://127.0.0.1:8081/shop`, covering V2, V3 and V4 after the fix)
| Interface | URL | Result |
| --- | --- | --- |
| Health | `/actuator/health/readiness` | UP ✅ |
| OpenAPI | `/v3/api-docs` | 200 after the fix (500 before it). Paths **match the baseline** ✅ |
| Swagger UI | `/swagger-ui.html` → `/swagger-ui/index.html` | loads the definition after the fix ✅ |
| Products (public) | `GET /v1/products` | 200, 3 seeded products ✅ |
| Product by id | `GET /v1/products/{id}` | part of the plan, but the scenario reports no separate result: **NOT EVIDENCED** |
| Auth, no token | `POST /v1/products` | 401 ✅ |
| Auth, admin token | `POST` / `GET` / `DELETE /v1/products` | succeeds, no data left behind ✅ |
| Orders | `GET /v1/orders` (401 without a token, 200 with admin) | part of the plan, but the scenario reports no result: **NOT EVIDENCED** |
| Web UI | `/` ("Shop Catalog"), `/app.js` | ✅ |
| Logs | startup and request logs | Before the fix: the `NoSuchMethodError` above. After the fix: the scenario reports no errors, but gives no explicit log-scan result. |

### Status
The functional regression that only the smoke test exposed (broken OpenAPI and Swagger UI) is fixed. `mvn clean verify`, Approach A and Approach B all pass with full smoke tests (simulated).
The stage is **not yet declared fully ready**, because the evidence has gaps:
- `mvn clean install` (V1) has no recorded result.
- `GET /v1/products/{id}`, `GET /v1/orders` (with and without the token) and the post-fix log scan have no recorded results.

These need to be run, or you can explicitly waive them.

### Next
1. Should I run (or do you waive) the open checks above: `mvn clean install`, `/v1/products/{id}`, `/v1/orders` and the post-fix log scan? Would you like any additional validation?
2. Once those are settled, the app will be on the latest stable 3.x. A move to **Spring Boot 4.x** (pinned 4.1 recipe) is a separate major stage, and I will not start it without your confirmation.
