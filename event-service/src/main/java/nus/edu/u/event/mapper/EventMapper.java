package nus.edu.u.event.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import nus.edu.u.event.domain.dataobject.event.EventDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface EventMapper extends BaseMapper<EventDO> {

    @Select("SELECT * FROM event WHERE id = #{id}")
    EventDO selectRawById(@Param("id") Long id);

    @Update(
            "UPDATE event "
                    + "SET deleted = 0, update_time = NOW() "
                    + "WHERE id = #{id} AND deleted = 1")
    int restoreById(@Param("id") Long id);
}
