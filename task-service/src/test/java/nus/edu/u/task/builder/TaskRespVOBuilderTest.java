package nus.edu.u.task.builder;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import nus.edu.u.shared.rpc.events.EventRespDTO;
import nus.edu.u.task.domain.dataobject.task.TaskDO;
import nus.edu.u.task.domain.dataobject.user.UserDO;
import nus.edu.u.task.domain.vo.task.TaskRespVO;
import org.junit.jupiter.api.Test;

class TaskRespVOBuilderTest {

    @Test
    void build_usesSuppliersAndResolvers() {
        TaskDO task =
                TaskDO.builder()
                        .id(1L)
                        .eventId(9L)
                        .name("Plan")
                        .description("desc")
                        .status(1)
                        .remark("remark")
                        .startTime(LocalDateTime.now())
                        .endTime(LocalDateTime.now().plusHours(2))
                        .build();

        EventRespDTO event = new EventRespDTO();
        event.setId(9L);
        event.setName("Launch");

        UserDO assigner =
                UserDO.builder()
                        .id(20L)
                        .username("Alice")
                        .email("a@example.com")
                        .phone("111")
                        .build();
        UserDO assignee =
                UserDO.builder()
                        .id(30L)
                        .username("Bob")
                        .email("b@example.com")
                        .phone("222")
                        .build();

        TaskRespVOBuilder builder = TaskRespVOBuilder.from(task);
        builder.withEventMapper(null);
        builder.withAssignerSupplier(null);
        builder.withAssigneeGroupsResolver(null);

        TaskRespVO result =
                builder.withEventSupplier(() -> event)
                        .withEventMapper(
                                dto -> {
                                    TaskRespVO.EventVO eventVO = new TaskRespVO.EventVO();
                                    eventVO.setId(dto.getId());
                                    eventVO.setName(dto.getName().toUpperCase());
                                    return eventVO;
                                })
                        .withAssignerSupplier(() -> assigner)
                        .withAssignerGroupsResolver(
                                user -> {
                                    TaskRespVO.AssignerUserVO.GroupVO groupVO =
                                            new TaskRespVO.AssignerUserVO.GroupVO();
                                    groupVO.setId(100L);
                                    groupVO.setName("Core");
                                    return List.of(groupVO);
                                })
                        .withAssignee(assignee)
                        .withAssigneeSupplier(() -> UserDO.builder().id(99L).build())
                        .withAssigneeGroupsResolver(user -> null)
                        .build();

        assertThat(result.getEvent().getName()).isEqualTo("LAUNCH");
        assertThat(result.getAssignerUser().getId()).isEqualTo(assigner.getId());
        assertThat(result.getAssignerUser().getGroups())
                .extracting(TaskRespVO.AssignerUserVO.GroupVO::getName)
                .containsExactly("Core");
        assertThat(result.getAssignedUser().getId()).isEqualTo(assignee.getId());
        assertThat(result.getAssignedUser().getGroups()).isEmpty();
    }
}
