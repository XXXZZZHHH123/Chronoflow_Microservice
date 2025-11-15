package nus.edu.u.user.mapper.notification;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import nus.edu.u.shared.rpc.notification.dto.common.AttachmentDTO;
import nus.edu.u.shared.rpc.notification.dto.common.NotificationRequestDTO;
import nus.edu.u.shared.rpc.notification.dto.member.RegSearchReqDTO;
import nus.edu.u.shared.rpc.notification.enums.common.NotificationChannel;
import nus.edu.u.shared.rpc.notification.enums.common.NotificationEventType;
import nus.edu.u.shared.rpc.notification.enums.email.EmailProvider;
import nus.edu.u.shared.rpc.notification.enums.template.TemplateProvider;

public class MemberNotificationMapper {

    public static NotificationRequestDTO RegMemberToNotification(RegSearchReqDTO req) {

        Map<String, Object> vars =
                Map.of(
                        "organizationId", req.getOrganizationId(),
                        "userId", req.getUserId(),
                        "recipientEmail", req.getRecipientEmail());

        List<AttachmentDTO> attachments = List.of();

        return NotificationRequestDTO.builder()
                .channel(NotificationChannel.EMAIL)
                .emailProvider(EmailProvider.AWS_SES)
                .templateProvider(TemplateProvider.Thymeleaf)
                .to(req.getRecipientEmail())
                .userId(String.valueOf(req.getUserId()))
                .recipientKey("email:" + req.getRecipientEmail())
                .templateId("/user/member-invite")
                .variables(vars)
                .locale(Locale.ENGLISH)
                .attachment(attachments)
                .eventId("member-invitation-" + req.getOrganizationId() + "-" + req.getUserId())
                .notificationEventType(NotificationEventType.MEMBER_INVITE)
                .build();
    }
}
