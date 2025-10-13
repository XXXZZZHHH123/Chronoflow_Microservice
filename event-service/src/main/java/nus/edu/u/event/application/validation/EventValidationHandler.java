package nus.edu.u.event.application.validation;

public interface EventValidationHandler {

    boolean supports(EventValidationContext context);

    void validate(EventValidationContext context);
}
