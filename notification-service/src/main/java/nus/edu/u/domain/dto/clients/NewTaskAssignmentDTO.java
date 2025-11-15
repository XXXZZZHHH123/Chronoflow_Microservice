package nus.edu.u.domain.dto.clients;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewTaskAssignmentDTO {
    private String taskId;
    private String eventId;
    private String assigneeUserId;
    private String assigneeEmail;
    private String assignerName;
    private String taskName;
    private String eventName;
    private String description;
}
