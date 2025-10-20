package nus.edu.u.services.ws;

import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.configuration.ws.WsGatewayLimitPropertiesConfig;
import nus.edu.u.domain.dataObject.common.NotificationDeliveryDO;
import nus.edu.u.domain.dto.ws.WsRequestDTO;
import nus.edu.u.enums.common.NotificationChannel;
import nus.edu.u.enums.common.NotificationStatus;
import nus.edu.u.exception.RateLimitExceededException;
import nus.edu.u.provider.ws.WebSocketGatewayClient;
import nus.edu.u.repositories.common.NotificationDeliveryRepository;
import nus.edu.u.services.rateLimiter.RateLimiter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class WsServiceImpl implements WsService {

    private final NotificationDeliveryRepository deliveryRepo;
    private final WebSocketGatewayClient wsClient;
    private final RateLimiter rateLimiter;
    private final WsGatewayLimitPropertiesConfig props;

    @Override
    public String sendToUser(String userId, WsRequestDTO base) {
        if (userId == null || userId.isBlank())
            throw new IllegalArgumentException("userId is required");

        // build a dto with recipientKey = ws:user:<userId>
        WsRequestDTO dto =
                WsRequestDTO.builder()
                        .eventId(base.getEventId())
                        .recipientKey("ws:user:" + userId)
                        .userId(userId)
                        .title(base.getTitle())
                        .body(base.getBody())
                        .data(base.getData())
                        .type(base.getType())
                        .build();

        return send(dto);
    }

    @Override
    @Transactional
    public String send(WsRequestDTO dto) {
        // --- Guards / rate-limit ---
        if (!rateLimiter.allow(props.getRateKey(), props.getRateLimit(), props.getRateWindow())) {
            throw new RateLimitExceededException("Rate limit exceeded for WebSocket");
        }
        if (dto.getEventId() == null || dto.getEventId().isBlank())
            throw new IllegalArgumentException("eventId is required");
        if (dto.getRecipientKey() == null || dto.getRecipientKey().isBlank())
            throw new IllegalArgumentException("recipientKey is required");
        if (dto.getType() == null)
            throw new IllegalArgumentException("type (NotificationEventType) is required");
        if (dto.getUserId() == null || dto.getUserId().isBlank())
            throw new IllegalArgumentException("userId is required");

        NotificationDeliveryDO delivery = null;

        try {
            // 1) Insert parent delivery row and flush so idempotency can trip early
            delivery =
                    deliveryRepo.saveAndFlush(
                            NotificationDeliveryDO.builder()
                                    .eventId(dto.getEventId())
                                    .recipientKey(dto.getRecipientKey())
                                    .channel(NotificationChannel.WS)
                                    .type(dto.getType())
                                    .status(NotificationStatus.CREATED)
                                    .build());

            // 2) Call gateway (no WS-channel table; gateway persists Mongo feed)
            Map<String, Object> data =
                    dto.getData() == null ? Collections.emptyMap() : dto.getData();

            // NOTE: the gateway client expects WsPushRequestDTO; we can map inline
            var gwDto =
                    WsRequestDTO.builder()
                            .userId(dto.getUserId())
                            .eventId(dto.getEventId())
                            .type(dto.getType())
                            .title(dto.getTitle())
                            .body(dto.getBody())
                            .data(data)
                            .build();

            wsClient.sendToUser(gwDto).block();

            // 3) Success
            delivery.setStatus(NotificationStatus.DELIVERED);
            deliveryRepo.save(delivery);

            log.info(
                    "WS DELIVERED: eventId={}, recipientKey={}, userId={}",
                    dto.getEventId(),
                    dto.getRecipientKey(),
                    dto.getUserId());
            return "ACCEPTED";

        } catch (DataIntegrityViolationException dup) {
            // (eventId + channel + recipientKey) unique ⇒ idempotent duplicate
            log.info(
                    "WS duplicate suppressed (idempotent): eventId={}, recipientKey={}",
                    dto.getEventId(),
                    dto.getRecipientKey());
            return "ALREADY_ACCEPTED";

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
            return "FAILED";

        } catch (Exception ex) {
            // Generic failure
            log.warn(
                    "WS FAILED: eventId={}, recipientKey={}, userId={}, err={}",
                    dto.getEventId(),
                    dto.getRecipientKey(),
                    dto.getUserId(),
                    ex.toString(),
                    ex);
            try {
                if (delivery != null) {
                    delivery.setStatus(NotificationStatus.FAILED);
                    deliveryRepo.save(delivery);
                }
            } catch (Exception ignore) {
            }
            return "FAILED";
        }
    }
}
