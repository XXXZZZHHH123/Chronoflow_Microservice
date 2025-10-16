package nus.edu.u.event.domain.dataobject.group;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nus.edu.u.event.domain.dataobject.event.EventDO;
import nus.edu.u.event.domain.dataobject.user.UserDO;
import nus.edu.u.framework.mybatis.base.TenantBaseDO;

/**
 * Department entity that represents an event group.
 *
 * <p>This mirrors the legacy monolith schema so that we can move behaviour across without schema
 * changes during the extraction.
 */
@TableName("sys_dept")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeptDO extends TenantBaseDO implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @TableId private Long id;

    private String name;

    private Integer sort;

    /** Related to {@link UserDO#getId()} */
    private Long leadUserId;

    private String remark;

    /** Department status, see {@link nus.edu.u.common.enums.CommonStatusEnum}. */
    private Integer status;

    /** Related to {@link EventDO#getId()} */
    private Long eventId;
}
