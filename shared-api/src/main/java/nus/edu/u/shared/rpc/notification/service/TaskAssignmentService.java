package nus.edu.u.shared.rpc.notification.service;

import nus.edu.u.shared.rpc.notification.dto.task.NewTaskAssignmentDTO;

public interface TaskAssignmentService {
    String notifyNewTaskToAssigneePush(NewTaskAssignmentDTO dto);

    String notifyNewTaskToAssigneeEmail(NewTaskAssignmentDTO dto); // EMAIL

    String notifyNewTaskToAssigneeWs(NewTaskAssignmentDTO dto);
}
