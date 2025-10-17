package nus.edu.u.core.ws;

import com.chronoflow.notification.core.common.NotificationSender;
import com.chronoflow.notification.domain.dto.common.NotificationRequestDTO;
import com.chronoflow.notification.domain.dto.common.RenderedTemplateDTO;
import com.chronoflow.notification.domain.dto.ws.WsRequestDTO;
import com.chronoflow.notification.enums.common.NotificationChannel;
import com.chronoflow.notification.services.template.push.PushTemplateService;
import com.chronoflow.notification.services.ws.WsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WsNotificationSender implements NotificationSender {

    private final PushTemplateService templateService; // reuse for WS
    private final WsService wsService;

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.WS;
    }

    @Override
    public String send(NotificationRequestDTO request) {
        if (request.userId() == null || request.userId().isBlank()) {
            throw new IllegalArgumentException("userId is required for WebSocket notifications");
        }
        if (request.eventId() == null || request.eventId().isBlank()) {
            throw new IllegalArgumentException("eventId is required for WebSocket notifications");
        }
        if (request.type() == null) {
            throw new IllegalArgumentException("type (NotificationEventType) is required");
        }

        // --- Render template (reuse PushTemplateService) ---
        String title;
        String body;
        Map<String, Object> extras;
        try {
            RenderedTemplateDTO rendered = templateService.render(
                    request.templateId(),
                    request.variables(),
                    request.locale() == null ? Locale.ENGLISH : request.locale()
            );
            title  = rendered.getTitle()  != null ? rendered.getTitle()  : "Notification";
            body   = rendered.getBody()   != null ? rendered.getBody()   : "";
            extras = rendered.getExtras() != null ? rendered.getExtras() : Map.of();
        } catch (Exception ex) {
            Map<String, Object> vars = request.variables() == null ? Map.of() : request.variables();
            title  = String.valueOf(vars.getOrDefault("title", "Notification"));
            body   = String.valueOf(vars.getOrDefault("body", ""));
            @SuppressWarnings("unchecked")
            Map<String, Object> fallbackExtras =
                    (vars.get("extras") instanceof Map<?, ?> m) ? (Map<String, Object>) m : Map.of();
            extras = fallbackExtras;
            log.debug("[WS] template fallback: {}", ex.getMessage());
        }

        String recipientKey = "ws:user:" + request.userId();

        WsRequestDTO dto = WsRequestDTO.builder()
                .userId(request.userId())
                .eventId(request.eventId())
                .type(request.type())
                .title(title)
                .recipientKey(recipientKey)
                .body(body)
                .data(extras)
                .build();

        String result = wsService.send(dto);

        log.info("[WS] notification initiated for userId={} eventId={} status={}",
                request.userId(), request.eventId(), result);

        return result;
    }
}