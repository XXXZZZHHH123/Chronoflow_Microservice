package nus.edu.u.user.controller.user;

import static nus.edu.u.common.constant.PermissionConstants.*;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.common.core.domain.CommonResult;
import nus.edu.u.user.convert.UserConvert;
import nus.edu.u.user.domain.dataobject.user.UserDO;
import nus.edu.u.user.domain.dto.CreateUserDTO;
import nus.edu.u.user.domain.dto.UpdateUserDTO;
import nus.edu.u.user.domain.vo.user.*;
import nus.edu.u.user.handler.SentinelBlockHandler;
import nus.edu.u.user.handler.SentinelFallbackHandler;
import nus.edu.u.user.service.excel.ExcelService;
import nus.edu.u.user.service.user.UserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users/organizer")
@Validated
@Slf4j
public class OrganizerController {
    @Resource private UserService userService;

    @Resource private UserConvert userConvert;

    @Resource private ExcelService excelService;

    @SaCheckPermission(CREATE_MEMBER)
    @PostMapping("/create/user")
    @SentinelResource(
            value = "POST:/users/organizer/create/user",
            blockHandlerClass = SentinelBlockHandler.class,
            blockHandler = "handleCreateBlock",
            fallbackClass = SentinelFallbackHandler.class,
            fallback = "handleCreateFallback"
    )
    public CommonResult<Long> createUserForOrganizer(@Valid @RequestBody CreateUserReqVO req) {
        CreateUserDTO dto = userConvert.toDTO(req);
        Long userId = userService.createUserWithRoleIds(dto);
        return CommonResult.success(userId);
    }

    @SaCheckPermission(UPDATE_MEMBER)
    @PatchMapping("/update/user/{id}")
    @SentinelResource(
            value = "PATCH:/users/organizer/update/user/{id}",
            blockHandlerClass = SentinelBlockHandler.class,
            blockHandler = "handleUpdateBlock",
            fallbackClass = SentinelFallbackHandler.class,
            fallback = "handleUpdateFallback"
    )
    public CommonResult<UpdateUserRespVO> updateUserForOrganizer(
            @PathVariable("id") Long id, @Valid @RequestBody UpdateUserReqVO vo) {
        UpdateUserDTO dto = userConvert.toDTO(vo);
        dto.setId(id);

        UserDO updated = userService.updateUserWithRoleIds(dto);
        // Query the user's role ID
        List<Long> roleIds = userService.getAliveRoleIdsByUserId(updated.getId());

        UpdateUserRespVO respVO = userConvert.toUpdateUserRespVO(updated);
        respVO.setRoleIds(roleIds);
        return CommonResult.success(respVO);
    }

    @SaCheckPermission(DELETE_MEMBER)
    @DeleteMapping("/delete/user/{id}")
    @SentinelResource(
            value = "DELETE:/users/organizer/delete/user/{id}",
            blockHandlerClass = SentinelBlockHandler.class,
            blockHandler = "handleDeleteBlock",
            fallbackClass = SentinelFallbackHandler.class,
            fallback = "handleFallback"
    )
    public CommonResult<Boolean> softDeleteUser(@PathVariable("id") Long id) {
        userService.softDeleteUser(id);
        return CommonResult.success(Boolean.TRUE);
    }

    @SaCheckPermission(RESTORE_MEMBER)
    @PatchMapping("/restore/user/{id}")
    @SentinelResource(
            value = "PATCH:/users/organizer/restore/user/{id}",
            blockHandlerClass = SentinelBlockHandler.class,
            blockHandler = "handleUpdateBlock",
            fallbackClass = SentinelFallbackHandler.class,
            fallback = "handleFallback"
    )
    public CommonResult<Boolean> restoreUser(@PathVariable("id") Long id) {
        userService.restoreUser(id);
        return CommonResult.success(Boolean.TRUE);
    }

    @SaCheckPermission(DISABLE_MEMBER)
    @PatchMapping("/disable/user/{id}")
    @SentinelResource(
            value = "PATCH:/users/organizer/disable/user/{id}",
            blockHandlerClass = SentinelBlockHandler.class,
            blockHandler = "handleUpdateBlock",
            fallbackClass = SentinelFallbackHandler.class,
            fallback = "handleFallback"
    )
    public CommonResult<Boolean> disableUser(@PathVariable("id") Long id) {
        userService.disableUser(id);
        return CommonResult.success(true);
    }

    @SaCheckPermission(ENABLE_MEMBER)
    @PatchMapping("/enable/user/{id}")
    @SentinelResource(
            value = "PATCH:/users/organizer/enable/user/{id}",
            blockHandlerClass = SentinelBlockHandler.class,
            blockHandler = "handleUpdateBlock",
            fallbackClass = SentinelFallbackHandler.class,
            fallback = "handleFallback"
    )
    public CommonResult<Boolean> enableUser(@PathVariable("id") Long id) {
        userService.enableUser(id);
        return CommonResult.success(true);
    }

    @GetMapping("/users")
    @SentinelResource(
            value = "/users/organizer/users",
            blockHandlerClass = SentinelBlockHandler.class,
            blockHandler = "handleListBlock",
            fallbackClass = SentinelFallbackHandler.class,
            fallback = "handleListFallback"
    )
    public CommonResult<List<UserProfileRespVO>> getAllUserProfiles() {
        return CommonResult.success(userService.getAllUserProfiles());
    }

    @SaCheckPermission(CREATE_MEMBER)
    @PostMapping("/users/bulk-upsert")
    @SentinelResource(
            value = "POST:/users/organizer/users/bulk-upsert",
            blockHandlerClass = SentinelBlockHandler.class,
            blockHandler = "handleBulkBlock",
            fallbackClass = SentinelFallbackHandler.class,
            fallback = "handleBulkFallback"
    )
    public CommonResult<BulkUpsertUsersRespVO> bulkUpsertUsers(
            @RequestParam("file") MultipartFile file) throws IOException {

        List<CreateUserDTO> rows = excelService.parseCreateOrUpdateRows(file);
        BulkUpsertUsersRespVO result = userService.bulkUpsertUsers(rows);
        return CommonResult.success(result);
    }
}
