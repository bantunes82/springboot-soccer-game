# Confirmed Spring Boot 3.x → 4.x transition: decision trace

> **Evidence status:** Every observation below comes from the **simulated** fixture
> (`evals/fixtures/boot4-confirmed-transition/scenario.md`). I did **not** run Maven,
> start any process, run `curl`, or change any application file. "Observed" means
> "given by the scenario", not "seen during real execution".

## 1. Inspection (from scenario evidence)

| Item | Simulated observation |
| --- | --- |
| Effective Boot version | Root parent `org.springframework.boot:spring-boot-starter-parent:3.5.12` |
| Stable-release evidence | Fixture metadata: `3.5.12` = latest stable 3.x; `4.1.2` = latest stable 4.x (no snapshot/milestone/RC) |
| Prior stage | The 3.x build, integration tests, and smoke checks already passed |
| Authorization | User explicitly confirmed moving to 4.x, which authorizes this single major transition |
| Toolchain | `mvn` (no wrapper); compatible JDK; clean working tree, so there are no user changes to preserve |
| Recipe availability | Pinned Arconia artifact present in the fixture's configured repository |

**Documented validation checklist** (from the scenario's README files):

1. Root `README.md`: run `mvn clean install` from the repository root. Integration tests run in this lifecycle, so no extra profile or command is needed. Success means exit code 0, with compilation, unit tests, and ITs all passing.
2. `service/README.md`: run `java -jar service/target/service.jar --spring.profiles.active=smoke`, then `curl --fail http://localhost:8080/actuator/health` and `curl --fail http://localhost:8080/teams`. Both checks must succeed. Then stop the process.

**Smoke-test plan:** The only interfaces in the evidence are health (`/actuator/health`) and the REST endpoint `/teams`. The scenario includes no code or config, so I can't derive further interfaces such as Swagger/OpenAPI, security, or a web UI. In a real run I would inspect controllers, `application*.properties`, springdoc, and security config, then add anything I found. Here I record this as a limit of the evidence, not as proof that those interfaces are absent. There was also no baseline OpenAPI document to compare against.

## 2. Stage selection

Starting state: latest stable 3.x, and the next migration is confirmed. Immediate stage: **upgrade to the latest stable 4.x (4.1.2) with the 4.1 recipe**. No major version is skipped.

## 3. Ordered decision trace (successful branch)

1. **Run the pinned recipe exactly as specified**, from the repository root. I would not change coordinates or recipe IDs:
   ```bash
   mvn -U org.openrewrite.maven:rewrite-maven-plugin:6.46.1:run -Drewrite.recipeArtifactCoordinates=io.arconia.migrations:rewrite-spring:1.2.3 -Drewrite.activeRecipes=io.arconia.rewrite.spring.boot4.UpgradeSpringBoot_4_1
   ```
   *Simulated outcome 1:* the recipe succeeds.
2. **Inspect the version and diff.** I would review the diff and POMs, then resolve the effective version across the reactor, for example with `mvn help:effective-pom` on the service module. A successful recipe exit is not proof of the version.
   *Simulated:* the parent becomes `4.1.2`, and the service's effective POM confirms `4.1.2`. This matches the verified latest stable 4.x, and there is no inconsistency to investigate.
3. **Checklist item 1: `mvn clean install`** from the root, without skipping tests.
   *Simulated outcome 2:* compilation, unit tests, and integration tests all pass, so no fixes are needed.
4. **Smoke test for item 1.** This is a test-only item, so I would start the app with the primary documented run approach and smoke test it. Item 2 is that same run approach, so one run covers both, as long as nothing changes between them.
5. **Checklist item 2:** re-read the README files first; no changes are expected. Then:
   ```bash
   java -jar service/target/service.jar --spring.profiles.active=smoke   # background, capture logs
   curl --fail http://localhost:8080/actuator/health
   curl --fail http://localhost:8080/teams
   ```
   In a real run I would also check that health reports `UP` and that `/teams` returns non-empty JSON, and I would scan the logs for errors or deprecation warnings.
   *Simulated outcome 3:* startup, health, and teams checks all pass.
6. **Clean up:** stop only the `java -jar` process started for validation, and record each item's evidence.

### Final message (successful branch)

> **Version:** Spring Boot 3.5.12 → **4.1.2** (stage: 4.x). Stable-release evidence: the fixture metadata lists 4.1.2 as the latest stable 4.x. The root parent and the service's effective POM both resolve to 4.1.2.
>
> **Changes:** Recipe `io.arconia.rewrite.spring.boot4.UpgradeSpringBoot_4_1` (`io.arconia.migrations:rewrite-spring:1.2.3`, rewrite-maven-plugin 6.46.1). No manual fixes were needed.
>
> **Validation** (all results simulated by the scenario, not executed):
>
> | Source | Command(s) | Result | Smoke test | Evidence |
> | --- | --- | --- | --- | --- |
> | `README.md` → build | `mvn clean install` (root) | PASSED | Covered by the `service/README.md` run below (no changes in between) | Compile, unit tests, and ITs passed |
> | `service/README.md` → run | `java -jar service/target/service.jar --spring.profiles.active=smoke` + 2× `curl --fail` | PASSED | PASSED | Started; `/actuator/health` OK; `/teams` OK; process stopped |
>
> **Smoke tests:** `http://localhost:8080/actuator/health` passed and `http://localhost:8080/teams` passed. These are the only interfaces the evidence documents. No baseline was available to compare against, and no other interfaces (Swagger, auth, web UI) could be identified from the supplied evidence.
>
> **Status:** The 4.x stage is complete and every documented checklist item passed (in simulation). 4.1.2 is the latest stable major in the fixture, so no further major-version stage is pending.
>
> **Next:** Do you want any additional validation, such as a broader smoke test of other endpoints or extra profiles?

## 4. Alternate branch: pinned recipe artifact fails to resolve

This replaces outcome 1 and is reported separately. Its status is not combined with the successful branch.

- Maven reports that `io.arconia.migrations:rewrite-spring:1.2.3` cannot be resolved. There is no evidence that the recipe ran or that any POM changed.
- **Actions:** I would stop at step 1. I would not swap in a different recipe, change the artifact version, or edit the parent to `4.1.2` by hand. I would check the diff to confirm the tree is unchanged and still on `3.5.12`. I would not run steps 2–6, because there is no 4.x result to validate, and I would not claim any 4.x validation.
- **Report:**
  > **Version:** still Spring Boot **3.5.12**. The 4.x stage did **not** run.
  >
  > **Cause:** Maven could not resolve the pinned recipe artifact `io.arconia.migrations:rewrite-spring:1.2.3`, using the command `mvn -U org.openrewrite.maven:rewrite-maven-plugin:6.46.1:run -Drewrite.recipeArtifactCoordinates=io.arconia.migrations:rewrite-spring:1.2.3 -Drewrite.activeRecipes=io.arconia.rewrite.spring.boot4.UpgradeSpringBoot_4_1`. No files changed. The existing 3.x validation still stands.
  >
  > **Status:** The 4.x migration is blocked. It is neither started nor complete.
  >
  > **How would you like to proceed?** Options: (a) fix repository access or mirror settings so the pinned artifact resolves, then retry the same command; (b) approve a different recipe artifact or version that you specify; (c) do a manual migration following official Spring Boot 4 guidance. I'll wait for your answer before doing anything else.
