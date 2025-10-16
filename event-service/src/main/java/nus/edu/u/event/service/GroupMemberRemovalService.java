package nus.edu.u.event.service;

import static nus.edu.u.common.enums.ErrorCodeConstants.CANNOT_REMOVE_GROUP_LEADER;
import static nus.edu.u.common.enums.ErrorCodeConstants.CANNOT_REMOVE_MEMBER_WITH_PENDING_TASKS;
import static nus.edu.u.common.enums.ErrorCodeConstants.GROUP_NOT_FOUND;
import static nus.edu.u.common.enums.ErrorCodeConstants.USER_NOT_IN_GROUP;
import static nus.edu.u.common.utils.exception.ServiceExceptionUtil.exception;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.event.domain.dataobject.group.DeptDO;
import nus.edu.u.event.domain.dataobject.user.UserGroupDO;
import nus.edu.u.event.mapper.DeptMapper;
import nus.edu.u.event.mapper.UserGroupMapper;
import nus.edu.u.shared.rpc.task.TaskRpcService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class GroupMemberRemovalService {

    private final UserGroupMapper userGroupMapper;
    private final DeptMapper deptMapper;

    @DubboReference(check = false)
    private TaskRpcService taskRpcService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeMemberFromGroup(Long groupId, Long userId) {
        log.info("Removing user {} from group {}", userId, groupId);

        UserGroupDO relation =
                userGroupMapper.selectOne(
                        new LambdaQueryWrapper<UserGroupDO>()
                                .eq(UserGroupDO::getUserId, userId)
                                .eq(UserGroupDO::getDeptId, groupId));

        if (relation == null) {
            log.warn("User {} is not a member of group {}", userId, groupId);
            throw exception(USER_NOT_IN_GROUP);
        }

        DeptDO group = deptMapper.selectById(groupId);
        if (group == null) {
            throw exception(GROUP_NOT_FOUND);
        }

        if (group.getLeadUserId() != null && group.getLeadUserId().equals(userId)) {
            throw exception(CANNOT_REMOVE_GROUP_LEADER);
        }

        boolean hasPendingTasks =
                taskRpcService != null
                        && taskRpcService.hasPendingTasks(relation.getEventId(), userId);
        if (hasPendingTasks) {
            log.warn("User {} still has pending tasks for event {}", userId, relation.getEventId());
            throw exception(CANNOT_REMOVE_MEMBER_WITH_PENDING_TASKS);
        }

        userGroupMapper.deleteById(relation.getId());
        log.info("Removed user {} from group {}", userId, groupId);
    }
}
