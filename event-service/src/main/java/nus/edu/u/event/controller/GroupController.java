package nus.edu.u.event.controller;

import static nus.edu.u.common.core.domain.CommonResult.success;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.common.core.domain.CommonResult;
import nus.edu.u.event.domain.dto.group.AddMembersReqVO;
import nus.edu.u.event.domain.dto.group.CreateGroupReqVO;
import nus.edu.u.event.domain.dto.group.GroupRespVO;
import nus.edu.u.event.domain.dto.group.UpdateGroupReqVO;
import nus.edu.u.event.domain.dto.user.UserProfileRespVO;
import nus.edu.u.event.service.GroupApplicationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
@Validated
@RequiredArgsConstructor
@Slf4j
public class GroupController {

    private final GroupApplicationService groupApplicationService;

    @PostMapping("/groups/create")
    public CommonResult<Long> createGroup(@RequestBody @Valid CreateGroupReqVO reqVO) {
        log.info("Creating new group: {}", reqVO.getName());
        Long groupId = groupApplicationService.createGroup(reqVO);
        return success(groupId);
    }

    @PutMapping("/groups/update")
    public CommonResult<Boolean> updateGroup(@RequestBody @Valid UpdateGroupReqVO reqVO) {
        log.info("Updating group ID: {}", reqVO.getId());
        groupApplicationService.updateGroup(reqVO);
        return success(true);
    }

    @DeleteMapping("/groups/delete/{id}")
    public CommonResult<Boolean> deleteGroup(@PathVariable("id") Long id) {
        log.info("Deleting group ID: {}", id);
        groupApplicationService.deleteGroup(id);
        return success(true);
    }

    @PostMapping("/groups/{groupId}/members/{userId}")
    public CommonResult<Boolean> addMember(
            @PathVariable("groupId") Long groupId, @PathVariable("userId") Long userId) {
        log.info("Adding user {} to group {}", userId, groupId);
        groupApplicationService.addMemberToGroup(groupId, userId);
        return success(true);
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public CommonResult<Boolean> removeMember(
            @PathVariable("groupId") Long groupId, @PathVariable("userId") Long userId) {
        log.info("Removing user {} from group {}", userId, groupId);
        groupApplicationService.removeMemberFromGroup(groupId, userId);
        return success(true);
    }

    @GetMapping("/groups/{groupId}/members")
    public CommonResult<List<GroupRespVO.MemberInfo>> getGroupMembers(
            @PathVariable("groupId") Long groupId) {
        return success(groupApplicationService.getGroupMembers(groupId));
    }

    @PostMapping("/groups/{groupId}/members/batch")
    public CommonResult<Boolean> addMembers(
            @PathVariable("groupId") Long groupId, @RequestBody @Valid AddMembersReqVO reqVO) {
        log.info("Adding users {} to group {}", reqVO.getUserIds(), groupId);
        groupApplicationService.addMembersToGroup(groupId, reqVO.getUserIds());
        return success(true);
    }

    @DeleteMapping("/groups/{groupId}/members/batch")
    public CommonResult<Boolean> deleteMembers(
            @PathVariable("groupId") Long groupId, @RequestBody List<Long> userIds) {
        log.info("Deleting users {} from group {}", userIds, groupId);
        groupApplicationService.removeMembersFromGroup(groupId, userIds);
        return success(true);
    }

    @GetMapping("/groups/list")
    public CommonResult<List<GroupRespVO>> getGroupsByEvent(@RequestParam("eventId") Long eventId) {
        log.info("Getting groups for event ID: {}", eventId);
        return success(groupApplicationService.getGroupsByEvent(eventId));
    }

    @GetMapping("/groups/users")
    public CommonResult<List<UserProfileRespVO>> getAllUserProfiles() {
        return success(groupApplicationService.getAllUserProfiles());
    }
}
