package nus.edu.u.controllers;

import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import nus.edu.u.core.push.PushNotificationSender;
import nus.edu.u.domain.dto.common.NotificationRequestDTO;
import nus.edu.u.domain.dto.push.PushRequestDTO;
import nus.edu.u.enums.common.NotificationChannel;
import nus.edu.u.enums.common.NotificationEventType;
import nus.edu.u.services.push.PushService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/push")
public class PushTestController {

    private final PushService pushService;

    private final PushNotificationSender pushSender;

    /**
     * Test endpoint that uses PushNotificationSender (templating + fan-out).
     *
     * <p>Example: POST /api/v1/push/test-sender?userId=123&eventId=demo:123&title=Hello&body=World
     *
     * <p>If you want to use a template, pass templateId and omit title/body; otherwise title/body
     * are used as fallback variables.
     */
    @PostMapping("/test-sender")
    public ResponseEntity<?> sendWithSender(
            @RequestParam String userId,
            @RequestParam String eventId,
            @RequestParam(required = false) String templateId,
            @RequestParam(required = false, defaultValue = "Notification") String title,
            @RequestParam(required = false, defaultValue = "") String body) {
        // Variables passed to the template (or used as fallback by the sender)
        Map<String, Object> vars =
                Map.of(
                        "title", title,
                        "body", body,
                        // you can add more template vars here as needed
                        "extras", Map.of("source", "test-sender"));

        // NOTE: `to` and `recipientKey` are currently @NotBlank on your DTO.
        // They are not used by push, but must be populated to satisfy validation.
        NotificationRequestDTO req =
                NotificationRequestDTO.builder()
                        .channel(NotificationChannel.PUSH)
                        .userId(userId) // <-- REQUIRED for push fan-out
                        .templateId(templateId) // optional; sender will fallback to vars if null
                        .variables(vars)
                        .locale(Locale.ENGLISH)
                        .attachments(null) // not used for push
                        .eventId(eventId) // idempotency key
                        .type(NotificationEventType.ATTENDEE_INVITE) // pick any enum for testing
                        .build();

        String status = pushSender.send(req);
        // The sender returns a simple "ACCEPTED".
        // If you want per-device statuses in this endpoint, we can expose that later
        // by returning the Map from PushService.sendToUser, but this keeps the sender contract
        // intact.

        return ResponseEntity.accepted()
                .body(
                        Map.of(
                                "status", status,
                                "message", "Push send invoked via PushNotificationSender",
                                "userId", userId,
                                "eventId", eventId));
    }

    @PostMapping("/test")
    public ResponseEntity<?> sendTest(
            @RequestParam String userId,
            @RequestParam(defaultValue = "push-test:hello") String eventId,
            @RequestParam(defaultValue = "Hello!") String title,
            @RequestParam(defaultValue = "It works 🎉") String body) {

        var base =
                PushRequestDTO.builder()
                        .eventId(eventId)
                        .title(title)
                        .body(body)
                        .data(Map.of("demo", "true"))
                        .type(
                                NotificationEventType
                                        .ATTENDEE_INVITE) // any enum you want for the test
                        .build();

        var results = pushService.sendToUser(userId, base);
        return ResponseEntity.accepted().body(Map.of("status", "SENT", "results", results));
    }
}
