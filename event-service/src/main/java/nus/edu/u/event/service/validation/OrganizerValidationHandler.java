package nus.edu.u.event.service.validation;

import static nus.edu.u.common.enums.ErrorCodeConstants.ORGANIZER_NOT_FOUND;
import static nus.edu.u.common.utils.exception.ServiceExceptionUtil.exception;

import nus.edu.u.shared.rpc.user.UserRpcService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
public class OrganizerValidationHandler implements EventValidationHandler {

    @DubboReference(check = false)
    private UserRpcService userRpcService;

    @Override
    public boolean supports(EventValidationContext context) {
        return context.shouldValidateOrganizer();
    }

    @Override
    public void validate(EventValidationContext context) {
        Long organizerId = context.getRequestedOrganizerId();
        if (organizerId == null || !userRpcService.exists(organizerId)) {
            throw exception(ORGANIZER_NOT_FOUND);
        }
    }
}
