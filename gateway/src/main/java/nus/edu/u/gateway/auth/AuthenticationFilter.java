package nus.edu.u.gateway.auth;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import nus.edu.u.gateway.config.AuthConfig;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author Lu Shuwen
 * @date 2025-10-05
 */
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final AuthConfig authConfig;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public static final String LOGIN_ID_HEADER = "Login-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String path = request.getURI().getPath();

        // White list
        for (String pattern : authConfig.getWhiteList()) {
            if (pathMatcher.match(pattern, path)) {
                return chain.filter(exchange);
            }
        }

        try {
            StpUtil.checkLogin();

            ServerHttpRequest newRequest = request.mutate()
                    .header(LOGIN_ID_HEADER, String.valueOf(StpUtil.getLoginId()))
                    .build();

            return chain.filter(exchange.mutate().request(newRequest).build());

        } catch (Throwable ex) {
            return Mono.error(ex);
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

}
