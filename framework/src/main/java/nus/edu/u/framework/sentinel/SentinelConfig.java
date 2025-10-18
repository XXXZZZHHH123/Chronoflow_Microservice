package nus.edu.u.framework.sentinel;

import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlBlockHandler;
import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.callback.BlockExceptionHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;

@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
public class SentinelConfig {
    private final BlockExceptionHandler sentinelExceptionHandler;
    private final SentinelUrlCleaner sentinelUrlCleaner;

    @PostConstruct
    public void init() {
        WebCallbackManager.setUrlCleaner(sentinelUrlCleaner);

        log.info("Sentinel framework configuration completed");
    }
}