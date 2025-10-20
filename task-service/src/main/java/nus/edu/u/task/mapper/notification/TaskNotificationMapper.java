package nus.edu.u.task.mapper.notification;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import nus.edu.u.shared.rpc.notification.dto.common.NotificationRequestDTO;
import nus.edu.u.shared.rpc.notification.dto.task.NewTaskAssignmentDTO;
import nus.edu.u.shared.rpc.notification.enums.NotificationChannel;
import nus.edu.u.shared.rpc.notification.enums.NotificationEventType;

public class TaskNotificationMapper {

    /** Build the same idempotent eventId for every channel. */
    private static String idempotentEventId(NewTaskAssignmentDTO req) {
        // Must match the consumer’s buildEventId logic
        return String.join(
                "|",
                NotificationEventType.NEW_TASK_ASSIGN.name(),
                String.valueOf(req.getTaskId()),
                String.valueOf(req.getEventId()),
                String.valueOf(req.getAssigneeUserId()));
    }

    /** EMAIL */
    public static NotificationRequestDTO taskAssignmentToEmailNotification(
            NewTaskAssignmentDTO req) {
        Map<String, Object> vars =
                Map.of(
                        "taskId", req.getTaskId(),
                        "eventId", req.getEventId(),
                        "assigneeUserId", req.getAssigneeUserId(),
                        "assigneeEmail", req.getAssigneeEmail(),
                        "assignerName", req.getAssignerName(),
                        "taskName", req.getTaskName(),
                        "eventName", req.getEventName(),
                        "description", req.getDescription());

        return NotificationRequestDTO.builder()
                .channel(NotificationChannel.EMAIL)
                .to(req.getAssigneeEmail())
                .userId(req.getAssigneeUserId())
                .recipientKey("email:" + req.getAssigneeEmail())
                .templateId("new-task-assigned")
                .variables(vars)
                .locale(Locale.ENGLISH)
                .attachments(List.of())
                .eventId(idempotentEventId(req))
                .type(NotificationEventType.NEW_TASK_ASSIGN)
                .build();
    }

    /** PUSH */
    public static NotificationRequestDTO taskAssignmentToPushNotification(
            NewTaskAssignmentDTO req) {
        Map<String, Object> vars =
                Map.of(
                        "taskId", req.getTaskId(),
                        "eventId", req.getEventId(),
                        "assigneeUserId", req.getAssigneeUserId(),
                        "assignerName", req.getAssignerName(),
                        "taskName", req.getTaskName(),
                        "eventName", req.getEventName(),
                        "description", req.getDescription());

        return NotificationRequestDTO.builder()
                .channel(NotificationChannel.PUSH)
                .userId(req.getAssigneeUserId()) // devices resolved by userId on consumer side
                .templateId("new-task-assigned")
                .variables(vars)
                .locale(Locale.ENGLISH)
                .eventId(idempotentEventId(req)) // same idempotent ID as email
                .type(NotificationEventType.NEW_TASK_ASSIGN)
                .build();
    }

    public static NotificationRequestDTO taskAssignmentToWsNotification(NewTaskAssignmentDTO req) {
        Map<String, Object> vars =
                Map.of(
                        "taskId", req.getTaskId(),
                        "eventId", req.getEventId(),
                        "assigneeUserId", req.getAssigneeUserId(),
                        "assignerName", req.getAssignerName(),
                        "taskName", req.getTaskName(),
                        "eventName", req.getEventName(),
                        "description", req.getDescription(),
                        "deepLink",
                                String.format(
                                        "/events/%s/tasks/%s", req.getEventId(), req.getTaskId()));

        return NotificationRequestDTO.builder()
                .channel(NotificationChannel.WS)
                .userId(req.getAssigneeUserId())
                .templateId("new-task-assigned")
                .variables(vars)
                .locale(Locale.ENGLISH)
                .eventId(idempotentEventId(req))
                .type(NotificationEventType.NEW_TASK_ASSIGN)
                .build();
    }
}
