package nus.edu.u.wsgateway.service;

import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.wsgateway.domain.NotificationFeedDoc;
import nus.edu.u.wsgateway.dto.WsPushRequestDTO;
import nus.edu.u.wsgateway.repositories.NotificationFeedRepository;
import nus.edu.u.wsgateway.runtime.LocalConnectionRegistry;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedAndPushService {

    private final NotificationFeedRepository repo;
    private final LocalConnectionRegistry registry;

    /**
     * Upsert feed entry (by userId+eventId) and push to online subscribers. - If the pair exists:
     * re-push (touch) and return existing. - Else: create, push, and return new.
     */
    public Mono<NotificationFeedDoc> createOrTouchAndPush(WsPushRequestDTO req) {
        return repo.findByUserIdAndEventId(req.getUserId(), req.getEventId())
                .flatMap(
                        existing -> {
                            // Optional: update mutable fields if provided
                            boolean changed = false;
                            if (req.getTitle() != null
                                    && !req.getTitle().equals(existing.getTitle())) {
                                existing.setTitle(req.getTitle());
                                changed = true;
                            }
                            if (req.getBody() != null
                                    && !req.getBody().equals(existing.getBody())) {
                                existing.setBody(req.getBody());
                                changed = true;
                            }
                            if (req.getType() != null
                                    && !req.getType().equals(existing.getType())) {
                                existing.setType(req.getType());
                                changed = true;
                            }
                            if (req.getData() != null
                                    && !req.getData().equals(existing.getData())) {
                                existing.setData(req.getData());
                                changed = true;
                            }

                            existing.setUpdatedAt(Instant.now());

                            Mono<NotificationFeedDoc> afterSave =
                                    changed ? repo.save(existing) : Mono.just(existing);

                            return afterSave.flatMap(doc -> pushIfOnline(doc, req).thenReturn(doc));
                        })
                .switchIfEmpty(
                        repo.save(
                                        NotificationFeedDoc.builder()
                                                .userId(req.getUserId())
                                                .eventId(req.getEventId())
                                                .type(req.getType())
                                                .title(req.getTitle())
                                                .body(req.getBody())
                                                .data(
                                                        req.getData() == null
                                                                ? Map.of()
                                                                : req.getData())
                                                .createdAt(Instant.now())
                                                .updatedAt(Instant.now())
                                                .build())
                                .flatMap(doc -> pushIfOnline(doc, req).thenReturn(doc)));
    }

    /**
     * If the user has an active sink, emit the payload. On first successful emission for a given
     * document, best-effort mark deliveredAt.
     */
    private Mono<Void> pushIfOnline(NotificationFeedDoc doc, WsPushRequestDTO req) {
        final String userId = req.getUserId();

        // Fast check: if no subscribers, skip work.
        if (!registry.hasUser(userId)) {
            return Mono.empty();
        }

        // Try to emit to the user's sink (non-blocking, backpressure-aware).
        boolean pushed = registry.pushObject(userId, req);
        if (!pushed) {
            // Could be overflow/terminated/no channel at this instant.
            // We simply skip deliveredAt in that case.
            return Mono.empty();
        }

        // Mark deliveredAt once (best-effort). Ignore races/errors.
        if (doc.getDeliveredAt() == null) {
            doc.setDeliveredAt(Instant.now());
            return repo.save(doc)
                    .doOnError(
                            e ->
                                    log.debug(
                                            "[WS] deliveredAt save failed for {}: {}",
                                            doc.getId(),
                                            e.toString()))
                    .onErrorResume(e -> Mono.empty())
                    .then();
        }

        return Mono.empty();
    }

    /** Page ordered by createdAt desc; optional 'before' cursor; hard cap limit to [1..100]. */
    public Flux<NotificationFeedDoc> page(String userId, int limit, Instant before) {
        var page = PageRequest.of(0, Math.max(1, Math.min(limit, 100)));
        return (before == null)
                ? repo.findByUserIdOrderByCreatedAtDesc(userId, page)
                : repo.findByUserIdAndCreatedAtLessThanOrderByCreatedAtDesc(userId, before, page);
    }

    public Mono<Long> unreadCount(String userId) {
        return repo.countByUserIdAndOpenedAtIsNull(userId);
    }

    public Mono<Long> markSeen(String userId, Iterable<String> ids) {
        return Flux.fromIterable(ids)
                .flatMap(
                        id ->
                                repo.findById(id)
                                        .filter(
                                                doc ->
                                                        userId.equals(doc.getUserId())
                                                                && doc.getSeenAt() == null)
                                        .flatMap(
                                                doc -> {
                                                    doc.setSeenAt(Instant.now());
                                                    return repo.save(doc);
                                                }))
                .count();
    }

    public Mono<Long> markOpened(String userId, Iterable<String> ids) {
        return Flux.fromIterable(ids)
                .flatMap(
                        id ->
                                repo.findById(id)
                                        .filter(
                                                doc ->
                                                        userId.equals(doc.getUserId())
                                                                && doc.getOpenedAt() == null)
                                        .flatMap(
                                                doc -> {
                                                    doc.setOpenedAt(Instant.now());
                                                    return repo.save(doc);
                                                }))
                .count();
    }
}
