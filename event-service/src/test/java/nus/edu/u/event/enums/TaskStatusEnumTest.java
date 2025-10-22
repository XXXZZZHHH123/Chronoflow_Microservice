package nus.edu.u.event.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class TaskStatusEnumTest {

    @Test
    void fromStatus_null_returnsNull() {
        assertNull(TaskStatusEnum.fromStatus(null));
    }

    @Test
    void fromStatus_valid_returnsEnum() {
        TaskStatusEnum s = TaskStatusEnum.fromStatus(2);
        assertEquals(TaskStatusEnum.COMPLETED, s);
        assertEquals(2, s.getStatus());
        assertEquals("Completed", s.getName());
    }

    @Test
    void fromStatusOrDefault_unknown_returnsPending() {
        TaskStatusEnum s = TaskStatusEnum.fromStatusOrDefault(999);
        assertEquals(TaskStatusEnum.PENDING, s);
    }
}
