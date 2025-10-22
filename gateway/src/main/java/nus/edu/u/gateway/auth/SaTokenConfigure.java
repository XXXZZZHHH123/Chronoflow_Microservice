package nus.edu.u.gateway.auth;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.gateway.config.AuthConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Lu Shuwen
 * @date 2025-10-05
 */
@Configuration
@Slf4j
public class SaTokenConfigure {

    private final AuthConfig authConfig;

    // Delegates Sa-Token static calls to an invoker to make logic testable
    private SaInvoker saInvoker;

    // Default constructor used by Spring
    public SaTokenConfigure(AuthConfig authConfig) {
        this.authConfig = authConfig;
        this.saInvoker = new DefaultSaInvoker();
    }

    // Package-private constructor for tests to inject a mock invoker
    SaTokenConfigure(AuthConfig authConfig, SaInvoker saInvoker) {
        this.authConfig = authConfig;
        this.saInvoker = saInvoker;
    }

    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                .addInclude("/**")
                .addExclude(buildExcludes())
                .setAuth(obj -> doAuthLogic())
                .setError(e -> handleError(e));
    }

    // package-private for testing
    String[] buildExcludes() {
        return authConfig.getWhiteList().toArray(new String[0]);
    }

    // package-private for testing: perform the auth logic
    void doAuthLogic() {
        saInvoker.performAuth();
    }

    // package-private for testing: handle error
    SaResult handleError(Throwable e) {
        return saInvoker.handleError(e);
    }

    // package-private invoker interface for delegating static Sa-Token calls
    interface SaInvoker {
        void performAuth();

        SaResult handleError(Throwable e);
    }

    // default implementation using Sa-Token static APIs
    static class DefaultSaInvoker implements SaInvoker {

        @Override
        public void performAuth() {
            SaRouter.notMatch(SaHttpMethod.OPTIONS).free(r -> StpUtil.checkLogin());
        }

        @Override
        public SaResult handleError(Throwable e) {
            SaRequest request = SaHolder.getRequest();
            log.info("拦截请求: {} {} {}", request.getMethod(), request.getUrl(), e.getMessage());
            return SaResult.error(e.getMessage());
        }
    }
}
