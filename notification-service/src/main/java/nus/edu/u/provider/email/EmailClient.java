package nus.edu.u.provider.email;

import java.util.List;
import nus.edu.u.domain.dto.common.AttachmentDTO;
import nus.edu.u.domain.dto.email.EmailSendResultDTO;

public interface EmailClient {
    EmailSendResultDTO sendEmail(
            String to, String subject, String html, List<AttachmentDTO> attachments);
}
