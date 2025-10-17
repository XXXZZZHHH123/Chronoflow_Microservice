package nus.edu.u.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.domain.dto.common.NotificationRequestDTO;
import nus.edu.u.enums.common.NotificationChannel;
import nus.edu.u.enums.common.NotificationEventType;
import nus.edu.u.services.common.NotificationService;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationSubscriber {

    private static final String SUBSCRIPTION = "chronoflow-notification-sub";

    private final PubSubTemplate pubSubTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @PostConstruct
    public void subscribe() {
        log.info("[PUBSUB] Subscribing to {}", SUBSCRIPTION);

        pubSubTemplate.subscribe(SUBSCRIPTION, message -> {
            try {
                String data = message.getPubsubMessage().getData().toStringUtf8();
                log.debug("[PUBSUB] Received message: {}", data);

                EmailPubSubMessage m = objectMapper.readValue(data, EmailPubSubMessage.class);

                if (m.getTo() == null || m.getEventId() == null) {
                    log.warn("[PUBSUB] ⚠️ Skipping invalid message: missing required fields");
                    message.ack(); // 👈 ACK even for bad messages — don’t requeue poison data
                    return;
                }

                NotificationRequestDTO req = NotificationRequestDTO.builder()
                        .channel(NotificationChannel.EMAIL)
                        .to(m.getTo())
                        .recipientKey("email:" + m.getTo())
                        .templateId(Optional.ofNullable(m.getTemplateId()).orElse("member-invite"))
                        .variables(m.getVariables() != null ? m.getVariables() : Map.of())
                        .locale(Optional.ofNullable(m.getLocale()).orElse(Locale.ENGLISH))
                        .eventId(m.getEventId())
                        .type(NotificationEventType.MEMBER_INVITE)
                        .build();

                String result = notificationService.send(req);
                log.info("[PUBSUB] ✅ Email notification processed. to={} eventId={} result={}",
                        m.getTo(), m.getEventId(), result);

                message.ack();

            } catch (Exception e) {
                log.error("[PUBSUB] ❌ Error processing message — will NOT requeue", e);
                message.ack(); // 👈 Important: ack to avoid infinite retry
                // Optionally: publish bad payload to DLQ manually
            }
        });
    }
}