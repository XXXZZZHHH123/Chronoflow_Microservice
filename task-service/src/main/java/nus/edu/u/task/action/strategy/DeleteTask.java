package nus.edu.u.task.action.strategy;

import static nus.edu.u.common.enums.ErrorCodeConstants.TASK_DELETE_FAILED;
import static nus.edu.u.common.utils.exception.ServiceExceptionUtil.exception;

import nus.edu.u.task.action.AbstractTaskStrategy;
import nus.edu.u.task.domain.dataobject.task.TaskDO;
import nus.edu.u.task.domain.dto.TaskActionDTO;
import nus.edu.u.task.enums.TaskActionEnum;
import org.springframework.stereotype.Component;

/**
 * @author Lu Shuwen
 * @date 2025-10-02
 */
@Component
public class DeleteTask extends AbstractTaskStrategy {

    @Override
    public TaskActionEnum getType() {
        return TaskActionEnum.DELETE;
    }

    @Override
    public void execute(TaskDO task, TaskActionDTO actionDTO, Object... params) {
        boolean isSuccess = taskMapper.deleteById(task.getId()) > 0;
        if (!isSuccess) {
            throw exception(TASK_DELETE_FAILED);
        }
        taskLogService.insertTaskLog(task.getId(), null, getType().getCode(), null);
    }
}
