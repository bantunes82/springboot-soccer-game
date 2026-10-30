# Spring Boot 3.x stabilization — decision trace (simulated)

> **Execution disclaimer.** I didn't run anything. I didn't execute Maven, Docker, curl, or any other command, and I didn't change any application files. Every outcome below comes from the isolated fixture `scenario.md` and is labelled **[SIM]**. Lines labelled **[DECISION]** are my reasoning. The fixture's versions and release metadata are test inputs, not claims about real releases.

---

## 1. Inspect the project

| # | Action / exact command | Observation |
| --- | --- | --- |
| 1.1 | `git status --porcelain` | [SIM] The working tree is clean, so no user changes need protecting. |
| 1.2 | `find . -name README.md -o -name AGENTS.md -o -name CLAUDE.md` | [SIM] Found the root `README.md` (section "Run") and `AGENTS.md` (section "Verification"). No `CLAUDE.md` exists. |
| 1.3 | Read the root `pom.xml`. Then run `mvn help:effective-pom` to confirm the inherited version. | [SIM] The parent is `spring-boot-starter-parent:3.2.5`, so the effective Boot version is **3.2.5**. |
| 1.4 | `mvn -v`, `java -version`, `docker info` | [SIM] `mvn` is the launcher and there is no wrapper. JDK 21 is active. Docker is running. |
| 1.5 | Inspect the code and configuration for interfaces the app exposes | [SIM] Settings: `server.port=8081` and `context-path=/shop`. springdoc `2.3.0` is pinned through a property, with `springdoc.swagger-ui.path=/swagger-ui.html`. `ProductController` has public GETs and admin-only POST/DELETE. `OrderController` has an admin-only GET. A static `index.html` ("Shop Catalog") loads `app.js`. The app is a Keycloak resource server, and `scripts/get-token.sh admin admin` prints a token. Flyway seeds 3 products. |
| 1.6 | Destination | [DECISION] The user asked for "latest 3.x". The fixture metadata says the latest stable 3.x is **3.5.12**. Current version 3.2.5 is an older 3.x, so the stage is **3.2.5 → 3.5.12 using the pinned 3.5 recipe**. This is a minor-version stage within the same major, so it doesn't cross a major boundary. |

### Documented validation checklist (built now, kept for the whole stage)

| ID | Source | Command(s) / working dir | Prerequisites | Success signal |
| --- | --- | --- | --- | --- |
| V1 | `AGENTS.md` → Verification | `mvn clean verify` (repo root) | JDK 21, Docker for integration tests | Exit 0; unit and integration tests pass |
| V2 | `README.md` → Run → Approach A | `mvn spring-boot:run` (repo root) | Docker + Compose (PostgreSQL) | `curl --fail http://127.0.0.1:8081/shop/actuator/health/readiness` succeeds, plus smoke checks S1–S8 |
| V3 | `README.md` → Run → Approach B | `mvn spring-boot:build-image`, then `docker compose -f infra/compose.yaml --profile app up` | Docker, buildpacks | Image builds; same readiness check and smoke checks S1–S8 |
| V0 | Skill §3.2 | `mvn clean install` (repo root) | Same as V1 | Exit 0, with tests not skipped |

**Smoke checks for each run approach (S1–S8).** No document mentions Swagger, the API, or the web page. Under skill §3.5, I still exercise the documented readiness check, the Swagger/API docs, the public endpoints, and authenticated calls, because a token helper exists. I derived these checks from the code inspection in 1.5:

| ID | Check | Expected |
| --- | --- | --- |
| S1 | `curl --fail http://127.0.0.1:8081/shop/actuator/health/readiness` | 200, `UP` |
| S2 | `curl -s http://127.0.0.1:8081/shop/v1/products` and `GET /shop/v1/products/{id}` | 200; 3 seeded products |
| S3 | `curl -s http://127.0.0.1:8081/shop/` and `GET /shop/app.js` | 200; "Shop Catalog" page; script loads |
| S4 | `curl -s -o /dev/null -w '%{http_code}' -X POST http://127.0.0.1:8081/shop/v1/products` (no token) | 401 |
| S5 | `TOKEN=$(scripts/get-token.sh admin admin)`, then POST, GET, and DELETE on `/shop/v1/products` with `Authorization: Bearer $TOKEN`. Also `GET /shop/v1/orders` with and without the token. | Create, read, and delete succeed; orders return 200 with the token and 401 without it |
| S6 | `curl --fail http://127.0.0.1:8081/shop/v3/api-docs` | 200; paths match the pre-upgrade baseline |
| S7 | Open `http://127.0.0.1:8081/shop/swagger-ui/index.html` (also follow the `/shop/swagger-ui.html` redirect) | The UI loads the API definition |
| S8 | Check the application log | No startup or runtime errors |

### Pre-upgrade baseline

[DECISION] Before running the recipe, I capture the baseline that S6 compares against. On 3.2.5, I start the app the Approach A way and run:

```bash
curl -s http://127.0.0.1:8081/shop/v3/api-docs | jq -S '.paths | keys' > /private/var/folders/pq/4j1gndmx4_9_rslkf1_1h8xh0000gp/T/opencode/api-docs-baseline.json
```

After that, I stop the app, which triggers Compose `down`. [SIM] The fixture confirms that a pre-upgrade baseline was captured.

## 2. Run the stage recipe

```bash
mvn -U org.openrewrite.maven:rewrite-maven-plugin:6.46.1:run --define rewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:6.37.1 --define rewrite.activeRecipes=org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5 --define rewrite.exportDatatables=true
```

[SIM] The recipe succeeds.

## 3. Inspect and validate

| Step | Command | Result |
| --- | --- | --- |
| 3.1 | `git diff`, then `mvn help:effective-pom \| grep -A2 spring-boot-starter-parent` | [SIM] The parent is now **3.5.12** in the effective POM, which matches the verified stable target. The springdoc property is still `2.3.0` because the recipe didn't touch it. [DECISION] springdoc 2.3.0 was built for Boot 3.2 / Spring Framework 6.1. I note it as a risk to check during the smoke tests. |
| 3.2 | `mvn clean verify` (V1) | [SIM] Passes: all unit and integration tests pass. |
| 3.3 | `mvn clean install` (V0) | Not covered by the fixture. The scenario gives no outcome for this command. See the Status section. |
| 3.4 | Integration-test wiring | [DECISION] `AGENTS.md` says `clean verify` runs the integration tests. No extra profile is documented. |
| 3.5 | Re-read `README.md` and `AGENTS.md` | [SIM] Nothing changed, so the checklist stays the same. |
| 3.6 | V2: `mvn spring-boot:run` (in the background, logging to `.../T/opencode/approachA.log`), then S1–S8 | [SIM] S1 is UP. S2 returns 200 with 3 products. S3 serves the page and `app.js`. S4 returns 401. S5 authenticated create, read, and delete succeed. **S6 fails**: `GET /shop/v3/api-docs` returns **500**, and the log shows `NoSuchMethodError: 'void org.springframework.web.method.ControllerAdviceBean.<init>(java.lang.Object)'`. **S7 fails**: Swagger UI can't load the API definition. |

**Diagnosis.** [DECISION] This is a binary incompatibility. springdoc 2.3.0 calls a `ControllerAdviceBean` constructor that the Spring Framework version shipped with Boot 3.5.x no longer has. The tests didn't catch it, so only the smoke test revealed it. I don't patch around it in code. The fix is to align springdoc with Boot 3.5.

**Fix.** Bump the springdoc version property in `pom.xml` from `2.3.0` to `2.8.9`. This is a simulated edit; I made no real file change.

**Rerun the affected items.**

| Step | Command | Result |
| --- | --- | --- |
| 3.7 | Stop Approach A (Ctrl-C; Boot runs Compose `down`), then `mvn clean verify` | [SIM] Passes. |
| 3.8 | `mvn spring-boot:run`, then S1–S8 | [SIM] All pass, including S6 and S7. The api-docs paths match the baseline (`diff` of the `jq -S '.paths \| keys'` output shows no differences). Then I stop the app, which triggers Compose `down`. |
| 3.9 | V3: `mvn spring-boot:build-image`, then `docker compose -f infra/compose.yaml --profile app up -d`, then S1–S8, then `docker compose -f infra/compose.yaml --profile app down` | [SIM] The image builds, Compose starts, and every check passes. |
| 3.10 | Cleanup | Only the processes and containers I started for validation are stopped. The final diff contains the parent bump to 3.5.12 plus the springdoc bump to 2.8.9. |

---

## Final report

**Version.** Spring Boot 3.2.5 → **3.5.12** (stage: latest stable 3.x). The target comes from the fixture release metadata, and the result is confirmed in the effective POM. All of this is simulated.

**Changes.**
- I ran the pinned OpenRewrite recipe `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5` (rewrite-spring 6.37.1, plugin 6.46.1).
- Manual fix: `springdoc-openapi-starter-webmvc-ui` 2.3.0 → **2.8.9**. Version 2.3.0 caused a `NoSuchMethodError` on `ControllerAdviceBean.<init>(Object)` with Boot 3.5, which broke `/v3/api-docs` and Swagger UI. The test suite didn't catch this; only the smoke test did.

**Validation.** All results below are simulated from `scenario.md`; none were executed for real.

| Item | Source | Command(s) | Result | Evidence [SIM] |
| --- | --- | --- | --- | --- |
| V1 | `AGENTS.md` → Verification | `mvn clean verify` | PASSED (before and after the fix) | All unit and integration tests pass |
| V2 | `README.md` → Run → Approach A | `mvn spring-boot:run` + S1–S8 | PASSED after the fix (failed first) | Readiness UP. Products 200 (3). Page and `app.js` load. Unauthenticated POST returns 401. Admin create, read, and delete OK. api-docs 200 and matches the baseline. Swagger UI OK. Before the fix: api-docs returned 500 with `NoSuchMethodError` |
| V3 | `README.md` → Run → Approach B | `mvn spring-boot:build-image`; `docker compose -f infra/compose.yaml --profile app up` + S1–S8 | PASSED | Image built, Compose up, all smoke checks pass |
| V0 | Skill §3.2 | `mvn clean install` | NOT EVIDENCED | The fixture gives no outcome for this command. `clean verify` covers the same phases except `install` |

Fixture gaps for the smoke checks:
- The fixture doesn't explicitly report `GET /v1/orders` (S5).
- It doesn't explicitly report the `/swagger-ui.html` redirect (S7).
- It reports admin create, read, and delete generally and the "same checks" for Approach B.

I list these checks as part of the plan, but I have no specific evidence for them.

**Status.** Every documented checklist item (V1–V3) passed in the simulation, after the springdoc fix. The stage is ready apart from one gap: the skill-required `mvn clean install` has no simulated outcome. It should be run, or you can waive it because `clean verify` passed, before the stage is called complete. Nothing was actually executed, and no repository files were changed.

**Next.** You asked for the latest 3.x, and that's reached, so no major migration is part of this request. Shall I run (or would you like to waive) `mvn clean install`? Is there any other validation you'd like? Moving on to Spring Boot 4.x would be a separate stage, and I would only start it with your explicit confirmation.
