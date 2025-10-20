package nus.edu.u.domain.dto.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nus.edu.u.enums.common.NotificationChannel;
import nus.edu.u.enums.common.NotificationEventType;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO {

    @NotNull private NotificationChannel channel;

    private String to;

    private String userId;

    private String recipientKey;

    private String templateId;

    private Map<String, Object> variables;

    private Locale locale;

    private List<AttachmentDTO> attachments;

    @NotBlank private String eventId;

    @NotNull private NotificationEventType type;

    /** Immutable-style helper methods */
    public NotificationRequestDTO withLocale(Locale locale) {
        return this.toBuilder().locale(locale).build();
    }

    public NotificationRequestDTO withVariables(Map<String, Object> vars) {
        return this.toBuilder().variables(vars).build();
    }

    public NotificationRequestDTO withTemplateId(String templateId) {
        return this.toBuilder().templateId(templateId).build();
    }
}
