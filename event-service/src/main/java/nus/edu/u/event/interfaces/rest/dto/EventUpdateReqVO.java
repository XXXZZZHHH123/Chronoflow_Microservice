package nus.edu.u.event.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class EventUpdateReqVO {
    private Long id;

    private String eventName;
    private String description;
    private String location;
    private Long organizerId;
    private List<Long> participantUserIds;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime endTime;

    private Integer status;
    private String remark;
}
