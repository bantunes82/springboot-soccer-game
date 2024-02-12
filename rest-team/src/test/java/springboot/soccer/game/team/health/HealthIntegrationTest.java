package springboot.soccer.game.team.health;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import springboot.soccer.game.team.config.TestContainersConfiguration;


import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(TestContainersConfiguration.class)
@Tag("integration")
class HealthIntegrationTest {

    private static final String HEALTH_PATH = "/actuator/health";
    private static final String LIVENESS_RESPONSE = """
            {
               "status":"UP"
            }
            """;
    private static final String READINESS_RESPONSE = """
            {
               "status":"UP",
               "components":{
                  "db":{
                     "status":"UP",
                     "details":{
                        "database":"PostgreSQL",
                        "validationQuery":"isValid()"
                     }
                  },
                  "keycloak":{
                     "status":"UP",
                     "details":{
                        "Keycloak connections health check":"Success"
                     }
                  },
                  "readinessState":{
                     "status":"UP"
                  }
               }
            }
            """;

    private static final String HEALTH_RESPONSE = """
            {
               "status":"UP",
               "components":{
                  "db":{
                     "status":"UP",
                     "details":{
                        "database":"PostgreSQL",
                        "validationQuery":"isValid()"
                     }
                  },
                  "keycloak":{
                     "status":"UP",
                     "details":{
                        "Keycloak connections health check":"Success"
                     }
                  },
                  "readinessState":{
                     "status":"UP"
                  }
               }
            }
            """;
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
                .andExpect(content().json(LIVENESS_RESPONSE));
    }

    @Test
    void shouldPingReadiness() throws Exception {
        mockMvc.perform(get(HEALTH_PATH + "/readiness")
                        .accept(APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(READINESS_RESPONSE));

    }

    @Test
    void shouldPingHealth() throws Exception {
        mockMvc.perform(get(HEALTH_PATH)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(HEALTH_RESPONSE));
    }

}
