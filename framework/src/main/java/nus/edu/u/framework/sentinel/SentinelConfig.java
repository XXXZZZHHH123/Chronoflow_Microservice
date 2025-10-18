package nus.edu.u.framework.sentinel;

import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlBlockHandler;
import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlCleaner;
import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class SentinelConfig {

    private final SentinelExceptionHandler sentinelExceptionHandler;

    public SentinelConfig(SentinelExceptionHandler sentinelExceptionHandler) {
        this.sentinelExceptionHandler = sentinelExceptionHandler;
    }

    @PostConstruct
    public void init() {
        WebCallbackManager.setUrlBlockHandler((UrlBlockHandler) sentinelExceptionHandler);
    }

    @Bean
    public SentinelExceptionHandler sentinelExceptionHandler() {
        return new SentinelExceptionHandler();
    }
    @Bean
    public UrlCleaner sentinelUrlCleaner() {
        return new SentinelUrlCleaner();
    }


}
