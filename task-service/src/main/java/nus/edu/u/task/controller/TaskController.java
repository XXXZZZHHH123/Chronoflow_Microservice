package nus.edu.u.task.controller;

import static nus.edu.u.common.constant.PermissionConstants.CREATE_TASK;
import static nus.edu.u.common.constant.PermissionConstants.DELETE_TASK;
import static nus.edu.u.common.constant.PermissionConstants.QUERY_TASK;
import static nus.edu.u.common.constant.PermissionConstants.UPDATE_TASK;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nus.edu.u.common.core.domain.CommonResult;
import nus.edu.u.task.domain.vo.task.TaskCreateReqVO;
import nus.edu.u.task.domain.vo.task.TaskDashboardRespVO;
import nus.edu.u.task.domain.vo.task.TaskRespVO;
import nus.edu.u.task.domain.vo.task.TaskUpdateReqVO;
import nus.edu.u.task.domain.vo.taskLog.TaskLogRespVO;
import nus.edu.u.task.service.TaskApplicationService;
import nus.edu.u.task.service.TaskLogApplicationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
@Validated
@RequiredArgsConstructor
public class TaskController {

    private final TaskApplicationService taskApplicationService;
    private final TaskLogApplicationService taskLogApplicationService;

    @SaCheckPermission(CREATE_TASK)
    @PostMapping("{eventId}")
    public CommonResult<TaskRespVO> create(
            @PathVariable("eventId") Long eventId, @Valid @ModelAttribute TaskCreateReqVO request) {
        TaskRespVO resp = taskApplicationService.createTask(eventId, request);
        return CommonResult.success(resp);
    }

    @GetMapping("{eventId}/{taskId}")
    public CommonResult<TaskRespVO> getTask(
            @PathVariable("eventId") Long eventId, @PathVariable("taskId") Long taskId) {
        TaskRespVO resp = taskApplicationService.getTask(eventId, taskId);
        return CommonResult.success(resp);
    }

    @GetMapping("/{eventId}")
    public CommonResult<List<TaskRespVO>> listByEvent(@PathVariable("eventId") Long eventId) {
        List<TaskRespVO> resp = taskApplicationService.listTasksByEvent(eventId);
        return CommonResult.success(resp);
    }

    @GetMapping("/dashboard")
    public CommonResult<TaskDashboardRespVO> dashboard() {
        Long memberId = StpUtil.getLoginIdAsLong();
        TaskDashboardRespVO resp = taskApplicationService.getByMemberId(memberId);
        return CommonResult.success(resp);
    }

    @PatchMapping("{eventId}/{taskId}")
    public CommonResult<TaskRespVO> update(
            @PathVariable("eventId") Long eventId,
            @PathVariable("taskId") Long taskId,
            @Valid @ModelAttribute TaskUpdateReqVO request) {
        TaskRespVO resp =
                taskApplicationService.updateTask(eventId, taskId, request, request.getType());
        return CommonResult.success(resp);
    }

    @SaCheckPermission(DELETE_TASK)
    @DeleteMapping("/{eventId}/{taskId}")
    public CommonResult<Boolean> delete(
            @PathVariable("eventId") Long eventId, @PathVariable("taskId") Long taskId) {
        taskApplicationService.deleteTask(eventId, taskId);
        return CommonResult.success(Boolean.TRUE);
    }

    @SaCheckPermission(QUERY_TASK)
    @GetMapping("/{eventId}/log/{taskId}")
    public CommonResult<List<TaskLogRespVO>> logs(@PathVariable("taskId") Long taskId) {
        List<TaskLogRespVO> resp = taskLogApplicationService.getTaskLog(taskId);
        return CommonResult.success(resp);
    }
}
