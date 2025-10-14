package nus.edu.u.task.service;

import static nus.edu.u.common.utils.exception.ServiceExceptionUtil.exception;
import static nus.edu.u.common.enums.ErrorCodeConstants.TASK_LOG_ERROR;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import nus.edu.u.task.domain.dataobject.task.TaskLogDO;
import nus.edu.u.task.domain.dataobject.user.UserDO;
import nus.edu.u.task.domain.vo.user.UserVO;
import nus.edu.u.shared.rpc.file.FileResultVO;
import nus.edu.u.task.domain.vo.taskLog.TaskLogRespVO;
import nus.edu.u.task.mapper.TaskLogMapper;
import nus.edu.u.task.mapper.UserMapper;
import nus.edu.u.shared.rpc.file.FileStorageRpcService;

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

    private final UserMapper userMapper;

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
        List<Long> userIdList =
                taskLogList.stream().map(TaskLogDO::getTargetUserId).collect(Collectors.toList());
        userIdList.addAll(
                taskLogList.stream()
                        .map(taskLog -> NumberUtil.parseLong(taskLog.getCreator()))
                        .toList());
        List<UserDO> userList = userMapper.selectBatchIds(userIdList);
        Map<Long, UserDO> userMap =
                userList.stream().collect(Collectors.toMap(UserDO::getId, user -> user));
        return taskLogList.stream()
                .map(
                        taskLog -> {
                            UserDO targetUser = userMap.get(taskLog.getTargetUserId());
                            UserVO targetUserVO = null;
                            if (ObjectUtil.isNotNull(targetUser)) {
                                targetUserVO = new UserVO();
                                targetUserVO.setId(targetUser.getId());
                                targetUserVO.setName(targetUser.getUsername());
                                targetUserVO.setEmail(targetUser.getEmail());
                            }
                            UserDO sourceUser =
                                    userMap.get(NumberUtil.parseLong(taskLog.getCreator()));
                            UserVO sourceUserVO = new UserVO();
                            if (ObjectUtil.isNotNull(sourceUser)) {
                                sourceUserVO.setId(sourceUser.getId());
                                sourceUserVO.setName(sourceUser.getUsername());
                                sourceUserVO.setEmail(sourceUser.getEmail());
                            }
                            List<FileResultVO> fileResults =
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
}
