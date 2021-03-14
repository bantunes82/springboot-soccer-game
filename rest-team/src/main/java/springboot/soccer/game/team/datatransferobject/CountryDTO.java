package springboot.soccer.game.team.datatransferobject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import springboot.soccer.game.team.constants.Validation;
import springboot.soccer.game.team.util.CountryCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@AllArgsConstructor
public class CountryDTO {

    @NotBlank(message = Validation.COUNTRY_NAME_BLANK)
    @Size(min = 3, max = 20, message = Validation.COUNTRY_NAME_SIZE)
    private final String name;

    @CountryCode
    private final String code;

}
