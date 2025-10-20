package nus.edu.u.domain.dto.common;

import lombok.*;

/**
 * Represents a domain-level "Task Assignment" event.
 *
 * <p>This is not tied to any specific notification channel — it's the pure business payload passed
 * from the Task domain to the Notification module.
 */
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
