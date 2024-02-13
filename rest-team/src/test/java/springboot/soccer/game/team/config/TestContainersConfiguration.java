package springboot.soccer.game.team.config;

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
    @ServiceConnection
    PostgreSQLContainer<?> postgres() {
        return new PostgreSQLContainer<>(POSTGRES_IMAGE);
    }

    @Bean
    GenericContainer<?> keycloak(DynamicPropertyRegistry registry) {
        GenericContainer<?> keycloakContainer = new GenericContainer<>(KEYCLOAK_IMAGE)
                .withCommand("""
                        -b 0.0.0.0 -Djboss.http.port=8082 -Dkeycloak.profile.feature.upload_scripts=enabled -Dkeycloak.migration.action=import \
                        -Dkeycloak.migration.provider=dir -Dkeycloak.migration.dir=/tmp/keycloak/realms -Dkeycloak.migration.strategy=OVERWRITE_EXISTING\
                        """)
                .withClasspathResourceMapping("./keycloak/realms/", "/tmp/keycloak/realms/", BindMode.READ_ONLY)
                .withStartupTimeout(Duration.ofSeconds(120))
                .withExposedPorts(8082)
                .withEnv("DB_VENDOR", "h2")
                .waitingFor(Wait.forHttp("/auth"));

        Supplier<Object> authServerUrl = () -> "http://%s:%d/auth".formatted(keycloakContainer.getHost(), keycloakContainer.getMappedPort(8082));

        registry.add("keycloak.auth-server-url", authServerUrl);

        return keycloakContainer;
    }

}