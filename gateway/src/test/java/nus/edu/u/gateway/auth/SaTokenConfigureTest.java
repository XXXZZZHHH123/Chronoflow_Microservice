package nus.edu.u.gateway.auth;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import nus.edu.u.gateway.config.AuthConfig;
import org.junit.jupiter.api.Test;

class SaTokenConfigureTest {

    @Test
    void getSaReactorFilter_createsFilter() {
        AuthConfig cfg = new AuthConfig();
        cfg.setWhiteList(List.of("/health", "/actuator"));

        SaTokenConfigure c = new SaTokenConfigure(cfg);

        assertNotNull(c.getSaReactorFilter());
        String[] excludes = c.buildExcludes();
        org.junit.jupiter.api.Assertions.assertArrayEquals(
                new String[] {"/health", "/actuator"}, excludes);
    }
}
