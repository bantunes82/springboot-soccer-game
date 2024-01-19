package springboot.soccer.game.team.datatransferobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Schema(description = "Errors", required = true, example =
        """
            {
                "timestamp": "2021-04-03T14:57:59.670094Z",
                "errors": {
                    "error": "Could not find team with id: 234",
                    "name": "Team name must be between 3 and 50 chars"
                 }
            }
        """)
@Getter
@NoArgsConstructor
public class ErrorDTO {

    private final Instant timestamp = Instant.now();

    private Map<String, String> errors = new HashMap<>();

    public ErrorDTO(@NonNull Map<String, String> errors) {
        this.errors = errors;
    }

}