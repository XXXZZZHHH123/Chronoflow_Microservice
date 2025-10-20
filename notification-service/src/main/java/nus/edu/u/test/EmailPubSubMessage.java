package nus.edu.u.test;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import nus.edu.u.domain.dto.common.AttachmentDTO;
import nus.edu.u.enums.common.NotificationEventType;

@Value
@Builder
@Jacksonized // <-- lets Jackson use the Lombok builder
public class EmailPubSubMessage {
    String to;
    String recipientKey;
    String templateId;
    Map<String, Object> variables;
    Locale locale;
    List<AttachmentDTO> attachments;
    String eventId;
    NotificationEventType type;
}
