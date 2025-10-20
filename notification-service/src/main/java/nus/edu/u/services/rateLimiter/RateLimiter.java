package nus.edu.u.services.rateLimiter;

import java.time.Duration;

public interface RateLimiter {
    boolean allow(String key, int limit, Duration window);
}
