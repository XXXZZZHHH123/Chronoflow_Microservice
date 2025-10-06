package nus.edu.u.framework.web;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @author Lu Shuwen
 * @date 2025-10-06
 */
@AutoConfiguration
public class WebAutoConfiguration {

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
