package nus.edu.u.wsgateway.runtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalConnectionRegistry {

    /** Per-user channel: a multicast sink + subscriber count */
    private static final class Channel {
        final Sinks.Many<String> sink =
                Sinks.many().multicast().onBackpressureBuffer(1024, false); // bounded buffer
        final AtomicInteger subscribers = new AtomicInteger(0);
    }

    private final Map<String, Channel> channels = new ConcurrentHashMap<>();
    private final ObjectMapper mapper; // for pushObject

    /**
     * Subscribe to a user's outbound stream; auto-refcount + cleanup when last subscriber leaves.
     */
    public Flux<String> stream(String userId) {
        Channel ch =
                channels.computeIfAbsent(
                        userId,
                        k -> {
                            log.info("[WS] create channel for userId={}", k);
                            return new Channel();
                        });

        return ch.sink
                .asFlux()
                .doOnSubscribe(
                        s -> {
                            int n = ch.subscribers.incrementAndGet();
                            log.info("[WS] userId={} subscribers={}", userId, n);
                        })
                .doFinally(
                        sig -> {
                            int n = ch.subscribers.decrementAndGet();
                            log.info(
                                    "[WS] userId={} disconnected (signal={}), subscribers={}",
                                    userId,
                                    sig,
                                    n);
                            if (n <= 0) {
                                // No active subscribers: drop channel to free memory
                                channels.remove(userId);
                                log.info("[WS] remove channel for userId={}", userId);
                            }
                        });
    }

    /** Try to push a JSON string to a user's channel. Returns true if accepted by the sink. */
    public boolean pushJson(String userId, String json) {
        Channel ch = channels.get(userId);
        if (ch == null) {
            log.debug("[WS] push ignored; no channel for userId={}", userId);
            return false;
        }
        Sinks.EmitResult r = ch.sink.tryEmitNext(json);
        if (r.isSuccess()) return true;

        // Handle backpressure/terminal states
        switch (r) {
            case FAIL_OVERFLOW ->
                    log.warn("[WS] buffer overflow for userId={} (drop message)", userId);
            case FAIL_TERMINATED, FAIL_CANCELLED -> {
                channels.remove(userId); // defensive cleanup
                log.debug("[WS] sink terminated for userId={} (removed)", userId);
            }
            default -> log.debug("[WS] emit result={} for userId={}", r, userId);
        }
        return false;
    }

    /** Convenience: serialize any object as JSON and push. */
    public boolean pushObject(String userId, Object payload) {
        try {
            return pushJson(userId, mapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            log.error("[WS] JSON serialization failed for userId={}: {}", userId, e.getMessage());
            return false;
        }
    }

    /** Is the user currently “online” (has at least one subscriber)? */
    public boolean hasUser(String userId) {
        Channel ch = channels.get(userId);
        return ch != null && ch.subscribers.get() > 0;
    }

    /** For metrics/debugging only */
    public int subscriberCount(String userId) {
        Channel ch = channels.get(userId);
        return ch == null ? 0 : ch.subscribers.get();
    }
}
