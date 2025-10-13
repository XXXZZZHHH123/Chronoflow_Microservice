package nus.edu.u.event.interfaces.rest;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nus.edu.u.common.core.domain.CommonResult;
import nus.edu.u.event.application.EventApplicationService;
import nus.edu.u.event.interfaces.rest.dto.EventCreateReqVO;
import nus.edu.u.event.interfaces.rest.dto.EventGroupRespVO;
import nus.edu.u.event.interfaces.rest.dto.EventRespVO;
import nus.edu.u.event.interfaces.rest.dto.EventUpdateReqVO;
import nus.edu.u.event.interfaces.rest.dto.UpdateEventRespVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.dev33.satoken.stp.StpUtil;

@RestController
@RequestMapping("/events")
@Validated
@RequiredArgsConstructor
public class EventController {

    private final EventApplicationService eventApplicationService;

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

    @PatchMapping("/{id}")
    public CommonResult<UpdateEventRespVO> update(
            @PathVariable("id") Long id, @Valid @RequestBody EventUpdateReqVO request) {
        UpdateEventRespVO respVO = eventApplicationService.updateEvent(id, request);
        return CommonResult.success(respVO);
    }

    @DeleteMapping("/{id}")
    public CommonResult<Boolean> delete(@PathVariable("id") Long id) {
        return CommonResult.success(eventApplicationService.deleteEvent(id));
    }

    @PatchMapping("/{id}/restore")
    public CommonResult<Boolean> restore(@PathVariable("id") Long id) {
        return CommonResult.success(eventApplicationService.restoreEvent(id));
    }

    @GetMapping("/{id}/assignable-groups")
    public CommonResult<List<EventGroupRespVO>> assignableGroups(@PathVariable("id") Long eventId) {
        return CommonResult.success(eventApplicationService.findAssignableGroups(eventId));
    }
}
