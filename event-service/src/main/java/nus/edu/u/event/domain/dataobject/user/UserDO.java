package nus.edu.u.event.domain.dataobject.user;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nus.edu.u.common.enums.CommonStatusEnum;
import nus.edu.u.framework.mybatis.base.TenantBaseDO;

@TableName(value = "sys_user", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDO extends TenantBaseDO implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @TableId private Long id;

    private String username;

    private String password;

    private String remark;

    private String email;

    private String phone;

    /** Enum {@link CommonStatusEnum} */
    private Integer status;

    private LocalDateTime loginTime;

    @Deprecated private Long deptId;

    @TableField(typeHandler = JacksonTypeHandler.class, value = "post_list")
    private List<Integer> postList;
}
