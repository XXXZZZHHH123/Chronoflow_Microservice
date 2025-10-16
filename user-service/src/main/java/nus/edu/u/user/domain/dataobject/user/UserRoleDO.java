package nus.edu.u.user.domain.dataobject.user;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serial;
import java.io.Serializable;
import lombok.*;
import nus.edu.u.framework.mybatis.base.TenantBaseDO;

/**
 * @author Lu Shuwen
 * @date 2025-09-10
 */
@TableName(value = "sys_user_role")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleDO extends TenantBaseDO implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @TableId private Long id;

    /** Related to {@link UserDO#getId()} */
    private Long userId;

    private Long roleId;
}
