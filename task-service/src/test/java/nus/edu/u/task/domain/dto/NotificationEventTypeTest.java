package nus.edu.u.task.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NotificationEventTypeTest {

    @Test
    void buildEventId_combinesParts() {
        String id =
                NotificationEventType.buildEventId(
                        NotificationEventType.NEW_TASK_ASSIGN, 123, "abc");
        assertEquals("new-task-assigned:123:abc", id);
    }

    @Test
    void toString_returnsCode() {
        assertEquals("organizer-welcome", NotificationEventType.ORGANIZER_WELCOME.toString());
    }
}
