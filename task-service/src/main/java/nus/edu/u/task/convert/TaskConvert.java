package nus.edu.u.task.convert;

import nus.edu.u.task.domain.dataobject.task.TaskDO;
import nus.edu.u.task.domain.vo.task.TaskCreateReqVO;
import nus.edu.u.task.domain.vo.task.TaskRespVO;
import nus.edu.u.task.domain.vo.task.TasksRespVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TaskConvert {
    TaskConvert INSTANCE = Mappers.getMapper(TaskConvert.class);

    @Mapping(target = "userId", source = "targetUserId")
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "remark", ignore = true)
    TaskDO convert(TaskCreateReqVO bean);

    @Mapping(target = "assignerUser", ignore = true)
    @Mapping(target = "assignedUser", ignore = true)
    TaskRespVO toRespVO(TaskDO bean);

    @Mapping(target = "assignedUser", ignore = true)
    @Mapping(target = "event", ignore = true)
    TasksRespVO toTasksRespVO(TaskDO bean);
}
