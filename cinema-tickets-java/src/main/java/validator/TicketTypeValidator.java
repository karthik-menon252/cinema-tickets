package validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;

import java.util.Arrays;


public class TicketTypeValidator implements ConstraintValidator<TicketType, TicketTypeRequest.Type> {
    private TicketTypeRequest.Type[] types;


    @Override
    public void initialize(TicketType constraintAnnotation) {
       this.types = constraintAnnotation.oneof();
    }

    @Override
    public boolean isValid(TicketTypeRequest.Type type, ConstraintValidatorContext constraintValidatorContext) {
        return null != type && Arrays.asList(types).contains(type);
    }
}
