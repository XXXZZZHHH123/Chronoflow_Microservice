package nus.edu.u.task.publisher;

import lombok.RequiredArgsConstructor;
import nus.edu.u.shared.rpc.notification.dto.task.NewTaskAssignmentDTO;
import nus.edu.u.shared.rpc.notification.service.TaskAssignmentService;
import nus.edu.u.task.mapper.notification.TaskNotificationMapper;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TaskNotificationPublisher implements TaskAssignmentService {

    private final NotificationPublisher notificationPublisher;


    @Override
    public String notifyNewTaskToAssigneePush(NewTaskAssignmentDTO dto) {
        return "";
    }

    @Override
    public String notifyNewTaskToAssigneeEmail(NewTaskAssignmentDTO dto) {
        return notificationPublisher.publish(TaskNotificationMapper.taskAssignmentToNotification(dto));
    }

    @Override
    public Map<String, String> notifyNewTaskAllChannels(NewTaskAssignmentDTO dto) {
        return Map.of();
    }
}
