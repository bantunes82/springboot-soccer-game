package springboot.soccer.game.team.config;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@OpenAPIDefinition(
        info = @Info(
                title = "Team Soccer API",
                description = "This API allows CRUD operations on a soccer team",
                version = "1.0",
                contact = @Contact(
                        name = "Bruno Romao Antunes",
                        url = "https://github.com/bantunes82",
                        email = "bantunes82@gmail.com")),
        externalDocs = @ExternalDocumentation(url = "https://github.com/bantunes82/springboot-soccer-game/tree/main/rest-team")
)
@SecuritySchemes(value = {
        @SecurityScheme(name = "Keycloak",
                openIdConnectUrl = "http://localhost:8082/auth/realms/team-realm/.well-known/openid-configuration",
                scheme = "bearer",
                type = SecuritySchemeType.OPENIDCONNECT,
                in = SecuritySchemeIn.HEADER,
                description = "Username and password for the user that belongs to TEAM role"
        )}
)
@Configuration
@EnableWebSecurity
public class ApplicationConfig {

    private static final String CLASSPATH_MESSAGES = "classpath:messages";
    private static final String PATH_ENDPOINT = "/v1/teams/**";

    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final String role;

    public ApplicationConfig(JwtAuthenticationConverter jwtAuthenticationConverter, @Value("${keycloak.security.role}") String role) {
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.role = role;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(http -> {
                    http.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/actuator/**").permitAll();
                    http.requestMatchers(HttpMethod.GET, PATH_ENDPOINT).permitAll();
                    http.requestMatchers(HttpMethod.PUT, PATH_ENDPOINT).hasRole(role);
                    http.requestMatchers(HttpMethod.DELETE, PATH_ENDPOINT).hasRole(role);
                    http.requestMatchers(HttpMethod.POST, PATH_ENDPOINT).hasRole(role);
                    http.requestMatchers(HttpMethod.PATCH, PATH_ENDPOINT).hasRole(role);
                    http.anyRequest().authenticated();
                })
                //.oauth2ResourceServer((oauth2) -> oauth2.jwt(withDefaults()))
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults(""); //DEFAULT "ROLE_"
    }


    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(3000))
                .setReadTimeout(Duration.ofMillis(3000))
                .build();
    }

    @Bean
    public LocalValidatorFactoryBean getValidator(MessageSource messageSource) {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource);
        return bean;
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename(CLASSPATH_MESSAGES);
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
