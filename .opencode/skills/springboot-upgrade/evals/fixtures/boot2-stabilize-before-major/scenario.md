# Isolated scenario: stabilize Boot 2.x

This is a workflow simulation, not a live Maven project. All versions, release
metadata, and command outcomes below are test inputs, not claims about releases
available today. No separate upgrade workflow document is supplied or required.

## Initial inspection
- Root `pom.xml` has parent `org.springframework.boot:spring-boot-starter-parent:2.3.12.RELEASE`.
- Root modules: `domain` and `api`; the API inherits the root's Boot version.
- Maven is `mvn`; the working tree is clean.
- Supplied repository metadata for this fixture identifies `2.7.18` as the
  latest stable 2.x. The fixture's metadata excludes preview releases.
- README: run `mvn clean install` from the root, then
  `java -jar api/target/api.jar --spring.profiles.active=smoke` and
  `curl --fail http://localhost:8080/health`. Stop the process started by the check.
- Integration tests are bound to `verify` in the default build lifecycle.

## Simulated outcomes, revealed in this order
1. The Boot 2.7 recipe succeeds and the root parent becomes `2.7.18`; both modules
   resolve that version.
2. The first `mvn clean install` fails compiling `api`: a custom
   `WebMvcConfigurerAdapter` subclass no longer compiles. Its overridden methods
   are compatible with `WebMvcConfigurer`.
3. After adapting that class to implement `WebMvcConfigurer`, another
   `mvn clean install` passes compilation, unit tests, and integration tests.
4. The README startup and health check pass.

The user's ultimate destination is 4.x. They have not answered a post-stage
confirmation question for 2.x → 3.x yet.
