package nus.edu.u.shared.rpc.notification.dto.Attendee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendeeInviteReqDTO {
    private String toEmail;
    private String attendeeName;
    private String attendeeMobile;

    private String organizationName;

    private byte[] qrCodeBytes;
    private String qrCodeContentType;

    private Long eventId;

    private String eventName;
    private String eventDate;
    private String eventLocation;
    private String eventDescription;
}
