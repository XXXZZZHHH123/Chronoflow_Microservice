package nus.edu.u.services.domains.task;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import nus.edu.u.domain.dto.common.NewTaskAssignmentDTO;
import nus.edu.u.domain.dto.common.NotificationRequestDTO;
import nus.edu.u.enums.common.NotificationChannel;
import nus.edu.u.enums.common.NotificationEventType;
import nus.edu.u.services.common.NotificationService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskAssignmentServiceImpl implements TaskAssignmentService {

    private final NotificationService notificationService;

    private String buildEventIdFor(NewTaskAssignmentDTO dto) {
        // idempotent per (task, domain-event, assignee)
        return NotificationEventType.buildEventId(
                NotificationEventType.NEW_TASK_ASSIGN,
                dto.getTaskId(),
                dto.getEventId(),
                dto.getAssigneeUserId());
    }

    private Map<String, Object> baseVars(NewTaskAssignmentDTO dto) {
        // shared variables for templates across channels
        return Map.of(
                "title", "New task assigned ✅",
                "taskName", dto.getTaskName(),
                "assignerName", dto.getAssignerName(),
                "eventName", dto.getEventName(),
                "eventId", dto.getEventId());
    }

    /** PUSH (FCM) */
    @Override
    public String notifyNewTaskToAssigneePush(NewTaskAssignmentDTO dto) {
        String eventId = buildEventIdFor(dto);

        NotificationRequestDTO req =
                NotificationRequestDTO.builder()
                        .channel(NotificationChannel.PUSH)
                        .userId(dto.getAssigneeUserId())
                        .templateId("new-task-assigned")
                        .variables(baseVars(dto))
                        .locale(Locale.ENGLISH)
                        .eventId(eventId)
                        .type(NotificationEventType.NEW_TASK_ASSIGN)
                        .build();

        return notificationService.send(req);
    }

    /** EMAIL */
    @Override
    public String notifyNewTaskToAssigneeEmail(NewTaskAssignmentDTO dto) {
        if (dto.getAssigneeEmail() == null || dto.getAssigneeEmail().isBlank()) {
            throw new IllegalArgumentException("assigneeEmail is required to send email");
        }

        String eventId = buildEventIdFor(dto);

        NotificationRequestDTO req =
                NotificationRequestDTO.builder()
                        .channel(NotificationChannel.EMAIL)
                        .to(dto.getAssigneeEmail())
                        .recipientKey("email:" + dto.getAssigneeEmail())
                        .templateId("new-task-assigned")
                        .variables(baseVars(dto))
                        .locale(Locale.ENGLISH)
                        .eventId(eventId)
                        .type(NotificationEventType.NEW_TASK_ASSIGN)
                        .build();

        return notificationService.send(req);
    }

    /** WEBSOCKET (feed + live push via gateway) */
    public String notifyNewTaskToAssigneeWs(NewTaskAssignmentDTO dto) {
        String eventId = buildEventIdFor(dto);

        // For WS, same template key is fine — title/body render; extras can be used as payload data
        // by the gateway
        NotificationRequestDTO req =
                NotificationRequestDTO.builder()
                        .channel(NotificationChannel.WS)
                        .userId(dto.getAssigneeUserId())
                        .templateId("new-task-assigned")
                        .variables(baseVars(dto))
                        .locale(Locale.ENGLISH)
                        .eventId(eventId)
                        .type(NotificationEventType.NEW_TASK_ASSIGN)
                        .build();

        return notificationService.send(req);
    }

    /** Aggregate: WS + PUSH (+ EMAIL if present) */
    @Override
    public Map<String, String> notifyNewTaskAllChannels(NewTaskAssignmentDTO dto) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("ws", notifyNewTaskToAssigneeWs(dto));
        result.put("push", notifyNewTaskToAssigneePush(dto));
        if (dto.getAssigneeEmail() != null && !dto.getAssigneeEmail().isBlank()) {
            result.put("email", notifyNewTaskToAssigneeEmail(dto));
        } else {
            result.put("email", "SKIPPED_NO_EMAIL");
        }
        return result;
    }
}
