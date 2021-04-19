package springboot.soccer.game.team.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class KeycloakHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        return Health.up().withDetail("Keycloak connections health check",200).build();
    }
}
