package nus.edu.u.task.rpc;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.common.exception.ServiceException;
import nus.edu.u.shared.rpc.task.TaskDTO;
import nus.edu.u.shared.rpc.task.TaskRpcService;
import nus.edu.u.task.convert.TaskRpcConvert;
import nus.edu.u.task.domain.vo.task.TaskRespVO;
import nus.edu.u.task.enums.TaskStatusEnum;
import nus.edu.u.task.service.TaskApplicationService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * RPC implementation exposing task data to other bounded contexts.
 *
 * <p>Provides lookup utilities for tasks grouped by events as well as pending-task checks that are
 * required by the event service to validate group membership operations.
 */
@DubboService
@Slf4j
@RequiredArgsConstructor
public class TaskRpcServiceImpl implements TaskRpcService {

    private final TaskApplicationService taskApplicationService;
    private final TaskRpcConvert taskRpcConvert;

    @Override
    public Map<Long, List<TaskDTO>> getTasksByEventIds(Collection<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<TaskDTO>> result = new LinkedHashMap<>();
        for (Long eventId : eventIds) {
            if (eventId == null) {
                continue;
            }
            try {
                List<TaskRespVO> tasks = taskApplicationService.listTasksByEvent(eventId);
                if (tasks == null || tasks.isEmpty()) {
                    continue;
                }
                List<TaskDTO> dtoList = safeDtoList(tasks);
                if (!dtoList.isEmpty()) {
                    result.put(eventId, dtoList);
                }
            } catch (ServiceException ex) {
                log.debug("Unable to fetch tasks for event {}: {}", eventId, ex.getMessage());
            }
        }
        return result;
    }

    @Override
    public boolean hasPendingTasks(Long eventId, Long userId) {
        if (eventId == null || userId == null) {
            return false;
        }

        try {
            return taskApplicationService.listTasksByMember(userId).stream()
                    .filter(Objects::nonNull)
                    .filter(task -> Objects.equals(eventId, task.getEventId()))
                    .anyMatch(
                            task ->
                                    !Objects.equals(
                                            task.getStatus(), TaskStatusEnum.COMPLETED.getStatus()));
        } catch (ServiceException ex) {
            log.debug(
                    "Unable to evaluate pending tasks for event {} and user {}: {}",
                    eventId,
                    userId,
                    ex.getMessage());
            return false;
        }
    }

    @Override
    public void deleteTasksByEventId(Long eventId) {
        if (eventId == null) {
            return;
        }

        try {
            List<TaskRespVO> tasks = taskApplicationService.listTasksByEvent(eventId);
            if (tasks == null || tasks.isEmpty()) {
                log.debug("No tasks to delete for event {}", eventId);
                return;
            }
            for (TaskRespVO task : tasks) {
                if (task == null || task.getId() == null) {
                    continue;
                }
                try {
                    taskApplicationService.deleteTask(eventId, task.getId());
                } catch (ServiceException ex) {
                    log.warn(
                            "Failed to delete task {} for event {}: {}",
                            task.getId(),
                            eventId,
                            ex.getMessage());
                }
            }
        } catch (ServiceException ex) {
            log.debug("Unable to delete tasks for event {}: {}", eventId, ex.getMessage());
        }
    }

    private List<TaskDTO> safeDtoList(List<TaskRespVO> tasks) {
        List<TaskDTO> dtoList = taskRpcConvert.toDtoList(tasks);
        if (dtoList == null) {
            return List.of();
        }
        return dtoList.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }
}
