package nus.edu.u.event.service.validation;

import static nus.edu.u.common.enums.ErrorCodeConstants.DUPLICATE_PARTICIPANTS;
import static nus.edu.u.common.enums.ErrorCodeConstants.ORGANIZER_NOT_FOUND;
import static nus.edu.u.common.enums.ErrorCodeConstants.PARTICIPANT_NOT_FOUND;
import static nus.edu.u.common.enums.ErrorCodeConstants.TIME_RANGE_INVALID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import nus.edu.u.common.exception.ServiceException;
import nus.edu.u.event.domain.dataobject.event.EventDO;
import nus.edu.u.event.domain.dto.event.EventCreateReqVO;
import nus.edu.u.event.domain.dto.event.EventUpdateReqVO;
import nus.edu.u.shared.rpc.user.UserInfoDTO;
import nus.edu.u.shared.rpc.user.UserRpcService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EventValidationHandlersTest {

    @Mock private UserRpcService userRpcService;

    private final OrganizerValidationHandler organizerHandler = new OrganizerValidationHandler();
    private final ParticipantValidationHandler participantHandler =
            new ParticipantValidationHandler();
    private final TimeRangeValidationHandler timeRangeHandler = new TimeRangeValidationHandler();

    @BeforeEach
    void initHandlers() {
        ReflectionTestUtils.setField(organizerHandler, "userRpcService", userRpcService);
        ReflectionTestUtils.setField(participantHandler, "userRpcService", userRpcService);
    }

    @Test
    void organizerHandler_supportsCreateContextAndValidatesSuccessfully() {
        EventCreateReqVO req = new EventCreateReqVO();
        req.setOrganizerId(101L);
        req.setStartTime(LocalDateTime.now());
        req.setEndTime(LocalDateTime.now().plusHours(2));

        EventValidationContext context = EventValidationContext.forCreate(req);
        assertThat(organizerHandler.supports(context)).isTrue();

        when(userRpcService.exists(101L)).thenReturn(true);
        organizerHandler.validate(context);
    }

    @Test
    void organizerHandler_skipValidationWhenOrganizerNotProvidedOnUpdate() {
        EventUpdateReqVO req = new EventUpdateReqVO();
        EventDO current =
                EventDO.builder()
                        .id(1L)
                        .startTime(LocalDateTime.now())
                        .endTime(LocalDateTime.now().plusHours(1))
                        .build();
        EventValidationContext context = EventValidationContext.forUpdate(req, current);

        assertThat(organizerHandler.supports(context)).isFalse();
    }

    @Test
    void organizerHandler_throwsWhenOrganizerMissing() {
        EventCreateReqVO req = new EventCreateReqVO();
        req.setOrganizerId(202L);
        req.setStartTime(LocalDateTime.now());
        req.setEndTime(LocalDateTime.now().plusHours(1));

        EventValidationContext context = EventValidationContext.forCreate(req);
        when(userRpcService.exists(202L)).thenReturn(false);

        assertThatThrownBy(() -> organizerHandler.validate(context))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(ORGANIZER_NOT_FOUND.getCode());
    }

    @Test
    void participantHandler_supportsWhenIdsProvided() {
        EventCreateReqVO req = baseCreateRequest();
        req.setParticipantUserIds(List.of(1L, 2L));

        EventValidationContext context = EventValidationContext.forCreate(req);
        assertThat(participantHandler.supports(context)).isTrue();
    }

    @Test
    void participantHandler_duplicateIdsThrowException() {
        EventCreateReqVO req = baseCreateRequest();
        req.setParticipantUserIds(List.of(1L, 1L));

        EventValidationContext context = EventValidationContext.forCreate(req);

        assertThatThrownBy(() -> participantHandler.validate(context))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(DUPLICATE_PARTICIPANTS.getCode());
    }

    @Test
    void participantHandler_missingUsersThrowException() {
        EventCreateReqVO req = baseCreateRequest();
        req.setParticipantUserIds(List.of(1L, 2L));

        when(userRpcService.getUsers(List.of(1L, 2L))).thenReturn(Map.of(1L, userInfo(1L)));

        EventValidationContext context = EventValidationContext.forCreate(req);

        assertThatThrownBy(() -> participantHandler.validate(context))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(PARTICIPANT_NOT_FOUND.getCode());
    }

    @Test
    void participantHandler_passesWhenAllUsersExist() {
        EventCreateReqVO req = baseCreateRequest();
        req.setParticipantUserIds(List.of(1L, 2L));

        when(userRpcService.getUsers(List.of(1L, 2L)))
                .thenReturn(Map.of(1L, userInfo(1L), 2L, userInfo(2L)));

        EventValidationContext context = EventValidationContext.forCreate(req);
        participantHandler.validate(context);
    }

    @Test
    void timeRangeHandler_allowsValidRange() {
        EventCreateReqVO req = baseCreateRequest();
        EventValidationContext context = EventValidationContext.forCreate(req);

        timeRangeHandler.validate(context);
    }

    @Test
    void timeRangeHandler_throwsWhenStartNotBeforeEnd() {
        EventUpdateReqVO update = new EventUpdateReqVO();
        update.setStartTime(LocalDateTime.of(2025, 1, 1, 12, 0));
        update.setEndTime(LocalDateTime.of(2025, 1, 1, 12, 0));

        EventDO current =
                EventDO.builder()
                        .id(1L)
                        .startTime(LocalDateTime.of(2025, 1, 1, 9, 0))
                        .endTime(LocalDateTime.of(2025, 1, 1, 10, 0))
                        .build();
        EventValidationContext context = EventValidationContext.forUpdate(update, current);

        assertThatThrownBy(() -> timeRangeHandler.validate(context))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(TIME_RANGE_INVALID.getCode());
    }

    private EventCreateReqVO baseCreateRequest() {
        EventCreateReqVO req = new EventCreateReqVO();
        req.setOrganizerId(10L);
        req.setStartTime(LocalDateTime.now());
        req.setEndTime(LocalDateTime.now().plusHours(2));
        return req;
    }

    private UserInfoDTO userInfo(Long id) {
        return UserInfoDTO.builder().id(id).status(1).build();
    }
}
