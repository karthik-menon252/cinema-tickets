package validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD})
@Constraint(validatedBy = TicketTypeValidator.class)
public @interface TicketType {
    TicketTypeRequest.Type[] oneof();
    String message() default "Ticket type must be one of {oneof}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
