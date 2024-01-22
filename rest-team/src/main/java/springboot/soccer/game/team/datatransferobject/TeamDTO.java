package springboot.soccer.game.team.datatransferobject;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import springboot.soccer.game.team.validation.ConstraintMessage;
import springboot.soccer.game.team.validation.Range;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "Team Soccer", requiredMode = Schema.RequiredMode.REQUIRED)
public record TeamDTO(
        @NotBlank(message = ConstraintMessage.TEAM_NAME_BLANK)
        @Size(min = 3, max = 50, message = ConstraintMessage.TEAM_NAME_SIZE)
        String name,
        String nickName,
        @Schema(example = "1910-07-01", type = "string",
                implementation = LocalDate.class,
                pattern = "yyyy-MM-dd",
                description = "Team founded date")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @Past(message = ConstraintMessage.TEAM_FOUNDED_PAST)
        @NotNull(message = ConstraintMessage.TEAM_FOUNDED_BLANK)
        LocalDate founded,

        @Range(min = 1.0, max = 10.0)
        Double level,

        @NotBlank(message = ConstraintMessage.TEAM_PICTURE_BLANK)
        String picture,

        @NotNull(message = ConstraintMessage.TEAM_COUNTRY_NULL)
        @Valid
        CountryDTO countryDTO) {

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String nickName;
        private LocalDate founded;
        private Double level;
        private String picture;
        private CountryDTO countryDTO;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder nickName(String nickName) {
            this.nickName = nickName;
            return this;
        }

        public Builder founded(LocalDate founded) {
            this.founded = founded;
            return this;
        }

        public Builder level(Double level) {
            this.level = level;
            return this;
        }

        public Builder picture(String picture) {
            this.picture = picture;
            return this;
        }

        public Builder countryDTO(CountryDTO countryDTO) {
            this.countryDTO = countryDTO;
            return this;
        }

        public TeamDTO build() {
            return new TeamDTO(name, nickName, founded, level, picture, countryDTO);
        }

    }

}