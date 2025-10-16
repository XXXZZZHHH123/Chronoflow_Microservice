package nus.edu.u.gateway.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Lu Shuwen
 * @date 2025-10-05
 */
@Component
@ConfigurationProperties(prefix = "gateway")
@Data
public class AuthConfig {

    private List<String> whiteList = new ArrayList<>();
}
