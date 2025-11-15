package nus.edu.u.domain.dto.common;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.*;
import nus.edu.u.enums.common.NotificationChannel;
import nus.edu.u.enums.common.NotificationEventType;
import nus.edu.u.enums.email.EmailProvider;
import nus.edu.u.enums.push.PushProvider;
import nus.edu.u.enums.template.TemplateProvider;
import nus.edu.u.enums.ws.WSProvider;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
