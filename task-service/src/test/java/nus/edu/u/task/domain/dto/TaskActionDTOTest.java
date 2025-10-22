package nus.edu.u.task.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class TaskActionDTOTest {

    @Test
    void builder_setsFields() {
        LocalDateTime now = LocalDateTime.now();
        TaskActionDTO dto =
                TaskActionDTO.builder()
                        .name("T")
                        .description("D")
                        .startTime(now)
                        .endTime(now.plusHours(1))
                        .targetUserId(5L)
                        .remark("r")
                        .build();

        assertEquals("T", dto.getName());
        assertEquals("D", dto.getDescription());
        assertEquals(5L, dto.getTargetUserId());
        assertEquals("r", dto.getRemark());
    }
}
