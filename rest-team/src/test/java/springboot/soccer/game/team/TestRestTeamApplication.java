package springboot.soccer.game.team;

import org.springframework.boot.SpringApplication;
import springboot.soccer.game.team.config.TestContainersConfiguration;

public class TestRestTeamApplication {

    public static void main(String[] args) {
        SpringApplication.from(RestTeamApplication::main)
                .with(TestContainersConfiguration.class)
                .run(args);
    }
}
