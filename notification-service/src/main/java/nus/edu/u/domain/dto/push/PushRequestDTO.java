package nus.edu.u.domain.dto.push;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nus.edu.u.enums.common.NotificationEventType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushRequestDTO {
    // Required for dedupe and recording
    private String eventId;
    private String recipientKey; // e.g. "push:token:<theToken>" or "user:<userId>"

    // Actual push content
    private String token; // FCM device token (or topic if you go topic route)
    private String title; // visible title
    private String body; // visible body
    private Map<String, Object> data; // extra key/values for the app

    // Your business “type” if you want to reuse enums
    private NotificationEventType type;
}
