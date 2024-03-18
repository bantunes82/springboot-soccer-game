package springboot.soccer.game.team;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import springboot.soccer.game.team.config.RestTeamRuntimeHints;

@SpringBootApplication
@ImportRuntimeHints(RestTeamRuntimeHints.class)
public class RestTeamApplication {

    public static void main(String[] args) {//NOSONAR
        SpringApplication.run(RestTeamApplication.class, args);
    }


}
