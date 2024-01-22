package springboot.soccer.game.team.datatransferobject;

import io.swagger.v3.oas.annotations.media.Schema;
import springboot.soccer.game.team.validation.ConstraintMessage;
import springboot.soccer.game.team.validation.CountryCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Schema(description = "Country", requiredMode = Schema.RequiredMode.REQUIRED)
public record CountryDTO(
        @NotBlank(message = ConstraintMessage.COUNTRY_NAME_BLANK)
        @Size(min = 3, max = 20, message = ConstraintMessage.COUNTRY_NAME_SIZE)
        String name,
        @CountryCode
        String code) {

}
