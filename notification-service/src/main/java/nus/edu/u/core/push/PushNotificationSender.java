package nus.edu.u.core.push;

import com.chronoflow.notification.core.common.NotificationSender;
import com.chronoflow.notification.domain.dto.common.NotificationRequestDTO;
import com.chronoflow.notification.domain.dto.common.RenderedTemplateDTO;
import com.chronoflow.notification.domain.dto.push.PushRequestDTO;
import com.chronoflow.notification.enums.common.NotificationChannel;
import com.chronoflow.notification.services.push.PushService;
import com.chronoflow.notification.services.template.push.PushTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

/**
 * Channel-specific sender for push notifications (via FCM or similar).
 * Uses PushTemplateService for rendering and PushService for fan-out delivery.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PushNotificationSender implements NotificationSender {

    private final PushTemplateService pushTemplateService;
    private final PushService pushService;

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.PUSH;
    }

    @Override
    public String send(NotificationRequestDTO request) {
        // --- Validation ---
        if (request.userId() == null || request.userId().isBlank()) {
            throw new IllegalArgumentException("userId is required for push notifications");
        }

        // --- Render template or fallback ---
        String title;
        String body;
        Map<String, Object> extras;

        try {
            RenderedTemplateDTO rendered = pushTemplateService.render(
                    request.templateId(),
                    request.variables(),
                    request.locale() == null ? Locale.ENGLISH : request.locale()
            );
            title  = rendered.getTitle() != null ? rendered.getTitle() : "Notification";
            body   = rendered.getBody()  != null ? rendered.getBody()  : "";
            extras = rendered.getExtras() != null ? rendered.getExtras() : Map.of();

        } catch (Exception templateErr) {
            // Graceful fallback if no template or rendering fails
            Map<String, Object> vars = request.variables() == null ? Map.of() : request.variables();
            title  = String.valueOf(vars.getOrDefault("title", "Notification"));
            body   = String.valueOf(vars.getOrDefault("body", ""));
            extras = (Map<String, Object>) vars.getOrDefault("extras", Map.of());
            log.debug("Push template fallback: {}", templateErr.getMessage());
        }

        // --- Build and delegate to PushService ---
        PushRequestDTO base = PushRequestDTO.builder()
                .eventId(request.eventId())
                .title(title)
                .body(body)
                .data(extras)
                .type(request.type())
                .build();

        pushService.sendToUser(request.userId(), base);

        log.info("Push notification initiated for userId={}, eventId={}",
                request.userId(), request.eventId());

        return "ACCEPTED";
    }
}
