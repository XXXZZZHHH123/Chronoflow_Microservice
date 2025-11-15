package nus.edu.u.services.idempotency;

import java.time.Duration;

public interface IdempotencyService {
    boolean tryClaim(String key, Duration ttl);
}
