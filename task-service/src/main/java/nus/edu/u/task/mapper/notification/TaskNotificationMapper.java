package nus.edu.u.task.mapper.notification;

import nus.edu.u.shared.rpc.notification.dto.common.AttachmentDTO;
import nus.edu.u.shared.rpc.notification.dto.common.NotificationRequestDTO;
import nus.edu.u.shared.rpc.notification.dto.task.NewTaskAssignmentDTO;
import nus.edu.u.shared.rpc.notification.enums.NotificationChannel;
import nus.edu.u.shared.rpc.notification.enums.NotificationEventType;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskNotificationMapper {

    public static NotificationRequestDTO taskAssignmentToNotification(NewTaskAssignmentDTO req) {

        Map<String, Object> vars = Map.of(
                "taskId",        req.getTaskId(),
                "eventId",       req.getEventId(),
                "assigneeUserId",req.getAssigneeUserId(),
                "assigneeEmail", req.getAssigneeEmail(),
                "assignerName",  req.getAssignerName(),
                "taskName",      req.getTaskName(),
                "eventName",     req.getEventName(),
                "description",   req.getDescription()
        );

        List<AttachmentDTO> attachments = List.of();

        return NotificationRequestDTO.builder()
                .channel(NotificationChannel.EMAIL)
                .to(req.getAssigneeEmail())
                .userId(req.getAssigneeUserId())
                .recipientKey("email:" + req.getAssigneeEmail())
                .templateId("new-task-assigned")
                .variables(vars)
                .locale(Locale.ENGLISH)
                .attachments(attachments)
                .eventId("task-assignment-" + req.getEventId() + "-" + req.getTaskId())
                .type(NotificationEventType.NEW_TASK_ASSIGN)
                .build();
    }
}
