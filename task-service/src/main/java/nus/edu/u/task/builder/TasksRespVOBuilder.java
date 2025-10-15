package nus.edu.u.task.builder;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import nus.edu.u.shared.rpc.events.EventRespDTO;
import nus.edu.u.task.convert.TaskConvert;
import nus.edu.u.task.domain.dataobject.task.TaskDO;
import nus.edu.u.task.domain.dataobject.user.UserDO;
import nus.edu.u.task.domain.vo.task.TasksRespVO;

/** Builder dedicated to {@link TasksRespVO} for dashboard aggregations. */
public final class TasksRespVOBuilder {

    private final TasksRespVO response;
    private final TaskDO task;

    private EventRespDTO event;
    private Supplier<EventRespDTO> eventSupplier = () -> null;
    private Function<EventRespDTO, TasksRespVO.EventVO> eventMapper = this::toEventSummary;

    private UserDO assignee;
    private Supplier<UserDO> assigneeSupplier = () -> null;
    private Function<UserDO, List<TasksRespVO.AssignedUserVO.GroupVO>> groupResolver =
            user -> Collections.emptyList();

    private TasksRespVOBuilder(TaskDO task) {
        this.task = task;
        this.response = TaskConvert.INSTANCE.toTasksRespVO(task);
    }

    public static TasksRespVOBuilder from(TaskDO task) {
        return new TasksRespVOBuilder(task);
    }

    public TasksRespVOBuilder withEvent(EventRespDTO event) {
        this.event = event;
        return this;
    }

    public TasksRespVOBuilder withEventSupplier(Supplier<EventRespDTO> supplier) {
        if (supplier != null) {
            this.eventSupplier = supplier;
        }
        return this;
    }

    public TasksRespVOBuilder withEventMapper(
            Function<EventRespDTO, TasksRespVO.EventVO> mapper) {
        if (mapper != null) {
            this.eventMapper = mapper;
        }
        return this;
    }

    public TasksRespVOBuilder withAssignee(UserDO assignee) {
        this.assignee = assignee;
        return this;
    }

    public TasksRespVOBuilder withAssigneeSupplier(Supplier<UserDO> supplier) {
        if (supplier != null) {
            this.assigneeSupplier = supplier;
        }
        return this;
    }

    public TasksRespVOBuilder withGroupResolver(
            Function<UserDO, List<TasksRespVO.AssignedUserVO.GroupVO>> resolver) {
        if (resolver != null) {
            this.groupResolver = resolver;
        }
        return this;
    }

    public TasksRespVO build() {
        EventRespDTO eventData = event != null ? event : eventSupplier.get();
        response.setEvent(eventMapper.apply(eventData));

        UserDO userData = assignee != null ? assignee : assigneeSupplier.get();
        if (userData != null) {
            response.setAssignedUser(toAssignedUser(userData));
        } else {
            response.setAssignedUser(null);
        }
        return response;
    }

    private TasksRespVO.EventVO toEventSummary(EventRespDTO event) {
        if (event == null) {
            return null;
        }
        TasksRespVO.EventVO eventVO = new TasksRespVO.EventVO();
        eventVO.setId(event.getId());
        eventVO.setName(event.getName());
        eventVO.setDescription(event.getDescription());
        eventVO.setOrganizerId(event.getOrganizerId());
        eventVO.setLocation(event.getLocation());
        eventVO.setStatus(event.getStatus());
        eventVO.setStartTime(event.getStartTime());
        eventVO.setEndTime(event.getEndTime());
        eventVO.setRemark(event.getRemark());
        return eventVO;
    }

    private TasksRespVO.AssignedUserVO toAssignedUser(UserDO user) {
        TasksRespVO.AssignedUserVO assignedUserVO = new TasksRespVO.AssignedUserVO();
        assignedUserVO.setId(user.getId());
        assignedUserVO.setName(user.getUsername());
        assignedUserVO.setEmail(user.getEmail());
        assignedUserVO.setPhone(user.getPhone());
        List<TasksRespVO.AssignedUserVO.GroupVO> groups = groupResolver.apply(user);
        assignedUserVO.setGroups(groups != null ? groups : Collections.emptyList());
        return assignedUserVO;
    }
}
