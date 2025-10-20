package nus.edu.u.core.ws;

import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.core.common.NotificationSender;
import nus.edu.u.domain.dto.common.NotificationRequestDTO;
import nus.edu.u.domain.dto.common.RenderedTemplateDTO;
import nus.edu.u.domain.dto.ws.WsRequestDTO;
import nus.edu.u.enums.common.NotificationChannel;
import nus.edu.u.services.template.push.PushTemplateService;
import nus.edu.u.services.ws.WsService;
import org.springframework.stereotype.Component;

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
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            throw new IllegalArgumentException("userId is required for WebSocket notifications");
        }
        if (request.getEventId() == null || request.getEventId().isBlank()) {
            throw new IllegalArgumentException("eventId is required for WebSocket notifications");
        }
        if (request.getType() == null) {
            throw new IllegalArgumentException("type (NotificationEventType) is required");
        }

        // --- Render template (reuse PushTemplateService) ---
        String title;
        String body;
        Map<String, Object> extras;
        try {
            RenderedTemplateDTO rendered =
                    templateService.render(
                            request.getTemplateId(),
                            request.getVariables(),
                            request.getLocale() == null ? Locale.ENGLISH : request.getLocale());
            title = rendered.getTitle() != null ? rendered.getTitle() : "Notification";
            body = rendered.getBody() != null ? rendered.getBody() : "";
            extras = rendered.getExtras() != null ? rendered.getExtras() : Map.of();
        } catch (Exception ex) {
            Map<String, Object> vars =
                    request.getVariables() == null ? Map.of() : request.getVariables();
            title = String.valueOf(vars.getOrDefault("title", "Notification"));
            body = String.valueOf(vars.getOrDefault("body", ""));
            @SuppressWarnings("unchecked")
            Map<String, Object> fallbackExtras =
                    (vars.get("extras") instanceof Map<?, ?> m)
                            ? (Map<String, Object>) m
                            : Map.of();
            extras = fallbackExtras;
            log.debug("[WS] template fallback: {}", ex.getMessage());
        }

        String recipientKey = "ws:user:" + request.getUserId();

        WsRequestDTO dto =
                WsRequestDTO.builder()
                        .userId(request.getUserId())
                        .eventId(request.getEventId())
                        .type(request.getType())
                        .title(title)
                        .recipientKey(recipientKey)
                        .body(body)
                        .data(extras)
                        .build();

        String result = wsService.send(dto);

        log.info(
                "[WS] notification initiated for userId={} eventId={} status={}",
                request.getUserId(),
                request.getEventId(),
                result);

        return result;
    }
}
