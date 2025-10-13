package nus.edu.u.event.domain.dataobject.event;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
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
import nus.edu.u.framework.mybatis.base.TenantBaseDO;

/**
 * Persistent representation of an event.
 *
 * <p>Initially mirrors the monolith schema so that we can gradually extract behaviour into the
 * dedicated microservice.
 */
@TableName("event")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class EventDO extends TenantBaseDO implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @TableId private Long id;

    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long userId;

    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String name;

    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String description;

    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime startTime;

    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String location;

    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime endTime;

    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Integer status;

    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String remark;
}
