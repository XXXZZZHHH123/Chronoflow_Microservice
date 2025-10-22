package nus.edu.u.gateway.auth;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.dev33.satoken.util.SaResult;
import nus.edu.u.gateway.config.AuthConfig;
import org.junit.jupiter.api.Test;

class SaTokenConfigureInvokerTest {

    @Test
    void doAuthLogic_delegates_to_invoker() {
        AuthConfig cfg = new AuthConfig();
        SaTokenConfigure.SaInvoker invoker = mock(SaTokenConfigure.SaInvoker.class);

        SaTokenConfigure cfgObj = new SaTokenConfigure(cfg, invoker);

        cfgObj.doAuthLogic();

        verify(invoker).performAuth();
    }

    @Test
    void handleError_delegates_and_returns_result() {
        AuthConfig cfg = new AuthConfig();
        SaTokenConfigure.SaInvoker invoker = mock(SaTokenConfigure.SaInvoker.class);
        when(invoker.handleError(any(Throwable.class))).thenReturn(SaResult.error("x"));

        SaTokenConfigure cfgObj = new SaTokenConfigure(cfg, invoker);

        SaResult res = cfgObj.handleError(new RuntimeException("x"));

        verify(invoker).handleError(any(Throwable.class));
        // We don't assert equals on SaResult deep equality; just check non-null
        org.junit.jupiter.api.Assertions.assertNotNull(res);
    }
}
