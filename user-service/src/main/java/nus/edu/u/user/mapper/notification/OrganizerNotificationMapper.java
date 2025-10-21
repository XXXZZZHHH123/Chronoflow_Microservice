package nus.edu.u.user.mapper.notification;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import nus.edu.u.shared.rpc.notification.dto.common.NotificationRequestDTO;
import nus.edu.u.shared.rpc.notification.dto.organizer.RegOrganizerReqDTO;
import nus.edu.u.shared.rpc.notification.enums.NotificationChannel;
import nus.edu.u.shared.rpc.notification.enums.NotificationEventType;

public class OrganizerNotificationMapper {

    public static NotificationRequestDTO RegOrganizerToNotification(RegOrganizerReqDTO req) {
        // validate required
        String to = Objects.toString(req.getUserEmail(), "").trim();
        if (to.isEmpty()) {
            throw new IllegalArgumentException("userEmail is required for organizer welcome email");
        }
        String username = Objects.toString(req.getUsername(), "").trim();
        String orgCode = Objects.toString(req.getOrganizationCode(), "").trim();

        Map<String, Object> vars =
                Map.of(
                        "name", Objects.toString(req.getName(), ""),
                        "username", username,
                        "userEmail", to,
                        "mobile", Objects.toString(req.getMobile(), ""),
                        "organizationName", Objects.toString(req.getOrganizationName(), ""),
                        "organizationAddress", Objects.toString(req.getOrganizationAddress(), ""),
                        "organizationCode", orgCode);

        // safe keys (idempotency + routing)
        String recipientKey = "email:" + to;
        String eventId = "organizer-registration-" + (orgCode.isEmpty() ? "unknown" : orgCode);

        return NotificationRequestDTO.builder()
                .channel(NotificationChannel.EMAIL)
                .to(to)
                .userId(username.isEmpty() ? to : username) // fallback to email if username missing
                .recipientKey(recipientKey)
                .templateId("welcome-email-organizer")
                .variables(vars)
                .locale(Locale.ENGLISH)
                .attachments(List.of())
                .eventId(eventId)
                .type(NotificationEventType.ORGANIZER_WELCOME)
                .build();
    }
}
