package nus.edu.u.core.email;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
        Locale locale = request.getLocale() != null ? request.getLocale() : Locale.ENGLISH;

        // Render email template directly via channel-specific service
        RenderedTemplateDTO rendered =
                emailTemplateService.render(
                        request.getTemplateId(), request.getVariables(), locale);

        // Merge attachments (template + runtime)
        List<AttachmentDTO> attachments = new ArrayList<>();
        if (rendered.getAttachments() != null) attachments.addAll(rendered.getAttachments());
        if (request.getAttachments() != null) attachments.addAll(request.getAttachments());

        // Build channel-specific DTO
        EmailRequestDTO email =
                EmailRequestDTO.builder()
                        .to(request.getTo())
                        .recipientKey(request.getRecipientKey())
                        .subject(rendered.getSubject())
                        .html(rendered.getHtml())
                        .eventId(request.getEventId())
                        .type(request.getType())
                        .attachments(attachments)
                        .build();

        // Delegate to email service (idempotency/rate-limit handled there)
        return emailService.send(email);
    }
}
