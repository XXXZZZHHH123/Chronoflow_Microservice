package nus.edu.u.services.clients;

import java.util.*;
import lombok.RequiredArgsConstructor;
import nus.edu.u.domain.dto.clients.RegOrganizerReqDTO;
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
public class OrganizerEmailServiceImpl implements OrganizerEmailService {

    private static final String WELCOME_EMAIL_ORGANIZER_TEMPLATE_ID = "welcome-email-organizer";
    private static final String LOGO_CID = "logo";

    private final EmailNotificationService emailNotificationService;

    @Override
    public void sendWelcomeEmailOrganizer(RegOrganizerReqDTO req) {

        Map<String, Object> vars = getOrganizerRequestVars(req);

        List<AttachmentDTO> attachments = new ArrayList<>();
        try {
            var res = new ClassPathResource("images/email/logo.png");
            if (res.exists()) {
                byte[] bytes = StreamUtils.copyToByteArray(res.getInputStream());
                attachments.add(
                        new AttachmentDTO(
                                "logo.png",
                                "image/png",
                                bytes,
                                null, // objectName
                                true, // inline
                                LOGO_CID));
            }

            var request =
                    NotificationRequestDTO.builder()
                            .channel(NotificationChannel.EMAIL)
                            .templateProvider(TemplateProvider.Thymeleaf)
                            .emailProvider(EmailProvider.AWS_SES)
                            .to(req.getUserEmail())
                            .templateId(WELCOME_EMAIL_ORGANIZER_TEMPLATE_ID)
                            .variables(vars)
                            .locale(Locale.ENGLISH)
                            .attachment(attachments)
                            .build();

            emailNotificationService.send(request);

        } catch (Exception ignored) {
            // if logo missing, just proceed without it
        }
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
