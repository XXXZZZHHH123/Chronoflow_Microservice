package nus.edu.u.user.controller.user;

import static nus.edu.u.common.core.domain.CommonResult.error;
import static nus.edu.u.common.core.domain.CommonResult.success;
import static nus.edu.u.common.enums.ErrorCodeConstants.REG_FAIL;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.common.core.domain.CommonResult;
import nus.edu.u.user.domain.vo.reg.RegMemberReqVO;
import nus.edu.u.user.domain.vo.reg.RegOrganizerReqVO;
import nus.edu.u.user.domain.vo.reg.RegSearchReqVO;
import nus.edu.u.user.domain.vo.reg.RegSearchRespVO;
import nus.edu.u.user.handler.SentinelBlockHandler;
import nus.edu.u.user.handler.SentinelFallbackHandler;
import nus.edu.u.user.service.user.RegService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Registration controller
 *
 * @author Lu Shuwen
 * @date 2025-09-10
 */
@RestController
@RequestMapping("/users/reg")
@Validated
@Slf4j
public class RegController {

    @Resource private RegService regService;

    @PostMapping("/search")
    @SentinelResource(
            value = "/users/reg/search",
            blockHandlerClass = SentinelBlockHandler.class,
            blockHandler = "handleBlock",
            fallbackClass = SentinelFallbackHandler.class,
            fallback = "handleFallback"
    )
    public CommonResult<RegSearchRespVO> search(@RequestBody @Valid RegSearchReqVO regSearchReqVO) {
        return success(regService.search(regSearchReqVO));
    }

    @PostMapping("/member")
    @SentinelResource(
            value = "/users/reg/member",
            blockHandlerClass = SentinelBlockHandler.class,
            blockHandler = "handleRegisterBlock",
            fallbackClass = SentinelFallbackHandler.class,
            fallback = "handleRegisterFallback"
    )

    public CommonResult<Boolean> registerAsMember(
            @RequestBody @Valid RegMemberReqVO regMemberReqVO) {
        boolean isSuccess = regService.registerAsMember(regMemberReqVO);
        return isSuccess ? success(true) : error(REG_FAIL);
    }

    @PostMapping("/organizer")
    @SentinelResource(
            value = "/users/reg/organizer",
            blockHandlerClass = SentinelBlockHandler.class,
            blockHandler = "handleRegisterBlock",
            fallbackClass = SentinelFallbackHandler.class,
            fallback = "handleRegisterFallback"
    )
    public CommonResult<Boolean> registerAsOrganizer(
            @RequestBody @Valid RegOrganizerReqVO regOrganizerReqVO) {
        boolean isSuccess = regService.registerAsOrganizer(regOrganizerReqVO);
        return isSuccess ? success(true) : error(REG_FAIL);
    }
}
