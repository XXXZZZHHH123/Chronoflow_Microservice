package nus.edu.u.event.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import nus.edu.u.event.domain.dataobject.EventParticipantDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EventParticipantMapper extends BaseMapper<EventParticipantDO> {}
