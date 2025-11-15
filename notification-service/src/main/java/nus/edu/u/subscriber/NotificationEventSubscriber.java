package nus.edu.u.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import jakarta.annotation.PostConstruct;
import java.util.Locale;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.domain.dto.common.NotificationRequestDTO;
import nus.edu.u.enums.common.NotificationChannel;
import nus.edu.u.services.email.EmailNotificationService;
import nus.edu.u.services.push.PushNotificationService;
import nus.edu.u.services.ws.WSNotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventSubscriber {

    private static final String SUBSCRIPTION = "chronoflow-notification-sub";

    private final PubSubTemplate pubSubTemplate;
    private final ObjectMapper objectMapper;

    // mark these final so Lombok injects them
    private final EmailNotificationService emailNotificationService;
    private final PushNotificationService pushNotificationService;
    private final WSNotificationService wsNotificationService;

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
                                || req.getNotificationEventType() == null) {
                            log.warn("[PUBSUB] Invalid message, missing required fields: {}", data);
                            message.ack(); // prevent requeue
                            return;
                        }

                        NotificationRequestDTO requestDTO = req;

                        // Apply sane defaults
                        if (req.getLocale() == null) {
                            req.setLocale(Locale.ENGLISH);
                        }
                        if (req.getVariables() == null) {
                            req.setVariables(Map.of());
                        }

                        NotificationChannel channel = req.getChannel();

                        // Dispatch by channel
                        switch (channel) {
                            case EMAIL -> emailNotificationService.send(req);
                            case PUSH -> pushNotificationService.send(req);
                            case WS -> wsNotificationService.send(req);
                            default -> {
                                log.warn("[PUBSUB] Unsupported notification channel: {}", channel);
                                message.ack();
                                return;
                            }
                        }

                        log.info(
                                "[PUBSUB] Processed notification. eventId={} channel={}",
                                req.getEventId(),
                                channel);

                        message.ack();

                    } catch (Exception e) {
                        log.error("[PUBSUB] Error processing message", e);
                        // Always ack to avoid infinite retry loops
                        message.ack();
                    }
                });
    }
}