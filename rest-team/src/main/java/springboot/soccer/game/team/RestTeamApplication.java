package springboot.soccer.game.team;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

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
@SpringBootApplication
public class RestTeamApplication {

    private static final String CLASSPATH_MESSAGES = "classpath:messages";

    public static void main(String[] args) {
        SpringApplication.run(RestTeamApplication.class, args);
    }

    @Bean
    public LocalValidatorFactoryBean getValidator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource());
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
