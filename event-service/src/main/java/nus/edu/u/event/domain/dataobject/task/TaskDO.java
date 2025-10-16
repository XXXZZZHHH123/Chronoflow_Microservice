package nus.edu.u.event.domain.dataobject.task;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nus.edu.u.event.domain.dataobject.event.EventDO;
import nus.edu.u.event.domain.dataobject.user.UserDO;
import nus.edu.u.framework.mybatis.base.TenantBaseDO;

/**
 * Task persistence object.
 *
 * <p>We keep the schema aligned with the legacy monolith for a smoother migration.
 */
@TableName("task")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskDO extends TenantBaseDO implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @TableId private Long id;

    /** Related to {@link UserDO#getId()} */
    private Long userId;

    /** Related to {@link EventDO#getId()} */
    private Long eventId;

    private String name;

    private String description;

    /** Related to {@link nus.edu.u.event.enums.TaskStatusEnum} */
    private Integer status;

    private String remark;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
