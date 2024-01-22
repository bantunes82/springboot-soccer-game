package springboot.soccer.game.team.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccessToken(@JsonProperty("access_token") String accessToken)
{
}
