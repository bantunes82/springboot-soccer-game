# Setup used to migrate this application from Spring Boot 3.2.5 to 4.0.7

🔗 **Pull Request:**
https://github.com/bantunes82/springboot-soccer-game/pull/4/changes

🔗 **OpenRewrite recipe used for this upgrade:**
https://docs.openrewrite.org/recipes/java/spring/boot4/upgradespringboot_4_0-community-edition

🔧 **Maven command used to upgrade the application from Spring Boot 3.2.5 to 4.0.7:**
```bash
mvn -U org.openrewrite.maven:rewrite-maven-plugin:run \
  --define rewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:6.36.1 \
  --define rewrite.activeRecipes=org.openrewrite.java.spring.boot4.UpgradeSpringBoot_4_0 \
  --define rewrite.exportDatatables=true
```
---

# Follow-up: Spring Boot 4.0.7 to 4.1.2

**Maven command used** (requires Maven 3.9+; the 4.1 build plugins reject Maven 3.6.x):
```bash
mvn -U org.openrewrite.maven:rewrite-maven-plugin:6.46.1:run \
  -Drewrite.recipeArtifactCoordinates=io.arconia.migrations:rewrite-spring:1.2.3 \
  -Drewrite.activeRecipes=io.arconia.rewrite.spring.boot4.UpgradeSpringBoot_4_1 \
  -Drewrite.exportDatatables=true
```
Manual fix: the recipe renamed `MemberCategory.DECLARED_FIELDS` to `INVOKE_DECLARED_FIELDS`, which does not exist in Spring Framework 7; `RestTeamRuntimeHints` uses `ACCESS_DECLARED_FIELDS` instead.
