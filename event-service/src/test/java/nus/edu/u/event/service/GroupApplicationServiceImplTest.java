package nus.edu.u.event.service;

import static nus.edu.u.common.enums.ErrorCodeConstants.ADD_MEMBERS_FAILED;
import static nus.edu.u.common.enums.ErrorCodeConstants.EVENT_NOT_FOUND;
import static nus.edu.u.common.enums.ErrorCodeConstants.GET_GROUP_ID_FAILED;
import static nus.edu.u.common.enums.ErrorCodeConstants.GROUP_MEMBER_ALREADY_EXISTS;
import static nus.edu.u.common.enums.ErrorCodeConstants.GROUP_NAME_EXISTS;
import static nus.edu.u.common.enums.ErrorCodeConstants.GROUP_NOT_FOUND;
import static nus.edu.u.common.enums.ErrorCodeConstants.USER_ALREADY_IN_OTHER_GROUP_OF_EVENT;
import static nus.edu.u.common.enums.ErrorCodeConstants.USER_NOT_FOUND;
import static nus.edu.u.common.enums.ErrorCodeConstants.USER_STATUS_INVALID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import nus.edu.u.common.enums.CommonStatusEnum;
import nus.edu.u.common.exception.ServiceException;
import nus.edu.u.event.convert.UserConvert;
import nus.edu.u.event.domain.dataobject.event.EventDO;
import nus.edu.u.event.domain.dataobject.group.DeptDO;
import nus.edu.u.event.domain.dataobject.user.UserGroupDO;
import nus.edu.u.event.domain.dto.group.*;
import nus.edu.u.event.domain.dto.user.UserProfileRespVO;
import nus.edu.u.event.mapper.DeptMapper;
import nus.edu.u.event.mapper.EventMapper;
import nus.edu.u.event.mapper.UserGroupMapper;
import nus.edu.u.shared.rpc.group.GroupDTO;
import nus.edu.u.shared.rpc.group.GroupMemberDTO;
import nus.edu.u.shared.rpc.user.RoleBriefDTO;
import nus.edu.u.shared.rpc.user.UserInfoDTO;
import nus.edu.u.shared.rpc.user.UserProfileDTO;
import nus.edu.u.shared.rpc.user.UserRpcService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GroupApplicationServiceImplTest {

    @Mock private UserRpcService userRpcService;
    @Mock private DeptMapper deptMapper;
    @Mock private EventMapper eventMapper;
    @Mock private UserGroupMapper userGroupMapper;
    @Mock private GroupMemberRemovalService groupMemberRemovalService;
    @Mock private UserConvert userConvert;

    @InjectMocks private GroupApplicationServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "userRpcService", userRpcService);
    }

    @Test
    void createGroup_withLeadUser_addsLeadAndReturnsId() {
        CreateGroupReqVO req = new CreateGroupReqVO();
        req.setEventId(7L);
        req.setName("Logistics");
        req.setLeadUserId(101L);
        req.setSort(1);
        req.setRemark("Support team");

        when(eventMapper.selectById(req.getEventId()))
                .thenReturn(EventDO.builder().id(req.getEventId()).name("Summit").build());
        when(deptMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userRpcService.getUsers(anyCollection()))
                .thenReturn(
                        Map.of(
                                req.getLeadUserId(),
                                UserInfoDTO.builder()
                                        .id(req.getLeadUserId())
                                        .username("Lead")
                                        .status(CommonStatusEnum.ENABLE.getStatus())
                                        .build()));

        AtomicReference<DeptDO> inserted = new AtomicReference<>();
        when(deptMapper.insert(any(DeptDO.class)))
                .thenAnswer(
                        invocation -> {
                            DeptDO dept = invocation.getArgument(0);
                            dept.setId(55L);
                            inserted.set(dept);
                            return 1;
                        });
        when(deptMapper.selectById(55L)).thenAnswer(invocation -> inserted.get());
        when(userGroupMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userGroupMapper.insert(any(UserGroupDO.class))).thenReturn(1);

        Long groupId = service.createGroup(req);

        assertThat(groupId).isEqualTo(55L);

        ArgumentCaptor<UserGroupDO> relationCaptor = ArgumentCaptor.forClass(UserGroupDO.class);
        verify(userGroupMapper).insert(relationCaptor.capture());
        assertThat(relationCaptor.getValue().getUserId()).isEqualTo(req.getLeadUserId());
        assertThat(relationCaptor.getValue().getDeptId()).isEqualTo(55L);
    }

    @Test
    void addMemberToGroup_whenUserAlreadyInAnotherGroup_throwsException() {
        long groupId = 10L;
        long userId = 200L;
        DeptDO group = DeptDO.builder().id(groupId).eventId(9L).leadUserId(300L).build();
        when(deptMapper.selectById(groupId)).thenReturn(group);
        when(userRpcService.getUsers(anyCollection()))
                .thenReturn(
                        Map.of(
                                userId,
                                UserInfoDTO.builder()
                                        .id(userId)
                                        .username("Member")
                                        .status(CommonStatusEnum.ENABLE.getStatus())
                                        .build()));

        UserGroupDO relation =
                UserGroupDO.builder()
                        .id(1L)
                        .userId(userId)
                        .eventId(group.getEventId())
                        .deptId(groupId + 1)
                        .build();
        when(userGroupMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(relation);

        ServiceException ex =
                assertThrows(
                        ServiceException.class, () -> service.addMemberToGroup(groupId, userId));

        assertThat(ex.getCode()).isEqualTo(USER_ALREADY_IN_OTHER_GROUP_OF_EVENT.getCode());
        verify(userGroupMapper, never()).insert(any());
    }

    @Test
    void createGroup_whenEventMissing_throwsEventNotFound() {
        CreateGroupReqVO req = new CreateGroupReqVO();
        req.setEventId(99L);
        when(eventMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> service.createGroup(req))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(EVENT_NOT_FOUND.getCode());
    }

    @Test
    void createGroup_whenNameExists_throwsGroupNameExists() {
        CreateGroupReqVO req = baseCreateRequest();
        when(eventMapper.selectById(req.getEventId()))
                .thenReturn(EventDO.builder().id(req.getEventId()).build());
        when(deptMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThatThrownBy(() -> service.createGroup(req))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(GROUP_NAME_EXISTS.getCode());
    }

    @Test
    void createGroup_whenLeadUserMissing_throwsUserNotFound() {
        CreateGroupReqVO req = baseCreateRequest();
        req.setLeadUserId(88L);

        when(eventMapper.selectById(req.getEventId()))
                .thenReturn(EventDO.builder().id(req.getEventId()).build());
        when(deptMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userRpcService.getUsers(anyCollection())).thenReturn(Map.of());

        assertThatThrownBy(() -> service.createGroup(req))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(USER_NOT_FOUND.getCode());
    }

    @Test
    void createGroup_whenIdNotGenerated_throwsGetIdFailed() {
        CreateGroupReqVO req = baseCreateRequest();

        when(eventMapper.selectById(req.getEventId()))
                .thenReturn(EventDO.builder().id(req.getEventId()).build());
        when(deptMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userRpcService.getUsers(anyCollection()))
                .thenReturn(
                        Map.of(
                                req.getLeadUserId(),
                                UserInfoDTO.builder()
                                        .id(req.getLeadUserId())
                                        .status(CommonStatusEnum.ENABLE.getStatus())
                                        .build()));
        when(deptMapper.insert(any(DeptDO.class)))
                .thenAnswer(
                        invocation -> {
                            DeptDO dept = invocation.getArgument(0);
                            dept.setId(null);
                            return 1;
                        });

        assertThatThrownBy(() -> service.createGroup(req))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(GET_GROUP_ID_FAILED.getCode());
    }

    @Test
    void updateGroup_whenGroupMissing_throwsGroupNotFound() {
        UpdateGroupReqVO req = new UpdateGroupReqVO();
        req.setId(44L);
        when(deptMapper.selectById(44L)).thenReturn(null);

        assertThatThrownBy(() -> service.updateGroup(req))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(GROUP_NOT_FOUND.getCode());
    }

    @Test
    void updateGroup_validatesLeadUserWhenPresent() {
        UpdateGroupReqVO req = new UpdateGroupReqVO();
        req.setId(1L);
        req.setLeadUserId(88L);
        when(deptMapper.selectById(1L)).thenReturn(DeptDO.builder().id(1L).build());
        when(userRpcService.getUsers(anyCollection()))
                .thenReturn(Map.of(88L, UserInfoDTO.builder().id(88L).status(1).build()));

        service.updateGroup(req);
        verify(deptMapper).updateById(any(DeptDO.class));
    }

    @Test
    void deleteGroup_whenGroupMissing_throwsGroupNotFound() {
        when(deptMapper.selectById(9L)).thenReturn(null);

        assertThatThrownBy(() -> service.deleteGroup(9L))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(GROUP_NOT_FOUND.getCode());
    }

    @Test
    void addMemberToGroup_whenUserStatusInvalid_throwsException() {
        long groupId = 12L;
        long userId = 33L;
        when(deptMapper.selectById(groupId))
                .thenReturn(DeptDO.builder().id(groupId).eventId(1L).build());
        when(userRpcService.getUsers(anyCollection()))
                .thenReturn(
                        Map.of(
                                userId,
                                UserInfoDTO.builder()
                                        .id(userId)
                                        .status(CommonStatusEnum.DISABLE.getStatus())
                                        .build()));

        assertThatThrownBy(() -> service.addMemberToGroup(groupId, userId))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(USER_STATUS_INVALID.getCode());
    }

    @Test
    void addMemberToGroup_whenAlreadyInTargetGroup_throwsMemberExists() {
        long groupId = 12L;
        long userId = 33L;
        when(deptMapper.selectById(groupId))
                .thenReturn(DeptDO.builder().id(groupId).eventId(1L).build());
        when(userRpcService.getUsers(anyCollection()))
                .thenReturn(
                        Map.of(
                                userId,
                                UserInfoDTO.builder()
                                        .id(userId)
                                        .status(CommonStatusEnum.ENABLE.getStatus())
                                        .build()));
        when(userGroupMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(
                        UserGroupDO.builder().deptId(groupId).eventId(1L).userId(userId).build());

        assertThatThrownBy(() -> service.addMemberToGroup(groupId, userId))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(GROUP_MEMBER_ALREADY_EXISTS.getCode());
    }

    @Test
    void addMemberToGroup_insertSuccess() {
        long groupId = 20L;
        long userId = 30L;
        when(deptMapper.selectById(groupId))
                .thenReturn(DeptDO.builder().id(groupId).eventId(5L).build());
        when(userRpcService.getUsers(anyCollection()))
                .thenReturn(
                        Map.of(
                                userId,
                                UserInfoDTO.builder()
                                        .id(userId)
                                        .status(CommonStatusEnum.ENABLE.getStatus())
                                        .build()));
        when(userGroupMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userGroupMapper.insert(any(UserGroupDO.class))).thenReturn(1);

        service.addMemberToGroup(groupId, userId);

        verify(userGroupMapper).insert(any(UserGroupDO.class));
    }

    @Test
    void removeMemberFromGroup_delegatesToRemovalService() {
        service.removeMemberFromGroup(1L, 2L);
        verify(groupMemberRemovalService).removeMemberFromGroup(1L, 2L);
    }

    @Test
    void addMembersToGroup_recordsFailures() {
        GroupApplicationServiceImpl spyService = spy(service);
        ReflectionTestUtils.setField(spyService, "userRpcService", userRpcService);
        doThrow(new RuntimeException("fail")).when(spyService).addMemberToGroup(1L, 2L);

        assertThatThrownBy(() -> spyService.addMembersToGroup(1L, List.of(2L)))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(ADD_MEMBERS_FAILED.getCode());
    }

    @Test
    void addMembersToGroup_withEmptyList_doesNothing() {
        GroupApplicationServiceImpl spyService = spy(service);
        ReflectionTestUtils.setField(spyService, "userRpcService", userRpcService);

        spyService.addMembersToGroup(5L, List.of());

        verify(spyService, never()).addMemberToGroup(any(), any());
    }

    @Test
    void removeMembersFromGroup_propagatesFirstException() {
        ServiceException failure = new ServiceException(USER_ALREADY_IN_OTHER_GROUP_OF_EVENT);
        doThrow(failure).when(groupMemberRemovalService).removeMemberFromGroup(1L, 2L);

        assertThatThrownBy(() -> service.removeMembersFromGroup(1L, List.of(2L, 3L)))
                .isSameAs(failure);
    }

    @Test
    void removeMembersFromGroup_withEmptyList_doesNothing() {
        service.removeMembersFromGroup(9L, List.of());

        verify(groupMemberRemovalService, never()).removeMemberFromGroup(any(), any());
    }

    @Test
    void getGroupsByEvent_whenEventMissing_throwsEventNotFound() {
        when(eventMapper.selectById(1L)).thenReturn(null);

        assertThatThrownBy(() -> service.getGroupsByEvent(1L))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(EVENT_NOT_FOUND.getCode());
    }

    @Test
    void getGroupMembers_filtersDisabledUsers() {
        when(userGroupMapper.selectList(any()))
                .thenReturn(
                        List.of(
                                UserGroupDO.builder().deptId(1L).userId(10L).build(),
                                UserGroupDO.builder().deptId(1L).userId(11L).build()));
        when(userRpcService.getUsers(anyCollection()))
                .thenReturn(
                        Map.of(
                                10L,
                                UserInfoDTO.builder()
                                        .id(10L)
                                        .status(CommonStatusEnum.ENABLE.getStatus())
                                        .username("Alice")
                                        .email("a@example.com")
                                        .build(),
                                11L,
                                UserInfoDTO.builder()
                                        .id(11L)
                                        .status(CommonStatusEnum.DISABLE.getStatus())
                                        .build()));

        List<GroupRespVO.MemberInfo> members = service.getGroupMembers(1L);

        assertThat(members).hasSize(1);
        assertThat(members.get(0).getUsername()).isEqualTo("Alice");
    }

    @Test
    void getAllUserProfiles_convertsViaUserConvert() {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(1L);
        dto.setName("Alice");
        UserProfileRespVO vo = new UserProfileRespVO();
        vo.setId(1L);
        when(userRpcService.getEnabledUserProfiles()).thenReturn(List.of(dto));
        when(userConvert.toProfile(dto)).thenReturn(vo);

        List<UserProfileRespVO> profiles = service.getAllUserProfiles();

        assertThat(profiles).hasSize(1);
        assertThat(profiles.get(0).getId()).isEqualTo(1L);
    }

    private CreateGroupReqVO baseCreateRequest() {
        CreateGroupReqVO req = new CreateGroupReqVO();
        req.setEventId(1L);
        req.setName("Team");
        req.setLeadUserId(55L);
        req.setSort(1);
        req.setRemark("remark");
        return req;
    }

    @Test
    void getGroupDTOsByEventIds_combinesGroupMetadataAndMembers() {
        Set<Long> eventIds = Set.of(1L, 2L);
        LocalDateTime now = LocalDateTime.now();

        DeptDO group1 =
                DeptDO.builder()
                        .id(11L)
                        .eventId(1L)
                        .name("Ops")
                        .leadUserId(101L)
                        .sort(1)
                        .status(CommonStatusEnum.ENABLE.getStatus())
                        .remark("Ops group")
                        .build();
        group1.setCreateTime(now.minusDays(1));
        DeptDO group2 =
                DeptDO.builder()
                        .id(22L)
                        .eventId(2L)
                        .name("Dev")
                        .leadUserId(102L)
                        .sort(2)
                        .status(CommonStatusEnum.DISABLE.getStatus())
                        .remark("Dev group")
                        .build();
        group2.setCreateTime(now.minusHours(2));

        when(deptMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(group1, group2));

        List<UserGroupDO> relations =
                List.of(
                        UserGroupDO.builder()
                                .deptId(11L)
                                .eventId(1L)
                                .userId(201L)
                                .joinTime(now.minusDays(2))
                                .build(),
                        UserGroupDO.builder()
                                .deptId(11L)
                                .eventId(1L)
                                .userId(101L)
                                .joinTime(now.minusDays(3))
                                .build(),
                        UserGroupDO.builder()
                                .deptId(22L)
                                .eventId(2L)
                                .userId(202L)
                                .joinTime(now.minusDays(4))
                                .build());
        when(userGroupMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(relations);

        when(userRpcService.getUsers(anyCollection()))
                .thenAnswer(
                        invocation -> {
                            Collection<Long> ids = invocation.getArgument(0);
                            Map<Long, UserInfoDTO> map = new HashMap<>();
                            for (Long id : ids) {
                                map.put(
                                        id,
                                        UserInfoDTO.builder()
                                                .id(id)
                                                .username("User" + id)
                                                .status(CommonStatusEnum.ENABLE.getStatus())
                                                .roles(
                                                        List.of(
                                                                RoleBriefDTO.builder()
                                                                        .id(id * 10)
                                                                        .name("Role" + id)
                                                                        .build()))
                                                .build());
                            }
                            return map;
                        });

        Map<Long, List<GroupDTO>> result = service.getGroupDTOsByEventIds(eventIds);

        assertThat(result).containsKeys(1L, 2L);
        List<GroupDTO> event1Groups = result.get(1L);
        assertThat(event1Groups).hasSize(1);
        GroupDTO groupDto1 = event1Groups.get(0);
        assertThat(groupDto1.getId()).isEqualTo(11L);
        assertThat(groupDto1.getLeadUserId()).isEqualTo(101L);
        assertThat(groupDto1.getMembers())
                .extracting(GroupMemberDTO::getUserId)
                .containsExactlyInAnyOrder(201L, 101L);

        List<GroupDTO> event2Groups = result.get(2L);
        assertThat(event2Groups).hasSize(1);
        GroupDTO groupDto2 = event2Groups.get(0);
        assertThat(groupDto2.getMembers())
                .extracting(GroupMemberDTO::getUserId)
                .containsExactly(202L);
    }

    @Test
    void getGroupDTOsByEventIds_whenInputEmpty_returnsEmptyMap() {
        Map<Long, List<GroupDTO>> result = service.getGroupDTOsByEventIds(List.of());

        assertThat(result).isEmpty();
        verify(deptMapper, never()).selectList(any());
        verify(userGroupMapper, never()).selectList(any());
    }

    @Test
    void getAllUserProfiles_convertsDtoAndMapPayloads() {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(1L);
        dto.setName("Alice");

        Map<String, Object> mapPayload = new HashMap<>();
        mapPayload.put("id", 2L);
        mapPayload.put("name", "Bob");

        @SuppressWarnings("unchecked")
        List<UserProfileDTO> payload =
                (List<UserProfileDTO>) (List<?>) List.of(dto, mapPayload, "unexpected");

        when(userRpcService.getEnabledUserProfiles()).thenReturn(payload);
        when(userConvert.toProfile(any(UserProfileDTO.class)))
                .thenAnswer(
                        invocation -> {
                            UserProfileDTO source = invocation.getArgument(0);
                            UserProfileRespVO resp = new UserProfileRespVO();
                            resp.setId(source.getId());
                            resp.setName(source.getName());
                            return resp;
                        });

        List<UserProfileRespVO> profiles = service.getAllUserProfiles();

        assertThat(profiles).hasSize(2);
        assertThat(profiles.stream().map(UserProfileRespVO::getId).toList())
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void removeMembersFromGroup_rethrowsFirstException() {
        long groupId = 9L;
        List<Long> userIds = List.of(301L, 302L);
        ServiceException failure =
                new ServiceException(USER_ALREADY_IN_OTHER_GROUP_OF_EVENT.getCode(), "failure");

        doThrow(failure)
                .when(groupMemberRemovalService)
                .removeMemberFromGroup(groupId, userIds.get(0));
        doNothing().when(groupMemberRemovalService).removeMemberFromGroup(groupId, userIds.get(1));

        ServiceException thrown =
                assertThrows(
                        ServiceException.class,
                        () -> service.removeMembersFromGroup(groupId, userIds));

        assertThat(thrown).isSameAs(failure);
        verify(groupMemberRemovalService).removeMemberFromGroup(groupId, userIds.get(1));
    }

    @Test
    void addMembersToGroup_whenAnyAdditionFails_throwsAggregatedException() {
        long groupId = 15L;
        List<Long> userIds = List.of(401L, 402L);

        GroupApplicationServiceImpl spyService = spy(service);
        ReflectionTestUtils.setField(spyService, "userRpcService", userRpcService);

        doThrow(new RuntimeException("boom"))
                .when(spyService)
                .addMemberToGroup(groupId, userIds.get(0));
        doNothing().when(spyService).addMemberToGroup(groupId, userIds.get(1));

        assertThatThrownBy(() -> spyService.addMembersToGroup(groupId, userIds))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(ADD_MEMBERS_FAILED.getCode());

        verify(spyService).addMemberToGroup(groupId, userIds.get(1));
    }
}
