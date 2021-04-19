package springboot.soccer.game.team.health;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Tag("integration")
public class HealthIntegrationTest {

    private static String HEALTH_PATH = "/actuator/health";

    @Autowired
    private MockMvc mockMvc;

    @Container
    private static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>("postgres:13.2")
            .withDatabaseName("teams_database")
            .withUsername("team")
            .withPassword("team");

    @DynamicPropertySource
    static void registryProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
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
                .andExpect(content().json("{\"status\":\"UP\",\"components\":{\"db\":{\"status\":\"UP\",\"details\":{\"database\":\"PostgreSQL\",\"validationQuery\":\"isValid()\"}},\"keycloak\":{\"status\":\"UP\",\"details\":{\"Keycloak connections health check\":200}},\"readinessState\":{\"status\":\"UP\"}}}"));

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
                .andExpect(content().json("{\"components\":{\"keycloak\":{\"status\":\"UP\",\"details\":{\"Keycloak connections health check\":200}}}}"))
                .andExpect(content().json("{\"components\":{\"livenessState\":{\"status\":\"UP\"}}}}"))
                .andExpect(content().json("{\"components\":{\"ping\":{\"status\":\"UP\"}}}}"))
                .andExpect(content().json("{\"components\":{\"readinessState\":{\"status\":\"UP\"}}}}"))
                .andExpect(content().json("{\"groups\":[\"liveness\",\"readiness\"]}"));
    }

}
