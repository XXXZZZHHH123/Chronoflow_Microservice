package nus.edu.u.task.domain.vo.taskLog;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import nus.edu.u.shared.rpc.file.FileResultDTO;
import nus.edu.u.task.domain.vo.user.UserVO;

/**
 * @author Lu Shuwen
 * @date 2025-10-03
 */
@Data
@Builder
public class TaskLogRespVO {

    private Long id;

    private Integer action;

    private UserVO targetUser;

    private UserVO sourceUser;

    private LocalDateTime createTime;

    private List<FileResultDTO> fileResults;

    private String remark;
}
