package nus.edu.u.user.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.common.core.domain.CommonResult;
import nus.edu.u.common.enums.ErrorCodeConstants;

/**
 * Sentinel 统一限流处理器
 *
 * @author Fan Yazhuoting
 * @date 2025-10-17
 */
@Slf4j
public class SentinelBlockHandler {

    /**
     * 通用限流处理
     */
    public static CommonResult<?> handleBlock(BlockException ex) {
        log.warn("触发限流保护: {}", ex.getRule());
        return CommonResult.error(
                ErrorCodeConstants.TOO_MANY_REQUESTS.getCode(),
                "系统繁忙，请稍后重试"
        );
    }

    /**
     * 登录接口限流处理
     */
    public static CommonResult<?> handleLoginBlock(BlockException ex) {
        log.warn("登录接口触发限流: {}", ex.getMessage());
        return CommonResult.error(
                ErrorCodeConstants.TOO_MANY_REQUESTS.getCode(),
                "登录请求过于频繁，请稍后重试"
        );
    }

    /**
     * 注册接口限流处理
     */
    public static CommonResult<?> handleRegisterBlock(BlockException ex) {
        log.warn("注册接口触发限流: {}", ex.getMessage());
        return CommonResult.error(
                ErrorCodeConstants.TOO_MANY_REQUESTS.getCode(),
                "注册请求过于频繁，请稍后重试"
        );
    }

    /**
     * 创建操作限流处理
     */
    public static CommonResult<?> handleCreateBlock(BlockException ex) {
        log.warn("创建操作触发限流: {}", ex.getMessage());
        return CommonResult.error(
                ErrorCodeConstants.TOO_MANY_REQUESTS.getCode(),
                "创建操作过于频繁，请稍后重试"
        );
    }

    /**
     * 更新操作限流处理
     */
    public static CommonResult<?> handleUpdateBlock(BlockException ex) {
        log.warn("更新操作触发限流: {}", ex.getMessage());
        return CommonResult.error(
                ErrorCodeConstants.TOO_MANY_REQUESTS.getCode(),
                "更新操作过于频繁，请稍后重试"
        );
    }

    /**
     * 删除操作限流处理
     */
    public static CommonResult<?> handleDeleteBlock(BlockException ex) {
        log.warn("删除操作触发限流: {}", ex.getMessage());
        return CommonResult.error(
                ErrorCodeConstants.TOO_MANY_REQUESTS.getCode(),
                "删除操作过于频繁，请稍后重试"
        );
    }

    /**
     * 批量操作限流处理
     */
    public static CommonResult<?> handleBulkBlock(BlockException ex) {
        log.warn("批量操作触发限流: {}", ex.getMessage());
        return CommonResult.error(
                ErrorCodeConstants.TOO_MANY_REQUESTS.getCode(),
                "批量操作请求过多，请稍后重试"
        );
    }

    /**
     * 查询列表限流处理
     */
    public static CommonResult<?> handleListBlock(BlockException ex) {
        log.warn("查询列表触发限流: {}", ex.getMessage());
        return CommonResult.error(
                ErrorCodeConstants.TOO_MANY_REQUESTS.getCode(),
                "查询过于频繁，请稍后重试"
        );
    }
}