package nus.edu.u.event.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import nus.edu.u.event.domain.dataobject.user.UserGroupDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserGroupMapper extends BaseMapper<UserGroupDO> {

    @Update(
            "UPDATE sys_user_group "
                    + "SET deleted = 0, update_time = NOW() "
                    + "WHERE event_id = #{eventId} AND deleted = 1")
    int restoreByEventId(@Param("eventId") Long eventId);
}
