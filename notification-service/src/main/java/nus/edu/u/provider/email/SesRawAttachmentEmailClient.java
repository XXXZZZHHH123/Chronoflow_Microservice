package nus.edu.u.provider.email;

import jakarta.activation.DataHandler;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SesRawAttachmentEmailClient implements EmailClient {

    private final SesV2Client ses;
    private final EmailProviderPropertiesConfig props;

    @Override
    public EmailSendResultDTO sendEmail(String to, String subject, String html, List<AttachmentDTO> attachments) {
        try {
            // --------- DEBUG: log what we're going to inline/attach ----------
            if (attachments != null) {
                String summary = attachments.stream()
                        .map(a -> String.format(
                                "inline=%s, cid=%s, filename=%s, bytes=%s, ct=%s",
                                a.isInline(),
                                a.getContentId(),
                                a.getFilename(),
                                a.getBytes() == null ? 0 : a.getBytes().length,
                                a.getContentType()))
                        .collect(Collectors.joining(" | "));
                log.info("Email attachments summary: {}", summary);
            }
            // ------------------------------------------------------------------

            // 1) Basic message shell
            Session session = Session.getInstance(new Properties());
            MimeMessage mime = new MimeMessage(session);
            mime.setFrom(new InternetAddress(props.getFrom()));
            mime.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            mime.setSubject(subject, StandardCharsets.UTF_8.name());

            // TOP: mixed
            MimeMultipart mixed = new MimeMultipart("mixed");
            mime.setContent(mixed);

            // related (html + inline images)
            MimeBodyPart relatedContainer = new MimeBodyPart();
            MimeMultipart related = new MimeMultipart("related");
            relatedContainer.setContent(related);
            mixed.addBodyPart(relatedContainer);

            // alternative (text/plain + text/html) INSIDE related
            MimeBodyPart alternativeContainer = new MimeBodyPart();
            MimeMultipart alternative = new MimeMultipart("alternative");
            alternativeContainer.setContent(alternative);
            related.addBodyPart(alternativeContainer);

            // plain text (fallback)
            MimeBodyPart textPart = new MimeBodyPart();
            String plain = stripHtmlForFallback(html);
            textPart.setText(plain, StandardCharsets.UTF_8.name());
            alternative.addBodyPart(textPart);

            // html
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setText(html, StandardCharsets.UTF_8.name(), "html");
            alternative.addBodyPart(htmlPart);

            // inline images (must be in the SAME "related" container as the html)
            if (attachments != null) {
                for (AttachmentDTO a : attachments) {
                    if (a == null || !a.isInline()) continue;
                    byte[] bytes = a.getBytes();
                    if (bytes == null || bytes.length == 0) {
                        log.warn("Skipping inline part with empty bytes. cid={}", a.getContentId());
                        continue;
                    }

                    MimeBodyPart inlinePart = new MimeBodyPart();
                    String contentType = safeContentType(a.getContentType());
                    inlinePart.setDataHandler(new DataHandler(new ByteArrayDataSource(bytes, contentType)));

                    String cid = (a.getContentId() != null && !a.getContentId().isBlank())
                            ? a.getContentId()
                            : deriveCidFrom(a.getFilename());
                    // MUST be <cid> without "cid:" prefix
                    inlinePart.setHeader("Content-ID", "<" + cid + ">");
                    inlinePart.setDisposition("inline");
                    if (a.getFilename() != null) inlinePart.setFileName(a.getFilename());

                    related.addBodyPart(inlinePart);
                }
            }

            // non-inline attachments (optional)
            if (attachments != null) {
                for (AttachmentDTO a : attachments) {
                    if (a == null || a.isInline()) continue;
                    byte[] bytes = a.getBytes();
                    if (bytes == null || bytes.length == 0) {
                        log.warn("Skipping attachment with empty bytes. filename={}", a.getFilename());
                        continue;
                    }

                    MimeBodyPart attachPart = new MimeBodyPart();
                    String contentType = safeContentType(a.getContentType());
                    attachPart.setDataHandler(new DataHandler(new ByteArrayDataSource(bytes, contentType)));
                    attachPart.setFileName(a.getFilename() != null ? a.getFilename() : "attachment");
                    attachPart.setDisposition("attachment");

                    mixed.addBodyPart(attachPart);
                }
            }

            // finalize
            mime.saveChanges();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            mime.writeTo(out);

            RawMessage raw = RawMessage.builder()
                    .data(SdkBytes.fromByteArray(out.toByteArray()))
                    .build();

            SendEmailRequest req = SendEmailRequest.builder()
                    .fromEmailAddress(props.getFrom())
                    .destination(d -> d.toAddresses(to)) // ok to keep for RAW
                    .content(EmailContent.builder().raw(raw).build())
                    .build();

            SendEmailResponse resp = ses.sendEmail(req);
            return new EmailSendResultDTO(EmailProvider.AWS_SES, resp.messageId());

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email with inline images", e);
        }
    }

    private static String safeContentType(String ct) {
        return (ct == null || ct.isBlank()) ? "application/octet-stream" : ct;
    }

    private static String deriveCidFrom(String filename) {
        if (filename == null || filename.isBlank()) return "inline-" + System.nanoTime();
        String just = filename.replaceAll("[^A-Za-z0-9]", "");
        return (just.isEmpty() ? "inline" : just) + "-" + System.nanoTime();
    }

    private static String stripHtmlForFallback(String html) {
        return html == null ? "" : html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }
}