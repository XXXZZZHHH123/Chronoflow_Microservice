package nus.edu.u.task.domain.dto;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class EmailPubSubMessage {
    private String to;
    private String recipientKey;
    private String templateId;
    private Map<String, Object> variables;
    private Locale locale;
    private List<AttachmentDTO> attachments;
    private String eventId;
    private NotificationEventType type;
}
