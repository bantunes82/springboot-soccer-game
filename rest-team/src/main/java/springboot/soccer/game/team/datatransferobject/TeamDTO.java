package springboot.soccer.game.team.datatransferobject;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import springboot.soccer.game.team.constants.Validation;
import springboot.soccer.game.team.util.Range;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description="Team Soccer", required = true)
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamDTO {

    @NotBlank(message = Validation.TEAM_NAME_BLANK)
    @Size(min = 3, max = 50, message = Validation.TEAM_NAME_SIZE)
    private final String name;

    private final String nickName;

    @Schema(example = "1910-07-01", type = "string",
            implementation = LocalDate.class,
            pattern = "yyyy-MM-dd",
            description = "Team founded date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Past(message = Validation.TEAM_FOUNDED_PAST)
    @NotNull(message = Validation.TEAM_FOUNDED_BLANK)
    private final LocalDate founded;

    @Range(min = 1.0, max = 10.0)
    private final Double level;

    @NotBlank(message = Validation.TEAM_PICTURE_BLANK)
    private final String picture;

    @NotNull(message = Validation.TEAM_COUNTRY_NULL)
    @Valid
    private final CountryDTO countryDTO;

}