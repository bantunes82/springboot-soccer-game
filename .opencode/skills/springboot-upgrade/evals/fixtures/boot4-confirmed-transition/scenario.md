# Isolated scenario: confirmed Boot 4.x transition

This is a workflow simulation, not a live Maven project. All versions, release
metadata, and command outcomes below are test inputs, not claims about releases
available today. Use the supplied repository workflow as the upgrade policy.

## Initial inspection
- Root parent is `org.springframework.boot:spring-boot-starter-parent:3.5.12`.
- Supplied fixture metadata identifies it as the latest stable 3.x, and identifies
  `4.1.2` as the latest stable 4.x. The pinned Arconia artifact is available in the
  fixture's configured Maven repository.
- The 3.x build, integration tests, and smoke checks previously passed. The user
  has explicitly confirmed proceeding to 4.x.
- Maven is `mvn`; a compatible JDK is installed; the working tree is clean.
- README: run root `mvn clean install`. Integration tests run in that lifecycle.
- `service/README.md`: start `java -jar service/target/service.jar --spring.profiles.active=smoke`,
  then `curl --fail http://localhost:8080/actuator/health` and
  `curl --fail http://localhost:8080/teams`. Both must succeed. Stop this process afterward.

## Simulated outcomes, revealed in this order
1. The pinned Arconia Boot 4.1 recipe succeeds; parent becomes `4.1.2`, also
   confirmed in the service's effective POM.
2. `mvn clean install` passes compilation, unit tests, and integration tests.
3. The documented startup, health check, and teams endpoint check all pass.

## Alternate failure branch
Instead of outcome 1, suppose Maven reports that
`io.arconia.migrations:rewrite-spring:1.2.3` cannot be resolved. There is no evidence
that the recipe ran or the POM changed. Explain how this branch changes your
actions and report; do not combine its status with the successful branch.
