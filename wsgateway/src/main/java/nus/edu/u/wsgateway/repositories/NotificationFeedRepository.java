package nus.edu.u.wsgateway.repositories;

import java.time.Instant;
import nus.edu.u.wsgateway.domain.NotificationFeedDoc;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationFeedRepository
        extends ReactiveMongoRepository<NotificationFeedDoc, String> {

    Mono<NotificationFeedDoc> findByUserIdAndEventId(String userId, String eventId);

    Flux<NotificationFeedDoc> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Flux<NotificationFeedDoc> findByUserIdAndCreatedAtLessThanOrderByCreatedAtDesc(
            String userId, Instant before, Pageable pageable);

    Mono<Long> countByUserIdAndOpenedAtIsNull(String userId);
}
