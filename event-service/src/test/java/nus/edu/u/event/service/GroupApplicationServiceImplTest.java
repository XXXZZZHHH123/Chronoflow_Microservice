package nus.edu.u.event.service;

import static nus.edu.u.common.enums.ErrorCodeConstants.ADD_MEMBERS_FAILED;
import static nus.edu.u.common.enums.ErrorCodeConstants.USER_ALREADY_IN_OTHER_GROUP_OF_EVENT;
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
import nus.edu.u.event.domain.dto.group.CreateGroupReqVO;
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

        EventDO event1 = EventDO.builder().id(1L).name("Kickoff").build();
        EventDO event2 = EventDO.builder().id(2L).name("Wrap-up").build();
        when(eventMapper.selectBatchIds(eventIds)).thenReturn(List.of(event1, event2));

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
        when(userGroupMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(relations)
                .thenReturn(relations);

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
