package nus.edu.u.attendee.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import nus.edu.u.attendee.domain.vo.attendee.AttendeeInfoRespVO;
import nus.edu.u.attendee.domain.vo.attendee.AttendeeQrCodeRespVO;
import nus.edu.u.attendee.domain.vo.attendee.AttendeeReqVO;
import nus.edu.u.attendee.domain.vo.checkin.CheckInReqVO;
import nus.edu.u.attendee.domain.vo.checkin.CheckInRespVO;
import nus.edu.u.attendee.domain.vo.checkin.GenerateQrCodesReqVO;
import nus.edu.u.attendee.domain.vo.checkin.GenerateQrCodesRespVO;
import nus.edu.u.attendee.service.AttendeeService;
import nus.edu.u.attendee.service.excel.ExcelService;
import nus.edu.u.common.core.domain.CommonResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class AttendeeControllerTest {

    @Mock private AttendeeService attendeeService;

    @Mock private ExcelService excelService;

    @InjectMocks private AttendeeController controller;

    private AttendeeQrCodeRespVO sampleQr;

    @BeforeEach
    void setUp() {
        sampleQr =
                AttendeeQrCodeRespVO.builder()
                        .id(1L)
                        .attendeeEmail("a@example.com")
                        .attendeeName("Alice")
                        .attendeeMobile("123")
                        .checkInToken("tok-1")
                        .qrCodeBase64("base64")
                        .checkInStatus(0)
                        .build();
    }

    @Test
    void list_returnsList() {
        when(attendeeService.list(10L)).thenReturn(List.of(sampleQr));

        CommonResult<List<AttendeeQrCodeRespVO>> res = controller.list(10L);

        assertTrue(res.isSuccess());
        assertNotNull(res.getData());
        assertEquals(1, res.getData().size());
        assertEquals("a@example.com", res.getData().get(0).getAttendeeEmail());
    }

    @Test
    void get_returnsOne() {
        when(attendeeService.get(1L)).thenReturn(sampleQr);

        CommonResult<AttendeeQrCodeRespVO> res = controller.get(1L);

        assertTrue(res.isSuccess());
        assertEquals("Alice", res.getData().getAttendeeName());
    }

    @Test
    void delete_returnsTrue() {
        doNothing().when(attendeeService).delete(2L);

        CommonResult<Boolean> res = controller.delete(2L);

        assertTrue(res.isSuccess());
        assertTrue(res.getData());
    }

    @Test
    void attendeePreview_setsMessage() {
        AttendeeInfoRespVO info =
                AttendeeInfoRespVO.builder()
                        .attendeeName("Bob")
                        .attendeeEmail("bob@example.com")
                        .checkInStatus(0)
                        .checkInTime(LocalDateTime.now())
                        .eventName("Event X")
                        .build();

        when(attendeeService.getAttendeeInfo("token-123")).thenReturn(info);

        CommonResult<AttendeeInfoRespVO> res = controller.attendeePreview("token-123");

        assertTrue(res.isSuccess());
        assertEquals("bob@example.com", res.getData().getAttendeeEmail());
        assertNotNull(res.getData().getMessage());
        assertTrue(res.getData().getMessage().contains("please show this QR code"));
    }

    @Test
    void checkIn_returnsResp() {
        CheckInReqVO req = new CheckInReqVO();
        req.setToken("t1");

        CheckInRespVO resp =
                CheckInRespVO.builder()
                        .eventId(99L)
                        .eventName("Ev")
                        .userId(5L)
                        .userName("U")
                        .checkInTime(LocalDateTime.now())
                        .message("ok")
                        .success(true)
                        .build();

        when(attendeeService.checkIn("t1")).thenReturn(resp);

        CommonResult<CheckInRespVO> res = controller.checkIn(req);

        assertTrue(res.isSuccess());
        assertEquals(99L, res.getData().getEventId());
    }

    @Test
    void generateQrCodeByExcel_emptyFile_throws() {
        MockMultipartFile empty = new MockMultipartFile("file", new byte[0]);

        // Controller should throw ServiceException via exception(BAD_REQUEST) when file is empty.
        assertThrows(
                nus.edu.u.common.exception.ServiceException.class,
                () -> controller.generateQrCodeByExcel(1L, empty));
    }

    @Test
    void update_returnsUpdated() {
        AttendeeReqVO req = new AttendeeReqVO();
        req.setEmail("new@example.com");
        req.setName("New");
        req.setMobile("999");

        AttendeeQrCodeRespVO updated =
                AttendeeQrCodeRespVO.builder()
                        .id(3L)
                        .attendeeEmail("new@example.com")
                        .attendeeName("New")
                        .build();

        when(attendeeService.update(3L, req)).thenReturn(updated);

        CommonResult<AttendeeQrCodeRespVO> res = controller.update(3L, req);

        assertTrue(res.isSuccess());
        assertEquals(3L, res.getData().getId());
        assertEquals("new@example.com", res.getData().getAttendeeEmail());
    }

    @Test
    void generateQrCodes_direct_success() {
        GenerateQrCodesReqVO req = new GenerateQrCodesReqVO();
        req.setEventId(5L);
        req.setAttendees(List.of(new AttendeeReqVO()));

        GenerateQrCodesRespVO resp =
                GenerateQrCodesRespVO.builder()
                        .eventId(5L)
                        .eventName("E")
                        .totalCount(1)
                        .attendees(List.of(sampleQr))
                        .build();

        when(attendeeService.generateQrCodesForAttendees(req)).thenReturn(resp);

        CommonResult<GenerateQrCodesRespVO> res = controller.generateQrCodes(req);

        assertTrue(res.isSuccess());
        assertEquals(1, res.getData().getTotalCount());
    }

    @Test
    void generateQrCodeByExcel_success() {
        MockMultipartFile file = new MockMultipartFile("file", "content".getBytes());

        AttendeeReqVO a1 = new AttendeeReqVO();
        a1.setEmail("x@x.com");
        a1.setName("X");
        a1.setMobile("1");

        when(excelService.importAttendees(file)).thenReturn(List.of(a1));

        GenerateQrCodesRespVO resp =
                GenerateQrCodesRespVO.builder()
                        .eventId(7L)
                        .totalCount(1)
                        .attendees(List.of(sampleQr))
                        .build();

        when(attendeeService.generateQrCodesForAttendees(any())).thenReturn(resp);

        CommonResult<GenerateQrCodesRespVO> res = controller.generateQrCodeByExcel(7L, file);

        assertTrue(res.isSuccess());
        assertEquals(1, res.getData().getTotalCount());
    }
}
