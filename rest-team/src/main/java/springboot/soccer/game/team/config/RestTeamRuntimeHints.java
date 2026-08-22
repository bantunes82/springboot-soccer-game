package springboot.soccer.game.team.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

public class RestTeamRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Register resources
        hints.resources().registerPattern("messages*");

        // Register Spring Boot Actuator endpoint handler for reflection
        // Required for native image compilation to avoid MissingReflectionRegistrationError
        registerActuatorReflectionHints(hints);
    }

    private void registerActuatorReflectionHints(RuntimeHints hints) {
        // Register AbstractWebMvcEndpointHandlerMapping$OperationHandler for reflection
        hints.reflection().registerType(
                TypeReference.of("org.springframework.boot.webmvc.actuate.endpoint.web.AbstractWebMvcEndpointHandlerMapping$OperationHandler"),
                typeHint -> typeHint.withMembers(MemberCategory.DECLARED_FIELDS)
        );

        // Register ServletWebOperation for reflection (related class that might be needed)
        hints.reflection().registerType(
                TypeReference.of("org.springframework.boot.webmvc.actuate.endpoint.web.AbstractWebMvcEndpointHandlerMapping$ServletWebOperation"),
                typeHint -> typeHint.withMembers(MemberCategory.DECLARED_FIELDS, MemberCategory.INVOKE_DECLARED_METHODS)
        );
    }
}
