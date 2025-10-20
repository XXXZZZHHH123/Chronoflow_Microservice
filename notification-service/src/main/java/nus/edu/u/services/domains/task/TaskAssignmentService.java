package nus.edu.u.services.domains.task;

import nus.edu.u.domain.dto.common.NewTaskAssignmentDTO;

import java.util.Map;

public interface TaskAssignmentService {
    public String notifyNewTaskToAssigneePush(NewTaskAssignmentDTO dto);
    String notifyNewTaskToAssigneeEmail(NewTaskAssignmentDTO dto);    // EMAIL
    Map<String, String> notifyNewTaskAllChannels(NewTaskAssignmentDTO dto); // Both
}
