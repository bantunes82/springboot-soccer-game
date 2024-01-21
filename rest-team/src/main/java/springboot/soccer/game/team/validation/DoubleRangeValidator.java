package springboot.soccer.game.team.validation;

import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Slf4j
public class DoubleRangeValidator implements ConstraintValidator<Range, Object> {

    private Double maxPrecision;
    private Double minPrecision;
    private String message;

    @Override
    public void initialize(Range doubleRange) {
        maxPrecision = doubleRange.max();
        minPrecision = doubleRange.min();
        message = doubleRange.message();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        boolean isValid = false;

        if (object instanceof Double value) {
            isValid = value.compareTo(maxPrecision) <= 0 && value.compareTo(minPrecision) >= 0;

            if (!isValid) {
                log.debug("Range is invalid: {}", value);
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message)
                        .addConstraintViolation();
            }
        }

        return isValid;
    }
}
