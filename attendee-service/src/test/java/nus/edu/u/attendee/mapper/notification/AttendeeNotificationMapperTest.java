package nus.edu.u.attendee.mapper.notification;

import static org.junit.jupiter.api.Assertions.*;

import nus.edu.u.shared.rpc.notification.dto.Attendee.AttendeeInviteReqDTO;
import nus.edu.u.shared.rpc.notification.dto.common.AttachmentDTO;
import nus.edu.u.shared.rpc.notification.dto.common.NotificationRequestDTO;
import org.junit.jupiter.api.Test;

class AttendeeNotificationMapperTest {

    @Test
    void attendeeInvitationToNotification_withQr_buildsAttachments() {
        AttendeeInviteReqDTO req =
                AttendeeInviteReqDTO.builder()
                        .toEmail("to@x.com")
                        .attendeeName("A")
                        .attendeeMobile("123")
                        .organizationName("Org")
                        .qrCodeBytes(new byte[] {1, 2, 3})
                        .qrCodeContentType("image/png")
                        .eventId(5L)
                        .eventName("E")
                        .eventDate("2025-10-22")
                        .eventLocation("L")
                        .eventDescription("D")
                        .build();

        NotificationRequestDTO dto =
                AttendeeNotificationMapper.attendeeInvitationToNotification(req);

        assertEquals("to@x.com", dto.getTo());
        assertEquals("attendee-qr-invite", dto.getTemplateId());
        assertEquals("email:to@x.com", dto.getRecipientKey());
        assertEquals(1, dto.getAttachments().size());
        AttachmentDTO a = dto.getAttachments().get(0);
        assertEquals("qrcode.png", a.getFilename());
        assertEquals("image/png", a.getContentType());
    }

    @Test
    void attendeeInvitationToNotification_withoutQr_noAttachments() {
        AttendeeInviteReqDTO req =
                AttendeeInviteReqDTO.builder()
                        .toEmail("to@x.com")
                        .attendeeName("A")
                        .attendeeMobile("123")
                        .organizationName("Org")
                        .eventId(5L)
                        .eventName("E")
                        .eventDate("2025-10-22")
                        .eventLocation("L")
                        .eventDescription("D")
                        .build();

        NotificationRequestDTO dto =
                AttendeeNotificationMapper.attendeeInvitationToNotification(req);

        assertEquals("to@x.com", dto.getTo());
        assertNotNull(dto.getVariables());
        assertEquals(0, dto.getAttachments().size());
    }
}
