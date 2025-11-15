package nus.edu.u.services.push;

import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.configuration.push.PushLimitPropertiesConfig;
import nus.edu.u.domain.dataObject.common.NotificationDeliveryDO;
import nus.edu.u.domain.dataObject.push.PushMessageDO;
import nus.edu.u.domain.dto.common.NotificationRequestDTO;
import nus.edu.u.domain.dto.push.PushRequestDTO;
import nus.edu.u.enums.common.NotificationChannel;
import nus.edu.u.enums.common.NotificationStatus;
import nus.edu.u.enums.push.PushStatus;
import nus.edu.u.exception.RateLimitExceededException;
import nus.edu.u.provider.push.PushClient;
import nus.edu.u.provider.push.PushClientFactory;
import nus.edu.u.repositories.common.NotificationDeliveryRepository;
import nus.edu.u.repositories.push.PushMessageRepository;
import nus.edu.u.services.devices.DeviceRegistryService;
import nus.edu.u.services.notification.TransportImplementor;
import nus.edu.u.services.rateLimiter.RateLimiter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service("pushTransport")
@RequiredArgsConstructor
@Slf4j
public class PushTransport implements TransportImplementor {
    private final NotificationDeliveryRepository deliveryRepo;
    private final PushMessageRepository pushRepo;
    private final RateLimiter rateLimiter;
    private final PushLimitPropertiesConfig props;
    private final PushClientFactory pushClientFactory = PushClientFactory.getInstance();
    private final DeviceRegistryService deviceRegistry;

    @Override
    public void process(NotificationRequestDTO notificationRequestDTO) {
        var results = new LinkedHashMap<String, String>();
        var devices = deviceRegistry.activeDevices(notificationRequestDTO.getUserId());

        if (devices.isEmpty()) {
            log.info(
                    "Push skipped: no active devices for userId={}",
                    notificationRequestDTO.getUserId());
        }

        for (var d : devices) {
            var dto =
                    NotificationRequestDTO.builder()
                            .eventId(notificationRequestDTO.getEventId())
                            // per-device idempotency:
                            .recipientKey("push:token:" + d.getToken())
                            .pushProvider(notificationRequestDTO.getPushProvider())
                            .templateProvider(notificationRequestDTO.getTemplateProvider())
                            .token(d.getToken())
                            .title(notificationRequestDTO.getTitle())
                            .body(notificationRequestDTO.getBody())
                            .data(notificationRequestDTO.getData())
                            .notificationEventType(
                                    notificationRequestDTO.getNotificationEventType())
                            .build();

            String status = null;

            try {
                this.sendToUserDevices(dto);
            } catch (Exception e) {
                status = "Failed";
            }

            status = "Successful";

            results.put(d.getId(), status);
        }
    }

    @Transactional
    public void sendToUserDevices(NotificationRequestDTO notificationRequestDTO) {
        {
            // --- Guards ---
            if (!rateLimiter.allow(
                    props.getRateKey(), props.getRateLimit(), props.getRateWindow())) {
                throw new RateLimitExceededException("Rate limit exceeded for push");
            }
            if (notificationRequestDTO.getEventId() == null
                    || notificationRequestDTO.getEventId().isBlank())
                throw new IllegalArgumentException("eventId is required");
            if (notificationRequestDTO.getRecipientKey() == null
                    || notificationRequestDTO.getRecipientKey().isBlank())
                throw new IllegalArgumentException("recipientKey is required");
            if (notificationRequestDTO.getToken() == null
                    || notificationRequestDTO.getToken().isBlank())
                throw new IllegalArgumentException("token is required");
            if (notificationRequestDTO.getNotificationEventType() == null)
                throw new IllegalArgumentException("type (NotificationEventType) is required");

            NotificationDeliveryDO delivery = null;
            PushMessageDO pushRow = null;

            try {
                // 1) Insert delivery and FLUSH so unique constraint triggers before external call
                delivery =
                        deliveryRepo.saveAndFlush(
                                NotificationDeliveryDO.builder()
                                        .eventId(notificationRequestDTO.getEventId())
                                        .recipientKey(notificationRequestDTO.getRecipientKey())
                                        .channel(NotificationChannel.PUSH)
                                        .type(notificationRequestDTO.getNotificationEventType())
                                        .status(NotificationStatus.CREATED)
                                        .build());

                // 2) Channel row (PENDING)
                pushRow =
                        pushRepo.save(
                                PushMessageDO.builder()
                                        .delivery(delivery)
                                        .token(notificationRequestDTO.getToken())
                                        .status(PushStatus.PENDING)
                                        .build());

                // 3) Send via provider
                Map<String, Object> data =
                        notificationRequestDTO.getData() == null
                                ? Collections.emptyMap()
                                : notificationRequestDTO.getData();

                PushRequestDTO pushRequestDTO =
                        PushRequestDTO.builder()
                                .token(notificationRequestDTO.getToken())
                                .recipientKey(notificationRequestDTO.getRecipientKey())
                                .type(notificationRequestDTO.getNotificationEventType())
                                .title(notificationRequestDTO.getTitle())
                                .body(notificationRequestDTO.getBody())
                                .data(data)
                                .build();

                PushClient pushClient =
                        pushClientFactory.getClient(notificationRequestDTO.getPushProvider());

                String providerMsgId = pushClient.send(pushRequestDTO);

                // 4) Success state
                pushRow.setFcmId(providerMsgId);
                pushRepo.save(pushRow.markSent(providerMsgId));

                delivery.setStatus(NotificationStatus.DELIVERED);
                deliveryRepo.save(delivery);

                log.info(
                        "Push DELIVERED: eventId={}, recipientKey={}, token={}",
                        notificationRequestDTO.getEventId(),
                        notificationRequestDTO.getRecipientKey(),
                        notificationRequestDTO.getToken());

            } catch (DataIntegrityViolationException dup) {
                // idempotent duplicate (eventId + channel + recipientKey)
                log.info(
                        "Duplicate push suppressed (idempotent): eventId={}, recipientKey={}",
                        notificationRequestDTO.getEventId(),
                        notificationRequestDTO.getRecipientKey());

            } catch (FirebaseMessagingException fme) {
                String code =
                        (fme.getMessagingErrorCode() != null)
                                ? fme.getMessagingErrorCode().name()
                                : null;
                log.warn(
                        "FCM error: code={}, message={}, token={}",
                        code,
                        fme.getMessage(),
                        notificationRequestDTO.getToken());

                // If token is invalid/expired, immediately revoke it so we won't reuse it
                if ("UNREGISTERED".equals(code)) {
                    try {
                        deviceRegistry.revokeByToken(notificationRequestDTO.getToken());
                        log.info(
                                "Token revoked due to UNREGISTERED: {}",
                                notificationRequestDTO.getToken());
                    } catch (Exception ignore) {
                        // keep original failure
                    }
                }

                // best-effort state marking
                try {
                    if (pushRow != null) pushRepo.save(pushRow.markFailed(fme.getMessage()));
                } catch (Exception ignore) {
                }
                try {
                    if (delivery != null) {
                        delivery.setStatus(NotificationStatus.FAILED);
                        deliveryRepo.save(delivery);
                    }
                } catch (Exception ignore) {
                }

            } catch (Exception ex) {
                // generic failure
                log.warn(
                        "Push FAILED: eventId={}, recipientKey={}, token={}, err={}",
                        notificationRequestDTO.getEventId(),
                        notificationRequestDTO.getRecipientKey(),
                        notificationRequestDTO.getToken(),
                        ex.getMessage(),
                        ex);
                try {
                    if (pushRow != null) pushRepo.save(pushRow.markFailed(ex.getMessage()));
                } catch (Exception ignore) {
                }
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
}
