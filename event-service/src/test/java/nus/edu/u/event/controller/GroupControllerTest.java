package nus.edu.u.event.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import nus.edu.u.common.core.domain.CommonResult;
import nus.edu.u.event.domain.dto.group.AddMembersReqVO;
import nus.edu.u.event.domain.dto.group.CreateGroupReqVO;
import nus.edu.u.event.domain.dto.group.GroupRespVO;
import nus.edu.u.event.domain.dto.group.UpdateGroupReqVO;
import nus.edu.u.event.domain.dto.user.UserProfileRespVO;
import nus.edu.u.event.service.GroupApplicationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupControllerTest {

    @Mock private GroupApplicationService groupApplicationService;
    @InjectMocks private GroupController controller;

    @Test
    void createGroup_delegatesToApplicationService() {
        CreateGroupReqVO req = new CreateGroupReqVO();
        req.setName("Ops");
        when(groupApplicationService.createGroup(req)).thenReturn(1L);

        CommonResult<Long> result = controller.createGroup(req);

        assertThat(result.getData()).isEqualTo(1L);
        verify(groupApplicationService).createGroup(req);
    }

    @Test
    void updateGroup_returnsTrue() {
        UpdateGroupReqVO req = new UpdateGroupReqVO();
        req.setId(10L);

        CommonResult<Boolean> result = controller.updateGroup(req);

        assertThat(result.getData()).isTrue();
        verify(groupApplicationService).updateGroup(req);
    }

    @Test
    void deleteGroup_returnsTrue() {
        CommonResult<Boolean> result = controller.deleteGroup(5L);

        assertThat(result.getData()).isTrue();
        verify(groupApplicationService).deleteGroup(5L);
    }

    @Test
    void addMember_returnsTrue() {
        CommonResult<Boolean> result = controller.addMember(2L, 3L);

        assertThat(result.getData()).isTrue();
        verify(groupApplicationService).addMemberToGroup(2L, 3L);
    }

    @Test
    void removeMember_returnsTrue() {
        CommonResult<Boolean> result = controller.removeMember(7L, 8L);

        assertThat(result.getData()).isTrue();
        verify(groupApplicationService).removeMemberFromGroup(7L, 8L);
    }

    @Test
    void getGroupMembers_returnsMembers() {
        List<GroupRespVO.MemberInfo> members = List.of(new GroupRespVO.MemberInfo());
        when(groupApplicationService.getGroupMembers(9L)).thenReturn(members);

        CommonResult<List<GroupRespVO.MemberInfo>> result = controller.getGroupMembers(9L);

        assertThat(result.getData()).isSameAs(members);
        verify(groupApplicationService).getGroupMembers(9L);
    }

    @Test
    void addMembers_batchDelegates() {
        AddMembersReqVO req = new AddMembersReqVO();
        req.setUserIds(List.of(1L, 2L));

        CommonResult<Boolean> result = controller.addMembers(4L, req);

        assertThat(result.getData()).isTrue();
        verify(groupApplicationService).addMembersToGroup(4L, req.getUserIds());
    }

    @Test
    void deleteMembers_batchDelegates() {
        List<Long> userIds = List.of(3L, 4L);

        CommonResult<Boolean> result = controller.deleteMembers(6L, userIds);

        assertThat(result.getData()).isTrue();
        verify(groupApplicationService).removeMembersFromGroup(6L, userIds);
    }

    @Test
    void getGroupsByEvent_returnsGroups() {
        List<GroupRespVO> groups = List.of(new GroupRespVO());
        when(groupApplicationService.getGroupsByEvent(11L)).thenReturn(groups);

        CommonResult<List<GroupRespVO>> result = controller.getGroupsByEvent(11L);

        assertThat(result.getData()).isSameAs(groups);
        verify(groupApplicationService).getGroupsByEvent(11L);
    }

    @Test
    void getAllUserProfiles_returnsProfiles() {
        List<UserProfileRespVO> profiles = List.of(new UserProfileRespVO());
        when(groupApplicationService.getAllUserProfiles()).thenReturn(profiles);

        CommonResult<List<UserProfileRespVO>> result = controller.getAllUserProfiles();

        assertThat(result.getData()).isSameAs(profiles);
        verify(groupApplicationService).getAllUserProfiles();
    }
}
