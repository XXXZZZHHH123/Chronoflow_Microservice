package nus.edu.u.user.mapper.notification;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import nus.edu.u.shared.rpc.notification.dto.common.AttachmentDTO;
import nus.edu.u.shared.rpc.notification.dto.common.NotificationRequestDTO;
import nus.edu.u.shared.rpc.notification.dto.organizer.RegOrganizerReqDTO;
import nus.edu.u.shared.rpc.notification.enums.NotificationChannel;
import nus.edu.u.shared.rpc.notification.enums.NotificationEventType;

public class OrganizerNotificationMapper {

    public static NotificationRequestDTO RegOrganizerToNotification(RegOrganizerReqDTO req) {
        Map<String, Object> vars =
                Map.of(
                        "name", req.getName(),
                        "username", req.getUsername(),
                        "userEmail", req.getUserEmail(),
                        "mobile", req.getMobile(),
                        "organizationName", req.getOrganizationName(),
                        "organizationAddress", req.getOrganizationAddress(),
                        "organizationCode", req.getOrganizationCode());

        List<AttachmentDTO> attachments = List.of();

        return NotificationRequestDTO.builder()
                .channel(NotificationChannel.EMAIL)
                .to(req.getUserEmail())
                .userId(req.getUsername())
                .recipientKey("email:" + req.getUserEmail())
                .templateId("welcome-email-organizer")
                .variables(vars)
                .locale(Locale.ENGLISH)
                .attachments(attachments)
                .eventId("organizer-registration-" + req.getOrganizationCode())
                .type(NotificationEventType.ORGANIZER_WELCOME)
                .build();
    }
}
