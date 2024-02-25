package springboot.soccer.game.team.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class KeycloakHealthIndicator implements HealthIndicator {

    private final String oidcAuthServerUrl;
    private final RestClient restClient;

    public KeycloakHealthIndicator(@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String oidcAuthServerUrl,  RestClient restClient) {
        this.oidcAuthServerUrl = oidcAuthServerUrl;
        this.restClient = restClient;
    }

    @Override
    public Health health() {
        if (checkHealthOfOidcAuthServer()) {
            return Health.up().withDetail("Keycloak connections health check", "Success").build();
        } else {
            return Health.down().withDetail("Keycloak connections health check", "Fail").build();
        }
    }

    private boolean checkHealthOfOidcAuthServer() {
        try {
            return restClient
                    .get()
                    .uri(oidcAuthServerUrl)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity()
                    .getStatusCode() == HttpStatus.OK;
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
