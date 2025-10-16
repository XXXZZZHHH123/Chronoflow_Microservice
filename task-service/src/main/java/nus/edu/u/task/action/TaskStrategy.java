package nus.edu.u.task.action;

import nus.edu.u.task.domain.dataobject.task.TaskDO;
import nus.edu.u.task.domain.dto.TaskActionDTO;
import nus.edu.u.task.enums.TaskActionEnum;

/**
 * task action strategy class
 *
 * @author Lu Shuwen
 * @date 2025-10-02
 */
public interface TaskStrategy {

    void execute(TaskDO task, TaskActionDTO taskActionDTO, Object... params);

    TaskActionEnum getType();
}
