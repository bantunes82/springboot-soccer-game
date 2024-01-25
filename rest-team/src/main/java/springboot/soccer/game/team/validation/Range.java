package springboot.soccer.game.team.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD, ANNOTATION_TYPE, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = DoubleRangeValidator.class)
@Documented
public @interface Range
{
    String message() default ConstraintMessage.TEAM_LEVEL_INVALID;

    double min() default Double.MIN_VALUE;

    double max() default Double.MAX_VALUE;

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}
