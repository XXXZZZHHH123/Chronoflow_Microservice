package nus.edu.u.file.domain.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serial;
import java.io.Serializable;
import lombok.*;
import nus.edu.u.framework.mybatis.base.TenantBaseDO;

/**
 * File data object for table file
 *
 * @author Lu Shuwen
 * @date 2025-08-28
 */
@TableName(value = "file")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileDO extends TenantBaseDO implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @TableId private Long id;

    /** Related to {@link TaskLogDO#getId()} */
    private Long taskLogId;

    /** Related to {@link EventDO#getId()} */
    private Long eventId;

    private String name;

    private String objectName;

    private String provider;

    private String type;

    private Long size;
}
