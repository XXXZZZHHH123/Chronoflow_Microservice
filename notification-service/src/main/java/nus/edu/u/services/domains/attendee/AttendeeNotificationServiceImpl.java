package nus.edu.u.services.domains.attendee;


import lombok.RequiredArgsConstructor;
import nus.edu.u.domain.dto.common.AttachmentDTO;
import nus.edu.u.domain.dto.common.NotificationRequestDTO;
import nus.edu.u.domain.dto.email.AttendeeInviteReqDTO;
import nus.edu.u.enums.common.NotificationChannel;
import nus.edu.u.enums.common.NotificationEventType;
import nus.edu.u.services.common.NotificationService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AttendeeNotificationServiceImpl implements AttendeeNotificationService {

    private static final String ATTENDEE_INVITE_TEMPLATE_ID = "attendee-qr-invite";
    private static final String LOGO_CID = "logo";
    private static final String INLINE_CID = "attendee-qr";

    private final NotificationService notificationService;

    @Override
    public String sendAttendeeInviteEmail(AttendeeInviteReqDTO req) {

        // 1) Build template variables
        Map<String, Object> vars = getAttendeeInviteTemplateVars(req);

        // 2) Build attachments (inline QR + inline logo)
        List<AttachmentDTO> attachments = new ArrayList<>();
        attachments.addAll(getInlineQRCode(req));
        attachments.addAll(getInlineLogoAttachment());

        // DDD-style deterministic event id so retries don’t duplicate:
        String eventId = NotificationEventType.buildEventId(
                NotificationEventType.ATTENDEE_INVITE,
                req.getToEmail(),
                req.getOrganizationName(),
                req.getEventId() == null ? "no-event" : String.valueOf(req.getEventId())
        );

        // 4) Dispatch
        NotificationRequestDTO request = NotificationRequestDTO.builder()
                .channel(NotificationChannel.EMAIL)
                .to(req.getToEmail())
                .recipientKey(req.getToEmail())
                .templateId(ATTENDEE_INVITE_TEMPLATE_ID)
                .variables(vars)
                .locale(Locale.ENGLISH)
                .attachments(attachments)
                .eventId(eventId)
                .type(NotificationEventType.ATTENDEE_INVITE)
                .build();

        return notificationService.send(request);
    }

    private static Map<String, Object> getAttendeeInviteTemplateVars(AttendeeInviteReqDTO req) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("subject", "Your QR code for " + req.getOrganizationName());
        vars.put("attendeeName", req.getAttendeeName());
        vars.put("organizationName", req.getOrganizationName());
        vars.put("attendeeMobile", req.getAttendeeMobile());
        vars.put("logoCid", LOGO_CID);
        vars.put("qrCodeCid", INLINE_CID);

        vars.put("eventName", req.getEventName());
        vars.put("eventDate", req.getEventDate());
        vars.put("eventLocation", req.getEventLocation());
        vars.put("eventDescription", req.getEventDescription());
        return vars;
    }

    private static List<AttachmentDTO> getInlineQRCode(AttendeeInviteReqDTO req) {
        List<AttachmentDTO> attachments = new ArrayList<>();
        if (req.getQrCodeBytes() != null && req.getQrCodeBytes().length > 0) {
            attachments.add(
                    new AttachmentDTO(
                            null,
                            req.getQrCodeContentType() != null
                                    ? req.getQrCodeContentType()
                                    : "image/png",
                            req.getQrCodeBytes(),
                            null,
                            true,
                            INLINE_CID));
        }
        return attachments;
    }

    private static List<AttachmentDTO> getInlineLogoAttachment() {
        List<AttachmentDTO> attachments = new ArrayList<>();
        try {
            var res = new ClassPathResource("images/logo.png");
            if (res.exists()) {
                byte[] bytes = StreamUtils.copyToByteArray(res.getInputStream());
                attachments.add(
                        new AttachmentDTO("chronoflow", "image/png", bytes, null, true, LOGO_CID));
            }
        } catch (Exception ignored) {
            // ignore missing logo
        }
        return attachments;
    }
}