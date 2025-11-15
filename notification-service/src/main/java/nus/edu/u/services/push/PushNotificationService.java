package nus.edu.u.services.push;

import nus.edu.u.domain.dto.common.NotificationRequestDTO;
import nus.edu.u.domain.dto.template.TemplateRequestDTO;
import nus.edu.u.domain.dto.template.TemplateResponseDTO;
import nus.edu.u.services.notification.NotificationService;
import nus.edu.u.services.notification.TemplateEngineImplementor;
import nus.edu.u.services.notification.TransportImplementor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class PushNotificationService extends NotificationService {

    public PushNotificationService(
            @Qualifier("pushTransport") TransportImplementor transportImplementor,
            @Qualifier("generalTemplate") TemplateEngineImplementor templateEngineImplementor) {
        super(transportImplementor, templateEngineImplementor);
    }

    public void send(NotificationRequestDTO dto) {
        TemplateRequestDTO templateRequestDTO =
                TemplateRequestDTO.builder()
                        .templateId(dto.getTemplateId())
                        .variables(dto.getVariables())
                        .locale(dto.getLocale())
                        .templateProvider(dto.getTemplateProvider())
                        .build();

        TemplateResponseDTO tpl = templateEngineImplementor.process(templateRequestDTO);

        NotificationRequestDTO pushReq =
                NotificationRequestDTO.builder()
                        .userId(dto.getUserId())
                        .notificationEventType(dto.getNotificationEventType())
                        .pushProvider(dto.getPushProvider())
                        .emailProvider(dto.getEmailProvider())
                        .recipientKey(dto.getRecipientKey())
                        .channel(dto.getChannel())
                        .templateProvider(dto.getTemplateProvider())
                        .eventId(dto.getEventId())
                        .to(dto.getTo())
                        .subject(tpl.getSubject())
                        .body(tpl.getBody())
                        .attachment(dto.getAttachment())
                        .build();

        transportImplementor.process(pushReq);
    }
}
