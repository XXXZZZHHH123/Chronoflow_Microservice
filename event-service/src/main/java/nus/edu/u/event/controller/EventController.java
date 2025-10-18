package nus.edu.u.event.controller;

import static nus.edu.u.common.constant.PermissionConstants.*;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nus.edu.u.common.core.domain.CommonResult;
import nus.edu.u.event.domain.dto.event.EventCreateReqVO;
import nus.edu.u.event.domain.dto.event.EventGroupRespVO;
import nus.edu.u.event.domain.dto.event.EventRespVO;
import nus.edu.u.event.domain.dto.event.EventUpdateReqVO;
import nus.edu.u.event.domain.dto.event.UpdateEventRespVO;
import nus.edu.u.event.service.EventApplicationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
@Validated
@RequiredArgsConstructor
public class EventController {

    private final EventApplicationService eventApplicationService;

    @SaCheckPermission(CREATE_EVENT)
    @PostMapping
    public CommonResult<EventRespVO> create(@Valid @RequestBody EventCreateReqVO request) {
        Long organizerId = StpUtil.getLoginIdAsLong();
        request.setOrganizerId(organizerId);
        EventRespVO resp = eventApplicationService.createEvent(request);
        return CommonResult.success(resp);
    }

    @GetMapping("/{id}")
    public CommonResult<EventRespVO> getById(@PathVariable("id") Long id) {
        return CommonResult.success(eventApplicationService.getEvent(id));
    }

    @GetMapping
    public CommonResult<List<EventRespVO>> getByOrganizer() {
        Long organizerId = StpUtil.getLoginIdAsLong();
        return CommonResult.success(eventApplicationService.getEventsByOrganizer(organizerId));
    }

    @SaCheckPermission(UPDATE_EVENT)
    @PatchMapping("/{id}")
    public CommonResult<UpdateEventRespVO> update(
            @PathVariable("id") Long id, @Valid @RequestBody EventUpdateReqVO request) {
        UpdateEventRespVO respVO = eventApplicationService.updateEvent(id, request);
        return CommonResult.success(respVO);
    }

    @SaCheckPermission(DELETE_EVENT)
    @DeleteMapping("/{id}")
    public CommonResult<Boolean> delete(@PathVariable("id") Long id) {
        return CommonResult.success(eventApplicationService.deleteEvent(id));
    }

    @SaCheckPermission(UPDATE_EVENT)
    @PatchMapping("/{id}/restore")
    public CommonResult<Boolean> restore(@PathVariable("id") Long id) {
        return CommonResult.success(eventApplicationService.restoreEvent(id));
    }

    @SaCheckPermission(ASSIGN_TASK)
    @GetMapping("/{id}/assignable-groups")
    public CommonResult<List<EventGroupRespVO>> assignableGroups(@PathVariable("id") Long eventId) {
        return CommonResult.success(eventApplicationService.findAssignableGroups(eventId));
    }
}
