package nus.edu.u.services.domains.attendee;

import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.domain.dto.common.AttachmentDTO;
import nus.edu.u.domain.dto.common.NotificationRequestDTO;
import nus.edu.u.domain.dto.email.AttendeeInviteReqDTO;
import nus.edu.u.enums.common.NotificationChannel;
import nus.edu.u.enums.common.NotificationEventType;
import nus.edu.u.services.common.NotificationService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class AttendeeNotificationServiceImpl implements AttendeeNotificationService {

    private static final String TEMPLATE_ID = "attendee-qr-invite";
    private static final String QR_CID = "qr-code";
    private static final String LOGO_CID = "logo";

    private final NotificationService notificationService;

    @Override
    public String sendAttendeeInviteEmail(AttendeeInviteReqDTO req) {
        // basic guardrails
        if (req.getToEmail() == null || req.getToEmail().isBlank()) {
            throw new IllegalArgumentException("toEmail is required");
        }

        Map<String, Object> vars = buildTemplateVars(req);

        List<AttachmentDTO> attachments = new ArrayList<>(2);
        addInlineQrIfPresent(req, attachments);
        addInlineLogoIfPresent(attachments);

        // deterministic event id (no dupes on retries)
        String eventId =
                NotificationEventType.buildEventId(
                        NotificationEventType.ATTENDEE_INVITE,
                        req.getToEmail(),
                        req.getOrganizationName(),
                        String.valueOf(req.getEventId() == null ? "no-event" : req.getEventId()));

        NotificationRequestDTO request =
                NotificationRequestDTO.builder()
                        .channel(NotificationChannel.EMAIL)
                        .to(req.getToEmail())
                        .recipientKey("email:" + req.getToEmail()) // normalize
                        .templateId(TEMPLATE_ID)
                        .variables(vars)
                        .locale(Locale.ENGLISH)
                        .attachments(attachments)
                        .eventId(eventId)
                        .type(NotificationEventType.ATTENDEE_INVITE)
                        .build();

        log.info(
                "Sending attendee invite email: to={}, cids=[qr:{}, logo:{}], atts={}",
                req.getToEmail(),
                QR_CID,
                LOGO_CID,
                attachments.size());

        return notificationService.send(request);
    }

    private static Map<String, Object> buildTemplateVars(AttendeeInviteReqDTO req) {
        Map<String, Object> v = new HashMap<>();
        v.put("subject", "Your QR code for " + nullSafe(req.getOrganizationName(), "your event"));
        v.put("attendeeName", req.getAttendeeName());
        v.put("organizationName", req.getOrganizationName());
        v.put("attendeeMobile", req.getAttendeeMobile());
        v.put("qrCodeCid", QR_CID);
        v.put("logoCid", LOGO_CID);
        v.put("eventName", req.getEventName());
        v.put("eventDate", req.getEventDate());
        v.put("eventLocation", req.getEventLocation());
        v.put("eventDescription", req.getEventDescription());
        return v;
    }

    /** Add the QR image as an inline part (CID = qr-code). No filename on inline. */
    private static void addInlineQrIfPresent(AttendeeInviteReqDTO req, List<AttachmentDTO> out) {
        byte[] bytes = req.getQrCodeBytes();
        if (bytes != null && bytes.length > 0) {
            out.add(
                    AttachmentDTO.builder()
                            .filename(null)
                            .contentType(
                                    req.getQrCodeContentType() != null
                                            ? req.getQrCodeContentType()
                                            : "image/png")
                            .bytes(bytes)
                            .url(null)
                            .inline(true)
                            .contentId(QR_CID)
                            .build());
        } else {
            log.warn("No QR bytes provided; inline QR will be omitted.");
        }
    }

    /** Load logo from classpath and attach as inline (CID = logo). No filename on inline. */
    private static void addInlineLogoIfPresent(List<AttachmentDTO> out) {
        try {
            ClassPathResource res = new ClassPathResource("/images/logo.png");
            if (res.exists()) {
                byte[] bytes = StreamUtils.copyToByteArray(res.getInputStream());
                out.add(
                        AttachmentDTO.builder()
                                .filename(null)
                                .contentType("image/png")
                                .bytes(bytes)
                                .url(null)
                                .inline(true)
                                .contentId(LOGO_CID)
                                .build());
            } else {
                log.warn("Inline logo not found at classpath: images/logo.png");
            }
        } catch (Exception e) {
            log.warn("Failed to load inline logo: {}", e.toString());
        }
    }

    private static String nullSafe(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s;
    }
}
