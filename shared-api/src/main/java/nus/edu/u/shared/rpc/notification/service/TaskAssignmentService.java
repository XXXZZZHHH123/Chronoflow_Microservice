package nus.edu.u.shared.rpc.notification.service;

import nus.edu.u.shared.rpc.notification.dto.task.NewTaskAssignmentDTO;

import java.util.Map;

public interface TaskAssignmentService {
    public String notifyNewTaskToAssigneePush(NewTaskAssignmentDTO dto);
    String notifyNewTaskToAssigneeEmail(NewTaskAssignmentDTO dto);    // EMAIL
    Map<String, String> notifyNewTaskAllChannels(NewTaskAssignmentDTO dto); // Both
}
