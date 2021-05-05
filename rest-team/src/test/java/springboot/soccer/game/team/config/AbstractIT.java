package springboot.soccer.game.team.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class AbstractIT {

   @Container
    private static PostgreSQLContainer DATABASE = new PostgreSQLContainer<>("postgres:13.2")
            .withDatabaseName("teams_database")
            .withUsername("team")
            .withPassword("team");
    @Container
    private static GenericContainer IDENTITY_ACCESS_MANAGEMENT = new GenericContainer("quay.io/keycloak/keycloak:12.0.4")
            .withCommand("-b 0.0.0.0 -Djboss.http.port=8082 -Dkeycloak.profile.feature.upload_scripts=enabled -Dkeycloak.migration.action=import " +
                    "-Dkeycloak.migration.provider=dir -Dkeycloak.migration.dir=/tmp/keycloak/realms -Dkeycloak.migration.strategy=OVERWRITE_EXISTING")
            .withClasspathResourceMapping("./keycloak/realms/", "/tmp/keycloak/realms/", BindMode.READ_ONLY)
            .withStartupTimeout(Duration.ofSeconds(120))
            .withExposedPorts(8082)
            .withEnv("DB_VENDOR", "h2")
            .waitingFor(Wait.forHttp("/auth"));


    @DynamicPropertySource
    static void registryProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", DATABASE::getJdbcUrl);
        registry.add("spring.datasource.password", DATABASE::getPassword);
        registry.add("spring.datasource.username", DATABASE::getUsername);

        String authServerUrl = String.format("http://%s:%d/auth", IDENTITY_ACCESS_MANAGEMENT.getHost(), IDENTITY_ACCESS_MANAGEMENT.getMappedPort(8082));
        registry.add("keycloak.auth-server-url", () -> authServerUrl);
        registry.add("keycloak.realm", () -> KeycloakConfiguration.kEYCLOAK_REALM);
        registry.add("keycloak.resource", () -> KeycloakConfiguration.kEYCLOAK_RESOURCE);
        registry.add("keycloak.bearer-only", () -> KeycloakConfiguration.kEYCLOAK_BEARER_ONLY);
        registry.add("keycloak.security-constraints[0].authRoles[0]", () -> KeycloakConfiguration.kEYCLOAK_AUTH_ROLES);
        registry.add("keycloak.security-constraints[0].securityCollections[0].patterns[0]", () -> KeycloakConfiguration.kEYCLOAK_PATTERNS);
        registry.add("keycloak.security-constraints[0].securityCollections[0].methods[0]", () -> KeycloakConfiguration.kEYCLOAK_METHOD0);
        registry.add("keycloak.security-constraints[0].securityCollections[0].methods[1]", () -> KeycloakConfiguration.kEYCLOAK_METHOD1);
        registry.add("keycloak.security-constraints[0].securityCollections[0].methods[2]", () -> KeycloakConfiguration.kEYCLOAK_METHOD2);
        registry.add("keycloak.security-constraints[0].securityCollections[0].methods[3]", () -> KeycloakConfiguration.kEYCLOAK_METHOD3);
    }
}