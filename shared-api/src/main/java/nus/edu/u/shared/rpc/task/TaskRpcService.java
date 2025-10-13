package nus.edu.u.shared.rpc.task;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface TaskRpcService {

    Map<Long, List<TaskDTO>> getTasksByEventIds(Collection<Long> eventIds);
}
