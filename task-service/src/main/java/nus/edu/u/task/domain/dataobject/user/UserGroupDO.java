package nus.edu.u.task.domain.dataobject.user;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nus.edu.u.framework.mybatis.base.TenantBaseDO;

@TableName(value = "sys_user_group")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserGroupDO extends TenantBaseDO implements Serializable {

    @TableId private Long id;

    private Long userId;

    private Long deptId;

    private Long eventId;

    private LocalDateTime joinTime;

    private Integer roleType;
}
