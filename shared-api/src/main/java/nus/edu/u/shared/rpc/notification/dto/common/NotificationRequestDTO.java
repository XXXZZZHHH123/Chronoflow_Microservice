package nus.edu.u.shared.rpc.notification.dto.common;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nus.edu.u.shared.rpc.notification.enums.common.NotificationChannel;
import nus.edu.u.shared.rpc.notification.enums.common.NotificationEventType;
import nus.edu.u.shared.rpc.notification.enums.email.EmailProvider;
import nus.edu.u.shared.rpc.notification.enums.push.PushProvider;
import nus.edu.u.shared.rpc.notification.enums.template.TemplateProvider;
import nus.edu.u.shared.rpc.notification.enums.ws.WSProvider;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO {

    String userId;
    NotificationChannel channel;
    EmailProvider emailProvider;
    PushProvider pushProvider;
    WSProvider wsProvider;
    NotificationEventType notificationEventType;
    String eventId;
    String to;
    String templateId;
    String subject;
    String body;
    String recipientKey;
    String token;
    String title;
    TemplateProvider templateProvider;
    Map<String, Object> variables;
    Locale locale;
    List<AttachmentDTO> attachment;
    private Map<String, Object> data;
}
