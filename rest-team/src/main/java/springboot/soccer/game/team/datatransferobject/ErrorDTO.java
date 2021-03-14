package springboot.soccer.game.team.datatransferobject;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Getter
@NoArgsConstructor
public class ErrorDTO {

    private final Instant timestamp = Instant.now();

    private Map<String, String> errors = new HashMap<>();

    public ErrorDTO(@NonNull Map<String, String> errors) {
        this.errors = errors;
    }

}