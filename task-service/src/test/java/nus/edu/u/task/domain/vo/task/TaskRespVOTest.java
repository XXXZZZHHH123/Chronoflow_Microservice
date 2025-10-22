package nus.edu.u.task.domain.vo.task;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class TaskRespVOTest {

    @Test
    void nestedSetterGetter_roundtrip() {
        TaskRespVO.AssignerUserVO assigner = new TaskRespVO.AssignerUserVO();
        assigner.setId(1L);
        assigner.setName("A");

        TaskRespVO.AssignerUserVO.GroupVO g = new TaskRespVO.AssignerUserVO.GroupVO();
        g.setId(2L);
        g.setName("G");
        assigner.setGroups(List.of(g));

        assertEquals(1L, assigner.getId());
        assertEquals("A", assigner.getName());
        assertEquals(1, assigner.getGroups().size());
        assertEquals("G", assigner.getGroups().get(0).getName());
    }
}
