package nus.edu.u.attendee.mapper.notification;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import nus.edu.u.shared.rpc.notification.dto.Attendee.AttendeeInviteReqDTO;
import nus.edu.u.shared.rpc.notification.dto.common.AttachmentDTO;
import nus.edu.u.shared.rpc.notification.dto.common.NotificationRequestDTO;
import nus.edu.u.shared.rpc.notification.enums.common.NotificationChannel;
import nus.edu.u.shared.rpc.notification.enums.common.NotificationEventType;
import nus.edu.u.shared.rpc.notification.enums.email.EmailProvider;
import nus.edu.u.shared.rpc.notification.enums.template.TemplateProvider;

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
                .emailProvider(EmailProvider.AWS_SES)
                .templateProvider(TemplateProvider.Thymeleaf)
                .to(req.getToEmail())
                .recipientKey("email:" + req.getToEmail())
                .templateId("attendee/attendee-qr-invite")
                .variables(vars)
                .locale(Locale.ENGLISH)
                .attachment(attachments)
                .eventId("attendee-invite-" + req.getEventId() + "-" + req.getToEmail())
                .notificationEventType(NotificationEventType.ATTENDEE_INVITE)
                .build();
    }
}
