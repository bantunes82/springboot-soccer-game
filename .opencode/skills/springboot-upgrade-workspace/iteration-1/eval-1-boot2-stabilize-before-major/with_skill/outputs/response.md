# Simulated decision trace: stabilize Spring Boot 2.x

This is a read-only workflow simulation using `scenario.md`, the skill, and `springboot-upgrade-workflow.md`. All outcomes below are supplied simulated observations. No Maven, startup, or HTTP commands were executed, and no application files were changed. Commands shown are the actions for a real run.

## 1. Inspect and select the immediate stage

- The supplied root POM uses `org.springframework.boot:spring-boot-starter-parent:2.3.12.RELEASE`. The reactor contains `domain` and `api`; the API inherits the root Boot version.
- The fixture reports a clean working tree and specifies `mvn` as the launcher. Run the stage from the reactor root.
- Select **2.3.12.RELEASE → 2.7.18** as the immediate stage. The source for the stable target is the fixture's supplied repository metadata, which identifies `2.7.18` as the latest stable 2.x and excludes previews. This is simulated release evidence, not a claim about current public releases.
- Although the ultimate destination is 4.x, workflow steps 2–2.2 require current-major stabilization first. Complete and validate this stage, then obtain confirmation before 3.x. Do not skip 3.x on the way to 4.x.

The pre-execution message would be: “The service is on Spring Boot 2.3.12.RELEASE. I will first stabilize it on 2.7.18, the latest stable 2.x identified by the supplied metadata, and validate the full reactor and README smoke check before asking about migration to 3.x.”

## 2. Apply the pinned Boot 2.7 recipe

Use the workflow's exact command from the root:

```bash
mvn -U org.openrewrite.maven:rewrite-maven-plugin:6.46.1:run --define rewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:6.37.1 --define rewrite.activeRecipes=org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_7 --define rewrite.exportDatatables=true
```

**Simulated observation:** the recipe succeeds. Inspect its diff and the root/module POMs rather than treating exit success as version verification. The supplied resulting parent is `2.7.18`, and both modules resolve `2.7.18`, matching the selected target. Review all recipe changes before validation.

## 3. Build, repair, and repeat validation

Following the README and workflow, run from the root:

```bash
mvn clean install
```

**Simulated observation:** the first build fails compiling `api` because a custom subclass of `WebMvcConfigurerAdapter` no longer compiles. This stage is incomplete at this point.

Repair the affected class by replacing its `WebMvcConfigurerAdapter` import with `org.springframework.web.servlet.config.annotation.WebMvcConfigurer` and changing `extends WebMvcConfigurerAdapter` to `implements WebMvcConfigurer`. Preserve the existing overridden methods and configuration behavior; the fixture explicitly states that the overrides are compatible with the interface. The fixture does not supply a class name or file path, so no specific source file is invented here.

After that repair, rerun the full root command:

```bash
mvn clean install
```

**Simulated observation:** compilation, unit tests, and integration tests pass. Integration tests are bound to `verify` in the default lifecycle; `install` includes that phase, so the supplied configuration requires no separate integration-test profile. Do not skip tests.

## 4. Perform the documented runtime check

Once the build passes, follow the supplied README startup instruction:

```bash
java -jar api/target/api.jar --spring.profiles.active=smoke
```

With the application running, use another terminal for:

```bash
curl --fail http://localhost:8080/health
```

**Simulated observation:** startup and the health check pass. Stop the foreground Java process started for this check with Ctrl+C in its terminal. Clean up only resources started for this validation. In a real run, inspect the final diff for the Boot parent update and compatible MVC repair, and retain the build/test and smoke-check results for reporting. No final diff or logs beyond the fixture's stated outcomes are available in this simulation.

## 5. Report and pause

The simulated evidence establishes readiness for the **2.x stabilization stage**: the actual resulting version matches the supplied stable target, the compilation failure is repaired, the full build including unit/integration tests passes, and README runtime validation passes. The 3.x and 4.x migrations remain pending. The request for 4.x does not replace the workflow's post-stage confirmation.

### Final message to the user

> In this simulation, Spring Boot was stabilized from **2.3.12.RELEASE to 2.7.18** across `domain` and `api`. The target is the latest stable 2.x according to the fixture's supplied repository metadata.
>
> The pinned Boot 2.7 OpenRewrite recipe succeeded. The first build exposed an API compilation failure; adapting the custom MVC configuration to implement `WebMvcConfigurer` resolved it. The repeated `mvn clean install` passed compilation, unit tests, and integration tests. The README smoke-profile startup and `/health` check also passed.
>
> The 2.x stage is ready according to the simulated evidence. No commands were actually executed or application files changed during this exercise.
>
> Would you like any additional validation before moving on? Do you confirm proceeding to the latest stable **Spring Boot 3.x** stage next, on the way to 4.x?

Wait for the user's answer before executing a later stage.
