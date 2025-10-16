package nus.edu.u.event.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import nus.edu.u.event.domain.dto.group.CreateGroupReqVO;
import nus.edu.u.event.domain.dto.group.GroupRespVO;
import nus.edu.u.event.domain.dto.group.UpdateGroupReqVO;
import nus.edu.u.event.domain.dto.user.UserProfileRespVO;
import nus.edu.u.shared.rpc.group.GroupDTO;

public interface GroupApplicationService {

    Long createGroup(CreateGroupReqVO reqVO);

    void updateGroup(UpdateGroupReqVO reqVO);

    void deleteGroup(Long id);

    void addMemberToGroup(Long groupId, Long userId);

    void removeMemberFromGroup(Long groupId, Long userId);

    List<GroupRespVO.MemberInfo> getGroupMembers(Long groupId);

    void addMembersToGroup(Long groupId, List<Long> userIds);

    void removeMembersFromGroup(Long groupId, List<Long> userIds);

    List<GroupRespVO> getGroupsByEvent(Long eventId);

    Map<Long, List<GroupRespVO>> getGroupsByEventIds(Collection<Long> eventIds);

    Map<Long, List<GroupDTO>> getGroupDTOsByEventIds(Collection<Long> eventIds);

    List<UserProfileRespVO> getAllUserProfiles();
}
