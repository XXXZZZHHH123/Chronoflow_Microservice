package nus.edu.u.domain.dto.common;

import lombok.Builder;
import lombok.Value;

/**
 * Represents a domain-level "Task Assignment" event.
 *
 * This is not tied to any specific notification channel — it's the pure business payload
 * passed from the Task domain to the Notification module.
 */
@Value
@Builder

public class NewTaskAssignmentDTO {

    /**
     * Unique task identifier in the system (used for idempotency/event keys).
     */
    String taskId;

    String eventId;

    /**
     * ID of the user who is being assigned this task (the assignee).
     */
    String assigneeUserId;

    String assigneeEmail;

    /**
     * Display name of the person assigning the task.
     */
    String assignerName;

    /**
     * Human-friendly task name or title.
     */
    String taskName;

    /**
     * Optional name of the parent event, project, or campaign this task belongs to.
     */
    String eventName;

    /**
     * Optional description or remarks — can be null.
     * You may use it later for richer templates or audit logs.
     */
    String description;
}
