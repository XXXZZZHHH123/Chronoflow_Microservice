package nus.edu.u.event.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import nus.edu.u.event.domain.dataobject.group.DeptDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeptMapper extends BaseMapper<DeptDO> {}
