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