package nus.edu.u.provider.email;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nus.edu.u.configuration.email.EmailProviderPropertiesConfig;
import nus.edu.u.domain.dto.common.AttachmentDTO;
import nus.edu.u.domain.dto.email.EmailSendResultDTO;
import nus.edu.u.enums.email.EmailProvider;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.*;

@Service
@RequiredArgsConstructor
public class SesEmailClient implements EmailClient {

    private final SesV2Client ses;
    private final EmailProviderPropertiesConfig props;

    @Override
    public EmailSendResultDTO sendEmail(
            String to, String subject, String html, List<AttachmentDTO> attachments) {
        if (attachments != null && !attachments.isEmpty())
            throw new UnsupportedOperationException("Use raw client for attachments");

        var req =
                SendEmailRequest.builder()
                        .fromEmailAddress(props.getFrom())
                        .destination(Destination.builder().toAddresses(to).build())
                        .content(
                                EmailContent.builder()
                                        .simple(
                                                Message.builder()
                                                        .subject(
                                                                Content.builder()
                                                                        .data(subject)
                                                                        .build())
                                                        .body(
                                                                Body.builder()
                                                                        .html(
                                                                                Content.builder()
                                                                                        .data(html)
                                                                                        .build())
                                                                        .build())
                                                        .build())
                                        .build())
                        .build();

        var resp = ses.sendEmail(req);
        return new EmailSendResultDTO(EmailProvider.AWS_SES, resp.messageId());
    }
}
