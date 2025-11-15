package nus.edu.u.services.ws;

import nus.edu.u.domain.dto.common.NotificationRequestDTO;
import nus.edu.u.domain.dto.template.TemplateRequestDTO;
import nus.edu.u.domain.dto.template.TemplateResponseDTO;
import nus.edu.u.services.notification.NotificationService;
import nus.edu.u.services.notification.TemplateEngineImplementor;
import nus.edu.u.services.notification.TransportImplementor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class WSNotificationService extends NotificationService {

    public WSNotificationService(
            @Qualifier("wsTransport") TransportImplementor transportImplementor,
            @Qualifier("generalTemplate") TemplateEngineImplementor templateEngineImplementor) {
        super(transportImplementor, templateEngineImplementor);
    }

    public void send(NotificationRequestDTO notificationRequestDTO) {
        TemplateRequestDTO templateRequestDTO =
                TemplateRequestDTO.builder()
                        .templateId(notificationRequestDTO.getTemplateId())
                        .variables(notificationRequestDTO.getVariables())
                        .locale(notificationRequestDTO.getLocale())
                        .templateProvider(notificationRequestDTO.getTemplateProvider())
                        .build();

        TemplateResponseDTO tpl = templateEngineImplementor.process(templateRequestDTO);

        NotificationRequestDTO wsReq =
                NotificationRequestDTO.builder()
                        .wsProvider(notificationRequestDTO.getWsProvider())
                        .templateProvider(notificationRequestDTO.getTemplateProvider())
                        .notificationEventType(notificationRequestDTO.getNotificationEventType())
                        .recipientKey(notificationRequestDTO.getRecipientKey())
                        .channel(notificationRequestDTO.getChannel())
                        .to(notificationRequestDTO.getTo())
                        .subject(tpl.getSubject())
                        .eventId(notificationRequestDTO.getEventId())
                        .body(tpl.getBody())
                        .userId(notificationRequestDTO.getUserId())
                        .attachment(notificationRequestDTO.getAttachment())
                        .build();
        transportImplementor.process(wsReq);
    }
}
