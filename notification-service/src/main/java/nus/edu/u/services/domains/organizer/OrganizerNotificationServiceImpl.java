package nus.edu.u.services.domains.organizer;

import java.util.*;
import lombok.RequiredArgsConstructor;
import nus.edu.u.domain.dto.common.AttachmentDTO;
import nus.edu.u.domain.dto.common.NotificationRequestDTO;
import nus.edu.u.domain.dto.email.RegOrganizerReqDTO;
import nus.edu.u.enums.common.NotificationChannel;
import nus.edu.u.enums.common.NotificationEventType;
import nus.edu.u.services.common.NotificationService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Service
@RequiredArgsConstructor
public class OrganizerNotificationServiceImpl implements OrganizerNotificationService {

    private static final String WELCOME_EMAIL_ORGANIZER_TEMPLATE_ID = "welcome-email-organizer";
    private static final String LOGO_CID = "logo";

    private final NotificationService notificationService;

    @Override
    public String sendWelcomeOrganizerEmail(RegOrganizerReqDTO req) {

        Map<String, Object> vars = getOrganizerRequestVars(req);

        List<AttachmentDTO> attachments = new ArrayList<>();
        try {
            var res = new ClassPathResource("images/logo.png");
            if (res.exists()) {
                byte[] bytes = StreamUtils.copyToByteArray(res.getInputStream());
                attachments.add(
                        new AttachmentDTO(
                                "images/logo.png",
                                "image/png",
                                bytes,
                                null, // url
                                true, // inline
                                LOGO_CID));
            }
        } catch (Exception ignored) {
            // if logo missing, just proceed without it
        }

        var eventId =
                NotificationEventType.buildEventId(
                        NotificationEventType.ORGANIZER_WELCOME, req.getUserEmail());

        var request =
                NotificationRequestDTO.builder()
                        .channel(NotificationChannel.EMAIL)
                        .to(req.getUserEmail())
                        .recipientKey(req.getUserEmail())
                        .templateId(WELCOME_EMAIL_ORGANIZER_TEMPLATE_ID)
                        .variables(vars)
                        .locale(Locale.ENGLISH)
                        .attachments(attachments)
                        .type(NotificationEventType.ORGANIZER_WELCOME)
                        .eventId(eventId)
                        .build();

        return notificationService.send(request);
    }

    private static Map<String, Object> getOrganizerRequestVars(RegOrganizerReqDTO req) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("subject", "Welcome to ChronoFlow, " + req.getName() + "!");
        vars.put("name", req.getName());
        vars.put("username", req.getUsername());
        vars.put("email", req.getUserEmail());
        vars.put("mobile", req.getMobile());
        vars.put("organizationName", req.getOrganizationName());
        vars.put(
                "organizationAddress",
                req.getOrganizationAddress() == null ? "" : req.getOrganizationAddress());
        vars.put(
                "organizationCode",
                req.getOrganizationCode() == null ? "" : req.getOrganizationCode());
        vars.put("logoCid", LOGO_CID);

        return vars;
    }
}
