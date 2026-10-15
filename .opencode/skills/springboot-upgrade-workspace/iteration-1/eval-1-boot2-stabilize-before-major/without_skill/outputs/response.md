# Immediate stage: stabilize Spring Boot 2.x

This is a read-only simulation using `scenario.md` and `springboot-upgrade-workflow.md`. No Maven, startup, or HTTP commands were executed, and no application files were changed. Commands below describe the actions that would be taken in a real upgrade; all reported outcomes are supplied simulated observations.

## Ordered decision trace

1. **Establish the starting version and immediate target.** The fixture reports a clean working tree and a root `pom.xml` with parent `org.springframework.boot:spring-boot-starter-parent:2.3.12.RELEASE`. The reactor contains `domain` and `api`, with the API inheriting the root Boot version. Supplied metadata identifies `2.7.18` as the latest stable 2.x, excluding previews. Under workflow steps 2–2.2, the immediate stage is therefore **2.3.12.RELEASE → 2.7.18**. The ultimate 4.x destination does not bypass stabilization or the subsequent major-version confirmation.

2. **Apply the pinned Boot 2.7 recipe from the repository root.** The exact proposed command is:

   ```bash
   mvn -U org.openrewrite.maven:rewrite-maven-plugin:6.46.1:run --define rewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:6.37.1 --define rewrite.activeRecipes=org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_7 --define rewrite.exportDatatables=true
   ```

   **Simulated observation:** The recipe succeeds. Following workflow step 3, inspect the resulting root `pom.xml` and module inheritance/resolved versions: the root parent is now `2.7.18`, and both modules resolve `2.7.18`. This conclusion comes from the fixture's post-recipe evidence, rather than from assuming what the recipe name guarantees.

3. **Run full reactor validation.** From the root, the proposed command is:

   ```bash
   mvn clean install
   ```

   The default lifecycle binds integration tests to `verify`, which `install` includes, so this command is the supplied compilation, unit-test, and integration-test validation path.

   **Simulated observation:** The first build fails during compilation in `api` because a custom class extends `WebMvcConfigurerAdapter`. The stage is not validated at this point; later migration steps must wait.

4. **Repair the compilation failure and repeat validation.** In a real upgrade, locate the affected API configuration class, replace its `WebMvcConfigurerAdapter` import with `org.springframework.web.servlet.config.annotation.WebMvcConfigurer`, and change `extends WebMvcConfigurerAdapter` to `implements WebMvcConfigurer`. Preserve its configuration annotations and compatible overridden method implementations. The fixture states those overrides are compatible with the interface; it does not supply a class name or source path, so none is invented here.

   After that repair, rerun from the root:

   ```bash
   mvn clean install
   ```

   **Simulated observation:** This second build passes compilation, unit tests, and integration tests. The repair and successful revalidation are simulated only; no source edit or build was performed during this evaluation.

5. **Complete the documented runtime validation.** Following workflow steps 5–5.2, use the README instructions supplied in the fixture. Its root build requirement has been satisfied by the simulated successful rerun above. Start the API in a terminal from the root:

   ```bash
   java -jar api/target/api.jar --spring.profiles.active=smoke
   ```

   Once startup completes, run in a second terminal:

   ```bash
   curl --fail http://localhost:8080/health
   ```

   Then stop the application process started for this check with `Ctrl-C` in its original terminal. In an actual run, perform that cleanup even if the health check fails.

   **Simulated observation:** README startup and the health check pass. Cleanup is a required proposed action; the fixture does not separately report its outcome. No application process was started in this evaluation.

6. **Report this stage and pause for the user's decisions.** The supplied evidence supports readiness of the simulated 2.7.18 stage after repair, full build validation, and the README smoke check. Ask about additional validation, then ask for confirmation to proceed to 3.x under workflow steps 6–7. The user has not supplied that post-stage confirmation. Do not invoke the Boot 3.x or 4.x recipes at this point.

## Final message to the user

In the supplied simulation, the Spring Boot 2.x stabilization stage is ready at **2.7.18**. The pinned Boot 2.7 recipe succeeded, and both modules resolve that version. The initial API compilation failure was resolved by adapting the custom configuration class to implement `WebMvcConfigurer`; the subsequent `mvn clean install` passed compilation, unit tests, and integration tests. The README startup and `/health` smoke check also passed.

These are simulated outcomes from the fixture: I did not run Maven, modify application files, or start the application.

Would you like any additional validation of the 2.7.18 stage? After that, do you confirm proceeding with the Spring Boot 3.x migration as the next stage toward 4.x?
