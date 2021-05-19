package springboot.soccer.game.team.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Component
public class KeycloakHealthIndicator implements HealthIndicator {

    @Value("${keycloak.auth-server-url}/realms/${keycloak.realm}")
    private String oidcAuthServerUrl;

    private RestTemplate restTemplate;

    @Autowired
    public KeycloakHealthIndicator(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
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
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<>(headers);

            return restTemplate
                    .exchange(oidcAuthServerUrl, HttpMethod.GET, entity, Void.class)
                    .getStatusCode() == HttpStatus.OK;
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
