package nus.edu.u.framework.security;

import nus.edu.u.framework.security.satoken.StpPermissionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @author Lu Shuwen
 * @date 2025-10-15
 */
@AutoConfiguration
public class SecurityAutoConfiguration {

    @Bean
    public StpPermissionHandler stpPermissionHandler() {
        return new StpPermissionHandler();
    }
}
