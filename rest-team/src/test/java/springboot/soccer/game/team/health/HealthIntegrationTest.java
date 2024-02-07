package springboot.soccer.game.team.health;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import springboot.soccer.game.team.config.AbstractIT;


import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Tag("integration")
class HealthIntegrationTest extends AbstractIT {

    private static final String HEALTH_PATH = "/actuator/health";

    private final MockMvc mockMvc;

    @Autowired
    HealthIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
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
