package nus.edu.u.services.clients;

import java.util.*;
import lombok.RequiredArgsConstructor;
import nus.edu.u.domain.dto.clients.RegSearchReqDTO;
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
public class MemberEmailServiceImpl implements MemberEmailService {

    private static final String MEMBER_INVITE_TEMPLATE_ID = "member-invite";
    private static final String LOGO_CID = "logo";
    private static final String INVITE_BASE_URL =
            "https://chronoflow-frontend-production.up.railway.app/login";

    private final EmailNotificationService emailNotificationService;

    @Override
    public void sendMemberInviteEmail(String recipientEmail, RegSearchReqDTO req) {

        String inviteUrl =
                INVITE_BASE_URL
                        + "?organisation_id="
                        + req.getOrganizationId()
                        + "&user_id="
                        + req.getUserId();

        Map<String, Object> vars = getMemberInviteVars(req, inviteUrl);

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
                            .emailProvider(EmailProvider.AWS_SES)
                            .templateProvider(TemplateProvider.Thymeleaf)
                            .to(recipientEmail)
                            .templateId(MEMBER_INVITE_TEMPLATE_ID)
                            .variables(vars)
                            .locale(Locale.ENGLISH)
                            .attachment(attachments)
                            .build();

            emailNotificationService.send(request);
        } catch (Exception ignored) {

        }
    }

    private static Map<String, Object> getMemberInviteVars(RegSearchReqDTO req, String inviteUrl) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("subject", "You’re invited to join ChronoFlow");
        vars.put("organizationId", req.getOrganizationId());
        vars.put("userId", req.getUserId());
        vars.put("inviteUrl", inviteUrl);
        vars.put("logoCid", LOGO_CID);
        return vars;
    }
}
