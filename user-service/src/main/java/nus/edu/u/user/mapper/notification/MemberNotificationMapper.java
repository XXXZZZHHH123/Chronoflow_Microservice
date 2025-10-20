package nus.edu.u.user.mapper.notification;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import nus.edu.u.shared.rpc.notification.dto.common.AttachmentDTO;
import nus.edu.u.shared.rpc.notification.dto.common.NotificationRequestDTO;
import nus.edu.u.shared.rpc.notification.dto.member.RegSearchReqDTO;
import nus.edu.u.shared.rpc.notification.enums.NotificationChannel;
import nus.edu.u.shared.rpc.notification.enums.NotificationEventType;

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
                .to(req.getRecipientEmail())
                .userId(String.valueOf(req.getUserId()))
                .recipientKey("email:" + req.getRecipientEmail())
                .templateId("member-invite")
                .variables(vars)
                .locale(Locale.ENGLISH)
                .attachments(attachments)
                .eventId("member-invitation-" + req.getOrganizationId() + "-" + req.getUserId())
                .type(NotificationEventType.MEMBER_INVITE)
                .build();
    }
}
