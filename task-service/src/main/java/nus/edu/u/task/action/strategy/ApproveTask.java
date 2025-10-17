package nus.edu.u.task.action.strategy;

import static nus.edu.u.common.constant.PermissionConstants.UPDATE_TASK;
import static nus.edu.u.common.enums.ErrorCodeConstants.*;
import static nus.edu.u.common.utils.exception.ServiceExceptionUtil.exception;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import nus.edu.u.task.action.AbstractTaskStrategy;
import nus.edu.u.task.domain.dataobject.task.TaskDO;
import nus.edu.u.task.domain.dto.TaskActionDTO;
import nus.edu.u.task.enums.TaskActionEnum;
import nus.edu.u.task.enums.TaskStatusEnum;
import org.springframework.stereotype.Component;

/**
 * @author Lu Shuwen
 * @date 2025-10-03
 */
@Component
public class ApproveTask extends AbstractTaskStrategy {

    @Override
    public TaskActionEnum getType() {
        return TaskActionEnum.APPROVE;
    }

    @Override
    public void execute(TaskDO task, TaskActionDTO actionDTO, Object... params) {
        StpUtil.checkPermission(UPDATE_TASK);
        validateTimeRange(
                task,
                task.getStartTime(),
                task.getEndTime(),
                actionDTO.getEventStartTime(),
                actionDTO.getEventEndTime());
        if (!ObjectUtil.equals(task.getCreator(), StpUtil.getLoginId().toString())) {
            throw exception(MODIFY_OTHER_TASK_ERROR);
        }
        if (!ObjectUtil.equals(task.getStatus(), TaskStatusEnum.PENDING_APPROVAL.getStatus())) {
            throw exception(
                    MODIFY_WRONG_TASK_STATUS,
                    getType().getAction(),
                    TaskStatusEnum.getEnum(task.getStatus()));
        }
        task.setStatus(TaskStatusEnum.COMPLETED.getStatus());
        boolean isSuccess = taskMapper.updateById(task) > 0;
        if (!isSuccess) {
            throw exception(APPROVE_TASK_FAILED);
        }
        taskLogService.insertTaskLog(
                task.getId(), null, getType().getCode(), actionDTO.getRemark());
    }
}
