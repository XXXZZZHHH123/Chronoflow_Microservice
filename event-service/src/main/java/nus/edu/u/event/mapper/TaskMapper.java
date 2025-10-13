package nus.edu.u.event.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import nus.edu.u.event.domain.dataobject.task.TaskDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskMapper extends BaseMapper<TaskDO> {}
