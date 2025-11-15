package nus.edu.u.services.clients;

import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.domain.dto.clients.AttendeeInviteReqDTO;
import nus.edu.u.domain.dto.common.AttachmentDTO;
import nus.edu.u.domain.dto.common.NotificationRequestDTO;
import nus.edu.u.enums.common.NotificationChannel;
import nus.edu.u.enums.email.EmailProvider;
import nus.edu.u.enums.template.TemplateProvider;
import nus.edu.u.services.email.EmailNotificationService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendeeEmailServiceImpl implements AttendeeEmailService {

    private static final String ATTENDEE_INVITE_TEMPLATE_ID = "attendee-qr-invite";
    private static final String LOGO_CID = "logo";
    private static final String INLINE_CID = "attendee-qr";

    private final EmailNotificationService emailNotificationService;

    @Override
    public void sendAttendeeInvite(AttendeeInviteReqDTO req) {

        try {
            Map<String, Object> vars = getAttendeeInviteTemplateVars(req);

            List<AttachmentDTO> attachments = new ArrayList<>();
            attachments.addAll(getInlineQRCode(req));
            attachments.addAll(getInlineLogoAttachment());

            var request =
                    NotificationRequestDTO.builder()
                            .channel(NotificationChannel.EMAIL)
                            .to(req.getToEmail())
                            .templateId(ATTENDEE_INVITE_TEMPLATE_ID)
                            .emailProvider(EmailProvider.AWS_SES)
                            .templateProvider(TemplateProvider.Thymeleaf)
                            .variables(vars)
                            .locale(Locale.ENGLISH)
                            .attachment(attachments)
                            .build();

            emailNotificationService.send(request);
        } catch (Exception e) {

        }
    }

    private static Map<String, Object> getAttendeeInviteTemplateVars(AttendeeInviteReqDTO req) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("subject", "Your QR code for " + req.getOrganizationName());
        vars.put("attendeeName", req.getAttendeeName());
        vars.put("organizationName", req.getOrganizationName());
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
            var res = new ClassPathResource("images/email/logo.png");
            if (res.exists()) {
                byte[] bytes = StreamUtils.copyToByteArray(res.getInputStream());
                attachments.add(
                        new AttachmentDTO("logo.png", "image/png", bytes, null, true, LOGO_CID));
            }
        } catch (Exception ignored) {
            // ignore missing logo
        }
        return attachments;
    }
}
