package nus.edu.u.task.rpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nus.edu.u.shared.rpc.task.TaskDTO;
import nus.edu.u.task.domain.dataobject.task.TaskDO;
import nus.edu.u.task.mapper.TaskLogMapper;
import nus.edu.u.task.mapper.TaskMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskRpcServiceImplTest {

    @Mock private TaskMapper taskMapper;
    @Mock private TaskLogMapper taskLogMapper;

    @InjectMocks private TaskRpcServiceImpl rpcService;

    @Test
    void getTasksByEventIds_whenNullIds_returnsEmpty() {
        Map<Long, List<TaskDTO>> result = rpcService.getTasksByEventIds(null);

        assertThat(result).isEmpty();
        verifyNoInteractions(taskMapper, taskLogMapper);
    }

    @Test
    void getTasksByEventIds_whenMapperReturnsNull_returnsEmpty() {
        when(taskMapper.selectList(any())).thenReturn(null);

        Map<Long, List<TaskDTO>> result = rpcService.getTasksByEventIds(List.of(1L));

        assertThat(result).isEmpty();
        verify(taskMapper).selectList(any());
        verifyNoInteractions(taskLogMapper);
    }

    @Test
    void getTasksByEventIds_groupsTasksByEventAndFiltersInvalidEntries() {
        TaskDO taskOne = task(1L, 10L, 1);
        TaskDO taskTwo = task(2L, 10L, 2);
        TaskDO otherEvent = task(3L, 20L, 3);
        TaskDO withoutEvent = task(4L, null, 4);

        when(taskMapper.selectList(any()))
                .thenReturn(Arrays.asList(taskOne, null, withoutEvent, taskTwo, otherEvent));

        Map<Long, List<TaskDTO>> result = rpcService.getTasksByEventIds(Set.of(10L, 20L));

        assertThat(result.keySet()).containsExactlyInAnyOrder(10L, 20L);
        assertThat(result.get(10L)).extracting(TaskDTO::getId).containsExactly(1L, 2L);
        assertThat(result.get(10L)).extracting(TaskDTO::getStatus).containsExactly(1, 2);
        assertThat(result.get(20L)).extracting(TaskDTO::getId).containsExactly(3L);
        verify(taskMapper).selectList(any());
    }

    @Test
    void hasPendingTasks_whenEventOrUserMissing_returnsFalse() {
        assertThat(rpcService.hasPendingTasks(null, 2L)).isFalse();
        assertThat(rpcService.hasPendingTasks(10L, null)).isFalse();
        verifyNoInteractions(taskMapper);
    }

    @Test
    void hasPendingTasks_whenMapperReturnsNull_returnsFalse() {
        when(taskMapper.selectCount(any())).thenReturn(null);

        assertThat(rpcService.hasPendingTasks(10L, 2L)).isFalse();
        verify(taskMapper).selectCount(any());
    }

    @Test
    void hasPendingTasks_whenCountZero_returnsFalse() {
        when(taskMapper.selectCount(any())).thenReturn(0L);

        assertThat(rpcService.hasPendingTasks(10L, 2L)).isFalse();
    }

    @Test
    void hasPendingTasks_whenPendingExists_returnsTrue() {
        when(taskMapper.selectCount(any())).thenReturn(5L);

        assertThat(rpcService.hasPendingTasks(10L, 2L)).isTrue();
    }

    @Test
    void deleteTasksByEventId_whenEventIdNull_skipsAllWork() {
        rpcService.deleteTasksByEventId(null);

        verifyNoInteractions(taskMapper, taskLogMapper);
    }

    @Test
    void deleteTasksByEventId_whenNoTasksFound_exitsEarly() {
        when(taskMapper.selectList(any())).thenReturn(List.of());

        rpcService.deleteTasksByEventId(88L);

        verify(taskMapper).selectList(any());
        verify(taskMapper, never()).delete(any());
        verifyNoInteractions(taskLogMapper);
    }

    @Test
    void deleteTasksByEventId_whenTasksHaveNoIds_skipsLogDeletion() {
        TaskDO withoutId = task(null, 77L, 1);
        when(taskMapper.selectList(any())).thenReturn(List.of(withoutId));
        when(taskMapper.delete(any())).thenReturn(1);

        rpcService.deleteTasksByEventId(77L);

        verify(taskLogMapper, never()).delete(any());
        verify(taskMapper).delete(any());
    }

    @Test
    void deleteTasksByEventId_removesLogsAndTasksForEvent() {
        TaskDO first = task(1L, 99L, 1);
        TaskDO second = task(2L, 99L, 2);
        when(taskMapper.selectList(any())).thenReturn(List.of(first, second));
        when(taskLogMapper.delete(any())).thenReturn(2);
        when(taskMapper.delete(any())).thenReturn(2);

        rpcService.deleteTasksByEventId(99L);

        verify(taskLogMapper).delete(any());
        verify(taskMapper).delete(any());
    }

    private TaskDO task(Long id, Long eventId, Integer status) {
        return TaskDO.builder().id(id).eventId(eventId).status(status).build();
    }
}
