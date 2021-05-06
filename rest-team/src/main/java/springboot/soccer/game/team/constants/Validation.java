package springboot.soccer.game.team.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Validation {

    public static final String TEAM_NAME_BLANK = "{constraint.team.nameNotBlank}";
    public static final String TEAM_NAME_SIZE =  "{constraint.team.nameSize}";
    public static final String TEAM_FOUNDED_BLANK = "{constraint.team.foundedNotBlank}";
    public static final String TEAM_FOUNDED_PAST = "{constraint.team.foundedPast}";
    public static final String TEAM_PICTURE_BLANK = "{constraint.team.pictureNotBlank}";
    public static final String TEAM_LEVEL_INVALID = "{constraint.team.levelInvalid}";
    public static final String TEAM_COUNTRY_NULL = "{constraint.team.countryNotNull}";

    public static final String COUNTRY_NAME_BLANK = "{constraint.country.nameNotBlank}";
    public static final String COUNTRY_NAME_SIZE =  "{constraint.country.nameSize}";
    public static final String COUNTRY_CODE_INVALID = "{constraint.country.codeInvalid}";

    public static final String TEAM_NOT_FOUND = "exception.team.not.found";
    public static final String THERE_IS_NOT_ANY_TEAM = "exception.there.is.not.any.team";
    public static final String ERROR_TO_PERSIST = "exception.error.to.persist";
}
