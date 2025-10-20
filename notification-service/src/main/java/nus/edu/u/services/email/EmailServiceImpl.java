package nus.edu.u.services.email;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.configuration.email.EmailLimitPropertiesConfig;
import nus.edu.u.domain.dataObject.common.NotificationDeliveryDO;
import nus.edu.u.domain.dataObject.email.EmailMessageDO;
import nus.edu.u.domain.dto.common.AttachmentDTO;
import nus.edu.u.domain.dto.email.EmailRequestDTO;
import nus.edu.u.enums.common.NotificationChannel;
import nus.edu.u.enums.common.NotificationStatus;
import nus.edu.u.enums.email.EmailProvider;
import nus.edu.u.enums.email.EmailStatus;
import nus.edu.u.exception.RateLimitExceededException;
import nus.edu.u.provider.email.EmailClient;
import nus.edu.u.provider.email.EmailClientFactory;
import nus.edu.u.repositories.common.NotificationDeliveryRepository;
import nus.edu.u.repositories.email.EmailMessageRepository;
import nus.edu.u.services.rateLimiter.RateLimiter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final NotificationDeliveryRepository deliveryRepo;
    private final EmailMessageRepository messageRepo;
    private final RateLimiter rateLimiter;
    private final EmailLimitPropertiesConfig props;
    private final EmailClientFactory emailClientFactory;

    @Override
    @Transactional
    public String send(EmailRequestDTO dto) {
        // Guards
        if (!rateLimiter.allow(props.getRateKey(), props.getRateLimit(), props.getRateWindow())) {
            throw new RateLimitExceededException("Rate limit exceeded for sending emails");
        }
        if (dto.getEventId() == null || dto.getEventId().isBlank()) {
            throw new IllegalArgumentException("eventId is required for idempotency");
        }
        if (dto.getRecipientKey() == null || dto.getRecipientKey().isBlank()) {
            throw new IllegalArgumentException("recipientKey is required");
        }
        if (dto.getType() == null) {
            throw new IllegalArgumentException("type (NotificationEventType) is required");
        }

        try {
            // 1) Insert delivery and FLUSH so unique constraint trips *before* external side
            // effects
            NotificationDeliveryDO delivery =
                    NotificationDeliveryDO.builder()
                            .eventId(dto.getEventId())
                            .recipientKey(dto.getRecipientKey())
                            .channel(NotificationChannel.EMAIL)
                            .type(dto.getType())
                            .status(NotificationStatus.CREATED)
                            .build();

            delivery = deliveryRepo.saveAndFlush(delivery);

            // 2) Insert channel row; you can flush here as well if you want it guaranteed in DB
            EmailMessageDO emailRow =
                    EmailMessageDO.builder()
                            .delivery(delivery)
                            .provider(EmailProvider.AWS_SES)
                            .status(EmailStatus.PENDING)
                            .build();

            emailRow = messageRepo.save(emailRow);
            // messageRepo.flush(); // optional

            // 3) Send via provider
            List<AttachmentDTO> attachments =
                    (dto.getAttachments() == null) ? Collections.emptyList() : dto.getAttachments();

            EmailClient client = emailClientFactory.getClient(attachments);
            var result =
                    client.sendEmail(dto.getTo(), dto.getSubject(), dto.getHtml(), attachments);

            // 4) Mark success
            emailRow.setProvider(result.provider());
            messageRepo.save(emailRow.markSent(result.providerMessageId()));

            delivery.setStatus(NotificationStatus.DELIVERED);
            deliveryRepo.save(delivery);

            log.info(
                    "Email DELIVERED: eventId={}, recipientKey={}, to={}",
                    dto.getEventId(),
                    dto.getRecipientKey(),
                    dto.getTo());
            return "ACCEPTED";

        } catch (org.springframework.dao.DataIntegrityViolationException dup) {
            // Unique (event_id, channel, recipient_key) hit — idempotent duplicate
            log.info(
                    "Duplicate email suppressed (idempotent): eventId={}, recipientKey={}",
                    dto.getEventId(),
                    dto.getRecipientKey());
            return "ALREADY_ACCEPTED";

        } catch (Exception ex) {
            log.error(
                    "Email FAILED: eventId={}, recipientKey={}, to={}, error={}",
                    dto.getEventId(),
                    dto.getRecipientKey(),
                    dto.getTo(),
                    ex.getMessage(),
                    ex);
            return "FAILED";
        }
    }
}
