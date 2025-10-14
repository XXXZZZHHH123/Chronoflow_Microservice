package nus.edu.u.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import nus.edu.u.task.domain.dataobject.task.TaskLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskLogMapper extends BaseMapper<TaskLogDO> {}
