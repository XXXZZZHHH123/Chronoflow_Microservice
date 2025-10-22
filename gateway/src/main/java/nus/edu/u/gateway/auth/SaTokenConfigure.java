package nus.edu.u.gateway.auth;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.gateway.config.AuthConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Lu Shuwen
 * @date 2025-10-05
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SaTokenConfigure {

    private final AuthConfig authConfig;

    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                .addInclude("/**")
                .addExclude(authConfig.getWhiteList().toArray(new String[0]))
                .setAuth(
                        obj -> {
                            SaRouter.notMatch(SaHttpMethod.OPTIONS).free(r -> StpUtil.checkLogin());
                            // 校验权限 SaRouter.match("/api/test1", r ->
                            // StpUtil.checkPermission("api.test1"));
                            // TODO 拆分stater SaRouter.match("/actuator/**", r ->
                            // StpUtil.checkRole("ADMIN"));
                        })
                .setError(
                        e -> {
                            SaRequest request = SaHolder.getRequest();
                            log.info(
                                    "拦截请求: {} {} {}",
                                    request.getMethod(),
                                    request.getUrl(),
                                    e.getMessage());
                            return SaResult.error(e.getMessage());
                        });
    }
}
