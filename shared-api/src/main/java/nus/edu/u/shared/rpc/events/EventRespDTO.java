package nus.edu.u.shared.rpc.events;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class EventRespDTO implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String description;
    private Long organizerId;
    private Integer joiningParticipants;
    private String location;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
            timezone = "UTC")
    private LocalDateTime startTime;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
            timezone = "UTC")
    private LocalDateTime endTime;

    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private List<GroupVO> groups;
    private TaskStatusVO taskStatus;

    @Data
    public static class GroupVO implements Serializable {

        @Serial private static final long serialVersionUID = 1L;

        private String id;
        private String name;
    }

    @Data
    public static class TaskStatusVO implements Serializable {

        @Serial private static final long serialVersionUID = 1L;

        private Integer total;
        private Integer remaining;
        private Integer completed;
    }
}
