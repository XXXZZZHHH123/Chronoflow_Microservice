package nus.edu.u.shared.rpc.notification.dto.common;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nus.edu.u.shared.rpc.notification.enums.NotificationChannel;
import nus.edu.u.shared.rpc.notification.enums.NotificationEventType;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO {

    private NotificationChannel channel;

    private String to;

    private String userId;

    private String recipientKey;

    private String templateId;

    private Map<String, Object> variables;

    private Locale locale;

    private List<AttachmentDTO> attachments;

    private String eventId;

    private NotificationEventType type;
}
