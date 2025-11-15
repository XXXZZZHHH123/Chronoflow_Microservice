package nus.edu.u.services.email;

import nus.edu.u.domain.dto.common.NotificationRequestDTO;
import nus.edu.u.domain.dto.template.TemplateRequestDTO;
import nus.edu.u.domain.dto.template.TemplateResponseDTO;
import nus.edu.u.services.notification.NotificationService;
import nus.edu.u.services.notification.TemplateEngineImplementor;
import nus.edu.u.services.notification.TransportImplementor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService extends NotificationService {

    public EmailNotificationService(
            @Qualifier("emailTransport") TransportImplementor transportImplementor,
            @Qualifier("generalTemplate") TemplateEngineImplementor templateEngineImplementor) {
        super(transportImplementor, templateEngineImplementor);
    }

    @Override
    public void send(NotificationRequestDTO dto) {

        TemplateRequestDTO templateRequestDTO =
                TemplateRequestDTO.builder()
                        .templateId(dto.getTemplateId())
                        .templateProvider(dto.getTemplateProvider())
                        .variables(dto.getVariables())
                        .locale(dto.getLocale())
                        .templateProvider(dto.getTemplateProvider())
                        .build();

        TemplateResponseDTO tpl = templateEngineImplementor.process(templateRequestDTO);

        NotificationRequestDTO emailReq =
                NotificationRequestDTO.builder()
                        .notificationEventType(dto.getNotificationEventType())
                        .templateProvider(dto.getTemplateProvider())
                        .eventId(dto.getEventId())
                        .recipientKey(dto.getRecipientKey())
                        .emailProvider(dto.getEmailProvider())
                        .to(dto.getTo())
                        .subject(tpl.getSubject())
                        .body(tpl.getBody())
                        .attachment(dto.getAttachment())
                        .build();

        transportImplementor.process(emailReq);
    }
}
