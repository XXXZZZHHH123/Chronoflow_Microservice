package nus.edu.u.event.service.validation;

public interface EventValidationHandler {

    boolean supports(EventValidationContext context);

    void validate(EventValidationContext context);
}
