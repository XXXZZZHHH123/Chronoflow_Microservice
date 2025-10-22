package nus.edu.u.event.service;

import static nus.edu.u.common.enums.ErrorCodeConstants.CANNOT_REMOVE_GROUP_LEADER;
import static nus.edu.u.common.enums.ErrorCodeConstants.CANNOT_REMOVE_MEMBER_WITH_PENDING_TASKS;
import static nus.edu.u.common.enums.ErrorCodeConstants.GROUP_NOT_FOUND;
import static nus.edu.u.common.enums.ErrorCodeConstants.USER_NOT_IN_GROUP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import nus.edu.u.common.exception.ServiceException;
import nus.edu.u.event.domain.dataobject.group.DeptDO;
import nus.edu.u.event.domain.dataobject.user.UserGroupDO;
import nus.edu.u.event.mapper.DeptMapper;
import nus.edu.u.event.mapper.UserGroupMapper;
import nus.edu.u.shared.rpc.task.TaskRpcService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GroupMemberRemovalServiceTest {

    @Mock private UserGroupMapper userGroupMapper;
    @Mock private DeptMapper deptMapper;
    @Mock private TaskRpcService taskRpcService;

    @InjectMocks private GroupMemberRemovalService service;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(service, "taskRpcService", taskRpcService);
    }

    @Test
    void removeMemberFromGroup_missingRelation_throwsException() {
        when(userGroupMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThatThrownBy(() -> service.removeMemberFromGroup(1L, 2L))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", USER_NOT_IN_GROUP.getCode());

        verify(userGroupMapper, never()).deleteById(any());
    }

    @Test
    void removeMemberFromGroup_missingGroup_throwsException() {
        UserGroupDO relation =
                UserGroupDO.builder().id(5L).deptId(1L).eventId(9L).userId(2L).build();
        when(userGroupMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(relation);
        when(deptMapper.selectById(1L)).thenReturn(null);

        assertThatThrownBy(() -> service.removeMemberFromGroup(1L, 2L))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", GROUP_NOT_FOUND.getCode());
    }

    @Test
    void removeMemberFromGroup_leadUser_throwsException() {
        UserGroupDO relation =
                UserGroupDO.builder().id(5L).deptId(1L).eventId(9L).userId(2L).build();
        DeptDO group = DeptDO.builder().id(1L).leadUserId(2L).build();

        when(userGroupMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(relation);
        when(deptMapper.selectById(1L)).thenReturn(group);

        assertThatThrownBy(() -> service.removeMemberFromGroup(1L, 2L))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", CANNOT_REMOVE_GROUP_LEADER.getCode());
    }

    @Test
    void removeMemberFromGroup_pendingTasks_throwsException() {
        UserGroupDO relation =
                UserGroupDO.builder().id(5L).deptId(1L).eventId(9L).userId(2L).build();
        DeptDO group = DeptDO.builder().id(1L).leadUserId(3L).build();

        when(userGroupMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(relation);
        when(deptMapper.selectById(1L)).thenReturn(group);
        when(taskRpcService.hasPendingTasks(9L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> service.removeMemberFromGroup(1L, 2L))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue(
                        "code", CANNOT_REMOVE_MEMBER_WITH_PENDING_TASKS.getCode());

        verify(userGroupMapper, never()).deleteById(any());
    }

    @Test
    void removeMemberFromGroup_successfullyDeletesRelation() {
        UserGroupDO relation =
                UserGroupDO.builder().id(5L).deptId(1L).eventId(9L).userId(2L).build();
        DeptDO group = DeptDO.builder().id(1L).leadUserId(3L).build();

        when(userGroupMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(relation);
        when(deptMapper.selectById(1L)).thenReturn(group);
        when(taskRpcService.hasPendingTasks(9L, 2L)).thenReturn(false);

        service.removeMemberFromGroup(1L, 2L);

        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        verify(userGroupMapper).deleteById(captor.capture());
        assertThat(captor.getValue()).isEqualTo(relation.getId());
    }

    @Test
    void removeMemberFromGroup_whenTaskServiceUnavailable_stillRemovesRelation() {
        ReflectionTestUtils.setField(service, "taskRpcService", null);

        UserGroupDO relation =
                UserGroupDO.builder().id(5L).deptId(1L).eventId(9L).userId(2L).build();
        DeptDO group = DeptDO.builder().id(1L).leadUserId(3L).build();

        when(userGroupMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(relation);
        when(deptMapper.selectById(1L)).thenReturn(group);

        service.removeMemberFromGroup(1L, 2L);

        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        verify(userGroupMapper).deleteById(captor.capture());
        assertThat(captor.getValue()).isEqualTo(relation.getId());

        // restore mocked field for other tests
        ReflectionTestUtils.setField(service, "taskRpcService", taskRpcService);
    }
}
