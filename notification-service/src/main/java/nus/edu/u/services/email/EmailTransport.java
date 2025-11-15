package nus.edu.u.services.email;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.configuration.email.EmailLimitPropertiesConfig;
import nus.edu.u.domain.dataObject.common.NotificationDeliveryDO;
import nus.edu.u.domain.dataObject.email.EmailMessageDO;
import nus.edu.u.domain.dto.common.AttachmentDTO;
import nus.edu.u.domain.dto.common.NotificationRequestDTO;
import nus.edu.u.domain.dto.email.EmailRequestDTO;
import nus.edu.u.enums.common.NotificationChannel;
import nus.edu.u.enums.common.NotificationStatus;
import nus.edu.u.enums.email.EmailStatus;
import nus.edu.u.exception.RateLimitExceededException;
import nus.edu.u.provider.email.EmailClient;
import nus.edu.u.provider.email.EmailClientFactory;
import nus.edu.u.repositories.common.NotificationDeliveryRepository;
import nus.edu.u.repositories.email.EmailMessageRepository;
import nus.edu.u.services.notification.TransportImplementor;
import nus.edu.u.services.rateLimiter.RateLimiter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service("emailTransport")
@RequiredArgsConstructor
@Slf4j
public class EmailTransport implements TransportImplementor {

    private final NotificationDeliveryRepository deliveryRepository;
    private final EmailMessageRepository emailMessageRepository;
    private final EmailClientFactory emailClientFactory = EmailClientFactory.getInstance();
    private final RateLimiter rateLimiter;
    private final EmailLimitPropertiesConfig props;

    public void process(NotificationRequestDTO notificationRequestDTO) {
        var to = notificationRequestDTO.getTo();
        var subject = notificationRequestDTO.getSubject();
        var html = notificationRequestDTO.getBody();
        List<AttachmentDTO> attachments =
                (notificationRequestDTO.getAttachment() != null)
                        ? notificationRequestDTO.getAttachment()
                        : Collections.emptyList();

        if (!rateLimiter.allow(props.getRateKey(), props.getRateLimit(), props.getRateWindow())) {
            throw new RateLimitExceededException("Rate limit exceeded for sending emails");
        }
        if (notificationRequestDTO.getEventId() == null || notificationRequestDTO.getEventId().isBlank()) {
            throw new IllegalArgumentException("eventId is required for idempotency");
        }
        if (notificationRequestDTO.getRecipientKey() == null || notificationRequestDTO.getRecipientKey().isBlank()) {
            throw new IllegalArgumentException("recipientKey is required");
        }
        if (notificationRequestDTO.getNotificationEventType() == null) {
            throw new IllegalArgumentException("type (NotificationEventType) is required");
        }

        try {

            //Outbox microservice architecture
            // Create the Notification delivery DO object
            NotificationDeliveryDO delivery =
                    NotificationDeliveryDO.builder()
                            .eventId(notificationRequestDTO.getEventId())
                            .recipientKey(notificationRequestDTO.getRecipientKey())
                            .channel(NotificationChannel.EMAIL)
                            .type(notificationRequestDTO.getNotificationEventType())
                            .status(NotificationStatus.CREATED)
                            .build();

            delivery = deliveryRepository.saveAndFlush(delivery);


            // Create only for the email
            EmailMessageDO emailRow =
                    EmailMessageDO.builder()
                            .delivery(delivery)
                            .provider(notificationRequestDTO.getEmailProvider())
                            .status(EmailStatus.PENDING)
                            .build();

            emailRow = emailMessageRepository.save(emailRow);

            //Choose email provider/client
            EmailClient client =
                    emailClientFactory.getClient(notificationRequestDTO.getEmailProvider());


            //Construct the EmailRequestDTO for client sending
            EmailRequestDTO requestDTO =
                    EmailRequestDTO.builder()
                            .to(to)
                            .subject(subject)
                            .html(html)
                            .attachments(attachments)
                            .provider(notificationRequestDTO.getEmailProvider())
                            .build();

            //Send the email
            client.sendEmail(requestDTO);

            //Mark successful
            emailMessageRepository.save(emailRow);

            delivery.setStatus(NotificationStatus.DELIVERED);
            deliveryRepository.save(delivery);

            log.info(
                    "Email DELIVERED: eventId={}, recipientKey={}, to={}",
                    notificationRequestDTO.getEventId(),
                    notificationRequestDTO.getRecipientKey(),
                    notificationRequestDTO.getTo());

        } catch (DataIntegrityViolationException dup) {
            // Unique (event_id, channel, recipient_key) hit — idempotent duplicate
            log.info(
                    "Duplicate email suppressed (idempotent): eventId={}, recipientKey={}",
                    notificationRequestDTO.getEventId(),
                    notificationRequestDTO.getRecipientKey());

        } catch (Exception ex) {
            log.error(
                    "Email FAILED: eventId={}, recipientKey={}, to={}, error={}",
                    notificationRequestDTO.getEventId(),
                    notificationRequestDTO.getRecipientKey(),
                    notificationRequestDTO.getTo(),
                    ex.getMessage(),
                    ex);
        }
    }
}

