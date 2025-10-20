package nus.edu.u.domain.dto.email;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nus.edu.u.domain.dto.common.AttachmentDTO;
import nus.edu.u.enums.common.NotificationEventType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequestDTO {

    /** Recipient email address */
    private String to;

    /** Recipient identifier for correlation (e.g. "email:thetnaungsoe19981@gmail.com") */
    private String recipientKey;

    /** Subject line of the email */
    private String subject;

    /** HTML or plain text body */
    private String html;

    /** Upstream event ID for idempotency and correlation */
    private String eventId;

    /** Notification type (e.g. ORGANIZER_WELCOME, MEMBER_INVITE, ATTENDEE_INVITE) */
    private NotificationEventType type;

    /** Optional attachments (images, PDFs, etc.) */
    private List<AttachmentDTO> attachments;
}
