package nus.edu.u.domain.dto.common;

import java.util.Locale;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nus.edu.u.enums.common.NotificationChannel;

/** Unified template rendering DTO — supports Email, Push, and WebSocket channels. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateRenderRequestDTO {
    private NotificationChannel channel; // EMAIL | PUSH | WS
    private String templateKey; // e.g. "organizer-welcome"
    private Map<String, Object> variables; // thymeleaf variables + {subject,title,text,extras,...}
    @Builder.Default private Locale locale = Locale.ENGLISH; // fallback
}
