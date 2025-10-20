package nus.edu.u.framework.sentinel;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
public class SentinelConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public SentinelUrlCleaner sentinelUrlCleaner() {
        return new SentinelUrlCleaner();
    }

    @Bean
    public SentinelExceptionHandler sentinelExceptionHandler() {
        return new SentinelExceptionHandler(objectMapper);
    }
}
