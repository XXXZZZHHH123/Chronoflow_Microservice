package nus.edu.u.event.domain.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class EventUpdateReqVO {
    private Long id;

    @JsonProperty("name")
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
