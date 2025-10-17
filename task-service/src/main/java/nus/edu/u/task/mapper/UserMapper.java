package nus.edu.u.task.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import nus.edu.u.shared.rpc.user.UserProfileDTO;
import nus.edu.u.task.domain.dataobject.user.UserDO;
import nus.edu.u.task.domain.vo.user.UserProfileRespVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<UserDO> {

    @InterceptorIgnore(tenantLine = "true")
    UserDO selectByIdWithoutTenant(@Param("id") Long id);

    UserProfileRespVO toVo(UserProfileDTO dto);

    List<UserProfileRespVO> toVoList(List<UserProfileDTO> list);
}
