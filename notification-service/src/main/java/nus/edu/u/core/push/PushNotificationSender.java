package nus.edu.u.core.push;

import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.core.common.NotificationSender;
import nus.edu.u.domain.dto.common.NotificationRequestDTO;
import nus.edu.u.domain.dto.common.RenderedTemplateDTO;
import nus.edu.u.domain.dto.push.PushRequestDTO;
import nus.edu.u.enums.common.NotificationChannel;
import nus.edu.u.services.push.PushService;
import nus.edu.u.services.template.push.PushTemplateService;
import org.springframework.stereotype.Component;

/**
 * Channel-specific sender for push notifications (via FCM or similar). Uses PushTemplateService for
 * rendering and PushService for fan-out delivery.
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
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            throw new IllegalArgumentException("userId is required for push notifications");
        }

        // --- Render template or fallback ---
        String title;
        String body;
        Map<String, Object> extras;

        try {
            RenderedTemplateDTO rendered =
                    pushTemplateService.render(
                            request.getTemplateId(),
                            request.getVariables(),
                            request.getLocale() == null ? Locale.ENGLISH : request.getLocale());
            title = rendered.getTitle() != null ? rendered.getTitle() : "Notification";
            body = rendered.getBody() != null ? rendered.getBody() : "";
            extras = rendered.getExtras() != null ? rendered.getExtras() : Map.of();

        } catch (Exception templateErr) {
            // Graceful fallback if no template or rendering fails
            Map<String, Object> vars =
                    request.getVariables() == null ? Map.of() : request.getVariables();
            title = String.valueOf(vars.getOrDefault("title", "Notification"));
            body = String.valueOf(vars.getOrDefault("body", ""));
            extras = (Map<String, Object>) vars.getOrDefault("extras", Map.of());
            log.debug("Push template fallback: {}", templateErr.getMessage());
        }

        // --- Build and delegate to PushService ---
        PushRequestDTO base =
                PushRequestDTO.builder()
                        .eventId(request.getEventId())
                        .title(title)
                        .body(body)
                        .data(extras)
                        .type(request.getType())
                        .build();

        pushService.sendToUser(request.getUserId(), base);

        log.info(
                "Push notification initiated for userId={}, eventId={}",
                request.getUserId(),
                request.getEventId());

        return "ACCEPTED";
    }
}
