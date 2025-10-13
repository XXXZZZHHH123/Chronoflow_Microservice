package nus.edu.u.event.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import nus.edu.u.event.domain.dataobject.user.UserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<UserDO> {

    @InterceptorIgnore(tenantLine = "true")
    UserDO selectByIdWithoutTenant(@Param("id") Long id);
}
