package nus.edu.u.controllers;

import com.chronoflow.notification.core.push.PushNotificationSender;
import com.chronoflow.notification.domain.dto.common.AttachmentDTO;
import com.chronoflow.notification.domain.dto.common.NotificationRequestDTO;
import com.chronoflow.notification.enums.common.NotificationChannel;
import com.chronoflow.notification.enums.common.NotificationEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/push")
public class PushTestSenderController {

    private final PushNotificationSender pushSender; // directly use the PUSH sender

    /**
     * Example:
     * POST /api/v1/push/test/sender?userId=123&eventId=demo:task:42
     * body (JSON) optional or use query params below
     */
    @PostMapping("/test/sender")
    public ResponseEntity<Map<String, Object>> sendViaSender(
            @RequestParam String userId,
            @RequestParam(defaultValue = "demo:task:42") String eventId,
            @RequestParam(defaultValue = "Build Email Templates") String taskName,
            @RequestParam(defaultValue = "Alice Tan") String assignerName,
            @RequestParam(defaultValue = "ChronoFlow Kickoff") String eventName
    ) {
        // Variables consumed by push template: push/new-task-assigned.txt
        Map<String, Object> vars = Map.of(
                "title", "New task assigned ✅",
                "taskName", taskName,
                "assignerName", assignerName,
                "eventName", eventName
        );

        // Build a NotificationRequestDTO for PUSH (fan-out happens inside PushService)
        NotificationRequestDTO req = NotificationRequestDTO.builder()
                .channel(NotificationChannel.PUSH)
                .userId(userId)                       // required for push fan-out
                .templateId("new-task-assigned")      // thymeleaf file: templates/push/new-task-assigned.txt
                .variables(vars)
                .locale(Locale.ENGLISH)
                .attachments(List.<AttachmentDTO>of())
                .eventId(eventId)                     // idempotency key
                .type(NotificationEventType.ATTENDEE_INVITE)
                .build();

        String status = pushSender.send(req);

        return ResponseEntity.accepted().body(Map.of(
                "status", status,
                "userId", userId,
                "eventId", eventId
        ));
    }
}