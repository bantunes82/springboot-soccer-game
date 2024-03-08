package springboot.soccer.game.team;

import org.springframework.boot.SpringApplication;
import springboot.soccer.game.team.config.TestContainersConfiguration;

import java.util.Arrays;

public class TestRestTeamApplication {

    public static void main(String[] args) {
        args = Arrays.copyOf(args, args.length + 1);
        args[args.length-1] = "--spring.profiles.active=test";

        SpringApplication.from(RestTeamApplication::main)
                .with(TestContainersConfiguration.class)
                .run(args);
    }
}
