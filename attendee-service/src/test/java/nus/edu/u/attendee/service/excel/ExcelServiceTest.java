package nus.edu.u.attendee.service.excel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.alibaba.excel.EasyExcel;
import java.io.ByteArrayOutputStream;
import java.util.List;
import nus.edu.u.attendee.domain.vo.attendee.AttendeeReqVO;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class ExcelServiceTest {

    private final ExcelService excelService = new ExcelService();

    @Test
    void importAttendees_success() throws Exception {
        AttendeeReqVO a = new AttendeeReqVO();
        a.setEmail("x@x.com");
        a.setName("X");
        a.setMobile("123");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EasyExcel.write(out, AttendeeReqVO.class).sheet().doWrite(List.of(a));

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "attendees.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        out.toByteArray());

        List<AttendeeReqVO> list = excelService.importAttendees(file);

        assertEquals(1, list.size());
        assertEquals("x@x.com", list.get(0).getEmail());
    }

    @Test
    void importAttendees_duplicateEmail_throws() throws Exception {
        AttendeeReqVO a1 = new AttendeeReqVO();
        a1.setEmail("dup@x.com");
        a1.setName("A");
        a1.setMobile("1");

        AttendeeReqVO a2 = new AttendeeReqVO();
        a2.setEmail("dup@x.com");
        a2.setName("B");
        a2.setMobile("2");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EasyExcel.write(out, AttendeeReqVO.class).sheet().doWrite(List.of(a1, a2));

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "attendees.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        out.toByteArray());

        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class, () -> excelService.importAttendees(file));
        assertEquals(true, ex.getMessage().contains("Duplicate email"));
    }

    @Test
    void importAttendees_missingField_throws() throws Exception {
        AttendeeReqVO a = new AttendeeReqVO();
        a.setEmail(""); // missing email
        a.setName("N");
        a.setMobile("M");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EasyExcel.write(out, AttendeeReqVO.class).sheet().doWrite(List.of(a));

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "attendees.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        out.toByteArray());

        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class, () -> excelService.importAttendees(file));
        // message contains the problematic email (empty string)
        assertEquals(true, ex.getMessage().contains("Invalid data for attendee"));
    }
}
