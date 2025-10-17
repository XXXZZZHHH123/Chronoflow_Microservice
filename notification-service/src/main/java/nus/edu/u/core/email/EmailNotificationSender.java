package nus.edu.u.core.email;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.core.common.NotificationSender;
import nus.edu.u.domain.dto.common.AttachmentDTO;
import nus.edu.u.domain.dto.common.NotificationRequestDTO;
import nus.edu.u.domain.dto.common.RenderedTemplateDTO;
import nus.edu.u.domain.dto.email.EmailRequestDTO;
import nus.edu.u.enums.common.NotificationChannel;
import nus.edu.u.services.email.EmailService;
import nus.edu.u.services.template.email.EmailTemplateService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationSender implements NotificationSender {

    private final EmailTemplateService emailTemplateService;
    private final EmailService emailService;

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.EMAIL;
    }

    @Override
    public String send(NotificationRequestDTO request) {
        // Fallback locale
        Locale locale = request.locale() != null ? request.locale() : Locale.ENGLISH;

        // Render email template directly via channel-specific service
        RenderedTemplateDTO rendered =
                emailTemplateService.render(request.templateId(), request.variables(), locale);

        // Merge attachments (template + runtime)
        List<AttachmentDTO> attachments = new ArrayList<>();
        if (rendered.getAttachments() != null) attachments.addAll(rendered.getAttachments());
        if (request.attachments() != null) attachments.addAll(request.attachments());

        // Build channel-specific DTO
        EmailRequestDTO email = EmailRequestDTO.builder()
                .to(request.to())
                .recipientKey(request.recipientKey())
                .subject(rendered.getSubject())
                .html(rendered.getHtml())
                .eventId(request.eventId())
                .type(request.type())
                .attachments(attachments)
                .build();

        // Delegate to email service (idempotency/rate-limit handled there)
        return emailService.send(email);
    }
}