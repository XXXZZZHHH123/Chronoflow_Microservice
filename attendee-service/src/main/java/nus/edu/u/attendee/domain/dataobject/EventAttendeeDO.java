package nus.edu.u.attendee.domain.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.*;
import nus.edu.u.framework.mybatis.base.TenantBaseDO;

/**
 * Event Attendee data object for table event_attendee
 *
 * @author Fan yazhuoting
 * @date 2025-10-02
 */
@TableName("event_attendee")
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class EventAttendeeDO extends TenantBaseDO implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long eventId;

    private String attendeeEmail;
    private String attendeeName;
    private String attendeeMobile;

    private String checkInToken;
    private Integer checkInStatus;
    private LocalDateTime checkInTime;
    private LocalDateTime qrCodeGeneratedTime;
}
