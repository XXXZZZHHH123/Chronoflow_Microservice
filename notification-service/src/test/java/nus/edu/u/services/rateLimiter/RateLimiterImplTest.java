package nus.edu.u.services.rateLimiter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RateLimiterImplTest {

    @Mock private StringRedisTemplate redis;
    @Mock private ValueOperations<String, String> valueOps;

    private RateLimiterImpl rateLimiter;

    @BeforeEach
    void setUp() {
        when(redis.opsForValue()).thenReturn(valueOps);
        rateLimiter = new RateLimiterImpl(redis);
    }

    @Test
    void allowFirstHitSetsExpiryAndAllows() {
        when(valueOps.increment("key")).thenReturn(1L);

        boolean allowed = rateLimiter.allow("key", 5, Duration.ofSeconds(30));

        assertThat(allowed).isTrue();
        verify(redis).expire("key", Duration.ofSeconds(30));
    }

    @Test
    void allowSubsequentHitsWithinLimit() {
        when(valueOps.increment("key")).thenReturn(3L);

        boolean allowed = rateLimiter.allow("key", 5, Duration.ofSeconds(30));

        assertThat(allowed).isTrue();
        verify(redis, never()).expire(eq("key"), eq(Duration.ofSeconds(30)));
    }

    @Test
    void denyWhenLimitExceeded() {
        when(valueOps.increment("key")).thenReturn(6L);

        boolean allowed = rateLimiter.allow("key", 5, Duration.ofSeconds(30));

        assertThat(allowed).isFalse();
    }
}
