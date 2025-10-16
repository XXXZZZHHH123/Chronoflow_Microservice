package nus.edu.u.task.domain.dataobject.task;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.*;
import nus.edu.u.framework.mybatis.base.TenantBaseDO;

@TableName(value = "task_log")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskLogDO extends TenantBaseDO implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @TableId private Long id;

    /** Related to {@link TaskDO#getId()} */
    private Long taskId;

    /** Related to {@link TaskActionEnum} */
    private Integer action;

    private Long targetUserId;

    private BigDecimal moneyCost;

    private BigDecimal laborHour;

    private String remark;
}
