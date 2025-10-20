package nus.edu.u.task.publisher;

import lombok.RequiredArgsConstructor;
import nus.edu.u.shared.rpc.notification.dto.task.NewTaskAssignmentDTO;
import nus.edu.u.shared.rpc.notification.service.TaskAssignmentService;
import nus.edu.u.task.mapper.notification.TaskNotificationMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskNotificationPublisher implements TaskAssignmentService {

    private final NotificationPublisher notificationPublisher;

    @Override
    public String notifyNewTaskToAssigneePush(NewTaskAssignmentDTO dto) {
        return notificationPublisher.publish(
                TaskNotificationMapper.taskAssignmentToPushNotification(dto));
    }

    @Override
    public String notifyNewTaskToAssigneeEmail(NewTaskAssignmentDTO dto) {
        return notificationPublisher.publish(
                TaskNotificationMapper.taskAssignmentToEmailNotification(dto));
    }

    @Override
    public String notifyNewTaskToAssigneeWs(NewTaskAssignmentDTO dto) {
        return notificationPublisher.publish(
                TaskNotificationMapper.taskAssignmentToWsNotification(dto));
    }
}
