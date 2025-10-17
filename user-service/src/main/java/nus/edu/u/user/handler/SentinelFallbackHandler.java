package nus.edu.u.user.handler;

import lombok.extern.slf4j.Slf4j;
import nus.edu.u.common.core.domain.CommonResult;
import nus.edu.u.common.enums.ErrorCodeConstants;

/**
 * Sentinel 统一降级处理器
 *
 * @author Lu Shuwen
 * @date 2025-10-17
 */
@Slf4j
public class SentinelFallbackHandler {

    /**
     * 通用降级处理
     */
    public static CommonResult<?> handleFallback(Throwable ex) {
        log.error("触发服务降级: {}", ex.getMessage(), ex);
        return CommonResult.error(
                ErrorCodeConstants.SERVICE_DEGRADED.getCode(),
                "服务暂时不可用，请稍后重试"
        );
    }

    /**
     * 登录接口降级处理
     */
    public static CommonResult<?> handleLoginFallback(Throwable ex) {
        log.error("登录服务降级: {}", ex.getMessage(), ex);
        return CommonResult.error(
                ErrorCodeConstants.SERVICE_DEGRADED.getCode(),
                "登录服务暂时不可用，请稍后重试"
        );
    }

    /**
     * 注册接口降级处理
     */
    public static CommonResult<?> handleRegisterFallback(Throwable ex) {
        log.error("注册服务降级: {}", ex.getMessage(), ex);
        return CommonResult.error(
                ErrorCodeConstants.SERVICE_DEGRADED.getCode(),
                "注册服务暂时不可用，请稍后重试"
        );
    }

    /**
     * 创建操作降级处理
     */
    public static CommonResult<?> handleCreateFallback(Throwable ex) {
        log.error("创建操作服务降级: {}", ex.getMessage(), ex);
        return CommonResult.error(
                ErrorCodeConstants.SERVICE_DEGRADED.getCode(),
                "创建操作暂时不可用，请稍后重试"
        );
    }

    /**
     * 更新操作降级处理
     */
    public static CommonResult<?> handleUpdateFallback(Throwable ex) {
        log.error("更新操作服务降级: {}", ex.getMessage(), ex);
        return CommonResult.error(
                ErrorCodeConstants.SERVICE_DEGRADED.getCode(),
                "更新操作暂时不可用，请稍后重试"
        );
    }

    /**
     * 查询列表降级处理 - 返回空列表
     */
    public static CommonResult<?> handleListFallback(Throwable ex) {
        log.error("查询列表服务降级: {}", ex.getMessage(), ex);
        return CommonResult.success(java.util.Collections.emptyList());
    }

    /**
     * 批量操作降级处理
     */
    public static CommonResult<?> handleBulkFallback(Throwable ex) {
        log.error("批量操作服务降级: {}", ex.getMessage(), ex);
        return CommonResult.error(
                ErrorCodeConstants.SERVICE_DEGRADED.getCode(),
                "批量操作暂时不可用，请稍后重试"
        );
    }
}