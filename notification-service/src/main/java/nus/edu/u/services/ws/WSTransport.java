package nus.edu.u.services.ws;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.configuration.ws.WsGatewayLimitPropertiesConfig;
import nus.edu.u.domain.dataObject.common.NotificationDeliveryDO;
import nus.edu.u.domain.dto.common.NotificationRequestDTO;
import nus.edu.u.domain.dto.ws.WsRequestDTO;
import nus.edu.u.enums.common.NotificationChannel;
import nus.edu.u.enums.common.NotificationStatus;
import nus.edu.u.exception.RateLimitExceededException;
import nus.edu.u.provider.ws.WSClient;
import nus.edu.u.provider.ws.WSClientFactory;
import nus.edu.u.repositories.common.NotificationDeliveryRepository;
import nus.edu.u.services.notification.TransportImplementor;
import nus.edu.u.services.rateLimiter.RateLimiter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service("wsTransport")
@RequiredArgsConstructor
@Slf4j
public class WSTransport implements TransportImplementor {

    private final NotificationDeliveryRepository deliveryRepo;
    // static factory singleton (non-Spring, like your EmailClientFactory)
    private final WSClientFactory clientFactory = WSClientFactory.getInstance();
    private final RateLimiter rateLimiter;
    private final WsGatewayLimitPropertiesConfig props;

    @Override
    @Transactional
    public void process(NotificationRequestDTO notificationRequestDTO) {

        if (notificationRequestDTO == null) {
            throw new IllegalArgumentException("notificationRequestDTO must not be null");
        }

        // Only handle WS channel
        if (notificationRequestDTO.getChannel() != NotificationChannel.WS) {
            log.debug("WSTransport skipping non-WS notification: {}", notificationRequestDTO.getChannel());
            return;
        }

        // --- Rate limit (same as WsServiceImpl) ---
        if (!rateLimiter.allow(props.getRateKey(), props.getRateLimit(), props.getRateWindow())) {
            throw new RateLimitExceededException("Rate limit exceeded for WebSocket");
        }

        // Basic validation – adjust getters according to your NotificationRequestDTO
        if (notificationRequestDTO.getEventId() == null
                || notificationRequestDTO.getEventId().isBlank()) {
            throw new IllegalArgumentException("eventId is required");
        }
        if (notificationRequestDTO.getRecipientKey() == null
                || notificationRequestDTO.getRecipientKey().isBlank()) {
            throw new IllegalArgumentException("recipientKey is required");
        }
        if (notificationRequestDTO.getNotificationEventType() == null) {
            throw new IllegalArgumentException("type (NotificationEventType) is required");
        }
        if (notificationRequestDTO.getUserId() == null
                || notificationRequestDTO.getUserId().isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }

        NotificationDeliveryDO delivery = null;

        try {
            // 1) Insert parent delivery row and flush so idempotency can trip early
            delivery =
                    deliveryRepo.saveAndFlush(
                            NotificationDeliveryDO.builder()
                                    .eventId(notificationRequestDTO.getEventId())
                                    .recipientKey(notificationRequestDTO.getRecipientKey())
                                    .channel(NotificationChannel.WS)
                                    .type(notificationRequestDTO.getNotificationEventType())
                                    .status(NotificationStatus.CREATED)
                                    .build());

            // 2) Build WS payload from NotificationRequestDTO
            Map<String, Object> data =
                    notificationRequestDTO.getVariables() == null
                            ? Collections.emptyMap()
                            : new HashMap<>(notificationRequestDTO.getVariables());

            WsRequestDTO gwDto =
                    WsRequestDTO.builder()
                            .userId(notificationRequestDTO.getUserId())
                            .eventId(notificationRequestDTO.getEventId())
                            .type(notificationRequestDTO.getNotificationEventType())
                            .title(notificationRequestDTO.getTitle())
                            .body(notificationRequestDTO.getBody())
                            .data(data)
                            .build();

            // 3) Resolve WS client from factory and send
            WSClient wsClient = clientFactory.getClient(notificationRequestDTO.getWsProvider());
            wsClient.sendWSNotification(gwDto).block();

            // 4) Success
            delivery.setStatus(NotificationStatus.DELIVERED);
            deliveryRepo.save(delivery);

            log.info(
                    "WS DELIVERED: eventId={}, recipientKey={}, userId={}",
                    notificationRequestDTO.getEventId(),
                    notificationRequestDTO.getRecipientKey(),
                    notificationRequestDTO.getUserId());

        } catch (DataIntegrityViolationException dup) {
            // (eventId + channel + recipientKey) unique ⇒ idempotent duplicate
            log.info(
                    "WS duplicate suppressed (idempotent): eventId={}, recipientKey={}",
                    notificationRequestDTO.getEventId(),
                    notificationRequestDTO.getRecipientKey());

        } catch (WebClientResponseException wcre) {
            // Provider (gateway) error
            log.warn(
                    "WS FAILED: status={} reason={} body={}",
                    wcre.getRawStatusCode(),
                    wcre.getStatusText(),
                    wcre.getResponseBodyAsString());
            try {
                if (delivery != null) {
                    delivery.setStatus(NotificationStatus.FAILED);
                    deliveryRepo.save(delivery);
                }
            } catch (Exception ignore) {
            }

        } catch (Exception ex) {
            // Generic failure
            log.warn(
                    "WS FAILED: eventId={}, recipientKey={}, userId={}, err={}",
                    notificationRequestDTO.getEventId(),
                    notificationRequestDTO.getRecipientKey(),
                    notificationRequestDTO.getUserId(),
                    ex.toString(),
                    ex);
            try {
                if (delivery != null) {
                    delivery.setStatus(NotificationStatus.FAILED);
                    deliveryRepo.save(delivery);
                }
            } catch (Exception ignore) {
            }
        }
    }
}