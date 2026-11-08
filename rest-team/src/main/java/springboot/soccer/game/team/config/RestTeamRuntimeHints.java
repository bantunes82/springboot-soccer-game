package springboot.soccer.game.team.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class RestTeamRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Register resources
        hints.resources().registerPattern("messages*");

        // springdoc (springdoc.show-actuator=true) reads this field reflectively to document the actuator endpoints
        hints.reflection().registerTypeIfPresent(classLoader,
                "org.springframework.boot.webmvc.actuate.endpoint.web.AbstractWebMvcEndpointHandlerMapping$OperationHandler",
                MemberCategory.ACCESS_DECLARED_FIELDS);
    }
}
