package nus.edu.u.framework.sentinel;

import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.common.core.domain.CommonResult;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SentinelExceptionHandler implements BlockExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            String resourceName,
            BlockException ex)
            throws Exception {

        CommonResult<?> result = buildResponse(ex);

        log.warn(
                "[Sentinel] {} blocked - Resource: {}, URI: {} {}",
                ex.getClass().getSimpleName(),
                resourceName,
                request.getMethod(),
                request.getRequestURI());

        response.setStatus(result.getCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    private CommonResult<?> buildResponse(BlockException ex) {
        if (ex instanceof FlowException || ex instanceof ParamFlowException) {
            return CommonResult.error(429, "Too many requests, please try again later");
        } else if (ex instanceof DegradeException || ex instanceof SystemBlockException) {
            return CommonResult.error(503, "Service temporarily unavailable");
        } else if (ex instanceof AuthorityException) {
            return CommonResult.error(403, "Access denied");
        }
        return CommonResult.error(429, "System busy, please try again later");
    }
}
