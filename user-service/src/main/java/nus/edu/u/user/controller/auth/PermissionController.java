package nus.edu.u.user.controller.auth;

import static nus.edu.u.common.core.domain.CommonResult.success;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.common.core.domain.CommonResult;
import nus.edu.u.user.domain.vo.permission.PermissionReqVO;
import nus.edu.u.user.domain.vo.permission.PermissionRespVO;
import nus.edu.u.user.handler.SentinelBlockHandler;
import nus.edu.u.user.handler.SentinelFallbackHandler;
import nus.edu.u.user.service.permission.PermissionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author Lu Shuwen
 * @date 2025-09-29
 */
@RestController
@RequestMapping("/users/permissions")
@Validated
@Slf4j
public class PermissionController {

    @Resource private PermissionService permissionService;

    @SaCheckRole(
            value = {"ORGANIZER", "ADMIN"},
            mode = SaMode.OR)
    @GetMapping
    @SentinelResource(
            value = "/users/permissions",
            blockHandlerClass = SentinelBlockHandler.class,
            blockHandler = "handleListBlock",
            fallbackClass = SentinelFallbackHandler.class,
            fallback = "handleListFallback"
    )
    public CommonResult<List<PermissionRespVO>> list() {
        return success(permissionService.listPermissions());
    }

    @SaCheckRole("ADMIN")
    @PostMapping
    @SentinelResource(
            value = "POST:/users/permissions",
            blockHandlerClass = SentinelBlockHandler.class,
            blockHandler = "handleCreateBlock",
            fallbackClass = SentinelFallbackHandler.class,
            fallback = "handleCreateFallback"
    )
    public CommonResult<Long> create(@RequestBody @Valid PermissionReqVO reqVO) {
        return success(permissionService.createPermission(reqVO));
    }

    @SaCheckRole(
            value = {"ORGANIZER", "ADMIN"},
            mode = SaMode.OR)
    @GetMapping("/{id}")
    @SentinelResource(
            value = "GET:/users/permissions/{id}",
            blockHandlerClass = SentinelBlockHandler.class,
            blockHandler = "handleBlock",
            fallbackClass = SentinelFallbackHandler.class,
            fallback = "handleFallback"
    )
    public CommonResult<PermissionRespVO> getPermission(@PathVariable("id") Long id) {
        return success(permissionService.getPermission(id));
    }

    @SaCheckRole("ADMIN")
    @PatchMapping("/{id}")
    @SentinelResource(
            value = "PATCH:/users/permissions/{id}",
            blockHandlerClass = SentinelBlockHandler.class,
            blockHandler = "handleUpdateBlock",
            fallbackClass = SentinelFallbackHandler.class,
            fallback = "handleUpdateFallback"
    )
    public CommonResult<PermissionRespVO> update(
            @PathVariable("id") Long id, @RequestBody @Valid PermissionReqVO reqVO) {
        return success(permissionService.updatePermission(id, reqVO));
    }

    @SaCheckRole("ADMIN")
    @DeleteMapping("/{id}")
    @SentinelResource(
            value = "DELETE:/users/permissions/{id}",
            blockHandlerClass = SentinelBlockHandler.class,
            blockHandler = "handleDeleteBlock",
            fallbackClass = SentinelFallbackHandler.class,
            fallback = "handleFallback"
    )
    public CommonResult<Boolean> delete(@PathVariable("id") Long id) {
        return success(permissionService.deletePermission(id));
    }
}