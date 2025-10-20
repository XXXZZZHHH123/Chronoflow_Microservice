package nus.edu.u.configuration.push;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "notification.push")
public class PushLimitPropertiesConfig {
    /** e.g. "rate:push:global" */
    private String rateKey = "rate:push:global";
    /** max messages per window */
    private int rateLimit = 200;
    /** window length, e.g. PT1M (1 minute) */
    private Duration rateWindow = Duration.ofMinutes(1);
}