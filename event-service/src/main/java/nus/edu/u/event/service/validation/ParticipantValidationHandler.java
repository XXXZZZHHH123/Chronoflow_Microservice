package nus.edu.u.event.service.validation;

import static nus.edu.u.common.enums.ErrorCodeConstants.DUPLICATE_PARTICIPANTS;
import static nus.edu.u.common.enums.ErrorCodeConstants.PARTICIPANT_NOT_FOUND;
import static nus.edu.u.common.utils.exception.ServiceExceptionUtil.exception;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.shared.rpc.user.UserInfoDTO;
import nus.edu.u.shared.rpc.user.UserRpcService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
@Slf4j
public class ParticipantValidationHandler implements EventValidationHandler {

    @DubboReference(check = false)
    private UserRpcService userRpcService;

    @Override
    public boolean supports(EventValidationContext context) {
        List<Long> ids = context.getParticipantUserIds();
        return ids != null && !ids.isEmpty();
    }

    @Override
    public void validate(EventValidationContext context) {
        List<Long> ids = context.getParticipantUserIds();
        if (ids == null || ids.isEmpty()) {
            return;
        }

        List<Long> distinct =
                ids.stream().filter(id -> id != null).distinct().collect(Collectors.toList());
        if (distinct.size() != ids.size()) {
            throw exception(DUPLICATE_PARTICIPANTS);
        }

        Map<Long, UserInfoDTO> users = userRpcService.getUsers(distinct);
        List<Long> existIds = users.keySet().stream().toList();

        if (existIds.size() != distinct.size()) {
            Set<Long> missing = new HashSet<>(distinct);
            missing.removeAll(new HashSet<>(existIds));
            log.warn("Missing participants: {}", missing);
            throw exception(PARTICIPANT_NOT_FOUND);
        }
    }
}
