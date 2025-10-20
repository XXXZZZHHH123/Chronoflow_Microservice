package nus.edu.u.attendee.mapper.notification;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import nus.edu.u.shared.rpc.notification.dto.Attendee.AttendeeInviteReqDTO;
import nus.edu.u.shared.rpc.notification.dto.common.AttachmentDTO;
import nus.edu.u.shared.rpc.notification.dto.common.NotificationRequestDTO;
import nus.edu.u.shared.rpc.notification.enums.NotificationChannel;
import nus.edu.u.shared.rpc.notification.enums.NotificationEventType;

public class AttendeeNotificationMapper {

    public static NotificationRequestDTO attendeeInvitationToNotification(
            AttendeeInviteReqDTO req) {

        Map<String, Object> vars =
                Map.of(
                        "attendeeName", req.getAttendeeName(),
                        "attendeeMobile", req.getAttendeeMobile(),
                        "organizationName", req.getOrganizationName(),
                        "eventName", req.getEventName(),
                        "eventDate", req.getEventDate(),
                        "eventLocation", req.getEventLocation(),
                        "eventDescription", req.getEventDescription());

        List<AttachmentDTO> attachments =
                (req.getQrCodeBytes() != null)
                        ? List.of(
                                AttachmentDTO.builder()
                                        .filename("qrcode.png")
                                        .contentType(
                                                req.getQrCodeContentType() != null
                                                        ? req.getQrCodeContentType()
                                                        : "image/png")
                                        .bytes(req.getQrCodeBytes())
                                        .inline(true)
                                        .contentId("qr-code")
                                        .build())
                        : List.of();

        return NotificationRequestDTO.builder()
                .channel(NotificationChannel.EMAIL)
                .to(req.getToEmail())
                .recipientKey("email:" + req.getToEmail())
                .templateId("attendee-qr-invite")
                .variables(vars)
                .locale(Locale.ENGLISH)
                .attachments(attachments)
                .eventId("attendee-invite-" + req.getEventId() + "-" + req.getToEmail())
                .type(NotificationEventType.ATTENDEE_INVITE)
                .build();
    }
}
