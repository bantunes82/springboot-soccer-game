package springboot.soccer.game.team.container;


import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@Testcontainers
public class Containers {

    @Container
    private static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>("postgres:10.5")
            .withDatabaseName("teams_database")
            .withUsername("team")
            .withPassword("team");

    @DynamicPropertySource
    static void registryProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    }


}
