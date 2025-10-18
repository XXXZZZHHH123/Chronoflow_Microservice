package nus.edu.u.task.builder;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import nus.edu.u.shared.rpc.events.EventRespDTO;
import nus.edu.u.task.domain.dataobject.task.TaskDO;
import nus.edu.u.task.domain.dataobject.user.UserDO;
import nus.edu.u.task.domain.vo.task.TasksRespVO;
import org.junit.jupiter.api.Test;

class TasksRespVOBuilderTest {

    @Test
    void build_populatesEventSummaryAndAssignedUser() {
        TaskDO task =
                TaskDO.builder()
                        .id(99L)
                        .eventId(1L)
                        .name("Compile report")
                        .startTime(LocalDateTime.now())
                        .endTime(LocalDateTime.now().plusHours(1))
                        .build();

        EventRespDTO event = new EventRespDTO();
        event.setId(1L);
        event.setName("Townhall");

        UserDO assignee =
                UserDO.builder()
                        .id(88L)
                        .username("Charlie")
                        .email("charlie@example.com")
                        .phone("333")
                        .build();

        TasksRespVOBuilder builder = TasksRespVOBuilder.from(task);
        builder.withEventSupplier(null);
        builder.withGroupResolver(null);

        TasksRespVO response =
                builder.withEvent(event)
                        .withEventMapper(
                                dto -> {
                                    TasksRespVO.EventVO vo = new TasksRespVO.EventVO();
                                    vo.setId(dto.getId());
                                    vo.setName(dto.getName().toLowerCase());
                                    return vo;
                                })
                        .withAssigneeSupplier(() -> assignee)
                        .withGroupResolver(
                                user -> {
                                    TasksRespVO.AssignedUserVO.GroupVO group =
                                            new TasksRespVO.AssignedUserVO.GroupVO();
                                    group.setId(501L);
                                    group.setName("Support");
                                    return List.of(group);
                                })
                        .build();

        assertThat(response.getEvent().getName()).isEqualTo("townhall");
        assertThat(response.getAssignedUser().getId()).isEqualTo(assignee.getId());
        assertThat(response.getAssignedUser().getGroups())
                .extracting(TasksRespVO.AssignedUserVO.GroupVO::getName)
                .containsExactly("Support");
    }

    @Test
    void build_whenNoAssigneeSupplier_producesNullAssignedUser() {
        TaskDO task = TaskDO.builder().id(1L).eventId(2L).name("Solo task").build();

        TasksRespVO response = TasksRespVOBuilder.from(task).build();

        assertThat(response.getAssignedUser()).isNull();
    }
}
