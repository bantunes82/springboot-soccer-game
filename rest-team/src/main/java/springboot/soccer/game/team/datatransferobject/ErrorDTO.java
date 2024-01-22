package springboot.soccer.game.team.datatransferobject;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import java.util.Objects;

@Schema(description = "Errors", requiredMode = Schema.RequiredMode.REQUIRED, example =
        """
                    {
                        "errors": {
                            "error": "Could not find team with id: 234",
                            "name": "Team name must be between 3 and 50 chars"
                         }
                    }
                """)
public record ErrorDTO(Map<String, String> errors) {
    public ErrorDTO {
        if (Objects.isNull(errors)) {
            throw new NullPointerException("the errors cannot be null");
        }
        errors = Map.copyOf(errors);
    }

}