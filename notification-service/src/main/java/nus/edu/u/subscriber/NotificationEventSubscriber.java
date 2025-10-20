package nus.edu.u.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import jakarta.annotation.PostConstruct;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.domain.dto.common.NotificationRequestDTO;
import nus.edu.u.services.common.NotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventSubscriber {

    private static final String SUBSCRIPTION = "chronoflow-notification-sub";

    private final PubSubTemplate pubSubTemplate;
    private final ObjectMapper objectMapper;

    private final NotificationService notificationService;

    @PostConstruct
    public void startSubscriber() {
        log.info("[PUBSUB] Subscribing to '{}'", SUBSCRIPTION);

        pubSubTemplate.subscribe(
                SUBSCRIPTION,
                message -> {
                    String data = null;
                    try {
                        data = message.getPubsubMessage().getData().toStringUtf8();
                        log.debug("[PUBSUB] Received raw message: {}", data);

                        // Parse JSON into DTO
                        NotificationRequestDTO req =
                                objectMapper.readValue(data, NotificationRequestDTO.class);

                        // Validate required fields
                        if (req.getChannel() == null
                                || req.getEventId() == null
                                || req.getType() == null) {
                            log.warn("[PUBSUB] Invalid message, missing required fields: {}", data);
                            message.ack(); // prevent requeue
                            return;
                        }

                        // Apply sane defaults
                        if (req.getLocale() == null) req = req.withLocale(Locale.ENGLISH);
                        if (req.getVariables() == null) req = req.withVariables(Map.of());

                        // Call NotificationService (email/push/ws etc.)
                        String result = notificationService.send(req);
                        log.info(
                                "[PUBSUB] Processed notification. eventId={} channel={} result={}",
                                req.getEventId(),
                                req.getChannel(),
                                result);

                        message.ack();

                    } catch (Exception e) {
                        log.error("[PUBSUB] Error processing message", e);

                        // Always ack to avoid infinite retry loops
                        message.ack();
                    }
                });
    }
}
