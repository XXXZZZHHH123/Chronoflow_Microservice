package nus.edu.u.event.rpc.mock;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nus.edu.u.shared.rpc.task.TaskDTO;
import nus.edu.u.shared.rpc.task.TaskRpcService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class MockTaskRpcService implements TaskRpcService {

    @Override
    public Map<Long, List<TaskDTO>> getTasksByEventIds(Collection<Long> eventIds) {
        Map<Long, List<TaskDTO>> result = new HashMap<>();
        if (eventIds == null) {
            return result;
        }
        for (Long eventId : eventIds) {
            if (eventId == null) {
                continue;
            }
            result.put(
                    eventId,
                    List.of(
                            TaskDTO.builder().id(9001L).eventId(eventId).status(2).build(),
                            TaskDTO.builder().id(9002L).eventId(eventId).status(1).build()));
        }
        return result;
    }

    @Override
    public boolean hasPendingTasks(Long eventId, Long userId) {
        return false;
    }
}
