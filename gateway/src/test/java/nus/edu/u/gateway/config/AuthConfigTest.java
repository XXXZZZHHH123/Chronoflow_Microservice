package nus.edu.u.gateway.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class AuthConfigTest {

    @Test
    void whiteList_setter_getter() {
        AuthConfig cfg = new AuthConfig();
        cfg.setWhiteList(List.of("/health", "/actuator"));

        assertEquals(2, cfg.getWhiteList().size());
        assertEquals("/health", cfg.getWhiteList().get(0));
    }
}
