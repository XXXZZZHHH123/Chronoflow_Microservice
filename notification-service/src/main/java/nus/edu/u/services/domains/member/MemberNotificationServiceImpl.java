package nus.edu.u.services.domains.member;

import java.util.*;
import lombok.RequiredArgsConstructor;
import nus.edu.u.domain.dto.common.AttachmentDTO;
import nus.edu.u.domain.dto.common.NotificationRequestDTO;
import nus.edu.u.domain.dto.email.RegSearchReqDTO;
import nus.edu.u.enums.common.NotificationChannel;
import nus.edu.u.enums.common.NotificationEventType;
import nus.edu.u.services.common.NotificationService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Service
@RequiredArgsConstructor
public class MemberNotificationServiceImpl implements MemberNotificationService {

    private static final String MEMBER_INVITE_TEMPLATE_ID = "member-invite";
    private static final String LOGO_CID = "logo";
    private static final String INVITE_BASE_URL =
            "https://chronoflow-frontend-production.up.railway.app/login";

    private final NotificationService notificationService;

    @Override
    public String sendMemberInviteEmail(RegSearchReqDTO req) {

        String inviteUrl =
                INVITE_BASE_URL
                        + "?organisation_id="
                        + req.getOrganizationId()
                        + "&user_id="
                        + req.getUserId();

        Map<String, Object> vars = getMemberInviteVars(req, inviteUrl);

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

        }

        var eventId =
                NotificationEventType.buildEventId(
                        NotificationEventType.MEMBER_INVITE,
                        req.getOrganizationId(),
                        req.getUserId());

        var request =
                NotificationRequestDTO.builder()
                        .channel(NotificationChannel.EMAIL)
                        .to(req.getRecipientEmail())
                        .recipientKey(req.getRecipientEmail())
                        .templateId(MEMBER_INVITE_TEMPLATE_ID)
                        .variables(vars)
                        .locale(Locale.ENGLISH)
                        .attachments(attachments)
                        .type(NotificationEventType.MEMBER_INVITE)
                        .eventId(eventId)
                        .build();

        return notificationService.send(request);
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
