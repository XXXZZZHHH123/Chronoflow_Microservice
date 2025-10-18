package nus.edu.u.framework.sentinel;

import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.common.core.domain.CommonResult;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Sentinel Global Exception Handler
 */
@RestControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class SentinelExceptionHandler implements BlockExceptionHandler {

    @ExceptionHandler(FlowException.class)
    public CommonResult<?> handleFlowException(FlowException ex, HttpServletRequest request) {
        String resource = ex.getRule() != null ? ex.getRule().getResource() : "unknown";
        log.warn("Flow control triggered - URI: {}, Resource: {}, Threshold: {}",
                request.getRequestURI(), resource,
                ex.getRule() != null ? ex.getRule().getCount() : "N/A");

        return CommonResult.error(429, "Too many requests, please try again later");
    }

    @ExceptionHandler(DegradeException.class)
    public CommonResult<?> handleDegradeException(DegradeException ex, HttpServletRequest request) {
        String resource = ex.getRule() != null ? ex.getRule().getResource() : "unknown";
        log.error("Service degradation triggered - URI: {}, Resource: {}",
                request.getRequestURI(), resource);

        return CommonResult.error(503, "Service temporarily unavailable, please try again later");
    }

    @ExceptionHandler(ParamFlowException.class)
    public CommonResult<?> handleParamFlowException(ParamFlowException ex, HttpServletRequest request) {
        log.warn("Hot parameter flow control triggered - URI: {}", request.getRequestURI());
        return CommonResult.error(429, "This operation is too frequent, please try again later");
    }

    @ExceptionHandler(AuthorityException.class)
    public CommonResult<?> handleAuthorityException(AuthorityException ex, HttpServletRequest request) {
        log.warn("Authorization rule triggered - URI: {}", request.getRequestURI());
        return CommonResult.error(403, "Access denied");
    }

    @ExceptionHandler(BlockException.class)
    public CommonResult<?> handleBlockException(BlockException ex, HttpServletRequest request) {
        log.warn("Request blocked by Sentinel - URI: {}, Exception: {}",
                request.getRequestURI(), ex.getClass().getSimpleName());

        return CommonResult.error(429, "System busy, please try again later");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, String resourceName, BlockException e) throws Exception {
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");

        String json = "{\"code\":429,\"msg\":\"Too many requests, please try again later\"}";
        response.getWriter().write(json);
    }
}