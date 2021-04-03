package springboot.soccer.game.team.datatransferobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Schema(description = "Errors", required = true, example = "{\n" +
        "  \"timestamp\": \"2021-04-03T14:57:59.670094Z\",\n" +
        "  \"errors\": {\n" +
        "    \"error\": \"Could not find team with id: 234\"\n" +
        "  }\n" +
        "}")
@Getter
@NoArgsConstructor
public class ErrorDTO {

    private final Instant timestamp = Instant.now();

    private Map<String, String> errors = new HashMap<>();

    public ErrorDTO(@NonNull Map<String, String> errors) {
        this.errors = errors;
    }

}