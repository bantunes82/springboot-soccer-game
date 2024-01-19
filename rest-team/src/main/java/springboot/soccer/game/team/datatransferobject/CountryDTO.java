package springboot.soccer.game.team.datatransferobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import springboot.soccer.game.team.validation.ConstraintMessage;
import springboot.soccer.game.team.validation.CountryCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Schema(description="Country", requiredMode = Schema.RequiredMode.REQUIRED)
@Getter
@AllArgsConstructor
public class CountryDTO {

    @NotBlank(message = ConstraintMessage.COUNTRY_NAME_BLANK)
    @Size(min = 3, max = 20, message = ConstraintMessage.COUNTRY_NAME_SIZE)
    private final String name;

    @CountryCode
    private final String code;

}
