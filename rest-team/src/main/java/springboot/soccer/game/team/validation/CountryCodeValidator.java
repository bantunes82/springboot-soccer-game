package springboot.soccer.game.team.validation;

import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Locale;

@Slf4j
public class CountryCodeValidator implements ConstraintValidator<CountryCode, Object> {

    private String message;

    @Override
    public void initialize(CountryCode countryCode) {
        message = countryCode.message();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        boolean isValid = false;

        if (object instanceof String) {
            String value = object.toString();
            isValid = Arrays.stream(Locale.getISOCountries())
                    .anyMatch(code -> code.equals(value));

            if (!isValid) {
                log.debug("Country Code is invalid: {}", value);
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message)
                        .addConstraintViolation();
            }
        }

        return isValid;
    }
}
