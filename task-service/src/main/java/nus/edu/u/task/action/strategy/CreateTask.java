package nus.edu.u.task.action.strategy;

import static nus.edu.u.common.enums.ErrorCodeConstants.TASK_CREATE_FAILED;
import static nus.edu.u.common.utils.exception.ServiceExceptionUtil.exception;

import nus.edu.u.task.action.AbstractTaskStrategy;
import nus.edu.u.task.domain.dataobject.task.TaskDO;
import nus.edu.u.task.domain.dto.TaskActionDTO;
import nus.edu.u.task.enums.TaskActionEnum;
import nus.edu.u.task.enums.TaskStatusEnum;
import org.springframework.stereotype.Component;

/**
 * @author Lu Shuwen
 * @date 2025-10-02
 */
@Component
public class CreateTask extends AbstractTaskStrategy {

    @Override
    public TaskActionEnum getType() {
        return TaskActionEnum.CREATE;
    }

    @Override
    public void execute(TaskDO task, TaskActionDTO actionDTO, Object... params) {
        validateTimeRange(
                task,
                task.getStartTime(),
                task.getEndTime(),
                actionDTO.getEventStartTime(),
                actionDTO.getEventEndTime());
        task.setStatus(TaskStatusEnum.PENDING.getStatus());
        task.setUserId(actionDTO.getTargetUserId());
        boolean isSuccess = taskMapper.insert(task) > 0;
        if (!isSuccess) {
            throw exception(TASK_CREATE_FAILED);
        }
        Long taskLogId =
                taskLogService.insertTaskLog(
                        task.getId(),
                        actionDTO.getTargetUserId(),
                        getType().getCode(),
                        actionDTO.getRemark());
        uploadFiles(taskLogId, task.getEventId(), actionDTO.getFiles());
    }
}
