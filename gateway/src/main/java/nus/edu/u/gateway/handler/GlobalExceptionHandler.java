package nus.edu.u.gateway.handler;

import static nus.edu.u.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;

import lombok.extern.slf4j.Slf4j;
import nus.edu.u.common.core.domain.CommonResult;
import nus.edu.u.gateway.util.WebFrameworkUtils;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author Lu Shuwen
 * @date 2025-10-05
 */
@Component
@Slf4j
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // 已经 commit，则直接返回异常
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // 转换成 CommonResult
        CommonResult<?> result;
        if (ex instanceof ResponseStatusException) {
            result = responseStatusExceptionHandler(exchange, (ResponseStatusException) ex);
        } else {
            result = defaultExceptionHandler(exchange, ex);
        }

        // 返回给前端
        return WebFrameworkUtils.writeJSON(exchange, result);
    }

    /** Handling the ResponseStatusException thrown by Spring Cloud Gateway by default */
    private CommonResult<?> responseStatusExceptionHandler(
            ServerWebExchange exchange, ResponseStatusException ex) {
        ServerHttpRequest request = exchange.getRequest();
        log.error(
                "[responseStatusExceptionHandler][uri({}/{}) Error]",
                request.getURI(),
                request.getMethod(),
                ex);
        return CommonResult.error(ex.getStatusCode().value(), ex.getReason());
    }

    /** Handle system exceptions and handle everything */
    @ExceptionHandler(value = Exception.class)
    public CommonResult<?> defaultExceptionHandler(ServerWebExchange exchange, Throwable ex) {
        ServerHttpRequest request = exchange.getRequest();
        log.error(
                "[defaultExceptionHandler][uri({}/{}) Error]",
                request.getURI(),
                request.getMethod(),
                ex);
        // 返回 ERROR CommonResult
        return CommonResult.error(INTERNAL_SERVER_ERROR.getCode(), INTERNAL_SERVER_ERROR.getMsg());
    }
}
