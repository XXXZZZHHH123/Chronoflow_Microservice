package nus.edu.u.task.service;

import static nus.edu.u.common.enums.ErrorCodeConstants.*;
import static nus.edu.u.common.utils.exception.ServiceExceptionUtil.exception;
import static nus.edu.u.task.enums.TaskActionEnum.getUpdateTaskAction;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.shared.rpc.events.EventRespDTO;
import nus.edu.u.shared.rpc.events.EventRpcService;
import nus.edu.u.shared.rpc.group.GroupDTO;
import nus.edu.u.shared.rpc.group.GroupMemberDTO;
import nus.edu.u.shared.rpc.group.GroupRpcService;
import nus.edu.u.shared.rpc.user.UserInfoDTO;
import nus.edu.u.shared.rpc.user.UserRpcService;
import nus.edu.u.task.action.TaskActionFactory;
import nus.edu.u.task.builder.TaskRespVOBuilder;
import nus.edu.u.task.builder.TasksRespVOBuilder;
import nus.edu.u.task.convert.TaskConvert;
import nus.edu.u.task.domain.dataobject.group.DeptDO;
import nus.edu.u.task.domain.dataobject.task.TaskDO;
import nus.edu.u.task.domain.dataobject.user.UserDO;
import nus.edu.u.task.domain.dto.TaskActionDTO;
import nus.edu.u.task.domain.vo.task.TaskCreateReqVO;
import nus.edu.u.task.domain.vo.task.TaskDashboardRespVO;
import nus.edu.u.task.domain.vo.task.TaskRespVO;
import nus.edu.u.task.domain.vo.task.TaskUpdateReqVO;
import nus.edu.u.task.domain.vo.task.TasksRespVO;
import nus.edu.u.task.enums.TaskActionEnum;
import nus.edu.u.task.mapper.TaskMapper;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskApplicationServiceImpl implements TaskApplicationService {

    private final TaskMapper taskMapper;

    @DubboReference(check = false)
    private EventRpcService eventRpcService;

    @DubboReference(check = false)
    private UserRpcService userRpcService;

    @DubboReference(check = false)
    private GroupRpcService groupRpcService;

    private final TaskActionFactory taskActionFactory;

    @Override
    @Transactional
    public TaskRespVO createTask(Long eventId, TaskCreateReqVO reqVO) {
        EventRespDTO event = eventRpcService.getEvent(eventId);
        if (event == null) {
            throw exception(EVENT_NOT_FOUND);
        }

        UserDO assignee = fetchUser(reqVO.getTargetUserId());
        if (assignee == null) {
            throw exception(TASK_ASSIGNEE_NOT_FOUND);
        }

        UserDO assigner = fetchUser(event.getOrganizerId());
        Long eventTenantId = assigner != null ? assigner.getTenantId() : null;
        if (eventTenantId != null && !Objects.equals(eventTenantId, assignee.getTenantId())) {
            throw exception(TASK_ASSIGNEE_TENANT_MISMATCH);
        }

        TaskDO task = TaskConvert.INSTANCE.convert(reqVO);
        task.setEventId(eventId);
        task.setTenantId(eventTenantId);
        task.setStartTime(reqVO.getStartTime());
        task.setEndTime(reqVO.getEndTime());

        TaskActionDTO actionDTO =
                TaskActionDTO.builder()
                        .startTime(reqVO.getStartTime())
                        .endTime(reqVO.getEndTime())
                        .files(reqVO.getFiles())
                        .targetUserId(reqVO.getTargetUserId())
                        .eventStartTime(event.getStartTime())
                        .eventEndTime(event.getEndTime())
                        .build();
        taskActionFactory.getStrategy(TaskActionEnum.CREATE).execute(task, actionDTO);

        return TaskRespVOBuilder.from(task)
                .withEvent(event)
                .withEventSupplier(() -> fetchEvent(task.getEventId()))
                .withAssigner(assigner)
                .withAssignerSupplier(() -> fetchUser(event.getOrganizerId()))
                .withAssignerGroupsResolver(
                        user -> resolveAssignerGroups(user.getId(), event.getId()))
                .withAssignee(assignee)
                .withAssigneeSupplier(() -> fetchUser(task.getUserId()))
                .withAssigneeGroupsResolver(
                        user -> resolveCrudGroups(user.getId(), event.getId(), null))
                .build();
    }

    @Override
    @Transactional
    public TaskRespVO updateTask(Long eventId, Long taskId, TaskUpdateReqVO reqVO, Integer type) {
        EventRespDTO event = eventRpcService.getEvent(eventId);
        if (event == null) {
            throw exception(EVENT_NOT_FOUND);
        }

        TaskDO task = taskMapper.selectById(taskId);
        if (task == null || !Objects.equals(task.getEventId(), eventId)) {
            throw exception(TASK_NOT_FOUND);
        }

        UserDO assigner = fetchUser(event.getOrganizerId());
        Long eventTenantId = assigner != null ? assigner.getTenantId() : null;

        UserDO assignee = null;
        if (reqVO.getTargetUserId() != null) {
            assignee = fetchUser(reqVO.getTargetUserId());
            if (assignee == null) {
                throw exception(TASK_ASSIGNEE_NOT_FOUND);
            }

            if (eventTenantId != null && !Objects.equals(eventTenantId, assignee.getTenantId())) {
                throw exception(TASK_ASSIGNEE_TENANT_MISMATCH);
            }
        }

        if (!Arrays.asList(getUpdateTaskAction()).contains(reqVO.getType())) {
            throw exception(WRONG_TASK_ACTION_TYPE);
        }
        TaskActionDTO actionDTO =
                TaskActionDTO.builder()
                        .name(reqVO.getName())
                        .description(reqVO.getDescription())
                        .startTime(reqVO.getStartTime())
                        .endTime(reqVO.getEndTime())
                        .eventStartTime(event.getStartTime())
                        .eventEndTime(event.getEndTime())
                        .targetUserId(reqVO.getTargetUserId())
                        .files(reqVO.getFiles())
                        .remark(reqVO.getRemark())
                        .build();

        taskActionFactory.getStrategy(TaskActionEnum.getEnum(type)).execute(task, actionDTO);

        return TaskRespVOBuilder.from(task)
                .withEvent(event)
                .withEventSupplier(() -> fetchEvent(task.getEventId()))
                .withAssigner(assigner)
                .withAssignerSupplier(() -> fetchUser(event.getOrganizerId()))
                .withAssignerGroupsResolver(
                        user -> resolveAssignerGroups(user.getId(), event.getId()))
                .withAssignee(assignee)
                .withAssigneeSupplier(() -> fetchUser(task.getUserId()))
                .withAssigneeGroupsResolver(
                        user -> resolveCrudGroups(user.getId(), event.getId(), null))
                .build();
    }

    @Override
    @Transactional
    public void deleteTask(Long eventId, Long taskId) {
        EventRespDTO event = eventRpcService.getEvent(eventId);
        if (event == null) {
            throw exception(EVENT_NOT_FOUND);
        }

        TaskDO task = taskMapper.selectById(taskId);
        if (task == null || !Objects.equals(task.getEventId(), eventId)) {
            throw exception(TASK_NOT_FOUND);
        }
        taskActionFactory.getStrategy(TaskActionEnum.DELETE).execute(task, null);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskRespVO getTask(Long eventId, Long taskId) {
        EventRespDTO event = eventRpcService.getEvent(eventId);
        if (event == null) {
            throw exception(EVENT_NOT_FOUND);
        }

        TaskDO task = taskMapper.selectById(taskId);
        if (task == null || !Objects.equals(task.getEventId(), eventId)) {
            throw exception(TASK_NOT_FOUND);
        }

        UserDO assigner = fetchUser(event.getOrganizerId());

        return TaskRespVOBuilder.from(task)
                .withEvent(event)
                .withEventSupplier(() -> fetchEvent(task.getEventId()))
                .withAssigner(assigner)
                .withAssignerSupplier(() -> fetchUser(event.getOrganizerId()))
                .withAssignerGroupsResolver(
                        user -> resolveAssignerGroups(user.getId(), event.getId()))
                .withAssigneeSupplier(() -> fetchUser(task.getUserId()))
                .withAssigneeGroupsResolver(
                        user -> resolveCrudGroups(user.getId(), event.getId(), null))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskRespVO> listTasksByEvent(Long eventId) {
        EventRespDTO event = eventRpcService.getEvent(eventId);
        if (event == null) {
            throw exception(EVENT_NOT_FOUND);
        }

        List<TaskDO> tasks =
                taskMapper.selectList(
                        Wrappers.<TaskDO>lambdaQuery().eq(TaskDO::getEventId, eventId));

        if (tasks.isEmpty()) {
            return List.of();
        }

        List<Long> userIds =
                tasks.stream().map(TaskDO::getUserId).filter(Objects::nonNull).distinct().toList();

        Map<Long, UserDO> usersById = userIds.isEmpty() ? Map.of() : fetchUsersByIds(userIds);

        Map<Long, List<TaskRespVO.AssignedUserVO.GroupVO>> groupsByUserId =
                buildCrudGroupsByUser(usersById.keySet(), eventId);

        UserDO assigner = fetchUser(event.getOrganizerId());

        return tasks.stream()
                .map(
                        task -> {
                            Long userId = task.getUserId();
                            UserDO user = userId != null ? usersById.get(userId) : null;
                            return TaskRespVOBuilder.from(task)
                                    .withEvent(event)
                                    .withEventSupplier(() -> fetchEvent(task.getEventId()))
                                    .withAssigner(assigner)
                                    .withAssignerSupplier(() -> fetchUser(event.getOrganizerId()))
                                    .withAssignerGroupsResolver(
                                            assignerUser ->
                                                    resolveAssignerGroups(
                                                            assignerUser.getId(), eventId))
                                    .withAssignee(user)
                                    .withAssigneeSupplier(() -> fetchUser(task.getUserId()))
                                    .withAssigneeGroupsResolver(
                                            assignedUser ->
                                                    resolveCrudGroups(
                                                            assignedUser.getId(),
                                                            eventId,
                                                            groupsByUserId))
                                    .build();
                        })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskRespVO> listTasksByMember(Long memberId) {
        UserDO member = fetchUser(memberId);
        if (member == null) {
            throw exception(USER_NOT_FOUND);
        }

        List<TaskDO> tasks =
                taskMapper.selectList(
                        Wrappers.<TaskDO>lambdaQuery().eq(TaskDO::getUserId, memberId));

        if (tasks.isEmpty()) {
            return List.of();
        }

        Map<Long, UserDO> usersById = Map.of(memberId, member);
        Map<Long, Map<Long, List<TaskRespVO.AssignedUserVO.GroupVO>>> crudGroupsCache =
                new LinkedHashMap<>();
        Map<Long, Map<Long, List<TaskRespVO.AssignerUserVO.GroupVO>>> assignerGroupsCache =
                new LinkedHashMap<>();

        List<Long> eventIds =
                tasks.stream().map(TaskDO::getEventId).filter(Objects::nonNull).distinct().toList();

        Map<Long, EventRespDTO> eventsById = fetchEventsByIds(eventIds);

        Map<Long, UserDO> assignersByEventId = buildAssignersByEventId(eventsById.values());

        return tasks.stream()
                .map(
                        task -> {
                            Long eventId = task.getEventId();
                            EventRespDTO event = eventId != null ? eventsById.get(eventId) : null;
                            Long currentEventId = eventId;
                            return TaskRespVOBuilder.from(task)
                                    .withEvent(event)
                                    .withEventSupplier(() -> fetchEvent(task.getEventId()))
                                    .withAssigner(assignersByEventId.get(eventId))
                                    .withAssignerSupplier(
                                            () -> {
                                                EventRespDTO fallbackEvent =
                                                        fetchEvent(task.getEventId());
                                                if (fallbackEvent == null) {
                                                    return null;
                                                }
                                                Long assignerId = fallbackEvent.getOrganizerId();
                                                return fetchUser(assignerId);
                                            })
                                    .withAssignerGroupsResolver(
                                            assignerUser ->
                                                    resolveAssignerGroupsWithCache(
                                                            assignerUser,
                                                            currentEventId,
                                                            assignerGroupsCache))
                                    .withAssignee(member)
                                    .withAssigneeSupplier(() -> fetchUser(task.getUserId()))
                                    .withAssigneeGroupsResolver(
                                            assignedUser ->
                                                    resolveCrudGroupsWithCache(
                                                            assignedUser,
                                                            currentEventId,
                                                            crudGroupsCache,
                                                            usersById.keySet()))
                                    .build();
                        })
                .toList();
    }

    private List<TasksRespVO> listDashboardTasksByMember(UserDO member, List<TaskDO> tasks) {
        if (tasks.isEmpty()) {
            return List.of();
        }

        Map<Long, UserDO> usersById = Map.of(member.getId(), member);
        Collection<Long> memberIds = usersById.keySet();
        Map<Long, Map<Long, List<TasksRespVO.AssignedUserVO.GroupVO>>> dashboardGroupsCache =
                new LinkedHashMap<>();

        List<Long> eventIds =
                tasks.stream().map(TaskDO::getEventId).filter(Objects::nonNull).distinct().toList();

        Map<Long, EventRespDTO> eventsById = fetchEventsByIds(eventIds);

        return tasks.stream()
                .map(
                        task -> {
                            Long eventId = task.getEventId();
                            EventRespDTO event = eventId != null ? eventsById.get(eventId) : null;
                            Long currentEventId = eventId;
                            return TasksRespVOBuilder.from(task)
                                    .withEvent(event)
                                    .withEventSupplier(() -> fetchEvent(task.getEventId()))
                                    .withAssignee(member)
                                    .withAssigneeSupplier(() -> fetchUser(task.getUserId()))
                                    .withGroupResolver(
                                            assignedUser ->
                                                    resolveDashboardGroupsWithCache(
                                                            assignedUser,
                                                            currentEventId,
                                                            dashboardGroupsCache,
                                                            memberIds))
                                    .build();
                        })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDashboardRespVO getByMemberId(Long memberId) {
        UserDO member = fetchUser(memberId);
        if (member == null) {
            throw exception(USER_NOT_FOUND);
        }

        List<TaskDO> memberTasks =
                taskMapper.selectList(
                        Wrappers.<TaskDO>lambdaQuery().eq(TaskDO::getUserId, member.getId()));

        TaskDashboardRespVO dashboard = new TaskDashboardRespVO();
        dashboard.setMember(toMemberVO(member));
        dashboard.setGroups(resolveMemberGroups(member, memberTasks));
        dashboard.setTasks(listDashboardTasksByMember(member, memberTasks));
        return dashboard;
    }

    private EventRespDTO fetchEvent(Long eventId) {
        if (eventId == null) {
            return null;
        }
        return eventRpcService.getEvent(eventId);
    }

    private Map<Long, EventRespDTO> fetchEventsByIds(Collection<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, EventRespDTO> events = new LinkedHashMap<>();
        for (Long id : eventIds) {
            if (id == null || events.containsKey(id)) {
                continue;
            }
            EventRespDTO event = eventRpcService.getEvent(id);
            if (event != null) {
                events.put(id, event);
            }
        }
        return events;
    }

    private UserDO fetchUser(Long userId) {
        if (userId == null) {
            return null;
        }
        Map<Long, UserInfoDTO> dtoMap = userRpcService.getUsers(List.of(userId));
        if (dtoMap == null || dtoMap.isEmpty()) {
            return null;
        }
        return toUser(dtoMap.get(userId));
    }

    private Map<Long, UserDO> buildAssignersByEventId(Collection<EventRespDTO> events) {
        if (events == null || events.isEmpty()) {
            return Map.of();
        }

        List<Long> assignerIds =
                events.stream()
                        .filter(Objects::nonNull)
                        .map(EventRespDTO::getOrganizerId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        Map<Long, UserDO> assignersByUserId = fetchUsersByIds(assignerIds);
        return events.stream()
                .filter(Objects::nonNull)
                .collect(
                        Collectors.toMap(
                                EventRespDTO::getId,
                                event -> assignersByUserId.get(event.getOrganizerId()),
                                (existing, replacement) -> existing));
    }

    private Map<Long, UserDO> fetchUsersByIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, UserInfoDTO> dtoMap = userRpcService.getUsers(userIds);
        if (dtoMap == null || dtoMap.isEmpty()) {
            return Map.of();
        }
        return dtoMap.values().stream()
                .map(this::toUser)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(UserDO::getId, Function.identity()));
    }

    private Map<Long, List<DeptDO>> fetchUserDeptsByEvent(Collection<Long> userIds, Long eventId) {
        if (eventId == null) {
            return Map.of();
        }
        return fetchUserDeptsByEvents(userIds, List.of(eventId));
    }

    private List<DeptDO> fetchUserDeptsByEvent(Long userId, Long eventId) {
        if (userId == null || eventId == null) {
            return List.of();
        }
        return fetchUserDeptsByEvents(List.of(userId), List.of(eventId))
                .getOrDefault(userId, List.of());
    }

    private Map<Long, List<DeptDO>> fetchUserDeptsByEvents(
            Collection<Long> userIds, Collection<Long> eventIds) {
        if (userIds == null || userIds.isEmpty() || eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        List<Long> distinctEventIds =
                eventIds.stream().filter(Objects::nonNull).distinct().toList();
        if (distinctEventIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<GroupDTO>> groupsByEvent =
                groupRpcService.getGroupsByEventIds(distinctEventIds);
        if (groupsByEvent == null || groupsByEvent.isEmpty()) {
            return Map.of();
        }

        Set<Long> userIdSet =
                userIds.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
        if (userIdSet.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<DeptDO>> result = new LinkedHashMap<>();
        Map<Long, Set<Long>> seenGroupIds = new LinkedHashMap<>();

        for (List<GroupDTO> groups : groupsByEvent.values()) {
            if (groups == null || groups.isEmpty()) {
                continue;
            }
            for (GroupDTO group : groups) {
                DeptDO dept = toDept(group);
                if (dept == null || dept.getId() == null) {
                    continue;
                }
                Set<Long> memberIds = extractGroupMemberIds(group);
                for (Long userId : memberIds) {
                    if (!userIdSet.contains(userId)) {
                        continue;
                    }
                    Set<Long> seenIds =
                            seenGroupIds.computeIfAbsent(userId, ignored -> new LinkedHashSet<>());
                    if (seenIds.add(dept.getId())) {
                        result.computeIfAbsent(userId, ignored -> new ArrayList<>()).add(dept);
                    }
                }
            }
        }

        return result;
    }

    private Set<Long> extractGroupMemberIds(GroupDTO group) {
        Set<Long> memberIds = new LinkedHashSet<>();
        if (group == null) {
            return memberIds;
        }
        if (group.getLeadUserId() != null) {
            memberIds.add(group.getLeadUserId());
        }
        if (group.getMembers() != null) {
            for (GroupMemberDTO member : group.getMembers()) {
                if (member != null && member.getUserId() != null) {
                    memberIds.add(member.getUserId());
                }
            }
        }
        return memberIds;
    }

    private DeptDO toDept(GroupDTO group) {
        if (group == null) {
            return null;
        }
        DeptDO dept = new DeptDO();
        dept.setId(group.getId());
        dept.setName(group.getName());
        dept.setSort(group.getSort());
        dept.setLeadUserId(group.getLeadUserId());
        dept.setRemark(group.getRemark());
        dept.setStatus(group.getStatus());
        dept.setEventId(group.getEventId());
        return dept;
    }

    private UserDO toUser(UserInfoDTO dto) {
        if (dto == null) {
            return null;
        }
        UserDO user = new UserDO();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setStatus(dto.getStatus());
        user.setTenantId(dto.getTenantId());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setCreateTime(dto.getCreateTime());
        user.setUpdateTime(dto.getUpdateTime());
        return user;
    }

    private Map<Long, List<TaskRespVO.AssignedUserVO.GroupVO>> buildCrudGroupsByUser(
            Collection<Long> userIds, Long eventId) {
        if (userIds == null || userIds.isEmpty() || eventId == null) {
            return Map.of();
        }
        Map<Long, List<DeptDO>> deptsByUserId = fetchUserDeptsByEvent(userIds, eventId);
        if (deptsByUserId.isEmpty()) {
            return Map.of();
        }
        return deptsByUserId.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry ->
                                        entry.getValue().stream()
                                                .map(this::toCrudGroupVO)
                                                .collect(Collectors.toList())));
    }

    private List<TaskRespVO.AssignedUserVO.GroupVO> resolveCrudGroups(
            Long userId,
            Long eventId,
            Map<Long, List<TaskRespVO.AssignedUserVO.GroupVO>> groupsByUserId) {
        if (userId == null || eventId == null) {
            return List.of();
        }
        if (groupsByUserId != null) {
            if (groupsByUserId.containsKey(userId)) {
                return groupsByUserId.getOrDefault(userId, List.of());
            }
            return List.of();
        }
        return fetchUserDeptsByEvent(userId, eventId).stream()
                .map(this::toCrudGroupVO)
                .collect(Collectors.toList());
    }

    private List<TaskRespVO.AssignedUserVO.GroupVO> resolveCrudGroups(Long userId, Long eventId) {
        return resolveCrudGroups(userId, eventId, null);
    }

    private TaskRespVO.AssignedUserVO.GroupVO toCrudGroupVO(DeptDO dept) {
        TaskRespVO.AssignedUserVO.GroupVO groupVO = new TaskRespVO.AssignedUserVO.GroupVO();
        groupVO.setId(dept.getId());
        groupVO.setName(dept.getName());
        return groupVO;
    }

    private List<TaskRespVO.AssignerUserVO.GroupVO> resolveAssignerGroups(
            Long userId, Long eventId) {
        if (userId == null || eventId == null) {
            return List.of();
        }
        return fetchUserDeptsByEvent(userId, eventId).stream()
                .map(
                        dept -> {
                            TaskRespVO.AssignerUserVO.GroupVO groupVO =
                                    new TaskRespVO.AssignerUserVO.GroupVO();
                            groupVO.setId(dept.getId());
                            groupVO.setName(dept.getName());
                            return groupVO;
                        })
                .collect(Collectors.toList());
    }

    private List<TaskRespVO.AssignerUserVO.GroupVO> resolveAssignerGroupsWithCache(
            UserDO user,
            Long eventId,
            Map<Long, Map<Long, List<TaskRespVO.AssignerUserVO.GroupVO>>> cache) {
        if (user == null || user.getId() == null || eventId == null) {
            return List.of();
        }
        Map<Long, List<TaskRespVO.AssignerUserVO.GroupVO>> groupsForEvent =
                cache.computeIfAbsent(eventId, ignored -> new LinkedHashMap<>());
        return groupsForEvent.computeIfAbsent(
                user.getId(), ignored -> resolveAssignerGroups(user.getId(), eventId));
    }

    private List<TaskRespVO.AssignedUserVO.GroupVO> resolveCrudGroupsWithCache(
            UserDO user,
            Long eventId,
            Map<Long, Map<Long, List<TaskRespVO.AssignedUserVO.GroupVO>>> cache,
            Collection<Long> usersToPrefetch) {
        if (user == null || user.getId() == null || eventId == null) {
            return List.of();
        }
        Map<Long, List<TaskRespVO.AssignedUserVO.GroupVO>> groupsForEvent =
                cache.computeIfAbsent(
                        eventId, ignored -> buildCrudGroupsByUser(usersToPrefetch, eventId));
        return groupsForEvent.getOrDefault(user.getId(), List.of());
    }

    private Map<Long, List<TasksRespVO.AssignedUserVO.GroupVO>> buildDashboardGroupsByUser(
            Collection<Long> userIds, Long eventId) {
        if (userIds == null || userIds.isEmpty() || eventId == null) {
            return Map.of();
        }
        Map<Long, List<DeptDO>> deptsByUserId = fetchUserDeptsByEvent(userIds, eventId);
        if (deptsByUserId.isEmpty()) {
            return Map.of();
        }
        return deptsByUserId.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry ->
                                        entry.getValue().stream()
                                                .map(this::toDashboardGroupVO)
                                                .collect(Collectors.toList())));
    }

    private List<TasksRespVO.AssignedUserVO.GroupVO> resolveDashboardGroups(
            Long userId,
            Long eventId,
            Map<Long, List<TasksRespVO.AssignedUserVO.GroupVO>> groupsByUserId) {
        if (userId == null || eventId == null) {
            return List.of();
        }
        if (groupsByUserId != null) {
            if (groupsByUserId.containsKey(userId)) {
                return groupsByUserId.getOrDefault(userId, List.of());
            }
            return List.of();
        }
        return fetchUserDeptsByEvent(userId, eventId).stream()
                .map(this::toDashboardGroupVO)
                .collect(Collectors.toList());
    }

    private List<TasksRespVO.AssignedUserVO.GroupVO> resolveDashboardGroups(
            Long userId, Long eventId) {
        return resolveDashboardGroups(userId, eventId, null);
    }

    private TasksRespVO.AssignedUserVO.GroupVO toDashboardGroupVO(DeptDO dept) {
        TasksRespVO.AssignedUserVO.GroupVO groupVO = new TasksRespVO.AssignedUserVO.GroupVO();
        groupVO.setId(dept.getId());
        groupVO.setName(dept.getName());
        groupVO.setEventId(dept.getEventId());
        groupVO.setLeadUserId(dept.getLeadUserId());
        groupVO.setRemark(dept.getRemark());
        return groupVO;
    }

    private List<TasksRespVO.AssignedUserVO.GroupVO> resolveDashboardGroupsWithCache(
            UserDO user,
            Long eventId,
            Map<Long, Map<Long, List<TasksRespVO.AssignedUserVO.GroupVO>>> cache,
            Collection<Long> usersToPrefetch) {
        if (user == null || user.getId() == null || eventId == null) {
            return List.of();
        }
        Map<Long, List<TasksRespVO.AssignedUserVO.GroupVO>> groupsForEvent =
                cache.computeIfAbsent(
                        eventId, ignored -> buildDashboardGroupsByUser(usersToPrefetch, eventId));
        return groupsForEvent.getOrDefault(user.getId(), List.of());
    }

    private TaskDashboardRespVO.MemberVO toMemberVO(UserDO member) {
        TaskDashboardRespVO.MemberVO memberVO = new TaskDashboardRespVO.MemberVO();
        memberVO.setId(member.getId());
        memberVO.setUsername(member.getUsername());
        memberVO.setEmail(member.getEmail());
        memberVO.setPhone(member.getPhone());
        memberVO.setStatus(member.getStatus());
        memberVO.setCreateTime(member.getCreateTime());
        memberVO.setUpdateTime(member.getUpdateTime());
        return memberVO;
    }

    private List<TaskDashboardRespVO.GroupVO> resolveMemberGroups(
            UserDO member, List<TaskDO> memberTasks) {
        if (member == null || member.getId() == null) {
            return List.of();
        }

        Collection<Long> eventIds =
                memberTasks.stream()
                        .map(TaskDO::getEventId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<Long, List<DeptDO>> deptsByUser =
                fetchUserDeptsByEvents(List.of(member.getId()), eventIds);
        List<DeptDO> depts = deptsByUser.getOrDefault(member.getId(), List.of());

        return depts.stream()
                .map(
                        dept -> {
                            TaskDashboardRespVO.GroupVO groupVO = new TaskDashboardRespVO.GroupVO();
                            groupVO.setId(dept.getId());
                            groupVO.setName(dept.getName());
                            groupVO.setSort(dept.getSort());
                            groupVO.setLeadUserId(dept.getLeadUserId());
                            groupVO.setRemark(dept.getRemark());
                            groupVO.setStatus(dept.getStatus());
                            groupVO.setEvent(toGroupEvent(dept.getEventId()));
                            return groupVO;
                        })
                .collect(Collectors.toList());
    }

    private TaskDashboardRespVO.GroupVO.EventVO toGroupEvent(Long eventId) {
        if (eventId == null) {
            return null;
        }

        EventRespDTO event = eventRpcService.getEvent(eventId);
        if (event == null) {
            return null;
        }

        TaskDashboardRespVO.GroupVO.EventVO eventVO = new TaskDashboardRespVO.GroupVO.EventVO();
        eventVO.setId(event.getId());
        eventVO.setName(event.getName());
        eventVO.setDescription(event.getDescription());
        eventVO.setLocation(event.getLocation());
        eventVO.setStatus(event.getStatus());
        eventVO.setStartTime(event.getStartTime());
        eventVO.setEndTime(event.getEndTime());
        eventVO.setRemark(event.getRemark());
        return eventVO;
    }
}
