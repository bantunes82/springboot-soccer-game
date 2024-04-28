package springboot.soccer.game.team.config;

import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;
import java.util.function.Supplier;


@TestConfiguration(proxyBeanMethods = false)
    public class TestContainersConfiguration {

    private final static String POSTGRES_IMAGE = "postgres:13.2";
    private final static String KEYCLOAK_IMAGE = "quay.io/keycloak/keycloak:12.0.4";

    @Bean
    @RestartScope
    @ServiceConnection
    PostgreSQLContainer<?> postgres() {
        return new PostgreSQLContainer<>(POSTGRES_IMAGE);
    }

    @Bean
    //@RestartScope - it is not possible to use RestartScope annotation for any container of type GenericContainer
    GenericContainer<?> keycloak(DynamicPropertyRegistry registry) {
        GenericContainer<?> keycloakContainer = new GenericContainer<>(KEYCLOAK_IMAGE)
                .withCommand("""
                        -b 0.0.0.0 -Djboss.http.port=8080 -Dkeycloak.profile.feature.upload_scripts=enabled -Dkeycloak.migration.action=import \
                        -Dkeycloak.migration.provider=dir -Dkeycloak.migration.dir=/tmp/keycloak/realms -Dkeycloak.migration.strategy=OVERWRITE_EXISTING\
                        """)
                .withEnv("KEYCLOAK_USER", "admin")
                .withEnv("KEYCLOAK_PASSWORD", "admin")
                .withEnv("DB_VENDOR", "h2")
                .withClasspathResourceMapping("./keycloak/realms/", "/tmp/keycloak/realms/", BindMode.READ_ONLY)
                .withStartupTimeout(Duration.ofSeconds(120))
                .withExposedPorts(8080)
                .waitingFor(Wait.forHttp("/auth").forStatusCode(200));

        Supplier<Object> authServerUrl = () -> "http://%s:%d/auth".formatted(keycloakContainer.getHost(), keycloakContainer.getFirstMappedPort());
        registry.add("keycloak.auth-server-url", authServerUrl);

        return keycloakContainer;
    }

}