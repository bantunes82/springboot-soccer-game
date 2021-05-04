package springboot.soccer.game.team.health;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Tag("integration")
class HealthIntegrationTest {

    private static String HEALTH_PATH = "/actuator/health";

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
    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void registryProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", DATABASE::getJdbcUrl);
        registry.add("spring.datasource.password", DATABASE::getPassword);
        registry.add("spring.datasource.username", DATABASE::getUsername);

        String authServerUrl = String.format("http://%s:%d/auth", IDENTITY_ACCESS_MANAGEMENT.getHost(), IDENTITY_ACCESS_MANAGEMENT.getMappedPort(8082));
        registry.add("keycloak.auth-server-url", () -> authServerUrl);
    }

    @Test
    void shouldPingLiveness() throws Exception {
        mockMvc.perform(get(HEALTH_PATH + "/liveness")
                .accept(APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json("{\"status\":\"UP\"}"));
    }

    @Test
    void shouldPingReadiness() throws Exception {
        mockMvc.perform(get(HEALTH_PATH + "/readiness")
                .accept(APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json("{\"status\":\"UP\",\"components\":{\"db\":{\"status\":\"UP\",\"details\":{\"database\":\"PostgreSQL\",\"validationQuery\":\"isValid()\"}},\"keycloak\":{\"status\":\"UP\",\"details\":{\"Keycloak connections health check\":Success}},\"readinessState\":{\"status\":\"UP\"}}}"));

    }

    @Test
    void shouldPingHealth() throws Exception {
        mockMvc.perform(get(HEALTH_PATH)
                .accept(APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json("{\"status\":\"UP\"}"))
                .andExpect(content().json("{\"components\":{\"db\":{\"status\":\"UP\",\"details\":{\"database\":\"PostgreSQL\",\"validationQuery\":\"isValid()\"}}}}"))
                .andExpect(content().json("{\"components\":{\"keycloak\":{\"status\":\"UP\",\"details\":{\"Keycloak connections health check\":Success}}}}"))
                .andExpect(content().json("{\"components\":{\"livenessState\":{\"status\":\"UP\"}}}}"))
                .andExpect(content().json("{\"components\":{\"ping\":{\"status\":\"UP\"}}}}"))
                .andExpect(content().json("{\"components\":{\"readinessState\":{\"status\":\"UP\"}}}}"))
                .andExpect(content().json("{\"groups\":[\"liveness\",\"readiness\"]}"));
    }

}
