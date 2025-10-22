package nus.edu.u.attendee.service;

import static nus.edu.u.common.enums.ErrorCodeConstants.ALREADY_CHECKED_IN;
import static nus.edu.u.common.enums.ErrorCodeConstants.ATTENDEE_CREATION_FAILED;
import static nus.edu.u.common.enums.ErrorCodeConstants.ATTENDEE_NOT_EXIST;
import static nus.edu.u.common.enums.ErrorCodeConstants.CHECKIN_ENDED;
import static nus.edu.u.common.enums.ErrorCodeConstants.CHECKIN_NOT_STARTED;
import static nus.edu.u.common.enums.ErrorCodeConstants.EVENT_ATTENDEE_NOT_FOUND;
import static nus.edu.u.common.enums.ErrorCodeConstants.EVENT_NOT_ACTIVE;
import static nus.edu.u.common.enums.ErrorCodeConstants.EVENT_NOT_FOUND;
import static nus.edu.u.common.enums.ErrorCodeConstants.INVALID_CHECKIN_TOKEN;
import static nus.edu.u.common.enums.ErrorCodeConstants.UPDATE_ATTENDEE_FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.stp.StpUtil;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import nus.edu.u.attendee.domain.dataobject.EventAttendeeDO;
import nus.edu.u.attendee.domain.vo.attendee.AttendeeReqVO;
import nus.edu.u.attendee.domain.vo.checkin.CheckInRespVO;
import nus.edu.u.attendee.domain.vo.checkin.GenerateQrCodesReqVO;
import nus.edu.u.attendee.domain.vo.checkin.GenerateQrCodesRespVO;
import nus.edu.u.attendee.domain.vo.qrcode.QrCodeRespVO;
import nus.edu.u.attendee.mapper.EventAttendeeMapper;
import nus.edu.u.attendee.publisher.AttendeeNotificationPublisher;
import nus.edu.u.attendee.service.qrcode.QrCodeService;
import nus.edu.u.common.constant.Constants;
import nus.edu.u.common.enums.EventStatusEnum;
import nus.edu.u.common.exception.ServiceException;
import nus.edu.u.shared.rpc.events.EventRespDTO;
import nus.edu.u.shared.rpc.events.EventRespDTO.GroupVO;
import nus.edu.u.shared.rpc.events.EventRpcService;
import nus.edu.u.shared.rpc.user.TenantDTO;
import nus.edu.u.shared.rpc.user.UserRpcService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AttendeeServiceImplTest {

    @Mock private EventAttendeeMapper attendeeMapper;
    @Mock private EventRpcService eventRpcService;
    @Mock private QrCodeService qrCodeService;
    @Mock private UserRpcService userRpcService;
    @Mock private AttendeeNotificationPublisher attendeeNotificationPublisher;

    @InjectMocks private AttendeeServiceImpl service;

    private static final String BASE_URL = "http://test-host";
    private static final String SAMPLE_BASE64 =
            Base64.getEncoder().encodeToString("qr-image".getBytes());

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "eventRpcService", eventRpcService);
        ReflectionTestUtils.setField(service, "userRpcService", userRpcService);
        ReflectionTestUtils.setField(service, "baseUrl", BASE_URL);
        ReflectionTestUtils.setField(
                service, "attendeeNotificationPublisher", attendeeNotificationPublisher);
    }

    @Test
    void list_returnsEmptyWhenNoAttendees() {
        when(attendeeMapper.selectByEventId(1L)).thenReturn(List.of());

        assertThat(service.list(1L)).isEmpty();
    }

    @Test
    void list_mapsAttendees() {
        EventAttendeeDO attendee = attendee(10L, 2L);
        attendee.setAttendeeEmail("a@test.com");
        attendee.setAttendeeMobile("123");
        attendee.setAttendeeName("Alice");
        attendee.setCheckInToken("token");
        attendee.setCheckInStatus(0);
        when(attendeeMapper.selectByEventId(2L)).thenReturn(List.of(attendee));

        var results = service.list(2L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAttendeeEmail()).isEqualTo("a@test.com");
        assertThat(results.get(0).getCheckInToken()).isEqualTo("token");
    }

    @Test
    void get_returnsNullWhenMissing() {
        when(attendeeMapper.selectById(5L)).thenReturn(null);

        assertThat(service.get(5L)).isNull();
    }

    @Test
    void get_returnsAttendeeInfo() {
        EventAttendeeDO attendee = attendee(8L, 1L);
        attendee.setAttendeeEmail("b@test.com");
        attendee.setAttendeeName("Bob");
        attendee.setAttendeeMobile("456");
        attendee.setCheckInToken("token");
        attendee.setCheckInStatus(1);
        when(attendeeMapper.selectById(8L)).thenReturn(attendee);

        var result = service.get(8L);

        assertThat(result.getAttendeeEmail()).isEqualTo("b@test.com");
        assertThat(result.getCheckInStatus()).isEqualTo(1);
    }

    @Test
    void delete_missingAttendeeThrows() {
        when(attendeeMapper.selectById(7L)).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.delete(7L));
        assertThat(ex.getCode()).isEqualTo(ATTENDEE_NOT_EXIST.getCode());
    }

    @Test
    void delete_existingAttendeeRemovesRecord() {
        EventAttendeeDO attendee = attendee(9L, 1L);
        when(attendeeMapper.selectById(9L)).thenReturn(attendee);

        service.delete(9L);

        verify(attendeeMapper).deleteById(9L);
    }

    @Test
    void update_missingAttendeeThrows() {
        when(attendeeMapper.selectById(11L)).thenReturn(null);

        ServiceException ex =
                assertThrows(ServiceException.class, () -> service.update(11L, req()));
        assertThat(ex.getCode()).isEqualTo(ATTENDEE_NOT_EXIST.getCode());
    }

    @Test
    void update_checkedInAttendeeThrows() {
        EventAttendeeDO attendee = attendee(11L, 3L);
        attendee.setCheckInStatus(1);
        when(attendeeMapper.selectById(11L)).thenReturn(attendee);

        ServiceException ex =
                assertThrows(ServiceException.class, () -> service.update(11L, req()));
        assertThat(ex.getCode()).isEqualTo(UPDATE_ATTENDEE_FAILED.getCode());
        verify(eventRpcService, never()).getEvent(any());
    }

    @Test
    void update_eventMissingThrows() {
        EventAttendeeDO attendee = attendee(11L, 3L);
        when(attendeeMapper.selectById(11L)).thenReturn(attendee);
        when(eventRpcService.getEvent(attendee.getEventId())).thenReturn(null);

        ServiceException ex =
                assertThrows(ServiceException.class, () -> service.update(11L, req()));
        assertThat(ex.getCode()).isEqualTo(EVENT_NOT_FOUND.getCode());
    }

    @Test
    void update_updateFailureThrows() {
        EventAttendeeDO attendee = attendee(11L, 3L);
        when(attendeeMapper.selectById(11L)).thenReturn(attendee);
        when(eventRpcService.getEvent(attendee.getEventId())).thenReturn(event(3L));
        when(attendeeMapper.updateById(attendee)).thenReturn(0);

        ServiceException ex =
                assertThrows(ServiceException.class, () -> service.update(11L, req()));
        assertThat(ex.getCode()).isEqualTo(UPDATE_ATTENDEE_FAILED.getCode());
    }

    @Test
    void update_generatesTokenAndSendsEmail() {
        EventAttendeeDO attendee = attendee(11L, 3L);
        attendee.setCheckInToken(null);
        when(attendeeMapper.selectById(11L)).thenReturn(attendee);
        EventRespDTO event = event(3L);
        when(eventRpcService.getEvent(attendee.getEventId())).thenReturn(event);
        when(attendeeMapper.updateById(attendee)).thenReturn(1);
        when(qrCodeService.generateEventCheckInQrWithToken(any())).thenReturn(qrCodeResponse());
        when(userRpcService.getTenantById(1L)).thenReturn(tenant());

        SaTokenContextMockUtil.setMockContext();
        StpUtil.login(123L);
        StpUtil.getSession().set(Constants.SESSION_TENANT_ID, 1L);

        try {
            var response = service.update(11L, req());

            assertThat(response.getCheckInToken()).isNotBlank();
            assertThat(response.getQrCodeBase64()).isEqualTo(SAMPLE_BASE64);
            verify(attendeeMapper, times(2)).updateById(attendee);
            verify(qrCodeService).generateEventCheckInQrWithToken(attendee.getCheckInToken());
        } finally {
            StpUtil.logout(123L);
            SaTokenContextMockUtil.clearContext();
        }
    }

    @Test
    void update_whenTenantMissingStillSendsEmail() {
        EventAttendeeDO attendee = attendee(21L, 4L);
        attendee.setCheckInToken(null);
        when(attendeeMapper.selectById(21L)).thenReturn(attendee);
        EventRespDTO event = event(4L);
        when(eventRpcService.getEvent(attendee.getEventId())).thenReturn(event);
        when(attendeeMapper.updateById(attendee)).thenReturn(1);
        when(qrCodeService.generateEventCheckInQrWithToken(any())).thenReturn(qrCodeResponse());
        when(userRpcService.getTenantById(1L)).thenReturn(null);

        SaTokenContextMockUtil.setMockContext();
        StpUtil.login(456L);
        StpUtil.getSession().set(Constants.SESSION_TENANT_ID, 1L);

        try {
            var response = service.update(21L, req());

            assertThat(response.getQrCodeBase64()).isEqualTo(SAMPLE_BASE64);
            verify(attendeeNotificationPublisher).sendAttendeeInviteEmail(any());
        } finally {
            StpUtil.logout(456L);
            SaTokenContextMockUtil.clearContext();
        }
    }

    @Test
    void update_whenTenantLookupFails_throwsCreationFailed() {
        EventAttendeeDO attendee = attendee(31L, 6L);
        attendee.setCheckInToken(null);
        when(attendeeMapper.selectById(31L)).thenReturn(attendee);
        EventRespDTO event = event(6L);
        when(eventRpcService.getEvent(attendee.getEventId())).thenReturn(event);
        when(attendeeMapper.updateById(attendee)).thenReturn(1);
        when(qrCodeService.generateEventCheckInQrWithToken(any())).thenReturn(qrCodeResponse());
        when(userRpcService.getTenantById(1L)).thenThrow(new RuntimeException("rpc down"));

        SaTokenContextMockUtil.setMockContext();
        StpUtil.login(789L);
        StpUtil.getSession().set(Constants.SESSION_TENANT_ID, 1L);

        try {
            ServiceException ex =
                    assertThrows(ServiceException.class, () -> service.update(31L, req()));
            assertThat(ex.getCode()).isEqualTo(ATTENDEE_CREATION_FAILED.getCode());
            verify(attendeeNotificationPublisher, never()).sendAttendeeInviteEmail(any());
        } finally {
            StpUtil.logout(789L);
            SaTokenContextMockUtil.clearContext();
        }
    }

    @Test
    void checkIn_invalidTokenThrows() {
        when(attendeeMapper.selectByToken("missing")).thenReturn(null);

        ServiceException ex =
                assertThrows(ServiceException.class, () -> service.checkIn("missing"));
        assertThat(ex.getCode()).isEqualTo(INVALID_CHECKIN_TOKEN.getCode());
    }

    @Test
    void checkIn_alreadyCheckedInThrows() {
        EventAttendeeDO attendee = attendee(1L, 5L);
        attendee.setCheckInStatus(1);
        when(attendeeMapper.selectByToken("token")).thenReturn(attendee);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.checkIn("token"));
        assertThat(ex.getCode()).isEqualTo(ALREADY_CHECKED_IN.getCode());
    }

    @Test
    void checkIn_eventMissingThrows() {
        EventAttendeeDO attendee = attendee(1L, 5L);
        attendee.setCheckInStatus(0);
        when(attendeeMapper.selectByToken("token")).thenReturn(attendee);
        when(eventRpcService.getEvent(attendee.getEventId())).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.checkIn("token"));
        assertThat(ex.getCode()).isEqualTo(EVENT_NOT_FOUND.getCode());
    }

    @Test
    void checkIn_eventNotActiveThrows() {
        EventAttendeeDO attendee = attendee(1L, 5L);
        attendee.setCheckInStatus(0);
        when(attendeeMapper.selectByToken("token")).thenReturn(attendee);
        EventRespDTO event = event(5L);
        event.setStatus(EventStatusEnum.COMPLETED.getCode());
        when(eventRpcService.getEvent(attendee.getEventId())).thenReturn(event);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.checkIn("token"));
        assertThat(ex.getCode()).isEqualTo(EVENT_NOT_ACTIVE.getCode());
    }

    @Test
    void checkIn_notStartedThrows() {
        EventAttendeeDO attendee = attendee(1L, 5L);
        when(attendeeMapper.selectByToken("token")).thenReturn(attendee);
        EventRespDTO event = event(5L);
        event.setStartTime(LocalDateTime.now().plusHours(3));
        event.setEndTime(event.getStartTime().plusHours(2));
        when(eventRpcService.getEvent(attendee.getEventId())).thenReturn(event);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.checkIn("token"));
        assertThat(ex.getCode()).isEqualTo(CHECKIN_NOT_STARTED.getCode());
    }

    @Test
    void checkIn_endedThrows() {
        EventAttendeeDO attendee = attendee(1L, 5L);
        when(attendeeMapper.selectByToken("token")).thenReturn(attendee);
        EventRespDTO event = event(5L);
        event.setStartTime(LocalDateTime.now().minusHours(3));
        event.setEndTime(LocalDateTime.now().minusHours(1));
        when(eventRpcService.getEvent(attendee.getEventId())).thenReturn(event);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.checkIn("token"));
        assertThat(ex.getCode()).isEqualTo(CHECKIN_ENDED.getCode());
    }

    @Test
    void checkIn_successUpdatesStatus() {
        EventAttendeeDO attendee = attendee(1L, 5L);
        when(attendeeMapper.selectByToken("token")).thenReturn(attendee);
        EventRespDTO event = event(5L);
        event.setStartTime(LocalDateTime.now().minusHours(1));
        event.setEndTime(LocalDateTime.now().plusHours(1));
        when(eventRpcService.getEvent(attendee.getEventId())).thenReturn(event);
        when(attendeeMapper.updateById(attendee)).thenReturn(1);

        CheckInRespVO resp = service.checkIn("token");

        assertThat(resp.getSuccess()).isTrue();
        assertThat(attendee.getCheckInStatus()).isEqualTo(1);
        verify(attendeeMapper).updateById(attendee);
    }

    @Test
    void generateQrCodes_eventMissingThrows() {
        GenerateQrCodesReqVO req = new GenerateQrCodesReqVO();
        req.setEventId(8L);
        req.setAttendees(List.of(req("a@test.com")));
        when(eventRpcService.getEvent(8L)).thenReturn(null);

        ServiceException ex =
                assertThrows(
                        ServiceException.class, () -> service.generateQrCodesForAttendees(req));
        assertThat(ex.getCode()).isEqualTo(EVENT_NOT_FOUND.getCode());
    }

    @Test
    void generateQrCodes_allFailedThrows() {
        GenerateQrCodesReqVO req = new GenerateQrCodesReqVO();
        req.setEventId(8L);
        AttendeeReqVO bad = new AttendeeReqVO();
        bad.setEmail("");
        bad.setName("NoEmail");
        bad.setMobile("123");
        req.setAttendees(List.of(bad));
        when(eventRpcService.getEvent(8L)).thenReturn(event(8L));

        ServiceException ex =
                assertThrows(
                        ServiceException.class, () -> service.generateQrCodesForAttendees(req));
        assertThat(ex.getCode()).isEqualTo(ATTENDEE_CREATION_FAILED.getCode());
    }

    @Test
    void generateQrCodes_partialSuccessReturnsSummary() {
        GenerateQrCodesReqVO req = new GenerateQrCodesReqVO();
        req.setEventId(8L);
        AttendeeReqVO duplicate = req("dup@test.com");
        AttendeeReqVO fresh = req("fresh@test.com");
        req.setAttendees(List.of(duplicate, fresh));

        EventRespDTO event = event(8L);
        when(eventRpcService.getEvent(8L)).thenReturn(event);
        when(attendeeMapper.selectByEventAndEmail(8L, "dup@test.com"))
                .thenReturn(attendee(100L, 8L));
        when(attendeeMapper.selectByEventAndEmail(8L, "fresh@test.com")).thenReturn(null);
        when(qrCodeService.generateEventCheckInQrWithToken(any())).thenReturn(qrCodeResponse());
        doAnswer(
                        invocation -> {
                            EventAttendeeDO attendee = invocation.getArgument(0);
                            attendee.setId(200L);
                            return 1;
                        })
                .when(attendeeMapper)
                .insert(any(EventAttendeeDO.class));

        GenerateQrCodesRespVO resp = service.generateQrCodesForAttendees(req);

        assertThat(resp.getTotalCount()).isEqualTo(1);
        assertThat(resp.getAttendees())
                .extracting("attendeeEmail")
                .containsExactly("fresh@test.com");
    }

    @Test
    void getCheckInToken_blankEmailThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.getCheckInToken(1L, " "));
    }

    @Test
    void getCheckInToken_eventMissingThrows() {
        when(eventRpcService.getEvent(1L)).thenReturn(null);

        ServiceException ex =
                assertThrows(
                        ServiceException.class, () -> service.getCheckInToken(1L, "a@test.com"));
        assertThat(ex.getCode()).isEqualTo(EVENT_NOT_FOUND.getCode());
    }

    @Test
    void getCheckInToken_attendeeMissingThrows() {
        when(eventRpcService.getEvent(1L)).thenReturn(event(1L));
        when(attendeeMapper.selectByEventAndEmail(1L, "a@test.com")).thenReturn(null);

        ServiceException ex =
                assertThrows(
                        ServiceException.class, () -> service.getCheckInToken(1L, "a@test.com"));
        assertThat(ex.getCode()).isEqualTo(EVENT_ATTENDEE_NOT_FOUND.getCode());
    }

    @Test
    void getCheckInToken_generatesNewTokenWhenMissing() {
        EventAttendeeDO attendee = attendee(10L, 1L);
        attendee.setCheckInToken(null);
        when(eventRpcService.getEvent(1L)).thenReturn(event(1L));
        when(attendeeMapper.selectByEventAndEmail(1L, "a@test.com")).thenReturn(attendee);
        when(attendeeMapper.updateById(attendee)).thenReturn(1);

        String token = service.getCheckInToken(1L, "a@test.com");

        assertThat(token).isEqualTo(attendee.getCheckInToken());
        verify(attendeeMapper).updateById(attendee);
    }

    @Test
    void getCheckInToken_returnsExistingToken() {
        EventAttendeeDO attendee = attendee(10L, 1L);
        attendee.setCheckInToken("existing");
        when(eventRpcService.getEvent(1L)).thenReturn(event(1L));
        when(attendeeMapper.selectByEventAndEmail(1L, "a@test.com")).thenReturn(attendee);

        String token = service.getCheckInToken(1L, "a@test.com");

        assertThat(token).isEqualTo("existing");
        verify(attendeeMapper, never()).updateById(any());
    }

    @Test
    void getAttendeeInfo_blankTokenThrows() {
        ServiceException ex =
                assertThrows(ServiceException.class, () -> service.getAttendeeInfo(""));
        assertThat(ex.getCode()).isEqualTo(INVALID_CHECKIN_TOKEN.getCode());
    }

    @Test
    void getAttendeeInfo_missingAttendeeThrows() {
        when(attendeeMapper.selectByToken("token")).thenReturn(null);

        ServiceException ex =
                assertThrows(ServiceException.class, () -> service.getAttendeeInfo("token"));
        assertThat(ex.getCode()).isEqualTo(INVALID_CHECKIN_TOKEN.getCode());
    }

    @Test
    void getAttendeeInfo_eventMissingThrows() {
        EventAttendeeDO attendee = attendee(1L, 2L);
        when(attendeeMapper.selectByToken("token")).thenReturn(attendee);
        when(eventRpcService.getEvent(attendee.getEventId())).thenReturn(null);

        ServiceException ex =
                assertThrows(ServiceException.class, () -> service.getAttendeeInfo("token"));
        assertThat(ex.getCode()).isEqualTo(EVENT_NOT_FOUND.getCode());
    }

    @Test
    void getAttendeeInfo_successReturnsDetails() {
        EventAttendeeDO attendee = attendee(1L, 2L);
        attendee.setAttendeeName("Alice");
        attendee.setAttendeeEmail("a@test.com");
        attendee.setCheckInStatus(1);
        attendee.setCheckInTime(LocalDateTime.now());
        when(attendeeMapper.selectByToken("token")).thenReturn(attendee);
        EventRespDTO event = event(2L);
        event.setName("Event");
        when(eventRpcService.getEvent(attendee.getEventId())).thenReturn(event);

        var resp = service.getAttendeeInfo("token");

        assertThat(resp.getEventName()).isEqualTo("Event");
        assertThat(resp.getAttendeeEmail()).isEqualTo("a@test.com");
    }

    private EventAttendeeDO attendee(Long id, Long eventId) {
        EventAttendeeDO attendee =
                EventAttendeeDO.builder()
                        .id(id)
                        .eventId(eventId)
                        .attendeeEmail("temp@test.com")
                        .attendeeName("Temp")
                        .attendeeMobile("000")
                        .checkInStatus(0)
                        .checkInToken("token-" + id)
                        .build();
        attendee.setCreateTime(LocalDateTime.now().minusDays(1));
        attendee.setUpdateTime(LocalDateTime.now().minusHours(1));
        return attendee;
    }

    private EventRespDTO event(Long id) {
        EventRespDTO dto = new EventRespDTO();
        dto.setId(id);
        dto.setName("Event-" + id);
        dto.setDescription("desc");
        dto.setLocation("hall");
        dto.setStatus(EventStatusEnum.ACTIVE.getCode());
        dto.setStartTime(LocalDateTime.now().minusHours(1));
        dto.setEndTime(LocalDateTime.now().plusHours(1));
        dto.setGroups(List.of(new GroupVO()));
        return dto;
    }

    private AttendeeReqVO req() {
        AttendeeReqVO req = new AttendeeReqVO();
        req.setEmail("updated@test.com");
        req.setName("Updated");
        req.setMobile("123456");
        return req;
    }

    private AttendeeReqVO req(String email) {
        AttendeeReqVO req = new AttendeeReqVO();
        req.setEmail(email);
        req.setName("Name-" + email);
        req.setMobile("999");
        return req;
    }

    private QrCodeRespVO qrCodeResponse() {
        return QrCodeRespVO.builder()
                .base64Image(SAMPLE_BASE64)
                .contentType("image/png")
                .size(400)
                .build();
    }

    private TenantDTO tenant() {
        TenantDTO tenant = new TenantDTO();
        tenant.setName("Tenant");
        tenant.setId(1L);
        return tenant;
    }
}
