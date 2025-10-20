package nus.edu.u.wsgateway.socket;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.wsgateway.runtime.LocalConnectionRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class WsHandler implements WebSocketHandler {

    private final LocalConnectionRegistry registry;

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        URI uri = session.getHandshakeInfo().getUri();
        String userId =
                UriComponentsBuilder.fromUri(uri).build().getQueryParams().getFirst("userId");

        if (userId == null || userId.isBlank()) {
            log.warn("[WS] missing userId query param for session {}", session.getId());
            return session.close(CloseStatus.BAD_DATA);
        }

        log.info("[WS] connect userId={}, session={}", userId, session.getId());

        // Outbound: stream messages from the user's sink to this session
        var outbound = registry.stream(userId).map(session::textMessage);

        // Inbound: optional logging / simple ping
        var inbound =
                session.receive()
                        .doOnNext(
                                msg -> {
                                    WebSocketMessage.Type type = msg.getType();
                                    if (type == WebSocketMessage.Type.TEXT) {
                                        String text = msg.getPayloadAsText();
                                        if ("ping".equalsIgnoreCase(text)) {
                                            session.send(Mono.just(session.textMessage("pong")))
                                                    .subscribe(
                                                            null,
                                                            ex ->
                                                                    log.debug(
                                                                            "[WS] pong send failed to {}: {}",
                                                                            session.getId(),
                                                                            ex.toString()));
                                        } else {
                                            log.debug(
                                                    "[WS] inbound text {} -> {}",
                                                    session.getId(),
                                                    text);
                                        }
                                    } else {
                                        log.trace(
                                                "[WS] ignoring {} from {}", type, session.getId());
                                    }
                                })
                        .onErrorResume(
                                ex -> {
                                    log.warn(
                                            "[WS] receive error session {}: {}",
                                            session.getId(),
                                            ex.toString());
                                    return Mono.empty();
                                })
                        .then();

        // Keep both directions alive; channel cleanup is handled by registry.stream() ref-count
        return Mono.when(session.send(outbound), inbound)
                .doFinally(
                        sig ->
                                log.info(
                                        "[WS] disconnect userId={}, session={}, signal={}",
                                        userId,
                                        session.getId(),
                                        sig));
    }
}
