package nus.edu.u.provider.email;

import jakarta.activation.DataHandler;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.configuration.email.EmailProviderPropertiesConfig;
import nus.edu.u.domain.dto.common.AttachmentDTO;
import nus.edu.u.domain.dto.email.EmailSendResultDTO;
import nus.edu.u.enums.email.EmailProvider;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.RawMessage;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SendEmailResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class SesRawAttachmentEmailClient implements EmailClient {

    private final SesV2Client ses;
    private final EmailProviderPropertiesConfig props;

    @Override
    public EmailSendResultDTO sendEmail(
            String to, String subject, String html, List<AttachmentDTO> attachments) {
        try {
            // Log summary to verify bytes + cids
            if (attachments != null) {
                for (AttachmentDTO a : attachments) {
                    log.info(
                            "ATT part -> inline={}, cid={}, filename={}, bytes={}, ct={}",
                            a.isInline(),
                            a.getContentId(),
                            a.getFilename(),
                            a.getBytes() == null ? 0 : a.getBytes().length,
                            a.getContentType());
                }
            }

            Session session = Session.getInstance(new Properties());
            MimeMessage mime = new MimeMessage(session);
            mime.setFrom(new InternetAddress(props.getFrom()));
            mime.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            mime.setSubject(subject, StandardCharsets.UTF_8.name());

            // TOP: mixed
            MimeMultipart mixed = new MimeMultipart("mixed");
            mime.setContent(mixed);

            // RELATED: html + inline images (same container)
            MimeBodyPart relatedContainer = new MimeBodyPart();
            MimeMultipart related = new MimeMultipart("related");
            relatedContainer.setContent(related);
            mixed.addBodyPart(relatedContainer);

            // HTML first
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setText(html, StandardCharsets.UTF_8.name(), "html");
            related.addBodyPart(htmlPart);

            // Inline images (no filename, no manual transfer-encoding)
            if (attachments != null) {
                for (AttachmentDTO a : attachments) {
                    if (a == null || !a.isInline()) continue;
                    byte[] bytes = a.getBytes();
                    if (bytes == null || bytes.length == 0) {
                        log.warn("Skipping inline with empty bytes; cid={}", a.getContentId());
                        continue;
                    }

                    String cid =
                            (a.getContentId() != null && !a.getContentId().isBlank())
                                    ? a.getContentId()
                                    : deriveCidFrom(a.getFilename());

                    MimeBodyPart inlinePart = new MimeBodyPart();
                    inlinePart.setDataHandler(
                            new DataHandler(
                                    new ByteArrayDataSource(
                                            bytes, safeContentType(a.getContentType()))));
                    inlinePart.setHeader("Content-ID", "<" + cid + ">");
                    inlinePart.setDisposition("inline");
                    // IMPORTANT: don't set filename for inline parts
                    related.addBodyPart(inlinePart);
                }
            }

            // Regular attachments (if any)
            if (attachments != null) {
                for (AttachmentDTO a : attachments) {
                    if (a == null || a.isInline()) continue;
                    byte[] bytes = a.getBytes();
                    if (bytes == null || bytes.length == 0) {
                        log.warn(
                                "Skipping attachment with empty bytes; filename={}",
                                a.getFilename());
                        continue;
                    }

                    MimeBodyPart attachPart = new MimeBodyPart();
                    attachPart.setDataHandler(
                            new DataHandler(
                                    new ByteArrayDataSource(
                                            bytes, safeContentType(a.getContentType()))));
                    attachPart.setFileName(
                            a.getFilename() != null ? a.getFilename() : "attachment");
                    attachPart.setDisposition("attachment");
                    mixed.addBodyPart(attachPart);
                }
            }

            mime.saveChanges();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            mime.writeTo(out);

            RawMessage raw =
                    RawMessage.builder().data(SdkBytes.fromByteArray(out.toByteArray())).build();

            SendEmailRequest req =
                    SendEmailRequest.builder()
                            .fromEmailAddress(props.getFrom())
                            .destination(d -> d.toAddresses(to)) // fine to keep
                            .content(EmailContent.builder().raw(raw).build())
                            .build();

            SendEmailResponse resp = ses.sendEmail(req);
            return new EmailSendResultDTO(EmailProvider.AWS_SES, resp.messageId());

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email with inline images", e);
        }
    }

    private static String safeContentType(String ct) {
        return (ct == null || ct.isBlank()) ? "image/png" : ct;
    }

    private static String deriveCidFrom(String filename) {
        if (filename == null || filename.isBlank()) return "inline-" + System.nanoTime();
        String just = filename.replaceAll("[^A-Za-z0-9]", "");
        return (just.isEmpty() ? "inline" : just) + "-" + System.nanoTime();
    }
}
