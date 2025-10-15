package nus.edu.u.task.service;

import java.util.List;
import nus.edu.u.task.domain.vo.taskLog.TaskLogRespVO;

/**
 * @author Lu Shuwen
 * @date 2025-10-02
 */
public interface TaskLogApplicationService {

    Long insertTaskLog(Long taskId, Long targetUserId, Integer action, String remark);

    List<TaskLogRespVO> getTaskLog(Long taskId);
}
