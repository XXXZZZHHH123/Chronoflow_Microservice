package nus.edu.u.task.mapper.notification;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import nus.edu.u.shared.rpc.notification.dto.common.NotificationRequestDTO;
import nus.edu.u.shared.rpc.notification.dto.task.NewTaskAssignmentDTO;
import nus.edu.u.shared.rpc.notification.enums.common.NotificationChannel;
import nus.edu.u.shared.rpc.notification.enums.common.NotificationEventType;
import nus.edu.u.shared.rpc.notification.enums.email.EmailProvider;
import nus.edu.u.shared.rpc.notification.enums.push.PushProvider;
import nus.edu.u.shared.rpc.notification.enums.template.TemplateProvider;
import nus.edu.u.shared.rpc.notification.enums.ws.WSProvider;

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
                .emailProvider(EmailProvider.AWS_SES)
                .templateProvider(TemplateProvider.Thymeleaf)
                .to(req.getAssigneeEmail())
                .userId(req.getAssigneeUserId())
                .recipientKey("email:" + req.getAssigneeEmail())
                .templateId("task/new-task-assigned")
                .variables(vars)
                .locale(Locale.ENGLISH)
                .attachment(List.of())
                .eventId(idempotentEventId(req))
                .notificationEventType(NotificationEventType.NEW_TASK_ASSIGN)
                .build();
    }

    /** PUSH */
    public static NotificationRequestDTO taskAssignmentToPushNotification(
            NewTaskAssignmentDTO req) {
        Map<String, Object> vars =
                Map.of(
                        "taskId", req.getTaskId(),
                        "eventId", req.getEventId(),
                        "userId", req.getAssigneeUserId(),
                        "assigneeUserId", req.getAssigneeUserId(),
                        "assignerName", req.getAssignerName(),
                        "taskName", req.getTaskName(),
                        "eventName", req.getEventName(),
                        "description", req.getDescription(),
                        "templateProvider", TemplateProvider.Thymeleaf);

        return NotificationRequestDTO.builder()
                .channel(NotificationChannel.PUSH)
                .notificationEventType(NotificationEventType.NEW_TASK_ASSIGN)
                .pushProvider(PushProvider.FCM)
                .recipientKey("push" + req.getAssigneeEmail())
                .templateProvider(TemplateProvider.Thymeleaf)
                .userId(req.getAssigneeUserId()) // devices resolved by userId on consumer side
                .templateId("task/new-task-assigned-inline")
                .variables(vars)
                .locale(Locale.ENGLISH)
                .eventId(idempotentEventId(req)) // same idempotent ID as email
                .notificationEventType(NotificationEventType.NEW_TASK_ASSIGN)
                .build();
    }

    /** WS */
    public static NotificationRequestDTO taskAssignmentToWsNotification(NewTaskAssignmentDTO req) {
        Map<String, Object> vars =
                Map.of(
                        "taskId", req.getTaskId(),
                        "userId", req.getAssigneeUserId(),
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
                .userId(req.getAssigneeUserId())
                .channel(NotificationChannel.WS)
                .notificationEventType(NotificationEventType.NEW_TASK_ASSIGN)
                .wsProvider(WSProvider.FLUX)
                .recipientKey("ws" + req.getAssigneeUserId())
                .channel(NotificationChannel.WS)
                .userId(req.getAssigneeUserId())
                .templateProvider(TemplateProvider.Thymeleaf)
                .templateId("task/new-task-assigned-inline")
                .variables(vars)
                .locale(Locale.ENGLISH)
                .eventId(idempotentEventId(req))
                .notificationEventType(NotificationEventType.NEW_TASK_ASSIGN)
                .build();
    }
}
