package nus.edu.u.domain.dto.test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendeeInviteReqDTOTest {
    private String toEmail;
    private String attendeeName;
    private String attendeeMobile;

    private String organizationName;

    private String base64Image;
    private String qrCodeContentType;

    private Long eventId;

    private String eventName;
    private String eventDate;
    private String eventLocation;
    private String eventDescription;
}
