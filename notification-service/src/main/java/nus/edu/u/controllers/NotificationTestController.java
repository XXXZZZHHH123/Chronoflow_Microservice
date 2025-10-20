package nus.edu.u.controllers;

import jakarta.validation.Valid;
import java.util.Base64;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import nus.edu.u.domain.dto.email.AttendeeInviteReqDTO;
import nus.edu.u.domain.dto.email.RegOrganizerReqDTO;
import nus.edu.u.domain.dto.email.RegSearchReqDTO;
import nus.edu.u.domain.dto.test.AttendeeInviteReqDTOTest;
import nus.edu.u.services.domains.attendee.AttendeeNotificationService;
import nus.edu.u.services.domains.member.MemberNotificationService;
import nus.edu.u.services.domains.organizer.OrganizerNotificationService;
import nus.edu.u.services.push.PushService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Test controller that exposes two ways to send: 1) JSON body (attachments as base64) -> /send/json
 * 2) multipart/form-data (real file upload) -> /send/attachments
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationTestController {

    private final OrganizerNotificationService organizerNotificationService;
    private final MemberNotificationService memberNotificationService;
    private final AttendeeNotificationService attendeeNotificationService;
    private final PushService pushService;

    //    @PostMapping
    //    public String sendNotificationByToken(@RequestBody PushMessageDTO pushMessageDTO)
    //    {
    //        return pushService.sendNotificationByToken(pushMessageDTO);
    //    }

    @PostMapping(value = "/attendee-invite", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> sendAttendeeInvite(
            @RequestBody AttendeeInviteReqDTOTest reqTest) throws Exception {

        // Decode Base64 → byte[]
        byte[] qrCodeBytes = Base64.getDecoder().decode(reqTest.getBase64Image());

        // Map into actual DTO used by the service
        var req =
                AttendeeInviteReqDTO.builder()
                        .toEmail(reqTest.getToEmail())
                        .attendeeName(reqTest.getAttendeeName())
                        .attendeeMobile(reqTest.getAttendeeMobile())
                        .organizationName(reqTest.getOrganizationName())
                        .qrCodeBytes(qrCodeBytes)
                        .qrCodeContentType(reqTest.getQrCodeContentType())
                        .eventId(reqTest.getEventId())
                        .eventName(reqTest.getEventName())
                        .eventDate(reqTest.getEventDate())
                        .eventLocation(reqTest.getEventLocation())
                        .eventDescription(reqTest.getEventDescription())
                        .build();

        // Send using your existing service
        String status = attendeeNotificationService.sendAttendeeInviteEmail(req);

        HttpStatus http = "ALREADY_ACCEPTED".equals(status) ? HttpStatus.OK : HttpStatus.ACCEPTED;

        return ResponseEntity.status(http).body(Map.of("status", status));
    }

    @PostMapping("/test/organizer/welcome")
    public ResponseEntity<?> sendWelcomeEmail(@Valid @RequestBody RegOrganizerReqDTO req) {
        String result = organizerNotificationService.sendWelcomeOrganizerEmail(req);
        HttpStatus status = "ALREADY_ACCEPTED".equals(result) ? HttpStatus.OK : HttpStatus.ACCEPTED;
        return ResponseEntity.status(status)
                .body(Map.of("status", result, "email", req.getUserEmail()));
    }

    @PostMapping("/test/member-invite")
    public ResponseEntity<Map<String, String>> testMemberInvite(
            @RequestParam String to, @RequestParam Long organizationId, @RequestParam Long userId) {
        var req = new RegSearchReqDTO(organizationId, userId, to);
        String result = memberNotificationService.sendMemberInviteEmail(req);
        return ResponseEntity.accepted().body(Map.of("status", result));
    }
}
