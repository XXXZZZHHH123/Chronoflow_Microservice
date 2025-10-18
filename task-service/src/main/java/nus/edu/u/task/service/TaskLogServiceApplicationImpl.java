package nus.edu.u.task.service;

import static nus.edu.u.common.enums.ErrorCodeConstants.TASK_LOG_ERROR;
import static nus.edu.u.common.utils.exception.ServiceExceptionUtil.exception;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.shared.rpc.file.FileResultDTO;
import nus.edu.u.shared.rpc.file.FileStorageRpcService;
import nus.edu.u.shared.rpc.user.UserInfoDTO;
import nus.edu.u.shared.rpc.user.UserRpcService;
import nus.edu.u.task.domain.dataobject.task.TaskLogDO;
import nus.edu.u.task.domain.vo.taskLog.TaskLogRespVO;
import nus.edu.u.task.domain.vo.user.UserVO;
import nus.edu.u.task.mapper.TaskLogMapper;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lu Shuwen
 * @date 2025-10-02
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskLogServiceApplicationImpl implements TaskLogApplicationService {

    private final TaskLogMapper taskLogMapper;

    @DubboReference(check = false)
    private final UserRpcService userRpcService;

    @DubboReference(check = false)
    private final FileStorageRpcService fileStorageRpcService;

    @Override
    @Transactional
    public Long insertTaskLog(Long taskId, Long targetUserId, Integer action, String remark) {
        TaskLogDO taskLogDO =
                TaskLogDO.builder()
                        .taskId(taskId)
                        .targetUserId(targetUserId)
                        .action(action)
                        .remark(remark)
                        .build();
        boolean isSuccess = taskLogMapper.insert(taskLogDO) > 0;
        if (!isSuccess) {
            throw exception(TASK_LOG_ERROR);
        }
        return taskLogDO.getId();
    }

    @Override
    public List<TaskLogRespVO> getTaskLog(Long taskId) {
        List<TaskLogDO> taskLogList =
                taskLogMapper.selectList(
                        new LambdaQueryWrapper<TaskLogDO>().eq(TaskLogDO::getTaskId, taskId));
        if (taskLogList == null || taskLogList.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> userIds =
                taskLogList.stream()
                        .map(TaskLogDO::getTargetUserId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
        taskLogList.stream()
                .map(taskLog -> NumberUtil.parseLong(taskLog.getCreator()))
                .filter(Objects::nonNull)
                .forEach(userIds::add);

        final Map<Long, UserInfoDTO> userMap;
        if (!userIds.isEmpty()) {
            Map<Long, UserInfoDTO> fetchedUsers = userRpcService.getUsers(userIds);
            if (fetchedUsers != null) {
                userMap = fetchedUsers;
            } else {
                userMap = Map.of();
            }
        } else {
            userMap = Map.of();
        }

        return taskLogList.stream()
                .map(
                        taskLog -> {
                            UserInfoDTO targetUser = userMap.get(taskLog.getTargetUserId());
                            UserVO targetUserVO = toUserVO(targetUser);

                            Long creatorId = NumberUtil.parseLong(taskLog.getCreator());
                            UserInfoDTO sourceUser =
                                    creatorId != null ? userMap.get(creatorId) : null;
                            UserVO sourceUserVO = toUserVO(sourceUser);
                            if (ObjectUtil.isNull(sourceUserVO)) {
                                sourceUserVO = new UserVO();
                            }

                            List<FileResultDTO> fileResults =
                                    fileStorageRpcService.downloadFilesByTaskLogId(taskLog.getId());
                            return TaskLogRespVO.builder()
                                    .id(taskLog.getId())
                                    .action(taskLog.getAction())
                                    .createTime(taskLog.getCreateTime())
                                    .targetUser(targetUserVO)
                                    .sourceUser(sourceUserVO)
                                    .fileResults(fileResults)
                                    .remark(taskLog.getRemark())
                                    .build();
                        })
                .toList();
    }

    private UserVO toUserVO(UserInfoDTO dto) {
        if (dto == null) {
            return null;
        }
        return UserVO.builder()
                .id(dto.getId())
                .name(dto.getUsername())
                .email(dto.getEmail())
                .build();
    }
}
