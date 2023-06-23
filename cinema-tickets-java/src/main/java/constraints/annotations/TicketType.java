package constraints.annotations;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import constraints.validator.TicketTypeValidator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD})
@Constraint(validatedBy = TicketTypeValidator.class)
public @interface TicketType {
    TicketTypeRequest.Type[] oneOf();

    String message() default "Invalid ticket type: must be one of {oneOf}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
